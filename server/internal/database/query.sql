-- name: CreateUser :one
INSERT INTO users (
  id,
  username,
  password,
  dateofbirth,
  publickey
) VALUES (
  $1,
  $2,
  $3,
  $4,
  $5
) RETURNING *;

-- name: CreateMessage :one
INSERT INTO messages (
  id,
  senderid,
  receiverid,
  message,
  timestamp
) VALUES (
  $1,
  $2,
  $3,
  $4,
  $5
) RETURNING *;

-- name: GetAllChatsForUser :many
SELECT * FROM messages WHERE senderid = $1 OR receiverid = $1 ORDER BY timestamp ASC;

-- name: GetMessageBySenderAndReceiver :many
SELECT * FROM messages WHERE senderid = $1 AND receiverid = $2 ORDER BY timestamp ASC;

-- name: GetUserByUsername :one
SELECT * FROM users WHERE username = $1;
