// Package hub — Room is the core game session between two players.
// It owns the game loop goroutine and all round state.

package hub

import (
	"log"
	"math/rand/v2"
	"sync"
	"time"

	"mathduel-ws/api"
)

// answerEvent carries a player's answer into the game loop.
type answerEvent struct {
	client  *Client
	payload AnswerPayload
}

// RoomConfig holds per-room game settings derived from Config.
type RoomConfig struct {
	TotalRounds  int
	RoundTimeout time.Duration
	Difficulty   string
}

// Room manages the lifecycle of one game between Player1 and Player2.
type Room struct {
	ID      string
	Player1 *Client
	Player2 *Client

	cfg       *RoomConfig
	apiClient *api.Client

	// answers receives inbound answers from both players' ReadPump goroutines.
	answers chan answerEvent
	// done is closed when the room must abort (player disconnect, fatal error).
	done chan struct{}
	once sync.Once

	// mutable state — guarded by mu
	mu          sync.Mutex
	sessionID   string
	currentQ    *api.NestQuestion
	p1Answered  bool
	p2Answered  bool
	scores      map[string]int
	allQuestions []api.NestQuestion
	questionPool []api.NestQuestion
}

func newRoom(id string, p1, p2 *Client, cfg *RoomConfig, apiClient *api.Client) *Room {
	r := &Room{
		ID:        id,
		Player1:   p1,
		Player2:   p2,
		cfg:       cfg,
		apiClient: apiClient,
		answers:   make(chan answerEvent, 8),
		done:      make(chan struct{}),
		scores:    map[string]int{p1.UserID: 0, p2.UserID: 0},
	}
	p1.room = r
	p2.room = r
	return r
}

// Start launches the game loop in a separate goroutine.
func (r *Room) Start() {
	go r.run()
}

// ── Game loop ─────────────────────────────────────────────────────────────────

func (r *Room) run() {
	defer r.cleanup()

	// 1. Persist the session in NestJS so the result can be saved later.
	session, err := r.apiClient.CreateGameSession(r.Player1.UserID, r.Player2.UserID)
	if err != nil {
		log.Printf("[room %s] create session error: %v", r.ID, err)
		r.broadcastError("INIT_FAILED", "No se pudo crear la sesión en el servidor.")
		return
	}

	r.mu.Lock()
	r.sessionID = session.ID
	r.mu.Unlock()

	pool, err := r.apiClient.GetPlayableQuestions()
	if err != nil {
		log.Printf("[room %s] get questions pool error: %v", r.ID, err)
		r.broadcastError("QUESTION_UNAVAILABLE", "No se pudieron cargar preguntas.")
		return
	}

	if len(pool) == 0 {
		log.Printf("[room %s] no playable questions found", r.ID)
		r.broadcastError("QUESTION_UNAVAILABLE", "No hay preguntas disponibles para iniciar la partida.")
		return
	}

	r.mu.Lock()
	r.allQuestions = append([]api.NestQuestion(nil), pool...)
	r.questionPool = append([]api.NestQuestion(nil), pool...)
	r.mu.Unlock()

	// 2. Notify both players — game is on.
	r.notifyGameStarted()

	// 3. Play N rounds.
	for round := 1; round <= r.cfg.TotalRounds; round++ {
		if aborted := r.playRound(round); aborted {
			return
		}
	}

	// 4. Determine winner and broadcast final result.
	winnerID := r.determineWinner()
	reason := "rounds_completed"
	if winnerID == "" {
		reason = "draw"
	}
	r.broadcastGameOver(winnerID, reason)

	// 5. Persist the result.
	var winnerPtr *string
	if winnerID != "" {
		winnerPtr = &winnerID
	}
	if err := r.apiClient.FinishGameSession(r.sessionID, winnerPtr); err != nil {
		log.Printf("[room %s] finish session error: %v", r.ID, err)
	}
}

// playRound runs a single round; returns true if the game was aborted.
func (r *Room) playRound(roundNum int) (aborted bool) {
	// Reset per-round state before fetching the question.
	r.mu.Lock()
	r.currentQ = nil
	r.p1Answered = false
	r.p2Answered = false
	r.mu.Unlock()

	// Drain any stale answers from the previous round.
	for len(r.answers) > 0 {
		<-r.answers
	}

	question, ok := r.takeUniqueQuestion()
	if !ok {
		log.Printf("[room %s] no unique questions left for round %d", r.ID, roundNum)
		r.broadcastError("QUESTION_UNAVAILABLE", "No hay preguntas suficientes para continuar.")
		return true
	}

	r.mu.Lock()
	r.currentQ = question
	r.mu.Unlock()

	// Push the question to both players (without the correct option).
	r.broadcastRoundStarted(roundNum, question)

	// Wait for answers or timeout.
	timer := time.NewTimer(r.cfg.RoundTimeout)
	defer timer.Stop()

	roundWinner := ""
	received := 0

collect:
	for received < 2 {
		select {
		case event, ok := <-r.answers:
			if !ok {
				return true
			}
			received++

			r.mu.Lock()
			correct := r.currentQ != nil && event.payload.OptionID == r.currentQ.CorrectOptionID
			alreadyWon := roundWinner != ""
			r.mu.Unlock()

			if correct && !alreadyWon {
				roundWinner = event.client.UserID
				r.mu.Lock()
				r.scores[event.client.UserID]++
				r.mu.Unlock()
			}

		case <-timer.C:
			break collect

		case <-r.done:
			return true
		}
	}

	// Announce round outcome.
	r.mu.Lock()
	correctID := ""
	if r.currentQ != nil {
		correctID = r.currentQ.CorrectOptionID
	}
	scores := copyScores(r.scores)
	r.mu.Unlock()

	r.broadcastRoundResult(roundNum, roundWinner, correctID, scores)

	// Brief pause before the next round so clients can display the result.
	select {
	case <-time.After(3 * time.Second):
	case <-r.done:
		return true
	}

	return false
}

