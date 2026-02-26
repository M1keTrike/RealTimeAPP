package hub

import (
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"log"
	"net/http"
	"sync"
	"sync/atomic"

	gorillaws "github.com/gorilla/websocket"

	"mathduel-ws/api"
	"mathduel-ws/auth"
	"mathduel-ws/config"
)

var upgrader = gorillaws.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	// Allow all origins — the mobile app connects from arbitrary IPs.
	CheckOrigin: func(r *http.Request) bool { return true },
}

// Hub is the central coordinator: it registers/unregisters clients,
// maintains the matchmaking queue, and tracks active rooms.
type Hub struct {
	cfg       *config.Config
	validator *auth.Validator
	apiClient *api.Client

	// clients maps userID → active Client.
	clients map[string]*Client
	// waiting is the FIFO queue of players seeking a match.
	waiting []*Client
	// rooms maps roomID → Room.
	rooms map[string]*Room

	register   chan *Client
	unregister chan *Client

	mu      sync.Mutex
	roomSeq int64 // atomic counter for room IDs
}

func NewHub(cfg *config.Config, validator *auth.Validator, apiClient *api.Client) *Hub {
	return &Hub{
		cfg:        cfg,
		validator:  validator,
		apiClient:  apiClient,
		clients:    make(map[string]*Client),
		rooms:      make(map[string]*Room),
		register:   make(chan *Client, 32),
		unregister: make(chan *Client, 32),
	}
}

// Run is the event loop for the Hub. It must be started in a goroutine.
func (h *Hub) Run() {
	for {
		select {
		case c := <-h.register:
			h.onRegister(c)
		case c := <-h.unregister:
			h.onUnregister(c)
		}
	}
}

// ── Registration ──────────────────────────────────────────────────────────────

func (h *Hub) onRegister(c *Client) {
	h.mu.Lock()
	defer h.mu.Unlock()

	// If the same user reconnects, close the stale connection first.
	if prev, ok := h.clients[c.UserID]; ok {
		log.Printf("[hub] replacing stale connection for %s", c.UserID)
		h.dropFromWaiting(prev)
		prev.close()
	}

	h.clients[c.UserID] = c
	log.Printf("[hub] registered %s (%s), online=%d", c.UserID, c.Username, len(h.clients))

	// Acknowledge the connection.
	if data, err := build(TypeAuthenticated, AuthenticatedPayload{
		UserID:   c.UserID,
		Username: c.Username,
	}); err == nil {
		c.Send(data)
	}

	h.matchmake(c)
}

func (h *Hub) onUnregister(c *Client) {
	h.mu.Lock()
	defer h.mu.Unlock()

	if _, ok := h.clients[c.UserID]; !ok {
		return // already removed
	}

	delete(h.clients, c.UserID)
	h.dropFromWaiting(c)
	c.close()

	log.Printf("[hub] unregistered %s, online=%d", c.UserID, len(h.clients))

	// If the client was in an active game, let the room handle the forfeit.
	if c.room != nil {
		go c.room.PlayerDisconnected(c)
	}
}

// ── Matchmaking ───────────────────────────────────────────────────────────────

// matchmake tries to pair c with a waiting player; otherwise enqueues c.
// Must be called with h.mu held.
func (h *Hub) matchmake(c *Client) {
	if len(h.waiting) == 0 {
		h.waiting = append(h.waiting, c)
		if data, err := build(TypeWaiting, WaitingPayload{
			Message: "Buscando oponente...",
		}); err == nil {
			c.Send(data)
		}
		log.Printf("[hub] %s is waiting for a match (queue=%d)", c.UserID, len(h.waiting))
		return
	}

	opponent := h.waiting[0]
	h.waiting = h.waiting[1:]

	roomID := h.nextRoomID()
	cfg := &RoomConfig{
		TotalRounds:  h.cfg.GameRounds,
		RoundTimeout: h.cfg.RoundTimeout(),
		Difficulty:   "ALL",
	}
	room := newRoom(roomID, opponent, c, cfg, h.apiClient)
	h.rooms[roomID] = room

	log.Printf("[hub] matched %s vs %s → room %s", opponent.UserID, c.UserID, roomID)
	room.Start()
}

func (h *Hub) dropFromWaiting(c *Client) {
	for i, w := range h.waiting {
		if w.UserID == c.UserID {
			h.waiting = append(h.waiting[:i], h.waiting[i+1:]...)
			return
		}
	}
}

// ── HTTP handler ──────────────────────────────────────────────────────────────

// ServeWS is the HTTP handler that upgrades a connection to WebSocket
// after validating the JWT passed as the `token` query parameter.
//
//	ws://host:8080/ws?token=<JWT>
func (h *Hub) ServeWS(w http.ResponseWriter, r *http.Request) {
	token := r.URL.Query().Get("token")
	if token == "" {
		http.Error(w, `{"error":"missing token"}`, http.StatusUnauthorized)
		return
	}

	claims, err := h.validator.Validate(token)
	if err != nil {
		log.Printf("[hub] invalid token from %s: %v", r.RemoteAddr, err)
		http.Error(w, `{"error":"invalid or expired token"}`, http.StatusUnauthorized)
		return
	}

	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("[hub] upgrade error: %v", err)
		return
	}

	c := newClient(claims.Sub, claims.Username, claims.Role, conn, h)
	h.register <- c

	go c.WritePump()
	go c.ReadPump()
}

// ── Utilities ─────────────────────────────────────────────────────────────────

func (h *Hub) nextRoomID() string {
	seq := atomic.AddInt64(&h.roomSeq, 1)
	return fmt.Sprintf("room-%d-%s", seq, shortRand())
}

func shortRand() string {
	b := make([]byte, 4)
	rand.Read(b)
	return hex.EncodeToString(b)
}
