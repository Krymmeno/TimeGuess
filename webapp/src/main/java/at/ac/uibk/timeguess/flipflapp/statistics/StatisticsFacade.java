package at.ac.uibk.timeguess.flipflapp.statistics;

import at.ac.uibk.timeguess.flipflapp.user.exception.UserNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class StatisticsFacade {

  private final UserStatisticsService userStatisticsService;

  private final GameStatisticsService gameStatisticsService;

  public StatisticsFacade(final UserStatisticsService userStatisticsService,
      final GameStatisticsService gameStatisticsService) {
    this.userStatisticsService = userStatisticsService;
    this.gameStatisticsService = gameStatisticsService;
  }

  /**
   * Returns different statistics concerning the user
   *
   * @param userId id needed for finding the user
   * @return statistics about a user
   * @throws UserNotFoundException if the user was not found
   */
  public UserStatistics getUserStatistics(final Long userId) {
    return userStatisticsService.getUserStatistics(userId);
  }

  /**
   * Returns different statistics concerning the overall game
   *
   * @return statistics about the game
   */
  public GameStatistics getGameStatistics() {
    return gameStatisticsService.getGameStatistics();
  }

}
