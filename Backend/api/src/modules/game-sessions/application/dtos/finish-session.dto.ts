import { IsOptional, IsUUID } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class FinishSessionDto {
  @ApiProperty({ description: 'UUID del jugador ganador (omitido en empate)', example: 'uuid-v4', required: false, nullable: true })
  @IsOptional()
  @IsUUID('4')
  winnerId?: string;
}
