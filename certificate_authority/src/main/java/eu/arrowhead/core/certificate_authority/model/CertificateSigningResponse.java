package eu.arrowhead.core.certificate_authority.model;

public class CertificateSigningResponse {

  private String pemEncodedSignedCert;

  public CertificateSigningResponse() {
  }

  public CertificateSigningResponse(String pemEncodedSignedCert) {
    this.pemEncodedSignedCert = pemEncodedSignedCert;
  }

  public String getPemEncodedSignedCert() {
    return pemEncodedSignedCert;
  }

  public void setPemEncodedSignedCert(String pemEncodedSignedCert) {
    this.pemEncodedSignedCert = pemEncodedSignedCert;
  }
}
