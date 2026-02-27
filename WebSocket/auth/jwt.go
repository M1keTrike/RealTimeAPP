package auth

import (
	"errors"
	"fmt"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

// Claims mirrors the JWT payload produced by the NestJS API.
// NestJS @nestjs/jwt uses HS256 and sets: sub, email, username, role, iat, exp.
type Claims struct {
	Sub      string `json:"sub"`
	Email    string `json:"email"`
	Username string `json:"username"`
	Role     string `json:"role"`
	jwt.RegisteredClaims
}

// Validator validates JWTs using the shared JWT_SECRET.
type Validator struct {
	secret []byte
}

func NewValidator(secret string) *Validator {
	return &Validator{secret: []byte(secret)}
}

// Validate parses and verifies the token, returning its claims.
func (v *Validator) Validate(tokenString string) (*Claims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(t *jwt.Token) (interface{}, error) {
		if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", t.Header["alg"])
		}
		return v.secret, nil
	})
	if err != nil {
		return nil, err
	}

	claims, ok := token.Claims.(*Claims)
	if !ok || !token.Valid {
		return nil, errors.New("invalid token claims")
	}

	return claims, nil
}

// GenerateServiceToken mints a long-lived ADMIN JWT for server-to-server calls to NestJS.
// This token uses the same JWT_SECRET so NestJS accepts it as any other valid token.
func GenerateServiceToken(secret string) (string, error) {
	claims := Claims{
		Sub:      "ws-server",
		Email:    "ws@mathduel.internal",
		Username: "ws_server",
		Role:     "ADMIN",
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(365 * 24 * time.Hour)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(secret))
}
