package at.ac.uibk.timeguess.timeflapp.bluetooth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tinyb.BluetoothManager;

/**
 * Bluetooth related Spring configuration. Cannot be used in test environment due to missing native
 * TinyB libraries and Bluetooth controller.
 */
@Configuration
@Profile("!test")
public class BluetoothConfig {

  @Bean
  public BluetoothManager bluetoothManager() {
    return BluetoothManager.getBluetoothManager();
  }
}