func (r *Room) takeUniqueQuestion() (*api.NestQuestion, bool) {
	r.mu.Lock()
	defer r.mu.Unlock()

	if len(r.questionPool) == 0 {
		if len(r.allQuestions) == 0 {
			return nil, false
		}
		r.questionPool = append([]api.NestQuestion(nil), r.allQuestions...)
	}

	idx := rand.IntN(len(r.questionPool))
	q := r.questionPool[idx]
	r.questionPool[idx] = r.questionPool[len(r.questionPool)-1]
	r.questionPool = r.questionPool[:len(r.questionPool)-1]

	return &q, true
}

// HandleAnswer is called from a player's ReadPump goroutine.
func (r *Room) HandleAnswer(c *Client, p AnswerPayload) {
	r.mu.Lock()
	defer r.mu.Unlock()

	// Ignore answers for a question we are not currently playing.
	if r.currentQ == nil || p.QuestionID != r.currentQ.ID {
		return
	}

	// Each player may answer at most once per round.
	if c.UserID == r.Player1.UserID {
		if r.p1Answered {
			return
		}
		r.p1Answered = true
	} else {
		if r.p2Answered {
			return
		}
		r.p2Answered = true
	}

	select {
	case r.answers <- answerEvent{client: c, payload: p}:
	default:
	}
}

// PlayerDisconnected is called by the hub when a player's connection drops.
// It declares the remaining player the winner and shuts the room down.
func (r *Room) PlayerDisconnected(disconnected *Client) {
	r.once.Do(func() {
		var winner *Client
		if disconnected.UserID == r.Player1.UserID {
			winner = r.Player2
		} else {
			winner = r.Player1
		}

		log.Printf("[room %s] player %s disconnected — %s wins by forfeit",
			r.ID, disconnected.UserID, winner.UserID)

		// Signal the game loop to stop.
		close(r.done)

		r.mu.Lock()
		scores := copyScores(r.scores)
		sid := r.sessionID
		r.mu.Unlock()

		// Notify the surviving player.
		if data, err := build(TypeGameOver, GameOverPayload{
			WinnerID: winner.UserID,
			Reason:   "opponent_disconnected",
			Scores:   scores,
		}); err == nil {
			winner.Send(data)
		}

		// Persist the result if the session was already created.
		if sid != "" {
			winnerID := winner.UserID
			if err := r.apiClient.FinishGameSession(sid, &winnerID); err != nil {
				log.Printf("[room %s] finish session (disconnect) error: %v", r.ID, err)
			}
		}
	})
}

// ── Helpers ───────────────────────────────────────────────────────────────────

func (r *Room) determineWinner() string {
	p1 := r.scores[r.Player1.UserID]
	p2 := r.scores[r.Player2.UserID]
	if p1 == p2 {
		return ""
	}
	if p1 > p2 {
		return r.Player1.UserID
	}
	return r.Player2.UserID
}

func (r *Room) cleanup() {
	r.Player1.room = nil
	r.Player2.room = nil
}

func (r *Room) broadcast(data []byte) {
	r.Player1.Send(data)
	r.Player2.Send(data)
}

func (r *Room) notifyGameStarted() {
	send := func(to, opponent *Client) {
		data, err := build(TypeGameStarted, GameStartedPayload{
			SessionID:   r.sessionID,
			Opponent:    PlayerInfo{UserID: opponent.UserID, Username: opponent.Username},
			TotalRounds: r.cfg.TotalRounds,
		})
		if err == nil {
			to.Send(data)
		}
	}
	send(r.Player1, r.Player2)
	send(r.Player2, r.Player1)
}

func (r *Room) broadcastRoundStarted(roundNum int, q *api.NestQuestion) {
	opts := make([]QuestionOption, len(q.Options))
	for i, o := range q.Options {
		opts[i] = QuestionOption{ID: o.ID, Text: o.Text}
	}
	data, err := build(TypeRoundStarted, RoundStartedPayload{
		RoundNumber: roundNum,
		Question: QuestionData{
			ID:         q.ID,
			Statement:  q.Statement,
			Difficulty: q.Difficulty,
			Options:    opts,
			// CorrectOptionID intentionally omitted — never sent to clients.
		},
		TimeLimit: int(r.cfg.RoundTimeout.Seconds()),
	})
	if err == nil {
		r.broadcast(data)
	}
}

func (r *Room) broadcastRoundResult(roundNum int, winnerID, correctOptionID string, scores map[string]int) {
	data, err := build(TypeRoundResult, RoundResultPayload{
		RoundNumber:     roundNum,
		WinnerID:        winnerID,
		CorrectOptionID: correctOptionID,
		Scores:          scores,
	})
	if err == nil {
		r.broadcast(data)
	}
}

func (r *Room) broadcastGameOver(winnerID, reason string) {
	r.mu.Lock()
	scores := copyScores(r.scores)
	r.mu.Unlock()

	data, err := build(TypeGameOver, GameOverPayload{
		WinnerID: winnerID,
		Reason:   reason,
		Scores:   scores,
	})
	if err == nil {
		r.broadcast(data)
	}
}

func (r *Room) broadcastError(code, msg string) {
	data, err := build(TypeError, ErrorPayload{Code: code, Message: msg})
	if err == nil {
		r.broadcast(data)
	}
}

func copyScores(s map[string]int) map[string]int {
	out := make(map[string]int, len(s))
	for k, v := range s {
		out[k] = v
	}
	return out
}
