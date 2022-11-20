package at.ac.uibk.timeguess.flipflapp.timeflip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import at.ac.uibk.timeguess.flipflapp.game.GameService;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

public class TimeFlipServiceMockTest {

  private TimeFlipService timeFlipService;
  GameRoomService gameRoomService;
  private GameService gameService;

  @BeforeEach
  void setUp() {
    TimeFlipRepository timeFlipRepository = mock(TimeFlipRepository.class);
    gameRoomService = mock(GameRoomService.class);
    ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    gameService = mock(GameService.class);
    timeFlipService = spy(
        new TimeFlipService(timeFlipRepository, gameRoomService, applicationEventPublisher,
            gameService));
  }

  @Test
  void checkForInactiveTimeFlips() {
    TimeFlip timeFlip1 = new TimeFlip("a", "");
    timeFlip1.setStatus(TimeFlipStatus.ACTIVE);
    TimeFlip timeFlip2 = new TimeFlip("b", "");
    timeFlip2.setStatus(TimeFlipStatus.PENDING);
    TimeFlip timeFlip3 = new TimeFlip("c", "");
    timeFlip3.setStatus(TimeFlipStatus.INACTIVE);

    doReturn(List.of(timeFlip1, timeFlip2, timeFlip3)).when(timeFlipService).getAllTimeFlips();

    final Topic randomTopic = new Topic("Random", true);
    GameRoom gameRoom1 = new GameRoom(1L, 1L, "1", randomTopic, MaxPoints.TEN, 2);
    gameRoom1.setTimeFlip(timeFlip1);
    GameRoom gameRoom2 = new GameRoom(2L, 1L, "2", randomTopic, MaxPoints.TEN, 2);
    gameRoom2.setTimeFlip(timeFlip2);
    GameRoom gameRoom3 = new GameRoom(3L, 1L, "3", randomTopic, MaxPoints.TEN, 2);
    gameRoom3.setTimeFlip(timeFlip3);

    doReturn(List.of(gameRoom1, gameRoom2, gameRoom3)).when(gameRoomService).getAllGameRooms();
    doCallRealMethod().when(gameRoomService).getGameRoomsForTimeFlip(any());

    timeFlipService.checkForInactiveTimeFlips();

    assertThat(gameRoom1.getTimeFlip()).isEqualTo(timeFlip1);
    assertThat(gameRoom2.getTimeFlip()).isEqualTo(timeFlip2);
    assertThat(gameRoom3.getTimeFlip()).isNull();

    verify(gameRoomService, never()).sendGameRoomUpdate(gameRoom1);
    verify(gameRoomService, never()).sendGameRoomUpdate(gameRoom2);
    verify(gameRoomService).sendGameRoomUpdate(gameRoom3);

    verify(gameService, never()).abortGamesAssociatedWithTimeFlip(timeFlip1);
    verify(gameService, never()).abortGamesAssociatedWithTimeFlip(timeFlip2);
    verify(gameService).abortGamesAssociatedWithTimeFlip(timeFlip3);
  }
}
