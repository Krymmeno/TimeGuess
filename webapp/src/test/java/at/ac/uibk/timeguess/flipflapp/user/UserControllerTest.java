package at.ac.uibk.timeguess.flipflapp.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_ADMIN;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAMEMANAGER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_INACTIVE_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void getAllUsersNotLoggedIn() throws Exception {
    mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());
  }

  @Test
  void getAllUsersAsAdmin() throws Exception {
    mockMvc
        .perform(get("/api/users")
            .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME))))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(RANDOM_ADMIN.FIRSTNAME)))
        .andExpect(content().string(containsString(RANDOM_GAMEMANAGER.LASTNAME)))
        .andExpect(content().string(containsString(RANDOM_PLAYER.FIRSTNAME)));
  }

  @Test
  void getAllUsersAsGameManager() throws Exception {
    mockMvc
        .perform(get("/api/users")
            .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(RANDOM_ADMIN.FIRSTNAME)))
        .andExpect(content().string(containsString(RANDOM_GAMEMANAGER.LASTNAME)))
        .andExpect(content().string(containsString(RANDOM_PLAYER.FIRSTNAME)));
  }

  @Test
  void getAllUsersAsPlayer() throws Exception {
    mockMvc
        .perform(get("/api/users")
            .with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAllActiveUsersAsPlayer() throws Exception {
    mockMvc
        .perform(get("/api/users/active")
            .with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME))))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString(RANDOM_INACTIVE_PLAYER.USERNAME))))
        .andExpect(content().string(containsString(RANDOM_PLAYER.USERNAME)));
  }

  @Test
  void createPlayer() throws Exception {
    mockMvc
        .perform(post("/api/users").content("""
            {
                "username": "FancyJonas",
                "firstName": "Jonas",
                "lastName": "Test",
                "password": "passwd",
                "active": true,
                "role": "PLAYER"
            }
            """).with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string(containsString("FancyJonas")));

    Assertions.assertTrue(passwordEncoder.matches("passwd",
        userRepository.findByUsername("FancyJonas").orElseThrow().getPassword()));
  }

  @Test
  void createAdminAsGameManager() throws Exception {
    mockMvc.perform(post("/api/users").content("""
        {
            "username": "FancyJonas",
            "firstName": "Jonas",
            "lastName": "Test",
            "password": "passwd",
            "active": true,
            "role": "ADMIN"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }

  @Test
  void createPlayerWithTakenUsername() throws Exception {
    mockMvc.perform(post("/api/users").content("""
        {
            "username": "randomplayer",
            "firstName": "Jonas",
            "lastName": "Test",
            "password": "passwd",
            "active": true,
            "role": "PLAYER"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isConflict());
  }

  @Test
  void createUserAsPlayer() throws Exception {
    mockMvc.perform(post("/api/users").content("""
        {
            "username": "FancyJonas",
            "firstName": "Jonas",
            "lastName": "Test",
            "password": "passwd",
            "active": true,
            "role": "PLAYER"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }

  @Test
  void createUserWithMissingField() throws Exception {
    mockMvc.perform(post("/api/users").content("""
        {
            "username": "FancyJonas",
            "firstName": "Jonas",
            "lastName": "Test",
            "password": "passwd",
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void deactivateUserAsPlayer() throws Exception {
    User user = userRepository.findByUsername(RANDOM_ADMIN.USERNAME).orElseThrow();
    mockMvc
        .perform(delete("/api/users/%d".formatted(user.getUserId()))
            .with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  void deactivateAdminAsGameManager() throws Exception {
    User admin = userRepository.findByUsername("admin").orElseThrow();
    Assertions.assertEquals(admin.getRole(), UserRole.ADMIN);
    mockMvc
        .perform(delete("/api/users/%d".formatted(admin.getUserId()))
            .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  void deactivateGameManagerAsGameManager() throws Exception {
    User gameManager = userRepository.findByUsername("gamemanager").orElseThrow();
    Assertions.assertEquals(gameManager.getRole(), UserRole.GAMEMANAGER);
    mockMvc
        .perform(delete("/api/users/%d".formatted(gameManager.getUserId()))
            .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isForbidden());
  }

  @Test
  void deactivateUser() throws Exception {
    User user = userRepository.findByUsername("player1").orElseThrow();
    mockMvc
        .perform(delete("/api/users/%d".formatted(user.getUserId()))
            .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME))))
        .andExpect(status().isOk()).andExpect(content().string(containsString("player")));
    assertThat(userRepository.findById(user.getUserId()).orElseThrow().getActive()).isFalse();
  }

  @Test
  void deactivateUnknownUser() throws Exception {
    mockMvc
        .perform(delete("/api/users/%d".formatted(-1L))
            .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME))))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateAsPlayer() throws Exception {
    User player1 = userRepository.findByUsername("player1").orElseThrow();
    assertThat(player1.getRole()).isEqualTo(UserRole.PLAYER);
    mockMvc.perform(put("/api/users/%d".formatted(player1.getUserId())).content("""
        {
            "username": "FancyJonas",
            "firstName": "Daniel",
            "lastName": "Test",
            "password": "NewPassword",
            "active": true,
            "role": "PLAYER"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }

  @Test
  void updateAdminAsGameManager() throws Exception {
    User admin = userRepository.findByUsername(RANDOM_ADMIN.USERNAME).orElseThrow();
    assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    mockMvc.perform(put("/api/users/%d".formatted(1L)).content("""
        {
            "username": "FancyJonas",
            "firstName": "Daniel",
            "lastName": "Test",
            "password": "NewPassword",
            "active": true,
            "role": "ADMIN"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }

  @Test
  void updatePlayerAsGameManager() throws Exception {
    final User player = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();
    assertThat(player.getRole()).isEqualTo(UserRole.PLAYER);
    mockMvc
        .perform(put("/api/users/%d".formatted(2L)).content("""
            {
                "username": "%s",
                "firstName": "%s",
                "lastName": "Test",
                "password": "NewPassword",
                "active": true,
                "role": "PLAYER"
            }
            """.formatted("ObiWan", "Kenobi"))
            .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string(containsString("Kenobi")));
  }

  @Test
  void updateUserAsAdmin() throws Exception {
    User user = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();
    Assertions.assertNotEquals("Daniel",
        userRepository.findById(user.getUserId()).orElseThrow().getFirstName());
    mockMvc
        .perform(put("/api/users/%d".formatted(1L)).content("""
            {
                "username": "FancyJonas",
                "firstName": "Daniel",
                "lastName": "Test",
                "password": "NewPassword",
                "active": true,
                "role": "PLAYER"
            }
            """).with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string(containsString("Daniel")));
  }

  @Test
  void updateMe() throws Exception {
    User user = userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME).orElseThrow();
    mockMvc
        .perform(put("/api/users/%d".formatted(user.getUserId())).content("""
            {
                "username": "username",
                "firstName": "NotSoRandom",
                "lastName": "gamemanager",
                "password": "NewPassword",
                "active": true,
                "role": "GAMEMANAGER"
            }
            """).with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string(containsString("NotSoRandom")));
  }

  @Test
  void updateRoleAsMe() throws Exception {
    final User user = userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME).orElseThrow();
    mockMvc.perform(put("/api/users/%d".formatted(user.getUserId())).content("""
        {
            "username": "username",
            "firstName": "NotSoRandom",
            "lastName": "gamemanager",
            "password": "NewPassword",
            "active": true,
            "role": "PLAYER"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }

  @Test
  void updateRoleAsAdmin() throws Exception {
    final User player = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();
    mockMvc
        .perform(put("/api/users/%d".formatted(player.getUserId())).content("""
            {
                "username": "FancyJonas",
                "firstName": "Daniel",
                "lastName": "Test",
                "password": "NewPassword",
                "active": true,
                "role": "ADMIN"
            }
            """).with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string(containsString("ADMIN")));
  }

  @Test
  void updateMyRoleAsAdmin() throws Exception {
    final User admin = userRepository.findByUsername(RANDOM_ADMIN.USERNAME).orElseThrow();
    mockMvc
        .perform(put("/api/users/%d".formatted(admin.getUserId())).content("""
            {
                "username": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "active": true,
                "role": "GAMEMANAGER"
            }
            """.formatted(admin.getUsername(), admin.getFirstName(), admin.getLastName()))
            .with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateRoleAsGameManager() throws Exception {
    final User player = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();
    assertThat(player.getRole()).isEqualTo(UserRole.PLAYER);
    mockMvc.perform(put("/api/users/%d".formatted(player.getUserId())).content("""
        {
            "username": "FancyJonas",
            "firstName": "Daniel",
            "lastName": "Test",
            "password": "NewPassword",
            "active": true,
            "role": "ADMIN"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }

  @Test
  void updatePasswordWithNull() throws Exception {
    final User gameManager =
        userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME).orElseThrow();
    mockMvc
        .perform(put("/api/users/%d".formatted(gameManager.getUserId())).content("""
            {
                "username": "FancyJonas",
                "firstName": "Daniel",
                "lastName": "Test",
                "password": null,
                "active": true,
                "role": "ADMIN"
            }
            """).with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string(containsString("Daniel")))
        .andExpect(content().string(containsString(RANDOM_GAMEMANAGER.USERNAME)));
  }

  @Test
  void updatePasswordWithEmpty() throws Exception {
    final User gameManager =
        userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME).orElseThrow();
    mockMvc.perform(put("/api/users/%d".formatted(gameManager.getUserId())).content("""
        {
            "username": "FancyJonas",
            "firstName": "Daniel",
            "lastName": "Test",
            "password": "",
            "active": true,
            "role": "ADMIN"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  void updateUsernameWithNull() throws Exception {
    final User gameManager =
        userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME).orElseThrow();
    mockMvc.perform(put("/api/users/%d".formatted(gameManager.getUserId())).content("""
        {
            "username": null,
            "firstName": "Daniel",
            "lastName": "Test",
            "password": "asdf",
            "active": true,
            "role": "ADMIN"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_ADMIN.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void updateUserWithTakenUsername() throws Exception {
    final User gameManager =
        userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME).orElseThrow();
    assertThat(userRepository.findAll().stream().map(User::getUsername).toList()).asList()
        .contains("admin");
    assertThat(gameManager.getUsername()).isNotEqualTo("admin");
    mockMvc.perform(put("/api/users/%d".formatted(gameManager.getUserId())).content("""
        {
            "username": "admin",
            "firstName": "Daniel",
            "lastName": "Test",
            "active": true,
            "role": "GAMEMANAGER"
        }
        """).with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }

  @Test
  void getAuthenticatedWithoutToken() throws Exception {
    mockMvc.perform(get("/api/users/me")).andExpect(status().isForbidden());
  }

  @Test
  void getAuthenticatedUser() throws Exception {
    mockMvc
        .perform(get("/api/users/me")
            .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk()).andExpect(content().string(containsString("GAMEMANAGER")))
        .andExpect(content().string(containsString(RANDOM_GAMEMANAGER.FIRSTNAME)))
        .andExpect(content().string(containsString(RANDOM_GAMEMANAGER.LASTNAME)));
  }
}
