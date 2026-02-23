import { RegisterDto } from '../application/dtos/register.dto';
import { LoginDto } from '../application/dtos/login.dto';

export const mockRegisterDto = (): RegisterDto =>
  ({
    username: 'testuser',
    email: 'test@example.com',
    password: 'password123',
  }) as RegisterDto;

export const mockLoginDto = (): LoginDto =>
  ({
    email: 'test@example.com',
    password: 'password123',
  }) as LoginDto;
