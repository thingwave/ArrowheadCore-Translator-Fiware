package eu.arrowhead.core.spokes;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpDateGenerator;


/**
 * ResponseAllowCORS is responsible for adding <code>AllowCORS<c/ode> header to the
 * outgoing responses. This interceptor is recommended for server side protocol
 * processors.
 *
 * @since 4.0
 */
//XXX: Find another way to annotate class as ThreadSafe since class http.annotation.ThreadSafe removed
//@ThreadSafe
public class ResponseAllowCORS implements HttpResponseInterceptor {

    private static final HttpDateGenerator DATE_GENERATOR = new HttpDateGenerator();

    public ResponseAllowCORS() {
        super();
    }

    public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException
                ("HTTP response may not be null.");
        }            
        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
//        response.setHeader("Access-Control-Max-Age", "1209600");
    }

}