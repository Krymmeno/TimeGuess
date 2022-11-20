package at.ac.uibk.timeguess.timeflapp.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.context.ApplicationEvent;
import tinyb.BluetoothDevice;

public class TimeFlipConnectedEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  @SuppressFBWarnings(value = "SE_BAD_FIELD")
  private final BluetoothDevice device;

  public TimeFlipConnectedEvent(final Object source, final BluetoothDevice device) {
    super(source);
    this.device = device;
  }

  public BluetoothDevice getDevice() {
    return device;
  }

  @Override
  public String toString() {
    return String.format("Connected to %s", device.getName());
  }
}
