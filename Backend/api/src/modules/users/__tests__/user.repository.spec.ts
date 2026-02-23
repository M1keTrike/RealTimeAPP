import { Test, TestingModule } from '@nestjs/testing';
import { getRepositoryToken } from '@nestjs/typeorm';
import { UserRepository } from '../infrastructure/persistence/user.repository';
import { UserOrmEntity } from '../infrastructure/persistence/user.orm-entity';
import { mockUser, mockUserOrmEntity, MOCK_USER_ID } from './user.fixtures';

describe('UserRepository', () => {
  let userRepository: UserRepository;

  const mockOrmRepo = {
    save: jest.fn(),
    findOneBy: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        UserRepository,
        {
          provide: getRepositoryToken(UserOrmEntity),
          useValue: mockOrmRepo,
        },
      ],
    }).compile();

    userRepository = module.get<UserRepository>(UserRepository);
    jest.clearAllMocks();
  });

  describe('save', () => {
    it('debe llamar a ormRepo.save con el entity mapeado', async () => {
      mockOrmRepo.save.mockResolvedValue(undefined);

      await userRepository.save(mockUser());

      expect(mockOrmRepo.save).toHaveBeenCalledTimes(1);
      expect(mockOrmRepo.save).toHaveBeenCalledWith(
        expect.objectContaining({
          id: MOCK_USER_ID,
          username: 'testuser',
          email: 'test@example.com',
        }),
      );
    });
  });

  describe('findById', () => {
    it('debe retornar un User cuando el id existe', async () => {
      mockOrmRepo.findOneBy.mockResolvedValue(mockUserOrmEntity());

      const result = await userRepository.findById(MOCK_USER_ID);

      expect(result).not.toBeNull();
      expect(result!.id).toBe(MOCK_USER_ID);
      expect(mockOrmRepo.findOneBy).toHaveBeenCalledWith({ id: MOCK_USER_ID });
    });

    it('debe retornar null cuando el id no existe', async () => {
      mockOrmRepo.findOneBy.mockResolvedValue(null);

      const result = await userRepository.findById('id-inexistente');

      expect(result).toBeNull();
    });
  });

  describe('findByEmail', () => {
    it('debe retornar un User cuando el email existe', async () => {
      mockOrmRepo.findOneBy.mockResolvedValue(mockUserOrmEntity());

      const result = await userRepository.findByEmail('test@example.com');

      expect(result).not.toBeNull();
      expect(result!.email).toBe('test@example.com');
      expect(mockOrmRepo.findOneBy).toHaveBeenCalledWith({
        email: 'test@example.com',
      });
    });

    it('debe retornar null cuando el email no existe', async () => {
      mockOrmRepo.findOneBy.mockResolvedValue(null);

      const result = await userRepository.findByEmail('noexiste@example.com');

      expect(result).toBeNull();
    });
  });

  describe('findByUsername', () => {
    it('debe retornar un User cuando el username existe', async () => {
      mockOrmRepo.findOneBy.mockResolvedValue(mockUserOrmEntity());

      const result = await userRepository.findByUsername('testuser');

      expect(result).not.toBeNull();
      expect(result!.username).toBe('testuser');
      expect(mockOrmRepo.findOneBy).toHaveBeenCalledWith({
        username: 'testuser',
      });
    });

    it('debe retornar null cuando el username no existe', async () => {
      mockOrmRepo.findOneBy.mockResolvedValue(null);

      const result = await userRepository.findByUsername('noexiste');

      expect(result).toBeNull();
    });
  });
});
