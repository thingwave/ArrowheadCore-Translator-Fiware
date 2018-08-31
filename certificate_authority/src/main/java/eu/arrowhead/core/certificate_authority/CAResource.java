package eu.arrowhead.core.certificate_authority;

import eu.arrowhead.core.certificate_authority.model.CertificateSigningRequest;
import eu.arrowhead.core.certificate_authority.model.CertificateSigningResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("ca")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CAResource {

  @POST
  public CertificateSigningResponse getSignedCertificate(CertificateSigningRequest request) {
    return CAService.signCertificate(request);
  }

}
