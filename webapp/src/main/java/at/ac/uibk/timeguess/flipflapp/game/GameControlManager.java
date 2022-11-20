package at.ac.uibk.timeguess.flipflapp.game;

import at.ac.uibk.timeguess.flipflapp.game.round.Activity;
import at.ac.uibk.timeguess.flipflapp.game.round.GameRound;
import at.ac.uibk.timeguess.flipflapp.game.round.Result;
import at.ac.uibk.timeguess.flipflapp.game.round.RoundPoints;
import at.ac.uibk.timeguess.flipflapp.game.round.Time;
import at.ac.uibk.timeguess.flipflapp.team.Team;
import at.ac.uibk.timeguess.flipflapp.term.Term;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipFacet;
import at.ac.uibk.timeguess.flipflapp.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.lang3.tuple.Triple;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameControlManager {
  private final Game game;
  private final TimeFlip timeFlip;
  private final Map<Byte, TimeFlipFacet> timeFlipFacetMap;
  private final Queue<Term> terms = new LinkedList<>();
  private final Queue<Pair<Team, Queue<User>>> currentTeamPlayers = new ConcurrentLinkedQueue<>();
  private Long roundNumber = 0L;
  private LocalDateTime lastRoundStart;

  private GameState gameState = GameState.INITIALIZED;
  private static final Logger LOGGER = LoggerFactory.getLogger(GameControlManager.class);

  private GameControlManager(Game game, TimeFlip timeFlip) {
    this.game = game;
    this.timeFlip = timeFlip;
    this.timeFlipFacetMap = timeFlip.getTimeFlipFacetMap();
    List<Term> gameTerms = new ArrayList<>(game.getTopic().getTerms());
    Collections.shuffle(gameTerms, new Random());
    this.terms.addAll(gameTerms);

    game.getTeams().forEach(
        t -> currentTeamPlayers.add(Pair.with(t, new ConcurrentLinkedQueue<>(t.getPlayers()))));
  }

  public static GameControlManager of(Game game, TimeFlip timeFlip) {
    return new GameControlManager(game, timeFlip);
  }

  public Game getGame() {
    return this.game;
  }

  public TimeFlip getTimeFlip() {
    return this.timeFlip;
  }

  public GameRound getCurrentRound() {
    return game.getCurrentRound();
  }

  public GameState getGameState() {
    return this.gameState;
  }

  public LocalDateTime getLastRoundStart() {
    return this.lastRoundStart;
  }

  private <T> T cycleQueue(Queue<T> queue) {
    T t = queue.poll();
    queue.add(t);
    return queue.peek();
  }

  private Triple<Team, User, Term> getNextRound() {
    cycleQueue(currentTeamPlayers);
    Team nextTeam = currentTeamPlayers.peek().getValue0();
    Queue<User> players = currentTeamPlayers.peek().getValue1();
    User nextPlayer = cycleQueue(players);
    Term nextTerm = cycleQueue(terms);

    return Triple.of(nextTeam, nextPlayer, nextTerm);
  }

  /**
   * @return the team which is currently guessing
   */
  public Team getCurrentTeam() {
    return currentTeamPlayers.peek().getValue0();
  }

  /**
   * start a new round for the game with the given id
   *
   * @throws InvalidGameActionException the action is not valid in the current game state
   */
  public void startNewRound() {
    Long gameId = game.getGameId();

    if (!Objects.equals(gameState, GameState.INITIALIZED)
        && !Objects.equals(gameState, GameState.EXPECTING_ROUNDSTART)) {
      String error = "cannot start round of Game(%d) in state: %s".formatted(gameId, gameState);
      LOGGER.warn(error);
      throw new InvalidGameActionException(error);
    }

    roundNumber++;
    Triple<Team, User, Term> nextTeamUserTerm = getNextRound();
    Team nextTeam = nextTeamUserTerm.getLeft();
    User nextUser = nextTeamUserTerm.getMiddle();
    Term nextTerm = nextTeamUserTerm.getRight();
    GameRound currentRound = GameRound.of(game, roundNumber, nextUser, nextTerm);
    lastRoundStart = LocalDateTime.now();
    game.setCurrentTeam(nextTeam);
    game.setCurrentRound(currentRound);

    gameState = GameState.EXPECTING_PROPS;
  }

  /**
   * set the properties of the current GameRound, for the game with the given id
   *
   * @param facet the facet which can be mapped to activity time and roundPoints
   * @throws InvalidGameActionException the action is not valid in the current game state
   */
  public void setRoundProperties(Byte facet) {
    Long gameId = game.getGameId();
    GameRound currentRound = game.getCurrentRound();

    if (!Objects.equals(gameState, GameState.EXPECTING_PROPS)) {
      String error = "cannot set props of Game(%d) in state: %s".formatted(gameId, gameState);
      LOGGER.warn(error);
      throw new InvalidGameActionException(error);
    }

    TimeFlipFacet timeFlipFacet = timeFlipFacetMap.get(facet);
    Activity activity = timeFlipFacet.getActivity();
    RoundPoints roundPoints = timeFlipFacet.getRoundPoints();
    Time time = timeFlipFacet.getTime();
    currentRound.setProperties(activity, time, roundPoints);

    gameState = GameState.EXPECTING_ROUND_END;
  }

  /**
   * set the end of the Round
   *
   * @throws InvalidGameActionException the action is not valid in the current game state
   */
  public void setRoundEnd() {
    Long gameId = game.getGameId();
    GameRound currentRound = game.getCurrentRound();

    if (!Objects.equals(gameState, GameState.EXPECTING_ROUND_END)) {
      String error = "cannot set round end of Game(%d) in state: %s".formatted(gameId, gameState);
      LOGGER.warn(error);
      throw new InvalidGameActionException(error);
    }

    currentRound.stopTimer();

    gameState = GameState.EXPECTING_RESULT;
  }

  /**
   * check if the given gameRound matches the current gameRound, if it does set the end of the round
   *
   * @param gameRound the gameRound
   * @return true if the round was stopped, else false
   */
  public Boolean stopRound(GameRound gameRound) {
    GameRound currentRound = game.getCurrentRound();
    if (Objects.equals(gameRound, currentRound)
        && Objects.equals(gameState, GameState.EXPECTING_ROUND_END)) {
      return com.spencerwi.either.Result.attempt(() -> {
        setRoundEnd();
        return true;
      }).isOk();
    }

    return false;
  }

  /**
   * set the result of the current round for the game with the given gameId
   *
   * @param result the result of the current gameRound
   * @throws InvalidGameActionException the action is not valid in the current game state
   */
  public void setRoundResult(Result result) {
    Long gameId = game.getGameId();
    GameRound currentRound = game.getCurrentRound();

    if (Objects.equals(gameState, GameState.EXPECTING_ROUND_END)
        && Objects.equals(result, Result.RULE_VIOLATION)) {
      setRoundEnd();
    }

    if (!Objects.equals(gameState, GameState.EXPECTING_RESULT)) {
      String error = "cannot set result of Game(%d) in state: %s".formatted(gameId, gameState);
      LOGGER.warn(error);
      throw new InvalidGameActionException(error);
    }

    currentRound.setResult(result);
    game.addCurrentRound();

    gameState = (game.getWinner().isEmpty()) ? GameState.EXPECTING_ROUNDSTART : GameState.FINISHED;
  }

}
