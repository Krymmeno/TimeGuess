package at.ac.uibk.timeguess.flipflapp.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAMEMANAGER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@AutoConfigureMockMvc
@Transactional
public class StatisticsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void getGameStatistics() throws Exception {
    mockMvc.perform(get("/api/statistics/games")
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isOk());
  }

  @Test
  public void getGameStatisticsAsPlayer() throws Exception {
    mockMvc.perform(get("/api/statistics/games")
        .with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME))))
        .andExpect(status().isOk());
  }

  @Test
  public void getUserStatistics() throws Exception {
    final User user = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();
    mockMvc.perform(get("/api/statistics/users/%d".formatted(user.getUserId()))
        .with(user(userDetailsService.loadUserByUsername(user.getUsername()))))
        .andExpect(status().isOk());
  }

  @Test
  public void getUserStatisticsForDifferentUser() throws Exception {
    final User gameManager = userRepository.findByUsername(RANDOM_GAMEMANAGER.USERNAME)
        .orElseThrow();
    final User user = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();
    mockMvc.perform(get("/api/statistics/users/%d".formatted(gameManager.getUserId()))
        .with(user(userDetailsService.loadUserByUsername(user.getUsername()))))
        .andExpect(status().isOk());
  }

  @Test
  public void getUserStatisticsForNotExistingUser() throws Exception {
    assertThat(userRepository.findById(-1L)).isEmpty();
    mockMvc.perform(get("/api/statistics/users/%d".formatted(-1))
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME))))
        .andExpect(status().isNotFound());
  }
}
