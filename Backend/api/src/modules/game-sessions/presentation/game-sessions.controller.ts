import { Controller, Post, Body } from '@nestjs/common';
import { JoinOrCreateSessionUseCase } from '../application/use-cases/join-or-create-session.use-case';
import { MatchmakeDto } from '../application/dtos/matchmake.dto';

@Controller('game-sessions')
export class GameSessionsController {
  constructor(
    private readonly joinOrCreateSessionUseCase: JoinOrCreateSessionUseCase,
  ) {}

  @Post('matchmake')
  async matchmake(@Body() dto: MatchmakeDto) {
  const session = await this.joinOrCreateSessionUseCase.execute(dto.userId);      
  return {
      success: true,
      data: session,
    };
  }
}