package com.digivalet.pmsi.api;

import javax.servlet.ServletConfig;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.api.factories.CheckoutApiServiceFactory;
import com.digivalet.pmsi.model.CheckoutRequest;
import com.digivalet.pmsi.model.ResponseData;
import io.swagger.annotations.ApiParam;

@Path("/checkout")


@io.swagger.annotations.Api(description = "the checkout API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-08-29T09:41:32.663Z")
public class CheckoutApi  {
   private final CheckoutApiService delegate;

   public CheckoutApi(@Context ServletConfig servletContext) {
      CheckoutApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("CheckoutApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (CheckoutApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = CheckoutApiServiceFactory.getCheckoutApi();
      }

      this.delegate = delegate;
   }

    @POST
    
    
    @Produces({ "application/vnd.digivalet.v1+json" })
    @io.swagger.annotations.ApiOperation(value = "Check Out", notes = "", response = ResponseData.class, tags={ "Guest Actions", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Void.class) })
    public Response checkOut(@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("Access-Token") String accessToken   		
,@ApiParam(value = "Guest Details to checkout Room" ,required=true) CheckoutRequest body
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.checkOut(accessToken,body,securityContext);
    }
}
