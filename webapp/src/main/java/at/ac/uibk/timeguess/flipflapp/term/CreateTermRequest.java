package at.ac.uibk.timeguess.flipflapp.term;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@SuppressFBWarnings(value = "EQ_UNUSUAL")
public record CreateTermRequest(@NotBlank String name, @NotNull Long topicId) {

  public String getName() {
    return name;
  }

  public Long getTopicId() {
    return topicId;
  }
}