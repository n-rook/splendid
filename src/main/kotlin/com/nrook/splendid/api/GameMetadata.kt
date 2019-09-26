import com.nrook.splendid.auth.UserAccount
import java.time.Instant

/**
 * A game being played between a user and an AI.
 */
data class GameMetadata(
    val id: Int,
    val playerOneUser: UserAccount?,
    val playerOneAi: Int?,
    val playerTwoUser: UserAccount?,
    val playerTwoAi: Int?,
    val startTime: Instant
) {
  init {
    if (playerOneUser == null && playerOneAi == null) throw Error("Player 1 is null");
    if (playerTwoUser == null && playerTwoAi == null) throw Error("Player 2 is null");
    if (playerOneUser != null && playerOneAi != null) throw Error("Both user and AI set for P1")
    if (playerTwoUser != null && playerTwoAi != null) throw Error("Both user and AI set for P1")
  }
}
