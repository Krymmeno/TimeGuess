package at.ac.uibk.timeguess.timeflapp.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tinyb.BluetoothDevice;

class TimeFlipConfiguredEventTest {

  private static final String DEVICE_NAME = "MockTimeFlip";

  private BluetoothDevice device;
  private TimeFlipConfiguredEvent timeFlipConfiguredEvent;

  @BeforeEach
  void setUp() {
    device = mock(BluetoothDevice.class);
    when(device.getName()).thenReturn(DEVICE_NAME);
    timeFlipConfiguredEvent = new TimeFlipConfiguredEvent(this, device);
  }

  @Test
  void getDevice() {
    assertThat(timeFlipConfiguredEvent.getDevice()).isEqualTo(device);
    assertThat(timeFlipConfiguredEvent.getDevice()).isNotEqualTo(mock(BluetoothDevice.class));
  }

  @Test
  void testToString() {
    assertThat(timeFlipConfiguredEvent.toString()).contains(DEVICE_NAME);
  }
}