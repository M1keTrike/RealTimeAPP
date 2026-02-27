import { User, UserRole, AuthProvider } from '../domain/entities/user.entity';
import { UserOrmEntity } from '../infrastructure/persistence/user.orm-entity';

export const MOCK_USER_ID = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
export const MOCK_DATE = new Date('2024-01-01T00:00:00.000Z');
const MOCK_ROLE: UserRole = UserRole.PLAYER;
export const MOCK_PASSWORD_HASH =
  '$2b$10$abcdefghijklmnopqrstuuHASHEDPASSWORDMOCK';

export const mockUser = (): User =>
  new User(
    MOCK_USER_ID,
    'testuser',
    'test@example.com',
    MOCK_PASSWORD_HASH,
    1200,
    MOCK_ROLE,
    AuthProvider.LOCAL,
    MOCK_DATE,
  );

export const mockUserOrmEntity = (): UserOrmEntity => {
  const orm = new UserOrmEntity();
  orm.id = MOCK_USER_ID;
  orm.username = 'testuser';
  orm.email = 'test@example.com';
  orm.passwordHash = MOCK_PASSWORD_HASH;
  orm.eloRating = 1200;
  orm.role = MOCK_ROLE;
  orm.authProvider = AuthProvider.LOCAL;
  orm.createdAt = MOCK_DATE;
  return orm;
};
