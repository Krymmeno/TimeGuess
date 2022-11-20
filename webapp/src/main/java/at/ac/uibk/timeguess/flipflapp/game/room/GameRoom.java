package at.ac.uibk.timeguess.flipflapp.game.room;

import at.ac.uibk.timeguess.flipflapp.game.Game;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.team.Color;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.user.User;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class GameRoom {

  private final Set<Color> availableTeamsList = new CopyOnWriteArraySet<>();
  private final List<GameRoomUser> gameRoomUsers = new CopyOnWriteArrayList<>();
  private final Set<User> invitedUsers = new CopyOnWriteArraySet<>();
  private final Long gameRoomId;
  private Long gameHostId;
  private String name;
  private Topic topic;
  private MaxPoints maxPoints;
  private String gameCreationErrors;
  private Boolean allPlayersReady;
  private Boolean gameStarted;
  private TimeFlip timeFlip;
  private final Integer minTeamSize;

  public GameRoom(Long gameRoomId, Long gameHostId, String name, Topic topic, MaxPoints maxPoints,
      Integer minTeamSize) {
    this.gameRoomId = gameRoomId;
    this.gameHostId = gameHostId;
    this.name = name;
    this.topic = topic;
    this.maxPoints = maxPoints;
    this.minTeamSize = minTeamSize;
    this.allPlayersReady = true;
    this.gameStarted = false;
    this.gameCreationErrors = "";
  }

  private void setGameCreationInfo() {
    gameCreationErrors = Game.of(this).fold(l -> l, r -> "");
    allPlayersReady = allPlayersReady();
  }

  public void leaveRoom(User player) {
    final Optional<GameRoomUser> user = gameRoomUsers.stream()
        .filter(gameRoomUser -> gameRoomUser.getUser().equals(player)).findFirst();
    user.ifPresent(gameRoomUsers::remove);
    invitedUsers.remove(player);
    setGameCreationInfo();
  }

  public void joinRoom(User player) {
    invitedUsers.remove(player);
    gameRoomUsers.add(new GameRoomUser(player, null, false));
    setGameCreationInfo();
  }

  private void movePlayer(User player, Color color) {
    final Optional<GameRoomUser> gameRoomUser =
        gameRoomUsers.stream().filter(roomUser -> roomUser.getUser().equals(player)).findFirst();
    if (color == null) {
      gameRoomUser.ifPresent(roomUser -> roomUser.setTeamColor(null));
    }
    if (availableTeamsList.contains(color)) {
      gameRoomUser.ifPresent(roomUser -> roomUser.setTeamColor(color));
    }
  }

  public void createTeam(Color teamColor) {
    availableTeamsList.add(teamColor);
    setGameCreationInfo();
  }

  public void joinTeam(User player, Color color) {
    movePlayer(player, color);
    setMark(player, false);
    setGameCreationInfo();
  }

  public void leaveTeam(User player) {
    movePlayer(player, null);
    setMark(player, false);
    setGameCreationInfo();
  }

  public void deleteTeam(Color teamColor) {
    gameRoomUsers.stream().filter(gameRoomUser -> teamColor.equals(gameRoomUser.getTeamColor()))
        .map(GameRoomUser::getUser).toList().forEach(this::leaveTeam);
    availableTeamsList.remove(teamColor);
    setGameCreationInfo();
  }

  public void setMark(User player, boolean mark) {
    gameRoomUsers.stream().filter(gameRoomUser -> gameRoomUser.getUser().equals(player)).findFirst()
        .ifPresent(gameRoomUser -> gameRoomUser.setReady(mark));
    setGameCreationInfo();
  }

  public void setGameStarted() {
    setGameCreationInfo();
    gameStarted = true;
  }

  public boolean getGameStarted() {
    return gameStarted;
  }

  public Boolean getAllPlayersReady() {
    return this.allPlayersReady;
  }

  public String getGameCreationError() {
    return this.gameCreationErrors;
  }

  public List<GameRoomUser> getGameRoomUsers() {
    return gameRoomUsers;
  }

  public Set<Color> getAvailableTeamsList() {
    return availableTeamsList;
  }

  public Long getGameHostId() {
    return gameHostId;
  }

  public void setGameHostId(Long gameHostId) {
    Optional.ofNullable(gameHostId).ifPresent(g -> this.gameHostId = g);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getGameRoomId() {
    return gameRoomId;
  }

  private Boolean allPlayersReady() {
    return gameRoomUsers.stream().filter(gu -> !gu.getUser().getUserId().equals(gameHostId))
        .allMatch(GameRoomUser::isReady);
  }

  public Topic getTopic() {
    return topic;
  }

  public void setTopic(Topic topic) {
    this.topic = topic;
    setGameCreationInfo();
  }

  public MaxPoints getMaxPoints() {
    return maxPoints;
  }

  public void setMaxPoints(MaxPoints maxPoints) {
    this.maxPoints = maxPoints;
    setGameCreationInfo();
  }

  /**
   * add an user to the list of invited users
   *
   * @param user the user to be invited to the gameRoom
   */
  public void addInvitedUser(User user) {
    if (user != null && !gameStarted
        && gameRoomUsers.stream().noneMatch(gu -> Objects.equals(gu.getUser(), user))) {
      invitedUsers.add(user);
    }
  }

  public boolean removeInvitedUser(User user) {
    return invitedUsers.remove(user);
  }

  public Set<User> getInvitedUsers() {
    return invitedUsers;
  }

  public TimeFlip getTimeFlip() {
    return timeFlip;
  }

  public void setTimeFlip(TimeFlip timeFlip) {
    this.timeFlip = timeFlip;
    setGameCreationInfo();
  }

  public Integer getMinTeamSize() {
    return minTeamSize;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final GameRoom gameRoom = (GameRoom) o;
    return Objects.equals(gameRoomUsers, gameRoom.gameRoomUsers)
        && Objects.equals(gameRoomId, gameRoom.gameRoomId)
        && Objects.equals(gameHostId, gameRoom.gameHostId) && Objects.equals(name, gameRoom.name)
        && Objects.equals(topic, gameRoom.topic) && maxPoints == gameRoom.maxPoints;
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameRoomUsers, gameRoomId, gameHostId, name, topic, maxPoints);
  }

  @Override
  public String toString() {
    return "GameRoom{" +
        "availableTeamsList=" + availableTeamsList +
        ", gameRoomUsers=" + gameRoomUsers +
        ", invitedUsers=" + invitedUsers +
        ", gameRoomId=" + gameRoomId +
        ", gameHostId=" + gameHostId +
        ", name='" + name + '\'' +
        ", topic=" + topic +
        ", maxPoints=" + maxPoints +
        ", gameCreationErrors='" + gameCreationErrors + '\'' +
        ", allPlayersReady=" + allPlayersReady +
        ", gameStarted=" + gameStarted +
        ", timeFlip=" + timeFlip +
        ", minTeamSize=" + minTeamSize +
        '}';
  }
}
