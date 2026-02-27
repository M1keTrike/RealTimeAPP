import { Injectable, Inject, ConflictException } from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { v4 as uuidv4 } from 'uuid';
import { USER_REPOSITORY } from '../../../users/domain/repositories/user.repository.interface';
import type { IUserRepository } from '../../../users/domain/repositories/user.repository.interface';
import { User } from '../../../users/domain/entities/user.entity';
import { RegisterDto } from '../dtos/register.dto';

@Injectable()
export class RegisterUseCase {
  constructor(
    @Inject(USER_REPOSITORY)
    private readonly userRepo: IUserRepository,
  ) {}

  async execute(dto: RegisterDto): Promise<User> {
    const existingByEmail = await this.userRepo.findByEmail(dto.email);
    if (existingByEmail) {
      throw new ConflictException('El email ya está registrado.');
    }

    const existingByUsername = await this.userRepo.findByUsername(dto.username);
    if (existingByUsername) {
      throw new ConflictException('El username ya está en uso.');
    }

    const passwordHash = await bcrypt.hash(dto.password, 10);
    const user = new User(uuidv4(), dto.username, dto.email, passwordHash);

    await this.userRepo.save(user);
    return user;
  }
}
