import { Test, TestingModule } from '@nestjs/testing';
import { UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { LoginUseCase } from '../application/use-cases/login.use-case';
import { USER_REPOSITORY } from '../../users/domain/repositories/user.repository.interface';
import {
  mockUser,
  MOCK_USER_ID,
  MOCK_PASSWORD_HASH,
} from '../../users/__tests__/user.fixtures';
import { mockLoginDto } from './auth.fixtures';

jest.mock('bcrypt');

describe('LoginUseCase', () => {
  let useCase: LoginUseCase;

  const mockUserRepository = {
    save: jest.fn(),
    findById: jest.fn(),
    findByEmail: jest.fn(),
    findByUsername: jest.fn(),
  };

  const mockJwtService = {
    sign: jest.fn(() => 'mock.jwt.token'),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        LoginUseCase,
        { provide: USER_REPOSITORY, useValue: mockUserRepository },
        { provide: JwtService, useValue: mockJwtService },
      ],
    }).compile();

    useCase = module.get<LoginUseCase>(LoginUseCase);

    jest.clearAllMocks();
  });

  describe('execute', () => {
    it('debe retornar un accessToken cuando las credenciales son correctas', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(mockUser());
      (bcrypt.compare as jest.Mock).mockResolvedValue(true);
      mockJwtService.sign.mockReturnValue('mock.jwt.token');

      const result = await useCase.execute(mockLoginDto());

      expect(result).toEqual({ accessToken: 'mock.jwt.token' });
    });

    it('debe firmar el JWT con el payload correcto del usuario', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(mockUser());
      (bcrypt.compare as jest.Mock).mockResolvedValue(true);

      await useCase.execute(mockLoginDto());

      expect(mockJwtService.sign).toHaveBeenCalledWith({
        sub: MOCK_USER_ID,
        email: 'test@example.com',
        username: 'testuser',
      });
    });

    it('debe verificar la contraseña contra el hash almacenado', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(mockUser());
      (bcrypt.compare as jest.Mock).mockResolvedValue(true);

      await useCase.execute(mockLoginDto());

      expect(bcrypt.compare).toHaveBeenCalledWith('password123', MOCK_PASSWORD_HASH);
    });

    it('debe lanzar UnauthorizedException si el usuario no existe', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(null);

      await expect(useCase.execute(mockLoginDto())).rejects.toThrow(
        new UnauthorizedException('Credenciales inválidas.'),
      );
    });

    it('no debe llamar a bcrypt ni a jwtService si el usuario no existe', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(null);

      await expect(useCase.execute(mockLoginDto())).rejects.toThrow(
        UnauthorizedException,
      );

      expect(bcrypt.compare).not.toHaveBeenCalled();
      expect(mockJwtService.sign).not.toHaveBeenCalled();
    });

    it('debe lanzar UnauthorizedException si la contraseña es incorrecta', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(mockUser());
      (bcrypt.compare as jest.Mock).mockResolvedValue(false);

      await expect(useCase.execute(mockLoginDto())).rejects.toThrow(
        new UnauthorizedException('Credenciales inválidas.'),
      );
    });

    it('no debe firmar el JWT si la contraseña es incorrecta', async () => {
      mockUserRepository.findByEmail.mockResolvedValue(mockUser());
      (bcrypt.compare as jest.Mock).mockResolvedValue(false);

      await expect(useCase.execute(mockLoginDto())).rejects.toThrow(
        UnauthorizedException,
      );

      expect(mockJwtService.sign).not.toHaveBeenCalled();
    });
  });
});
