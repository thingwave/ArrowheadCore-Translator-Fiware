package eu.arrowhead.core.certificate_authority.model;

import java.security.cert.X509Certificate;

public class CertificateInfo {

  private String commonName;
  private String publicKey;
  private String privateKey;
  private X509Certificate before;
  private X509Certificate after;

  public CertificateInfo() {
  }

  public CertificateInfo(String commonName, String publicKey, String privateKey) {
    this.commonName = commonName;
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  public CertificateInfo(String commonName, String publicKey, String privateKey, X509Certificate before, X509Certificate after) {
    this.commonName = commonName;
    this.publicKey = publicKey;
    this.privateKey = privateKey;
    this.before = before;
    this.after = after;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public X509Certificate getBefore() {
    return before;
  }

  public void setBefore(X509Certificate before) {
    this.before = before;
  }

  public X509Certificate getAfter() {
    return after;
  }

  public void setAfter(X509Certificate after) {
    this.after = after;
  }
}
