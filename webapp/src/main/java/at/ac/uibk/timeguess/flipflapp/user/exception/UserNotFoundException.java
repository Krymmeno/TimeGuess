package at.ac.uibk.timeguess.flipflapp.user.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(final Long userId) {
    super("User with id %d not found".formatted(userId));
  }
}
