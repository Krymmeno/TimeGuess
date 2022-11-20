package at.ac.uibk.timeguess.timeflapp.bluetooth;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tinyb.BluetoothDevice;
import tinyb.BluetoothException;
import tinyb.BluetoothManager;

@Component
@Profile("!test") // we don't want to run discovery on every integration test
public class DiscoveryRunner implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryRunner.class);

  private final BluetoothManager bluetoothManager;
  private final ConnectionManager connectionManager;
  private final String deviceName;
  private final Long secondsBetweenAttempts;

  public DiscoveryRunner(final BluetoothManager bluetoothManager,
      final ConnectionManager connectionManager,
      @Value("${timeflapp.timeflip.name}") final String deviceName,
      @Value("${timeflapp.discovery.interval:4}") final long secondsBetweenAttempts) {
    this.bluetoothManager = bluetoothManager;
    this.connectionManager = connectionManager;
    this.deviceName = deviceName;
    this.secondsBetweenAttempts = secondsBetweenAttempts;
  }

  /**
   * Runs discovery and triggers {@link ConnectionManager#connect(BluetoothDevice)} if a suitable
   * device is found.
   *
   * @throws InterruptedException if finding devices gets interrupted
   */
  @SuppressFBWarnings(value = "DM_EXIT")
  @Override
  public void run(final String... args) throws InterruptedException {
    final boolean discoveryStarted = bluetoothManager.startDiscovery();
    LOGGER.info("The discovery started: {}", discoveryStarted ? "true" : "false");

    final FindDevicesManager findDevicesManager = new FindDevicesManager(deviceName,
        secondsBetweenAttempts);
    final boolean findDevicesSuccess = findDevicesManager.findDevices(bluetoothManager);

    try {
      bluetoothManager.stopDiscovery();
    } catch (final BluetoothException e) {
      LOGGER.error("Discovery could not be stopped.");
    }

    LOGGER.info("All found devices:");
    bluetoothManager.getDevices()
        .forEach(d -> LOGGER.info("{} - {} ({})", d.getAddress(), d.getName(), d.getRSSI()));

    if (!findDevicesSuccess) {
      LOGGER.error("No {} devices found during discovery.", deviceName);
      System.exit(-1);
    }

    final BluetoothDevice device = findDevicesManager.getFoundDevices().iterator().next();
    LOGGER.info("Found {} device with address {} and RSSI {}", deviceName, device.getAddress(),
        device.getRSSI());

    connectionManager.connect(device);
  }

  /**
   * Stops discovery on shutdown.
   */
  @PreDestroy
  public void destroy() {
    if (bluetoothManager.getDiscovering()) {
      LOGGER.info("Stopping discovery due to shutdown");
      bluetoothManager.stopDiscovery();
    }
  }
}
