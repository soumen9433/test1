package com.digivalet.pmsi.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public abstract class GuestInformationApiService {
    public abstract Response getGuestInformation(String accessToken,String AccessToken,@NotNull String roomNumber, @NotNull String hotelCode,SecurityContext securityContext) throws NotFoundException;
}
