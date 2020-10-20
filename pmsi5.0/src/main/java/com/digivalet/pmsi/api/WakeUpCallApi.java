package com.digivalet.pmsi.api;

import javax.servlet.ServletConfig;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.api.factories.WakeUpCallApiServiceFactory;
import com.digivalet.pmsi.model.ResponseData;
import io.swagger.annotations.ApiParam;

@Path("/wakeUpCall")


@io.swagger.annotations.Api(description = "the wakeUpCall API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public class WakeUpCallApi  {
   private final WakeUpCallApiService delegate;

   public WakeUpCallApi(@Context ServletConfig servletContext) {
      WakeUpCallApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("WakeUpCallApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (WakeUpCallApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = WakeUpCallApiServiceFactory.getWakeUpCallApi();
      }

      this.delegate = delegate;
   }

    @POST
    
    
    @Produces({ "application/vnd.digivalet.v1+json" })
    @io.swagger.annotations.ApiOperation(value = "Set/Reset Wake up call", notes = "", response = ResponseData.class, tags={ "Guest Actions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = ResponseData.class) })
    public Response setResetWakeUpCall(@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("access_token") String accessToken
,@ApiParam(value = "Hotel Code for which wake up call needs to be set",required=true) @QueryParam("hotelCode") String hotelCode
,@ApiParam(value = "Room Number for which wake up call needs to be set",required=true) @QueryParam("roomNumber") String roomNumber
,@ApiParam(value = "Wake Up time which is required to be set for a Room number",required=true) @QueryParam("wakeUpTime") String wakeUpTime
,@ApiParam(value = "Wake Up date which is required to be set for a Room number",required=true) @QueryParam("wakeUpDate") String wakeUpDate
,@ApiParam(value = "Required operation to whether to Set/Reset the alarm",required=true) @QueryParam("operation") Boolean operation
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.setResetWakeUpCall(accessToken,hotelCode,roomNumber,wakeUpTime,wakeUpDate,operation,securityContext);
    }
}
