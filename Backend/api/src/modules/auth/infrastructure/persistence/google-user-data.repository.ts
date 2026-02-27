import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { IGoogleUserDataRepository } from '../../domain/repositories/google-user-data.repository.interface';
import { GoogleUserData } from '../../domain/entities/google-user-data.entity';
import { GoogleUserDataOrmEntity } from './google-user-data.orm-entity';
import { GoogleUserDataMapper } from './google-user-data.mapper';

@Injectable()
export class GoogleUserDataRepository implements IGoogleUserDataRepository {
  constructor(
    @InjectRepository(GoogleUserDataOrmEntity)
    private readonly ormRepo: Repository<GoogleUserDataOrmEntity>,
  ) {}

  async save(data: GoogleUserData): Promise<void> {
    const ormEntity = GoogleUserDataMapper.toPersistence(data);
    await this.ormRepo.save(ormEntity);
  }

  async findByUserId(userId: string): Promise<GoogleUserData | null> {
    const ormEntity = await this.ormRepo.findOneBy({ userId });
    return ormEntity ? GoogleUserDataMapper.toDomain(ormEntity) : null;
  }

  async findByGoogleId(googleId: string): Promise<GoogleUserData | null> {
    const ormEntity = await this.ormRepo.findOneBy({ googleId });
    return ormEntity ? GoogleUserDataMapper.toDomain(ormEntity) : null;
  }
}
