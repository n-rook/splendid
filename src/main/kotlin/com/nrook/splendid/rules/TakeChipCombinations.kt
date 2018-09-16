package com.nrook.splendid.rules

import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Multiset
import com.google.common.collect.Multisets
import com.google.common.collect.Sets
import kotlin.math.min

/**
 * Returns available combinations to take some number of chips, and to give some number back.
 */
fun takeDifferentChips(
    open: ImmutableMultiset<ChipColor>,
    mine: ImmutableMultiset<ChipColor>,
    take: Int,
    give: Int): ImmutableSet<TakeChipsResults> {
  // do recursion or something
  val availableColors = ImmutableSet.copyOf(open)
  val resultsBuilder = ImmutableSet.builder<TakeChipsResults>()

  if (min(take, give) > 0) {
    for (i in 1..min(take, give)) {
      resultsBuilder.addAll(
          takeDifferentChips(
              open, mine, take - i, give - i
          ))
    }
  }

  if (availableColors.size < take) {
    return ImmutableSet.of()
  }

  val takeOptions = Sets.combinations(availableColors, take).map { ImmutableMultiset.copyOf(it) }
  for (option in takeOptions) {
    if (give == 0) {
      resultsBuilder.add(
          TakeChipsResults(
              option, ImmutableMultiset.of()))
    } else {
      val availableToGive =
          ImmutableMultiset.copyOf(Multisets.filter(mine) { !option.contains(it) })
      for (giveCombination in multisetCombinations(availableToGive, give)) {
        resultsBuilder.add(TakeChipsResults(option, ImmutableMultiset.copyOf(giveCombination)))
      }
    }
  }

  return resultsBuilder.build()
}

fun takeSameChips(
    open: ImmutableMultiset<ChipColor>,
    mine: ImmutableMultiset<ChipColor>,
    take: Int,
    give: Int
): ImmutableSet<TakeChipsResults> {
  val resultsBuilder = ImmutableSet.builder<TakeChipsResults>()

  // Take, and then immediately return.
  if (min(take, give) > 0) {
    for (i in 1..min(take, give)) {
      resultsBuilder.addAll(
          takeSameChips(
              open, mine, take - i, give - i
          )
      )
    }
  }

  val takeColors = open.entrySet().filter { it.count >= take }.map { it.element }
  for (color in takeColors) {
    val takenChips: ImmutableMultiset<ChipColor> = ImmutableMultiset.builder<ChipColor>()
        .addCopies(color, take)
        .build()

    if (give == 0) {
      resultsBuilder.add(TakeChipsResults(takenChips, ImmutableMultiset.of()))
    } else {
      val availableToGive =
          ImmutableMultiset.copyOf(Multisets.filter(mine) { it != color })
      for (giveCombination in multisetCombinations(availableToGive, give)) {
        resultsBuilder.add(TakeChipsResults(takenChips, ImmutableMultiset.copyOf(giveCombination)))
      }
    }
  }

  return resultsBuilder.build()
}

data class TakeChipsResults(
    val take: ImmutableMultiset<ChipColor>, val give: ImmutableMultiset<ChipColor>)

// Return all possible combinations of "count" values from m.
// This might be inefficient.
private fun <T> multisetCombinations(m: Multiset<T>, count: Int): Set<Multiset<T>> {
  val builder = ImmutableSet.builder<Multiset<T>>()

  if (count == 0) {
    // The only combination is the empty set.
    return ImmutableSet.of(ImmutableMultiset.of())
  }
  if (m.size < count) {
    // There's no way to get count elements out of this multiset.
    return ImmutableSet.of()
  }

  val someEntry = m.entrySet().first()
  val rest = Multisets.filter(m) {someEntry.element != it}
  // Go through all options with 0, 1, ... n copies of someEntry.
  // n is the maximum number of copies possible: that is, the min of |someEntry| and count.
  for (i in 0..min(someEntry.count, count)) {
    for (restCombination in multisetCombinations(rest, count - i)) {
      builder.add(ImmutableMultiset.builder<T>()
          .addCopies(someEntry.element, i)
          .addAll(restCombination)
          .build()
      )
    }
  }

  return builder.build()
}
