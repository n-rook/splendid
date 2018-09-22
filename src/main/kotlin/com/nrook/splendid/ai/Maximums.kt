package com.nrook.splendid.ai

import com.google.common.collect.ImmutableSet
import java.util.ArrayList


fun <T> getMaxes(i: Iterable<T>, scorer: (v: T) -> Double): ImmutableSet<T> {
  var currentMax = Double.NEGATIVE_INFINITY
  var currentMaxes = ArrayList<T>()
  for (v in i) {
    val score = scorer(v)
    if (score > currentMax) {
      currentMax = score
      currentMaxes.clear()
      currentMaxes.add(v)
    } else if (score == currentMax) {
      currentMaxes.add(v)
    }
  }
  return ImmutableSet.copyOf(currentMaxes)
}