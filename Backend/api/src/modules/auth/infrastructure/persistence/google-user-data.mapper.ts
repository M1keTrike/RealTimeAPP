import { GoogleUserData } from '../../domain/entities/google-user-data.entity';
import { GoogleUserDataOrmEntity } from './google-user-data.orm-entity';

export class GoogleUserDataMapper {
  static toDomain(ormEntity: GoogleUserDataOrmEntity): GoogleUserData {
    return new GoogleUserData(
      ormEntity.id,
      ormEntity.userId,
      ormEntity.googleId,
      ormEntity.picture,
      ormEntity.locale,
      ormEntity.createdAt,
      ormEntity.updatedAt,
    );
  }

  static toPersistence(domainEntity: GoogleUserData): GoogleUserDataOrmEntity {
    const ormEntity = new GoogleUserDataOrmEntity();
    ormEntity.id = domainEntity.id;
    ormEntity.userId = domainEntity.userId;
    ormEntity.googleId = domainEntity.googleId;
    ormEntity.picture = domainEntity.picture;
    ormEntity.locale = domainEntity.locale;
    ormEntity.createdAt = domainEntity.createdAt;
    ormEntity.updatedAt = domainEntity.updatedAt;
    return ormEntity;
  }
}
