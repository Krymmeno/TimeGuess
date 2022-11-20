package at.ac.uibk.timeguess.flipflapp.websocket;

import at.ac.uibk.timeguess.flipflapp.game.GameService;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationConfig implements WebSocketMessageBrokerConfigurer {

  private final UserDetailsService userDetailsService;

  private GameRoomService gameRoomService;
  private GameService gameService;

  public WebSocketAuthenticationConfig(final UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Autowired
  public void setGameRoomService(@Lazy final GameRoomService gameRoomService) {
    this.gameRoomService = gameRoomService;
  }

  @Autowired
  public void setGameService(@Lazy final GameService gameService) {
    this.gameService = gameService;
  }

  @Override
  public void configureClientInboundChannel(final ChannelRegistration registration) {
    registration.interceptors(new JwtChannelInterceptor(userDetailsService), new DisconnectInterceptor(
        gameRoomService, gameService));
  }

}
