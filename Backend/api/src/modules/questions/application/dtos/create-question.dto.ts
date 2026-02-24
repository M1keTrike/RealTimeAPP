import { IsString, IsEnum, IsArray, IsNumber, ArrayMinSize, Min } from 'class-validator';
import { QuestionDifficulty } from '../../domain/entities/question.entity';
import { ApiProperty } from '@nestjs/swagger/dist/decorators/api-property.decorator';

export class CreateQuestionDto {
  @ApiProperty({
    description: 'El problema matemático a resolver',
    example: '2x + 5 = 15',
  })
  @IsString()
  statement: string;

  @ApiProperty({
    description: 'Nivel de dificultad de la pregunta',
    enum: QuestionDifficulty,
    example: QuestionDifficulty.MEDIUM,
  })
  @IsEnum(QuestionDifficulty)
  difficulty: QuestionDifficulty;

  @ApiProperty({
    description: 'Lista de opciones posibles (mínimo 2)',
    example: ['x = 3', 'x = 5', 'x = 7', 'x = 4'],
    type: [String],
  })
  @IsArray()
  @IsString({ each: true })
  @ArrayMinSize(2, { message: 'Debe haber al menos 2 opciones' })
  options: string[];

  @ApiProperty({
    description: 'El índice del arreglo de opciones que contiene la respuesta correcta (empieza en 0)',
    example: 1,
    minimum: 0,
  })
  @IsNumber()
  @Min(0)
  correctOptionIndex: number;
}