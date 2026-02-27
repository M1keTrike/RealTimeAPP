import { Controller, Post, Body } from '@nestjs/common';
import { RegisterUseCase } from '../application/use-cases/register.use-case';
import { LoginUseCase } from '../application/use-cases/login.use-case';
import { GoogleLoginUseCase } from '../application/use-cases/google-login.use-case';
import { RegisterDto } from '../application/dtos/register.dto';
import { LoginDto } from '../application/dtos/login.dto';
import { GoogleLoginDto } from '../application/dtos/google-login.dto';
import { ApiTags } from '@nestjs/swagger/dist/decorators/api-use-tags.decorator';
import { ApiResponse } from '@nestjs/swagger/dist/decorators/api-response.decorator';
import { ApiOperation } from '@nestjs/swagger/dist/decorators/api-operation.decorator';

@ApiTags('Auth (Autenticación)')
@Controller('auth')
export class AuthController {
  constructor(
    private readonly registerUseCase: RegisterUseCase,
    private readonly loginUseCase: LoginUseCase,
    private readonly googleLoginUseCase: GoogleLoginUseCase,
  ) {}

  @Post('register')
  @ApiOperation({ summary: 'Registrar un nuevo jugador en la plataforma' })
  @ApiResponse({ status: 201, description: 'Usuario registrado exitosamente.' })
  @ApiResponse({
    status: 400,
    description: 'Datos inválidos o el correo ya está en uso.',
  })
  async register(@Body() dto: RegisterDto) {
    const user = await this.registerUseCase.execute(dto);
    return {
      success: true,
      data: {
        id: user.id,
        username: user.username,
        email: user.email,
        eloRating: user.eloRating,
      },
    };
  }

  @Post('login')
  @ApiOperation({ summary: 'Iniciar sesión y obtener el token JWT' })
  @ApiResponse({
    status: 200,
    description: 'Inicio de sesión exitoso. Devuelve el token JWT.',
  })
  @ApiResponse({
    status: 401,
    description: 'Credenciales inválidas (correo o contraseña incorrectos).',
  })
  async login(@Body() dto: LoginDto) {
    const result = await this.loginUseCase.execute(dto);
    return {
      success: true,
      data: result,
    };
  }

  @Post('google')
  @ApiOperation({
    summary:
      'Iniciar sesión o registrarse con Google (idToken de Firebase/Google Sign-In)',
  })
  @ApiResponse({
    status: 200,
    description:
      'Login/registro exitoso. Devuelve el token JWT y datos del usuario.',
  })
  @ApiResponse({
    status: 401,
    description: 'El idToken de Google es inválido o ha expirado.',
  })
  async googleLogin(@Body() dto: GoogleLoginDto) {
    const result = await this.googleLoginUseCase.execute(dto);
    return {
      success: true,
      data: result,
    };
  }
}
