export enum UserRole {
  PLAYER = 'PLAYER',
  ADMIN = 'ADMIN',
}

export class User {
  constructor(
    public id: string,
    public username: string,
    public email: string,
    public passwordHash: string,
    public eloRating: number = 1200,
    public role: UserRole = UserRole.PLAYER,
    public createdAt: Date = new Date(),
  ) {}
}
