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
import com.digivalet.pmsi.api.factories.ShutdownApiServiceFactory;
import com.digivalet.pmsi.model.ResponseData;
import io.swagger.annotations.ApiParam;

@Path("/shutdown")


@io.swagger.annotations.Api(description = "the shutdown API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-12-14T23:29:50.546Z")
public class ShutdownApi  {
   private final ShutdownApiService delegate;

   public ShutdownApi(@Context ServletConfig servletContext) {
      ShutdownApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("ShutdownApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (ShutdownApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = ShutdownApiServiceFactory.getShutdownApi();
      }

      this.delegate = delegate;
   }

    @POST
    
    
    @Produces({ "application/vnd.digivalet.v1+json" })
    @io.swagger.annotations.ApiOperation(value = "Shutdown PMS", notes = "", response = ResponseData.class, tags={ "PMS Actions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = ResponseData.class) })
    public Response shutdown(@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("access_token") String accessToken
,@ApiParam(value = "Hotel Code for which check out request is initiated",required=true) @QueryParam("hotelCode") String hotelCode
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.shutdown(accessToken,hotelCode,securityContext);
    }
}
