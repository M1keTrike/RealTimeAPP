import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { IUserRepository } from '../../domain/repositories/user.repository.interface';
import { User } from '../../domain/entities/user.entity';
import { UserOrmEntity } from './user.orm-entity';
import { UserMapper } from './user.mapper';

@Injectable()
export class UserRepository implements IUserRepository {
  constructor(
    @InjectRepository(UserOrmEntity)
    private readonly ormRepo: Repository<UserOrmEntity>,
  ) {}

  async save(user: User): Promise<void> {
    const ormEntity = UserMapper.toPersistence(user);
    await this.ormRepo.save(ormEntity);
  }

  async findById(id: string): Promise<User | null> {
    const ormEntity = await this.ormRepo.findOneBy({ id });
    return ormEntity ? UserMapper.toDomain(ormEntity) : null;
  }

  async findByEmail(email: string): Promise<User | null> {
    const ormEntity = await this.ormRepo.findOneBy({ email });
    return ormEntity ? UserMapper.toDomain(ormEntity) : null;
  }

  async findByUsername(username: string): Promise<User | null> {
    const ormEntity = await this.ormRepo.findOneBy({ username });
    return ormEntity ? UserMapper.toDomain(ormEntity) : null;
  }
}
