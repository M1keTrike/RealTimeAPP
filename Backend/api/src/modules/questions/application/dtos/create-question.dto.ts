import { IsString, IsEnum, IsArray, IsNumber, ArrayMinSize, Min } from 'class-validator';
import { QuestionDifficulty } from '../../domain/entities/question.entity';

export class CreateQuestionDto {
  @IsString()
  statement: string;

  @IsEnum(QuestionDifficulty)
  difficulty: QuestionDifficulty;

  @IsArray()
  @IsString({ each: true })
  @ArrayMinSize(2, { message: 'Debe haber al menos 2 opciones' })
  options: string[];

  @IsNumber()
  @Min(0)
  correctOptionIndex: number;
}