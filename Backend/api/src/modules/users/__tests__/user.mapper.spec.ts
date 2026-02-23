import { UserMapper } from '../infrastructure/persistence/user.mapper';
import {
  mockUser,
  mockUserOrmEntity,
  MOCK_USER_ID,
  MOCK_DATE,
  MOCK_PASSWORD_HASH,
} from './user.fixtures';

describe('UserMapper', () => {
  describe('toDomain', () => {
    it('debe mapear todos los campos de OrmEntity a User correctamente', () => {
      const ormEntity = mockUserOrmEntity();

      const user = UserMapper.toDomain(ormEntity);

      expect(user.id).toBe(MOCK_USER_ID);
      expect(user.username).toBe('testuser');
      expect(user.email).toBe('test@example.com');
      expect(user.passwordHash).toBe(MOCK_PASSWORD_HASH);
      expect(user.eloRating).toBe(1200);
      expect(user.createdAt).toEqual(MOCK_DATE);
    });

    it('debe preservar el eloRating personalizado', () => {
      const ormEntity = mockUserOrmEntity();
      ormEntity.eloRating = 1500;

      const user = UserMapper.toDomain(ormEntity);

      expect(user.eloRating).toBe(1500);
    });
  });

  describe('toPersistence', () => {
    it('debe mapear todos los campos de User a OrmEntity correctamente', () => {
      const user = mockUser();

      const ormEntity = UserMapper.toPersistence(user);

      expect(ormEntity.id).toBe(MOCK_USER_ID);
      expect(ormEntity.username).toBe('testuser');
      expect(ormEntity.email).toBe('test@example.com');
      expect(ormEntity.passwordHash).toBe(MOCK_PASSWORD_HASH);
      expect(ormEntity.eloRating).toBe(1200);
      expect(ormEntity.createdAt).toEqual(MOCK_DATE);
    });

    it('la conversión toDomain(toPersistence(user)) debe ser idempotente', () => {
      const original = mockUser();

      const ormEntity = UserMapper.toPersistence(original);
      const restored = UserMapper.toDomain(ormEntity);

      expect(restored.id).toBe(original.id);
      expect(restored.username).toBe(original.username);
      expect(restored.email).toBe(original.email);
      expect(restored.passwordHash).toBe(original.passwordHash);
      expect(restored.eloRating).toBe(original.eloRating);
      expect(restored.createdAt).toEqual(original.createdAt);
    });
  });
});
