package hub

import (
	"encoding/json"
	"log"
	"sync"
	"time"

	gorillaws "github.com/gorilla/websocket"
)

const (
	writeWait      = 10 * time.Second
	pongWait       = 60 * time.Second
	pingPeriod     = (pongWait * 9) / 10
	maxMessageSize = 2048
)

// Client wraps a WebSocket connection for one authenticated player.
type Client struct {
	UserID   string
	Username string
	Role     string

	conn *gorillaws.Conn
	hub  *Hub
	send chan []byte

	// room is set when the client is matched into a game.
	room *Room

	mu     sync.Mutex
	closed bool
}

func newClient(userID, username, role string, conn *gorillaws.Conn, hub *Hub) *Client {
	return &Client{
		UserID:   userID,
		Username: username,
		Role:     role,
		conn:     conn,
		hub:      hub,
		send:     make(chan []byte, 256),
	}
}

// Send enqueues a message for delivery. Safe to call from any goroutine.
func (c *Client) Send(data []byte) {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.closed {
		return
	}
	select {
	case c.send <- data:
	default:
		log.Printf("[client %s] send buffer full, dropping message", c.UserID)
	}
}

// WritePump drains the send channel and writes to the WebSocket.
// It also sends periodic pings to keep the connection alive.
func (c *Client) WritePump() {
	ticker := time.NewTicker(pingPeriod)
	defer func() {
		ticker.Stop()
		c.conn.Close()
	}()

	for {
		select {
		case msg, ok := <-c.send:
			c.conn.SetWriteDeadline(time.Now().Add(writeWait))
			if !ok {
				c.conn.WriteMessage(gorillaws.CloseMessage, []byte{})
				return
			}
			if err := c.conn.WriteMessage(gorillaws.TextMessage, msg); err != nil {
				return
			}

		case <-ticker.C:
			c.conn.SetWriteDeadline(time.Now().Add(writeWait))
			if err := c.conn.WriteMessage(gorillaws.PingMessage, nil); err != nil {
				return
			}
		}
	}
}

// ReadPump reads inbound messages from the WebSocket connection.
// When it returns the client is unregistered from the hub.
func (c *Client) ReadPump() {
	defer func() {
		c.hub.unregister <- c
		c.conn.Close()
	}()

	c.conn.SetReadLimit(maxMessageSize)
	c.conn.SetReadDeadline(time.Now().Add(pongWait))
	c.conn.SetPongHandler(func(string) error {
		c.conn.SetReadDeadline(time.Now().Add(pongWait))
		return nil
	})

	for {
		_, raw, err := c.conn.ReadMessage()
		if err != nil {
			if gorillaws.IsUnexpectedCloseError(err,
				gorillaws.CloseGoingAway,
				gorillaws.CloseAbnormalClosure) {
				log.Printf("[client %s] read error: %v", c.UserID, err)
			}
			return
		}
		c.dispatch(raw)
	}
}

// dispatch routes an inbound message to the correct handler.
func (c *Client) dispatch(raw []byte) {
	var msg Message
	if err := json.Unmarshal(raw, &msg); err != nil {
		log.Printf("[client %s] malformed message: %v", c.UserID, err)
		return
	}

	switch msg.Type {
	case TypeAnswer:
		var p AnswerPayload
		if err := json.Unmarshal(msg.Payload, &p); err != nil {
			log.Printf("[client %s] bad answer payload: %v", c.UserID, err)
			return
		}
		if c.room != nil {
			c.room.HandleAnswer(c, p)
		}

	case TypePing:
		if data, err := build(TypePong, nil); err == nil {
			c.Send(data)
		}

	default:
		log.Printf("[client %s] unknown message type: %q", c.UserID, msg.Type)
	}
}

// close shuts down the send channel exactly once.
func (c *Client) close() {
	c.mu.Lock()
	defer c.mu.Unlock()
	if !c.closed {
		c.closed = true
		close(c.send)
	}
}
