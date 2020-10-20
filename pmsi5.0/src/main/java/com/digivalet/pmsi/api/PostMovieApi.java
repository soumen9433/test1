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
import com.digivalet.pmsi.api.factories.PostMovieApiServiceFactory;
import com.digivalet.pmsi.model.MovieDetails;
import com.digivalet.pmsi.model.ResponseData;
import io.swagger.annotations.ApiParam;

@Path("/postMovie")


@io.swagger.annotations.Api(description = "the postMovie API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-12-21T14:08:15.130Z")
public class PostMovieApi  {
   private final PostMovieApiService delegate;

   public PostMovieApi(@Context ServletConfig servletContext) {
      PostMovieApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("PostMovieApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (PostMovieApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = PostMovieApiServiceFactory.getPostMovieApi();
      }

      this.delegate = delegate;
   }

    @POST
    
    
    @Produces({ "application/vnd.digivalet.v1+json" })
    @io.swagger.annotations.ApiOperation(value = "Post Movie to PMS", notes = "", response = ResponseData.class, tags={ "Movie Posting", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 204, message = "Request Processed but server not returning anything", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media type", response = ResponseData.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error", response = ResponseData.class) })
    public Response postMovie(@ApiParam(value = "Access token for client verification" ,required=true)@HeaderParam("access_token") String accessToken
,@ApiParam(value = "Hotel Code for which check out request is initiated",required=true) @QueryParam("hotelCode") String hotelCode
,@ApiParam(value = "Room Number for which check out request is initiated",required=true) @QueryParam("roomNumber") String roomNumber
,@ApiParam(value = "Guest Id for which check out request is initiated.",required=true) @QueryParam("guestId") String guestId
,@ApiParam(value = "Details of movie for posting" ,required=true) MovieDetails data
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.postMovie(accessToken,hotelCode,roomNumber,guestId,data,securityContext);
    }
}
