package at.ac.uibk.timeguess.flipflapp.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import at.ac.uibk.timeguess.flipflapp.user.exception.UsernameTakenException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@Transactional
public class UserServiceTest {

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  private final User kirk = new User("JamesTKirk", "James", "Kirk", "passwd", UserRole.ADMIN, true);

  private final User scotty =
      new User("Scotty", "Montgomery", "Scott", "beam", UserRole.GAMEMANAGER, true);

  private final User spock = new User("Spock", "SchnTgai", "Spock", "passwd", UserRole.ADMIN, true);

  @BeforeEach
  void setUp() {
    Stream.of(kirk, scotty).forEach(userService::createUser);
  }

  @Test
  void testGetAllUsers() {
    assertTrue(userService.getAllUsers().containsAll(Set.of(scotty, kirk)));
  }

  @Test
  void createUser() {
    final User spockCreate = userService.createUser(spock);
    assertEquals(spockCreate.getLastName(), spock.getLastName());
    assertEquals(spockCreate.getRole(), spock.getRole());
    assertEquals(spockCreate.getActive(), spock.getActive());
    assertEquals(spockCreate.getUsername(), spock.getUsername());
    assertEquals(spock.getFirstName(), spockCreate.getFirstName());
    assertNotNull(spockCreate.getUserId());
    assertTrue(passwordEncoder.matches("passwd",
        userService.getUserById(spockCreate.getUserId()).orElseThrow().getPassword()));
  }

  @Test
  void createUserWithTakenName() {
    assertThrows(UsernameTakenException.class, () -> userService
        .createUser(new User("JamesTKirk", "James", "Kirk", "passwd", UserRole.ADMIN, true)));
  }

  @Test
  void testDelete() {
    final Long id = userService.getUserById(scotty.getUserId()).orElseThrow().getUserId();
    final User deadScotty = userService.deactivateUser(id);
    assertThat(deadScotty).isNotNull();
    assertEquals(deadScotty.getFirstName(), scotty.getFirstName());
  }

  @Test
  void testDeleteUnknownUser() {
    Assertions.assertTrue(userService.getUserById(-1L).isEmpty());
    assertThrows(UserNotFoundException.class, () -> userService.deactivateUser(-1L));
  }

  @Test
  void testGetById() {
    final Long id = kirk.getUserId();
    final Optional<User> user = userService.getUserById(id);
    assertTrue(user.isPresent());
    assertEquals(kirk.getRole(), user.get().getRole());
    assertEquals(kirk.getLastName(), user.get().getLastName());
  }

  @Test
  void testGetByNonExistentId() {
    final Long id = -1L;
    assertTrue(userService.getUserById(id).isEmpty());
  }

  @Test
  void updateUserWithPasswordChange() {
    final Long id = kirk.getUserId();
    final User newKirk = userService.updateUser(id,
        new User("NewJamesTKirk", "NewJames", "NewKirk", "securePasswd", UserRole.ADMIN, true));
    assertEquals(id, newKirk.getUserId());
    assertEquals(newKirk.getLastName(), kirk.getLastName());
    assertEquals(newKirk.getRole(), kirk.getRole());
    assertEquals(newKirk.getFirstName(), kirk.getFirstName());
    Assertions.assertNotEquals("NewJamesTKirk", kirk.getUsername());
    assertTrue(passwordEncoder.matches("securePasswd", kirk.getPassword()));
  }

  @Test
  void updateUserWithoutPasswordChange() {
    final Long id = kirk.getUserId();
    String oldPassword = kirk.getPassword();
    final User newKirk = userService.updateUser(id,
        new User("NewJamesTKirk", "NewJames", "NewKirk", "", UserRole.ADMIN, true));
    assertEquals(newKirk.getPassword(), oldPassword);
  }

  @Test
  void updateWithUnknownUser() {
    assertTrue(userRepository.findById(-1L).isEmpty());
    assertThrows(UserNotFoundException.class, () -> userService.updateUser(-1L,
        new User("NewJamesTKirk", "NewJames", "NewKirk", "", UserRole.ADMIN, true)));
  }

  @Test
  void wantsToUpdateRoleUnknownUser() {
    assertTrue(userRepository.findById(-1L).isEmpty());
    assertThrows(UserNotFoundException.class, () -> userService.wantsToUpdateRole(-1L,
        new User("NewJamesTKirk", "NewJames", "NewKirk", "", UserRole.ADMIN, true)));
  }

  @Test
  void wantsToUpdateRole() {
    assertTrue(userService.wantsToUpdateRole(scotty.getUserId(),
        new User("Scotty", "Montgomery", "Scott", "beam", UserRole.ADMIN, true)));
    assertFalse(userService.wantsToUpdateRole(scotty.getUserId(),
        new User("Scotty", "Montgomery", "Scott", "beam", UserRole.GAMEMANAGER, true)));
  }
}
