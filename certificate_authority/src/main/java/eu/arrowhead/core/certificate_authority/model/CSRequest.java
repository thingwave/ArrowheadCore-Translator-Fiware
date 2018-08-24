package eu.arrowhead.core.certificate_authority.model;

public class CSRequest {

  private String encodedPublicKey;

  public CSRequest() {
  }

  public CSRequest(String encodedPublicKey) {
    this.encodedPublicKey = encodedPublicKey;
  }

  public String getEncodedPublicKey() {
    return encodedPublicKey;
  }

  public void setEncodedPublicKey(String encodedPublicKey) {
    this.encodedPublicKey = encodedPublicKey;
  }
}
