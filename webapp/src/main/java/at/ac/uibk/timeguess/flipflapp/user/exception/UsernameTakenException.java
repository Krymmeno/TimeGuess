package at.ac.uibk.timeguess.flipflapp.user.exception;

import at.ac.uibk.timeguess.flipflapp.user.User;

public class UsernameTakenException extends RuntimeException {

  public UsernameTakenException(final User user) {
    super("Username: %s already taken".formatted(user.getUsername()));
  }
}
