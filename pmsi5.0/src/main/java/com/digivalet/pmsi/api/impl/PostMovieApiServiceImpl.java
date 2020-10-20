package com.digivalet.pmsi.api.impl;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.movieposting.DVSendMovieEvent;
import com.digivalet.movies.DVMovieEvent;
import com.digivalet.pmsi.api.NotFoundException;
import com.digivalet.pmsi.api.PostMovieApiService;
import com.digivalet.pmsi.model.MovieData;
import com.digivalet.pmsi.model.MovieDetails;
import com.digivalet.pmsi.model.ResponseData;
import com.digivalet.pmsi.model.SuccessResponse;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.result.DVResultMapper;

@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2017-12-21T14:08:15.130Z")
public class PostMovieApiServiceImpl extends PostMovieApiService
{
   static DVLogger dvLogger = DVLogger.getInstance();

   @Override
   public Response postMovie(String accessToken, @NotNull String hotelCode,
            @NotNull String roomNumber, @NotNull String guestId,
            MovieDetails data, SecurityContext securityContext)
            throws NotFoundException
   {
      JSONObject tokenVerify = null;
      Thread.currentThread().setName("POST-MOVIE-API");
      dvLogger.info(" Inside Post movie data: " + data.toString());
      try
      {

         tokenVerify = DVPmsMain.getInstance().getDvTokenValidation()
                  .validateToken(accessToken);

         if (tokenVerify.getBoolean("status") == false)
         {
            return Response.status(401).entity(tokenVerify.toString()).build();
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in verifying token ", e);
      }
      JSONObject request = new JSONObject();
      request.put("roomNumber", roomNumber);
      request.put("guestId", guestId);
      request.put("hotelCode", hotelCode);
      DVResult valid = null;
      try
      {
         valid = DVPmsMain.getInstance().getDvPmsController()
                  .validateRequest(request);
      }
      catch (Exception e1)
      {
         dvLogger.error("Error in validating ", e1);
      }
      if (valid.getCode() == DVResult.SUCCESS)
      {
         try
         {
            DVMovieEvent event = DVMovieEvent
                     .fromString(data.getDetails().get(0).getState());
            if (event == DVMovieEvent.stopped)
            {
               DVPmsMain.getInstance().getDvPmsController()
               .postMovie(roomNumber, guestId, data, event);
               MovieData movieData = data.getDetails().get(0);
               String inRoomDeviceId = data.getInRoomDeviceId();
               DVSendMovieEvent dvSendMovieEvent = new DVSendMovieEvent(
                        roomNumber, guestId, movieData, inRoomDeviceId);
               dvSendMovieEvent.start();
            }
            else if (event == DVMovieEvent.played)
            {
               MovieData movieData = data.getDetails().get(0);
               String inRoomDeviceId = data.getInRoomDeviceId();
               DVSendMovieEvent dvSendMovieEvent = new DVSendMovieEvent(
                        roomNumber, guestId, movieData, inRoomDeviceId);
               dvSendMovieEvent.start();
            }
            else if (event == DVMovieEvent.paused)
            {
               MovieData movieData = data.getDetails().get(0);
               String inRoomDeviceId = data.getInRoomDeviceId();
               DVSendMovieEvent dvSendMovieEvent = new DVSendMovieEvent(
                        roomNumber, guestId, movieData, inRoomDeviceId);
               dvSendMovieEvent.start();
            }
            else if (event == DVMovieEvent.purchased)
            {
               MovieData movieData = data.getDetails().get(0);
               String inRoomDeviceId = data.getInRoomDeviceId();
               DVSendMovieEvent dvSendMovieEvent = new DVSendMovieEvent(
                        roomNumber, guestId, movieData, inRoomDeviceId);
               dvSendMovieEvent.start();
               DVPmsMain.getInstance().getDvPmsController()
               .postMovie(roomNumber, guestId, data, event);
            }

            dvLogger.info(" Before sending response  ");
            ResponseData response = new ResponseData();
            response.setMessage("Success ");
            response.setResponseTag(200L);
            response.setStatus(true);
            List<SuccessResponse> success = new ArrayList<SuccessResponse>();
            SuccessResponse suc = new SuccessResponse();
            suc.setSuccess(true);
            success.add(suc);
            response.setData(success);

            dvLogger.info("response data: " + response.toString());
            return Response.status(200).entity(response).build();
         }
         catch (Exception e)
         {
            dvLogger.error("Error in post movie ", e);
         }

      }
      else
      {
         DVResultMapper dvResultMapper = new DVResultMapper();
         int apiCode = dvResultMapper.getApiCode(valid.getCode());
         ResponseData response = new ResponseData();
         response.setMessage("Success");
         response.setResponseTag((long) apiCode);
         response.setStatus(false);
         List<SuccessResponse> success = new ArrayList<SuccessResponse>();
         SuccessResponse suc = new SuccessResponse();
         suc.setSuccess(false);
         success.add(suc);
         response.setData(success);

         dvLogger.info("response data: " + response.toString());
         return Response.status(apiCode).entity(response).build();
      }
      return Response.status(500).entity("").build();


   }
}
