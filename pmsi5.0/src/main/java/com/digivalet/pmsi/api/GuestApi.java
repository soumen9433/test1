package com.digivalet.pmsi.api;

import javax.servlet.ServletConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.api.factories.GuestApiServiceFactory;
import com.digivalet.pmsi.model.DetailsResponse;
import com.digivalet.pmsi.model.GuestDetailRequest;
import io.swagger.annotations.ApiParam;

@Path("/guest")


@io.swagger.annotations.Api(description = "the guest API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2020-01-21T13:21:56.464Z")
public class GuestApi  {
   private final GuestApiService delegate;

   public GuestApi(@Context ServletConfig servletContext) {
      GuestApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("GuestApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (GuestApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = GuestApiServiceFactory.getGuestApi();
      }

      this.delegate = delegate;
   }

    @POST
    @Path("/details")
    @Consumes({ "application/vnd.digivalet.v1+json" })
    @Produces({ "application/vnd.digivalet.v1+json" })
    @io.swagger.annotations.ApiOperation(value = "details", notes = "details", response = DetailsResponse.class, tags={ "details", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = DetailsResponse.class),
        
        @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid ID supplied", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Void.class) })
    public Response details(@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("Access-Token") String accessToken
,@ApiParam(value = "object" ,required=true) GuestDetailRequest guestDetailRequest
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.details(accessToken,guestDetailRequest,securityContext);
    }
}