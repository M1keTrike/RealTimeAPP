export enum UserRole {
  PLAYER = 'PLAYER',
  ADMIN = 'ADMIN',
}

export enum AuthProvider {
  LOCAL = 'LOCAL',
  GOOGLE = 'GOOGLE',
  BOTH = 'BOTH',
}

export class User {
  constructor(
    public id: string,
    public username: string,
    public email: string,
    public passwordHash: string | null,
    public eloRating: number = 1200,
    public role: UserRole = UserRole.PLAYER,
    public authProvider: AuthProvider = AuthProvider.LOCAL,
    public createdAt: Date = new Date(),
  ) {}
}
