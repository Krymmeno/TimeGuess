package at.ac.uibk.timeguess.flipflapp.security.jwt;

import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.AUTHORITIES;
import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.AUTHORIZATION_VALUE_PREFIX;
import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.IS_USER;

import at.ac.uibk.timeguess.flipflapp.security.UserDetailsAuthenticationToken;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserInactiveException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter which sets the currently authenticated principal based on the JWT provided in the
 * Authorization header.
 *
 * <p>The Authorization header's value must be prefixed by
 * {@link JwtConstants#AUTHORIZATION_VALUE_PREFIX}.
 *
 * <p>See {@link JwtAuthenticationController} for details on how the JWTs are created.
 *
 * <p>Based on at.ac.uibk.heidi.security.jwt.JwtAuthorizationFilter
 * (https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2)
 */
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  private final UserDetailsService userDetailsService;

  private final UserRepository userRepository;

  public JwtAuthorizationFilter(final UserDetailsService userDetailsService,
      final UserRepository userRepository) {
    this.userDetailsService = userDetailsService;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
      final HttpServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization != null && authorization.startsWith(AUTHORIZATION_VALUE_PREFIX)) {
      try {
        SecurityContextHolder.getContext().setAuthentication(getAuthentication(authorization));
      } catch (final SignatureVerificationException | TokenExpiredException | UsernameNotFoundException | UserInactiveException e) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(e.toString());
        return;
      }
    }
    chain.doFilter(request, response);
  }

  private Authentication getAuthentication(final String authorization) {
    final String token = authorization.substring(AUTHORIZATION_VALUE_PREFIX.length());
    final DecodedJWT decodedJwt = JwtUtils.getDecodedJwt(token);
    if (BooleanUtils.isFalse(decodedJwt.getClaim(IS_USER).asBoolean())) {
      return getAnonymousAuthenticationToken(token, decodedJwt);
    } else {
      return getUserDetailsAuthenticationToken(token, decodedJwt);
    }
  }

  private UserDetailsAuthenticationToken getUserDetailsAuthenticationToken(String token,
      DecodedJWT decodedJwt) {
    final String subject = decodedJwt.getSubject();
    final UserDetails userDetails = userDetailsService
        .loadUserByUsername(subject);
    final User user = userRepository.findByUsername(subject).orElseThrow(
        () -> new UsernameNotFoundException(
            "The User with username %s was not found".formatted(subject)));
    if (!user.getActive()) {
      throw new UserInactiveException(subject);
    }
    return new UserDetailsAuthenticationToken(userDetails, token);
  }

  private AnonymousAuthenticationToken getAnonymousAuthenticationToken(String token,
      DecodedJWT decodedJwt) {
    final String[] authorities = decodedJwt.getClaim(AUTHORITIES).asArray(String.class);
    final Set<SimpleGrantedAuthority> grantedAuthorities = Arrays.stream(authorities)
        .map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    return new AnonymousAuthenticationToken(token, decodedJwt.getSubject(), grantedAuthorities);
  }
}
