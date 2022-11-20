package at.ac.uibk.timeguess.timeflapp.bluetooth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

class DiscoveryRunnerTest {

  private static final String DEVICE_ADDRESS = "0123456789";
  private static final String DEVICE_NAME = "MockTimeFlip";

  private BluetoothDevice device;
  private BluetoothManager bluetoothManager;
  private ConnectionManager connectionManager;
  private DiscoveryRunner discoveryRunner;

  @BeforeEach
  void setUp() {
    device = mock(BluetoothDevice.class);
    when(device.getAddress()).thenReturn(DEVICE_ADDRESS);
    when(device.getName()).thenReturn(DEVICE_NAME);
    when(device.getRSSI()).thenReturn((short) -50);

    bluetoothManager = mock(BluetoothManager.class);
    connectionManager = mock(ConnectionManager.class);
    discoveryRunner = new DiscoveryRunner(bluetoothManager, connectionManager, DEVICE_NAME, 0);
  }

  @Test
  void run() throws Exception {
    when(bluetoothManager.startDiscovery()).thenReturn(true);
    when(bluetoothManager.getDevices()).thenReturn(Collections.singletonList(device));
    when(bluetoothManager.stopDiscovery()).thenReturn(true);
    discoveryRunner.run();
    verify(connectionManager).connect(device);
  }

  @Test
  void stopDiscoveryFailed() throws Exception {
    when(bluetoothManager.startDiscovery()).thenReturn(true);
    when(bluetoothManager.getDevices()).thenReturn(Collections.singletonList(device));
    when(bluetoothManager.stopDiscovery()).thenReturn(false);
    discoveryRunner.run();
    verify(connectionManager).connect(device);
  }

  @Test
  @ExpectSystemExitWithStatus(-1)
  void noDevices() throws Exception {
    when(bluetoothManager.startDiscovery()).thenReturn(true);
    when(bluetoothManager.getDevices()).thenReturn(Collections.emptyList());
    when(bluetoothManager.stopDiscovery()).thenReturn(true);
    discoveryRunner.run();
  }

  @Test
  void destroy() {
    when(bluetoothManager.getDiscovering()).thenReturn(true);
    discoveryRunner.destroy();
    verify(bluetoothManager).stopDiscovery();
  }
}