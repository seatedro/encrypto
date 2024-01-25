package server

import (
	"context"
	"strconv"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgtype"
	"github.com/rohitp934/encrypto-server/internal/sql"
)

func (s *FiberServer) RegisterFiberRoutes() {
	s.Get("/", s.healthHandler)
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
	dbUser, err := s.Db.CreateUser(context.Background(), sql.CreateUserParams{
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

func (s *FiberServer) Login()
