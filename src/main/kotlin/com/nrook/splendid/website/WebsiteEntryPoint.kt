package com.nrook.splendid.website

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
  val server = io.ktor.server.engine.embeddedServer(Netty, 8080) {
    routing {
      get("/") {
        call.respondText("Hello, world!", ContentType.Text.Plain)
      }
    }
  }

  server.start(true)
}

// API design
// Game API:
// POST /game/start: Start new game. Returns Game object with URL and ID
// GET /game/123: Get game data.
// GET /game/123/moves: Get available moves.
// GET /game/123/0, /game/123/0x, etc: Get historical data on previous turn.
// POST /game/123/move: Make a move. Returns updated game info.

// DATA TYPES:
// "game"
// "move"
//