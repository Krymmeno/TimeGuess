package at.ac.uibk.timeguess.flipflapp.game;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
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
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipFacet;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicService;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
public class GameControlManagerTest{

  @Autowired
  private TopicService topicService;

  @Autowired
  private UserRepository userRepository;

  Term term;

  Topic topic30;

  User u1;
  User u2;
  User u3;
  User u4;

  Game game;
  GameControlManager gameControlManager;

  TimeFlip timeFlip;
  Activity activity = Activity.DRAW;
  Time time = Time.ONE;
  RoundPoints roundPoints = RoundPoints.THREE;
  byte facetKey = 1;

  @BeforeEach
  public void setup() {
    u1 = userRepository.findByUsername(SamplePlayer.USERNAME + "_1").get();
    u2 = userRepository.findByUsername(SamplePlayer.USERNAME + "_2").get();
    u3 = userRepository.findByUsername(SamplePlayer.USERNAME + "_3").get();
    u4 = userRepository.findByUsername(SamplePlayer.USERNAME + "_4").get();
    topic30 = topicService.findTopicByName(SampleTopic.NAME + "_30").get();
    term = topic30.getTerms().stream().findFirst().get();

    GameRoom gameRoom = new GameRoom(1L, u1.getUserId(), "Hello", topic30, MaxPoints.TEN, 2);
    List.of(u1, u2, u3, u4).forEach(gameRoom::joinRoom);
    gameRoom.createTeam(Color.RED);
    gameRoom.createTeam(Color.BLUE);
    gameRoom.joinTeam(u1, Color.BLUE);
    gameRoom.joinTeam(u2, Color.BLUE);
    gameRoom.joinTeam(u3, Color.RED);
    gameRoom.joinTeam(u4, Color.RED);
    assertTrue(Game.of(gameRoom).isLeft());

    TimeFlipFacet timeFlipFacet = new TimeFlipFacet(activity, roundPoints, time);

    timeFlip = new TimeFlip("address", "name");
    timeFlip.getTimeFlipFacetMap().put(facetKey, timeFlipFacet);
    gameRoom.setTimeFlip(timeFlip);
    game = Game.of(gameRoom).getRight();
    gameControlManager = GameControlManager.of(game, timeFlip);
  }

  @Test
  @DirtiesContext
  public void checkOf(){
    assertThat(gameControlManager.getGameState()).isEqualTo(GameState.INITIALIZED);
    assertThat(gameControlManager.getCurrentRound()).isNull();

    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundProperties(facetKey));
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundResult(Result.WIN));
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundEnd());
    assertDoesNotThrow(() -> gameControlManager.startNewRound());

  }

  @Test
  @DirtiesContext
  public void testStartNewRound(){
    gameControlManager.startNewRound();
    assertThat(gameControlManager.getCurrentRound()).isNotNull();
    assertThat(gameControlManager.getGameState()).isEqualTo(GameState.EXPECTING_PROPS);

    assertThrows(InvalidGameActionException.class, () -> gameControlManager.startNewRound());
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundResult(Result.WIN));
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundEnd());
    assertDoesNotThrow(() -> gameControlManager.setRoundProperties(facetKey));

    GameRound currentRound = gameControlManager.getCurrentRound();
    assertThat(currentRound.getUser()).isNotNull();
  }

  @Test
  @DirtiesContext
  public void testSetRoundProperties(){
    gameControlManager.startNewRound();
    gameControlManager.setRoundProperties(facetKey);
    assertThat(gameControlManager.getGameState()).isEqualTo(GameState.EXPECTING_ROUND_END);

    assertThrows(InvalidGameActionException.class, () -> gameControlManager.startNewRound());
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundProperties(facetKey));
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundResult(Result.WIN));
    assertDoesNotThrow(() -> gameControlManager.setRoundEnd());

    GameRound currentRound = gameControlManager.getCurrentRound();
    assertThat(currentRound.getActivity()).isEqualTo(activity);
    assertThat(currentRound.getRoundPoints()).isEqualTo(roundPoints);
    assertThat(currentRound.getTime()).isEqualTo(time);
  }

  @Test
  @DirtiesContext
  public void testSetRoundEnd(){
    gameControlManager.startNewRound();
    gameControlManager.setRoundProperties(facetKey);
    gameControlManager.setRoundEnd();

    assertThat(gameControlManager.getGameState()).isEqualTo(GameState.EXPECTING_RESULT);

    assertThrows(InvalidGameActionException.class, () -> gameControlManager.startNewRound());
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundProperties(facetKey));
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundEnd());
    assertDoesNotThrow(() -> gameControlManager.setRoundResult(Result.WIN));

    GameRound currentRound = gameControlManager.getCurrentRound();
    assertThat(currentRound.getResult()).isEqualTo(Result.WIN);
  }

  @Test
  @DirtiesContext
  public void testSetRoundResult(){
    gameControlManager.startNewRound();
    gameControlManager.setRoundProperties(facetKey);
    gameControlManager.setRoundEnd();

    assertThat(gameControlManager.getGame().getGameRounds().size()).isEqualTo(0);
    gameControlManager.setRoundResult(Result.WIN);
    assertThat(gameControlManager.getGame().getGameRounds().size()).isEqualTo(1);
    assertThat(gameControlManager.getGameState()).isEqualTo(GameState.EXPECTING_ROUNDSTART);

    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundProperties(facetKey));
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundEnd());
    assertThrows(InvalidGameActionException.class, () -> gameControlManager.setRoundResult(Result.WIN));
    assertDoesNotThrow(() -> gameControlManager.startNewRound());
  }

  @Test
  @DirtiesContext
  public void testPlayer(){
    gameControlManager.startNewRound();

    LongStream.rangeClosed(0, 6).forEach((i) -> {
      GameRound gameRound = gameControlManager.getCurrentRound();
      User currentUser = gameRound.getUser();
      Team currentTeam = gameControlManager.getCurrentTeam();

      gameControlManager.setRoundProperties(facetKey);
      gameControlManager.setRoundEnd();
      gameControlManager.setRoundResult(Result.WIN);

      if (i != 6){
        gameControlManager.startNewRound();
      }

      GameRound nextGameRound = gameControlManager.getCurrentRound();
      User nextUser = nextGameRound.getUser();
      Team nextTeam = gameControlManager.getCurrentTeam();

      GameState gameState = gameControlManager.getGameState();
      if (i == 6){
        assertThat(gameState).isEqualTo(GameState.FINISHED);
        assertThrows(InvalidGameActionException.class, () -> gameControlManager.startNewRound());
      } else {
        assertThat(gameControlManager.getGameState()).isNotEqualTo(GameState.FINISHED);
        assertThat(currentUser).isNotEqualTo(nextUser);
        assertThat(currentTeam).isNotEqualTo(nextTeam);
      }

    });

  }
}

