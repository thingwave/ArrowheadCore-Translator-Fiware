package eu.arrowhead.core.certificate_authority.model;

public class CertificateSigningRequest {

  private String distinguishedName;
  private String pemPublicKey;

  public CertificateSigningRequest() {
  }

  public CertificateSigningRequest(String distinguishedName, String pemPublicKey) {
    this.distinguishedName = distinguishedName;
    this.pemPublicKey = pemPublicKey;
  }

  public String getDistinguishedName() {
    return distinguishedName;
  }

  public void setDistinguishedName(String distinguishedName) {
    this.distinguishedName = distinguishedName;
  }

  public String getPemPublicKey() {
    return pemPublicKey;
  }

  public void setPemPublicKey(String pemPublicKey) {
    this.pemPublicKey = pemPublicKey;
  }
}
