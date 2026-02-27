package api

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"math/rand/v2"
	"net/http"
	"time"
)

// --- Response types mirroring NestJS domain entities ---

// NestOption is a question answer option (id + text).
type NestOption struct {
	ID   string `json:"id"`
	Text string `json:"text"`
}

// NestQuestion is the full question returned by GET /questions (ADMIN).
// The correctOptionId field is only present in the admin response.
type NestQuestion struct {
	ID             string       `json:"id"`
	Statement      string       `json:"statement"`
	Difficulty     string       `json:"difficulty"`
	Options        []NestOption `json:"options"`
	CorrectOptionID string      `json:"correctOptionId"`
}

// NestGameSession is the game session returned by the NestJS API.
type NestGameSession struct {
	ID       string `json:"id"`
	User1ID  string `json:"user1Id"`
	User2ID  string `json:"user2Id"`
	WinnerID string `json:"winnerId"`
	Status   string `json:"status"`
}

// Client is an authenticated HTTP client for the NestJS REST API.
type Client struct {
	baseURL      string
	serviceToken string
	http         *http.Client
}

func NewClient(baseURL, serviceToken string) *Client {
	return &Client{
		baseURL:      baseURL,
		serviceToken: serviceToken,
		http:         &http.Client{Timeout: 10 * time.Second},
	}
}

func (c *Client) do(method, path string, body interface{}, out interface{}) error {
	var bodyReader io.Reader
	if body != nil {
		data, err := json.Marshal(body)
		if err != nil {
			return fmt.Errorf("marshal body: %w", err)
		}
		bodyReader = bytes.NewReader(data)
	}

	req, err := http.NewRequest(method, c.baseURL+path, bodyReader)
	if err != nil {
		return fmt.Errorf("build request: %w", err)
	}
	req.Header.Set("Authorization", "Bearer "+c.serviceToken)
	req.Header.Set("Content-Type", "application/json")

	resp, err := c.http.Do(req)
	if err != nil {
		return fmt.Errorf("execute request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		raw, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("NestJS API %d: %s", resp.StatusCode, string(raw))
	}

	if out != nil {
		return json.NewDecoder(resp.Body).Decode(out)
	}
	return nil
}

// GetRandomQuestion returns a random playable question from all difficulties.
func (c *Client) GetRandomQuestion(difficulty string) (*NestQuestion, error) {
	_ = difficulty
	pool, err := c.GetPlayableQuestions()
	if err != nil {
		return nil, err
	}
	if len(pool) == 0 {
		return nil, fmt.Errorf("no playable questions available")
	}

	q := pool[rand.IntN(len(pool))]
	return &q, nil
}

// GetPlayableQuestions fetches all questions across all difficulties and keeps
// only those with a non-empty correctOptionId.
func (c *Client) GetPlayableQuestions() ([]NestQuestion, error) {
	var resp struct {
		Success bool           `json:"success"`
		Data    []NestQuestion `json:"data"`
	}
	if err := c.do("GET", "/questions", nil, &resp); err != nil {
		return nil, fmt.Errorf("get questions: %w", err)
	}

	pool := make([]NestQuestion, 0, len(resp.Data))
	for _, q := range resp.Data {
		if q.CorrectOptionID != "" {
			pool = append(pool, q)
		}
	}

	return pool, nil
}

// CreateGameSession creates a new session in NestJS with both players already matched.
// Calls POST /game-sessions (ADMIN), which sets status = IN_PROGRESS immediately.
func (c *Client) CreateGameSession(user1ID, user2ID string) (*NestGameSession, error) {
	var resp struct {
		Success bool            `json:"success"`
		Data    NestGameSession `json:"data"`
	}
	body := map[string]string{"user1Id": user1ID, "user2Id": user2ID}
	if err := c.do("POST", "/game-sessions", body, &resp); err != nil {
		return nil, fmt.Errorf("create game session: %w", err)
	}
	return &resp.Data, nil
}

// FinishGameSession marks a session as FINISHED and records the winner.
// Calls PATCH /game-sessions/:id/finish (ADMIN).
func (c *Client) FinishGameSession(sessionID string, winnerID *string) error {
	body := map[string]string{}
	if winnerID != nil && *winnerID != "" {
		body["winnerId"] = *winnerID
	}
	path := fmt.Sprintf("/game-sessions/%s/finish", sessionID)
	return c.do("PATCH", path, body, nil)
}
