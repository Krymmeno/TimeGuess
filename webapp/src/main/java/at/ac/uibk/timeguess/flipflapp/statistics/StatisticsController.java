package at.ac.uibk.timeguess.flipflapp.statistics;

import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Statistics")
@ApiResponse(responseCode = "200")
@ApiResponse(responseCode = "401", description = "Expired or malformed JWT", content = @Content)
@ApiResponse(responseCode = "403", description = "Deficient permissions", content = @Content)
@RestController
@RequestMapping("api/statistics")
public class StatisticsController {

  private final StatisticsFacade statisticsFacade;

  public StatisticsController(
      final StatisticsFacade statisticsFacade) {
    this.statisticsFacade = statisticsFacade;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(UserNotFoundException.class)
  public String handleNotFound(final Exception e) {
    return e.getMessage();
  }

  @Operation(summary = "Get all user related statistics", security = @SecurityRequirement(name = "JWT"))
  @GetMapping("/users/{userId}")
  public UserStatistics getUserStatistics(@PathVariable final Long userId) {
    return statisticsFacade.getUserStatistics(userId);
  }

  @Operation(summary = "Get all game related statistics", security = @SecurityRequirement(name = "JWT"))
  @GetMapping("/games")
  public GameStatistics getGameStatistics() {
    return statisticsFacade.getGameStatistics();
  }

}
