
-- Naming scheme:
-- CamelCase
-- Singular

-- Table structure and hierarchy:
-- Enum tables:
-- Player. Represents either "Player 1" (ID 0) or "Player 2" (ID 1).
-- AI: Represents AI algorithms which are available. Right now this is basically an enum.
-- ChipColor: Colors of chips.
--
-- Backend stuff:
-- TrainingGameRecord. Represents complete self-play games used for training.
--
-- User-facing stuff (not yet implemented)
-- Game. Represents a (possibly ongoing) game.
-- GameState: Represents the state of a game on a given turn. Unique across "game" and "turn".
-- Note that P1's turn is represented as "1" and P2's as "1x", so there will have to be two
-- columns for turn. The latest GameState for a game is the current state.

-- MarketDevelopment: A development on a row. Uses "DevelopmentId".
-- ChipCount: Number of chips of a color possessed by a player.
-- ReservedDevelopment: Reserved development. Add a column for whether it's public or not.
-- PersonalDevelopment: Development owned by a player.
-- MarketNoble: A noble available.
-- PersonalNoble: A noble owned by a player.

-- Which player this is.
-- Player 1 has ID 0. Player 2 has ID 1.
CREATE TABLE IF NOT EXISTS Player(
  id INTEGER PRIMARY KEY ASC
);

INSERT OR IGNORE INTO Player (id) VALUES (0);
INSERT OR IGNORE INTO Player (id) VALUES (1);

CREATE TABLE IF NOT EXISTS ChipColor(
  id INTEGER PRIMARY KEY ASC,  -- see IDs in ChipColor.kt
  name TEXT UNIQUE NOT NULL
);

INSERT OR IGNORE INTO ChipColor (id, name) VALUES (0, 'GREEN');
INSERT OR IGNORE INTO ChipColor (id, name) VALUES (1, 'BLUE');
INSERT OR IGNORE INTO ChipColor (id, name) VALUES (2, 'RED');
INSERT OR IGNORE INTO ChipColor (id, name) VALUES (3, 'WHITE');
INSERT OR IGNORE INTO ChipColor (id, name) VALUES (4, 'BLACK');
INSERT OR IGNORE INTO ChipColor (id, name) VALUES (5, 'GOLD');

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

CREATE TABLE IF NOT EXISTS UserAccount(
  id INTEGER PRIMARY KEY ASC,
  name TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS Game(
  id INTEGER PRIMARY KEY ASC,
  playerOneUser INTEGER,
  playerOneAi INTEGER,
  playerTwoUser INTEGER,
  playerTwoAi INTEGER,
  startTime INTEGER NOT NULL,
  FOREIGN KEY(playerOneUser) REFERENCES UserAccount(id),
  FOREIGN KEY(playerOneAi) REFERENCES Ai(id),
  FOREIGN KEY(playerTwoUser) REFERENCES UserAccount(id),
  FOREIGN KEY(playerTwoAi) REFERENCES Ai(id),
  -- Only one of the two user identification fields should be null.
  CONSTRAINT "P1ExactlyOne" CHECK((playerOneUser IS NULL) != (playerOneAi IS NULL))
  CONSTRAINT "P2ExactlyOne" CHECK((playerTwoUser IS NULL) != (playerTwoAi IS NULL))
);

CREATE TABLE IF NOT EXISTS GameState(
  id INTEGER PRIMARY KEY ASC,
  game INTEGER NOT NULL,
  turn INTEGER NOT NULL,
  subTurnPlayer INTEGER NOT NULL,
  outcome INTEGER,  -- If the game is won, who won it.
  moveTime INTEGER NOT NULL,

  FOREIGN KEY(game) REFERENCES Game(id)
  FOREIGN KEY(subTurnPlayer) REFERENCES Player(id),
  FOREIGN KEY(outcome) REFERENCES Player(id)
);

CREATE TABLE IF NOT EXISTS MarketDevelopment(
  id INTEGER PRIMARY KEY ASC,
  gameState INTEGER NOT NULL,
  developmentId INTEGER NOT NULL,

  FOREIGN KEY(gameState) REFERENCES GameState(id)
);

CREATE TABLE IF NOT EXISTS ReservedDevelopment(
  id INTEGER PRIMARY KEY ASC,
  gameState INTEGER NOT NULL,
  owner INTEGER NOT NULL,
  developmentId INTEGER NOT NULL,
  -- TODO: It would be nice to track if it's public or not.

  FOREIGN KEY(owner) REFERENCES Player(id),
  FOREIGN KEY(gameState) REFERENCES GameState(id)
);

CREATE TABLE IF NOT EXISTS PersonalDevelopment(
  id INTEGER PRIMARY KEY ASC,
  gameState INTEGER NOT NULL,
  owner INTEGER NOT NULL,
  developmentId INTEGER NOT NULL,

  FOREIGN KEY(owner) REFERENCES Player(id),
  FOREIGN KEY(gameState) REFERENCES GameState(id)
);

-- Number of chips. Creating a row for "0" is optional.
CREATE TABLE IF NOT EXISTS ChipCount(
  id INTEGER PRIMARY KEY ASC,
  gameState INTEGER NOT NULL,
  owner INTEGER NOT NULL,
  chipCount INTEGER NOT NULL,
  chipColor INTEGER NOT NULL,

  FOREIGN KEY(gameState) REFERENCES GameState(id),
  FOREIGN KEY(owner) REFERENCES Player(id),
  FOREIGN KEY(chipColor) REFERENCES ChipColor(id)
);

CREATE TABLE IF NOT EXISTS MarketNoble(
  id INTEGER PRIMARY KEY ASC,
  gameState INTEGER NOT NULL,
  nobleId INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS PersonalNoble(
  id INTEGER PRIMARY KEY ASC,
  gameState INTEGER NOT NULL,
  owner INTEGER NOT NULL,
  nobleId INTEGER NOT NULL
);
