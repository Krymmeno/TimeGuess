package at.ac.uibk.timeguess.flipflapp.security.jwt;

public final class JwtConstants {

  private JwtConstants() {
  }

  public static final String AUTH_ENDPOINT = "/api/auth";
  public static final String TOKEN_TYPE = "Bearer";
  public static final String AUTHORIZATION_VALUE_PREFIX = "Bearer ";
  public static final String SECRET = "Gel8cT24KaQnu7s1Xy";
  public static final long EXPIRATION_TIME = 1000L * 3600L * 24L * 7L; // 7 days
  public static final String IS_USER = "is_user";
  public static final String AUTHORITIES = "authorities";
}
