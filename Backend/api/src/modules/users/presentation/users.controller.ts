import {
  Body,
  Controller,
  ForbiddenException,
  Param,
  Patch,
  Req,
  UseGuards,
} from '@nestjs/common';
import { JwtAuthGuard } from '../../auth/infrastructure/guards/jwt-auth.guard';
import { UpdateEloUseCase } from '../application/use-cases/update-elo.use-case';
import { UpdateEloDto } from './dtos/update-elo.dto';
import { UserRole } from '../domain/entities/user.entity';

@Controller('users')
export class UsersController {
  constructor(private readonly updateEloUseCase: UpdateEloUseCase) {}

  @UseGuards(JwtAuthGuard)
  @Patch(':id/elo')
  async updateEloRating(
    @Param('id') id: string,
    @Body() dto: UpdateEloDto,
    @Req() req: any,
  ) {
    if (req.user.userId !== id && req.user.role !== UserRole.ADMIN) {
      throw new ForbiddenException('No puedes actualizar el ELO de otro usuario.');
    }
    const user = await this.updateEloUseCase.execute(id, dto.eloRating);
    return {
      success: true,
      data: {
        id: user.id,
        username: user.username,
        eloRating: user.eloRating,
      },
    };
  }
}
