package at.ac.uibk.timeguess.flipflapp.topic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Topics")
@ApiResponse(responseCode = "200")
@ApiResponse(responseCode = "401", description = "Expired or malformed JWT", content = @Content)
@ApiResponse(responseCode = "403", description = "Deficient permissions", content = @Content)
@RestController
@RequestMapping("api/topics")
public class TopicController {


  private final TopicFacade topicFacade;

  public TopicController(TopicFacade topicFacade) {
    this.topicFacade = topicFacade;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({TopicNotFoundException.class})
  public String handleTopicNotFound(final Exception e) {
    return e.getMessage();
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler({TopicDuplicateException.class})
  public String handleDuplicateTopicName(final Exception e) {
    return e.getMessage();
  }

  @Operation(summary = "Create Topic", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "409", description = "Topic already exists", content = @Content)
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER','ADMIN')")
  @PostMapping
  public TopicDto addTopic(@RequestParam String topicName) {
    return topicFacade.addTopic(topicName);
  }

  @Operation(summary = "Update topic name", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content)
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER','ADMIN')")
  @PutMapping("/{topicId}")
  public TopicDto updateTopicName(@PathVariable final Long topicId,
      @RequestParam final String topicName) {
    return topicFacade.updateTopicName(topicId, topicName);
  }

  @Operation(summary = "Deactivate topic", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content)
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER','ADMIN')")
  @DeleteMapping("/{topicId}")
  public TopicDto deactivateTopic(@PathVariable final Long topicId) {
    return topicFacade.deactivate(topicId);
  }

  @Operation(summary = "Get all active topics", security = @SecurityRequirement(name = "JWT"))
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER', 'ADMIN')")
  @GetMapping
  public List<TopicDto> getAllActiveTopics() {
    return topicFacade.getAllActiveTopics();
  }

  @Operation(summary = "Get all available topics", security = @SecurityRequirement(name = "JWT"))
  @GetMapping("/available")
  public List<TopicDto> getAllAvailableTopics() {
    return topicFacade.getAllAvailableTopics();
  }

  @Operation(summary = "Import Terms for a Topic", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content)
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER','ADMIN')")
  @PostMapping(value = "/{topicId}/terms")
  @Transactional
  public TopicDto importTerms(@PathVariable final Long topicId, @RequestBody List<String> terms) {
    return topicFacade.importTerms(topicId, terms);
  }
}
