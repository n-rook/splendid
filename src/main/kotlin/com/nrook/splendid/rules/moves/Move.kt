package com.nrook.splendid.rules.moves

import com.google.common.collect.ImmutableMultiset
import com.nrook.splendid.rules.ChipColor
import com.nrook.splendid.rules.DevelopmentCard

/**
 * A move to be made.
 */
interface Move

/**
 * Take tokens from the central repository.
 *
 * There are two types of token-taking:
 * 1. Take three tokens of different colors.
 * 2. Take two tokens of the same color. This can only be done if there are four or more tokens
 * of that color available.
 * Gold tokens can't be received this way.
 */
data class TakeTokens(val tokens: ImmutableMultiset<ChipColor>): Move

/**
 * Reserve a single development card.
 *
 * It comes with a gold token.
 */
data class ReserveDevelopment(val card: DevelopmentCard): Move

/**
 * Purchase a development card.
 *
 * @property price The chips spent to acquire the card. Note that this price does not include
 *  resources the player has from their own developments.
 */
data class BuyDevelopment(val card: DevelopmentCard, val price: ImmutableMultiset<ChipColor>): Move

// To implement:
// You can only have ten tokens at once, so TakeTokens and ReserveDevelopment should specify the
// returned tokens. For now, this limit is not implemented.
//
// If you gain access to multiple nobles simultaneously, you choose one to get. This is rare,
// so it is not modeled yet.