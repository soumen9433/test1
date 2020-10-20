package com.digivalet.pmsi.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.model.CheckinCheckoutDetails;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-06-06T16:44:40.789Z")
public abstract class CheckinCheckoutApiService {
    public abstract Response checkinCheckout(String accessToken, @NotNull String hotelCode, @NotNull String roomNumber,CheckinCheckoutDetails body,SecurityContext securityContext) throws NotFoundException;
}
