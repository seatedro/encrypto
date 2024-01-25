-- Users
CREATE TABLE users (
  id UUID PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  dateofbirth DATE NOT NULL,
  publickey VARCHAR(5000)
);


-- Messages
CREATE TABLE messages (
  id UUID PRIMARY KEY,
  senderid UUID NOT NULL,
  receiverid UUID NOT NULL,
  message VARCHAR(5000) NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  FOREIGN KEY (senderid) REFERENCES users(id),
  FOREIGN KEY (receiverid) REFERENCES users(id)
);
