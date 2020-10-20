package com.digivalet.pmsi.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.model.CheckoutRequest;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-08-29T09:41:32.663Z")
public abstract class CheckoutApiService {
    public abstract Response checkOut(String accessToken,CheckoutRequest body,SecurityContext securityContext) throws NotFoundException;
}
