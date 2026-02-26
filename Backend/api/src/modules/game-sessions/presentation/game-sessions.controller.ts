import { Controller, Post, Patch, Body, Param, UseGuards } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiResponse, ApiParam } from '@nestjs/swagger';
import { JoinOrCreateSessionUseCase } from '../application/use-cases/join-or-create-session.use-case';
import { CreateSessionUseCase } from '../application/use-cases/create-session.use-case';
import { FinishSessionUseCase } from '../application/use-cases/finish-session.use-case';
import { MatchmakeDto } from '../application/dtos/matchmake.dto';
import { CreateSessionDto } from '../application/dtos/create-session.dto';
import { FinishSessionDto } from '../application/dtos/finish-session.dto';
import { JwtAuthGuard } from 'src/modules/auth/infrastructure/guards/jwt-auth.guard';
import { RolesGuard } from 'src/modules/auth/infrastructure/guards/roles.guard';
import { Roles } from 'src/core/application/decorators/roles.decorator';
import { UserRole } from 'src/modules/users/domain/entities/user.entity';

@ApiTags('Game Sessions')
@Controller('game-sessions')
export class GameSessionsController {
  constructor(
    private readonly joinOrCreateSessionUseCase: JoinOrCreateSessionUseCase,
    private readonly createSessionUseCase: CreateSessionUseCase,
    private readonly finishSessionUseCase: FinishSessionUseCase,
  ) {}

  @Post('matchmake')
  @ApiOperation({ summary: 'Emparejar jugador con sala disponible o crear una nueva' })
  @ApiResponse({ status: 201, description: 'Sesión creada o unida correctamente.' })
  async matchmake(@Body() dto: MatchmakeDto) {
    const session = await this.joinOrCreateSessionUseCase.execute(dto.userId);
    return { success: true, data: session };
  }

  @Post()
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Crear sesión directa con dos jugadores (Solo servicio WS/ADMIN)' })
  @ApiResponse({ status: 201, description: 'Sesión creada en estado IN_PROGRESS.' })
  @ApiResponse({ status: 403, description: 'Requiere rol ADMIN.' })
  async createSession(@Body() dto: CreateSessionDto) {
    const session = await this.createSessionUseCase.execute(dto.user1Id, dto.user2Id);
    return { success: true, data: session };
  }

  @Patch(':id/finish')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Finalizar una sesión con un ganador (Solo servicio WS/ADMIN)' })
  @ApiParam({ name: 'id', description: 'UUID de la sesión de juego' })
  @ApiResponse({ status: 200, description: 'Sesión finalizada correctamente.' })
  @ApiResponse({ status: 404, description: 'Sesión no encontrada.' })
  async finishSession(@Param('id') id: string, @Body() dto: FinishSessionDto) {
    await this.finishSessionUseCase.execute(id, dto.winnerId);
    return { success: true, message: 'Sesión finalizada correctamente' };
  }
}
