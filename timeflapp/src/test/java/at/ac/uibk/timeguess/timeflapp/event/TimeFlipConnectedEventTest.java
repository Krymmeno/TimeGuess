package at.ac.uibk.timeguess.timeflapp.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tinyb.BluetoothDevice;

class TimeFlipConnectedEventTest {

  private static final String DEVICE_NAME = "MockTimeFlip";

  private BluetoothDevice device;
  private TimeFlipConnectedEvent timeFlipConnectedEvent;

  @BeforeEach
  void setUp() {
    device = mock(BluetoothDevice.class);
    when(device.getName()).thenReturn(DEVICE_NAME);
    timeFlipConnectedEvent = new TimeFlipConnectedEvent(this, device);
  }

  @Test
  void getDevice() {
    assertThat(timeFlipConnectedEvent.getDevice()).isEqualTo(device);
    assertThat(timeFlipConnectedEvent.getDevice()).isNotEqualTo(mock(BluetoothDevice.class));
  }

  @Test
  void testToString() {
    assertThat(timeFlipConnectedEvent.toString()).contains(DEVICE_NAME);
  }
}