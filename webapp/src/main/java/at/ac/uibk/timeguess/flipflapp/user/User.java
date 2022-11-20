package at.ac.uibk.timeguess.flipflapp.user;

import at.ac.uibk.timeguess.flipflapp.game.round.GameRound;
import at.ac.uibk.timeguess.flipflapp.team.Team;
import at.ac.uibk.timeguess.flipflapp.validation.OnCreate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
public class User implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  @Schema(description = "Mandatory on create. Cannot be updated")
  @NotBlank
  @Column(unique = true)
  private String username;

  @Schema(description = "Mandatory on create. Cannot be updated")
  @NotNull
  @Enumerated(EnumType.STRING)
  private UserRole role;

  private String firstName;

  private String lastName;

  @NotBlank(groups = OnCreate.class)
  @JsonProperty(access = Access.WRITE_ONLY)
  private String password;

  @NotNull
  private Boolean active;

  @JsonIgnore
  @ManyToMany(mappedBy = "players")
  private Set<Team> teams;

  @JsonIgnore
  @OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.EAGER,
      cascade = CascadeType.ALL)
  private Set<GameRound> gameRounds;

  public User() {
  }

  public User(final String username, final String firstName, final String lastName,
      final String password, final UserRole role, final Boolean active) {
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
    this.active = active;
    this.role = role;
  }

  public Long getUserId() {
    return this.userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(final Boolean active) {
    this.active = active;
  }

  public UserRole getRole() {
    return this.role;
  }

  public void setRole(final UserRole role) {
    this.role = role;
  }

  public Set<GameRound> getGameRounds() {
    return this.gameRounds;
  }

  public Set<Team> getTeams() {
    return this.teams;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof User)) {
      return false;
    }
    final User other = (User) o;

    return Objects.equals(userId, other.userId) && Objects.equals(username, other.username)
        && Objects.equals(role, other.role) && Objects.equals(firstName, other.firstName)
        && Objects.equals(lastName, other.lastName) && Objects.equals(active, other.active);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, username, role, firstName, lastName, active);
  }

  @Override
  public String toString() {
    return "User{" + "username='" + username + '\'' + ", role=" + role + ", firstName='" + firstName
        + '\'' + ", lastName='" + lastName + '\'' + '}';
  }
}
