package at.ac.uibk.timeguess.flipflapp.websocket;

import at.ac.uibk.timeguess.flipflapp.game.GameNotFoundException;
import at.ac.uibk.timeguess.flipflapp.game.GameService;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomUser;
import at.ac.uibk.timeguess.flipflapp.security.TimeGuessPrincipal;
import at.ac.uibk.timeguess.flipflapp.security.UserDetailsAuthenticationToken;
import at.ac.uibk.timeguess.flipflapp.team.Team;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.websocket.exception.MissingHeaderException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.messaging.util.matcher.MessageMatcher;
import org.springframework.security.messaging.util.matcher.SimpMessageTypeMatcher;
import org.springframework.stereotype.Component;

@Component
public class DisconnectInterceptor implements ChannelInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DisconnectInterceptor.class);
  private static final Pattern PATTERN = Pattern.compile("\\/topic\\/(games)\\/(\\d+)");
  private static final String AUTHENTICATED_USER = "simpUser";
  private static final String SUBSCRIPTION_ID = "id";
  private static final String SUBSCRIPTION_DESTINATION = "destination";

  private final MessageMatcher<Object> disconnectMatcher
      = new SimpMessageTypeMatcher(SimpMessageType.DISCONNECT);
  private final MessageMatcher<Object> unsubscribeMatcher
      = new SimpMessageTypeMatcher(SimpMessageType.UNSUBSCRIBE);
  private final MessageMatcher<Object> subscribeMatcher
      = new SimpMessageTypeMatcher(SimpMessageType.SUBSCRIBE);
  private final Map<String, String> subscriptionDestinationMap = new ConcurrentHashMap<>();
  private final GameRoomService gameRoomService;
  private final GameService gameService;

  public DisconnectInterceptor(
      final GameRoomService gameRoomService, final GameService gameService) {
    this.gameRoomService = gameRoomService;
    this.gameService = gameService;
  }

  @Override
  public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
    if (!this.disconnectMatcher.matches(message) && !this.unsubscribeMatcher.matches(message)
        && !this.subscribeMatcher.matches(message)) {
      return message;
    }
    final StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (subscribeMatcher.matches(message)) {
      if (accessor != null) {
        final String subscriptionId = getHeader(accessor, SUBSCRIPTION_ID);
        final String destination = getHeader(accessor, SUBSCRIPTION_DESTINATION);
        subscriptionDestinationMap.put(subscriptionId, destination);
      }
    } else if (this.disconnectMatcher.matches(message)) {
      if (accessor != null) {
        final TimeGuessPrincipal timeGuessPrincipal = getPrincipal(accessor);
        if (timeGuessPrincipal != null) {
          final User authenticatedUser = timeGuessPrincipal.getUser();
          removeUserFromGameRoom(authenticatedUser);
          removeUserFromGames(authenticatedUser);
        }
      }
    } else if (this.unsubscribeMatcher.matches(message)) {
      if (accessor != null) {
        final String subscriptionId = getHeader(accessor, SUBSCRIPTION_ID);
        final TimeGuessPrincipal timeGuessPrincipal = getPrincipal(accessor);
        if (subscriptionDestinationMap.containsKey(subscriptionId)) {
          final Matcher matcher
              = PATTERN.matcher(subscriptionDestinationMap.get(subscriptionId));
          if (matcher.matches()) {
            if (timeGuessPrincipal != null) {
              final Long gameId = Long.valueOf(matcher.group(2));
              try {
                final boolean gameContainsCurrentUser = gameService.getGame(gameId).getTeams()
                    .stream()
                    .map(Team::getPlayers).flatMap(Collection::stream)
                    .map(User::getUserId)
                    .anyMatch(userId -> userId.equals(timeGuessPrincipal.getUser().getUserId()));
                if (gameContainsCurrentUser) {
                  this.gameService.abortGame(gameId);
                }
              } catch (GameNotFoundException e) {
                LOGGER.debug("Could not find game with ID {}", gameId);
              }
            }
          }
        }
      }
    }
    return message;
  }

  private TimeGuessPrincipal getPrincipal(final StompHeaderAccessor accessor) {
    final Object simpUser = accessor.getMessageHeaders().get(AUTHENTICATED_USER);
    final boolean isNotNullUserDetailsToken =
        Objects.nonNull(simpUser) && simpUser instanceof UserDetailsAuthenticationToken;
    if (isNotNullUserDetailsToken) {
      final Object principal = ((UserDetailsAuthenticationToken) simpUser).getPrincipal();
      if (principal instanceof TimeGuessPrincipal timeGuessPrincipal) {
        return timeGuessPrincipal;
      }
    }
    return null;
  }

  private String getHeader(final StompHeaderAccessor accessor, final String headerKey) {
    return Optional
        .ofNullable(accessor.getNativeHeader(headerKey))
        .filter(strings -> strings.size() > 0)
        .map(strings -> strings.get(0))
        .orElseThrow(() -> new MissingHeaderException(headerKey));
  }

  private void removeUserFromGameRoom(final User authenticatedUser) {
    this.gameRoomService.getAllGameRooms().stream()
        .filter(gameRoom -> gameRoom.getGameRoomUsers().stream().map(
            GameRoomUser::getUser).toList().contains(authenticatedUser))
        .forEach(gameRoom -> gameRoomService
            .removePlayer(gameRoom.getGameRoomId(), authenticatedUser.getUserId()));
  }

  private void removeUserFromGames(final User authenticatedUser) {
    this.gameService.abortGamesAssociatedWithUser(authenticatedUser);
  }
}
