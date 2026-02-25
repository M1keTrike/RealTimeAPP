package hub

import "encoding/json"

// ── Inbound message types (client → server) ──────────────────────────────────

const (
	TypeAnswer = "answer" // Player submits an answer
	TypePing   = "ping"   // Keep-alive
)

// ── Outbound message types (server → client) ─────────────────────────────────

const (
	TypeAuthenticated = "authenticated" // JWT validated, user info echoed back
	TypeWaiting       = "waiting"       // In matchmaking queue
	TypeGameStarted   = "game_started"  // Room formed, game begins
	TypeRoundStarted  = "round_started" // New round with question
	TypeRoundResult   = "round_result"  // Who won the round
	TypeGameOver      = "game_over"     // Final result
	TypeError         = "error"         // Something went wrong
	TypePong          = "pong"          // Response to ping
)

// Message is the top-level envelope for every WebSocket frame.
type Message struct {
	Type    string          `json:"type"`
	Payload json.RawMessage `json:"payload,omitempty"`
}

// ── Inbound payloads ─────────────────────────────────────────────────────────

// AnswerPayload is what the client sends when answering a question.
type AnswerPayload struct {
	QuestionID string `json:"question_id"`
	OptionID   string `json:"option_id"`
}

// ── Outbound payloads ────────────────────────────────────────────────────────

type AuthenticatedPayload struct {
	UserID   string `json:"user_id"`
	Username string `json:"username"`
}

type WaitingPayload struct {
	Message string `json:"message"`
}

type PlayerInfo struct {
	UserID   string `json:"user_id"`
	Username string `json:"username"`
}

type GameStartedPayload struct {
	SessionID   string     `json:"session_id"`
	Opponent    PlayerInfo `json:"opponent"`
	TotalRounds int        `json:"total_rounds"`
}

type QuestionOption struct {
	ID   string `json:"id"`
	Text string `json:"text"`
}

type QuestionData struct {
	ID        string           `json:"id"`
	Statement string           `json:"statement"`
	Difficulty string          `json:"difficulty"`
	Options   []QuestionOption `json:"options"`
}

type RoundStartedPayload struct {
	RoundNumber int          `json:"round_number"`
	Question    QuestionData `json:"question"`
	TimeLimit   int          `json:"time_limit_seconds"`
}

type RoundResultPayload struct {
	RoundNumber     int    `json:"round_number"`
	WinnerID        string `json:"winner_id"`        // empty = draw / timeout
	CorrectOptionID string `json:"correct_option_id"`
	Scores          map[string]int `json:"scores"`
}

type GameOverPayload struct {
	WinnerID string         `json:"winner_id"`
	Reason   string         `json:"reason"` // "rounds_completed" | "opponent_disconnected"
	Scores   map[string]int `json:"scores"`
}

type ErrorPayload struct {
	Code    string `json:"code"`
	Message string `json:"message"`
}

// build serialises a typed payload into a wire-ready []byte frame.
func build(msgType string, payload interface{}) ([]byte, error) {
	raw, err := json.Marshal(payload)
	if err != nil {
		return nil, err
	}
	return json.Marshal(Message{Type: msgType, Payload: json.RawMessage(raw)})
}
