package at.ac.uibk.timeguess.flipflapp.game.round;

/**
 * enum representing the maximum points needed to win a game
 */
public enum MaxPoints {
  TEN(10), TWENTY(20), THIRTY(30), FORTY(40), FIFTY(50), SIXTY(60), SEVENTY(70);

  public final int value;

  MaxPoints(int value) {
    this.value = value;
  }

  public int getPoints() {
    return this.value;
  }
}
