
-- Naming scheme:
-- CamelCase
-- Singular

-- Table structure and hierarchy:
-- Enum tables:
-- Player. Represents either "Player 1" (ID 0) or "Player 2" (ID 1).
-- AI: Represents AI algorithms which are available. Right now this is basically an enum.
--
-- Backend stuff:
-- TrainingGameRecord. Represents complete self-play games used for training.
--
-- User-facing stuff (not yet implemented)
-- Game. Represents a (possibly ongoing) game.
-- 

-- Which player this is.
-- Player 1 has ID 0. Player 2 has ID 1.
CREATE TABLE IF NOT EXISTS Player(
  id INTEGER PRIMARY KEY ASC
);

INSERT OR IGNORE INTO Player (id) VALUES (0);
INSERT OR IGNORE INTO Player (id) VALUES (1);

CREATE TABLE IF NOT EXISTS Ai(
  id INTEGER PRIMARY KEY ASC,
  name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS TrainingGameRecord(
  id INTEGER PRIMARY KEY ASC,

  playerOne INTEGER NOT NULL,
  playerTwo INTEGER NOT NULL,
  outcome INTEGER NOT NULL,
  startTime INTEGER NOT NULL,  -- Seconds past the epoch
  FOREIGN KEY(playerOne) REFERENCES Ai(id),
  FOREIGN KEY(playerTwo) REFERENCES Ai(id)
  FOREIGN KEY(outcome) REFERENCES Player(id)
);
