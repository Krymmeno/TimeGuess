package at.ac.uibk.timeguess.flipflapp.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAMEMANAGER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAME_ROOM;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.game.room.CreateGameRoomRequest;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomNotFoundException;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomUser;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import java.util.List;
import java.util.Set;
import javax.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@Transactional
public class TeamServiceTest {

  private GameRoom testGameRoom;

  @Autowired
  private TopicRepository topicRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GameRoomService gameRoomService;

  @Autowired
  private TeamService teamService;


  @BeforeEach
  public void setup() {
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    final User gameManager = userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME)
        .orElseThrow();
    testGameRoom = gameRoomService
        .createGameRoom(new CreateGameRoomRequest(topic.getTopicId(), RANDOM_GAME_ROOM.ROOM_NAME,
            RANDOM_GAME_ROOM.MAX_POINTS), gameManager.getUserId());
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
  public void createTeam() {
    teamService.createTeam(testGameRoom.getGameRoomId());
    assertThat(testGameRoom.getAvailableTeamsList()).containsAnyOf(Color.values());
  }

  @Test
  public void createTeamForNotExistingGameRoom() {
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(() -> teamService.createTeam(-1L));
  }

  @Test
  public void createTeamWithNoAvailableTeams() {
    testGameRoom.getAvailableTeamsList().addAll(Set.of(Color.values()));
    assertThatExceptionOfType(TooManyTeamsException.class)
        .isThrownBy(() -> teamService.createTeam(testGameRoom.getGameRoomId()));
  }

  @Test
  public void joinTeam() {
    testGameRoom.createTeam(Color.BLUE);
    assertThat(testGameRoom.getGameRoomUsers()).isNotEmpty();
    final GameRoomUser gameRoomUser = testGameRoom.getGameRoomUsers().get(0);
    teamService
        .joinTeam(testGameRoom.getGameRoomId(), gameRoomUser.getUser().getUserId(), Color.BLUE);
    assertThat(gameRoomUser.getTeamColor()).isEqualTo(Color.BLUE);
  }

  @Test
  public void joinTeamForNotExistingGameRoom() {
    assertThat(testGameRoom.getGameRoomUsers()).isNotEmpty();
    final GameRoomUser gameRoomUser = testGameRoom.getGameRoomUsers().get(0);
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(
            () -> teamService.joinTeam(-1L, gameRoomUser.getUser().getUserId(), Color.BLUE));
  }

  @Test
  public void joinTeamForNotExistingUser() {
    assertThatExceptionOfType(UserNotFoundException.class)
        .isThrownBy(
            () -> teamService.joinTeam(testGameRoom.getGameRoomId(), -1L, Color.BLUE));
  }

  @Test
  public void joinNotExistingTeam() {
    assertThat(testGameRoom.getGameRoomUsers()).isNotEmpty();
    final GameRoomUser gameRoomUser = testGameRoom.getGameRoomUsers().get(0);
    assertThatExceptionOfType(TeamNotFoundException.class)
        .isThrownBy(() -> teamService
            .joinTeam(testGameRoom.getGameRoomId(), gameRoomUser.getUser().getUserId(),
                Color.BLUE));
  }

  @Test
  public void leaveTeam() {
    testGameRoom.createTeam(Color.BLUE);
    assertThat(testGameRoom.getGameRoomUsers()).isNotEmpty();
    final GameRoomUser gameRoomUser = testGameRoom.getGameRoomUsers().get(0);
    testGameRoom.joinTeam(gameRoomUser.getUser(), Color.BLUE);
    teamService.leaveTeam(testGameRoom.getGameRoomId(), gameRoomUser.getUser().getUserId());
    assertThat(gameRoomUser.getTeamColor()).isEqualTo(null);
  }

  @Test
  public void leaveTeamForNotExistingGameRoom() {
    assertThat(testGameRoom.getGameRoomUsers()).isNotEmpty();
    final GameRoomUser gameRoomUser = testGameRoom.getGameRoomUsers().get(0);
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(() -> teamService.leaveTeam(-1L, gameRoomUser.getUser().getUserId()));
  }

  @Test
  public void leaveTeamForNotExistingUser() {
    assertThatExceptionOfType(UserNotFoundException.class)
        .isThrownBy(() -> teamService.leaveTeam(testGameRoom.getGameRoomId(), -1L));
  }

  @Test
  public void leaveTeamSetsReadyToFalse() {
    testGameRoom.createTeam(Color.BLUE);
    assertThat(testGameRoom.getGameRoomUsers()).isNotEmpty();
    final GameRoomUser gameRoomUser = testGameRoom.getGameRoomUsers().get(0);
    testGameRoom.joinTeam(gameRoomUser.getUser(), Color.BLUE);
    testGameRoom.setMark(gameRoomUser.getUser(), true);
    teamService.leaveTeam(testGameRoom.getGameRoomId(), gameRoomUser.getUser().getUserId());
    assertThat(gameRoomUser.getTeamColor()).isNull();
    assertThat(gameRoomUser.isReady()).isFalse();
  }

  @Test
  public void leaveTeamWithoutHavingATeam() {
    assertThat(testGameRoom.getGameRoomUsers()).isNotEmpty();
    final GameRoomUser gameRoomUser = testGameRoom.getGameRoomUsers().get(0);
    assertThat(gameRoomUser.getTeamColor()).isNull();
    teamService.leaveTeam(testGameRoom.getGameRoomId(), gameRoomUser.getUser().getUserId());
    assertThat(gameRoomUser.getTeamColor()).isNull();
  }

  @Test
  public void deleteTeam() {
    assertThat(testGameRoom.getGameRoomUsers()).isNotEmpty();
    final GameRoomUser gameRoomUser = testGameRoom.getGameRoomUsers().get(0);
    testGameRoom.createTeam(Color.BLUE);
    testGameRoom.joinTeam(gameRoomUser.getUser(), Color.BLUE);
    teamService.deleteTeam(testGameRoom.getGameRoomId(), Color.BLUE);
    assertThat(testGameRoom.getAvailableTeamsList()).doesNotContain(Color.BLUE);
    assertThat(gameRoomUser.getTeamColor()).isNull();
  }

  @Test
  public void deleteTeamForNotExistingGameRoom() {
    testGameRoom.createTeam(Color.BLUE);
    assertThatExceptionOfType(GameRoomNotFoundException.class)
        .isThrownBy(() -> teamService.deleteTeam(-1L, Color.BLUE));
  }

  @Test
  public void deleteNotExistingTeam() {
    assertThatExceptionOfType(TeamNotFoundException.class)
        .isThrownBy(() -> teamService.deleteTeam(testGameRoom.getGameRoomId(), Color.BLUE));
  }
}
