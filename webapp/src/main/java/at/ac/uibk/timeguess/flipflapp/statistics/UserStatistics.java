package at.ac.uibk.timeguess.flipflapp.statistics;

import at.ac.uibk.timeguess.flipflapp.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;

@SuppressFBWarnings(value = "EQ_UNUSUAL")
public record UserStatistics(@JsonProperty("wonGames") Integer wonGames,
                             @JsonProperty("lostGames") Integer lostGames,
                             @JsonProperty("wonRounds") Integer wonRounds,
                             @JsonProperty("lostRounds") Integer lostRounds,
                             @JsonProperty("fastestExplanationTime") Long fastestExplanationTime,
                             @JsonProperty("averageExplanationTime") Long averageExplanationTime,
                             @JsonProperty("teammates") Set<User> teammates) {

  public Integer getWonGames() {
    return wonGames;
  }

  public Integer getLostGames() {
    return lostGames;
  }

  public Integer getWonRounds() {
    return wonRounds;
  }

  public Integer getLostRounds() {
    return lostRounds;
  }

  public Long getFastestExplanationTime() {
    return fastestExplanationTime;
  }

  public Long getAverageExplanationTime() {
    return averageExplanationTime;
  }

  public Set<User> getTeammates() {
    return teammates;
  }
}
