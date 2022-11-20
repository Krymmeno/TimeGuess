package at.ac.uibk.timeguess.timeflapp.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tinyb.BluetoothDevice;

public class UpdateRequest {

  private final String deviceAddress;
  private final String deviceName;
  private final boolean connected;

  @JsonInclude(Include.NON_NULL)
  private final Byte batteryLevel;

  @JsonInclude(Include.NON_NULL)
  private final Byte facet;

  public UpdateRequest(final BluetoothDevice device, final boolean connected,
      final Byte batteryLevel, final Byte facet) {
    this.deviceAddress = device.getAddress();
    this.deviceName = device.getName();
    this.connected = connected;
    this.batteryLevel = batteryLevel;
    this.facet = facet;
  }

  public String getDeviceAddress() {
    return deviceAddress;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public boolean isConnected() {
    return connected;
  }

  public Byte getBatteryLevel() {
    return batteryLevel;
  }

  public Byte getFacet() {
    return facet;
  }

  @Override
  public String toString() {
    return "UpdateRequest{"
        + "deviceAddress='" + deviceAddress + '\''
        + ", deviceName='" + deviceName + '\''
        + ", connected=" + connected
        + ", batteryLevel=" + batteryLevel
        + ", facet=" + facet
        + '}';
  }
}
