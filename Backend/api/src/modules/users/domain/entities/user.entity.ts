export class User {
  constructor(
    public id: string,
    public username: string,
    public email: string,
    public passwordHash: string,
    public eloRating: number = 1200,
    public createdAt: Date = new Date(),
  ) {}
}
