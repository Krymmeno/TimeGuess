package at.ac.uibk.timeguess.flipflapp.security.jwt;


import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.AUTH_ENDPOINT;
import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.EXPIRATION_TIME;
import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.TOKEN_TYPE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Based on at.ac.uibk.heidi.security.jwt.JwtAuthenticationController
 * (https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2)
 */
@Tag(name = "Authentication")
@RestController
public class JwtAuthenticationController {

  private final AuthenticationManager authenticationManager;

  public JwtAuthenticationController(final AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(AuthenticationException.class)
  public String handleAuthenticationFailed(final Exception e) {
    return e.toString();
  }


  @Operation(summary = "Get new JWT")
  @ApiResponse(responseCode = "200")
  @ApiResponse(responseCode = "401", description = "Wrong credentials", content = @Content)
  @PostMapping(AUTH_ENDPOINT)
  public JwtAuthenticationResponse auth(
      @Validated @RequestBody final CredentialsDto credentialsDto) {
    final Authentication authenticate = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            credentialsDto.getUsername(),
            credentialsDto.getPassword(),
            List.of())
    );
    final String token = JwtUtils
        .createToken(((User) authenticate.getPrincipal()).getUsername());
    return new JwtAuthenticationResponse(token, TOKEN_TYPE, EXPIRATION_TIME);
  }
}
