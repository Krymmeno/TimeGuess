package at.ac.uibk.timeguess.flipflapp.game.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAMEMANAGER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAME_ROOM;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.team.Color;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipNotFoundException;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipRepository;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicNotFoundException;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@Transactional
public class GameRoomServiceTest {

  private GameRoom testGameRoom;

  private TimeFlip timeFlip;

  @Autowired
  private GameRoomService gameRoomService;

  @Autowired
  private TopicRepository topicRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TimeFlipRepository timeFlipRepository;

  @BeforeEach
  public void setup() {
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    timeFlip = timeFlipRepository.save(new TimeFlip("0C:61:CF:C7:95:90", "t2"));
    testGameRoom = gameRoomService
        .createGameRoom(new CreateGameRoomRequest(topic.getTopicId(), RANDOM_GAME_ROOM.ROOM_NAME,
                RANDOM_GAME_ROOM.MAX_POINTS),
            getUserIdForUsername(RANDOM_GAMEMANAGER.USERNAME));
  }

  @AfterEach
  public void teardown() {
    gameRoomService.getAllGameRooms()
        .stream()
        .map(GameRoom::getGameRoomId)
        .toList().forEach(gameRoomId -> {
      final List<Long> gameRoomUsers = gameRoomService.getGameRoomUsers(gameRoomId);
      gameRoomUsers.forEach(userId -> gameRoomService.removePlayer(gameRoomId, userId));
    });
  }

  @Test
  public void createGameRoom() {
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    final CreateGameRoomRequest createGameRoomRequest
        = new CreateGameRoomRequest(topic.getTopicId(), "room", MaxPoints.TEN);
    final Long userId = getUserIdForUsername(RANDOM_GAMEMANAGER.USERNAME);
    final GameRoom gameRoom = gameRoomService
        .createGameRoom(createGameRoomRequest, userId);
    assertThat(gameRoom).isNotNull();
    assertThat(gameRoom.getTopic().getTopicId()).isEqualTo(createGameRoomRequest.getTopicId());
    assertThat(gameRoom.getName()).isEqualTo(createGameRoomRequest.getRoomName());
    assertThat(gameRoom.getMaxPoints()).isEqualTo(createGameRoomRequest.getMaxPoints());
    assertThat(gameRoom.getGameRoomUsers().stream()
        .map(gameRoomUser -> gameRoomUser.getUser().getUserId())).contains(userId);
    assertThat(gameRoom.getGameHostId()).isEqualTo(userId);
    gameRoomService.removePlayer(gameRoom.getGameRoomId(), gameRoom.getGameHostId());
  }

  @Test
  public void createGameRoomWithNotExistingTopic() {
    assertThat(topicRepository.findById(-1L)).isEmpty();
    final CreateGameRoomRequest createGameRoomRequest
        = new CreateGameRoomRequest(-1L, "room", MaxPoints.TEN);
    assertThatExceptionOfType(TopicNotFoundException.class)
        .isThrownBy(() -> gameRoomService
            .createGameRoom(createGameRoomRequest, getUserIdForUsername(
                RANDOM_GAMEMANAGER.USERNAME)));
  }

  @Test
  public void createGameRoomWithNotExistingUser() {
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    final CreateGameRoomRequest createGameRoomRequest
        = new CreateGameRoomRequest(topic.getTopicId(), "room", MaxPoints.TEN);
    assertThatExceptionOfType(UserNotFoundException.class)
        .isThrownBy(() -> gameRoomService
            .createGameRoom(createGameRoomRequest, -1L));
  }

  @Test
  public void getGameRoom() {
    assertThat(gameRoomService.getAllGameRooms()).isNotEmpty();
    final GameRoom gameRoom = gameRoomService.getGameRoomIfExists(testGameRoom.getGameRoomId());
    assertThat(gameRoom).isNotNull();
    assertThat(gameRoom.getTopic()).isEqualTo(testGameRoom.getTopic());
  }

  @Test
  public void getNotExistingGameRoom() {
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(() -> gameRoomService.getGameRoomIfExists(-1L));
  }

  @Test
  public void getAllGameRooms() {
    assertThat(gameRoomService.getAllGameRooms().size()).isEqualTo(1);
  }

  @Test
  public void setMaxPoints() {
    assertThat(testGameRoom.getMaxPoints()).isNotEqualTo(MaxPoints.SIXTY);
    gameRoomService.setMaxPoints(testGameRoom.getGameRoomId(), MaxPoints.SIXTY);
    assertThat(testGameRoom.getMaxPoints()).isEqualTo(MaxPoints.SIXTY);
  }

  @Test
  public void setMaxPointsForNotExistingGameRoom() {
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(() -> gameRoomService.setMaxPoints(-1L, MaxPoints.TEN));
  }

