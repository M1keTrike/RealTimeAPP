import { Test, TestingModule } from '@nestjs/testing';
import { ConfigService } from '@nestjs/config';
import { JwtStrategy } from '../infrastructure/strategies/jwt.strategy';
import { MOCK_USER_ID } from '../../users/__tests__/user.fixtures';

describe('JwtStrategy', () => {
  let strategy: JwtStrategy;

  const mockConfigService = {
    get: jest.fn((key: string) => {
      if (key === 'JWT_SECRET') return 'test-secret';
      return undefined;
    }),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        JwtStrategy,
        { provide: ConfigService, useValue: mockConfigService },
      ],
    }).compile();

    strategy = module.get<JwtStrategy>(JwtStrategy);
  });

  describe('validate', () => {
    it('debe retornar el objeto de usuario a partir del payload JWT', async () => {
      const payload = {
        sub: MOCK_USER_ID,
        email: 'test@example.com',
        username: 'testuser',
        role: 'PLAYER',
      };

      const result = await strategy.validate(payload);

      expect(result).toEqual({
        userId: MOCK_USER_ID,
        email: 'test@example.com',
        username: 'testuser',
        role: 'PLAYER',
      });
    });

    it('debe mapear el campo sub del payload al campo userId del resultado', async () => {
      const payload = {
        sub: MOCK_USER_ID,
        email: 'otro@example.com',
        username: 'otrouser',
        role: 'PLAYER',
      };

      const result = await strategy.validate(payload);

      expect(result.userId).toBe(MOCK_USER_ID);
      expect((result as any).sub).toBeUndefined();
    });
  });
});
