package at.ac.uibk.timeguess.flipflapp.statistics;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import at.ac.uibk.timeguess.flipflapp.game.Game;
import at.ac.uibk.timeguess.flipflapp.game.GameRepository;
import at.ac.uibk.timeguess.flipflapp.game.round.GameRound;
import at.ac.uibk.timeguess.flipflapp.game.round.GameRoundRepository;
import at.ac.uibk.timeguess.flipflapp.game.round.Result;
import at.ac.uibk.timeguess.flipflapp.team.Team;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GameStatisticsService {

  private final GameRepository gameRepository;
  private final UserRepository userRepository;
  private final GameRoundRepository gameRoundRepository;
  private final TopicRepository topicRepository;

  public GameStatisticsService(final GameRepository gameRepository,
      final UserRepository userRepository,
      final GameRoundRepository gameRoundRepository,
      final TopicRepository topicRepository) {
    this.gameRepository = gameRepository;
    this.userRepository = userRepository;
    this.gameRoundRepository = gameRoundRepository;
    this.topicRepository = topicRepository;
  }

  /**
   * Returns statistics about the game
   *
   * @return statistics with information regarding the overall game
   */
  public GameStatistics getGameStatistics() {
    return new GameStatistics(getAllGamesPlayed(), getAllRegisteredUsers(), getAllActiveUsers(),
        getGamesPerTopic(), getAllTermsGuessed(), getGameRoundsPerDay(),
        getTermsGuessedCorrectlyPerTopic(), getTermsGuessedWronglyPerTopic(), getGamesWonPerUser());
  }

  private Integer getAllGamesPlayed() {
    return gameRepository.findAll().size();
  }

  private Integer getAllRegisteredUsers() {
    return userRepository.findAll().size();
  }

  private Integer getAllActiveUsers() {
    return userRepository.findAll().stream().filter(User::getActive).toList().size();
  }

  private Map<String, Integer> getGamesPerTopic() {
    return topicRepository.findAll().stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Topic::getName, topic -> topic.getGames().size()));
  }

  private Map<String, Long> getTermsGuessedWronglyPerTopic() {
    return topicRepository.findAll().stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Topic::getName,
            topic -> getTermsPerTopic(topic, Result.TIMEOUT) + getTermsPerTopic(topic,
                Result.RULE_VIOLATION)));
  }

  private Map<String, Long> getTermsGuessedCorrectlyPerTopic() {
    return topicRepository.findAll().stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Topic::getName, topic -> getTermsPerTopic(topic, Result.WIN)));
  }

  private long getTermsPerTopic(final Topic topic, final Result result) {
    return topic.getGames().stream()
        .map(Game::getGameRounds)
        .flatMap(Collection::stream)
        .filter(gameRound -> gameRound.getResult().equals(result))
        .count();
  }

  private long getAllTermsGuessed() {
    return gameRoundRepository.findAll().stream()
        .filter(gameRound -> gameRound.getResult().equals(Result.WIN))
        .count();
  }

  private Map<LocalDate, Long> getGameRoundsPerDay() {
    return gameRoundRepository.findAll().stream().map(GameRound::getGuessStart)
        .filter(Objects::nonNull)
        .collect(groupingBy(LocalDateTime::toLocalDate, counting()))
        .entrySet()
        .stream()
        .sorted(Entry.comparingByKey())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
  }

  private Map<String, Long> getGamesWonPerUser() {
    return userRepository.findAll().stream()
        .collect(Collectors.toMap(User::getUsername, user -> getWonGames(user.getUserId())))
        .entrySet()
        .stream()
        .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
  }

  private long getWonGames(final Long userId) {
    return gameRepository.findAll()
        .stream()
        .map(game -> game.getWinner().orElse(null)).filter(Objects::nonNull)
        .map(Team::getPlayers).flatMap(Collection::stream)
        .map(User::getUserId).filter(id -> id.equals(userId))
        .count();
  }
}
