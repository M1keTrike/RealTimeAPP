import { IsUUID, IsNotEmpty } from 'class-validator';

export class MatchmakeDto {
  @IsNotEmpty({ message: 'El userId no puede estar vacío.' })
  @IsUUID('4', { message: 'El userId debe ser un UUID v4 válido.' })
  userId: string;
}