package server

import (
	"context"

	"github.com/seatedro/encrypto-server/internal/database"
	"github.com/seatedro/encrypto-server/internal/sql"
)

func GetAllChats(db *database.Service, username string) (map[string][]sql.Message, error) {
	sender, err := db.GetUserByUsername(context.Background(), username)
	if err != nil {
		return nil, err
	}

	messages, err := db.GetAllChatsForUser(context.Background(), sender.ID)
	if err != nil {
		return nil, err
	}
	users := map[string][]sql.Message{}
	for _, message := range messages {
		var user sql.User
		if message.Senderid == sender.ID {
			user, err = db.GetUserById(context.Background(), message.Receiverid)
			if err != nil {
				return nil, err
			}
		} else {
			user, err = db.GetUserById(context.Background(), message.Senderid)
			if err != nil {
				return nil, err
			}
		}
		chat := []sql.Message{}
		chat = append(chat, message)
		users[user.Username] = chat
	}
	return users, nil
}
