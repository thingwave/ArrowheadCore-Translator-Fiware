package eu.arrowhead.core.certificate_authority.model;

public class CSResponse {

  private String signedPublicKey;
  private String issuerPublicKey;

  public CSResponse() {
  }

  public CSResponse(String signedPublicKey, String issuerPublicKey) {
    this.signedPublicKey = signedPublicKey;
    this.issuerPublicKey = issuerPublicKey;
  }

  public String getSignedPublicKey() {
    return signedPublicKey;
  }

  public void setSignedPublicKey(String signedPublicKey) {
    this.signedPublicKey = signedPublicKey;
  }

  public String getIssuerPublicKey() {
    return issuerPublicKey;
  }

  public void setIssuerPublicKey(String issuerPublicKey) {
    this.issuerPublicKey = issuerPublicKey;
  }
}
