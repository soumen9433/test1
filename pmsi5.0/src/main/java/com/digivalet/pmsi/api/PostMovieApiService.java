package com.digivalet.pmsi.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.model.MovieDetails;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-12-21T14:08:15.130Z")
public abstract class PostMovieApiService {
    public abstract Response postMovie(String accessToken, @NotNull String hotelCode, @NotNull String roomNumber, @NotNull String guestId,MovieDetails data,SecurityContext securityContext) throws NotFoundException;
}
