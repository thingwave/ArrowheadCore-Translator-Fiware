package eu.arrowhead.core.certificate_authority.model;

import io.github.olivierlemasle.ca.CSR;
import io.github.olivierlemasle.ca.DistinguishedName;
import java.security.PublicKey;

public class ArrowheadCSRImpl implements CSR {

  private final DistinguishedName dn;
  private final PublicKey publicKey;

  public ArrowheadCSRImpl(final DistinguishedName dn, final PublicKey publicKey) {
    this.dn = dn;
    this.publicKey = publicKey;
  }

  @Override
  public DistinguishedName getSubject() {
    return dn;
  }

  @Override
  public PublicKey getPublicKey() {
    return publicKey;
  }
}
