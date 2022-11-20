package at.ac.uibk.timeguess.flipflapp.game.round;

/**
 * enum representing the time of game round
 */
public enum Time {
  ONE(60000L), TWO(120000L), THREE(180000L);

  private final Long millis;

  Time(Long millis) {
    this.millis = millis;
  }

  public Long getMillis() {
    return this.millis;
  }
}
