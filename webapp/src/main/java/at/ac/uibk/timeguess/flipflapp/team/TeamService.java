package at.ac.uibk.timeguess.flipflapp.team;

import static at.ac.uibk.timeguess.flipflapp.game.GameConstants.GAME_ROOM_UPDATE_MSG;

import at.ac.uibk.timeguess.flipflapp.game.GameConstants;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomNotFoundException;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserService;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

  private final GameRoomService gameRoomService;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final UserService userService;
  private final Set<Color> teamColors = Set.of(Color.values());

  public TeamService(final GameRoomService gameRoomService,
      final SimpMessagingTemplate simpMessagingTemplate,
      final UserService userService) {
    this.gameRoomService = gameRoomService;
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.userService = userService;
  }

  /**
   * Creates a new team for a specific GameRoom
   *
   * @param gameRoomId id needed for finding the GameRoom
   * @throws GameRoomNotFoundException if the GameRoom was not found
   * @throws TooManyTeamsException     if no more teams can be created
   */
  public void createTeam(final Long gameRoomId) {
    final GameRoom gameRoom = gameRoomService.getGameRoomIfExists(gameRoomId);
    gameRoom.createTeam(getTeamColor(gameRoom));
    sendGameRoomUpdate(gameRoom);
  }

  /**
   * Lets a user join a team
   *
   * @param gameRoomId id needed for finding the GameRoom
   * @param userId     id needed for finding the User
   * @param team       the team the User wants to join
   * @throws GameRoomNotFoundException if the GameRoom was not found
   * @throws UserNotFoundException     if the User was not found
   * @throws TeamNotFoundException     if the team was not found for the specified GameRoom
   */
  public void joinTeam(final Long gameRoomId, final Long userId, final Color team) {
    final GameRoom gameRoom = gameRoomService.getGameRoomIfExists(gameRoomId);
    final User user = userService.getUserById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    if (!gameRoom.getAvailableTeamsList().contains(team)) {
      throw new TeamNotFoundException("The team with the color %s was not found.".formatted(team));
    }
    gameRoom.joinTeam(user, team);
    sendGameRoomUpdate(gameRoom);
  }

  /**
   * Lets a User leave a team
   *
   * @param gameRoomId id needed for finding the GameRoom
   * @param userId     id needed for finding the User
   * @throws GameRoomNotFoundException if the GameRoom was not found
   * @throws UserNotFoundException     if the User was not found
   */
  public void leaveTeam(final Long gameRoomId, final Long userId) {
    final GameRoom gameRoom = gameRoomService.getGameRoomIfExists(gameRoomId);
    final User user = userService.getUserById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    gameRoom.leaveTeam(user);
    sendGameRoomUpdate(gameRoom);
  }

  /**
   * Deletes a Team from a GameRoom
   *
   * @param gameRoomId id needed for finding the GameRoom
   * @param team       the team one wants to delete
   * @throws GameRoomNotFoundException if the GameRoom was not found
   * @throws TeamNotFoundException     if the Team was not found
   */
  public void deleteTeam(final Long gameRoomId, final Color team) {
    final GameRoom gameRoom = gameRoomService.getGameRoomIfExists(gameRoomId);
    if (!gameRoom.getAvailableTeamsList().contains(team)) {
      throw new TeamNotFoundException("The team with the color %s was not found.".formatted(team));
    }
    gameRoom.deleteTeam(team);
    sendGameRoomUpdate(gameRoom);
  }

  private void sendGameRoomUpdate(final GameRoom gameRoom) {
    simpMessagingTemplate
        .convertAndSend(GameConstants.WS_GAME_ROOM_PREFIX.formatted(gameRoom.getGameRoomId()),
            GAME_ROOM_UPDATE_MSG);
  }

  private Color getTeamColor(final GameRoom gameRoom) {
    final Set<Color> availableTeamColors = new HashSet<>(teamColors);
    availableTeamColors.removeAll(gameRoom.getAvailableTeamsList());
    if (!availableTeamColors.iterator().hasNext()) {
      throw new TooManyTeamsException(
          "You can't create more than %d teams.".formatted(Color.values().length));
    }
    return availableTeamColors.iterator().next();
  }
}
