import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { OAuth2Client, TokenPayload } from 'google-auth-library';

export interface GoogleTokenPayload {
  googleId: string;
  email: string;
  name: string | null;
  picture: string | null;
  locale: string | null;
}

@Injectable()
export class GoogleTokenVerifierService {
  private readonly client: OAuth2Client;
  private readonly clientId: string;

  constructor(private readonly configService: ConfigService) {
    this.clientId =
      '519791700688-lg6fu6r3ungtefs1hg9l8e7em68pffrs.apps.googleusercontent.com';
    this.client = new OAuth2Client(this.clientId);
  }

  /**
   * Verifica un idToken de Google Sign-In y extrae los datos del usuario.
   * Valida firma, expiraciÃ³n e audience automÃ¡ticamente.
   *
   * @throws UnauthorizedException si el token es invÃ¡lido o ha expirado.
   */
  async verify(idToken: string): Promise<GoogleTokenPayload> {
    try {
      const ticket = await this.client.verifyIdToken({
        idToken,
        audience: this.clientId,
      });

      const payload: TokenPayload | undefined = ticket.getPayload();

      if (!payload || !payload.sub || !payload.email) {
        throw new UnauthorizedException(
          'El idToken de Google no contiene los datos requeridos (sub, email).',
        );
      }

      return {
        googleId: payload.sub,
        email: payload.email,
        name: payload.name ?? null,
        picture: payload.picture ?? null,
        locale: payload.locale ?? null,
      };
    } catch (error) {
      if (error instanceof UnauthorizedException) {
        throw error;
      }

      throw new UnauthorizedException(
        'El idToken de Google es invÃ¡lido o ha expirado.',
      );
    }
  }
}
