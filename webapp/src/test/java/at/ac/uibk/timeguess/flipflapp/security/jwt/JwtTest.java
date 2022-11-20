package at.ac.uibk.timeguess.flipflapp.security.jwt;

import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.AUTHORIZATION_VALUE_PREFIX;
import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.EXPIRATION_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_ADMIN;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_INACTIVE_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class,
    TestDataConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class JwtTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MockMvc mockMvc;

  private JwtTestHelper jwtTestHelper;

  @BeforeEach
  void setUp() {
    this.jwtTestHelper = new JwtTestHelper(mockMvc);
  }

  @Test
  void getTokenWithCorrectCredentials() throws Exception {
    assertThat(userRepository.findByUsername(RANDOM_ADMIN.USERNAME)).isPresent();
    assertThat(jwtTestHelper.getToken(RANDOM_ADMIN.USERNAME, RANDOM_ADMIN.PASSWORD)).isNotNull();
  }

  @Test
  void getTokenWithWrongPassword() throws Exception {
    assertThat(userRepository.findByUsername(RANDOM_ADMIN.USERNAME)).isPresent();
    jwtTestHelper.getAuthenticationResponse(RANDOM_ADMIN.USERNAME, "wrong" + RANDOM_ADMIN.PASSWORD)
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getTokenForNonExistentUser() throws Exception {
    assertThat(userRepository.findByUsername("nonexistentuser")).isEmpty();
    jwtTestHelper.getAuthenticationResponse("nonexistentuser", "password")
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getTokenForInactiveUser() throws Exception {
    assertThat(userRepository.findByUsername(RANDOM_INACTIVE_PLAYER.USERNAME)).isPresent();
    jwtTestHelper
        .getAuthenticationResponse(RANDOM_INACTIVE_PLAYER.USERNAME, RANDOM_INACTIVE_PLAYER.PASSWORD)
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getWithoutToken() throws Exception {
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getWithCorrectToken() throws Exception {
    assertThat(userRepository.findByUsername(RANDOM_ADMIN.USERNAME)).isPresent();
    mockMvc.perform(get("/api/users")
        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE_PREFIX + jwtTestHelper
            .getToken(RANDOM_ADMIN.USERNAME, RANDOM_ADMIN.PASSWORD)))
        .andExpect(status().isOk());
  }

  @Test
  void getWithMalformedToken() throws Exception {
    assertThat(userRepository.findByUsername(RANDOM_ADMIN.USERNAME)).isPresent();
    mockMvc.perform(get("/api/users")
        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE_PREFIX + jwtTestHelper
            .getToken(RANDOM_ADMIN.USERNAME, RANDOM_ADMIN.PASSWORD) + "1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getWithExpiredToken() throws Exception {
    assertThat(userRepository.findByUsername(RANDOM_ADMIN.USERNAME)).isPresent();
    final String token = JwtUtils
        .createToken(RANDOM_ADMIN.USERNAME, new Date(System.currentTimeMillis() - 1000));
    mockMvc.perform(get("/api/users")
        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE_PREFIX + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getWithTokenForNonExistentUser() throws Exception {
    assertThat(userRepository.findByUsername("nonexistentuser")).isEmpty();
    final String token = JwtUtils
        .createToken("nonexistentuser", new Date(System.currentTimeMillis() + EXPIRATION_TIME));
    mockMvc.perform(get("/api/users")
        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE_PREFIX + token))
        .andExpect(status().isUnauthorized());
  }
}