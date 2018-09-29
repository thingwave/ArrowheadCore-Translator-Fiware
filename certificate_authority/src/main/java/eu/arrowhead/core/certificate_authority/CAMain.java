/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.core.certificate_authority.filter.CertAuthorityACF;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CAMain extends ArrowheadMain {

  static KeyStore cloudKeystore;
  static String trustStorePass;

  private CAMain(String[] args) {
    Set<Class<?>> classes = new HashSet<>(Arrays.asList(CAResource.class, CertAuthorityACF.class));
    String[] packages = {"eu.arrowhead.common.exception", "eu.arrowhead.common.json", "eu.arrowhead.common.filter"};
    init(CoreSystem.CERTIFICATE_AUTHORITY, args, classes, packages);

    trustStorePass = props.getProperty("truststorepass");
    cloudKeystore = SecurityUtils.loadKeyStore(props.getProperty("truststore"), trustStorePass);

    listenForInput();
  }

  public static void main(String[] args) {
    new CAMain(args);
  }

}
