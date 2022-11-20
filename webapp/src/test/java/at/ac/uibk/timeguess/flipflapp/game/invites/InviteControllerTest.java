package at.ac.uibk.timeguess.flipflapp.game.invites;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_ADMIN;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAMEMANAGER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.game.room.CreateGameRoomRequest;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import java.util.List;
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
public class InviteControllerTest {

  private GameRoom gameRoom;
  User gameHost;
  User randomPlayer;

  private Long gameRoomId;
  User toBeInvitedUser;

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

  @BeforeEach
  public void setup() {
    final Topic topic = topicRepository.findAll().get(0);
    gameHost = userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME).orElseThrow();
    randomPlayer = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();

    gameRoom = gameRoomService.createGameRoom(
        new CreateGameRoomRequest(topic.getTopicId(), "Room", MaxPoints.TEN), gameHost.getUserId());

    gameRoomId = gameRoom.getGameRoomId();
    Long gameHostId = gameRoom.getGameHostId();

    toBeInvitedUser = userRepository.findByUsername(RANDOM_ADMIN.USERNAME).get();
    CreateInviteRequest inviteRequest1 = new CreateInviteRequest(gameRoomId, List.of(toBeInvitedUser.getUserId()));
    gameRoomService.addInvite(inviteRequest1, gameHostId);
  }


  @Test
  public void createInvite() throws Exception {
    mockMvc.perform(post("/api/invites").content("""
        {
            "gameRoomId": %d,
            "userIdList": [
              %d
            ]
        }
        """.formatted(gameRoom.getGameRoomId(), toBeInvitedUser.getUserId()))
        .contentType(MediaType.APPLICATION_JSON)
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("%d".formatted(toBeInvitedUser.getUserId()))));
  }

  @Test
  public void createInviteForNotExistingUser() throws Exception {
    mockMvc.perform(post("/api/invites").content("""
        {
            "gameRoomId": %d,
            "userIdList": [
              -1
            ]
        }
        """.formatted(gameRoom.getGameRoomId()))
        .contentType(MediaType.APPLICATION_JSON)
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void createInviteForMultipleUsers() throws Exception {
    mockMvc.perform(post("/api/invites").content("""
        {
            "gameRoomId": %d,
            "userIdList": [
              %d,
              %d
            ]
        }
        """
        .formatted(gameRoom.getGameRoomId(), randomPlayer.getUserId(), toBeInvitedUser.getUserId()))
        .contentType(MediaType.APPLICATION_JSON)
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(randomPlayer.getUserId().toString())))
        .andExpect(content().string(containsString(toBeInvitedUser.getUserId().toString())));
  }

  @Test
  public void createInviteForMultipleUsersAndNotExistingUser() throws Exception {
    gameRoom.getInvitedUsers().clear();
    mockMvc.perform(post("/api/invites").content("""
        {
            "gameRoomId": %d,
            "userIdList": [
              -1,
              %d
            ]
        }
        """
        .formatted(gameRoom.getGameRoomId(), toBeInvitedUser.getUserId()))
        .contentType(MediaType.APPLICATION_JSON)
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isNotFound());
    assertThat(gameRoom.getInvitedUsers()).doesNotContain(toBeInvitedUser);
  }

  @Test
  public void createInviteForNotExistingGameRoom() throws Exception {
    mockMvc.perform(post("/api/invites").content("""
        {
            "gameRoomId": -1,
            "userIdList": [
              %d
            ]
        }
        """.formatted(toBeInvitedUser.getUserId()))
        .contentType(MediaType.APPLICATION_JSON)
        .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void createInviteAsPlayer() throws Exception {
    mockMvc.perform(post("/api/invites").content("""
        {
            "gameRoomId": %d,
            "userIdList": [
              %d
            ]
        }
        """.formatted(gameRoom.getGameRoomId(), toBeInvitedUser.getUserId()))
        .contentType(MediaType.APPLICATION_JSON)
        .with(user(userDetailsService.loadUserByUsername(randomPlayer.getUsername()))))
        .andExpect(status().isForbidden());
  }


  @Test
  public void getInvitesWithInvalidId() throws Exception {
    mockMvc
        .perform(get("/api/invites/-1")
            .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void getInvites() throws Exception {
    mockMvc
        .perform(get("/api/invites/%d".formatted(toBeInvitedUser.getUserId()))
            .with(user(userDetailsService.loadUserByUsername(toBeInvitedUser.getUsername()))))
        .andExpect(status().isOk());
  }

  @Test
  public void getInvitesUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/invites/%d".formatted(toBeInvitedUser.getUserId())))
        .andExpect(status().isForbidden());
  }

  @Test
  public void getInvitesFromOtherUser() throws Exception {
    mockMvc
        .perform(get("/api/invites/%d".formatted(toBeInvitedUser.getUserId()))
            .with(user(userDetailsService.loadUserByUsername(gameHost.getUsername()))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void acceptInvite() throws Exception {
    mockMvc.perform(post("/api/invites/%d/accept".formatted(gameRoomId))
        .with(user(userDetailsService.loadUserByUsername(toBeInvitedUser.getUsername()))))
        .andExpect(status().isOk());
  }

  @Test
  public void acceptInviteForNotExistingInvite() throws Exception {
    mockMvc.perform(post("/api/invites/%d/accept".formatted(-1))
        .with(user(userDetailsService.loadUserByUsername(toBeInvitedUser.getUsername()))))
        .andExpect(status().isNotFound());
  }

  @Test
  public void acceptInviteForNotInvitedPlayer() throws Exception {
    mockMvc.perform(post("/api/invites/%d/accept".formatted(gameRoomId))
        .with(user(userDetailsService.loadUserByUsername(randomPlayer.getUsername()))))
        .andExpect(status().isNotFound());
  }
}
