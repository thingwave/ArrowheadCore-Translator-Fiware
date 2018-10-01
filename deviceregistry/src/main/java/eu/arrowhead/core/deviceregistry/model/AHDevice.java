/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.deviceregistry.model;

public class AHDevice {

  private DeviceInformation info;

  public AHDevice(DeviceInformation info) {
    this.info = info;
  }

  public DeviceInformation getInfo() {
    return this.info;
  }

  public void setInfo(DeviceInformation info) {
    this.info = info;
  }

  public String toString() {
    return info.toString();
  }
}
