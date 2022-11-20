package at.ac.uibk.timeguess.flipflapp.game.room;

import at.ac.uibk.timeguess.flipflapp.team.Color;
import at.ac.uibk.timeguess.flipflapp.user.User;
import java.util.Objects;

public class GameRoomUser {

  private User user;

  private Color teamColor;

  private boolean isReady;

  public GameRoomUser(final User user, final Color teamColor, final boolean isReady) {
    this.user = user;
    this.teamColor = teamColor;
    this.isReady = isReady;
  }

  public User getUser() {
    return user;
  }

  public void setUser(final User user) {
    this.user = user;
  }

  public Color getTeamColor() {
    return teamColor;
  }

  public void setTeamColor(final Color teamColor) {
    this.teamColor = teamColor;
  }

  public boolean isReady() {
    return isReady;
  }

  public void setReady(final boolean ready) {
    isReady = ready;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final GameRoomUser that = (GameRoomUser) o;
    return isReady == that.isReady && user.equals(that.user) && teamColor == that.teamColor;
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, teamColor, isReady);
  }

  @Override
  public String toString() {
    return "GameRoomUser{" +
        "user=" + user +
        ", teamColor=" + teamColor +
        ", isReady=" + isReady +
        '}';
  }
}
