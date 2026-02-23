export class Round {
  constructor(
    public id: string,
    public gameSessionId: string,
    public questionId: string,
    public winnerId: string | null = null,
    public startedAt: Date = new Date(),
    public endedAt: Date | null = null,
  ) {}

  finishRound(winnerId: string | null): void {
    if (this.endedAt !== null) {
      throw new Error('Esta ronda ya ha finalizado.');
    }
    this.winnerId = winnerId;
    this.endedAt = new Date();
  }
}