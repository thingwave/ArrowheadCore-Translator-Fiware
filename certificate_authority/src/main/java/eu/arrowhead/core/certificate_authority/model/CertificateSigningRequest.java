package eu.arrowhead.core.certificate_authority.model;

public class CertificateSigningRequest {

  //TODO complex regex expression for validity check
  //Valid parts: CN, L, ST, O, Ou, C and possibly others like E for email? CN must be mandatory, everything else optional
  private String distinguishedName;
  //X509 cert public key bytes Base64 encoded
  private String encodedPublicKey;

  public CertificateSigningRequest() {
  }

  public CertificateSigningRequest(String distinguishedName, String encodedPublicKey) {
    this.distinguishedName = distinguishedName;
    this.encodedPublicKey = encodedPublicKey;
  }

  public String getDistinguishedName() {
    return distinguishedName;
  }

  public void setDistinguishedName(String distinguishedName) {
    this.distinguishedName = distinguishedName;
  }

  public String getEncodedPublicKey() {
    return encodedPublicKey;
  }

  public void setEncodedPublicKey(String encodedPublicKey) {
    this.encodedPublicKey = encodedPublicKey;
  }
}
