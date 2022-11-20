package at.ac.uibk.timeguess.flipflapp.team;

import at.ac.uibk.timeguess.flipflapp.game.Game;
import at.ac.uibk.timeguess.flipflapp.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * entity representing one team of a game.
 */
@Entity
public class Team implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long teamId;

  @NotNull(message = "PLAYER_NOT_PART_OF_A_TEAM")
  @Enumerated(EnumType.STRING)
  private Color color;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gameId")
  @JsonIgnore
  public Game game;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "team_user", joinColumns = @JoinColumn(name = "team_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private final Set<User> players = new HashSet<>();

  public Team() {
  }

  private Team(final Color color, Set<User> players, Game game) {
    this.color = color;
    this.game = game;
    Optional.ofNullable(players).ifPresent(this.players::addAll);
  }

  public static Team of(final Color color, final Set<User> players, Game game) {
    return new Team(color, players, game);
  }

  public Color getColor() {
    return this.color;
  }

  public Set<User> getPlayers() {
    return Collections.unmodifiableSet(this.players);
  }

  public Integer getPoints() {
    return Optional.ofNullable(game)
        .map(g -> g.getGameRounds().stream().filter(gr -> this.getPlayers().contains(gr.getUser()))
            .mapToInt(gr -> switch (gr.getResult()) {
              case WIN -> gr.getRoundPoints().value;
              case RULE_VIOLATION -> -1;
              case TIMEOUT -> 0;
            }).reduce(0, Integer::sum)).orElse(0);
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Team)) {
      return false;
    }
    final Team other = (Team) o;

    return Objects.equals(teamId, other.teamId) && Objects.equals(color, other.color)
        && Objects.equals(players, other.players);
  }

  @Override
  public int hashCode() {
    return Objects.hash(teamId, color, players);
  }

  @Override
  public String toString() {
    return "Team: " + this.color + " "
        + getPlayers().stream().map(User::toString).collect(Collectors.joining(" - ", "{ ", " }"));
  }
}
