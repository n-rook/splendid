
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
-- GameState: Represents the state of a game on a given turn. Unique across "game" and "turn".
-- Note that P1's turn is represented as "1" and P2's as "1x", so there will have to be two
-- columns for turn. The latest GameState for a game is the current state.

-- RowDevelopment: A development on a row. Uses "DevelopmentId".
-- ChipCount: Number of chips of a color possessed by a player.
-- ReservedDevelopment: Reserved development. Add a column for whether it's public or not.
-- Noble: A noble available. Include a row for who owns them-- P1/P2/nobody.

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
