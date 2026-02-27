import { Controller, Post, Get, Body, Query, ParseEnumPipe, UseGuards, Param, Delete, Put } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiBody, ApiResponse, ApiQuery, ApiParam } from '@nestjs/swagger';

import { QuestionDifficulty } from '../domain/entities/question.entity';
import { RolesGuard } from '../../auth/infrastructure/guards/roles.guard';
import { UserRole } from 'src/modules/users/domain/entities/user.entity';
import { Roles } from 'src/core/application/decorators/roles.decorator';
import { JwtAuthGuard } from 'src/modules/auth/infrastructure/guards/jwt-auth.guard';

import { CreateQuestionUseCase } from '../application/use-cases/create-question.use-case';
import { GetRandomQuestionUseCase } from '../application/use-cases/get-random-question.use-case';
import { GetAllQuestionsUseCase } from '../application/use-cases/get-all-questions.use-case';
import { UpdateQuestionUseCase } from '../application/use-cases/update-question.use-case';
import { DeleteQuestionUseCase } from '../application/use-cases/delete-question.use-case';
import { CreateQuestionDto } from '../application/dtos/create-question.dto';
import { UpdateQuestionDto } from '../application/dtos/update-question.dto';

@ApiTags('Questions (Catálogo)')
@Controller('questions')
export class QuestionsController {
  constructor(
    private readonly createQuestionUseCase: CreateQuestionUseCase,
    private readonly getRandomQuestionUseCase: GetRandomQuestionUseCase,
    private readonly getAllQuestionsUseCase: GetAllQuestionsUseCase, 
    private readonly updateQuestionUseCase: UpdateQuestionUseCase, 
    private readonly deleteQuestionUseCase: DeleteQuestionUseCase,
  ) {}

  @Post()
  @UseGuards(JwtAuthGuard, RolesGuard) 
  @Roles(UserRole.ADMIN) 
  @ApiBearerAuth() 
  @ApiOperation({ summary: 'Crear una pregunta (Solo ADMIN)' })
  @ApiResponse({ status: 201, description: 'La pregunta fue creada exitosamente.' })
  @ApiResponse({ status: 401, description: 'No autorizado (Falta Token).' })
  @ApiResponse({ status: 403, description: 'Prohibido (Requiere rol ADMIN).' })
  @ApiBody({
    schema: {
      example: {
        statement: "2x + 5 = 15",
        difficulty: "MEDIUM",
        options: ["x = 3", "x = 5", "x = 7", "x = 4"],
        correctOptionIndex: 1
      }
    }
  })
  async createQuestion(@Body() dto: CreateQuestionDto) {
    const question = await this.createQuestionUseCase.execute(dto);
    return {
      success: true,
      message: 'Pregunta creada correctamente',
      data: question, 
    };
  }

  @Get('random')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Obtener pregunta aleatoria ocultando la respuesta correcta' })
  @ApiQuery({ name: 'difficulty', enum: QuestionDifficulty, description: 'Dificultad deseada' })
  @ApiResponse({ status: 200, description: 'Devuelve la pregunta sin la respuesta correcta.' })
  @ApiResponse({ status: 404, description: 'No hay preguntas disponibles para esa dificultad.' })
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

  @Get()
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Obtener todo el catálogo de preguntas (Solo ADMIN)' })
  @ApiResponse({ status: 200, description: 'Lista completa de preguntas devuelta exitosamente.' })
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Obtener todo el catálogo (Solo ADMIN)' })
  async getAllQuestions() {
    const questions = await this.getAllQuestionsUseCase.execute();
    return { success: true, data: questions };
  }

  @Put(':id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Editar una pregunta existente (Solo ADMIN)' })
  @ApiParam({ name: 'id', description: 'El UUID de la pregunta a editar' })
  @ApiResponse({ status: 200, description: 'Pregunta actualizada correctamente.' })
  @ApiResponse({ status: 404, description: 'Pregunta no encontrada.' })
  @ApiOperation({ summary: 'Editar una pregunta (Solo ADMIN)' })
  async updateQuestion(@Param('id') id: string, @Body() dto: UpdateQuestionDto) {
    const question = await this.updateQuestionUseCase.execute(id, dto);
    return { success: true, message: 'Pregunta actualizada', data: question };
  }

  @Delete(':id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Eliminar una pregunta del catálogo (Solo ADMIN)' })
  @ApiParam({ name: 'id', description: 'El UUID de la pregunta a eliminar' })
  @ApiResponse({ status: 200, description: 'Pregunta eliminada correctamente.' })
  @ApiResponse({ status: 404, description: 'Pregunta no encontrada.' })  async deleteQuestion(@Param('id') id: string) {
    await this.deleteQuestionUseCase.execute(id);
    return { success: true, message: 'Pregunta eliminada correctamente' };
  }
}