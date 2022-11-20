package at.ac.uibk.timeguess.flipflapp.websocket.exception;

public class MissingJwtException extends RuntimeException {

  public MissingJwtException() {
    super();
  }

  public MissingJwtException(String message) {
    super(message);
  }
}
