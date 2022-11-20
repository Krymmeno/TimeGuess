package at.ac.uibk.timeguess.flipflapp.game;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.SamplePlayer;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.SampleTopic;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.round.Activity;
import at.ac.uibk.timeguess.flipflapp.game.round.GameRound;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.game.round.Result;
import at.ac.uibk.timeguess.flipflapp.game.round.RoundPoints;
import at.ac.uibk.timeguess.flipflapp.game.round.Time;
import at.ac.uibk.timeguess.flipflapp.team.Color;
import at.ac.uibk.timeguess.flipflapp.team.Team;
import at.ac.uibk.timeguess.flipflapp.term.Term;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicService;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.LongStream;
import javax.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
public class GameTest {

  @Autowired
  private TopicService topicService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GameRepository gameRepository;

  User gameHost;

  Term term;

  Topic topic0;
  Topic topic10;
  Topic topic30;

  User u1;
  User u2;
  User u3;
  User u4;
  User u5;
  User u6;

  TimeFlip timeFlip;

  @PostConstruct
  public void setup() {
    topic0 = topicService.findTopicByName(SampleTopic.NAME + "_0").get();
    topic10 = topicService.findTopicByName(SampleTopic.NAME + "_10").get();
    topic30 = topicService.findTopicByName(SampleTopic.NAME + "_30").get();

    gameHost = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).get();
    u1 = userRepository.findByUsername(SamplePlayer.USERNAME + "_1").get();
    u2 = userRepository.findByUsername(SamplePlayer.USERNAME + "_2").get();
    u3 = userRepository.findByUsername(SamplePlayer.USERNAME + "_3").get();
    u4 = userRepository.findByUsername(SamplePlayer.USERNAME + "_4").get();
    u5 = userRepository.findByUsername(SamplePlayer.USERNAME + "_5").get();
    u6 = userRepository.findByUsername(SamplePlayer.USERNAME + "_6").get();
    term = topic30.getTerms().stream().findFirst().get();


    GameRoom gameRoom = new GameRoom(1L, gameHost.getUserId(), "Hello", topic30, MaxPoints.TEN, 2);
    List.of(u1, u2, u3, u4).forEach(gameRoom::joinRoom);
    gameRoom.createTeam(Color.RED);
    gameRoom.createTeam(Color.BLUE);
    gameRoom.joinTeam(u1, Color.BLUE);
    gameRoom.joinTeam(u2, Color.BLUE);
    gameRoom.joinTeam(u3, Color.RED);
    gameRoom.joinTeam(u4, Color.RED);
    assertTrue(Game.of(gameRoom).isLeft());

