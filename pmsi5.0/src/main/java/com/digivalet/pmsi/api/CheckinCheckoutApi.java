package com.digivalet.pmsi.api;

import javax.servlet.ServletConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.digivalet.pmsi.api.factories.CheckinCheckoutApiServiceFactory;
import com.digivalet.pmsi.model.CheckinCheckoutDetails;
import com.digivalet.pmsi.model.ResponseData;
import io.swagger.annotations.ApiParam;

@Path("/checkinCheckout")


@io.swagger.annotations.Api(description = "the checkinCheckout API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-06-06T16:23:07.286Z")
public class CheckinCheckoutApi  {
   private final CheckinCheckoutApiService delegate;

   public CheckinCheckoutApi(@Context ServletConfig servletContext) {
      CheckinCheckoutApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("CheckinCheckoutApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (CheckinCheckoutApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = CheckinCheckoutApiServiceFactory.getCheckinCheckoutApi();
      }

      this.delegate = delegate;
   }

   @POST
   
   @Consumes({ "application/vnd.digivalet.v1+json" })
   @Produces({ "application/vnd.digivalet.v1+json" })
   @io.swagger.annotations.ApiOperation(value = "This service is used to Checkin/Checkout guest in Digivalet", notes = "", response = ResponseData.class, tags={ "Checkin Checkout Guest", })
   @io.swagger.annotations.ApiResponses(value = { 
       @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = ResponseData.class),
       
       @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = Void.class),
       
       @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = Void.class),
       
       @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
       
       @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found", response = Void.class),
       
       @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = Void.class),
       
       @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = Void.class) })
   public Response checkinCheckout(@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("Access-Token") String accessToken
,@ApiParam(value = "Hotel Code for which check out request is initiated",required=true) @QueryParam("hotelCode") String hotelCode
,@ApiParam(value = "Room Number for which check out request is initiated",required=true) @QueryParam("roomNumber") String roomNumber
,@ApiParam(value = "Guest Details" ,required=true) CheckinCheckoutDetails body
,@Context SecurityContext securityContext)
   throws NotFoundException {
       return delegate.checkinCheckout(accessToken,hotelCode,roomNumber,body,securityContext);
   }
}
