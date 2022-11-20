package at.ac.uibk.timeguess.flipflapp.game;

import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomUser;
import at.ac.uibk.timeguess.flipflapp.game.round.GameRound;
import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.team.Color;
import at.ac.uibk.timeguess.flipflapp.team.Team;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlip;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.user.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.spencerwi.either.Either;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

@Entity
public class Game implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  private Long gameId;

  @OneToMany(mappedBy = "game", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private final Set<Team> teams = new HashSet<>();

  @OneToMany(mappedBy = "game", orphanRemoval = true, cascade = CascadeType.ALL)
  private final Set<GameRound> gameRounds = new HashSet<>();

  @NotNull(message = "NO_TOPIC_SPECIFIED")
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "topicId")
  private Topic topic;

  @NotNull(message = "MAX_POINTS_NOT_SPECIFIED")
  @Enumerated(EnumType.STRING)
  private MaxPoints maxPoints;

  @Transient
  @JsonInclude(Include.NON_NULL)
  private String name;

  @Transient
  @JsonInclude(Include.NON_NULL)
  private GameRound currentRound;

  @Transient
  @JsonInclude(Include.NON_NULL)
  private Team currentTeam;

  public Game() {
  }

  private Game(GameRoom gameRoom) {
    this.topic = gameRoom.getTopic();
    this.maxPoints = gameRoom.getMaxPoints();
    this.gameId = gameRoom.getGameRoomId();
    this.name = gameRoom.getName();
  }

  private static Set<Team> getTeamsFromGameRoom(GameRoom gameRoom, Game game) {
    final Set<Team> teams = new HashSet<>();
    List<Color> colors = new ArrayList<>(Arrays.asList(Color.values()));
    colors.add(null);

    colors.forEach(c -> {
      Set<User> players = gameRoom.getGameRoomUsers().stream()
          .filter(gu -> Objects.equals(gu.getTeamColor(), c))
          .map(GameRoomUser::getUser).collect(Collectors.toSet());

      if (!players.isEmpty()) {
        teams.add(Team.of(c, players, game));
      }
    });

    return teams;

  }

  private void setTeams(Set<Team> teams) {
    this.teams.addAll(teams);
  }

  /**
   * attempts to create a game given a GameRoom.
   *
   * @param gameRoom the gameRoom of which a game should be created
   * @return Either String or Game, the Game or an ErrorMessage
   */
  public static Either<String, Game> of(GameRoom gameRoom) {
    Game game = new Game(gameRoom);
    Set<Team> teams = getTeamsFromGameRoom(gameRoom, game);
    game.setTeams(teams);

    final List<String> errors = new ArrayList<>();
    Validator v = Validation.buildDefaultValidatorFactory().getValidator();

    teams.forEach(t -> {
      String teamErrors = v.validate(t).stream().map(ConstraintViolation::getMessage)
          .collect(Collectors.joining(","));

      if (!teamErrors.isEmpty()) {
        errors.add(teamErrors);
      }
    });

    String gameErrors = v.validate(game).stream().map(ConstraintViolation::getMessage)
        .collect(Collectors.joining(","));
    if (!gameErrors.isEmpty()) {
      errors.add(gameErrors);
    }

    if (game.topic != null
        && Optional.ofNullable(game.topic.getTerms()).orElse(Collections.emptySet()).size() < 30) {
      errors.add("NOT_ENOUGH_TERMS");
    }

    TimeFlip timeFlip = gameRoom.getTimeFlip();
    if (timeFlip == null) {
      errors.add("NO_ASSOCIATED_TIMEFLIP");
    } else if (timeFlip.getTimeFlipFacetMap() == null) {
      errors.add("TIMEFLIP_NOT_CONFIGURED");
    }

    if (gameRoom.getAvailableTeamsList().size() < 2) {
      errors.add("NOT_ENOUGH_TEAMS");
    }

    boolean notEnoughPlayersPerTeam = gameRoom.getAvailableTeamsList().stream().anyMatch(color -> {
      Optional<Team> team = teams.stream().filter(t -> color.equals(t.getColor())).findFirst();
      return team.isEmpty() || team.get().getPlayers().size() < gameRoom.getMinTeamSize();
    });
    if (notEnoughPlayersPerTeam) {
      errors.add("NOT_ENOUGH_PLAYERS_IN_TEAM");
    }

    return (errors.isEmpty()) ? Either.right(game)
        : Either.left(String.join(",", errors));
  }

  public Set<Team> getTeams() {
    return Collections.unmodifiableSet(this.teams);
  }

  public Set<GameRound> getGameRounds() {
    return Collections.unmodifiableSet(this.gameRounds);
  }

  public Topic getTopic() {
    return topic;
  }

  public void setCurrentRound(GameRound currentRound) {
    this.currentRound = currentRound;
  }

  public void addCurrentRound() {
    gameRounds.add(currentRound);
  }

  public GameRound getCurrentRound() {
    return currentRound;
  }

  public MaxPoints getMaxPoints() {
    return maxPoints;
  }

  public Long getGameId() {
    return gameId;
  }

  public Optional<Team> getWinner() {
    return teams.stream().filter(t -> t.getPoints() >= maxPoints.value).findFirst();
  }

  public void setCurrentTeam(Team team) {
    this.currentTeam = team;
  }

  public Team getCurrentTeam() {
    return this.currentTeam;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Game other = (Game) o;
    return Objects.equals(gameId, other.gameId) && Objects.equals(teams, other.teams)
        && Objects.equals(topic, other.topic)
        && Objects.equals(maxPoints, other.maxPoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, teams, topic, maxPoints);
  }

  @Override
  public String toString() {
    return "Game{" + "gameId=" + gameId + ", teams=" + teams + ", gameRounds=" + gameRounds
        + ", topic=" + topic
        + ", maxPoints=" + maxPoints + '}';
  }
}
