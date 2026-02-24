import { ApiProperty } from '@nestjs/swagger/dist/decorators/api-property.decorator';
import { IsEmail, IsNotEmpty, IsString } from 'class-validator';

export class LoginDto {
  @ApiProperty({
    description: 'Correo electrónico registrado previamente',
    example: 'bug@example.com',
  })
  @IsEmail({}, { message: 'El email debe ser un correo válido.' })
  email: string;

  @ApiProperty({
    description: 'Contraseña del usuario',
    example: 'superpassword123',
  })
  @IsString()
  @IsNotEmpty({ message: 'La contraseña no puede estar vacía.' })
  password: string;
}
