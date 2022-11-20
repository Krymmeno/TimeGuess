package at.ac.uibk.timeguess.timeflapp.bluetooth;

import at.ac.uibk.timeguess.timeflapp.event.TimeFlipConnectedEvent;
import at.ac.uibk.timeguess.timeflapp.rest.UpdateSender;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tinyb.BluetoothDevice;

@Component
public class ConnectionManager {

  public static final int MAX_CONNECT_RETRIES = 5;

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

  private final ApplicationEventPublisher applicationEventPublisher;
  private final UpdateSender updateSender;

  private BluetoothDevice connectedDevice;

  public ConnectionManager(final ApplicationEventPublisher applicationEventPublisher,
      final UpdateSender updateSender) {
    this.applicationEventPublisher = applicationEventPublisher;
    this.updateSender = updateSender;
  }

  /**
   * Tries to connect to a device. If successful, the method publishes an event of type {@link
   * TimeFlipConnectedEvent}. If not, the application is terminated with status -1.
   *
   * @param device the device to connect to
   */
  @SuppressFBWarnings(value = "DM_EXIT")
  public void connect(final BluetoothDevice device) {
    int count = 0;
    while (true) {
      if (device.connect()) {
        LOGGER.info("Connection established");
        configureConnectedNotifications(device);
        connectedDevice = device;
        applicationEventPublisher.publishEvent(new TimeFlipConnectedEvent(this, connectedDevice));
        break;
      } else if (count <= MAX_CONNECT_RETRIES) {
        LOGGER.warn("Could not establish connection, retry");
        count++;
      } else {
        LOGGER.error("Could not establish connection");
        System.exit(-1);
      }
    }
  }

  private void configureConnectedNotifications(final BluetoothDevice device) {
    device.disableConnectedNotifications();
    device.enableConnectedNotifications(connected -> {
      if (!connected) {
        LOGGER.warn("Connection lost");
        System.exit(-1);
      }
    });
  }

  /**
   * Disconnects the device and sends an update to the server on shutdown.
   */
  @PreDestroy
  public void destroy() {
    if (connectedDevice != null) {
      LOGGER.info("Disconnecting from device {} due to shutdown", connectedDevice.getName());
      connectedDevice.disableConnectedNotifications();
      connectedDevice.disconnect();
      updateSender.sendUpdate(connectedDevice, false, null, null);
    }
  }

  /**
   * @return the currently connected device
   */
  public BluetoothDevice getConnectedDevice() {
    return connectedDevice;
  }
}
