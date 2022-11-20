package at.ac.uibk.timeguess.flipflapp.game;

import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomNotFoundException;
import at.ac.uibk.timeguess.flipflapp.game.room.InvalidGameRoomException;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Games")
@ApiResponse(responseCode = "200")
@ApiResponse(responseCode = "401", description = "Expired or malformed JWT", content = @Content)
@ApiResponse(responseCode = "403", description = "Deficient permissions", content = @Content)
@RestController
@RequestMapping("api/games")
public class GameController {

  private final GameService gameService;

  public GameController(GameService gameService) {
    this.gameService = gameService;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({UserNotFoundException.class, GameRoomNotFoundException.class,
      GameNotFoundException.class})
  public String handleNotFound(final Exception e) {
    return e.getMessage();
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(IllegalArgumentException.class)
  public String handleIllegalState(final Exception e) {
    return e.getMessage();
  }

  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  @ExceptionHandler({InvalidGameActionException.class, InvalidGameRoomException.class})
  public String handleIllegalAction(final Exception e) {
    return e.getMessage();
  }

  @Operation(summary = "Returns the game with the given id",
      security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "game not found", content = @Content)
  @GetMapping("/{gameId}")
  @PreAuthorize("""
      @gameService.isPlayer(#gameId, principal.user.userId) or hasAnyAuthority('ADMIN', 'GAMEMANAGER')
      """)
  public Game getGame(@PathVariable final Long gameId) {
    return gameService.getGame(gameId);
  }

  @Operation(summary = "Set the result of current round from the given game",
      security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game not found", content = @Content)
  @ApiResponse(responseCode = "405", description = "Cannot set the result of the current round",
      content = @Content)
  @PostMapping("/setGameRoundResult")
  @PreAuthorize("""
      @gameService.isPlayerFromNonGuessingTeam(#request.getGameId(), principal.user.userId)
      """)
  public Game setGameRoundResult(@RequestBody @Validated final SetGameResultRequest request) {
    return gameService.setGameRoundResult(request);
  }

  @Operation(summary = "start the game with the given id",
      security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game not found", content = @Content)
  @ApiResponse(responseCode = "405", description = "Invalid GameRoom", content = @Content)
  @PostMapping("/{gameRoomId}/startGame")
  @PreAuthorize("""
      @gameRoomService.getGameRoomIfExists(#gameRoomId).gameHostId == principal.user.userId
      """)
  public Game startGame(@PathVariable final Long gameRoomId) {
    return gameService.startGame(gameRoomId);
  }

  @Operation(summary = "start the next Game Round", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game not found", content = @Content)
  @ApiResponse(responseCode = "405", description = "Cannot start a new round of the game",
      content = @Content)
  @PostMapping("/{gameId}/startNextRound")
  @PreAuthorize("""
      @gameService.isPlayer(#gameId, principal.user.userId)
      """)
  public Game startNextRound(@PathVariable final Long gameId) {
    return gameService.startNewGameRound(gameId);
  }

  @Operation(summary = "Abort a game with the given id", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game not found", content = @Content)
  @PostMapping("/{gameId}/abort")
  @PreAuthorize("hasAuthority('ADMIN')")
  public void abortGame(@PathVariable final Long gameId) {
    gameService.abortGame(gameId);
  }
}
