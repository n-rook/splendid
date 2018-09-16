package com.nrook.splendid.rules.cards

import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.io.Resources
import com.nrook.splendid.rules.Color
import com.nrook.splendid.rules.DevelopmentCard
import com.nrook.splendid.rules.Noble
import com.nrook.splendid.rules.Row
import java.nio.charset.Charset

// Thanks to Raph Moimoi for originally compiling this list, and Alwin Wiewiorkafor correcting it

fun loadComponentsFromFile(): GameComponents {
  return GameComponents(loadDevelopmentCardsFromFile(), loadNoblesFromFile())
}

private fun loadDevelopmentCardsFromFile(): ImmutableSetMultimap<Row, DevelopmentCard> {
  val cards = ImmutableSetMultimap.builder<Row, DevelopmentCard>()

  val resource = Resources.getResource("developments.csv")
  for (line in Resources.readLines(resource, Charset.forName("UTF-8"))) {
    val (row, card) = parseDevelopmentFromLine(line)
    cards.put(row, card)
  }

  return cards.build()
}

private fun parseDevelopmentFromLine(line: String): Pair<Row, DevelopmentCard> {
  val split = line.split(",")

  val row = Row.values()[split[0].toInt() - 1]

  val price = ImmutableMultiset.builder<Color>()
  for (color in Color.values()) {
    val stringCount = split[color.ordinal + 1]
    if (!stringCount.isEmpty()) {
      price.addCopies(color, stringCount.toInt())
    }
  }

  val card = DevelopmentCard(
      split[7].toInt(),
      Color.valueOf(split[6].toUpperCase()),
      price.build()
  )
  return Pair(row, card)
}

private fun loadNoblesFromFile(): ImmutableSet<Noble> {
  val cards = ImmutableSet.builder<Noble>()

  val resource = Resources.getResource("nobles.csv")
  for (line in Resources.readLines(resource, Charset.forName("UTF-8"))) {
    cards.add(parseNobleFromLine(line))
  }

  return cards.build()
}

private fun parseNobleFromLine(line: String): Noble {
  val split = line.split(",")

  val requirements = ImmutableMultiset.builder<Color>()
  for (color in Color.values()) {
    val stringCount = split[color.ordinal]
    if (!stringCount.isEmpty()) {
      requirements.addCopies(color, stringCount.toInt())
    }
  }

  return Noble(3, requirements.build())
}
