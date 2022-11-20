package at.ac.uibk.timeguess.timeflapp.rest;

import at.ac.uibk.timeguess.timeflapp.bluetooth.CharacteristicsManager;
import at.ac.uibk.timeguess.timeflapp.bluetooth.ConnectionManager;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipConfiguredEvent;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipValueNotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Handles updates/notifications published by Bluetooth related classes.
 */
@Component
public class UpdateHandler {

  private static final long PERIODIC_UPDATE_RATE_IN_MILLISECONDS = 10_000L;

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHandler.class);

  private final UpdateSender updateSender;
  private final ConnectionManager connectionManager;
  private final CharacteristicsManager characteristicsManager;

  public UpdateHandler(final UpdateSender updateSender,
      final ConnectionManager connectionManager,
      final CharacteristicsManager characteristicsManager) {
    this.updateSender = updateSender;
    this.connectionManager = connectionManager;
    this.characteristicsManager = characteristicsManager;
  }

  /**
   * Sends periodic updates to the server.
   */
  @Scheduled(fixedRate = PERIODIC_UPDATE_RATE_IN_MILLISECONDS)
  public void periodicUpdate() {
    if (connectionManager.getConnectedDevice() != null) {
      LOGGER.debug("Sending periodic update");
      updateSender.sendUpdate(connectionManager.getConnectedDevice(), true,
          characteristicsManager.getBatteryLevel(), characteristicsManager.getFacet());
    }
  }

  /**
   * Reacts to {@link TimeFlipValueNotificationEvent} and sends an update to the server.
   *
   * @param valueNotificationEvent the event that was published after the value changed
   */
  @EventListener
  public void handleValueNotification(final TimeFlipValueNotificationEvent valueNotificationEvent) {
    LOGGER.debug("Sending update after {} notification", valueNotificationEvent.getType());
    updateSender.sendUpdate(connectionManager.getConnectedDevice(), true,
        characteristicsManager.getBatteryLevel(), characteristicsManager.getFacet());
  }

  /**
   * Reacts to {@link TimeFlipConfiguredEvent} and sends an update to the server.
   *
   * @param configuredEvent the event that was published after device configuration was done
   */
  @EventListener
  public void handleConfigured(final TimeFlipConfiguredEvent configuredEvent) {
    LOGGER.debug("Sending update after configure");
    updateSender
        .sendUpdate(configuredEvent.getDevice(), true, characteristicsManager.getBatteryLevel(),
            characteristicsManager.getFacet());
  }
}
