package at.ac.uibk.timeguess.flipflapp.game.invites;

public class InviteNotFoundException extends RuntimeException {

  public InviteNotFoundException(final Long userId, final Long gameRoomId) {
    super("The Invite for gameRoom: %d, user: %d was not found.".formatted(userId, gameRoomId));
  }
}
