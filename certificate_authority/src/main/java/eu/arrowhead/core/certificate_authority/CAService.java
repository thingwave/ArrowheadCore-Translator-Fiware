package eu.arrowhead.core.certificate_authority;

public class CAService {

  /*static Optional<CSResponse> signCertificate(CSRequest request){
    //Step 1: restore X509 certificate from encoded public key of requester Arrowhead System

  }*/


  /*
     1: generate KeyPair at client with the correct common name
     2: Send X509 cert to CA to sign (cert.getEncoded Base64 coded + cert type in a CSR pojo)
     3: CA restores the X509Cert from the CSR with the help of CertificateRep class
     4: CA checks common name correctness and then signs the cert
     5: CA sends back the CSR response with the same structure as the request
     6: client restores the signed x509 cert and adds is to a new keystore, along with the generated private key
   */

}
