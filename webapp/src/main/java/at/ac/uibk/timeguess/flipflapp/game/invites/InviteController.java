package at.ac.uibk.timeguess.flipflapp.game.invites;

import at.ac.uibk.timeguess.flipflapp.game.room.GameRoom;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomNotFoundException;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.security.TimeGuessPrincipal;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Invites")
@ApiResponse(responseCode = "200")
@ApiResponse(responseCode = "401", description = "Expired or malformed JWT", content = @Content)
@ApiResponse(responseCode = "403", description = "Deficient permissions", content = @Content)
@RestController
@RequestMapping("api/invites")
public class InviteController {

  private final GameRoomService gameRoomService;

  public InviteController(GameRoomService gameRoomService) {
    this.gameRoomService = gameRoomService;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({UserNotFoundException.class, GameRoomNotFoundException.class,
      InviteNotFoundException.class})
  public String handleNotFound(final Exception e) {
    return e.getMessage();
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(IllegalArgumentException.class)
  public String handleIllegalState(final Exception e) {
    return e.getMessage();
  }

  @Operation(summary = "Create a new invite", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "User/GameRoom not found", content = @Content)
  @ApiResponse(responseCode = "403", description = "Game already started or the Invite-Creator was not the GameHost", content = @Content)
  @PostMapping
  public GameRoom createInvite(@RequestBody @Validated final CreateInviteRequest inviteRequest,
      @AuthenticationPrincipal final TimeGuessPrincipal timeGuessPrincipal) {

    Long requestUserId = timeGuessPrincipal.getUser().getUserId();
    return gameRoomService.addInvite(inviteRequest, requestUserId);
  }

  @Operation(summary = "Get all invites of user", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
  @PreAuthorize("""
      hasAuthority('ADMIN')
      or #userId== principal.user.userId
      """)
  @GetMapping("/{userId}")
  public List<GameRoom> getInvites(@PathVariable final Long userId) {
    return gameRoomService.getAllInvites(userId);
  }


  @Operation(summary = "Accept an Invite", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Invite / User / GameRoom not found", content = @Content)
  @ApiResponse(responseCode = "403", description = "GameRoom already started a game", content = @Content)
  @PostMapping("/{gameRoomId}/accept")
  public GameRoom acceptInvite(@PathVariable final Long gameRoomId,
      @AuthenticationPrincipal final TimeGuessPrincipal timeGuessPrincipal) {
    Long requestUserId = timeGuessPrincipal.getUser().getUserId();
    return gameRoomService.acceptInvite(gameRoomId, requestUserId);
  }
}
