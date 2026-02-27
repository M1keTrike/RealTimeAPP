import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PassportModule } from '@nestjs/passport';
import { UserOrmEntity } from './persistence/user.orm-entity';
import { UserRepository } from './persistence/user.repository';
import { USER_REPOSITORY } from '../domain/repositories/user.repository.interface';
import { UsersController } from '../presentation/users.controller';
import { UpdateEloUseCase } from '../application/use-cases/update-elo.use-case';

@Module({
  imports: [
    TypeOrmModule.forFeature([UserOrmEntity]),
    PassportModule.register({ defaultStrategy: 'jwt' }),
  ],
  controllers: [UsersController],
  providers: [
    UpdateEloUseCase,
    {
      provide: USER_REPOSITORY,
      useClass: UserRepository,
    },
  ],
  exports: [USER_REPOSITORY],
})
export class UsersModule {}
