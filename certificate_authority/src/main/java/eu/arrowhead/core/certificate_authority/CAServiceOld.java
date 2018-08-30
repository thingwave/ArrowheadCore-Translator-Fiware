package eu.arrowhead.core.certificate_authority;

import static io.github.olivierlemasle.ca.CA.createSelfSignedCertificate;
import static io.github.olivierlemasle.ca.CA.dn;

import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.core.certificate_authority.model.CertificateInfo;
import io.github.olivierlemasle.ca.DistinguishedName;
import io.github.olivierlemasle.ca.RootCertificate;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class CAServiceOld {

  private static final long certValidity = 10 * 365 * 24 * 60 * 60;

  static RootCertificate generateRootCert(String distinguishedName, int validYears) {
    final DistinguishedName dn = dn(distinguishedName);
    return createSelfSignedCertificate(dn).validDuringYears(validYears).build();
  }

  static Optional<CertificateInfo> generateX509Certificate(final String systemName) {
    try {
      CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA256withRSA", null);
      keyGen.generate(2048);
      PrivateKey clientPrivateKey = keyGen.getPrivateKey();

      //Get signing cert stuff
      X509Certificate cloudCert = SecurityUtils.getFirstCertFromKeyStore(CAMain.cloudKeystore);
      String cloudCN = SecurityUtils.getCertCNFromSubject(cloudCert.getSubjectDN().getName());
      PrivateKey cloudPrivateKey = SecurityUtils.getPrivateKey(CAMain.cloudKeystore, CAMain.trustStorePass);

      String systemCN = systemName + "." + cloudCN;
      String systemDN = "CN=" + systemCN;
      X509Certificate systemCert = keyGen.getSelfCertificate(new X500Name(systemDN), new Date(), certValidity);
      System.out.println(Base64.getEncoder().encodeToString(systemCert.getPublicKey().getEncoded()));
      X509Certificate systemCert2 = createSignedCertificate(systemCert, cloudCert, cloudPrivateKey);

      String publicKey = Base64.getEncoder().encodeToString(systemCert.getPublicKey().getEncoded());
      System.out.println(publicKey);
      String privateKey = Base64.getEncoder().encodeToString(clientPrivateKey.getEncoded());

      return Optional.of(new CertificateInfo(systemCN, publicKey, privateKey, systemCert, systemCert2));
    } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | CertificateException | SignatureException | IOException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  private static X509Certificate createSignedCertificate(X509Certificate cetrificate, X509Certificate issuerCertificate,
                                                         PrivateKey issuerPrivateKey) {
    try {
      Principal issuer = issuerCertificate.getSubjectDN();
      String issuerSigAlg = issuerCertificate.getSigAlgName();

      byte[] inCertBytes = cetrificate.getTBSCertificate();
      X509CertInfo info = new X509CertInfo(inCertBytes);
      info.set(X509CertInfo.ISSUER, (X500Name) issuer);

      X509CertImpl outCert = new X509CertImpl(info);
      outCert.sign(issuerPrivateKey, issuerSigAlg);
      return outCert;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

}
