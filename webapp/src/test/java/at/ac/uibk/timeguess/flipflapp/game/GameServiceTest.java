package at.ac.uibk.timeguess.flipflapp.game;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.SamplePlayer;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.SampleTopic;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.game.room.CreateGameRoomRequest;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomNotFoundException;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.game.room.InvalidGameRoomException;
import at.ac.uibk.timeguess.flipflapp.game.round.Activity;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.game.round.Result;
import at.ac.uibk.timeguess.flipflapp.game.round.RoundPoints;
import at.ac.uibk.timeguess.flipflapp.game.round.Time;
import at.ac.uibk.timeguess.flipflapp.team.Color;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipFacet;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipFacetChangeEvent;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicService;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.LongStream;
import javax.transaction.Transactional;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
public class GameServiceTest{

  @Autowired
  private TopicService topicService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameRoomService gameRoomService;

  @Autowired
  private GameRepository gameRepository;

  Topic topic30;

  User u1;
  User u2;
  User u3;
  User u4;
  User u5;
  User u6;

  CreateGameRoomRequest createGameRoomRequest;
  GameRoom gameRoom;
  SetGameResultRequest setGameResultRequest;

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
    u5 = userRepository.findByUsername(SamplePlayer.USERNAME + "_5").get();
    u6 = userRepository.findByUsername(SamplePlayer.USERNAME + "_6").get();

    topic30 = topicService.findTopicByName(SampleTopic.NAME + "_30").get();

    createGameRoomRequest = new CreateGameRoomRequest(topic30.getTopicId(), "testRoom", MaxPoints.TEN);
    gameRoom = gameRoomService.createGameRoom(createGameRoomRequest, u1.getUserId());

    List.of(u2, u3, u4).forEach(gameRoom::joinRoom);
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

