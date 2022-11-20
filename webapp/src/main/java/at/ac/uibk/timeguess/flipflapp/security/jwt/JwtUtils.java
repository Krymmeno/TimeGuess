package at.ac.uibk.timeguess.flipflapp.security.jwt;

import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.EXPIRATION_TIME;
import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.SECRET;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Based on at.ac.uibk.heidi.security.jwt.JwtUtils
 * (https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2)
 */
public class JwtUtils {

  private JwtUtils() {
  }

  /**
   * @param username the username to include in the JWT
   * @return a JWT with default {@link JwtConstants#EXPIRATION_TIME}
   */
  public static String createToken(final String username) {
    return createToken(username, new Date(System.currentTimeMillis() + EXPIRATION_TIME));
  }

  /**
   * @param username  the username to include in the JWT
   * @param expiresAt the date when the JWT should expire
   * @return a JWT
   */
  public static String createToken(final String username, final Date expiresAt) {
    return JWT.create()
        .withSubject(username)
        .withExpiresAt(expiresAt)
        .sign(Algorithm.HMAC256(SECRET.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * @param token the JWT to decode
   * @return the decoded JWT
   * @throws SignatureVerificationException if the signature is invalid
   * @throws TokenExpiredException          if the token has expired
   */
  public static DecodedJWT getDecodedJwt(final String token) {
    return JWT.require(Algorithm.HMAC256(SECRET.getBytes(StandardCharsets.UTF_8))).build()
        .verify(token);
  }

  /**
   * @param token the JWT from which to extract the username
   * @return the username stored in the JWT
   * @throws SignatureVerificationException if the signature is invalid
   * @throws TokenExpiredException          if the token has expired
   */
  public static String getUsernameFromToken(final String token) {
    return getDecodedJwt(token).getSubject();
  }
}
