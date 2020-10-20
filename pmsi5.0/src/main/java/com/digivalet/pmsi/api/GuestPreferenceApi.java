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
import com.digivalet.pmsi.api.factories.GuestPreferenceApiServiceFactory;
import com.digivalet.pmsi.model.GuestPreferenceRequest;
import com.digivalet.pmsi.model.GuestPreferenceResponse;
import io.swagger.annotations.ApiParam;

@Path("/guestPreference")


@io.swagger.annotations.Api(description = "the guestPreference API")
@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2018-12-21T11:32:48.219Z")
public class GuestPreferenceApi
{
   private final GuestPreferenceApiService delegate;

   public GuestPreferenceApi(@Context ServletConfig servletContext)
   {
      GuestPreferenceApiService delegate = null;

      if (servletContext != null)
      {
         String implClass = servletContext
                  .getInitParameter("GuestPreferenceApi.implementation");
         if (implClass != null && !"".equals(implClass.trim()))
         {
            try
            {
               delegate = (GuestPreferenceApiService) Class.forName(implClass)
                        .newInstance();
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
         }
      }

      if (delegate == null)
      {
         delegate = GuestPreferenceApiServiceFactory.getGuestPreferenceApi();
      }

      this.delegate = delegate;
   }

   @POST
   @Consumes({"application/json"})
   @Produces({"application/json"})
   @io.swagger.annotations.ApiOperation(
            value = "This service is used to post guest preference to Digivalet",
            notes = "", response = GuestPreferenceResponse.class,
            tags = {"Post Guest Preference",})
   @io.swagger.annotations.ApiResponses(value = {
         @io.swagger.annotations.ApiResponse(code = 200,
                  message = "successful operation",
                  response = GuestPreferenceResponse.class),

         @io.swagger.annotations.ApiResponse(code = 204,
                  message = "Request Processed but server not returning anything",
                  response = Void.class),

         @io.swagger.annotations.ApiResponse(code = 400,
                  message = "Bad Request", response = Void.class),

         @io.swagger.annotations.ApiResponse(code = 401,
                  message = "Unauthorized", response = Void.class),

         @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found",
                  response = Void.class),

         @io.swagger.annotations.ApiResponse(code = 415,
                  message = "Unsupported Media type", response = Void.class),

         @io.swagger.annotations.ApiResponse(code = 500,
                  message = "Internal Server Error", response = Void.class)})
   public Response guestPreference(@ApiParam(
            value = "Access token for client verification",
            required = true) @HeaderParam("Access-Token") String accessToken,
            @ApiParam(value = "guest preference body for the reservation",
                     required = true) GuestPreferenceRequest preference,
            @Context SecurityContext securityContext) throws NotFoundException
   {
      return delegate.guestPreference(accessToken, preference, securityContext);
   }
}
