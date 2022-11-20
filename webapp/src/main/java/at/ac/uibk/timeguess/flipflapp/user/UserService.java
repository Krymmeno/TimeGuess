package at.ac.uibk.timeguess.flipflapp.user;

import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import at.ac.uibk.timeguess.flipflapp.user.exception.UsernameTakenException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * @return all users
   */
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  /**
   * Creates a new User
   *
   * @param user with all the data required to create a user
   * @return created user
   * @throws UsernameTakenException if username already exists in Database
   */
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public User createUser(final User user) {
    if (userRepository.findByUsername(user.getUsername()).isPresent()) {
      throw new UsernameTakenException(user);
    }
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    return userRepository.save(user);
  }

  /**
   * Deactivates a user.
   *
   * @param id of the user to be deactivated
   * @return deactivated user
   * @throws UserNotFoundException if id is not belonging to any user
   */
  public User deactivateUser(final long id) {
    User user = userRepository.findById(id).orElseThrow(
        () -> new UserNotFoundException(id));
    user.setActive(false);
    userRepository.save(user);
    return user;
  }

  /**
   * Update an existing User
   *
   * @param id          of the user to be updated
   * @param updatedUser new user data
   * @return updated User
   * @throws UserNotFoundException if id is not belonging to any user
   */
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public User updateUser(final long id, final User updatedUser) {
    User user = userRepository.findById(id).orElseThrow(
        () -> new UserNotFoundException(id));
    if (StringUtils.hasText(updatedUser.getPassword())) {
      user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
    }
    user.setFirstName(updatedUser.getFirstName());
    user.setActive(updatedUser.getActive());
    user.setLastName(updatedUser.getLastName());
    user.setRole(updatedUser.getRole());
    return userRepository.save(user);
  }

  /**
   * Returns a user by ID
   *
   * @param id of the wanted user
   * @return user from database if existing
   */
  public Optional<User> getUserById(final Long id) {
    return userRepository.findById(id);
  }

  /**
   * Returns a user by ID if he does not exists throw
   *
   * @param id of the wanted user
   * @return user from database if existing
   * @throws UserNotFoundException if the user with the given id could not be found
   */
  public User getUserIfExists(final Long id) {
    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
  }


  /**
   * checks if the role of an user changed
   *
   * @param oldUserId the id of the user to compare the role to
   * @param updatedUser the new user
   * @return whether or not the role of the old user differs from the updateUser role (True if different)
   * @throws UserNotFoundException if the oldUserId cannot be associated with an user
   */
  public Boolean wantsToUpdateRole(final Long oldUserId, final User updatedUser) {
    final User oldUser = userRepository.findById(oldUserId).orElseThrow(
        () -> new UserNotFoundException(oldUserId));
    return updatedUser.getRole() != null && !oldUser.getRole().equals(updatedUser.getRole());
  }

}
