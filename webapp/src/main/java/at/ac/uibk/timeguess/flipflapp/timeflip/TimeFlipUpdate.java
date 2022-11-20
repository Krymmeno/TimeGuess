package at.ac.uibk.timeguess.flipflapp.timeflip;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@SuppressFBWarnings(value = "EQ_UNUSUAL")
public record TimeFlipUpdate(@NotBlank String deviceAddress, @NotBlank String deviceName,
                             @NotNull Boolean connected, Byte batteryLevel, Byte facet) {

  /*
   * Some libraries don't work well with Records yet, so we still need classic Getters.
   */

  public String getDeviceAddress() {
    return deviceAddress;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public Boolean getConnected() {
    return connected;
  }

  public Byte getBatteryLevel() {
    return batteryLevel;
  }

  public Byte getFacet() {
    return facet;
  }
}
