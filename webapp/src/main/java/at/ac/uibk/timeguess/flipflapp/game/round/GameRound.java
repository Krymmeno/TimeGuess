package at.ac.uibk.timeguess.flipflapp.game.round;

import at.ac.uibk.timeguess.flipflapp.game.Game;
import at.ac.uibk.timeguess.flipflapp.term.Term;
import at.ac.uibk.timeguess.flipflapp.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * entity representing one round of a game with it`s result.
 */
@Entity
public class GameRound implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  private String gameRoundId;

  @NotNull(message = "GameRound result not set")
  @Enumerated(EnumType.STRING)
  private Result result;

  @NotNull(message = "GameRound current player not specified")
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "userId")
  private User user;

  @NotNull(message = "GameRound term not specified")
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "termId")
  private Term term;

  @NotNull(message = "GameRound activity not specified")
  @Enumerated(EnumType.STRING)
  private Activity activity;

  @NotNull(message = "GameRound Time not set")
  @Enumerated(EnumType.STRING)
  private Time time;

  @NotNull(message = "GameRound amount of points not set")
  @Enumerated(EnumType.STRING)
  private RoundPoints roundPoints;

  @NotNull(message = "GameRound guessStart cannot be null")
  @Column(columnDefinition = "TIMESTAMP")
  private LocalDateTime guessStart;

  @NotNull(message = "GameRound guessEnd cannot be null")
  @Column(columnDefinition = "TIMESTAMP")
  private LocalDateTime guessEnd;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "gameId")
  @JsonIgnore
  public Game game;

  public GameRound() {
  }

  private GameRound(Game game, Long roundNumber, User user, Term term) {
    this.game = game;
    this.user = user;
    this.term = term;
    this.gameRoundId = "%s_%s".formatted(game.getGameId(), String.valueOf(roundNumber));
  }

  public static GameRound of(Game game, Long roundNumber, User user, Term term) {
    return new GameRound(game, roundNumber, user, term);
  }

  public void setProperties(Activity activity, Time time, RoundPoints roundPoints) {
    this.activity = activity;
    this.time = time;
    this.roundPoints = roundPoints;
    guessStart = LocalDateTime.now();
  }

  public void stopTimer() {
    if (guessStart != null) {
      guessEnd = LocalDateTime.now();
    }
  }

  public void setResult(Result result) {
    this.result = result;
  }

  public User getUser() {
    return this.user;
  }

  public Term getTerm() {
    return this.term;
  }

  public Activity getActivity() {
    return this.activity;
  }

  public Time getTime() {
    return this.time;
  }

  public RoundPoints getRoundPoints() {
    return this.roundPoints;
  }

  public Result getResult() {
    return this.result;
  }

  public Long getGuessTimeMillis() {
    if (guessStart == null) {
      return null;
    }

    Long maxMillis = time.getMillis();
    LocalDateTime guessEndPoint = (guessEnd == null) ? LocalDateTime.now() : guessEnd;
    long guessStartEndDiffMillis = ChronoUnit.MILLIS.between(guessStart, guessEndPoint);

    return (guessStartEndDiffMillis > maxMillis) ? maxMillis : guessStartEndDiffMillis;
  }

  public LocalDateTime getGuessStart() {
    return this.guessStart;
  }

  public LocalDateTime getGuessEnd() {
    return this.guessEnd;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GameRound)) {
      return false;
    }
    final GameRound other = (GameRound) o;
    return Objects.equals(gameRoundId, other.gameRoundId) && Objects.equals(user, other.user)
        && Objects.equals(term, other.term) && Objects.equals(activity, other.activity)
        && Objects.equals(time, other.time) && Objects.equals(result, other.result)
        && Objects.equals(roundPoints, other.roundPoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameRoundId, user, term, activity, time, roundPoints);
  }

  @Override
  public String toString() {
    return "GameRound{" + "gameRoundId=" + gameRoundId + ", result=" + result + ", user=" + user
        + ", term=" + term
        + ", activity=" + activity + ", time=" + time + ", roundPoints=" + roundPoints + '}';
  }
}
