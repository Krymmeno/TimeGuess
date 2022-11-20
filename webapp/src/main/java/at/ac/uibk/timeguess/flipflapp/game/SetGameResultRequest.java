package at.ac.uibk.timeguess.flipflapp.game;

import at.ac.uibk.timeguess.flipflapp.game.round.Result;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.validation.constraints.NotNull;

@SuppressFBWarnings(value = "EQ_UNUSUAL")
public record SetGameResultRequest(@NotNull Long gameId, @NotNull Result result) {

  public Result getResult() {
    return result;
  }

  public Long getGameId() {
    return gameId;
  }
}
