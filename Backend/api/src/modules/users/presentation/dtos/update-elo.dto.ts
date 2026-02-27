import { IsInt, Min } from 'class-validator';

export class UpdateEloDto {
  @IsInt()
  @Min(0)
  eloRating: number;
}
