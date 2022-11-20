package at.ac.uibk.timeguess.flipflapp.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.util.Map;

@SuppressFBWarnings(value = "EQ_UNUSUAL")
public record GameStatistics(@JsonProperty("allGames") Integer allGames,
                             @JsonProperty("allRegisteredUsers") Integer allRegisteredUsers,
                             @JsonProperty("allActiveUsers") Integer allActiveUsers,
                             @JsonProperty("gamesPerTopic") Map<String, Integer> gamesPerTopic,
                             @JsonProperty("allTermsGuessed") long allTermsGuessed,
                             @JsonProperty("gameRoundsPerDay") Map<LocalDate, Long> gameRoundsPerDay,
                             @JsonProperty("termsGuessedCorrectlyPerTopic") Map<String, Long> termsGuessedCorrectlyPerTopic,
                             @JsonProperty("termsGuessedWronglyPerTopic") Map<String, Long> termsGuessedWronglyPerTopic,
                             @JsonProperty("gamesWonPerUser") Map<String, Long> gamesWonPerUser) {

  public Integer getAllGames() {
    return allGames;
  }

  public Integer getAllRegisteredUsers() {
    return allRegisteredUsers;
  }

  public Integer getAllActiveUsers() {
    return allActiveUsers;
  }

  public Map<String, Integer> getGamesPerTopic() {
    return gamesPerTopic;
  }

  public long getAllTermsGuessed() {
    return allTermsGuessed;
  }

  public Map<LocalDate, Long> getGameRoundsPerDay() {
    return gameRoundsPerDay;
  }

  public Map<String, Long> getTermsGuessedCorrectlyPerTopic() {
    return termsGuessedCorrectlyPerTopic;
  }

  public Map<String, Long> getTermsGuessedWronglyPerTopic() {
    return termsGuessedWronglyPerTopic;
  }

  public Map<String, Long> getGamesWonPerUser() {
    return gamesWonPerUser;
  }
}
