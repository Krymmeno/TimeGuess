package at.ac.uibk.timeguess.flipflapp.security.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

/**
 * Based on at.ac.uibk.heidi.security.jwt.JwtAuthenticationResponse
 * (https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2)
 */
@Schema(name = "JWT")
public class JwtAuthenticationResponse {

  private String accessToken;
  private String tokenType;

  @Schema(description = "Expiration time in ms.")
  private Long expiresIn;

  public JwtAuthenticationResponse() {
  }

  public JwtAuthenticationResponse(final String accessToken, final String tokenType,
      final Long expiresIn) {
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(final String accessToken) {
    this.accessToken = accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(final String tokenType) {
    this.tokenType = tokenType;
  }

  public Long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(final Long expiresIn) {
    this.expiresIn = expiresIn;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final JwtAuthenticationResponse that = (JwtAuthenticationResponse) o;
    return Objects.equals(accessToken, that.accessToken) && Objects
        .equals(tokenType, that.tokenType) && Objects.equals(expiresIn, that.expiresIn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, tokenType, expiresIn);
  }

  @Override
  public String toString() {
    return "JwtAuthenticationResponse{" +
        "accessToken='" + accessToken + '\'' +
        ", tokenType='" + tokenType + '\'' +
        ", expiresIn=" + expiresIn +
        '}';
  }
}
