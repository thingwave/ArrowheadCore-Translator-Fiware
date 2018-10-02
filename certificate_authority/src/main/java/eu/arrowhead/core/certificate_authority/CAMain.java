/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.core.certificate_authority.filter.CertAuthorityACF;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.core.Response;

public class CAMain extends ArrowheadMain {

  static KeyStore cloudKeystore;
  static String trustStorePass;
  static X509Certificate cloudCert;
  static String cloudCN;
  static String encodedAuthPublicKey;

  private CAMain(String[] args) {
    Set<Class<?>> classes = new HashSet<>(Arrays.asList(CAResource.class, CertAuthorityACF.class));
    String[] packages = {"eu.arrowhead.common.exception", "eu.arrowhead.common.json", "eu.arrowhead.common.filter"};
    init(CoreSystem.CERTIFICATE_AUTHORITY, args, classes, packages);

    trustStorePass = props.getProperty("truststorepass");
    cloudKeystore = SecurityUtils.loadKeyStore(props.getProperty("truststore"), trustStorePass);
    cloudCert = SecurityUtils.getFirstCertFromKeyStore(CAMain.cloudKeystore);
    cloudCN = SecurityUtils.getCertCNFromSubject(cloudCert.getSubjectX500Principal().getName());

    CompletableFuture.supplyAsync(() -> {
      Optional<String[]> optionalUri = Utility.getServiceInfo("AuthorizationControl");

      if (optionalUri.isPresent()) {
        String authUri = optionalUri.get()[0];
        authUri = authUri.substring(0, authUri.lastIndexOf("/")) + "/mgmt/publickey";
        Response response = Utility.sendRequest(authUri, "GET", null);
        encodedAuthPublicKey = response.readEntity(String.class);
      }
      return null;
    });

    listenForInput();
  }

  public static void main(String[] args) {
    new CAMain(args);
  }

}
