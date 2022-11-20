package at.ac.uibk.timeguess.flipflapp.term;

import at.ac.uibk.timeguess.flipflapp.game.round.GameRound;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * entity representing a game term can be guessed.
 */
@Entity
public class Term implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long termId;

  @NotBlank
  private String name;

  @NotNull
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "topicId")
  @JsonIgnore
  private Topic topic;

  @OneToMany(mappedBy = "term", orphanRemoval = true, fetch = FetchType.EAGER,
      cascade = CascadeType.ALL)
  @JsonIgnore
  private Set<GameRound> gameRounds;

  @JsonIgnore
  @NotNull
  private Boolean active;

  public Term() {
  }

  public Term(final String name, final Topic topic, final boolean active) {
    this.name = name;
    this.topic = topic;
    this.active = active;
  }

  public Long getTermId() {
    return termId;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Topic getTopic() {
    return this.topic;
  }

  public void setTopic(final Topic topic) {
    this.topic = topic;
  }

  public Set<GameRound> getGameRounds() {
    return this.gameRounds;
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
    if (!(o instanceof Term)) {
      return false;
    }
    final Term other = (Term) o;
    return Objects.equals(termId, other.termId) && Objects.equals(name, other.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(termId, name);
  }

  @Override
  public String toString() {
    return "Term{" + "termId=" + termId + ", name='" + name + '\'' + ", topic=" + topic + '}';
  }
}
