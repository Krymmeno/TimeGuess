package at.ac.uibk.timeguess.flipflapp.topic;

import at.ac.uibk.timeguess.flipflapp.game.Game;
import at.ac.uibk.timeguess.flipflapp.term.Term;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * entity representing a specific Topic which can hold multiple terms
 */
@Entity
public class Topic implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long topicId;

  @NotBlank
  @Column(unique = true)
  private String name;

  @OneToMany(mappedBy = "topic", orphanRemoval = true, fetch = FetchType.EAGER,
      cascade = CascadeType.ALL)
  private Set<Term> terms;

  @JsonIgnore
  @OneToMany(mappedBy = "topic", orphanRemoval = true, fetch = FetchType.EAGER,
      cascade = CascadeType.ALL)
  private Set<Game> games;

  @NotNull
  private Boolean active;

  public Topic() {
  }

  public Topic(final String name, Boolean active) {
    this.name = name;
    this.active = active;
  }

  public Long getTopicId() {
    return this.topicId;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Set<Term> getTerms() {
    return this.terms;
  }

  public void setTerms(Set<Term> terms) {
    this.terms = terms;
  }

  public Set<Game> getGames() {
    return this.games;
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Topic)) {
      return false;
    }
    final Topic other = (Topic) o;

    return Objects.equals(topicId, other.topicId) && Objects.equals(name, other.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(topicId, name);
  }

  @Override
  public String toString() {
    return "Topic{" + "topicId=" + topicId + ", name='" + name + '\'' + '}';
  }
}
