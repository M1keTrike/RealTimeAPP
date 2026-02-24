import { Controller, Post, Body } from '@nestjs/common';
import { RegisterUseCase } from '../application/use-cases/register.use-case';
import { LoginUseCase } from '../application/use-cases/login.use-case';
import { RegisterDto } from '../application/dtos/register.dto';
import { LoginDto } from '../application/dtos/login.dto';
import { ApiTags } from '@nestjs/swagger/dist/decorators/api-use-tags.decorator';
import { ApiResponse } from '@nestjs/swagger/dist/decorators/api-response.decorator';
import { ApiOperation } from '@nestjs/swagger/dist/decorators/api-operation.decorator';

// TODO: Activar cuando el servicio de Google OAuth esté configurado.
// import { Get, Req, UseGuards } from '@nestjs/common';
// import { AuthGuard } from '@nestjs/passport';
@ApiTags('Auth (Autenticación)')
@Controller('auth')
export class AuthController {
  constructor(
    private readonly registerUseCase: RegisterUseCase,
    private readonly loginUseCase: LoginUseCase,
  ) {}

  @Post('register')
  @ApiOperation({ summary: 'Registrar un nuevo jugador en la plataforma' })
  @ApiResponse({ status: 201, description: 'Usuario registrado exitosamente.' })
  @ApiResponse({ status: 400, description: 'Datos inválidos o el correo ya está en uso.' })
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
  @ApiResponse({ status: 200, description: 'Inicio de sesión exitoso. Devuelve el token JWT.' })
  @ApiResponse({ status: 401, description: 'Credenciales inválidas (correo o contraseña incorrectos).' })
  async login(@Body() dto: LoginDto) {
    const result = await this.loginUseCase.execute(dto);
    return {
      success: true,
      data: result,
    };
  }

  // TODO: Activar cuando el servicio de Google OAuth esté configurado.
  // @Get('google')
  // @UseGuards(AuthGuard('google'))
  // googleLogin() {}

  // @Get('google/callback')
  // @UseGuards(AuthGuard('google'))
  // googleCallback(@Req() req) {
  //   return this.googleLoginUseCase.execute(req.user);
  // }
}
