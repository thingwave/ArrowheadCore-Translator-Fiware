/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, you can obtain one at http://mozilla.org/MPL/2.0/. 
*
* This work was supported by National Funds through FCT (Portuguese
* Foundation for Science and Technology) and by the EU ECSEL JU
* funding, within Arrowhead project, ref. ARTEMIS/0001/2012,
* JU grant nr. 332987.
* ISEP, Polytechnic Institute of Porto.
*/
package eu.arrowhead.qos;

import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.ssl.SecurityUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class Main {

  private static Logger log = Logger.getLogger(Main.class.
      getName());
  private static Properties prop;

  public static final String BASE_URI = getProp().
      getProperty("base_uri", "http://0.0.0.0:8441/qos/");
  public static final String BASE_URI_SECURED = getProp().
      getProperty("base_uri_secured", "https://0.0.0.0:8442/qos/");

  public static void main(String[] args) throws IOException {
    PropertyConfigurator.
        configure("config" + File.separator + "log4j.properties");

    HttpServer server = null;
    HttpServer secureServer = null;
    if (args != null && args.length > 0) {
      switch (args[0]) {
        case "secure":
          secureServer = startSecureServer();
          break;
        case "both":
          server = startServer();
          secureServer = startSecureServer();
      }
    } else {
      server = startServer();
    }

    System.out.println("Press enter to shutdown QoS Server(s)...");
    System.in.read();

    if (server != null) {
      log.info("Stopping server at: " + BASE_URI);
      server.shutdownNow();
    }
    if (secureServer != null) {
      log.info("Stopping server at: " + BASE_URI);
      secureServer.shutdownNow();
    }

    System.out.println("QoS Server(s) stopped");
  }

  public static HttpServer startServer() throws IOException {
    log.info("Starting server at: " + BASE_URI);

    URI uri = UriBuilder.fromUri(BASE_URI).build();

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(QoSResource.class);
    config.packages("eu.arrowhead.common");
    config.property("isSecure", false);

    final HttpServer server = GrizzlyHttpServerFactory.
        createHttpServer(uri, config);
    server.getServerConfiguration().
        setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  public static HttpServer startSecureServer() throws IOException {
    log.info("Starting server at: " + BASE_URI_SECURED);

    URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(QoSResource.class);
    config.packages("eu.arrowhead.common");

    SSLContextConfigurator sslCon = new SSLContextConfigurator();

    String keystorePath = getProp().getProperty("ssl.keystore");
    String keystorePass = getProp().getProperty("ssl.keystorepass");
    String keyPass = getProp().getProperty("ssl.keypass");
    String truststorePath = getProp().getProperty("ssl.truststore");
    String truststorePass = getProp().getProperty("ssl.truststorepass");

    sslCon.setKeyStoreFile(keystorePath);
    sslCon.setKeyStorePass(keystorePass);
    sslCon.setKeyPass(keyPass);
    sslCon.setTrustStoreFile(truststorePath);
    sslCon.setTrustStorePass(truststorePass);

    SSLContext sslContext = sslCon.createSSLContext();
    eu.arrowhead.common.Utility.setSSLContext(sslContext);

    X509Certificate serverCert = null;
    try {
      KeyStore keyStore = SecurityUtils.
          loadKeyStore(keystorePath, keystorePass);
      serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    } catch (Exception ex) {
      throw new AuthenticationException(ex.getMessage());
    }
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.
        getSubjectDN().getName());
    log.info("Certificate of the secure server: " + serverCN);
    config.property("server_common_name", serverCN);

    final HttpServer server = GrizzlyHttpServerFactory.
        createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).
            setClientMode(false).setNeedClientAuth(true));
    server.getServerConfiguration().
        setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  public synchronized static Properties getProp() {
    try {
      if (prop == null) {
        prop = new Properties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        if (inputStream != null) {
          prop.load(inputStream);
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return prop;
  }
}