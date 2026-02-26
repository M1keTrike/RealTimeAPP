export class GoogleUserData {
  constructor(
    public id: string,
    public userId: string,
    public googleId: string,
    public picture: string | null = null,
    public locale: string | null = null,
    public createdAt: Date = new Date(),
    public updatedAt: Date = new Date(),
  ) {}
}