  @Test
  public void setTopic() {
    final Topic topic = topicRepository.save(new Topic("TOPIC", true));
    topic.setTerms(Collections.emptySet());
    assertThat(topicRepository.findAll()).isNotEmpty();
    assertThat(testGameRoom.getTopic().getTopicId()).isNotEqualTo(topic.getTopicId());
    gameRoomService.setTopic(testGameRoom.getGameRoomId(), topic.getTopicId());
    assertThat(testGameRoom.getTopic().getTopicId()).isEqualTo(topic.getTopicId());
  }

  @Test
  public void setTopicForNotExistingGameRoom() {
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(() -> gameRoomService.setMaxPoints(-1L, MaxPoints.TEN));
  }

  @Test
  public void setNotExistingTopic() {
    assertThatExceptionOfType(TopicNotFoundException.class)
        .isThrownBy(() -> gameRoomService.setTopic(testGameRoom.getGameRoomId(), -1L));
  }

  @Test
  public void removePlayer() {
    gameRoomService.removePlayer(testGameRoom.getGameRoomId(), testGameRoom.getGameHostId());
    assertThat(gameRoomService.getAllGameRooms()).doesNotContain(testGameRoom);
  }

  @Test
  public void removePlayerForNotExistingGameRoom() {
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(() -> gameRoomService
            .removePlayer(-1L, getUserIdForUsername(RANDOM_GAMEMANAGER.USERNAME)));
  }

  @Test
  public void removeNotExistingPlayer() {
    assertThatExceptionOfType(UserNotFoundException.class)
        .isThrownBy(() -> gameRoomService
            .removePlayer(testGameRoom.getGameRoomId(), -1L));
  }

  @Test
  public void setReady() {
    final GameRoomUser gameHost = testGameRoom.getGameRoomUsers().stream()
        .filter(
            gameRoomUser -> gameRoomUser.getUser().getUserId().equals(testGameRoom.getGameHostId()))
        .findFirst().orElseThrow();
    assertThat(gameHost.isReady()).isFalse();
    gameHost.setTeamColor(Color.BLUE);
    gameRoomService.setReady(testGameRoom.getGameRoomId(), testGameRoom.getGameHostId(), true);
    assertThat(gameHost.isReady()).isTrue();
  }

  @Test
  public void setReadyForNotExistingGameRoom() {
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(() -> gameRoomService
            .setReady(-1L, getUserIdForUsername(RANDOM_GAMEMANAGER.USERNAME), false));
  }

  @Test
  public void setReadyForNotExistingPlayer() {
    assertThatExceptionOfType(UserNotFoundException.class)
        .isThrownBy(() -> gameRoomService
            .setReady(testGameRoom.getGameRoomId(), -1L, false));
  }

  @Test
  public void setReadyToTrueWithoutTeam() {
    final GameRoomUser gameHost = testGameRoom.getGameRoomUsers().stream()
        .filter(
            gameRoomUser -> gameRoomUser.getUser().getUserId().equals(testGameRoom.getGameHostId()))
        .findFirst().orElseThrow();
    assertThat(gameHost.isReady()).isFalse();
    assertThat(gameHost.getTeamColor()).isNull();
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> gameRoomService
            .setReady(testGameRoom.getGameRoomId(), testGameRoom.getGameHostId(), true));
  }

  @Test
  public void getGameRoomUsers() {
    final List<Long> gameRoomUserIds = gameRoomService
        .getGameRoomUsers(
            gameRoomService.getGameRoomIfExists(testGameRoom.getGameRoomId()).getGameRoomId());
    assertThat(gameRoomUserIds).containsExactly(getUserIdForUsername(RANDOM_GAMEMANAGER.USERNAME));
  }

  @Test
  public void setTimeFlipWithUnknownFlip() {
    assertThat(gameRoomService.getAllGameRooms()).isNotEmpty();
    Assertions.assertThrows(TimeFlipNotFoundException.class,
        () -> gameRoomService.setTimeFlip(testGameRoom.getGameRoomId(), -1L));
  }

  @Test
  public void setTimeFlipWithUnknownRoom() {
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(() -> gameRoomService.setTimeFlip(-1L, timeFlip.getTimeFlipId()));
  }

  @Test
  public void setTimeFlip() {
    assertThat(testGameRoom.getTimeFlip()).isNull();
    assertThat(gameRoomService.getAllGameRooms()).isNotEmpty();
    gameRoomService.setTimeFlip(testGameRoom.getGameRoomId(), timeFlip.getTimeFlipId());
    assertThat(testGameRoom.getTimeFlip()).isNotNull();
  }

  private Long getUserIdForUsername(final String username) {
    return userRepository.findByUsername(username).orElseThrow().getUserId();
  }
}
