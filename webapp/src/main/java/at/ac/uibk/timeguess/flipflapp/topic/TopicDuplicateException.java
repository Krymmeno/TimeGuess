package at.ac.uibk.timeguess.flipflapp.topic;

public class TopicDuplicateException extends RuntimeException {

  public TopicDuplicateException(String message) {
    super("Topic with name: %s already exists".formatted(message));
  }
}
