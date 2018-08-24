/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "broker", uniqueConstraints = {@UniqueConstraint(columnNames = {"broker_name"})})
public class Broker {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotBlank
  @Column(name = "broker_name")
  @Size(max = 255, message = "Broker name must be 255 character at max")
  private String brokerName;

  @NotBlank
  @Size(min = 3, max = 255, message = "Address must be between 3 and 255 characters")
  private String address;

  @Min(value = 1, message = "Port can not be less than 1")
  @Max(value = 65535, message = "Port can not be greater than 65535")
  private Integer port;

  @Column(name = "is_secure")
  @Type(type = "yes_no")
  private Boolean secure = false;

  @Column(name = "authentication_info")
  @Size(max = 2047, message = "Authentication information must be 2047 character at max")
  private String authenticationInfo;

  public Broker() {
  }

  public Broker(String brokerName, String address, int port, boolean secure, String authenticationInfo) {
    this.brokerName = brokerName;
    this.address = address;
    this.port = port;
    this.secure = secure;
    this.authenticationInfo = authenticationInfo;
  }

  public Long getId() {
    return id;
  }

  public String getBrokerName() {
    return brokerName;
  }

  public void setBrokerName(String brokerName) {
    this.brokerName = brokerName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public Boolean isSecure() {
    return secure;
  }

  public void setSecure(Boolean secure) {
    this.secure = secure;
  }

  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Broker)) {
      return false;
    }

    Broker broker = (Broker) o;

    if (brokerName != null ? !brokerName.equals(broker.brokerName) : broker.brokerName != null) {
      return false;
    }
    if (address != null ? !address.equals(broker.address) : broker.address != null) {
      return false;
    }
    if (port != null ? !port.equals(broker.port) : broker.port != null) {
      return false;
    }
    return secure != null ? secure.equals(broker.secure) : broker.secure == null;
  }

  @Override
  public int hashCode() {
    int result = brokerName != null ? brokerName.hashCode() : 0;
    result = 31 * result + (address != null ? address.hashCode() : 0);
    result = 31 * result + (port != null ? port.hashCode() : 0);
    result = 31 * result + (secure != null ? secure.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Broker{");
    sb.append(" brokerName = ").append(brokerName);
    sb.append(", address = ").append(address);
    sb.append(", port = ").append(port);
    sb.append(", secure = ").append(secure);
    sb.append('}');
    return sb.toString();
  }
}
