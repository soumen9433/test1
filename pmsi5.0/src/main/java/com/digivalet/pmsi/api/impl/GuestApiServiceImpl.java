package com.digivalet.pmsi.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.api.GuestApiService;
import com.digivalet.pmsi.api.NotFoundException;
import com.digivalet.pmsi.model.DetailsResponse;
import com.digivalet.pmsi.model.GuestDetailRequest;

@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2020-01-21T13:21:56.464Z")
public class GuestApiServiceImpl extends GuestApiService
{

   DVLogger dvLogger = new DVLogger();

   @Override
   public Response details(String accessToken,
            GuestDetailRequest guestDetailRequest,
            SecurityContext securityContext) throws NotFoundException
   {
      Thread.currentThread().setName("Guest-Api");
      JSONObject tokenVerify = null;
      dvLogger.info("Access-Token : " + accessToken);
      DetailsResponse response = new DetailsResponse();
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
      try
      {
         List<Map<String, String>> guestResult = new ArrayList<Map<String,String>>();
         List<Map<String, String>> result = DVPmsMain.getInstance()
                  .getDvPmsController().getDetails(guestDetailRequest, guestResult);
         Map<String, List<Map<String, String>>> resposeData =
                  new HashMap<String, List<Map<String, String>>>();
         Map<String, List<Map<String, String>>> guestResposeData =
                  new HashMap<String, List<Map<String, String>>>();
         resposeData.put("data", result);
         guestResposeData.put("data", guestResult);
         if (!result.isEmpty())
         {
            response.setStatus(true);
            response.setMessage("Success");
            response.setOpration("getDetails");
            response.setData(resposeData);
            response.setGuestData(guestResposeData);
            response.responseTag(200L);
            return Response.status(200).entity(response).build();
         }
         else
         {
            response.setStatus(false);
            response.setMessage("No Records Found");
            response.setOpration("getDetails");
            response.setData(null);
            response.responseTag(204L);
            return Response.status(204).entity(response).build();
         }
      }
      catch (Exception e)
      {
         response.setStatus(false);
         response.setMessage("Internal Server Error");
         response.setOpration("getDetails");
         response.responseTag(400L);
         dvLogger.error("Internal Server Error on fetching guest information: ", e);
         return Response.status(400).entity(response).build();
      }
   }
}
