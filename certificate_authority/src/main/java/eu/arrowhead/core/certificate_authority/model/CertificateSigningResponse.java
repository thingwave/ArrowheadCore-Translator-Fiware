package eu.arrowhead.core.certificate_authority.model;

import java.security.cert.X509Certificate;

public class CertificateSigningResponse {

  private String pemSignedCert;
  private X509Certificate test;

  public CertificateSigningResponse() {
  }

  public CertificateSigningResponse(String pemSignedCert) {
    this.pemSignedCert = pemSignedCert;
  }

  public CertificateSigningResponse(String pemSignedCert, X509Certificate test) {
    this.pemSignedCert = pemSignedCert;
    this.test = test;
  }

  public String getPemSignedCert() {
    return pemSignedCert;
  }

  public void setPemSignedCert(String pemSignedCert) {
    this.pemSignedCert = pemSignedCert;
  }

  public X509Certificate getTest() {
    return test;
  }

  public void setTest(X509Certificate test) {
    this.test = test;
  }
}
