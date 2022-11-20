package at.ac.uibk.timeguess.flipflapp.game;

public class GameNotFoundException extends RuntimeException {

  public GameNotFoundException(final Long gameRoomId) {
    super((gameRoomId == null) ? "Cannot get Game with id NULL" : "The Game with id %d could not be found".formatted(gameRoomId));
  }
}
