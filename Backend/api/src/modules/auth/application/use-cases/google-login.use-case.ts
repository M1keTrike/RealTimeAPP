import {
  Injectable,
  Inject,
  Logger,
  InternalServerErrorException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { v4 as uuidv4 } from 'uuid';
import { USER_REPOSITORY } from '../../../users/domain/repositories/user.repository.interface';
import type { IUserRepository } from '../../../users/domain/repositories/user.repository.interface';
import {
  GOOGLE_USER_DATA_REPOSITORY,
  type IGoogleUserDataRepository,
} from '../../domain/repositories/google-user-data.repository.interface';
import {
  User,
  UserRole,
  AuthProvider,
} from '../../../users/domain/entities/user.entity';
import { GoogleUserData } from '../../domain/entities/google-user-data.entity';
import { GoogleTokenVerifierService } from '../../infrastructure/services/google-token-verifier.service';
import { GoogleLoginDto } from '../dtos/google-login.dto';
import type { LoginResponse } from './login.use-case';

@Injectable()
export class GoogleLoginUseCase {
  private readonly logger = new Logger(GoogleLoginUseCase.name);

  constructor(
    @Inject(USER_REPOSITORY)
    private readonly userRepo: IUserRepository,
    @Inject(GOOGLE_USER_DATA_REPOSITORY)
    private readonly googleUserDataRepo: IGoogleUserDataRepository,
    private readonly jwtService: JwtService,
    private readonly googleTokenVerifier: GoogleTokenVerifierService,
  ) {}

  async execute(dto: GoogleLoginDto): Promise<LoginResponse> {
    // 1. Verificar el idToken con Google
    const googlePayload = await this.googleTokenVerifier.verify(dto.idToken);

    // 2. Buscar usuario existente por email
    const existingUser = await this.userRepo.findByEmail(googlePayload.email);

    let user: User;

    if (!existingUser) {
      // CASO A: Usuario nuevo
      user = await this.createNewGoogleUser(googlePayload);
    } else {
      // Verificar si ya tiene GoogleUserData vinculado
      const existingGoogleData = await this.googleUserDataRepo.findByUserId(
        existingUser.id,
      );

      if (existingGoogleData) {
        // CASO B: Ya tiene Google vinculado - actualizar metadatos
        user = existingUser;
        await this.updateGoogleMetadata(existingGoogleData, googlePayload);
      } else {
        // CASO C: Era LOCAL - vincular Google y cambiar a BOTH
        user = await this.linkGoogleToExistingUser(existingUser, googlePayload);
      }
    }

    // 3. Emitir JWT con el mismo payload que LoginUseCase
    const payload = {
      sub: user.id,
      email: user.email,
      username: user.username,
      role: user.role,
    };

    const accessToken = this.jwtService.sign(payload);

    return {
      accessToken,
      id: user.id,
      username: user.username,
      email: user.email,
      eloRating: user.eloRating,
      role: user.role,
      createdAt: user.createdAt,
    };
  }

  /**
   * Caso A: Crea un User nuevo + GoogleUserData.
   * Genera un username unico a partir del nombre o email.
   */
  private async createNewGoogleUser(googlePayload: {
    googleId: string;
    email: string;
    name: string | null;
    picture: string | null;
    locale: string | null;
  }): Promise<User> {
    const username = await this.generateUniqueUsername(
      googlePayload.name,
      googlePayload.email,
    );

    const user = new User(
      uuidv4(),
      username,
      googlePayload.email,
      null,
      1200,
      UserRole.PLAYER,
      AuthProvider.GOOGLE,
    );

    try {
      await this.userRepo.save(user);
    } catch {
      throw new InternalServerErrorException(
        'Error al crear el usuario con Google.',
      );
    }

    const googleData = new GoogleUserData(
      uuidv4(),
      user.id,
      googlePayload.googleId,
      googlePayload.picture,
      googlePayload.locale,
    );

    await this.googleUserDataRepo.save(googleData);

    this.logger.log(
      `Cuenta creada: User [${user.id}] registrado con Google ID [${googlePayload.googleId}] via /auth/google`,
    );

    return user;
  }

  /**
   * Caso B: Actualiza metadatos de un GoogleUserData existente (picture, locale).
   */
  private async updateGoogleMetadata(
    existingGoogleData: GoogleUserData,
    googlePayload: {
      picture: string | null;
      locale: string | null;
    },
  ): Promise<void> {
    existingGoogleData.picture = googlePayload.picture;
    existingGoogleData.locale = googlePayload.locale;
    existingGoogleData.updatedAt = new Date();
    await this.googleUserDataRepo.save(existingGoogleData);
  }

  /**
   * Caso C: Vincula Google a un usuario LOCAL existente.
   * Cambia auth_provider a BOTH y crea GoogleUserData.
   */
  private async linkGoogleToExistingUser(
    existingUser: User,
    googlePayload: {
      googleId: string;
      picture: string | null;
      locale: string | null;
    },
  ): Promise<User> {
    existingUser.authProvider = AuthProvider.BOTH;
    await this.userRepo.save(existingUser);

    const googleData = new GoogleUserData(
      uuidv4(),
      existingUser.id,
      googlePayload.googleId,
      googlePayload.picture,
      googlePayload.locale,
    );

    await this.googleUserDataRepo.save(googleData);

    this.logger.log(
      `Cuenta vinculada: User [${existingUser.id}] asociado a Google ID [${googlePayload.googleId}] via /auth/google`,
    );

    return existingUser;
  }

  /**
   * Genera un username unico basado en el nombre de Google o la parte local del email.
   * Si ya existe, agrega un sufijo aleatorio de 4 caracteres.
   */
  private async generateUniqueUsername(
    name: string | null,
    email: string,
  ): Promise<string> {
    const base = name
      ? name.replace(/\s+/g, '').toLowerCase()
      : email.split('@')[0].toLowerCase();

    const sanitized = base.replace(/[^a-z0-9_]/g, '');

    const candidate = sanitized.length >= 3 ? sanitized : `user_${sanitized}`;

    const existing = await this.userRepo.findByUsername(candidate);
    if (!existing) {
      return candidate;
    }

    const maxAttempts = 5;
    for (let i = 0; i < maxAttempts; i++) {
      const suffix = Math.random().toString(36).substring(2, 6);
      const candidateWithSuffix = `${candidate}_${suffix}`;
      const exists = await this.userRepo.findByUsername(candidateWithSuffix);
      if (!exists) {
        return candidateWithSuffix;
      }
    }

    return `${candidate}_${uuidv4().substring(0, 8)}`;
  }
}
