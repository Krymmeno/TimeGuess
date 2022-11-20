package at.ac.uibk.timeguess.flipflapp.game.room;

import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@SuppressFBWarnings(value = "EQ_UNUSUAL")
public record CreateGameRoomRequest(@NotNull Long topicId,
                                    @NotBlank String roomName,
                                    @NotNull MaxPoints maxPoints) {

  public Long getTopicId() {
    return topicId;
  }

  public String getRoomName() {
    return roomName;
  }

  public MaxPoints getMaxPoints() {
    return maxPoints;
  }
}
