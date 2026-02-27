import { IsUUID, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreateSessionDto {
  @ApiProperty({ description: 'UUID del primer jugador', example: 'uuid-v4' })
  @IsNotEmpty()
  @IsUUID('4')
  user1Id: string;

  @ApiProperty({ description: 'UUID del segundo jugador', example: 'uuid-v4' })
  @IsNotEmpty()
  @IsUUID('4')
  user2Id: string;
}
