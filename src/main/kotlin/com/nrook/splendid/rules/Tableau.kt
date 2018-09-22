package com.nrook.splendid.rules

import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Multiset
import com.google.common.collect.Multisets

/**
 * A single player's game area.
 *
 * The tableau consists of a player's chips, their development cards, their nobles, and any
 * development cards they have reserved.
 */
data class Tableau(
    val chips: ImmutableMultiset<ChipColor>,
    val developments: ImmutableSet<DevelopmentCard>,
    val nobles: ImmutableSet<Noble>,
    val reservedDevelopments: ImmutableSet<DevelopmentCard>) {
  init {
    if (reservedDevelopments.size > MAX_RESERVED_DEVELOPMENTS) {
      throw Error("Cannot reserve ${reservedDevelopments.size} developments")
    }
  }

  val victoryPoints
    get() = developments.map { it.victoryPoints }.sum() +
        nobles.map { it.victoryPoints }.sum()

  /**
   * A multiset containing how many free resources of each type are available.
   */
  val developmentResources: ImmutableMultiset<Color> by lazy {
    val builder = ImmutableMultiset.builder<Color>()
    for (d in developments) {
      builder.add(d.color)
    }
    return@lazy builder.build()
  }

  /**
   * Returns how many free resources of this type are available on the tableau.
   */
  fun developmentResources(color: Color): Int {
    return developments.filter { it.color == color }.count()
  }

  /**
   * Returns whether a given development card can be purchased.
   */
  fun canAfford(d: DevelopmentCard): Boolean {
    var goldLeft = chips.count(ChipColor.GOLD)

    for (entry in d.price.entrySet()) {
      val availableResources = developmentResources(entry.element) +
          chips.count(entry.element.toChipColor())
      val goldNecessary = entry.count - availableResources
      if (goldNecessary > 0) {
        goldLeft -= goldNecessary
        if (goldLeft < 0) {
          return false
        }
      }
    }
    return true
  }

  /**
   * Returns whether, after purchasing this card, the player will be visited by this noble.
   */
  fun receivesVisit(newDevelopment: DevelopmentCard, n: Noble): Boolean {
    val newDevelopmentResources = Multisets.sum(
        developmentResources, ImmutableMultiset.of(newDevelopment.color))
    return Multisets.containsOccurrences(newDevelopmentResources, n.requirements)
  }

  /**
   * Returns the unique way to buy a development card using the least gold chips.
   */
  fun minimalPrice(d: DevelopmentCard): ImmutableMultiset<ChipColor> {
    var goldLeft = chips.count(ChipColor.GOLD)

    val chipPrice: ImmutableMultiset.Builder<ChipColor> = ImmutableMultiset.builder()
    for (entry in d.price.entrySet()) {
      val chipsNecessary = entry.count - developmentResources(entry.element)
      if (chipsNecessary <= 0) {
        continue
      }

      val matchingChipsAvailable = chips.count(entry.element.toChipColor())
      if (matchingChipsAvailable >= chipsNecessary) {
        chipPrice.addCopies(entry.element.toChipColor(), chipsNecessary)
      } else {
        val goldToUse = chipsNecessary - matchingChipsAvailable
        chipPrice.addCopies(entry.element.toChipColor(), matchingChipsAvailable)
        chipPrice.addCopies(ChipColor.GOLD, goldToUse)
        goldLeft -= goldToUse
        if (goldLeft < 0) {
          throw Error("No minimal price available; can't afford card")
        }
      }
    }

    return chipPrice.build()
  }

  /**
   * Add additional chips to this tableau.
   */
  fun addChips(addend: Multiset<ChipColor>): Tableau {
    // TODO Consider inlining
    return toBuilder().addChips(addend).build()
  }

  fun toBuilder(): Builder {
    return Builder(chips, developments, HashSet(nobles), HashSet(reservedDevelopments))
  }

  class Builder(
      private var chips: ImmutableMultiset<ChipColor>,
      private var developments: ImmutableSet<DevelopmentCard>,
      private var nobles: HashSet<Noble>,
      private var reservedDevelopments: HashSet<DevelopmentCard>) {
    fun build(): Tableau {
      return Tableau(chips, developments, ImmutableSet.copyOf(nobles), ImmutableSet.copyOf(reservedDevelopments))
    }

    fun addChips(addend: Multiset<ChipColor>): Builder {
      chips = ImmutableMultiset.copyOf(Multisets.sum(chips, addend))
      return this
    }

    fun subtractChips(subtrahend: Multiset<ChipColor>): Builder {
      if (!Multisets.containsOccurrences(chips, subtrahend)) {
        throw Error("Does not contain subtrahend")
      }
      chips = ImmutableMultiset.copyOf(Multisets.difference(chips, subtrahend))
      return this
    }

    fun addDevelopment(d: DevelopmentCard): Builder {
      developments = ImmutableSet.builder<DevelopmentCard>()
          .addAll(developments)
          .add(d)
          .build()
      return this
    }

    fun addNoble(n: Noble): Builder {
      nobles.add(n)
      return this
    }

    fun removeReservedDevelopment(d: DevelopmentCard): Builder {
      if (!reservedDevelopments.contains(d)) {
        throw Error("$d not in reserved developments")
      }
      reservedDevelopments.remove(d)
      return this
    }

    fun addReservedDevelopment(d: DevelopmentCard): Builder {
      reservedDevelopments.add(d)
      return this
    }
  }
}

val EMPTY_TABLEAU = Tableau(
    ImmutableMultiset.of(),
    ImmutableSet.of(),
    ImmutableSet.of(),
    ImmutableSet.of()
)
