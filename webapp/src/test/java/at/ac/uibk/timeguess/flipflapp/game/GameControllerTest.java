package at.ac.uibk.timeguess.flipflapp.game;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_ADMIN;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_INACTIVE_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.SamplePlayer;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.SampleTopic;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.game.room.CreateGameRoomRequest;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.game.round.Activity;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.game.round.Result;
import at.ac.uibk.timeguess.flipflapp.game.round.RoundPoints;
import at.ac.uibk.timeguess.flipflapp.game.round.Time;
import at.ac.uibk.timeguess.flipflapp.team.Color;
import at.ac.uibk.timeguess.flipflapp.team.Team;
import at.ac.uibk.timeguess.flipflapp.term.Term;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipFacet;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicService;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@AutoConfigureMockMvc
@Transactional
public class GameControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private TopicService topicService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameRoomService gameRoomService;

  private final String url = "/api/games/";

  Term term;

  Topic topic30;

  User gameHost;
  User u2;
  User u3;
  User u4;
  User u5;
  User u6;

  GameRoom gameRoom;
  GameRoom invalidGameRoom;
  SetGameResultRequest setGameResultRequest;
  byte facetKey = 1;

  Triple<String, Long, ResultMatcher> startGameTriple;
  Triple<String, Long, ResultMatcher> getGameTriple;

  @BeforeEach
  public void setup() {
    gameHost = userRepository.findByUsername(SamplePlayer.USERNAME + "_1").get();
    u2 = userRepository.findByUsername(SamplePlayer.USERNAME + "_2").get();
    u3 = userRepository.findByUsername(SamplePlayer.USERNAME + "_3").get();
    u4 = userRepository.findByUsername(SamplePlayer.USERNAME + "_4").get();
    u5 = userRepository.findByUsername(SamplePlayer.USERNAME + "_5").get();
    u6 = userRepository.findByUsername(SamplePlayer.USERNAME + "_6").get();

    topic30 = topicService.findTopicByName(SampleTopic.NAME + "_30").get();
    term = topic30.getTerms().stream().findFirst().get();

    CreateGameRoomRequest createGameRoomRequest = new CreateGameRoomRequest(topic30.getTopicId(), "testRoom", MaxPoints.TEN);
    gameRoom = gameRoomService.createGameRoom(createGameRoomRequest, gameHost.getUserId());

    invalidGameRoom = gameRoomService.createGameRoom(new CreateGameRoomRequest(topic30.getTopicId(), "testRoom", MaxPoints.TEN), u5.getUserId());

    List.of(u2, u3, u4).forEach(gameRoom::joinRoom);
    gameRoom.createTeam(Color.RED);
    gameRoom.createTeam(Color.BLUE);
    gameRoom.joinTeam(gameHost, Color.BLUE);
    gameRoom.joinTeam(u2, Color.BLUE);
    gameRoom.joinTeam(u3, Color.RED);
    gameRoom.joinTeam(u4, Color.RED);
    assertTrue(Game.of(gameRoom).isLeft());

    TimeFlipFacet timeFlipFacet = new TimeFlipFacet(Activity.DRAW, RoundPoints.ONE, Time.ONE);

    TimeFlip timeFlip = new TimeFlip("address", "name");
    timeFlip.getTimeFlipFacetMap().put(facetKey, timeFlipFacet);
    gameRoom.setTimeFlip(timeFlip);

    Long roomId = gameRoom.getGameRoomId();
    startGameTriple = Triple.of(gameHost.getUsername(), roomId, status().isOk());
    getGameTriple = Triple.of(gameHost.getUsername(), roomId, status().isOk());
    setGameResultRequest =  new SetGameResultRequest(roomId, Result.WIN);
   }

  private void start(Triple<String, Long, ResultMatcher> tc) throws Exception{
    mockMvc.perform(post(url + tc.getMiddle().toString() + "/startGame")
        .with(user(userDetailsService
            .loadUserByUsername(tc.getLeft())))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(tc.getRight());
  }

  private void getGame(Triple<String, Long, ResultMatcher> tc) throws Exception{
    mockMvc.perform(get(url + tc.getMiddle().toString())
        .with(user(userDetailsService
            .loadUserByUsername(tc.getLeft())))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(tc.getRight());
  }

  private String getSetGameResultRequestContent(Result result, Long gameId){
    return """
        {
            "result": "%s",
            "gameId": %d
        }
        """.formatted(result, gameId);
  }

  private void setResult(Triple<String, String, ResultMatcher> tc) throws Exception{
    mockMvc.perform(post(url + "setGameRoundResult").content(tc.getMiddle())
        .with(user(userDetailsService
            .loadUserByUsername(tc.getLeft())))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(tc.getRight());
  }


  @Test
  @DirtiesContext
  public void testStartGame() {
    Long roomId = gameRoom.getGameRoomId();
    List<Triple<String, Long, ResultMatcher>> testConfigList = new ArrayList<>();
    testConfigList.add(startGameTriple);
    testConfigList.add(Triple.of(u2.getUsername(), roomId, status().isForbidden()));
    testConfigList.add(Triple.of(u5.getUsername(), roomId, status().isForbidden()));
    testConfigList.add(Triple.of(RANDOM_ADMIN.USERNAME, roomId, status().isForbidden()));
    testConfigList.add(Triple.of(gameHost.getUsername(), -1L, status().isNotFound()));
    testConfigList.add(Triple.of(u5.getUsername(), invalidGameRoom.getGameRoomId(), status().isMethodNotAllowed()));

    testConfigList.forEach(tc -> {
      try {
        start(tc);
      } catch (Exception e) {
        fail();
      }
    });
  }

  @Test
  @DirtiesContext
  public void testGetGame() throws Exception{
    Long roomId = gameRoom.getGameRoomId();
    start(startGameTriple);

    List<Triple<String, Long, ResultMatcher>> testConfigList = new ArrayList<>();
    testConfigList.add(getGameTriple);
    testConfigList.add(Triple.of(u2.getUsername(), roomId, status().isOk()));
    testConfigList.add(Triple.of(u3.getUsername(), roomId, status().isOk()));
    testConfigList.add(Triple.of(RANDOM_ADMIN.USERNAME, roomId, status().isOk()));
    testConfigList.add(Triple.of(RANDOM_PLAYER.USERNAME, -1L, status().isNotFound()));
    testConfigList.add(Triple.of(u5.getUsername(), roomId, status().isOk()));

    testConfigList.forEach(tc -> {
      try {
        getGame(tc);
      } catch (Exception e) {
        fail();
      }
    });
  }


  @Test
  @DirtiesContext
  public void setResultValid() throws Exception{
    Long roomId = gameRoom.getGameRoomId();
    start(startGameTriple);

    gameService.setGameRoundProperties(roomId, facetKey);
    gameService.setRoundEnd(roomId);
    Team currentTeam = gameService.getGame(roomId).getCurrentTeam();
    User playingUser = currentTeam.getPlayers().stream().findFirst().get();
    User nonPlayingUser = gameService.getGame(roomId).getTeams().stream().filter(t -> !Objects.equals(t, currentTeam)).findFirst().get().getPlayers().stream().findFirst().get();

    List<Triple<String, String, ResultMatcher>> testConfigList = new ArrayList<>();
    testConfigList.add(Triple.of(nonPlayingUser.getUsername(), getSetGameResultRequestContent(Result.WIN, roomId), status().isOk()));
    testConfigList.add(Triple.of(playingUser.getUsername(), getSetGameResultRequestContent(Result.WIN, roomId), status().isForbidden()));

    testConfigList.forEach(tc -> {
      try {
        setResult(tc);
      } catch (Exception e) {
        fail();
      }
    });
  }

  @Test
  @DirtiesContext
  public void setResultInValid() throws Exception{
    Long roomId = gameRoom.getGameRoomId();
    start(startGameTriple);

    Team currentTeam = gameService.getGame(roomId).getCurrentTeam();
    User nonPlayingUser = gameService.getGame(roomId).getTeams().stream().filter(t -> !Objects.equals(t, currentTeam)).findFirst().get().getPlayers().stream().findFirst().get();

    List<Triple<String, String, ResultMatcher>> testConfigList = new ArrayList<>();
    testConfigList.add(Triple.of(nonPlayingUser.getUsername(), getSetGameResultRequestContent(Result.WIN, roomId), status().isMethodNotAllowed()));

    testConfigList.forEach(tc -> {
      try {
        setResult(tc);
      } catch (Exception e) {
        fail();
      }
    });
  }
}
