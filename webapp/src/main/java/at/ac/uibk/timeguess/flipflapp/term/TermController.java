package at.ac.uibk.timeguess.flipflapp.term;

import at.ac.uibk.timeguess.flipflapp.topic.TopicNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Terms")
@ApiResponse(responseCode = "200")
@ApiResponse(responseCode = "401", description = "Expired or malformed JWT", content = @Content)
@ApiResponse(responseCode = "403", description = "Deficient permissions", content = @Content)
@RestController
@RequestMapping("api/terms")
public class TermController {

  private final TermService termService;

  public TermController(TermService termService) {
    this.termService = termService;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({TermNotFoundException.class, TopicNotFoundException.class})
  public String handleNotFound(final Exception e) {
    return e.getMessage();
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler({TermDuplicateException.class})
  public String handleDuplicate(final Exception e) {
    return e.getMessage();
  }

  @Operation(summary = "Deactivate term", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Term not found", content = @Content)
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER','ADMIN')")
  @DeleteMapping("/{termId}")
  public Term deactivateTerm(@PathVariable final Long termId) {
    return termService.deactivateTerm(termId);
  }

  @Operation(summary = "Update term name", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Term not found", content = @Content)
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER','ADMIN')")
  @PutMapping("/{termId}")
  public Term updateTermName(@PathVariable final Long termId, @RequestParam final String termName) {
    return termService.updateTermName(termId, termName);
  }

  @Operation(summary = "Add new term", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Topic does not exist", content = @Content)
  @ApiResponse(responseCode = "409", description = "Term already exists in specified topic", content = @Content)
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER','ADMIN')")
  @PostMapping
  public Term addTerm(@Validated @RequestBody final CreateTermRequest term) {
    return termService.addTerm(term);
  }
}
