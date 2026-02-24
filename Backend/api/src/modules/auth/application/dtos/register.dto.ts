import { ApiProperty } from '@nestjs/swagger/dist/decorators/api-property.decorator';
import { IsEmail, IsNotEmpty, IsString, MinLength } from 'class-validator';

export class RegisterDto {
  @ApiProperty({
    description: 'Nombre de usuario visible en el juego',
    example: 'bug_player',
  })
  @IsString()
  @IsNotEmpty({ message: 'El username no puede estar vacío.' })
  username: string;

  @ApiProperty({
    description: 'Correo electrónico válido',
    example: 'bug@example.com',
  })
  @IsEmail({}, { message: 'El email debe ser un correo válido.' })
  email: string;

  @ApiProperty({
    description: 'Contraseña segura (mínimo 6 caracteres)',
    example: 'superpassword123',
    minLength: 6,
  })
  @IsString()
  @MinLength(6, { message: 'La contraseña debe tener al menos 6 caracteres.' })
  password: string;
}
