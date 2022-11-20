package at.ac.uibk.timeguess.flipflapp.user;

import at.ac.uibk.timeguess.flipflapp.security.TimeGuessPrincipal;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import at.ac.uibk.timeguess.flipflapp.user.exception.UsernameTakenException;
import at.ac.uibk.timeguess.flipflapp.validation.OnCreate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users")
@ApiResponse(responseCode = "200")
@ApiResponse(responseCode = "401", description = "Expired or malformed JWT", content = @Content)
@ApiResponse(responseCode = "403", description = "Deficient permissions", content = @Content)
@RestController
@RequestMapping("api/users")
public class UserController {

  private final UserService userService;

  public UserController(final UserService userService) {
    this.userService = userService;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(UserNotFoundException.class)
  public String handleNotFound(final Exception e) {
    return e.getMessage();
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler({UsernameTakenException.class})
  public String handleConflict(final Exception e) {
    return e.getMessage();
  }

  @Operation(summary = "Get all users", security = @SecurityRequirement(name = "JWT"))
  @PreAuthorize("hasAnyAuthority('GAMEMANAGER','ADMIN')")
  @GetMapping
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  @Operation(summary = "Get all active users", security = @SecurityRequirement(name = "JWT"))
  @GetMapping("/active")
  public List<User> getAllActiveUsers() {
    return userService.getAllUsers().stream().filter(User::getActive).toList();
  }

  @Operation(summary = "Create a user ", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "409", description = "Username already taken", content = @Content)
  @PreAuthorize("""
      hasAuthority('ADMIN') or
        (hasAuthority('GAMEMANAGER') and #user.role == T(at.ac.uibk.timeguess.flipflapp.user.UserRole).PLAYER)
        """)
  @PostMapping
  public User createUser(@Validated({Default.class, OnCreate.class}) @RequestBody final User user) {
    return userService.createUser(user);
  }

  @Operation(summary = "Update a user", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
  @PreAuthorize("""
      (hasAuthority('ADMIN') and ((@userService.wantsToUpdateRole(#id, #updatedUser) and #id != principal.user.userId) or (!@userService.wantsToUpdateRole(#id, #updatedUser))))
      or (hasAuthority('GAMEMANAGER') and !@userService.wantsToUpdateRole(#id, #updatedUser)
      and @userService.getUserById(#id).get().role == T(at.ac.uibk.timeguess.flipflapp.user.UserRole).PLAYER)
      or (principal.user.userId == #id and !@userService.wantsToUpdateRole(#id, #updatedUser))
      """)
  @PutMapping("/{id}")
  public User updateUser(@PathVariable final Long id, @Validated @RequestBody User updatedUser) {
    return userService.updateUser(id, updatedUser);
  }

  @Operation(summary = "Deactivate Users", security = @SecurityRequirement(name = "JWT"))
  @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
  @PreAuthorize("""
      hasAuthority('ADMIN') or
      (hasAuthority('GAMEMANAGER') and @userService.getUserById(#id).get().role == T(at.ac.uibk.timeguess.flipflapp.user.UserRole).PLAYER)
      """)
  @DeleteMapping("/{id}")
  public User deactivateUser(@PathVariable final Long id) {
    return userService.deactivateUser(id);
  }

  @Operation(summary = "Get the authenticated user", security = @SecurityRequirement(name = "JWT"))
  @GetMapping("/me")
  public User getAuthenticatedUser(@AuthenticationPrincipal final TimeGuessPrincipal principal) {
    return principal.getUser();
  }
}
