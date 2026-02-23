import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { DatabaseModule } from './core/infrastructure/database/database.module';
import { UsersModule } from './modules/users/infrastructure/users.module';
import { AuthModule } from './modules/auth/infrastructure/auth.module';
import { GameSessionsModule } from './modules/game-sessions/infrastructure/game-sessions.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env',
    }),
    DatabaseModule,
    UsersModule,
    AuthModule,
    GameSessionsModule
  ],
  controllers: [],
  providers: [],
})
export class AppModule {}
