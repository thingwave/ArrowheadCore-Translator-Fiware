package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.certificate_authority.model.CertificateInfo;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningRequest;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("ca")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CAResource {

  @GET
  @Path("{systemName}")
  public CertificateInfo getClientCertInfo(@PathParam("systemName") String systemName) {
    if (!systemName.matches("[A-Za-z0-9]+")) {
      throw new BadPayloadException("System name can only contain alphanumerical characters!");
    }
    return CAServiceOld.generateX509Certificate(systemName).orElseThrow(
        () -> new ArrowheadException("Certificate generation failed" + "."));
  }

  @POST
  public CertificateSigningResponse getSignedCertificate(CertificateSigningRequest request) {
    return CAService.signCertificate(request);
  }

}
