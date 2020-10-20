package com.digivalet.pmsi.api.impl;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.api.CheckoutApiService;
import com.digivalet.pmsi.api.NotFoundException;
import com.digivalet.pmsi.model.CheckoutRequest;
import com.digivalet.pmsi.model.ResponseData;
import com.digivalet.pmsi.model.SuccessResponse;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.result.DVResultMapper;

@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2017-11-03T08:52:10.265Z")
public class CheckoutApiServiceImpl extends CheckoutApiService
{
   static DVLogger dvLogger = DVLogger.getInstance();

   @Override
   public Response checkOut(String accessToken,CheckoutRequest body,
            SecurityContext securityContext) throws NotFoundException
   {

      Thread.currentThread().setName("CHECKOUT-API");
      JSONObject tokenVerify = null;
      dvLogger.info(" Inside CHECKOUT");
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
      String guestId= body.getDetails().get(0).getGuestId();
      String roomNumber= body.getDetails().get(0).getRoomNumber();
      String hotelCode= body.getDetails().get(0).getHotelCode();
      String targetDeviceId=body.getDetails().get(0).getTargetDeviceId();
      request.put("roomNumber", roomNumber);
      request.put("guestId", guestId);
      request.put("hotelCode", hotelCode);
      request.put("targetDeviceId", targetDeviceId);
      dvLogger.info("room Number: "+roomNumber);
      dvLogger.info("guestId: "+roomNumber);
      dvLogger.info("hotelCode: "+hotelCode);
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
        	 valid=DVPmsMain.getInstance().getDvPmsController()
                     .remoteCheckout(roomNumber, guestId,targetDeviceId);
            dvLogger.info(" Before sending response  ");
            ResponseData response = new ResponseData();
            if (valid.getCode() == DVResult.SUCCESS)
            {
               
               DVPmsMain.getInstance().getDvPmsController().sendMail(body);
               
            	response.setMessage(DVPmsMain.getInstance().getDVSettings().getExpressCheckoutSuccessMessage());
                response.setResponseTag(200L);
                response.setStatus(true);
                List<SuccessResponse> success = new ArrayList<SuccessResponse>();
                SuccessResponse suc = new SuccessResponse();
                suc.setSuccess(true);
                success.add(suc);
                response.setData(success);	
            }else 
            {
            	response.setMessage(DVPmsMain.getInstance().getDVSettings().getExpressCheckoutFailureMessage());
                response.setResponseTag(251L);
                response.setStatus(false);
                List<SuccessResponse> success = new ArrayList<SuccessResponse>();
                SuccessResponse suc = new SuccessResponse();
                suc.setSuccess(false);
                success.add(suc);
                response.setData(success);
            }
            
            dvLogger.info("response data: " + response.toString());
            return Response.status(200).entity(response).build();
         }
         catch (Exception e)
         {
            dvLogger.error("Error in remote checkout ", e);
         }

      }
      else
      {
         DVResultMapper dvResultMapper = new DVResultMapper();
         int apiCode = dvResultMapper.getApiCode(valid.getCode());
         ResponseData response = new ResponseData();
         response.setMessage("Success ");
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
