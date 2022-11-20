package at.ac.uibk.timeguess.flipflapp.game.room;

import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.security.TimeGuessPrincipal;
import at.ac.uibk.timeguess.flipflapp.timeflip.TimeFlipNotFoundException;
import at.ac.uibk.timeguess.flipflapp.topic.TopicNotFoundException;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GameRooms")
@ApiResponse(responseCode = "200")
@ApiResponse(responseCode = "401", description = "Expired or malformed JWT", content = @Content)
@ApiResponse(responseCode = "403", description = "Deficient permissions", content = @Content)
@RestController
@RequestMapping("api/gamerooms")
public class GameRoomController {

  private final GameRoomService gameRoomService;

  public GameRoomController(final GameRoomService gameRoomService) {
    this.gameRoomService = gameRoomService;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({UserNotFoundException.class, TopicNotFoundException.class,
      GameRoomNotFoundException.class,
      TimeFlipNotFoundException.class})
  public String handleNotFound(final Exception e) {
    return e.getMessage();
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(IllegalStateException.class)
  public String handleIllegalState(final Exception e) {
    return e.getMessage();
  }

  @Operation(summary = "Create a new game room", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "User/Topic was not found", content = @Content)
  @PostMapping
  public GameRoom createGameRoom(
      @RequestBody @Validated final CreateGameRoomRequest createGameRoomRequest,
      @AuthenticationPrincipal final TimeGuessPrincipal timeGuessPrincipal) {
    return gameRoomService
        .createGameRoom(createGameRoomRequest, timeGuessPrincipal.getUser().getUserId());
  }

  @Operation(summary = "Get a game room by id", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room was not found", content = @Content)
  @PreAuthorize("""
      hasAnyAuthority('ADMIN', 'GAMEMANAGER')
      or @gameRoomService.getGameRoomUsers(#gameRoomId).contains(principal.user.userId)
      or @gameRoomService.getGameRoomIfExists(#gameRoomId).invitedUsers.contains(principal.user.userId)
      """)
  @GetMapping("/{gameRoomId}")
  public GameRoom getGameRoom(@PathVariable final Long gameRoomId) {
    return gameRoomService.getGameRoomIfExists(gameRoomId);
  }

  @Operation(summary = "Get all game rooms", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room was not found", content = @Content)
  @PreAuthorize("hasAnyAuthority('ADMIN', 'GAMEMANAGER')")
  @GetMapping
  public List<GameRoom> getAllGameRooms() {
    return gameRoomService.getAllGameRooms();
  }

  @Operation(summary = "Set max points for game room", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room was not found", content = @Content)
  @PreAuthorize("""
      hasAuthority('ADMIN')
      or @gameRoomService.getGameRoomIfExists(#gameRoomId).gameHostId == principal.user.userId
      """)
  @PatchMapping("/{gameRoomId}/maxPoints")
  public void setMaxPoints(@PathVariable final Long gameRoomId,
      @NotNull @RequestParam final MaxPoints maxPoints) {
    gameRoomService.setMaxPoints(gameRoomId, maxPoints);
  }

  @Operation(summary = "Set topic for game room", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room was not found", content = @Content)
  @PreAuthorize("""
      hasAuthority('ADMIN')
      or @gameRoomService.getGameRoomIfExists(#gameRoomId).gameHostId == principal.user.userId
      """)
  @PatchMapping("/{gameRoomId}/topic")
  public void setTopic(@PathVariable final Long gameRoomId,
      @NotNull @RequestParam final Long topicId) {
    gameRoomService.setTopic(gameRoomId, topicId);
  }

  @Operation(summary = "Set ready status for a player in the game room", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room/user was not found", content = @Content)
  @ApiResponse(responseCode = "409", description = "User tried to set ready without a team", content = @Content)
  @PreAuthorize("""
      principal.user.userId == #userId
      """)
  @PatchMapping("/{gameRoomId}/users/{userId}/ready")
  public void setReady(@PathVariable final Long userId, @PathVariable final Long gameRoomId,
      @NotNull @RequestParam final Boolean isReady) {
    gameRoomService.setReady(gameRoomId, userId, isReady);
  }

  @Operation(summary = "Remove a player from the game room", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room/user was not found", content = @Content)
  @PreAuthorize("""
      @gameRoomService.getGameRoomIfExists(#gameRoomId).gameHostId == principal.user.userId
      or principal.user.userId == #userId
      """)
  @DeleteMapping("/{gameRoomId}/users/{userId}")
  public void removePlayer(@PathVariable final Long gameRoomId, @PathVariable final Long userId) {
    gameRoomService.removePlayer(gameRoomId, userId);
  }

  @Operation(summary = "Set timeflip for game room", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room or Timeflip was not found", content = @Content)
  @PreAuthorize("""
      hasAuthority('ADMIN')
      or @gameRoomService.getGameRoomIfExists(#gameRoomId).gameHostId == principal.user.userId
      """)
  @PatchMapping("/{gameRoomId}/timeflip/{timeflipId}")
  public void setTimeFlip(@PathVariable final Long gameRoomId,
      @PathVariable final Long timeflipId) {
    gameRoomService.setTimeFlip(gameRoomId, timeflipId);
  }
}
