package at.ac.uibk.timeguess.timeflapp.bluetooth;

import at.ac.uibk.timeguess.timeflapp.event.TimeFlipConfiguredEvent;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipConnectedEvent;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipValueNotificationEvent;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipValueNotificationEvent.Type;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;

@Component
public class CharacteristicsManager {

  public static final String BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb";
  public static final String BATTERY_LEVEL_CHARACTERISTIC_UUID = "00002a19-0000-1000-8000-00805f9b34fb";

  public static final String TIME_FLIP_SERVICE_UUID = "f1196f50-71a4-11e6-bdf4-0800200c9a66";
  public static final String PASSWORD_CHARACTERISTIC_UUID = "f1196f57-71a4-11e6-bdf4-0800200c9a66";
  public static final String FACETS_CHARACTERISTIC_UUID = "f1196f52-71a4-11e6-bdf4-0800200c9a66";

  private static final Logger LOGGER = LoggerFactory.getLogger(CharacteristicsManager.class);

  private final ApplicationEventPublisher applicationEventPublisher;
  private final String devicePassword;

  private BluetoothGattCharacteristic batteryLevelCharacteristic;
  private BluetoothGattCharacteristic passwordCharacteristic;
  private BluetoothGattCharacteristic facetsCharacteristic;

  private Byte batteryLevel;
  private Byte facet;

  public CharacteristicsManager(final ApplicationEventPublisher applicationEventPublisher,
      @Value("${timeflapp.timeflip.password}") final String devicePassword) {
    this.applicationEventPublisher = applicationEventPublisher;
    this.devicePassword = devicePassword;
  }

  /**
   * Reacts to {@link TimeFlipConnectedEvent}, initializes characteristics, reads initial values and
   * configures notifications.
   *
   * @param connectedEvent the event that was published after connect
   */
  @SuppressFBWarnings(value = "DM_EXIT")
  @EventListener
  public void handleConnected(final TimeFlipConnectedEvent connectedEvent) {
    try {
      LOGGER.info("Configuring device");
      final BluetoothGattService batteryService = connectedEvent.getDevice()
          .find(BATTERY_SERVICE_UUID);
      initBatteryLevel(batteryService);
      final BluetoothGattService timeFlipService = connectedEvent.getDevice()
          .find(TIME_FLIP_SERVICE_UUID);
      writePassword(timeFlipService);
      initFacet(timeFlipService);
      applicationEventPublisher
          .publishEvent(new TimeFlipConfiguredEvent(this, connectedEvent.getDevice()));
    } catch (final Exception e) {
      LOGGER.error("Error configuring device");
      System.exit(-1);
    }
    LOGGER.info("Done configuring device");
  }

  private void initBatteryLevel(final BluetoothGattService batteryService) {
    batteryLevelCharacteristic = batteryService.find(BATTERY_LEVEL_CHARACTERISTIC_UUID);
    batteryLevel = batteryLevelCharacteristic.readValue()[0];
    if (batteryLevelCharacteristic.getNotifying()) {
      batteryLevelCharacteristic.disableValueNotifications();
      LOGGER.info("Disabled previous battery level characteristic notifications");
    }
    batteryLevelCharacteristic.enableValueNotifications(data -> {
      batteryLevel = data[0];
      LOGGER.info("Battery level: {}", batteryLevel);
      applicationEventPublisher.publishEvent(
          new TimeFlipValueNotificationEvent(this, Type.BATTERY_LEVEL));
    });
  }

  private void writePassword(final BluetoothGattService timeFlipService) {
    passwordCharacteristic = timeFlipService.find(PASSWORD_CHARACTERISTIC_UUID);
    passwordCharacteristic.writeValue(StandardCharsets.US_ASCII.encode(devicePassword).array());
  }

  private void initFacet(final BluetoothGattService timeFlipService) {
    facetsCharacteristic = timeFlipService.find(FACETS_CHARACTERISTIC_UUID);
    facet = facetsCharacteristic.readValue()[0];
    if (facetsCharacteristic.getNotifying()) {
      facetsCharacteristic.disableValueNotifications();
      LOGGER.info("Disabled previous facets characteristic notifications");
    }
    facetsCharacteristic.enableValueNotifications(data -> {
      facet = data[0];
      LOGGER.info("Facet: {}", facet);
      applicationEventPublisher.publishEvent(
          new TimeFlipValueNotificationEvent(this, TimeFlipValueNotificationEvent.Type.FACET));
    });
  }

  /**
   * @return the current battery level in percent
   */
  public Byte getBatteryLevel() {
    return batteryLevel;
  }

  /**
   * @return the current facet
   */
  public Byte getFacet() {
    return facet;
  }
}