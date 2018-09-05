/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.certificate_authority;

import static io.github.olivierlemasle.ca.CA.dn;

import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.core.certificate_authority.model.ArrowheadSignerImpl;
import eu.arrowhead.core.certificate_authority.model.CSRequest;
import eu.arrowhead.core.certificate_authority.model.CSResponse;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningRequest;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningResponse;
import io.github.olivierlemasle.ca.Certificate;
import io.github.olivierlemasle.ca.DistinguishedName;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.ServiceConfigurationError;
import javax.ws.rs.core.Response.Status;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

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
    Encoder encoder = Base64.getEncoder();
    String pemEncodedSignedCert = encoder.encodeToString(encodedCert);

    System.out.println(SecurityUtils.getFirstCertFromKeyStore(CAMain.cloudKeystore).getSubjectX500Principal().getName());
    System.out.println(cert.getX509Certificate().getIssuerX500Principal().getName());
    if (!signerSubject.getX500Principal().equals(cert.getX509Certificate().getIssuerX500Principal())) {
      System.out.println("HOUSTON, WE HAVE A PROBLEM");
    }

    try {
      java.security.cert.Certificate[] chain = CAMain.cloudKeystore
          .getCertificateChain(SecurityUtils.getFirstAliasFromKeyStore(CAMain.cloudKeystore));
      String pemIntermediateCert = encoder.encodeToString(chain[0].getEncoded());
      String pemRootCert = encoder.encodeToString(chain[1].getEncoded());
      return new CertificateSigningResponse(pemEncodedSignedCert, pemIntermediateCert, pemRootCert);
    } catch (KeyStoreException | CertificateEncodingException e) {
      e.printStackTrace();
    }

    return new CertificateSigningResponse(pemEncodedSignedCert);
  }

  static CSResponse doBouncy(CSRequest request) {
    //Decode the PKCS10 certificate request
    byte[] csrBytes = Base64.getDecoder().decode(request.getEncodedCertRequest());
    JcaPKCS10CertificationRequest csr;
    try {
      csr = new JcaPKCS10CertificationRequest(csrBytes);
    } catch (IOException e) {
      throw new BadPayloadException("Failed to parse request as a JcaPKCS10CertificationRequest (" + e.getMessage() + ")", e);
    }

    //Compare common names, reject requests with invalid common name
    X509Certificate cloudCert = SecurityUtils.getFirstCertFromKeyStore(CAMain.cloudKeystore);
    final String clientCN = SecurityUtils.getCertCNFromSubject(csr.getSubject().toString());
    final String cloudCN = SecurityUtils.getCertCNFromSubject(cloudCert.getSubjectX500Principal().getName());
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(clientCN, cloudCN)) {
      throw new BadPayloadException("Certificate request does not have a valid common name! Valid common name: {systemName}." + cloudCN);
    }

    //Verify the signature on the certificate request
    ContentVerifierProvider verifierProvider;
    try {
      verifierProvider = new JcaContentVerifierProviderBuilder().setProvider("BCFIPS").build(csr.getSubjectPublicKeyInfo());
      if (!csr.isSignatureValid(verifierProvider)) {
        throw new AuthException("Certificate request has invalid signature! (key pair does not match)");
      }
    } catch (OperatorCreationException | PKCSException e) {
      throw new AuthException("Encapsulated " + e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
    }

    //Signed cert generation from public key and subject
    Date validFrom = Date.from(LocalDateTime.now().minusSeconds(5).atZone(ZoneId.systemDefault()).toInstant());
    Date validUntil = Date.from(LocalDateTime.now().plusYears(5).atZone(ZoneId.systemDefault()).toInstant());
    PublicKey clientKey;
    try {
      clientKey = csr.getPublicKey();
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      //This should not be possible after a successful signature verification
      throw new ServiceConfigurationError("Extracting the public key from the CSR failed (" + e.getMessage() + ")", e);
    }
    X509v3CertificateBuilder v3CertBldr = new JcaX509v3CertificateBuilder(cloudCert, // issuer
                                                                          BigInteger.valueOf(System.currentTimeMillis()) // serial number
                                                                                    .multiply(BigInteger.valueOf(10)), validFrom, // start time
                                                                          validUntil, // expiry time
                                                                          csr.getSubject(), // subject
                                                                          clientKey); // subject public key
    /* Adding the following extensions to the new certificate:
       1) The subject key identifier provides a hashed value that should uniquely identify the public key
       2) The authority key identifier provides a hashed value that should uniquely identify the issuer of the certificate
       3) And this basic constraint is for forbidding issuing other certificates under this certificate
     */
    try {
      JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
      v3CertBldr.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(clientKey));
      v3CertBldr.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(cloudCert));
      v3CertBldr.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
    } catch (NoSuchAlgorithmException | CertIOException | CertificateEncodingException e) {
      throw new AuthException("Appending extensions to the certificate failed! (" + e.getMessage() + ")", e);
    }

    JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA512withRSA").setProvider("BCFIPS");
    PrivateKey cloudPrivateKey = SecurityUtils.getPrivateKey(CAMain.cloudKeystore, CAMain.trustStorePass);
    String encodedSignedCert;
    try {
      X509Certificate clientCert = new JcaX509CertificateConverter().setProvider("BCFIPS")
                                                                    .getCertificate(v3CertBldr.build(signerBuilder.build(cloudPrivateKey)));
      encodedSignedCert = Base64.getEncoder().encodeToString(clientCert.getEncoded());
    } catch (CertificateException e) {
      throw new AuthException("Certificate encoding failed! (" + e.getMessage() + ")", e);
    } catch (OperatorCreationException e) {
      throw new AuthException("Certificate signing failed! (" + e.getMessage() + ")", e);
    }
    return new CSResponse(encodedSignedCert);
  }

}
