package at.ac.uibk.timeguess.flipflapp.game;

import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomNotFoundException;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.game.room.InvalidGameRoomException;
import at.ac.uibk.timeguess.flipflapp.game.round.GameRound;
import at.ac.uibk.timeguess.flipflapp.game.round.Result;
import at.ac.uibk.timeguess.flipflapp.team.Team;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipFacetChangeEvent;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserService;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class GameService {

  private final Map<Long, GameControlManager> gameControlManagerMap = new ConcurrentHashMap<>();
  private final GameRepository gameRepository;
  private final UserService userService;
  private final GameRoomService gameRoomService;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private static final Logger LOGGER = LoggerFactory.getLogger(GameService.class);

  public GameService(final GameRepository gameRepository,
      final SimpMessagingTemplate simpMessagingTemplate,
      final GameRoomService gameRoomService,
      final UserService userService) {
    this.gameRepository = gameRepository;
    this.userService = userService;
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.gameRoomService = gameRoomService;
  }

  /**
   * returns true if the user with the given id, is a player of the game with the given id
   *
   * @param gameId id of the desired game
   * @param userId id of the user
   * @return True if the player plays in the specified game
   * @throws GameNotFoundException if the given gameId could not be associated with a running game
   * @throws UserNotFoundException if the given userId could not be associated with a user
   */
  public Boolean isPlayer(Long gameId, Long userId) {
    final User user = userService.getUserIfExists(userId);
    Game game = getGame(gameId);
    return game.getTeams().stream().anyMatch(t -> t.getPlayers().contains(user));
  }

  /**
   * returns true if the user with the given id, is a player of the game with the given id, and does
   * not play in the currently guessing team
   *
   * @param gameId id of the desired game
   * @param userId id of the user
   * @return True if the player plays in the specified game, and is not part of the guessing team
   * @throws GameNotFoundException if the given gameId could not be associated with a running game
   * @throws UserNotFoundException if the given userId could not be associated with a user
   */
  public Boolean isPlayerFromNonGuessingTeam(Long gameId, Long userId) {
    final User user = userService.getUserIfExists(userId);
    GameControlManager manager = getGameControlManager(gameId);
    return isPlayer(gameId, userId) && !manager.getCurrentTeam().getPlayers().contains(user);
  }

  /**
   * Attempt to create a new Game
   *
   * @param gameRoomId the id of the gameRoom which starts the game
   * @return the newly created game
   * @throws InvalidGameRoomException  if the GameRoom is not valid
   * @throws GameRoomNotFoundException if the GameRoom could not be found
   * @throws IllegalArgumentException  if the gameRoom with the given id previously started a game
   */
  public Game startGame(final Long gameRoomId) {
    LOGGER.info("attempt to start game from GameRoom %d".formatted(gameRoomId));

    Long roomId = Optional.ofNullable(gameRoomId)
        .orElseThrow(() -> new IllegalArgumentException("gameRoomId cannot be null"));
    com.spencerwi.either.Result.attempt(() -> getGame(roomId)).ifOk(g -> {
      String errorMessage =
          "cannot create a game from a gameRoom which already started a game" + roomId;
      LOGGER.error(errorMessage);
      throw new IllegalArgumentException(errorMessage);
    });

    GameRoom gameRoom = gameRoomService.getGameRoomIfExists(gameRoomId);
    Game game = Game.of(gameRoom).getRightOrElseThrow(InvalidGameRoomException::new);

    gameControlManagerMap
        .put(game.getGameId(), GameControlManager.of(game, gameRoom.getTimeFlip()));
    gameRoomService.setGameStarted(gameRoom);
    startNewGameRound(game.getGameId());
    LOGGER.info("Game %d started".formatted(gameRoomId));

    sendGameUpdate(game);
    return game;
  }

  /**
   * returns the game with the given id. If the game is both persisted and part of the
   * gameControlManagerMap, we'll return the one of the map entry
   *
   * @param gameId id of the desired game
   * @return game if it exists
   * @throws GameNotFoundException if the given gameId could not found
   */
  public Game getGame(Long gameId) {
    return com.spencerwi.either.Result.attempt(() -> getRunningGame(gameId))
        .fold(e -> gameRepository.findById(gameId)
            .orElseThrow(() -> new GameNotFoundException(gameId)), g -> g);
  }

  /**
   * returns the gameControlManager holding the game with the given id
   *
   * @param gameId id of the desired game
   * @return gameManager if it exists
   * @throws GameNotFoundException if the given gameId could not found
   */
  public GameControlManager getGameControlManager(Long gameId) {
    Long id = Optional.ofNullable(gameId).orElseThrow(() -> new GameNotFoundException(null));
    return Optional.ofNullable(gameControlManagerMap.get(id))
        .orElseThrow(() -> new GameNotFoundException(id));
  }

  /**
   * returns the running game with the given id.
   *
   * @param gameId id of the desired game
   * @return game if it exists
   * @throws GameNotFoundException if the given gameId could not be associated with a running game
   */
  public Game getRunningGame(Long gameId) {
    return getGameControlManager(gameId).getGame();
  }

  /**
   * set the properties of the current GameRound, for the game with the given id
   *
   * @param gameId id of the desired game
   * @param facet  the facet which can be mapped to activity time and roundPoints
   * @return game the game
   * @throws GameNotFoundException      if the Game could not be found
   * @throws InvalidGameActionException the action is not valid in the current game state
   */
  public Game setGameRoundProperties(Long gameId, Byte facet) {
    LOGGER.info("Setting props of game: %d, %s".formatted(gameId, facet));

    Game game = getRunningGame(gameId);
    GameControlManager manager = getGameControlManager(gameId);
    GameRound gameRound = manager.getCurrentRound();
    manager.setRoundProperties(facet);
    new Timer().schedule(new StopRoundTask(manager, gameRound), gameRound.getTime().getMillis());

    sendGameUpdate(game);
    return game;
  }

  /**
   * Stop the timer for the current gameRound
   *
   * @param gameId id of the desired game
   * @return game the game
   * @throws GameNotFoundException      if the Game could not be found
   * @throws InvalidGameActionException the action is not valid in the current game state
   */
  public Game setRoundEnd(Long gameId) {
    LOGGER.info("stopping timer for game: %d".formatted(gameId));

    GameControlManager manager = getGameControlManager(gameId);
    Game game = manager.getGame();
    manager.setRoundEnd();

    sendGameUpdate(game);
    return game;
  }

  /**
   * set the result of the current round for the game with the given gameId
   *
   * @param setGameResultRequest the request containing result, and gameId
   * @return game the game
   * @throws GameNotFoundException      if the Game could not be found
   * @throws InvalidGameActionException the action is not valid in the current game state
   */
  public Game setGameRoundResult(SetGameResultRequest setGameResultRequest) {
    Long gameId = setGameResultRequest.getGameId();
    Result result = setGameResultRequest.getResult();
    LOGGER.info("Setting result of game: %d, %s".formatted(gameId, result));

    GameControlManager manager = getGameControlManager(gameId);
    Game game = manager.getGame();
    manager.setRoundResult(result);

    if (game.getWinner().isPresent()) {
      gameFinished(game);
    }

    sendGameUpdate(game);
    return game;
  }

  /**
   * start a new round for the game with the given id
   *
   * @param gameId id of the desired game
   * @return game the game
   * @throws GameNotFoundException      if the Game could not be found
   * @throws InvalidGameActionException the action is not valid in the current game state
   */
  public Game startNewGameRound(Long gameId) {
    LOGGER.info("starting new round for game: %d".formatted(gameId));

    GameControlManager manager = getGameControlManager(gameId);
    Game game = manager.getGame();
    manager.startNewRound();

    sendGameUpdate(game);
    return game;
  }

  private void gameFinished(Game game) {
    Long gameId = game.getGameId();
    LOGGER.info("GameFinished (id: %d)".formatted(gameId));
    try {
      gameRoomService.deleteGameRoom(gameId);
    } catch (GameRoomNotFoundException e) {
      LOGGER.error("GameFinished could not cleanup GameRoom with id: %d".formatted(gameId));
    }

    gameRepository.save(game);
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 1000)
  private void cleanUp() {
    LOGGER.debug("running cleanup");
    List<Long> toBeCleanedUpGameIds = gameControlManagerMap.entrySet().stream().filter(gm -> {
      LocalDateTime lastStart = gm.getValue().getLastRoundStart();
      return lastStart != null && ChronoUnit.HOURS.between(lastStart, LocalDateTime.now()) >= 1;
    }).map(Entry::getKey).toList();

    if (toBeCleanedUpGameIds.size() > 1) {
      LOGGER.info("cleaning up games: %s".formatted(toBeCleanedUpGameIds));
      toBeCleanedUpGameIds.forEach(gameControlManagerMap::remove);
    }
  }

  private void sendGameUpdate(final Game game) {
    simpMessagingTemplate.convertAndSend(GameConstants.WS_GAMES_PREFIX.formatted(game.getGameId()),
        GameConstants.GAME_UPDATE_MSG);
  }

  private void sendGameAbort(final Long gameId) {
    simpMessagingTemplate.convertAndSend(GameConstants.WS_GAMES_PREFIX.formatted(gameId),
        GameConstants.GAME_ABORT_MSG);
  }

  /**
   * EventListener listening of TimeFlipFacetChangeEvents. Upon receiving an event make actions
   * based on the current GameState
   *
   * @param e the captured event
   */
  @EventListener
  public void handleTimeFlipFacetChange(TimeFlipFacetChangeEvent e) {
    Optional<GameControlManager> optManager = gameControlManagerMap.values().stream()
        .filter(gm -> !GameState.FINISHED.equals(gm.getGameState()))
        .filter(gm -> Objects.equals(gm.getTimeFlip(), e.getTimeFlip())).findFirst();
    LOGGER.info("handleTimeFlipFacetChange: TimeFlip %s, facet %s, game %s"
        .formatted(e.getTimeFlip().getTimeFlipId(), e.getFacet(),
            optManager.map(GameControlManager::getGame).map(Game::getGameId).map(Objects::toString)
                .orElse("none")));

    if (optManager.isEmpty()) {
      return;
    }

    GameControlManager manager = optManager.get();
    Long gameId = manager.getGame().getGameId();
    GameState gameState = manager.getGameState();
    switch (gameState) {
      case EXPECTING_PROPS -> setGameRoundProperties(gameId, e.getFacet());
      case EXPECTING_ROUND_END -> setRoundEnd(gameId);
      default -> LOGGER
          .debug("TimeFlipFacetChangeEvent ignored: %s, gameState: %s"
              .formatted(e.getTimeFlip(), gameState));
    }
  }

  /**
   * Aborts all games that are associated with the given TimeFlip. Joined players will be informed.
   *
   * @param timeFlip the TimeFlip whose associated games should be aborted
   */
  public void abortGamesAssociatedWithTimeFlip(TimeFlip timeFlip) {
    final List<Entry<Long, GameControlManager>> gamesToAbort = gameControlManagerMap.entrySet()
        .stream().filter(e -> e.getValue().getTimeFlip().equals(timeFlip)).toList();

    gamesToAbort.stream().map(Entry::getKey).forEach(this::abortGame);
  }

  /**
   * Aborts all games that are associated with the given User. Joined players will be informed.
   *
   * @param user the User whose associated games should be aborted
   */
  public void abortGamesAssociatedWithUser(User user) {
    final List<Entry<Long, GameControlManager>> gamesToAbort = gameControlManagerMap.entrySet()
        .stream().filter(e -> e.getValue().getGame().getTeams().stream().map(
            Team::getPlayers).flatMap(Collection::stream).toList().contains(user)).toList();

    gamesToAbort.stream().map(Entry::getKey).forEach(this::abortGame);
  }

  /**
   * aborts the game with the given gameId
   *
   * @param gameId id of the game to be aborted
   */
  public void abortGame(final Long gameId) {
    if (!gameControlManagerMap.containsKey(gameId) || GameState.FINISHED
        .equals(gameControlManagerMap.get(gameId).getGameState())) {
      return;
    }
    sendGameAbort(gameId);
    gameControlManagerMap.remove(gameId);
    gameRoomService.deleteGameRoom(gameId);
  }

  private class StopRoundTask extends TimerTask {

    private final GameControlManager gameControlManager;
    private final GameRound gameRound;

    public StopRoundTask(GameControlManager gameControlManager, GameRound gameRound) {
      this.gameControlManager = gameControlManager;
      this.gameRound = gameRound;
    }

    @Override
    public void run() {
      boolean didStopRound = gameControlManager.stopRound(gameRound);

      if (didStopRound) {
        LOGGER.info("%s Stopped gameRound for game with id %d".formatted(new Date(),
            gameControlManager.getGame().getGameId()));
        sendGameUpdate(gameControlManager.getGame());
      }
    }
  }

}
