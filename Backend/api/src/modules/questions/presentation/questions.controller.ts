import { Controller, Post, Get, Body, Query, ParseEnumPipe } from '@nestjs/common';
import { CreateQuestionUseCase } from '../application/use-cases/create-question.use-case';
import { GetRandomQuestionUseCase } from '../application/use-cases/get-random-question.use-case';
import { CreateQuestionDto } from '../application/dtos/create-question.dto';
import { QuestionDifficulty } from '../domain/entities/question.entity';

@Controller('questions')
export class QuestionsController {
  constructor(
    private readonly createQuestionUseCase: CreateQuestionUseCase,
    private readonly getRandomQuestionUseCase: GetRandomQuestionUseCase,
  ) {}

  @Post()
  async createQuestion(@Body() dto: CreateQuestionDto) {
    const question = await this.createQuestionUseCase.execute(dto);
    return {
      success: true,
      message: 'Pregunta creada correctamente',
      data: question, 
    };
  }

  @Get('random')
  async getRandomQuestion(
    @Query('difficulty', new ParseEnumPipe(QuestionDifficulty)) difficulty: QuestionDifficulty
    ) {
    const question = await this.getRandomQuestionUseCase.execute(difficulty);
    return {
      success: true,
      data: {
        id: question.id,
        statement: question.statement,
        difficulty: question.difficulty,
        options: question.options,
        },
    };
  }
}