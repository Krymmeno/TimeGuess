package at.ac.uibk.timeguess.flipflapp.game;

/**
 * class holding game related constants
 */
public final class GameConstants {

  private GameConstants() {}

  public static final String WS_GAME_ROOM_PREFIX = "/topic/gamerooms/%d";

  public static final String GAME_ROOM_UPDATE_MSG = "GAME_ROOM_UPDATE";

  public static final String WS_INVITES_PREFIX = "/topic/invites/%d";

  public static final String INVITES_UPDATE_MSG = "INVITES_UPDATE";

  public static final String WS_GAMES_PREFIX = "/topic/games/%d";

  public static final String GAME_UPDATE_MSG = "GAME_UPDATE";

  public static final String GAME_ABORT_MSG = "GAME_ABORT";
}
