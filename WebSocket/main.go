package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"mathduel-ws/api"
	"mathduel-ws/auth"
	"mathduel-ws/config"
	"mathduel-ws/hub"
)

func main() {
	cfg := config.Load()

	if cfg.JWTSecret == "" {
		log.Fatal("[main] JWT_SECRET is required but not set")
	}

	validator := auth.NewValidator(cfg.JWTSecret)

	// The WS server mints its own ADMIN JWT to call NestJS endpoints that
	// require authentication (GET /questions, POST /game-sessions, etc.).
	serviceToken, err := auth.GenerateServiceToken(cfg.JWTSecret)
	if err != nil {
		log.Fatalf("[main] failed to generate service token: %v", err)
	}

	apiClient := api.NewClient(cfg.NestAPIURL, serviceToken)

	h := hub.NewHub(cfg, validator, apiClient)
	go h.Run()

	mux := http.NewServeMux()

	// Main WebSocket endpoint — clients connect here with their JWT.
	mux.HandleFunc("/ws", h.ServeWS)

	// Health check for Docker/load-balancer probes.
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"status":"ok","service":"mathduel-ws"}`))
	})

	srv := &http.Server{
		Addr:         ":" + cfg.Port,
		Handler:      mux,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	// Start the server in a goroutine so we can listen for OS signals below.
	go func() {
		log.Printf("[main] MathDuel WebSocket server listening on :%s", cfg.Port)
		log.Printf("[main] NestJS API → %s | rounds=%d | timeout=%ds",
			cfg.NestAPIURL, cfg.GameRounds, cfg.RoundTimeoutSecs)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("[main] server error: %v", err)
		}
	}()

	// Graceful shutdown on SIGINT / SIGTERM.
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("[main] shutting down gracefully…")
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("[main] forced shutdown: %v", err)
	}
	log.Println("[main] server stopped")
}