    Long roomId = gameRoom.getGameRoomId();
    setGameResultRequest =  new SetGameResultRequest(roomId, Result.WIN);
  }

  @Test
  @DirtiesContext
  public void testStartGame(){
    Long roomId = gameRoom.getGameRoomId();
    assertThrows(GameNotFoundException.class, () -> gameService.getGame(roomId));
    assertThrows(GameNotFoundException.class, () -> gameService.getRunningGame(roomId));
    assertThrows(GameNotFoundException.class, () -> gameService.getGameControlManager(roomId));

    assertThrows(GameRoomNotFoundException.class, () -> gameService.startGame(-1L));
    assertThrows(IllegalArgumentException.class, () -> gameService.startGame(null));
    GameRoom invalidGameRoom = gameRoomService.createGameRoom(createGameRoomRequest, u1.getUserId());
    assertThrows(InvalidGameRoomException.class, () -> gameService.startGame(invalidGameRoom.getGameRoomId()));

    Game game = gameService.startGame(gameRoom.getGameRoomId());
    assertDoesNotThrow(() -> assertThat(gameService.getGame(roomId)).isEqualTo(game));
    assertDoesNotThrow(() -> assertThat(gameService.getRunningGame(roomId)).isEqualTo(game));
    assertDoesNotThrow(() -> assertThat(gameService.getGameControlManager(roomId)).isNotNull());

    assertThat(game.getCurrentRound()).isNotNull();
    assertThat(game.getCurrentRound().getUser()).isNotNull();
    assertThrows(IllegalArgumentException.class, () -> gameService.startGame(gameRoom.getGameRoomId()));
  }

  @Test
  @DirtiesContext
  public void testStartNewGameRound(){
    Long roomId = gameRoom.getGameRoomId();
    gameService.startGame(gameRoom.getGameRoomId());

    gameService.setGameRoundProperties(roomId,facetKey);
    gameService.setRoundEnd(roomId);
    gameService.setGameRoundResult(setGameResultRequest);

    assertThrows(InvalidGameActionException.class, () -> gameService.setGameRoundProperties(roomId, facetKey));
    assertThrows(InvalidGameActionException.class, () -> gameService.setRoundEnd(roomId));
    assertThrows(InvalidGameActionException.class, () -> gameService.setGameRoundResult(setGameResultRequest));
    assertDoesNotThrow(() -> gameService.startNewGameRound(roomId));
  }

  @Test
  @DirtiesContext
  public void testSetGameRoundProperties(){
    Long roomId = gameRoom.getGameRoomId();
    gameService.startGame(gameRoom.getGameRoomId());

    assertThrows(InvalidGameActionException.class, () -> gameService.startNewGameRound(roomId));
    assertThrows(InvalidGameActionException.class, () -> gameService.setRoundEnd(roomId));
    assertThrows(InvalidGameActionException.class, () -> gameService.setGameRoundResult(setGameResultRequest));
    assertDoesNotThrow(() -> gameService.setGameRoundProperties(roomId, facetKey));
  }

  @Test
  @DirtiesContext
  public void testSetGameRoundEnd(){
    Long roomId = gameRoom.getGameRoomId();
    gameService.startGame(gameRoom.getGameRoomId());
    gameService.setGameRoundProperties(roomId, facetKey);

    assertThrows(InvalidGameActionException.class, () -> gameService.startNewGameRound(roomId));
    assertThrows(InvalidGameActionException.class, () -> gameService.setGameRoundProperties(roomId, facetKey));
    assertThrows(InvalidGameActionException.class, () -> gameService.setGameRoundResult(setGameResultRequest));
    assertDoesNotThrow(() -> gameService.setRoundEnd(roomId));
  }

  @Test
  @DirtiesContext
  public void testSetGameRoundResult(){
    Long roomId = gameRoom.getGameRoomId();
    gameService.startGame(gameRoom.getGameRoomId());
    gameService.setGameRoundProperties(roomId, facetKey);
    gameService.setRoundEnd(roomId);

    assertThrows(InvalidGameActionException.class, () -> gameService.startNewGameRound(roomId));
    assertThrows(InvalidGameActionException.class, () -> gameService.setGameRoundProperties(roomId, facetKey));
    assertThrows(InvalidGameActionException.class, () -> gameService.setRoundEnd(roomId));
    assertDoesNotThrow(() -> gameService.setGameRoundResult(setGameResultRequest));
  }


  @Test
  @DirtiesContext
  public void testIsPlayer() {
    Long roomId = gameRoom.getGameRoomId();
    gameService.startGame(gameRoom.getGameRoomId());
    Long u1id = u1.getUserId();
    Long u5id = u5.getUserId();

    assertThrows(GameNotFoundException.class, () -> gameService.isPlayer(-1L, u1id));
    assertThrows(UserNotFoundException.class, () -> gameService.isPlayer(roomId, -1L));
    assertThat(gameService.isPlayer(roomId, u5id)).isFalse();
    assertThat(gameService.isPlayer(roomId, u1id)).isTrue();
  }

  @Test
  @DirtiesContext
  public void testIsPlayerFromNonGuessingTeam() {
    Long roomId = gameRoom.getGameRoomId();
    Game game = gameService.startGame(gameRoom.getGameRoomId());
    User guessingTeamPlayer = game.getCurrentTeam().getPlayers().stream().findFirst().get();
    User nonGuessingTeamPlayer = game.getTeams().stream().filter(t -> !Objects.equals(game.getCurrentTeam(), t)).findFirst().get().getPlayers().stream().findFirst().get();

    Long u1id = u1.getUserId();
    Long u5id = u5.getUserId();

    assertThrows(GameNotFoundException.class, () -> gameService.isPlayerFromNonGuessingTeam(-1L, u1id));
    assertThrows(UserNotFoundException.class, () -> gameService.isPlayerFromNonGuessingTeam(roomId, -1L));
    assertThat(gameService.isPlayerFromNonGuessingTeam(roomId, u5id)).isFalse();
    assertThat(gameService.isPlayerFromNonGuessingTeam(roomId, guessingTeamPlayer.getUserId())).isFalse();
    assertThat(gameService.isPlayerFromNonGuessingTeam(roomId, nonGuessingTeamPlayer.getUserId())).isTrue();
  }

  @Test
  @DirtiesContext
  public void testHandleTimeFlipFacetChange(){
    Long roomId = gameRoom.getGameRoomId();

    TimeFlipFacetChangeEvent tffce = new TimeFlipFacetChangeEvent(this, timeFlip, facetKey);
    assertDoesNotThrow(() -> gameService.handleTimeFlipFacetChange(tffce));

    gameService.startGame(gameRoom.getGameRoomId());
    GameControlManager manager = gameService.getGameControlManager(roomId);

    assertThat(manager.getGameState()).isEqualTo(GameState.EXPECTING_PROPS);
    gameService.handleTimeFlipFacetChange(tffce);
    assertThat(manager.getGameState()).isEqualTo(GameState.EXPECTING_ROUND_END);
    gameService.handleTimeFlipFacetChange(tffce);
    assertThat(manager.getGameState()).isEqualTo(GameState.EXPECTING_RESULT);

    assertDoesNotThrow(() -> gameService.handleTimeFlipFacetChange(tffce));
    assertThat(manager.getGameState()).isEqualTo(GameState.EXPECTING_RESULT);
  }

  @Test
  @DirtiesContext
  @Transactional
  public void testComplete(){
    Long roomId = gameRoom.getGameRoomId();
    gameService.startGame(gameRoom.getGameRoomId());
    TimeFlipFacetChangeEvent tffce = new TimeFlipFacetChangeEvent(this, timeFlip, facetKey);

    LongStream.rangeClosed(0, 6).forEach((i) -> {
      gameService.handleTimeFlipFacetChange(tffce);
      gameService.handleTimeFlipFacetChange(tffce);
      gameService.setGameRoundResult(setGameResultRequest);

      if (i == 6){
        Optional<Game> optGame = gameRepository.findById(roomId);
        assertThat(optGame).isPresent();
        Game game = optGame.get();
        Hibernate.initialize(game.getGameRounds());
        assertThat(game.getWinner()).isPresent();
      } else {
        assertThat(gameRepository.findById(roomId)).isNotPresent();
        assertThat(gameService.getGame(roomId).getWinner()).isNotPresent();
        gameService.startNewGameRound(roomId);
      }
    });
  }
}

