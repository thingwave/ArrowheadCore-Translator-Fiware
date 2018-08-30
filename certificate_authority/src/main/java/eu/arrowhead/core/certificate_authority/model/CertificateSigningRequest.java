package eu.arrowhead.core.certificate_authority.model;

public class CertificateSigningRequest {

  private String encodedCert;

  public CertificateSigningRequest() {
  }

  public CertificateSigningRequest(String encodedCert) {
    this.encodedCert = encodedCert;
  }

  public String getEncodedCert() {
    return encodedCert;
  }

  public void setEncodedCert(String encodedCert) {
    this.encodedCert = encodedCert;
  }
}
