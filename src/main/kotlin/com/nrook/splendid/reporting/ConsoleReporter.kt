package com.nrook.splendid.reporting

import com.nrook.splendid.rules.ChipColor
import com.nrook.splendid.rules.Color
import com.nrook.splendid.rules.DevelopmentCard
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Noble
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.Row
import com.nrook.splendid.rules.Turn
import com.nrook.splendid.rules.moves.BuyDevelopment
import com.nrook.splendid.rules.moves.Move
import com.nrook.splendid.rules.moves.ReserveDevelopment
import com.nrook.splendid.rules.moves.TakeTokens

class ConsoleReporter {
  fun describeGame(g: Game) {
    val string = StringBuilder()
    for (row in Row.values().reversed()) {
      string.appendln("Row $row")
      for (card in g.developments.rows[row]!!.cards) {
        string.appendln("  - ${describeCard(card)}")
      }
    }
    val chipInfo = g.chips.entrySet().map { "${it.count}${encodeToken(it.element)}" }
        .joinToString(" ")
    string.appendln("Chips: $chipInfo")
    print(string)
  }

  fun reportMove(g: Game, m: Move) {
    describeGame(g)
    println()

    val moveDescription = when (m) {
      is TakeTokens -> {
        val tookTokens = "Took tokens ${tokensToList(m.tokens)}"
        val returnedTokens = if (m.returnedTokens.isEmpty()) ""
          else " (returned ${tokensToList(m.returnedTokens)})"

        tookTokens + returnedTokens
      }
      is BuyDevelopment -> {
        val noun = if (m.isReserved) "reserved card" else "card"
        val priceDescription = if (m.price.isEmpty())
          "for free"
        else "with ${tokensToList(m.price)}"
        val nobleDescription = if (m.noble == null)
          ""
        else " and visited by ${describeNoble(m.noble)}"

        "Bought $noun: ${describeCard(m.card)} $priceDescription$nobleDescription"
      }
      is ReserveDevelopment -> {
        val goldDescription = if (g.reserveDevelopmentMoveGetsGold())
          " (+1 [${encodeToken(ChipColor.GOLD)}])"
        else ""
        "Reserved card: ${describeCard(m.card)}$goldDescription"
      }
      else -> "Unknown move $m"
    }

    println("${describeTurn(g.turn)} | $moveDescription")
  }
}

private fun describeTurn(t: Turn): String {
  return when(t.player) {
    Player.ONE -> "${t.index} "
    Player.TWO -> "${t.index}x"
  }
}

private fun describeCard(d: DevelopmentCard): String {
  val price = d.price.entrySet().joinToString(
      separator = ", ", transform = {e -> "${e.count}${encodeColor(e.element)}"})
  return "${encodeColor(d.color)} (${d.victoryPoints}): $price"
}

private fun describeNoble(n: Noble): String {
  val values = n.requirements.elementSet().joinToString(" ", transform = { encodeColor(it)})
  return "N[$values]"
}

private fun tokensToList(t: Iterable<ChipColor>): String {
  return t.joinToString(separator = " ", transform = {c -> "(${encodeToken(c)})"})
}

private fun encodeColor(c: Color): String {
  return encodeToken(c.toChipColor())
}

private fun encodeToken(c: ChipColor): String {
  return when(c) {
    ChipColor.GOLD -> "X"
    ChipColor.GREEN -> "G"
    ChipColor.BLACK -> "B"
    ChipColor.WHITE -> "W"
    ChipColor.RED -> "R"
    ChipColor.BLUE -> "U"
  }
}