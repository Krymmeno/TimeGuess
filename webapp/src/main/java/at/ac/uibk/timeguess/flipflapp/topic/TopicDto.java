package at.ac.uibk.timeguess.flipflapp.topic;


import at.ac.uibk.timeguess.flipflapp.term.Term;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;

public class TopicDto {

  private Long topicId;

  private String name;

  @JsonProperty("terms")
  private Set<Term> activeTerms;

  public TopicDto(Long id, String name,
      Set<Term> activeTerms) {
    this.topicId = id;
    this.name = name;
    this.activeTerms = activeTerms;
  }

  public Long getTopicId() {
    return topicId;
  }

  public void setTopicId(Long topicId) {
    this.topicId = topicId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<Term> getActiveTerms() {
    return activeTerms;
  }

  public void setActiveTerms(Set<Term> activeTerms) {
    this.activeTerms = activeTerms;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TopicDto topicDto = (TopicDto) o;
    return topicId.equals(topicDto.topicId) && name.equals(topicDto.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(topicId, name);
  }
}
