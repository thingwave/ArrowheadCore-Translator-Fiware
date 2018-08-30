package eu.arrowhead.core.certificate_authority.model;

public class CertificateSigningResponse {

  private String pemSignedCert;

  public CertificateSigningResponse() {
  }

  public CertificateSigningResponse(String pemSignedCert) {
    this.pemSignedCert = pemSignedCert;
  }

  public String getPemSignedCert() {
    return pemSignedCert;
  }

  public void setPemSignedCert(String pemSignedCert) {
    this.pemSignedCert = pemSignedCert;
  }
}
