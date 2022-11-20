package at.ac.uibk.timeguess.flipflapp.user.exception;

public class UserInactiveException extends RuntimeException {

  public UserInactiveException(final String username) {
    super("The User with username %s is currently not active".formatted(username));
  }
}
