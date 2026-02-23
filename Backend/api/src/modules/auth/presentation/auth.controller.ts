import { Controller, Post, Body } from '@nestjs/common';
import { RegisterUseCase } from '../application/use-cases/register.use-case';
import { LoginUseCase } from '../application/use-cases/login.use-case';
import { RegisterDto } from '../application/dtos/register.dto';
import { LoginDto } from '../application/dtos/login.dto';

// TODO: Activar cuando el servicio de Google OAuth esté configurado.
// import { Get, Req, UseGuards } from '@nestjs/common';
// import { AuthGuard } from '@nestjs/passport';

@Controller('auth')
export class AuthController {
  constructor(
    private readonly registerUseCase: RegisterUseCase,
    private readonly loginUseCase: LoginUseCase,
  ) {}

  @Post('register')
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
