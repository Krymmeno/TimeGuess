package at.ac.uibk.timeguess.flipflapp.topic;

public class TopicNotFoundException extends RuntimeException {

  public TopicNotFoundException(Long topicId) {
    super("The topic with id %d was not found.".formatted(topicId));
  }
}
