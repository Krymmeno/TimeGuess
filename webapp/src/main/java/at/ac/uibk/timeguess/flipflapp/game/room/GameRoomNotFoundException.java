package at.ac.uibk.timeguess.flipflapp.game.room;

public class GameRoomNotFoundException extends RuntimeException {

  public GameRoomNotFoundException(final Long gameRoomId) {
    super("The GameRoom with id %d was not found.".formatted(gameRoomId));
  }
}
