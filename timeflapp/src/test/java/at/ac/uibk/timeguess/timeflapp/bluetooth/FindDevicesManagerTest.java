package at.ac.uibk.timeguess.timeflapp.bluetooth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

public class FindDevicesManagerTest {

  @Test
  public void testFindDevices() throws InterruptedException {
    final BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
    final List<BluetoothDevice> mockDevices = utilMockDevices();
    when(bluetoothManager.getDevices()).thenReturn(mockDevices);

    final FindDevicesManager findDevicesManager = new FindDevicesManager("TimeFlip", 0);
    assertNotNull(findDevicesManager);

    final boolean findDevicesSuccess = findDevicesManager.findDevices(bluetoothManager);
    assertTrue(findDevicesSuccess);

    final Set<BluetoothDevice> devices = findDevicesManager.getFoundDevices();
    assertNotNull(devices);
    assertEquals(2, devices.size());

    for (final BluetoothDevice device : devices) {
      if (device.getName().equals("timeflip")) {
        assertEquals("A8:A8:9F:B9:28:AD", device.getAddress());
        assertEquals(-20, device.getRSSI());
      } else if (device.getName().equals("TimeFlip2")) {
        assertEquals("B8:A8:9F:B9:28:AD", device.getAddress());
        assertEquals(-44, device.getRSSI());
      } else {
        fail("Unexpected device " + device.getName());
      }
    }
  }

  @Test
  public void testFindDevicesNoSearchDeviceNameFilter() throws InterruptedException {
    final BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
    final List<BluetoothDevice> mockDevices = utilMockDevices();
    when(bluetoothManager.getDevices()).thenReturn(mockDevices);

    final FindDevicesManager findDevicesManager = new FindDevicesManager("", 0);
    assertNotNull(findDevicesManager);
    assertTrue(findDevicesManager.findDevices(bluetoothManager));
    final Set<BluetoothDevice> devices = findDevicesManager.getFoundDevices();
    assertNotNull(devices);
    assertEquals(3, devices.size());
  }

  @Test
  public void testFindDevicesNull() throws InterruptedException {
    final BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
    when(bluetoothManager.getDevices()).thenReturn(null);

    final FindDevicesManager findDevicesManager = new FindDevicesManager("TimeFlip", 0);
    assertNotNull(findDevicesManager);
    assertFalse(findDevicesManager.findDevices(bluetoothManager));
  }

  @Test
  public void testFindDevicesFoundAfterSomeTime() throws InterruptedException {
    final BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
    final List<BluetoothDevice> mockDevices = utilMockDevices();
    when(bluetoothManager.getDevices())
        .thenReturn(Collections.emptyList())
        .thenReturn(Collections.emptyList())
        .thenReturn(mockDevices);

    final FindDevicesManager findDevicesManager = new FindDevicesManager("TimeFlip", 0);
    assertNotNull(findDevicesManager);
    assertTrue(findDevicesManager.findDevices(bluetoothManager));
  }

  @Test
  public void testFindDevicesFailed() throws InterruptedException {
    final BluetoothManager bluetoothManager = Mockito.mock(BluetoothManager.class);
    final List<BluetoothDevice> mockDevices = utilMockDevices();
    when(bluetoothManager.getDevices()).thenReturn(mockDevices);

    final FindDevicesManager findDevicesManager1 = new FindDevicesManager("headphones1", 0);
    assertNotNull(findDevicesManager1);
    assertFalse(findDevicesManager1.findDevices(bluetoothManager));
    assertNotNull(findDevicesManager1.getFoundDevices());

    final FindDevicesManager findDevicesManager2 = new FindDevicesManager("timeflip3", 0);
    assertNotNull(findDevicesManager2);
    assertFalse(findDevicesManager2.findDevices(bluetoothManager));
    assertNotNull(findDevicesManager2.getFoundDevices());

    final FindDevicesManager findDevicesManager3 = new FindDevicesManager("timeflip4", 0);
    assertNotNull(findDevicesManager3);
    assertFalse(findDevicesManager3.findDevices(bluetoothManager));
    assertNotNull(findDevicesManager3.getFoundDevices());
  }

  private List<BluetoothDevice> utilMockDevices() {
    final List<BluetoothDevice> mockDevices = new ArrayList<>();

    final BluetoothDevice mockTimeFlip1 = Mockito.mock(BluetoothDevice.class);
    when(mockTimeFlip1.getName()).thenReturn("timeflip");
    when(mockTimeFlip1.getAddress()).thenReturn("A8:A8:9F:B9:28:AD");
    when(mockTimeFlip1.getRSSI()).thenReturn((short) -20);
    mockDevices.add(mockTimeFlip1);

    final BluetoothDevice mockTimeFlip2 = Mockito.mock(BluetoothDevice.class);
    when(mockTimeFlip2.getName()).thenReturn("TimeFlip2");
    when(mockTimeFlip2.getAddress()).thenReturn("B8:A8:9F:B9:28:AD");
    when(mockTimeFlip2.getRSSI()).thenReturn((short) -44);
    mockDevices.add(mockTimeFlip2);

    final BluetoothDevice mockTimeFlip3 = Mockito.mock(BluetoothDevice.class);
    when(mockTimeFlip3.getName()).thenReturn("timeflip3");
    when(mockTimeFlip3.getAddress()).thenReturn("C8:A8:9F:B9:28:AD");
    when(mockTimeFlip3.getRSSI()).thenReturn((short) -91);
    mockDevices.add(mockTimeFlip3);

    final BluetoothDevice mockTimeFlip4 = Mockito.mock(BluetoothDevice.class);
    when(mockTimeFlip4.getName()).thenReturn("timeflip4");
    when(mockTimeFlip4.getAddress()).thenReturn("D8:A8:9F:B9:28:AD");
    when(mockTimeFlip4.getRSSI()).thenReturn((short) 0);
    mockDevices.add(mockTimeFlip4);

    final BluetoothDevice mockHeadphones = Mockito.mock(BluetoothDevice.class);
    when(mockHeadphones.getName()).thenReturn("headphones");
    when(mockHeadphones.getAddress()).thenReturn("D8:A8:9F:B9:28:AD");
    when(mockHeadphones.getRSSI()).thenReturn((short) -35);
    mockDevices.add(mockHeadphones);

    return mockDevices;
  }
}
