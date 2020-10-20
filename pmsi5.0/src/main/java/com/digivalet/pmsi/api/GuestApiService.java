package com.digivalet.pmsi.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.model.GuestDetailRequest;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2020-01-21T13:21:56.464Z")
public abstract class GuestApiService {
    public abstract Response details(String accessToken,GuestDetailRequest guestDetailRequest,SecurityContext securityContext) throws NotFoundException;
}
