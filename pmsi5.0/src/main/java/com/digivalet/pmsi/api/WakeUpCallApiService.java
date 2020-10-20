package com.digivalet.pmsi.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public abstract class WakeUpCallApiService {
    public abstract Response setResetWakeUpCall(String accessToken, @NotNull String hotelCode, @NotNull String roomNumber, @NotNull String wakeUpTime, @NotNull String wakeUpDate, @NotNull Boolean operation,SecurityContext securityContext) throws NotFoundException;
}
