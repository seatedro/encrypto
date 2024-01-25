package server

import (
	"context"
	"log"
	"os"

	"github.com/gofiber/fiber/v2"
	"github.com/jackc/pgx/v5"
	"github.com/rohitp934/encrypto-server/internal/database"
	"github.com/rohitp934/guam-adapters/postgresql"
	"github.com/rohitp934/guam/auth"
	"github.com/rohitp934/guam/middleware"
)

var a *auth.Auth

type FiberServer struct {
	*fiber.App
	Db *database.Service
}

func New() *FiberServer {
	ctx := context.Background()
	db, err := pgx.Connect(ctx, os.Getenv("DATABASE_URL"))
	if err != nil {
		log.Fatalf("Unable to connect to database: %v\n", err)
	}
	config := auth.Configuration{
		Env:        auth.ENV_DEVELOPMENT,
		Middleware: middleware.Fiber(),
		Adapter: postgresql.PostgresAdapter(ctx, db, postgresql.Tables{
			User:    "auth_user",
			Session: "user_session",
			Key:     "user_key",
		}, false),
		GetUserAttributes: func(databaseUser auth.UserSchema) map[string]interface{} {
			return map[string]interface{}{
				"username": databaseUser.Attributes["username"],
			}
		},
		Experimental: auth.Experimental{
			DebugMode: false,
		},
	}
	a, err = auth.Guam(config)
	if err != nil {
		log.Fatalf("Error initializing auth: %v\n", err)
	}
	server := &FiberServer{
		App: fiber.New(),
		Db:  database.New(),
	}

	return server
}
