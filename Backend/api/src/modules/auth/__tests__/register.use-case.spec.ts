import { Test, TestingModule } from '@nestjs/testing';
import { ConflictException } from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { RegisterUseCase } from '../application/use-cases/register.use-case';
import { USER_REPOSITORY } from '../../users/domain/repositories/user.repository.interface';
import {
  mockUser,
  MOCK_USER_ID,
  MOCK_PASSWORD_HASH,
} from '../../users/__tests__/user.fixtures';
import { mockRegisterDto } from './auth.fixtures';

jest.mock('bcrypt');
jest.mock('uuid', () => ({
  v4: jest.fn(() => 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
}));

describe('RegisterUseCase', () => {
  let useCase: RegisterUseCase;

  const mockUserRepository = {
    save: jest.fn(),
    findById: jest.fn(),
    findByEmail: jest.fn(),
    findByUsername: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        RegisterUseCase,
        { provide: USER_REPOSITORY, useValue: mockUserRepository },
      ],
    }).compile();

    useCase = module.get<RegisterUseCase>(RegisterUseCase);

    jest.clearAllMocks();
    (bcrypt.hash as jest.Mock).mockResolvedValue(MOCK_PASSWORD_HASH);
  });

  describe('execute', () => {
    it('debe registrar un usuario nuevo y retornarlo con elo por defecto', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(null);
      mockUserRepository.findByUsername.mockResolvedValue(null);
      mockUserRepository.save.mockResolvedValue(undefined);

      const result = await useCase.execute(mockRegisterDto());

      expect(result.id).toBe(MOCK_USER_ID);
      expect(result.username).toBe('testuser');
      expect(result.email).toBe('test@example.com');
      expect(result.passwordHash).toBe(MOCK_PASSWORD_HASH);
      expect(result.eloRating).toBe(1200);
    });

    it('debe llamar a bcrypt.hash con la contraseña y factor de costo 10', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(null);
      mockUserRepository.findByUsername.mockResolvedValue(null);
      mockUserRepository.save.mockResolvedValue(undefined);

      await useCase.execute(mockRegisterDto());

      expect(bcrypt.hash).toHaveBeenCalledWith('password123', 10);
    });

    it('debe persistir el usuario una sola vez', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(null);
      mockUserRepository.findByUsername.mockResolvedValue(null);
      mockUserRepository.save.mockResolvedValue(undefined);

      await useCase.execute(mockRegisterDto());

      expect(mockUserRepository.save).toHaveBeenCalledTimes(1);
    });

    it('debe lanzar ConflictException si el email ya está registrado', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(mockUser());

      await expect(useCase.execute(mockRegisterDto())).rejects.toThrow(
        new ConflictException('El email ya está registrado.'),
      );
    });

    it('no debe guardar ni hashear si el email ya existe', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(mockUser());

      await expect(useCase.execute(mockRegisterDto())).rejects.toThrow(
        ConflictException,
      );

      expect(bcrypt.hash).not.toHaveBeenCalled();
      expect(mockUserRepository.save).not.toHaveBeenCalled();
    });

    it('debe lanzar ConflictException si el username ya está en uso', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(null);
      mockUserRepository.findByUsername.mockResolvedValue(mockUser());

      await expect(useCase.execute(mockRegisterDto())).rejects.toThrow(
        new ConflictException('El username ya está en uso.'),
      );
    });

    it('no debe guardar si el username ya existe', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(null);
      mockUserRepository.findByUsername.mockResolvedValue(mockUser());

      await expect(useCase.execute(mockRegisterDto())).rejects.toThrow(
        ConflictException,
      );

      expect(mockUserRepository.save).not.toHaveBeenCalled();
    });
  });
});
