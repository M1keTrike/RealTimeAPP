import {
  User,
  UserRole,
  AuthProvider,
} from '../../domain/entities/user.entity';
import { UserOrmEntity } from './user.orm-entity';

export class UserMapper {
  static toDomain(ormEntity: UserOrmEntity): User {
    return new User(
      ormEntity.id,
      ormEntity.username,
      ormEntity.email,
      ormEntity.passwordHash,
      ormEntity.eloRating,
      ormEntity.role as UserRole,
      ormEntity.authProvider as AuthProvider,
      ormEntity.createdAt,
    );
  }

  static toPersistence(domainEntity: User): UserOrmEntity {
    const ormEntity = new UserOrmEntity();
    ormEntity.id = domainEntity.id;
    ormEntity.username = domainEntity.username;
    ormEntity.email = domainEntity.email;
    ormEntity.passwordHash = domainEntity.passwordHash;
    ormEntity.eloRating = domainEntity.eloRating;
    ormEntity.role = domainEntity.role;
    ormEntity.authProvider = domainEntity.authProvider;
    ormEntity.createdAt = domainEntity.createdAt;
    return ormEntity;
  }
}
