package eu.arrowhead.core.certificate_authority;

import static io.github.olivierlemasle.ca.CA.dn;

import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.core.certificate_authority.model.ArrowheadSignerImpl;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningRequest;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningResponse;
import io.github.olivierlemasle.ca.Certificate;
import io.github.olivierlemasle.ca.DistinguishedName;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import javax.ws.rs.core.Response.Status;

final class CAService {

  private CAService() throws AssertionError {
    throw new AssertionError("CAService is a non-instantiable class");
  }

  static CertificateSigningResponse signCertificate(CertificateSigningRequest csr) {
    final String clientCN = SecurityUtils.getCertCNFromSubject(csr.getDistinguishedName());
    final DistinguishedName signerSubject = dn(SecurityUtils.getFirstCertFromKeyStore(CAMain.cloudKeystore).getSubjectX500Principal().getName());
    final String cloudCN = SecurityUtils.getCertCNFromSubject(signerSubject.getName());
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN, cloudCN)) {
      throw new AuthException("Certificate does not have a valid common name! Valid common name: {systemName}." + cloudCN,
                              Status.BAD_REQUEST.getStatusCode());
    }

    final PublicKey publicKey = SecurityUtils.getPublicKey(csr.getPemPublicKey());
    final DistinguishedName subject = dn(csr.getDistinguishedName());

    final PublicKey cloudPublicKey = SecurityUtils.getFirstCertFromKeyStore(CAMain.cloudKeystore).getPublicKey();
    final PrivateKey cloudPrivateKey = SecurityUtils.getPrivateKey(CAMain.cloudKeystore, CAMain.trustStorePass);
    final KeyPair signerKeyPair = new KeyPair(cloudPublicKey, cloudPrivateKey);
    ArrowheadSignerImpl signer = new ArrowheadSignerImpl(signerKeyPair, signerSubject, publicKey, subject);
    final Certificate cert = signer.setRandomSerialNumber().validDuringYears(5).sign();

    byte[] encodedCert;
    try {
      encodedCert = cert.getX509Certificate().getEncoded();
    } catch (CertificateEncodingException e) {
      throw new AuthException("Signed certificate encoding failed! Cause: " + e.getMessage(), e);
    }
    String pemEncodedSignedCert = Base64.getEncoder().encodeToString(encodedCert);
    return new CertificateSigningResponse(pemEncodedSignedCert);
  }

}
