package at.ac.uibk.timeguess.flipflapp.timeflip;

import at.ac.uibk.timeguess.flipflapp.game.round.Activity;
import at.ac.uibk.timeguess.flipflapp.game.round.RoundPoints;
import at.ac.uibk.timeguess.flipflapp.game.round.Time;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class TimeFlipFacet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Long id;

  @NotNull
  private Activity activity;

  @NotNull
  private RoundPoints roundPoints;

  @NotNull
  private Time time;

  public TimeFlipFacet() {
  }

  public TimeFlipFacet(final Activity activity,
      final RoundPoints roundPoints, final Time time) {
    this.activity = activity;
    this.roundPoints = roundPoints;
    this.time = time;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Activity getActivity() {
    return activity;
  }

  public void setActivity(final Activity activity) {
    this.activity = activity;
  }

  public RoundPoints getRoundPoints() {
    return roundPoints;
  }

  public void setRoundPoints(final RoundPoints roundPoints) {
    this.roundPoints = roundPoints;
  }

  public Time getTime() {
    return time;
  }

  public void setTime(final Time time) {
    this.time = time;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TimeFlipFacet that = (TimeFlipFacet) o;
    return id.equals(that.id) && activity == that.activity && roundPoints == that.roundPoints
        && time == that.time;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, activity, roundPoints, time);
  }

  @Override
  public String toString() {
    return "TimeFlipFacet{" +
        "id=" + id +
        ", activity=" + activity +
        ", roundPoints=" + roundPoints +
        ", time=" + time +
        '}';
  }
}
