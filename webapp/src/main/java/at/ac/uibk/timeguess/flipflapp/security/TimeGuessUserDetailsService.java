package at.ac.uibk.timeguess.flipflapp.security;

import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Based on at.ac.uibk.heidi.security.HeidiUserDetailsService
 * (https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2)
 */
public class TimeGuessUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public TimeGuessUserDetailsService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * load the user by his userName and return the TimeGuessPrincipal
   *
   * @param username the userName of the User
   * @return TimeGuessPrincipal
   * @throws UsernameNotFoundException if the username could not be associated with an user
   */
  @Override
  public UserDetails loadUserByUsername(final String username) {
    final User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException(username));
    return new TimeGuessPrincipal(user);
  }
}
