export enum GameSessionStatus {
  WAITING = 'WAITING',
  IN_PROGRESS = 'IN_PROGRESS',
  FINISHED = 'FINISHED',
}

export class GameSession {
  constructor(
    public id: string,
    public user1Id: string,
    public user2Id: string | null = null,
    public winnerId: string | null = null,
    public status: GameSessionStatus = GameSessionStatus.WAITING,
    public createdAt: Date = new Date(),
  ) {}

  joinPlayer(userId: string): void {
    if (this.status !== GameSessionStatus.WAITING) {
      throw new Error('La sala no está disponible.');
    }
    if (this.user1Id === userId) {
      throw new Error('El usuario ya está en esta sala.');
    }
    
    this.user2Id = userId;
    this.status = GameSessionStatus.IN_PROGRESS;
  }

  finishGame(winnerId: string | null): void {
    this.winnerId = winnerId;
    this.status = GameSessionStatus.FINISHED;
  }
}