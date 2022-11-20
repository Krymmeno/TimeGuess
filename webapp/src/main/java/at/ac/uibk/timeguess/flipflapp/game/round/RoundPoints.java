package at.ac.uibk.timeguess.flipflapp.game.round;

/**
 * enum representing the points for a single gameRound
 */
public enum RoundPoints {
  ONE(1), TWO(2), THREE(3);

  public final int value;

  RoundPoints(int value) {
    this.value = value;
  }

  public int getPoints() {
    return this.value;
  }
}
