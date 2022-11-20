package at.ac.uibk.timeguess.flipflapp.timeflip;

import at.ac.uibk.timeguess.flipflapp.game.GameService;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TimeFlipService {

  public static final int SUM_FROM_1_TO_12 = 78;

  private final TimeFlipRepository timeFlipRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final GameRoomService gameRoomService;
  private final GameService gameService;

  private final Map<String, LocalDateTime> timeFlipUpdateTimes;
  private final Map<TimeFlip, Byte> timeFlipFacets;
  private final Map<TimeFlip, Byte> timeFlipBatteryLevels;

  public TimeFlipService(final TimeFlipRepository timeFlipRepository,
      GameRoomService gameRoomService,
      ApplicationEventPublisher applicationEventPublisher,
      GameService gameService) {
    this.timeFlipRepository = timeFlipRepository;
    this.gameRoomService = gameRoomService;
    this.applicationEventPublisher = applicationEventPublisher;
    this.gameService = gameService;
    this.timeFlipUpdateTimes = new ConcurrentHashMap<>();
    this.timeFlipFacets = new ConcurrentHashMap<>();
    this.timeFlipBatteryLevels = new ConcurrentHashMap<>();
  }

  /**
   * an event created when the new TimeFlipUpdate was recieved, containing the timeFlip and the new facet.
   *
   * @param timeFlip the TimeFlip which changed
   * @param facet the new facet
   */
  public void publishTimeFlipFacetChangeEvent(final TimeFlip timeFlip, Byte facet) {
    TimeFlipFacetChangeEvent timeFlipFacetChangeEvent = new TimeFlipFacetChangeEvent(this, timeFlip,
        facet);
    applicationEventPublisher.publishEvent(timeFlipFacetChangeEvent);
  }

  /**
   * upon recieving an update either register the timeflip as newly connected, or update the timeFlipUpdateTime.
   * If the recieved update differs from the last send update, create a publishTimeFlipFacetChangeEvent
   *
   * @param update the recieved TimeFlipUpdate
   * @return the timeflip that sent the upadte
   */
  public synchronized TimeFlip recognize(TimeFlipUpdate update) {
    Optional<TimeFlip> optTimeFlip = timeFlipRepository.findByDeviceAddress(update.deviceAddress());
    TimeFlip timeFlip;
    if (optTimeFlip.isPresent()) {
      timeFlip = optTimeFlip.get();
      timeFlipUpdateTimes.put(timeFlip.getDeviceAddress(), LocalDateTime.now());
      if (!update.connected()) {
        timeFlipUpdateTimes.remove(timeFlip.getDeviceAddress());
      }
    } else {
      timeFlipUpdateTimes.put(update.deviceAddress(), LocalDateTime.now());
      timeFlip = timeFlipRepository.save(new TimeFlip(update.deviceAddress(), update.deviceName()));
    }

    Byte facet = update.getFacet();
    Byte oldFacet = timeFlipFacets.get(timeFlip);

    if (facet != null) {
      facet = (byte) (((facet - 1) % 12) + 1); // TimeFlip could report facets greater than 12
      if (!Objects.equals(oldFacet, facet)) {
        timeFlipFacets.put(timeFlip, facet);
        publishTimeFlipFacetChangeEvent(timeFlip, facet);
      } else {
        timeFlipFacets.put(timeFlip, facet);
      }
    }

    if (update.getBatteryLevel() != null) {
      timeFlipBatteryLevels.put(timeFlip, update.getBatteryLevel());
    } else {
      timeFlipBatteryLevels.remove(timeFlip);
    }

    return timeFlip;
  }

  /**
   * Regularly checks for inactive TimeFlips. If there are such,<br>
   * - they will be removed from game rooms<br>
   * - all associated games will be aborted
   */
  @Scheduled(fixedDelay = 1000L)
  public void checkForInactiveTimeFlips() {
    final Stream<TimeFlip> inactiveTimeFlips = getAllTimeFlips().stream()
        .filter(timeFlip -> timeFlip.getStatus().equals(TimeFlipStatus.INACTIVE));

    inactiveTimeFlips.forEach(timeFlip -> {
      gameRoomService.getGameRoomsForTimeFlip(timeFlip).forEach(gameRoom -> {
        gameRoom.setTimeFlip(null);
        gameRoomService.sendGameRoomUpdate(gameRoom);
      });
      gameService.abortGamesAssociatedWithTimeFlip(timeFlip);
    });
  }


  /**
   * Get all TimeFlips with status
   *
   * @return all TimeFlips known to the System
   */
  public List<TimeFlip> getAllTimeFlips() {
    List<TimeFlip> timeFlips = timeFlipRepository.findAll();
    timeFlips.forEach(timeFlip -> {
      if (!timeFlipUpdateTimes.containsKey(timeFlip.getDeviceAddress())
          || timeFlipUpdateTimes.get(timeFlip.getDeviceAddress())
          .isBefore(LocalDateTime.now().minusMinutes(1))) {
        timeFlip.setStatus(TimeFlipStatus.INACTIVE);
      } else if (timeFlipUpdateTimes.get(timeFlip.getDeviceAddress())
          .isBefore(LocalDateTime.now().minusSeconds(20))) {
        timeFlip.setStatus(TimeFlipStatus.PENDING);
      } else {
        timeFlip.setStatus(TimeFlipStatus.ACTIVE);
      }
      timeFlip.setBatteryLevel(timeFlipBatteryLevels.get(timeFlip));
    });
    return timeFlips;
  }

  /**
   * Get all available TimeFlips
   *
   * @return all available TimeFlips
   */
  public List<TimeFlip> getAvailableTimeFlips() {
    return getAllTimeFlips().stream()
        .filter(timeFlip -> timeFlip.getStatus().equals(TimeFlipStatus.ACTIVE))
        .filter(
            timeFlip -> Objects.nonNull(timeFlip.getTimeFlipFacetMap()) && !timeFlip
                .getTimeFlipFacetMap().isEmpty())
        .filter(timeFlip -> gameRoomService.getAllGameRooms().stream()
            .noneMatch(gameRoom -> timeFlip.equals(gameRoom.getTimeFlip())))
        .toList();
  }

  /**
   * Calibrate the facets of a TimeFlip
   *
   * @param timeFlipId          id needed for finding the TimeFlip
   * @param newTimeFlipFacetMap map of new configuration per facet
   * @return update TimeFlip
   * @throws TimeFlipNotFoundException      if the TimeFlip was not found
   * @throws TimeFlipConfigurationException if the new configuration was not configured correctly
   */
  public TimeFlip calibrateTimeFlip(final Long timeFlipId,
      final Map<Byte, TimeFlipFacet> newTimeFlipFacetMap) {
    final TimeFlip timeFlip = timeFlipRepository.findById(timeFlipId)
        .orElseThrow(() -> new TimeFlipNotFoundException(timeFlipId));
    final Map<Byte, TimeFlipFacet> currentTimeFlipFacetMap = timeFlip.getTimeFlipFacetMap();
    newTimeFlipFacetMap.forEach((facet, timeFlipFacet) -> {
      if (facet < 1 || facet > 12) {
        throw new TimeFlipConfigurationException(
            "The TimeFlip with id %d does not have a facet with facet number %d."
                .formatted(timeFlipId, facet));
      }
      if (currentTimeFlipFacetMap.containsKey(facet)) {
        updateFacet(currentTimeFlipFacetMap.get(facet), timeFlipFacet);
      } else {
        currentTimeFlipFacetMap.put(facet, timeFlipFacet);
      }
    });
    /*
     * We take the sum from 1..12 corresponding to each facet, if the sum does not
     * match we know, that the user tried a wrong facet number, or maybe tried to
     * initialize a facet twice
     */
    final int sum = newTimeFlipFacetMap.keySet().stream().mapToInt(Byte::intValue).sum();
    if (newTimeFlipFacetMap.size() != 12 || sum != SUM_FROM_1_TO_12 || newTimeFlipFacetMap
        .containsValue(null)) {
      throw new TimeFlipConfigurationException(
          "The TimeFlip with id %d was not properly configured.".formatted(timeFlipId));
    }
    return timeFlipRepository.save(timeFlip);
  }

  private void updateFacet(final TimeFlipFacet oldFacet, final TimeFlipFacet newFacet) {
    oldFacet.setTime(newFacet.getTime());
    oldFacet.setActivity(newFacet.getActivity());
    oldFacet.setRoundPoints(newFacet.getRoundPoints());
  }
}
