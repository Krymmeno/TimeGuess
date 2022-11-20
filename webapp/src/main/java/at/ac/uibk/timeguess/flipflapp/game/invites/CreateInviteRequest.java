package at.ac.uibk.timeguess.flipflapp.game.invites;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@SuppressFBWarnings(value = "EQ_UNUSUAL")
public record CreateInviteRequest(@NotNull Long gameRoomId, @NotEmpty List<Long> userIdList) {

  public Long getGameRoomId() {
    return gameRoomId;
  }

  public List<Long> getUserIdList() {
    return userIdList;
  }
}