    timeFlip = new TimeFlip("address", "name");
    gameRoom.setTimeFlip(timeFlip);
    assertTrue(Game.of(gameRoom).isRight());
  }

  @Test
  @DirtiesContext
  public void testCreateTeam() {
    Team team = Team.of(Color.GREEN, null, null);
    assertEquals(Color.GREEN, team.getColor());
    assertEquals(Set.of(), team.getPlayers());

    team = Team.of(null, null, null);
    assertNull(team.getColor());
    assertEquals(Set.of(), team.getPlayers());

    team = Team.of(null, Set.of(u1, u2, u3), null);
    assertNull(team.getColor());
    assertEquals(Set.of(u1, u2, u3), team.getPlayers());

    team = Team.of(Color.ORANGE, Set.of(u1, u2, u3), null);
    assertEquals(Color.ORANGE, team.getColor());
    assertEquals(Set.of(u1, u2, u3), team.getPlayers());

    GameRoom gameRoom = new GameRoom(1L, gameHost.getUserId(), "Hello", topic30, MaxPoints.TEN, 2);
    List.of(u1, u2, u3, u4).forEach(u -> {
      gameRoom.joinRoom(u);
      assertTrue(Game.of(gameRoom).isLeft());
    });

    gameRoom.createTeam(Color.RED);
    gameRoom.createTeam(Color.BLUE);
    gameRoom.joinTeam(u1, Color.BLUE);
    gameRoom.joinTeam(u2, Color.BLUE);
    gameRoom.joinTeam(u3, Color.RED);
    gameRoom.joinTeam(u4, Color.RED);
    gameRoom.setTimeFlip(timeFlip);
    Game game = Game.of(gameRoom).getRight();

    team = Team.of(Color.ORANGE, Set.of(u1, u2, u3), game);
    assertEquals(Color.ORANGE, team.getColor());
    assertEquals(Set.of(u1, u2, u3), team.getPlayers());
  }

  private void checkTeam(GameRoom gameRoom, Color color, Set<User> players) {
    Game game = Game.of(gameRoom).getRight();
    assertTrue(game.getTeams().contains(Team.of(color, players, game)));
  }

  private void addThreePointsRound(Game game, Long roundNumber){
    game.setCurrentRound(GameRound.of(game, roundNumber, u4, term));
    GameRound currentRound = game.getCurrentRound();
    currentRound.setProperties(Activity.DRAW, Time.ONE, RoundPoints.THREE);
    currentRound.stopTimer();
    currentRound.setResult(Result.WIN);
    game.addCurrentRound();
  }

  @Test
  @DirtiesContext
  public void testCreateGame() {
    GameRoom gameRoom = new GameRoom(1L, gameHost.getUserId(), "Hello", topic30, MaxPoints.TEN, 2);

    var eg = Game.of(gameRoom);
    assertTrue(eg.isLeft());

    gameRoom.setTimeFlip(timeFlip);
    assertTrue(eg.isLeft());

    List.of(u1, u2, u3, u4).forEach(u -> {
      gameRoom.joinRoom(u);
      assertTrue(Game.of(gameRoom).isLeft());
    });

    gameRoom.createTeam(Color.RED);
    gameRoom.createTeam(Color.BLUE);
    gameRoom.joinTeam(u1, Color.BLUE);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.joinTeam(u2, Color.BLUE);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.joinTeam(u3, Color.RED);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.joinTeam(u4, Color.RED);
    assertTrue(Game.of(gameRoom).isRight());

    gameRoom.setGameHostId(null);
    assertTrue(Game.of(gameRoom).isRight());
    checkTeam(gameRoom, Color.BLUE, Set.of(u1, u2));
    checkTeam(gameRoom, Color.RED, Set.of(u3, u4));

    gameRoom.leaveRoom(u1);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.createTeam(Color.ORANGE);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.joinRoom(u5);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.joinTeam(u5, Color.ORANGE);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.joinRoom(u1);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.joinTeam(u1, Color.ORANGE);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.joinRoom(u6);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.joinTeam(u6, Color.BLUE);
    assertTrue(Game.of(gameRoom).isRight());
    checkTeam(gameRoom, Color.BLUE, Set.of(u6, u2));
    checkTeam(gameRoom, Color.RED, Set.of(u3, u4));
    checkTeam(gameRoom, Color.ORANGE, Set.of(u5, u1));

    gameRoom.deleteTeam(Color.ORANGE);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.leaveRoom(u1);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.leaveRoom(u5);
    assertTrue(Game.of(gameRoom).isRight());
    checkTeam(gameRoom, Color.BLUE, Set.of(u6, u2));
    checkTeam(gameRoom, Color.RED, Set.of(u3, u4));

    gameRoom.setTopic(null);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.setTopic(topic10);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.setTopic(topic0);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.setTopic(topic30);
    assertTrue(Game.of(gameRoom).isRight());

    gameRoom.setMaxPoints(null);
    assertTrue(Game.of(gameRoom).isLeft());

    gameRoom.setMaxPoints(MaxPoints.TWENTY);
    assertTrue(Game.of(gameRoom).isRight());
    checkTeam(gameRoom, Color.BLUE, Set.of(u6, u2));
    checkTeam(gameRoom, Color.RED, Set.of(u3, u4));

    Game game = Game.of(gameRoom).getRight();
    assertThat(game.getWinner()).isEmpty();
    LongStream.range(0, 6).forEach(i -> {
      addThreePointsRound(game, i);
      assertThat(game.getWinner()).isEmpty();
    });
    addThreePointsRound(game, 7L);
    assertThat(game.getWinner()).isPresent();

    Long gameId = gameRepository.save(game).getGameId();

    List<Game> allGames = gameRepository.findAll();
    assertEquals(1, allGames.stream().filter(g -> Objects.equals(g.getGameId(), gameId)).count());
    assertEquals(2, game.getTeams().size());
  }
}

