package eu.arrowhead.core.certificate_authority;

import static io.github.olivierlemasle.ca.CA.dn;

import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningRequest;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningResponse;
import io.github.olivierlemasle.ca.DistinguishedName;
import io.github.olivierlemasle.ca.SignerImpl;
import java.security.KeyPair;
import java.security.PublicKey;

final class CAService {

  private CAService() throws AssertionError {
    throw new AssertionError("CAService is a non-instantiable class");
  }

  /*
     1: generate KeyPair at client with the correct common name
     2: Send X509 cert to CA to sign (cert.getEncoded Base64 coded + cert type in a CSR pojo)

     3: CA restores the X509Cert from the CSR with the help of CertificateRep class
     4: CA checks common name correctness and then signs the cert
     5: CA sends back the CSR response with the same structure as the request

     6: client restores the signed x509 cert and adds is to a new keystore, along with the generated private key
   */

  static CertificateSigningResponse signCertificate(CertificateSigningRequest csr) {
    /*byte[] pemBytes;
    try {
      pemBytes = Base64.getDecoder().decode(csr.getDistinguishedName());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new AuthException("X509 cert decoding failed! Caused by: " + e.getMessage(), Status.BAD_REQUEST.getStatusCode(), e);
    }

    X509Certificate clientCert;
    try {
      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      //NOTE this method can take a Base64 coded String input stream too, meaning less code on my side
      clientCert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(pemBytes));
    } catch (CertificateException e) {
      throw new AuthException("Certificate parsing error: " + e.getMessage(), Status.BAD_REQUEST.getStatusCode(), e);
    }

    String clientCN = SecurityUtils.getCertCNFromSubject(clientCert.getSubjectDN().getName());
    String cloudCN = SecurityUtils.getCertCNFromSubject(SecurityUtils.getFirstCertFromKeyStore(CAMain.cloudKeystore).getSubjectDN().getName());
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN, cloudCN)) {
      throw new AuthException("Certificate does not have a valid common name! Valid common name: {systemName}." + cloudCN,
                              Status.BAD_REQUEST.getStatusCode());
    }*/

    final PublicKey publicKey = SecurityUtils.getPublicKey(csr.getPemPublicKey());
    final DistinguishedName dName = dn(csr.getDistinguishedName());
    final KeyPair pair = new KeyPair(getX509Certificate().getPublicKey(), getPrivateKey());
    final DistinguishedName signerSubject = dn(caCertificateHolder.getSubject());
    SignerImpl new SignerImpl(pair, signerSubject, request.getPublicKey(), request.getSubject());

    return null;
  }

}
