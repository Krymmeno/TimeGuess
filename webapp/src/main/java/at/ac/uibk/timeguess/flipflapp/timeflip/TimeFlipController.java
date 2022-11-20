package at.ac.uibk.timeguess.flipflapp.timeflip;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Tag(name = "TimeFlip")
@ApiResponse(responseCode = "200")
@ApiResponse(responseCode = "401", description = "Malformed JWT", content = @Content)
@ApiResponse(responseCode = "403", description = "Deficient permissions", content = @Content)
@RestController
@RequestMapping("api/timeflip")
public class TimeFlipController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TimeFlipController.class);

  private final TimeFlipService timeFlipService;

  public TimeFlipController(TimeFlipService timeFlipService) {
    this.timeFlipService = timeFlipService;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(TimeFlipNotFoundException.class)
  public String handleNotFound(final Exception e) {
    return e.getMessage();
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(TimeFlipConfigurationException.class)
  public String handleMisConfiguration(final Exception e) {
    return e.getMessage();
  }


  @Operation(summary = "Inform the server about TimeFlip's status")
  @PreAuthorize("hasAuthority('TIMEFLIP')")
  @PostMapping("/update")
  public void update(@Validated @RequestBody final TimeFlipUpdate timeFlipUpdate) {
    LOGGER.info("Update from TimeFlip: {}", timeFlipUpdate);
    timeFlipService.recognize(timeFlipUpdate);
  }

  @Operation(summary = "Get all timeflips ever connected with their current status")
  @PreAuthorize("hasAnyAuthority('PLAYER','GAMEMANAGER','ADMIN')")
  @GetMapping
  public List<TimeFlip> getAllTimeFlips() {
    return timeFlipService.getAllTimeFlips();
  }

  @Operation(summary = "Get all currently available timeflips")
  @PreAuthorize("hasAnyAuthority('PLAYER','GAMEMANAGER','ADMIN')")
  @GetMapping("/available")
  public List<TimeFlip> getAvailableTimeFlips() {
    return timeFlipService.getAvailableTimeFlips();
  }

  @Operation(summary = "Calibrate the facets of a TimeFlip")
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER','ADMIN')")
  @PostMapping("/{timeFlipId}")
  public TimeFlip calibrateTimeFlip(@PathVariable final Long timeFlipId,
      @Validated @RequestBody final Map<Byte, TimeFlipFacet> timeFlipFacetMap) {
    return timeFlipService.calibrateTimeFlip(timeFlipId, timeFlipFacetMap);
  }
}
