package at.ac.uibk.timeguess.flipflapp.game.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_ADMIN;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAMEMANAGER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.team.Color;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipService;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipUpdate;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@AutoConfigureMockMvc
@Transactional
public class GameRoomControllerTest {

  private GameRoom testGameRoom;

  private TimeFlip timeFlip;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private TopicRepository topicRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GameRoomService gameRoomService;

  @Autowired
  private TimeFlipService timeFlipService;

  @BeforeEach
  public void setup() {
    byte battery = 10;
    byte facet = 1;
    final User gameHost = userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME).orElseThrow();
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    testGameRoom = gameRoomService
        .createGameRoom(new CreateGameRoomRequest(topic.getTopicId(), "Room", MaxPoints.TEN),
            gameHost.getUserId());
    timeFlip = timeFlipService
        .recognize(new TimeFlipUpdate("5C:61:CF:C7:95:90", "Flipper", true, battery, facet));
  }

  @Test
  public void createGameRoom() throws Exception {
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    mockMvc.perform(post("/api/gamerooms").content("""
        {
            "roomName": "TEST",
            "topicId": %d,
            "maxPoints": "TEN"
        }
        """.formatted(topic.getTopicId()))
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("TEST")));
  }

  @Test
  public void createGameRoomUnauthenticated() throws Exception {
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    mockMvc.perform(post("/api/gamerooms").content("""
        {
            "roomName": "TEST",
            "topicId": %d,
            "maxPoints": "TEN"
        }
        """.formatted(topic.getTopicId()))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  public void createGameRoomWithMissingFields() throws Exception {
    mockMvc.perform(post("/api/gamerooms").content("""
        {
            "roomName": "TEST",
            "maxPoints": "TEN"
        }
        """)
        .with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void createGameRoomWithNotExistingTopic() throws Exception {
    assertThat(topicRepository.findById(-1L)).isEmpty();
    mockMvc.perform(post("/api/gamerooms").content("""
        {
            "roomName": "TEST",
            "topicId": -1,
            "maxPoints": "TEN"
        }
        """)
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getGameRoom() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(get("/api/gamerooms/%d".formatted(testGameRoom.getGameRoomId()))
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(testGameRoom.getName())));
  }

  @Test
  public void getGameRoomAsGameManager() throws Exception {
    mockMvc.perform(get("/api/gamerooms/%d".formatted(testGameRoom.getGameRoomId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(testGameRoom.getName())));
  }

  @Test
  public void getNotExistingGameRoom() throws Exception {
    mockMvc.perform(get("/api/gamerooms/%d".formatted(-1))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getGameRoomAsNonAssociatedPlayer() throws Exception {
    final User player = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();
    assertThat(testGameRoom.getGameRoomUsers().stream().map(GameRoomUser::getUser))
        .doesNotContain(player);
    mockMvc.perform(get("/api/gamerooms/%d".formatted(testGameRoom.getGameRoomId()))
        .with(user(userDetailsService.loadUserByUsername(player.getUsername()))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void getAllGameRoomsAsGameManager() throws Exception {
    mockMvc.perform(get("/api/gamerooms")
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk());
  }

  @Test
  public void getAllGameRoomsAsPlayer() throws Exception {
    mockMvc.perform(get("/api/gamerooms")
        .with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void setMaxPoints() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(patch("/api/gamerooms/%d/maxPoints".formatted(testGameRoom.getGameRoomId()))
        .param("maxPoints", "TWENTY")
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isOk());
  }

  @Test
  public void setMaxPointsAsNull() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc
        .perform(
            patch("/api/gamerooms/%d/maxPoints?maxPoints".formatted(testGameRoom.getGameRoomId()))
                .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void setInvalidMaxPoints() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(patch("/api/gamerooms/%d/maxPoints".formatted(testGameRoom.getGameRoomId()))
        .param("maxPoints", "TWENTY12")
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void setMaxPointsAsPlayer() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    testGameRoom.setGameHostId(-1L);
    mockMvc.perform(patch("/api/gamerooms/%d/maxPoints".formatted(testGameRoom.getGameRoomId()))
        .param("maxPoints", "TWENTY")
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isForbidden());
    testGameRoom.setGameHostId(gameHost.getUserId());
  }

  @Test
  public void setMaxPointsAsAdmin() throws Exception {
    mockMvc.perform(patch("/api/gamerooms/%d/maxPoints".formatted(testGameRoom.getGameRoomId()))
        .param("maxPoints", "TWENTY")
        .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME))))
        .andExpect(status().isOk());
  }

  @Test
  public void setMaxPointsAsNonAssociatedPlayer() throws Exception {
    final User user = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();
    testGameRoom.leaveRoom(user);
    mockMvc.perform(patch("/api/gamerooms/%d/maxPoints".formatted(testGameRoom.getGameRoomId()))
        .param("maxPoints", "TWENTY")
        .with(user(userDetailsService.loadUserByUsername(user.getUsername()))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void setTopic() throws Exception {
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(patch("/api/gamerooms/%d/topic".formatted(testGameRoom.getGameRoomId()))
        .param("topicId", String.valueOf(topic.getTopicId()))
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isOk());
  }

  @Test
  public void setTopicAsPlayer() throws Exception {
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    testGameRoom.setGameHostId(-1L);
    mockMvc.perform(patch("/api/gamerooms/%d/topic".formatted(testGameRoom.getGameRoomId()))
        .param("topicId", String.valueOf(topic.getTopicId()))
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isForbidden());
    testGameRoom.setGameHostId(gameHost.getUserId());
  }

  @Test
  public void setTopicAsAdmin() throws Exception {
    assertThat(topicRepository.findAll()).isNotEmpty();
    final Topic topic = topicRepository.findAll().get(0);
    mockMvc.perform(patch("/api/gamerooms/%d/topic".formatted(testGameRoom.getGameRoomId()))
        .param("topicId", String.valueOf(topic.getTopicId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME))))
        .andExpect(status().isOk());
  }

  @Test
  public void setNotExistingTopic() throws Exception {
    assertThat(topicRepository.findById(-1L)).isEmpty();
    mockMvc.perform(patch("/api/gamerooms/%d/topic".formatted(testGameRoom.getGameRoomId()))
        .param("topicId", String.valueOf(-1L))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void setReady() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    testGameRoom.createTeam(Color.BLUE);
    testGameRoom.joinTeam(gameHost, Color.BLUE);
    mockMvc.perform(patch("/api/gamerooms/%d/users/%d/ready"
        .formatted(testGameRoom.getGameRoomId(), gameHost.getUserId()))
        .param("isReady", "true")
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isOk());
    testGameRoom.deleteTeam(Color.BLUE);
  }

  @Test
  public void setReadyForDifferentUser() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(patch("/api/gamerooms/%d/users/%d/ready"
        .formatted(testGameRoom.getGameRoomId(), gameHost.getUserId()))
        .param("isReady", "true")
        .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void setReadyWithNull() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(patch("/api/gamerooms/%d/users/%d/ready?isReady"
        .formatted(testGameRoom.getGameRoomId(), gameHost.getUserId()))
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void setReadyWithoutTeam() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(patch("/api/gamerooms/%d/users/%d/ready"
        .formatted(testGameRoom.getGameRoomId(), gameHost.getUserId()))
        .param("isReady", "true")
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isConflict());
  }

  @Test
  public void leaveGameRoom() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(delete("/api/gamerooms/%d/users/%d"
        .formatted(testGameRoom.getGameRoomId(), gameHost.getUserId()))
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isOk());
  }

  @Test
  public void removePlayerFromGameRoom() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    final User gameManagerAsPlayer = userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME)
        .orElseThrow();
    testGameRoom.joinRoom(gameManagerAsPlayer);
    mockMvc.perform(delete("/api/gamerooms/%d/users/%d"
        .formatted(testGameRoom.getGameRoomId(), gameManagerAsPlayer.getUserId()))
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isOk());
    testGameRoom.leaveRoom(gameManagerAsPlayer);
  }

  @Test
  public void removeNotExistingPlayerFromGameRoom() throws Exception {
    assertThat(userRepository.findById(-1L)).isEmpty();
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(delete("/api/gamerooms/%d/users/%d"
        .formatted(testGameRoom.getGameRoomId(), -1L))
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void removePlayerAsPlayer() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    final User player = userRepository.findByUsername(RANDOM_PLAYER.USERNAME)
        .orElseThrow();
    testGameRoom.joinRoom(player);
    mockMvc.perform(delete("/api/gamerooms/%d/users/%d"
        .formatted(testGameRoom.getGameRoomId(), gameHost.getUserId()))
        .with(user(userDetailsService.loadUserByUsername(player.getUsername()))))
        .andExpect(status().isForbidden());
    testGameRoom.leaveRoom(player);
  }

  @Test
  public void setTimeFlipAsPlayer() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(patch("/api/gamerooms/%d/timeflip/%d"
        .formatted(testGameRoom.getGameRoomId(), timeFlip.getTimeFlipId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void setTimeFlipAsAdmin() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(patch("/api/gamerooms/%d/timeflip/%d"
        .formatted(testGameRoom.getGameRoomId(), timeFlip.getTimeFlipId()))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME))))
        .andExpect(status().isOk());
  }

  @Test
  void setTimeFlip() throws Exception {
    final User gameHost = userRepository.findById(testGameRoom.getGameHostId()).orElseThrow();
    mockMvc.perform(patch("/api/gamerooms/%d/timeflip/%d"
        .formatted(testGameRoom.getGameRoomId(), timeFlip.getTimeFlipId()))
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isOk());
  }

}
