import { GoogleUserData } from '../entities/google-user-data.entity';

export const GOOGLE_USER_DATA_REPOSITORY = Symbol(
  'GOOGLE_USER_DATA_REPOSITORY',
);

export interface IGoogleUserDataRepository {
  save(data: GoogleUserData): Promise<void>;
  findByUserId(userId: string): Promise<GoogleUserData | null>;
  findByGoogleId(googleId: string): Promise<GoogleUserData | null>;
}
