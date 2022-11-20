package at.ac.uibk.timeguess.flipflapp.team;

import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomNotFoundException;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Teams")
@ApiResponse(responseCode = "200")
@ApiResponse(responseCode = "401", description = "Expired or malformed JWT", content = @Content)
@ApiResponse(responseCode = "403", description = "Deficient permissions", content = @Content)
@RestController
@RequestMapping("api/gamerooms/{gameRoomId}/teams")
public class TeamController {

  private final TeamService teamService;

  public TeamController(final TeamService teamService) {
    this.teamService = teamService;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({UserNotFoundException.class, GameRoomNotFoundException.class,
      TeamNotFoundException.class})
  public String handleNotFound(final Exception e) {
    return e.getMessage();
  }


  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(TooManyTeamsException.class)
  public String handleTooManyTeams(final Exception e) {
    return e.getMessage();
  }


  @Operation(summary = "Create a new team", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room was not found", content = @Content)
  @PreAuthorize("@gameRoomService.getGameRoomIfExists(#gameRoomId).gameHostId == principal.user.userId")
  @PostMapping
  public void createTeam(@PathVariable final Long gameRoomId) {
    teamService.createTeam(gameRoomId);
  }

  @Operation(summary = "Join a team", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room/team was not found", content = @Content)
  @PreAuthorize("principal.user.userId == #userId")
  @PutMapping("/users/{userId}")
  public void joinTeam(@PathVariable final Long gameRoomId, @PathVariable final Long userId,
      @NotNull @RequestParam final Color teamColor) {
    teamService.joinTeam(gameRoomId, userId, teamColor);
  }

  @Operation(summary = "Leave current team", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room was not found", content = @Content)
  @PreAuthorize("principal.user.userId == #userId")
  @DeleteMapping("/users/{userId}")
  public void leaveTeam(@PathVariable final Long gameRoomId, @PathVariable final Long userId) {
    teamService.leaveTeam(gameRoomId, userId);
  }

  @Operation(summary = "Delete a team", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Game room/team was not found", content = @Content)
  @PreAuthorize("@gameRoomService.getGameRoomIfExists(#gameRoomId).gameHostId == principal.user.userId")
  @DeleteMapping
  public void deleteTeam(@PathVariable final Long gameRoomId,
      @NotNull @RequestParam final Color teamColor) {
    teamService.deleteTeam(gameRoomId, teamColor);
  }

}
