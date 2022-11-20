package at.ac.uibk.timeguess.flipflapp.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAMEMANAGER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAME_ROOM;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.game.room.CreateGameRoomRequest;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomUser;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@AutoConfigureMockMvc
@Transactional
public class TeamControllerTest {

  private GameRoom testGameRoom;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private GameRoomService gameRoomService;

  @Autowired
  private TopicRepository topicRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserDetailsService userDetailsService;

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
  public void createTeam() throws Exception {
    mockMvc.perform(post("/api/gamerooms/%d/teams".formatted(testGameRoom.getGameRoomId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk());
  }

  @Test
  public void createTeamUnauthenticated() throws Exception {
    mockMvc.perform(post("/api/gamerooms/%d/teams".formatted(testGameRoom.getGameRoomId())))
        .andExpect(status().isForbidden());
  }

  @Test
  public void createTeamForNotExistingGameRoom() throws Exception {
    mockMvc.perform(post("/api/gamerooms/%d/teams".formatted(-1L))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void createTeamAsPlayer() throws Exception {
    testGameRoom.setGameHostId(-1L);
    mockMvc.perform(post("/api/gamerooms/%d/teams".formatted(testGameRoom.getGameRoomId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void createTooManyTeams() throws Exception {
    testGameRoom.getAvailableTeamsList().addAll(Set.of(Color.values()));
    mockMvc.perform(post("/api/gamerooms/%d/teams".formatted(testGameRoom.getGameRoomId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isConflict());
  }

  @Test
  public void joinTeam() throws Exception {
    testGameRoom.createTeam(Color.BLUE);
    mockMvc.perform(put("/api/gamerooms/%d/teams/users/%d"
        .formatted(testGameRoom.getGameRoomId(), testGameRoom.getGameHostId()))
        .param("teamColor", Color.BLUE.toString())
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk());
  }

  @Test
  public void joinTeamForNotExistingGameRoom() throws Exception {
    mockMvc.perform(put("/api/gamerooms/%d/teams/users/%d"
        .formatted(-1L, testGameRoom.getGameHostId()))
        .param("teamColor", Color.BLUE.toString())
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void joinTeamForNotExistingTeam() throws Exception {
    assertThat(testGameRoom.getAvailableTeamsList()).doesNotContain(Color.BLUE);
    mockMvc.perform(put("/api/gamerooms/%d/teams/users/%d"
        .formatted(testGameRoom.getGameRoomId(), testGameRoom.getGameHostId()))
        .param("teamColor", Color.BLUE.toString())
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void joinTeamForNotExistingUser() throws Exception {
    assertThat(testGameRoom.getAvailableTeamsList()).doesNotContain(Color.BLUE);
    mockMvc.perform(put("/api/gamerooms/%d/teams/users/%d"
        .formatted(testGameRoom.getGameRoomId(), -1L))
        .param("teamColor", Color.BLUE.toString())
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void joinNullTeam() throws Exception {
    mockMvc.perform(put("/api/gamerooms/%d/teams/users/%d?teamColor"
        .formatted(testGameRoom.getGameRoomId(), testGameRoom.getGameHostId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void leaveTeam() throws Exception {
    testGameRoom.createTeam(Color.BLUE);
    testGameRoom.joinTeam(userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME).orElseThrow(),
        Color.BLUE);
    mockMvc.perform(delete("/api/gamerooms/%d/teams/users/%d"
        .formatted(testGameRoom.getGameRoomId(), testGameRoom.getGameHostId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk());
    final GameRoomUser gameHost = testGameRoom.getGameRoomUsers().stream()
        .filter(
            gameRoomUser -> gameRoomUser.getUser().getUserId().equals(testGameRoom.getGameHostId()))
        .findFirst().orElseThrow();
    assertThat(gameHost.getTeamColor()).isNull();
  }

  @Test
  public void leaveTeamForNotExistingGameRoom() throws Exception {
    mockMvc.perform(delete("/api/gamerooms/%d/teams/users/%d"
        .formatted(-1L, testGameRoom.getGameHostId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void leaveTeamForNotExistingUser() throws Exception {
    mockMvc.perform(delete("/api/gamerooms/%d/teams/users/%d"
        .formatted(testGameRoom.getGameRoomId(), -1L))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void leaveTeamSetsReadyToFalse() throws Exception {
    final User gameManager = userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME)
        .orElseThrow();
    testGameRoom.createTeam(Color.BLUE);
    testGameRoom.joinTeam(gameManager, Color.BLUE);
    testGameRoom.setMark(gameManager, true);
    mockMvc.perform(delete("/api/gamerooms/%d/teams/users/%d"
        .formatted(testGameRoom.getGameRoomId(), testGameRoom.getGameHostId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk());
    final GameRoomUser gameHost = testGameRoom.getGameRoomUsers().stream()
        .filter(
            gameRoomUser -> gameRoomUser.getUser().getUserId().equals(testGameRoom.getGameHostId()))
        .findFirst().orElseThrow();
    assertThat(gameHost.isReady()).isFalse();
  }

  @Test
  public void deleteTeam() throws Exception {
    testGameRoom.createTeam(Color.BLUE);
    mockMvc.perform(delete("/api/gamerooms/%d/teams"
        .formatted(testGameRoom.getGameRoomId()))
        .param("teamColor", Color.BLUE.toString())
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk());
  }

  @Test
  public void deleteTeamAsPlayer() throws Exception {
    testGameRoom.createTeam(Color.BLUE);
    testGameRoom.setGameHostId(-1L);
    mockMvc.perform(delete("/api/gamerooms/%d/teams"
        .formatted(testGameRoom.getGameRoomId()))
        .param("teamColor", Color.BLUE.toString())
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void deleteTeamForNotExistingGameRoom() throws Exception {
    testGameRoom.createTeam(Color.BLUE);
    mockMvc.perform(delete("/api/gamerooms/%d/teams"
        .formatted(-1L))
        .param("teamColor", Color.BLUE.toString())
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void deleteNotExistingTeam() throws Exception {
    mockMvc.perform(delete("/api/gamerooms/%d/teams"
        .formatted(testGameRoom.getGameRoomId()))
        .param("teamColor", Color.BLUE.toString())
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isNotFound());
  }
}
