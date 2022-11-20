package at.ac.uibk.timeguess.flipflapp.game.room;

import static at.ac.uibk.timeguess.flipflapp.game.GameConstants.GAME_ROOM_UPDATE_MSG;

import at.ac.uibk.timeguess.flipflapp.game.GameConstants;
import at.ac.uibk.timeguess.flipflapp.game.GameRepository;
import at.ac.uibk.timeguess.flipflapp.game.invites.CreateInviteRequest;
import at.ac.uibk.timeguess.flipflapp.game.invites.InviteNotFoundException;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipNotFoundException;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipRepository;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicNotFoundException;
import at.ac.uibk.timeguess.flipflapp.topic.TopicService;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserService;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameRoomService {

  private final AtomicLong maxGameRoomId;
  private final Map<Long, GameRoom> gameRoomMap = new ConcurrentHashMap<>();
  private final UserService userService;
  private final TopicService topicService;
  private final TimeFlipRepository timeFlipRepository;
  private final Integer minTeamSize;
  private SimpMessagingTemplate simpMessagingTemplate;

  public GameRoomService(final UserService userService, final TopicService topicService,
      final GameRepository gameRepository, TimeFlipRepository timeFlipRepository,
      @Value("${flipflapp.game.min-team-size:2}") final Integer minTeamSize) {
    this.userService = userService;
    this.topicService = topicService;
    this.timeFlipRepository = timeFlipRepository;
    this.minTeamSize = minTeamSize;
    this.maxGameRoomId = new AtomicLong(
        gameRepository.findAll().stream().mapToLong(g -> g.getGameId() + 1).max().orElse(0));
  }

  @Autowired
  public void setSimpMessagingTemplate(final SimpMessagingTemplate simpMessagingTemplate) {
    this.simpMessagingTemplate = simpMessagingTemplate;
  }


  /**
   * creates a new GameRoom
   *
   * @param createGameRoomRequest holds data needed for creating a new GameRoom
   * @param gameHostId            id of the game host
   * @return the newly created GameRoom
   * @throws UserNotFoundException  if the User was not found
   * @throws TopicNotFoundException if the Topic was not found
   */
  public GameRoom createGameRoom(final CreateGameRoomRequest createGameRoomRequest,
      final Long gameHostId) {
    final User gameHost = userService.getUserIfExists(gameHostId);
    final Topic topic = topicService.getTopicIfExists(createGameRoomRequest.topicId());
    final Long gameRoomId = maxGameRoomId.getAndIncrement();
    final GameRoom gameRoom = new GameRoom(gameRoomId, gameHost.getUserId(),
        createGameRoomRequest.roomName(), topic, createGameRoomRequest.maxPoints(), minTeamSize);

    gameRoom.joinRoom(gameHost);
    gameRoomMap.put(gameRoomId, gameRoom);
    return gameRoom;
  }

  /**
   * delete the given gameRoom
   *
   * @param gameRoomId the id of the GameRoom which should be deleted
   */
  public void deleteGameRoom(Long gameRoomId) {
    Optional.ofNullable(gameRoomMap.remove(gameRoomId)).ifPresent(this::sendGameRoomUpdate);
  }

  /**
   * Gets all GameRoom's
   *
   * @return list of all GameRoom's
   */
  public List<GameRoom> getAllGameRooms() {
    return Collections.unmodifiableList(gameRoomMap.values().stream().toList());
  }

  /**
   * Updates the maximum points for a specific GameRoom and sends an update to the specified
   * GameRoom
   *
   * @param gameRoomId id needed for finding the GameRoom
   * @param maxPoints  used to update the maxPoints of the according GameRoom
   * @throws GameRoomNotFoundException if the GameRoom was not found
   */
  public void setMaxPoints(final Long gameRoomId, final MaxPoints maxPoints) {
    final GameRoom gameRoom = getGameRoomIfExists(gameRoomId);

    gameRoom.setMaxPoints(maxPoints);
    sendGameRoomUpdate(gameRoom);
  }

  /**
   * Updates the topic for a specific GameRoom and sends an update to the specified GameRoom
   *
   * @param gameRoomId id needed for finding the GameRoom
   * @param topicId    id needed for finding the Topic
   * @throws GameRoomNotFoundException if the GameRoom was not found
   * @throws TopicNotFoundException    if the Topic was not found
   */
  public void setTopic(final Long gameRoomId, final Long topicId) {
    final GameRoom gameRoom = getGameRoomIfExists(gameRoomId);
    final Topic topic = topicService.getTopicIfExists(topicId);

    gameRoom.setTopic(topic);
    sendGameRoomUpdate(gameRoom);
  }

  /**
   * Removes a player from the specified GameRoom, if necessary elects a new game host, and sends an
   * update to the specified GameRoom
   *
   * @param gameRoomId id needed for finding the GameRoom
   * @param userId     id needed for finding the User
   * @throws GameRoomNotFoundException if the GameRoom was not found
   * @throws UserNotFoundException     if the User was not found
   */
  public void removePlayer(final Long gameRoomId, final Long userId) {
    final GameRoom gameRoom = getGameRoomIfExists(gameRoomId);
    final User user = userService.getUserIfExists(userId);

    gameRoom.leaveRoom(user);
    if (gameRoom.getGameRoomUsers().isEmpty()) {
      deleteGameRoom(gameRoomId);
    } else if (gameRoom.getGameHostId().equals(userId)) {
      final User newGameHost = chooseNewGameHost(gameRoom);
      gameRoom.setGameHostId(newGameHost.getUserId());
    }

    sendGameRoomUpdate(gameRoom);
  }

  /**
   * Updates the ready status of a User and sends an update to the specified GameRoom
   *
   * @param gameRoomId id needed for finding the GameRoom
   * @param userId     id needed for finding the User
   * @param isReady    holds the new status of the User
   * @throws GameRoomNotFoundException if the GameRoom was not found
   * @throws UserNotFoundException     if the User was not found
   * @throws IllegalStateException     if the User tried to set ready without having a Team
   */
  public void setReady(final Long gameRoomId, final Long userId, final Boolean isReady) {
    final GameRoom gameRoom = getGameRoomIfExists(gameRoomId);
    final User user = userService.getUserIfExists(userId);
    final Optional<GameRoomUser> firstUser = gameRoom.getGameRoomUsers().stream()
        .filter(gameRoomUser -> gameRoomUser.getUser().equals(user)).findFirst();

    if (firstUser.isPresent() && firstUser.get().getTeamColor() == null && isReady) {
      throw new IllegalStateException(
          "The user with the id %d wants the set ready without having a team"
              .formatted(user.getUserId()));
    }

    gameRoom.setMark(user, isReady);
    sendGameRoomUpdate(gameRoom);
  }

  /**
   * returns a list of all gameRoomUsers from the gameRoom with the given id
   *
   * @param gameRoomId id of the gameRoom
   * @return list of gameRoomUser
   * @throws GameRoomNotFoundException if the GameRoom was not found
   */
  public List<Long> getGameRoomUsers(final Long gameRoomId) {
    return getGameRoomIfExists(gameRoomId).getGameRoomUsers().stream()
        .map(GameRoomUser::getUser).map(User::getUserId).toList();
  }

  /**
   * set the game started needed to validate permissions of later actions
   *
   * @param gameRoom the GameRoom which should be set to started
   */
  public void setGameStarted(GameRoom gameRoom) {
    gameRoom.setGameStarted();
    gameRoom.getInvitedUsers().forEach(u -> {
      if (gameRoom.removeInvitedUser(u)) {
        sendInviteUpdate(u);
      }
    });

    sendGameRoomUpdate(gameRoom);
  }

  /**
   * Returns the gameRoom associated with the given gameRoomId
   *
   * @param gameRoomId the id of the gameRoom
   * @return the GameRoom
   * @throws GameRoomNotFoundException if the gameRoom was not found
   */
  public GameRoom getGameRoomIfExists(final Long gameRoomId) {
    return Optional.ofNullable(gameRoomMap.get(gameRoomId))
        .orElseThrow(() -> new GameRoomNotFoundException(gameRoomId));
  }

  /**
   * notify all members of the given gameRoom, and invited users about changes in the gameRoom
   *
   * @param gameRoom the gameRoom that changed
   */
  public void sendGameRoomUpdate(final GameRoom gameRoom) {
    simpMessagingTemplate.convertAndSend(
        GameConstants.WS_GAME_ROOM_PREFIX.formatted(gameRoom.getGameRoomId()),
        GAME_ROOM_UPDATE_MSG);
    Optional.of(gameRoom).ifPresent(g -> g.getInvitedUsers().forEach(this::sendInviteUpdate));
  }

  private User chooseNewGameHost(final GameRoom gameRoom) {
    return gameRoom.getGameRoomUsers().iterator().next().getUser();
  }

  /**
   * Assign TimeFlip to GameRoom
   *
   * @param gameRoomId id needed for finding the GameRoom
   * @param timeFlipId id needed for finding the TimeFlip
   * @throws GameRoomNotFoundException if gameRoom is not found in Database
   * @throws TimeFlipNotFoundException if TimeFlip is not found in Database
   */
  public void setTimeFlip(Long gameRoomId, Long timeFlipId) {
    final GameRoom gameRoom = getGameRoomIfExists(gameRoomId);
    final TimeFlip timeFlip = timeFlipRepository.findById(timeFlipId)
        .orElseThrow(() -> new TimeFlipNotFoundException(timeFlipId));

    gameRoom.setTimeFlip(timeFlip);
    sendGameRoomUpdate(gameRoom);
  }

  /**
   * @param timeFlip the TimeFlip to filter by
   * @return the list of game rooms where the specified TimeFlip was selected
   */
  public List<GameRoom> getGameRoomsForTimeFlip(TimeFlip timeFlip) {
    return getAllGameRooms().stream()
        .filter(gr -> gr.getTimeFlip() != null)
        .filter(gr -> gr.getTimeFlip().getDeviceAddress().equals(timeFlip.getDeviceAddress()))
        .toList();
  }

  /**
   * invite an user to a gameRoom
   *
   * @param inviteRequest the request holding the gameRoom and userId
   * @param createUserId  the the id of the user which created the request
   * @throws IllegalArgumentException  if the gameRoom already started a game
   * @throws UserNotFoundException     if the to be invited user does not exist
   * @throws GameRoomNotFoundException if gameRoom to which the user is invited does not exist
   */
  public GameRoom addInvite(CreateInviteRequest inviteRequest, Long createUserId) {
    final Long gameRoomId = inviteRequest.getGameRoomId();
    final GameRoom gameRoom = getGameRoomIfExists(gameRoomId);
    final Long gameHostId = gameRoom.getGameHostId();

    if (!Objects.equals(createUserId, gameHostId)) {
      throw new IllegalArgumentException(
          "user: %d, is not host(%d) of gameRoom: %d, and can not create invites for that Game-Room"
              .formatted(createUserId, gameHostId, gameRoomId));
    }
    if (gameRoom.getGameStarted()) {
      throw new IllegalArgumentException(
          "cannot create invites for Game-Rooms which already started a game");
    }
    inviteRequest.getUserIdList().stream()
        .map(userService::getUserIfExists)
        .forEach(gameRoom::addInvitedUser);
    sendGameRoomUpdate(gameRoom);
    return gameRoom;
  }

  /**
   * accept the invite to the gameRoom for the given userId gameRoomId and the userId together form
   * a unique `inviteId`
   *
   * @param gameRoomId the gameRoomId that will be joined
   * @param userId     the id of the user that joins the gameRoom
   * @throws IllegalArgumentException  if the gameRoom already started a game
   * @throws UserNotFoundException     if the to be invited user does not exist
   * @throws GameRoomNotFoundException if gameRoom to which the user is invited does not exist
   * @throws InviteNotFoundException   if gameRoom to which the user is invited does not exist
   */
  public GameRoom acceptInvite(Long gameRoomId, Long userId) {
    GameRoom gameRoom = getGameRoomIfExists(gameRoomId);
    final User player = userService.getUserIfExists(userId);

    if (gameRoom.getGameStarted()) {
      throw new IllegalArgumentException(
          "cannot accept an invite for Game-Rooms which already started a game");
    }

    if (!gameRoom.getInvitedUsers().contains(player)) {
      throw new InviteNotFoundException(userId, gameRoomId);
    }

    gameRoom.joinRoom(player);
    sendGameRoomUpdate(gameRoom);
    return gameRoom;
  }

  /**
   * Get all Invite's of specific user
   *
   * @param userId the user to retrieve the invites for
   * @return list of all invite's for a specific user
   * @throws UserNotFoundException if the given userId cannot be associated with a user
   */
  public List<GameRoom> getAllInvites(Long userId) {
    User user = userService.getUserIfExists(userId);
    return gameRoomMap.values().stream().filter(g -> g.getInvitedUsers().contains(user)).toList();
  }


  /**
   * notify the user to which the invite belongs about a change
   *
   * @param user the invite of which the associated should be notified
   */
  private void sendInviteUpdate(final User user) {
    simpMessagingTemplate.convertAndSend(
        GameConstants.WS_INVITES_PREFIX.formatted(user.getUserId()),
        GameConstants.INVITES_UPDATE_MSG);
  }
}
