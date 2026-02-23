import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UserOrmEntity } from './persistence/user.orm-entity';
import { UserRepository } from './persistence/user.repository';
import { USER_REPOSITORY } from '../domain/repositories/user.repository.interface';

@Module({
  imports: [TypeOrmModule.forFeature([UserOrmEntity])],
  providers: [
    {
      provide: USER_REPOSITORY,
      useClass: UserRepository,
    },
  ],
  exports: [USER_REPOSITORY],
})
export class UsersModule {}
