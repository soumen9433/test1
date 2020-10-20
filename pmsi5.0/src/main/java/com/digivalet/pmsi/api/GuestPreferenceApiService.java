package com.digivalet.pmsi.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.model.GuestPreferenceRequest;

@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2018-12-21T11:32:48.219Z")
public abstract class GuestPreferenceApiService
{
   public abstract Response guestPreference(String accessToken,
            GuestPreferenceRequest preference, SecurityContext securityContext)
            throws NotFoundException;
}
