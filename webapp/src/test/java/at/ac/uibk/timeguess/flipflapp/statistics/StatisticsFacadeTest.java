package at.ac.uibk.timeguess.flipflapp.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@AutoConfigureMockMvc
@Transactional
public class StatisticsFacadeTest {

  @Autowired
  private StatisticsFacade statisticsFacade;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void getUserStatistics() {
    final User user = userRepository.findByUsername(RANDOM_PLAYER.USERNAME).orElseThrow();
    final UserStatistics userStatistics = statisticsFacade.getUserStatistics(user.getUserId());
    assertThat(userStatistics).isNotNull();
    assertThat(userStatistics.getWonGames()).isGreaterThanOrEqualTo(0);
    assertThat(userStatistics.getLostGames()).isGreaterThanOrEqualTo(0);
    assertThat(userStatistics.getWonRounds()).isGreaterThanOrEqualTo(0);
    assertThat(userStatistics.getLostRounds()).isGreaterThanOrEqualTo(0);
    assertThat(userStatistics.getAverageExplanationTime()).isNull();
    assertThat(userStatistics.getFastestExplanationTime()).isNull();
    assertThat(userStatistics.getTeammates().size()).isGreaterThanOrEqualTo(0);
  }

  @Test
  public void getUserStatisticsForNotExistentUser() {
    assertThat(userRepository.findById(-1L)).isEmpty();
    assertThatExceptionOfType(UserNotFoundException.class)
        .isThrownBy(() -> statisticsFacade.getUserStatistics(-1L));
  }

  @Test
  public void getGameStatistics() {
    final GameStatistics gameStatistics = statisticsFacade.getGameStatistics();
    assertThat(gameStatistics).isNotNull();
    assertThat(gameStatistics.getAllGames()).isGreaterThanOrEqualTo(0);
    assertThat(gameStatistics.getAllRegisteredUsers()).isGreaterThanOrEqualTo(0);
    assertThat(gameStatistics.getAllActiveUsers()).isGreaterThanOrEqualTo(0);
    assertThat(gameStatistics.getGameRoundsPerDay()).isNotEmpty();
    assertThat(gameStatistics.getGamesPerTopic()).isNotEmpty();
    assertThat(gameStatistics.getAllTermsGuessed()).isGreaterThanOrEqualTo(0);
    assertThat(gameStatistics.getTermsGuessedCorrectlyPerTopic()).isNotEmpty();
    assertThat(gameStatistics.getTermsGuessedWronglyPerTopic()).isNotEmpty();
    assertThat(gameStatistics.getGamesWonPerUser()).isNotEmpty();
  }

}
