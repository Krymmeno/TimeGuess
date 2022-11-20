package at.ac.uibk.timeguess.flipflapp.security.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Based on at.ac.uibk.heidi.security.jwt.CredentialsDto
 * (https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2)
 */
@Schema(name = "Credentials")
public class CredentialsDto {

  @NotBlank
  private String username;

  @NotNull
  private String password;

  public CredentialsDto() {
  }

  public CredentialsDto(@NotBlank final String username,
      @NotNull final String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CredentialsDto that = (CredentialsDto) o;
    return username.equals(that.username) && password.equals(that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password);
  }

  @Override
  public String toString() {
    return "CredentialsDto{" +
        "username='" + username + '\'' +
        ", password='" + password + '\'' +
        '}';
  }
}
