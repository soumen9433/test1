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
import com.digivalet.pmsi.api.factories.SynchronizeApiServiceFactory;
import com.digivalet.pmsi.model.ResponseData;
import io.swagger.annotations.ApiParam;

@Path("/synchronize")


@io.swagger.annotations.Api(description = "the synchronize API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-12-14T23:09:33.161Z")
public class SynchronizeApi  {
   private final SynchronizeApiService delegate;

   public SynchronizeApi(@Context ServletConfig servletContext) {
      SynchronizeApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("SynchronizeApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (SynchronizeApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = SynchronizeApiServiceFactory.getSynchronizeApi();
      }

      this.delegate = delegate;
   }

    @POST
    
    
    @Produces({ "application/vnd.digivalet.v1+json" })
    @io.swagger.annotations.ApiOperation(value = "Synchronize with PMS", notes = "", response = ResponseData.class, tags={ "Sync PMS", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = ResponseData.class) })
    public Response synchronize(@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("access_token") String accessToken
,@ApiParam(value = "Hotel Code for which check out request is initiated",required=true) @QueryParam("hotelCode") String hotelCode
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.synchronize(accessToken,hotelCode,securityContext);
    }
}
