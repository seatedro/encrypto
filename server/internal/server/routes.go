package server

import (
	"context"
	"fmt"
	"strconv"
	"time"

	"github.com/gofiber/contrib/websocket"
	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/seatedro/encrypto-server/internal/sql"
	"github.com/seatedro/guam/auth"
)

func (s *FiberServer) RegisterFiberRoutes() {
	s.Get("/", s.healthHandler)
	s.Get("/encrypto", websocket.New(Setup))
	s.Post("/api/auth/")
}

func (s *FiberServer) healthHandler(c *fiber.Ctx) error {
	return c.JSON(s.Db.Health())
}

func (s *FiberServer) GetUserPublicKey(c *fiber.Ctx) error {
	username := c.Params("username")
	user, err := s.Db.GetUserByUsername(context.Background(), username)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": err.Error(),
		})
	}

	return c.JSON(fiber.Map{
		"username":  user.Username,
		"publicKey": user.Publickey,
	})
}

func (s *FiberServer) GetUserByUsername(c *fiber.Ctx) error {
	username := c.Params("username")
	user, err := s.Db.GetUserByUsername(context.Background(), username)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": err.Error(),
		})
	}
	return c.JSON(fiber.Map{
		"username": user.Username,
	})
}

func (s *FiberServer) Register(c *fiber.Ctx) error {
	var user struct {
		Username    string `json:"username"`
		Password    string `json:"password"`
		DateOfBirth string `json:"dateOfBirth"`
		Publickey   string `json:"publicKey"`
	}
	if err := c.BodyParser(&user); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": err.Error(),
		})
	}

	id := uuid.New()
	epoch, err := strconv.ParseInt(user.DateOfBirth, 10, 64)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": err.Error(),
		})
	}
	dob := time.Unix(epoch, 0)
	publicKey := pgtype.Text{
		String: user.Publickey,
		Valid:  true,
	}
	_, err = s.Db.CreateUser(context.Background(), sql.CreateUserParams{
		ID:          id,
		Username:    user.Username,
		Password:    user.Password,
		Dateofbirth: dob,
		Publickey:   publicKey,
	})
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": err.Error(),
		})
	}
	return c.JSON(fiber.Map{
		"success": true,
		"message": "User registered successfully",
	})
}

func (s *FiberServer) Login(c *fiber.Ctx) error {
	var loginReq struct {
		Username string `json:"username"`
		Password string `json:"password"`
	}
	if err := c.BodyParser(&loginReq); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": err.Error(),
		})
	}

	key := auth.CreateUserKey{
		Password:       &loginReq.Password,
		ProviderId:     "username",
		ProviderUserId: loginReq.Username,
	}

	options := auth.CreateUserOptions{
		Key: &key,
		Attributes: map[string]any{
			"username": loginReq.Username,
		},
	}
	user := a.CreateUser(options)
	if user == nil {
		return c.Status(fiber.StatusBadRequest).SendString("Error creating user")
	}
	fmt.Println("User: ", user)

	sessionOptions := auth.CreateSessionOptions{
		UserId:     user.UserId,
		Attributes: map[string]any{},
	}
	session, err := a.CreateSession(sessionOptions)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).SendString("Error creating session")
	}

	authRequest := a.HandleRequest(c)
	authRequest.SetSession(session)

	return c.JSON(fiber.Map{
		"success": true,
		"message": "User logged in successfully",
	})
}

func (s *FiberServer) Logout(c *fiber.Ctx) error {
	authRequest := a.HandleRequest(c)
	session := authRequest.Validate()
	if session == nil {
		return c.Status(fiber.StatusUnauthorized).SendString("Unauthorized")
	}
	a.InvalidateSession(session.SessionId)

	authRequest.SetSession(nil)

	return c.JSON(fiber.Map{
		"success": true,
		"message": "User logged out successfully",
	})
}

func (s *FiberServer) GetAllChats(c *fiber.Ctx) error {
	username := c.Params("username")
	chats, err := GetAllChats(s.Db, username)
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": err.Error(),
		})
	}

	return c.JSON(fiber.Map{
		"chats": chats,
	})
}
