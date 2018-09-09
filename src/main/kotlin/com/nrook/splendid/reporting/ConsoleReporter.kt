package com.nrook.splendid.reporting

import com.nrook.splendid.rules.ChipColor
import com.nrook.splendid.rules.Color
import com.nrook.splendid.rules.DevelopmentCard
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.Row
import com.nrook.splendid.rules.Turn
import com.nrook.splendid.rules.moves.BuyDevelopment
import com.nrook.splendid.rules.moves.Move
import com.nrook.splendid.rules.moves.TakeTokens

class ConsoleReporter {
  fun describeGame(g: Game) {
    val string = StringBuilder()
    for (row in Row.values().reversed()) {
      string.appendln("Row $row")
      for (card in g.developments.rows[row]) {
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
      is TakeTokens -> "Took tokens ${tokensToList(m.tokens)}"
      is BuyDevelopment -> {
        val priceDescription = if (m.price.isEmpty())
          "for free"
        else "with ${tokensToList(m.price)}"

        "Bought card: ${describeCard(m.card)} $priceDescription"
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
    ChipColor.BLUE -> "B"
  }
}