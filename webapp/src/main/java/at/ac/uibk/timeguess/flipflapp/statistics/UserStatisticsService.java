package at.ac.uibk.timeguess.flipflapp.statistics;

import at.ac.uibk.timeguess.flipflapp.game.Game;
import at.ac.uibk.timeguess.flipflapp.game.GameRepository;
import at.ac.uibk.timeguess.flipflapp.game.round.GameRound;
import at.ac.uibk.timeguess.flipflapp.game.round.GameRoundRepository;
import at.ac.uibk.timeguess.flipflapp.game.round.Result;
import at.ac.uibk.timeguess.flipflapp.team.Team;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import java.util.Collection;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class UserStatisticsService {

  private final UserRepository userRepository;

  private final GameRoundRepository gameRoundRepository;

  private final GameRepository gameRepository;

  public UserStatisticsService(final UserRepository userRepository,
      final GameRoundRepository gameRoundRepository,
      final GameRepository gameRepository) {
    this.userRepository = userRepository;
    this.gameRoundRepository = gameRoundRepository;
    this.gameRepository = gameRepository;
  }

  /**
   * @param userId id needed for finding the user
   * @return statistics about a specific user
   * @throws UserNotFoundException if the user was not found
   */
  public UserStatistics getUserStatistics(final Long userId) {
    userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    return new UserStatistics(getWonGames(userId), getLostGames(userId), getRoundsWon(userId),
        getRoundsLost(userId), getFastestExplanationTime(userId),
        getAverageExplanationTime(userId), getTeammates(userId));
  }

  private Integer getAllGames(final Long userId) {
    return gameRepository.findAll().stream()
        .map(Game::getTeams).flatMap(Collection::stream)
        .map(Team::getPlayers).flatMap(Collection::stream)
        .map(User::getUserId).filter(id -> id.equals(userId))
        .toList().size();
  }

  private Integer getWonGames(final Long userId) {
    return gameRepository.findAll()
        .stream()
        .map(game -> game.getWinner().orElse(null)).filter(Objects::nonNull)
        .map(Team::getPlayers).flatMap(Collection::stream)
        .map(User::getUserId).filter(id -> id.equals(userId))
        .toList().size();
  }

  private Integer getLostGames(final Long userId) {
    return getAllGames(userId) - getWonGames(userId);
  }

  private Integer getRoundsWon(final Long userId) {
    return getRoundsForUserWithState(userId, Result.WIN);
  }

  private Integer getRoundsLost(final Long userId) {
    return getRoundsForUserWithState(userId, Result.TIMEOUT) + getRoundsForUserWithState(userId,
        Result.RULE_VIOLATION);
  }

  private Long getFastestExplanationTime(final Long userId) {
    final OptionalLong min = gameRoundRepository.findAll().stream()
        .filter(gameRound -> gameRound.getUser().getUserId().equals(userId))
        .mapToLong(GameRound::getGuessTimeMillis).min();
    if (min.isPresent()) {
      return min.getAsLong();
    }
    return null;
  }

  private Long getAverageExplanationTime(final Long userId) {
    final OptionalDouble average = gameRoundRepository.findAll().stream()
        .filter(gameRound -> gameRound.getUser().getUserId().equals(userId))
        .mapToLong(GameRound::getGuessTimeMillis).average();
    if (average.isPresent()) {
      return Math.round(average.getAsDouble());
    }
    return null;
  }

  private Set<User> getTeammates(final Long userId) {
    final User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    return gameRepository.findAll().stream()
        .map(Game::getTeams).flatMap(Collection::stream)
        .map(Team::getPlayers).filter(users -> users.contains(user))
        .flatMap(Collection::stream).filter(teammate -> !teammate.equals(user))
        .collect(Collectors.toUnmodifiableSet());
  }

  private Integer getRoundsForUserWithState(final Long userId, final Result timeout) {
    final User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    return gameRoundRepository.findAll().stream()
        .filter(gameRound -> gameRound.getUser().equals(user)
            && gameRound.getResult().equals(timeout))
        .toList().size();
  }
}
