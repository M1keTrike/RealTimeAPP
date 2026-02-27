import { Injectable, Inject, NotFoundException, BadRequestException,ForbiddenException } from '@nestjs/common';
import { GAME_SESSION_REPOSITORY } from '../../domain/repositories/game-session.repository.interface';
import type { IGameSessionRepository } from '../../domain/repositories/game-session.repository.interface';
import { GameSessionStatus } from '../../domain/entities/game-session.entity';

@Injectable()
export class CancelSessionUseCase {
  constructor(
    @Inject(GAME_SESSION_REPOSITORY)
    private readonly gameSessionRepo: IGameSessionRepository,
  ) {}

  async execute(sessionId: string, userId: string): Promise<void> {
    const session = await this.gameSessionRepo.findById(sessionId);

    if (!session) {
      throw new NotFoundException('La partida no existe.');
    }

    if (session.user1Id !== userId) {
      throw new ForbiddenException('No tienes permiso para cancelar esta partida. ');
    }

    if (session.status !== GameSessionStatus.WAITING) {
      throw new BadRequestException('No puedes cancelar una partida que ya comenzó o finalizó.');
    }

    await this.gameSessionRepo.delete(sessionId);
  }
}