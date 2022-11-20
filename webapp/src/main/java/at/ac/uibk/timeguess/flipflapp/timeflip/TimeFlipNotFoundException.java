package at.ac.uibk.timeguess.flipflapp.timeflip;

public class TimeFlipNotFoundException extends RuntimeException {

  public TimeFlipNotFoundException(final Long timeFlipId) {
    super("TimeFlip with id %d not found".formatted(timeFlipId));
  }
}
