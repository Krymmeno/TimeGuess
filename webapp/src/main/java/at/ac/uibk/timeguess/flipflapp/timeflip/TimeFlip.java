package at.ac.uibk.timeguess.flipflapp.timeflip;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

@Entity
public class TimeFlip {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long timeFlipId;

  @NotBlank
  @Column(unique = true)
  private String deviceAddress;

  @NotBlank
  private String deviceName;

  @Transient
  @JsonInclude(Include.NON_NULL)
  private TimeFlipStatus status;

  @Transient
  @JsonInclude(Include.NON_NULL)
  private Byte batteryLevel;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private final Map<Byte, TimeFlipFacet> timeFlipFacetMap = new HashMap<>();

  public TimeFlip() {
  }

  public TimeFlip(String deviceAddress, String deviceName) {
    this.deviceAddress = deviceAddress;
    this.deviceName = deviceName;
  }

  public String getDeviceAddress() {
    return deviceAddress;
  }

  public void setDeviceAddress(String deviceAddress) {
    this.deviceAddress = deviceAddress;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public TimeFlipStatus getStatus() {
    return status;
  }

  public void setStatus(TimeFlipStatus status) {
    this.status = status;
  }

  public Byte getBatteryLevel() {
    return batteryLevel;
  }

  public void setBatteryLevel(Byte batteryLevel) {
    this.batteryLevel = batteryLevel;
  }

  public Long getTimeFlipId() {
    return timeFlipId;
  }

  public Map<Byte, TimeFlipFacet> getTimeFlipFacetMap() {
    return timeFlipFacetMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TimeFlip timeFlip = (TimeFlip) o;

    return Objects.equals(deviceAddress, timeFlip.deviceAddress);
  }

  @Override
  public int hashCode() {
    return deviceAddress != null ? deviceAddress.hashCode() : 0;
  }
}
