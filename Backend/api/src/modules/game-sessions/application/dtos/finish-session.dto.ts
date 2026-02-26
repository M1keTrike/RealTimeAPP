import { IsUUID, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class FinishSessionDto {
  @ApiProperty({ description: 'UUID del jugador ganador', example: 'uuid-v4' })
  @IsNotEmpty()
  @IsUUID('4')
  winnerId: string;
}
