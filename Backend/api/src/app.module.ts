import { Module } from '@nestjs/common';
<<<<<<< Updated upstream
import { AppController } from './app.controller';
import { AppService } from './app.service';

@Module({
  imports: [],
  controllers: [AppController],
  providers: [AppService],
=======
import { ConfigModule } from '@nestjs/config';
import { DatabaseModule } from './core/infrastructure/database/database.module';
import { UsersModule } from './modules/users/infrastructure/users.module';
import { AuthModule } from './modules/auth/infrastructure/auth.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env',
    }),
    DatabaseModule,
    UsersModule,
    AuthModule,
  ],
  controllers: [],
  providers: [],
>>>>>>> Stashed changes
})
export class AppModule {}
