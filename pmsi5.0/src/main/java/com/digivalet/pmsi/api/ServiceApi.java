package com.digivalet.pmsi.api;

import javax.servlet.ServletConfig;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.api.factories.ServiceApiServiceFactory;
import com.digivalet.pmsi.model.ResponseData;
import io.swagger.annotations.ApiParam;

@Path("/service")


@io.swagger.annotations.Api(description = "the service API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public class ServiceApi  {
   private final ServiceApiService delegate;

   public ServiceApi(@Context ServletConfig servletContext) {
      ServiceApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("ServiceApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (ServiceApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = ServiceApiServiceFactory.getServiceApi();
      }

      this.delegate = delegate;
   }

    @POST
    
    
    @Produces({ "application/vnd.digivalet.v1+json" })
    @io.swagger.annotations.ApiOperation(value = "Post service on/off at PMS", notes = "", response = ResponseData.class, tags={ "Guest Actions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = ResponseData.class) })
    public Response setServiceState(@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("access_token") String accessToken
,@ApiParam(value = "Hotel Code for which service needs to set on/off",required=true) @QueryParam("hotelCode") String hotelCode
,@ApiParam(value = "Room Number for which service needs to set on/off",required=true) @QueryParam("roomNumber") String roomNumber
,@ApiParam(value = "Service Name to set on/off",required=true, allowableValues="DND, MMR", defaultValue="") @DefaultValue("") @QueryParam("serviceName") String serviceName
,@ApiParam(value = "Required operation to whether to post service on/off",required=true, defaultValue="false") @DefaultValue("false") @QueryParam("operation") Boolean operation
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.setServiceState(accessToken,hotelCode,roomNumber,serviceName,operation,securityContext);
    }
}
