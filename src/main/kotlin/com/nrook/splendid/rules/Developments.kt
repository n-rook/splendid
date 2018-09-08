package com.nrook.splendid.rules

import com.google.common.collect.ImmutableSetMultimap

/**
 * The center of the table, where development cards are available to buy.
 */
data class Developments(val rows: ImmutableSetMultimap<Row, DevelopmentCard>)
