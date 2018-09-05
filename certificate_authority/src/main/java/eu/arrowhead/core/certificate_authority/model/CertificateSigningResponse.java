/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.certificate_authority.model;

public class CertificateSigningResponse {

  private String pemSignedCert;
  private String pemIntermediateCert;
  private String pemRootCert;

  public CertificateSigningResponse() {
  }

  public CertificateSigningResponse(String pemSignedCert) {
    this.pemSignedCert = pemSignedCert;
  }

  public CertificateSigningResponse(String pemSignedCert, String pemIntermediateCert, String pemRootCert) {
    this.pemSignedCert = pemSignedCert;
    this.pemIntermediateCert = pemIntermediateCert;
    this.pemRootCert = pemRootCert;
  }

  public String getPemSignedCert() {
    return pemSignedCert;
  }

  public void setPemSignedCert(String pemSignedCert) {
    this.pemSignedCert = pemSignedCert;
  }

  public String getPemIntermediateCert() {
    return pemIntermediateCert;
  }

  public void setPemIntermediateCert(String pemIntermediateCert) {
    this.pemIntermediateCert = pemIntermediateCert;
  }

  public String getPemRootCert() {
    return pemRootCert;
  }

  public void setPemRootCert(String pemRootCert) {
    this.pemRootCert = pemRootCert;
  }
}
