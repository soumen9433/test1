package com.digivalet.pmsi.serviceStatus.api;

import javax.servlet.ServletConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.api.NotFoundException;
import com.digivalet.pmsi.serviceStatus.factories.GetStatusApiServiceFactory;
import com.digivalet.pmsi.serviceStatus.model.GetStatusResponse;

@Path("/getStatus")


@io.swagger.annotations.Api(description = "the getStatus API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-08-27T07:13:54.980Z")
public class GetStatusApi  {
   private final GetStatusApiService delegate;

   public GetStatusApi(@Context ServletConfig servletContext) {
      GetStatusApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("GetStatusApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (GetStatusApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = GetStatusApiServiceFactory.getGetStatusApi();
      }

      this.delegate = delegate;
   }

    @GET
    
    @Consumes({ "application/vnd.digivalet.v1+json" })
    @Produces({ "application/vnd.digivalet.v1+json" })
    @io.swagger.annotations.ApiOperation(value = "Get Services Status", notes = "Provide the status of the services and its modules.", response = GetStatusResponse.class, tags={ "Status", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = GetStatusResponse.class),
        
        @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid Service Name", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Void.class) })
    public Response getStatus(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getStatus(securityContext);
    }
}
