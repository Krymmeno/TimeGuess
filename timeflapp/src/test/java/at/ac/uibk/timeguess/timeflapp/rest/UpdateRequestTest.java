package at.ac.uibk.timeguess.timeflapp.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tinyb.BluetoothDevice;

class UpdateRequestTest {

  private static final String DEVICE_ADDRESS = "0123456789";
  private static final String DEVICE_NAME = "MockTimeFlip";
  private static final Byte BATTERY_LEVEL = 95;
  private static final Byte FACET = 3;

  private UpdateRequest updateRequest;

  @BeforeEach
  void setUp() {
    final BluetoothDevice device = mock(BluetoothDevice.class);
    when(device.getAddress()).thenReturn(DEVICE_ADDRESS);
    when(device.getName()).thenReturn(DEVICE_NAME);
    updateRequest = new UpdateRequest(device, true, BATTERY_LEVEL, FACET);
  }

  @Test
  void getDeviceAddress() {
    assertThat(updateRequest.getDeviceAddress()).isEqualTo(DEVICE_ADDRESS);
  }

  @Test
  void getDeviceName() {
    assertThat(updateRequest.getDeviceName()).isEqualTo(DEVICE_NAME);
  }

  @Test
  void isConnected() {
    assertThat(updateRequest.isConnected()).isTrue();
  }

  @Test
  void getBatteryLevel() {
    assertThat(updateRequest.getBatteryLevel()).isEqualTo((byte) 95);
  }

  @Test
  void getFacet() {
    assertThat(updateRequest.getFacet()).isEqualTo((byte) 3);
  }

  @Test
  void testToString() {
    assertThat(updateRequest.toString())
        .contains("deviceAddress='" + DEVICE_ADDRESS + "'")
        .contains("deviceName='" + DEVICE_NAME + "'")
        .contains("connected=" + true)
        .contains("batteryLevel=" + BATTERY_LEVEL)
        .contains("facet=" + FACET);
  }
}