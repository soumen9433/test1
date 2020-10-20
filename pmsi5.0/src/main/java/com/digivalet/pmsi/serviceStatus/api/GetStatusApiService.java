package com.digivalet.pmsi.serviceStatus.api;



import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.api.NotFoundException;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-08-27T07:13:54.980Z")
public abstract class GetStatusApiService {
    public abstract Response getStatus(SecurityContext securityContext) throws NotFoundException;
}
