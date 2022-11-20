package at.ac.uibk.timeguess.flipflapp.security;

import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsAuthenticationToken extends AbstractAuthenticationToken {

  private final UserDetails principal;
  private final String token;

  public UserDetailsAuthenticationToken(final UserDetails principal, final String token) {
    super(principal.getAuthorities());
    this.principal = principal;
    this.token = token;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return token;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final UserDetailsAuthenticationToken that = (UserDetailsAuthenticationToken) o;
    return Objects.equals(principal, that.principal) && Objects
        .equals(token, that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), principal, token);
  }

  @Override
  public String toString() {
    return "UserDetailsAuthenticationToken{" +
        "principal=" + principal +
        ", token='" + token + '\'' +
        '}';
  }
}
