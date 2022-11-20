package at.ac.uibk.timeguess.timeflapp.bluetooth;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

/**
 * A class for found Bluetooth devices
 */
public class FindDevicesManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(FindDevicesManager.class);

  public static final int MIN_RSSI_ALLOWED = -80;

  private static final int ATTEMPTS_TO_FIND = 5;

  private final String searchDevice;
  private final long secondsBetweenAttempts;

  private final Set<BluetoothDevice> foundDevices = new HashSet<>();

  public FindDevicesManager(final String searchDeviceName, final long secondsBetweenAttempts) {
    Preconditions.checkNotNull(searchDeviceName,
        "Precondition violation - argument 'searchDeviceName' must not be NULL!");
    this.searchDevice = searchDeviceName;
    this.secondsBetweenAttempts = secondsBetweenAttempts;
  }

  /**
   * Search for Bluetooth devices until either at least one device could be found or until we
   * reached the maximum number of attempts. We sleep a certain amount of time after each attempt.
   * We need to we search long enough otherwise we may not find the device even though it is there.
   *
   * @param manager the Bluetooth manager from which we get the Bluetooth devices
   * @return true if at least one device could be found
   * @throws InterruptedException if sleep between attempts gets interrupted
   */
  public boolean findDevices(final BluetoothManager manager) throws InterruptedException {
    Preconditions
        .checkNotNull(manager, "Precondition violation - argument 'manager' must not be NULL!");

    for (int i = 0; i < ATTEMPTS_TO_FIND; i++) {
      final List<BluetoothDevice> devices = manager.getDevices();
      if (devices != null) {
        for (final BluetoothDevice device : devices) {
          checkDevice(device);
        }
      }
      if (!this.foundDevices.isEmpty()) {
        return true;
      }
      TimeUnit.SECONDS.sleep(secondsBetweenAttempts);
    }
    return false;
  }

  /**
   * Check if the provided device is one we are searching for and add it to found devices Filter
   * based on searchDeviceName and RSSI signal to ensure that we connect to the correct device and
   * to prevent unstable communication.
   *
   * @param device the device to check
   */
  private void checkDevice(final BluetoothDevice device) {
    Preconditions
        .checkNotNull(device, "Precondition violation - argument 'device' must not be NULL!");

    if (device.getName().toLowerCase(Locale.ROOT)
        .contains(this.searchDevice.toLowerCase(Locale.ROOT))) {
      final int rssi = device.getRSSI();
      if (rssi == 0) {
        LOGGER.info("{} with address {} has no signal.", this.searchDevice, device.getAddress());
      } else if (rssi < MIN_RSSI_ALLOWED) {
        LOGGER.info("{} with address {} has a very low signal ({})", this.searchDevice,
            device.getAddress(), rssi);
      } else {
        this.foundDevices.add(device);
      }
    }
  }

  public Set<BluetoothDevice> getFoundDevices() {
    return Collections.unmodifiableSet(this.foundDevices);
  }
}
