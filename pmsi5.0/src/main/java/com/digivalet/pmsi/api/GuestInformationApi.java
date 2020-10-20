package com.digivalet.pmsi.api;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.api.factories.GuestInformationApiServiceFactory;
import com.digivalet.pmsi.model.GuestData;
import io.swagger.annotations.ApiParam;

@Path("/guestInformation")


@io.swagger.annotations.Api(description = "the guestInformation API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public class GuestInformationApi  {
   private final GuestInformationApiService delegate;

   public GuestInformationApi(@Context ServletConfig servletContext) {
      GuestInformationApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("GuestInformationApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (GuestInformationApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = GuestInformationApiServiceFactory.getGuestInformationApi();
      }

      this.delegate = delegate;
   }

    @GET
    
    
    @Produces({ "application/vnd.digivalet.v1+json" })
    @io.swagger.annotations.ApiOperation(value = "Get Guest details required to show on device for a guest", notes = "", response = GuestData.class, tags={ "Guest Details", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = GuestData.class),
        
        @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = GuestData.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = GuestData.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = GuestData.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found", response = GuestData.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = GuestData.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = GuestData.class) })
    public Response getGuestInformation(@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("access_token") String accessToken
,@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("Access-Token") String AccessToken
,@ApiParam(value = "Room Number required to fetch bill details.",required=true) @QueryParam("roomNumber") String roomNumber
,@ApiParam(value = "Hotel code required to fetch bill details.",required=true) @QueryParam("hotelCode") String hotelCode
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getGuestInformation(accessToken,AccessToken,roomNumber,hotelCode,securityContext);
    }
}
