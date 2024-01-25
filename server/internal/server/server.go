package server

import (
	"github.com/gofiber/fiber/v2"
	"github.com/rohitp934/encrypto-server/internal/database"
)

type FiberServer struct {
	*fiber.App
	Db database.Service
}

func New() *FiberServer {
	server := &FiberServer{
		App: fiber.New(),
		Db:  database.New(),
	}

	return server
}
