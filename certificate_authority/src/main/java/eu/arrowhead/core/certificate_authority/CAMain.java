package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.SecurityUtils;
import io.github.olivierlemasle.ca.RootCertificate;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CAMain extends ArrowheadMain {

  static KeyStore trustStore;
  static String trustStorePass;
  static final RootCertificate rootCert;

  static {
    rootCert = CAServiceOld.generateRootCert("CN=arrowhead.eu", 10);
  }

  private CAMain(String[] args) {
    Set<Class<?>> classes = new HashSet<>(Collections.singletonList(CAResource.class));
    String[] packages = {"eu.arrowhead.common.exception", "eu.arrowhead.common.json", "eu.arrowhead.common.filter"};
    init(CoreSystem.CERTIFICATE_AUTHORITY, args, classes, packages);

    trustStorePass = props.getProperty("truststorepass");
    trustStore = SecurityUtils.loadKeyStore(props.getProperty("truststore"), trustStorePass);

    listenForInput();
  }

  public static void main(String[] args) {
    new CAMain(args);
  }

}
