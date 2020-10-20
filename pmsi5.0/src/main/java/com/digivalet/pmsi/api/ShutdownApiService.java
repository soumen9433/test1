package com.digivalet.pmsi.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-12-14T23:29:50.546Z")
public abstract class ShutdownApiService {
    public abstract Response shutdown(String accessToken, @NotNull String hotelCode,SecurityContext securityContext) throws NotFoundException;
}
