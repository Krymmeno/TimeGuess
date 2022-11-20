package at.ac.uibk.timeguess.flipflapp.game;

public class InvalidGameActionException extends RuntimeException {

  public InvalidGameActionException(String errorMsg) {
    super(errorMsg);
  }
}
