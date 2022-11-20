package at.ac.uibk.timeguess.timeflapp.bluetooth;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tinyb.BluetoothManager;

@Configuration
@Profile("test")
public class TestBluetoothConfig {

  @Bean
  public BluetoothManager bluetoothManager() {
    return mock(BluetoothManager.class);
  }
}
