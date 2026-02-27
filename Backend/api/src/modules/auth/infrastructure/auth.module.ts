import { Module } from '@nestjs/common';
import { JwtModule, JwtModuleOptions } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UsersModule } from '../../users/infrastructure/users.module';
import { AuthController } from '../presentation/auth.controller';
import { RegisterUseCase } from '../application/use-cases/register.use-case';
import { LoginUseCase } from '../application/use-cases/login.use-case';
import { GoogleLoginUseCase } from '../application/use-cases/google-login.use-case';
import { JwtStrategy } from './strategies/jwt.strategy';
import { GoogleTokenVerifierService } from './services/google-token-verifier.service';
import { GoogleUserDataOrmEntity } from './persistence/google-user-data.orm-entity';
import { GoogleUserDataRepository } from './persistence/google-user-data.repository';
import { GOOGLE_USER_DATA_REPOSITORY } from '../domain/repositories/google-user-data.repository.interface';

@Module({
  imports: [
    UsersModule,
    TypeOrmModule.forFeature([GoogleUserDataOrmEntity]),
    PassportModule.register({ defaultStrategy: 'jwt' }),
    JwtModule.registerAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (configService: ConfigService): JwtModuleOptions => ({
        secret: configService.get<string>('JWT_SECRET') ?? '',
        signOptions: {
          expiresIn: (configService.get<string>('JWT_EXPIRES_IN') ??
            '7d') as any,
        },
      }),
    }),
  ],
  controllers: [AuthController],
  providers: [
    RegisterUseCase,
    LoginUseCase,
    GoogleLoginUseCase,
    JwtStrategy,
    GoogleTokenVerifierService,
    {
      provide: GOOGLE_USER_DATA_REPOSITORY,
      useClass: GoogleUserDataRepository,
    },
  ],
})
export class AuthModule {}
