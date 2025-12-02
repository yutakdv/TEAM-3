package engine;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemEffect {
  // utility class: prevent instantiation
  private static final Logger logger = Core.getLogger();

  private ItemEffect() {
    // prevent instantiation
  }

  /*
   * When a player picks up a duration item,
   * attempt to spend the corresponding amount of coins. If the
   * player doesn't have enough coins the effect is not applied.
   */

  /* =========================SINGLE USE=================================* */

  /**
   * Applies the coin item effect to the specified player.
   *
   * @param gameState current game state instance.
   * @param playerId ID of the player to apply the effect to.
   * @param coinAmount amount of coins to add.
   */
  public static void applyCoinItem(
      final GameState gameState, final int playerId, final int coinAmount) {
    if (gameState == null) {
      return;
    }
    final int playerIndex = getPlayerIndex(playerId);
    final int beforeCoin = gameState.getCoins();

    gameState.addCoins(playerIndex, coinAmount);
    if (logger.isLoggable(Level.INFO)) {
      logger.info(
          "Player "
              + playerId
              + " added "
              + coinAmount
              + " coins. before : "
              + beforeCoin
              + ", after : "
              + gameState.getCoins());
    }
  }

  /**
   * Applies the heal item effect to the specified player.
   *
   * @param gameState current game state instance.
   * @param playerId ID of the player to apply the effect to.
   * @param lifeAmount amount of lives to add.
   */
  public static void applyHealItem(
      final GameState gameState, final int playerId, final int lifeAmount) {
    if (gameState == null) {
      return;
    }
    final int beforeLife = gameState.getLivesRemaining();

    // if 2p mode
    if (gameState.isCoop()) {
      if (gameState.getTeamLives() + lifeAmount > gameState.getTeamLivesCap()) {
        // if adding life exceeds max, add score and coin instead
        gameState.addCoins(getPlayerIndex(playerId), lifeAmount * 20);
        gameState.addScore(getPlayerIndex(playerId), lifeAmount * 20);
      } else {
        gameState.addLife(getPlayerIndex(playerId), lifeAmount);
      }
    } else { // 1p mode
      if (gameState.get1PlayerLives() + lifeAmount > 3) {
        // if adding life exceeds max, add score and coin instead
        gameState.addScore(getPlayerIndex(playerId), lifeAmount * 20);
        gameState.addCoins(getPlayerIndex(playerId), lifeAmount * 20);
      } else {

        gameState.addLife(getPlayerIndex(playerId), lifeAmount);
      }
    }
    if (logger.isLoggable(Level.INFO)) {
      logger.info(
          "Player added "
              + lifeAmount
              + " lives. before : "
              + beforeLife
              + ", after : "
              + gameState.getLivesRemaining());
    }
  }

  /**
   * Applies the score item effect to the specified player.
   *
   * @param gameState current game state instance.
   * @param playerId ID of the player to apply the effect to.
   * @param scoreAmount amount of score to add.
   */
  public static void applyScoreItem(
      final GameState gameState, final int playerId, final int scoreAmount) {
    if (gameState == null) {
      return;
    }
    final int playerIndex = getPlayerIndex(playerId);
    final int beforeScore = gameState.getScore(playerIndex);

    gameState.addScore(getPlayerIndex(playerId), scoreAmount);
    if (logger.isLoggable(Level.INFO)) {
      logger.info(
          "[ItemEffect - SCORE] Player "
              + playerId
              + " : "
              + beforeScore
              + " + "
              + scoreAmount
              + " -> "
              + gameState.getScore(playerIndex));
    }
  }

  /**
   * Converts a playerId (unknown : 0, player1 : 1, player2 : 2) to the corresponding array index.
   *
   * @param playerId ID of the player (0, 1, 2)
   * @return array index (player1 or unknown : 0, player2 : 1)
   */
  private static int getPlayerIndex(final int playerId) {
    return (playerId == 2) ? 1 : 0;
  }
}
