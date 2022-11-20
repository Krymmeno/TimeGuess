package at.ac.uibk.timeguess.flipflapp.websocket;

import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.AUTHORIZATION_VALUE_PREFIX;

import at.ac.uibk.timeguess.flipflapp.security.UserDetailsAuthenticationToken;
import at.ac.uibk.timeguess.flipflapp.security.jwt.JwtUtils;
import at.ac.uibk.timeguess.flipflapp.websocket.exception.MissingJwtException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.messaging.util.matcher.MessageMatcher;
import org.springframework.security.messaging.util.matcher.SimpMessageTypeMatcher;
import org.springframework.stereotype.Component;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(JwtChannelInterceptor.class);

  private final MessageMatcher<Object> connectMatcher
      = new SimpMessageTypeMatcher(SimpMessageType.CONNECT);

  private final UserDetailsService userDetailsService;

  public JwtChannelInterceptor(final UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }


  @Override
  public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
    logger.info("Message: {}", message);
    if (!this.connectMatcher.matches(message)) {
      return message;
    }
    final StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor != null) {
      final List<String> authorizationList = accessor
          .getNativeHeader(HttpHeaders.AUTHORIZATION);
      if (authorizationList == null || authorizationList.size() == 0) {
        throw new MissingJwtException("JWT is missing in CONNECT message.");
      }
      final String authorization = authorizationList.get(0);
      if (authorization != null && authorization.startsWith(AUTHORIZATION_VALUE_PREFIX)) {
        accessor.setUser(getAuthentication(authorization));
      }
    }

    return message;
  }

  private Authentication getAuthentication(final String authorization) {
    final String token = authorization.substring(AUTHORIZATION_VALUE_PREFIX.length());
    final UserDetails userDetails = userDetailsService
        .loadUserByUsername(JwtUtils.getUsernameFromToken(token));
    return new UserDetailsAuthenticationToken(userDetails, token);
  }
}
