package config

import (
	"log"
	"os"
	"strconv"
	"time"

	"github.com/joho/godotenv"
)

type Config struct {
	Port             string
	JWTSecret        string
	NestAPIURL       string
	GameRounds       int
	RoundTimeoutSecs int
}

func Load() *Config {
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found, loading from environment")
	}

	rounds, err := strconv.Atoi(getEnv("GAME_ROUNDS", "5"))
	if err != nil || rounds < 1 {
		rounds = 5
	}

	timeout, err := strconv.Atoi(getEnv("ROUND_TIMEOUT_SECONDS", "30"))
	if err != nil || timeout < 5 {
		timeout = 30
	}

	return &Config{
		Port:             getEnv("PORT", "8080"),
		JWTSecret:        getEnv("JWT_SECRET", ""),
		NestAPIURL:       getEnv("NESTJS_API_URL", "http://localhost:3000"),
		GameRounds:       rounds,
		RoundTimeoutSecs: timeout,
	}
}

func (c *Config) RoundTimeout() time.Duration {
	return time.Duration(c.RoundTimeoutSecs) * time.Second
}

func getEnv(key, defaultVal string) string {
	if val := os.Getenv(key); val != "" {
		return val
	}
	return defaultVal
}
