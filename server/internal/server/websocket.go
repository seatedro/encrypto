package server

import (
	"encoding/json"
	"sync"

	"github.com/gofiber/contrib/websocket"
)

type User struct {
	Conn      *websocket.Conn
	Username  string
	mu        sync.Mutex
	isClosing bool
}

type Message struct {
	SenderId   string `json:"senderId"`
	ReceiverId string `json:"receiverId"`
	Content    string `json:"content"`
}

var (
	users      = make(map[string]*User)
	register   = make(chan *User)
	unregister = make(chan *User)
)

func RunHub() {
	for {
		select {
		case user := <-register:
			users[user.Username] = user
			println("user registered", user.Username)

		case user := <-unregister:
			if _, ok := users[user.Username]; ok {
				delete(users, user.Username)
				println("user unregistered", user.Username)
			}
		}
	}
}

func Setup(c *websocket.Conn) {
	_, usernameMsg, err := c.ReadMessage()
	if err != nil {
		println("error reading username", err)
		return
	}

	username := string(usernameMsg)

	user := &User{Username: username, Conn: c}
	register <- user

	defer func() {
		unregister <- user
		c.Close()
	}()

	for {
		_, message, err := c.ReadMessage()
		if err != nil {
			if websocket.IsUnexpectedCloseError(err) {
				println("read error:", err)
			}
		}

		var msg Message
		err = json.Unmarshal(message, &msg)
		if err != nil {
			println("error unmarshalling message", err)
			continue
		}

		if msg.ReceiverId != "" {
			if receiverId, ok := users[msg.ReceiverId]; ok {
				sendMessage(receiverId, msg)
			} else {
				println("receiver not found", msg.ReceiverId)
			}
		}
	}
}

func sendMessage(user *User, msg Message) {
	user.mu.Lock()
	defer user.mu.Unlock()
	if user.isClosing {
		return
	}

	if err := user.Conn.WriteJSON(msg); err != nil {
		println("write error: ", err)
		user.isClosing = true
		user.Conn.Close()
		unregister <- user
	}
}
