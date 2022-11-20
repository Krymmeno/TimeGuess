package at.ac.uibk.timeguess.flipflapp.websocket.exception;

public class MissingHeaderException extends RuntimeException {

  public MissingHeaderException(final String header) {
    super("The needed header %s was missing".formatted(header));
  }
}
