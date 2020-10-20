package com.digivalet.pmsi.api.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.api.BillDetailsApiService;
import com.digivalet.pmsi.api.NotFoundException;
import com.digivalet.pmsi.datatypes.DVPmsBillData;
import com.digivalet.pmsi.events.DVBillEvent;
import com.digivalet.pmsi.events.DVBillEvent.BillFeatureEventType;
import com.digivalet.pmsi.events.DVSendPmsBill;
import com.digivalet.pmsi.model.ResponseData;
import com.digivalet.pmsi.model.SuccessResponse;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.result.DVResultMapper;


@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2017-11-03T08:52:10.265Z")
public class BillDetailsApiServiceImpl extends BillDetailsApiService
{
   DVLogger dvLogger = DVLogger.getInstance();

   @Override
   public Response getBillDetails(String accessToken,String AccessToken,
            @NotNull String roomNumber, @NotNull String guestId,
            @NotNull String hotelCode, SecurityContext securityContext)
            throws NotFoundException
   {
      Thread.currentThread().setName("BILL-DETAILS-API");
      JSONObject tokenVerify = null;
      dvLogger.info(" Inside get bill details");
      try
      {
         if(null==accessToken)
         {
            if(null!=AccessToken)
            {
               accessToken=AccessToken;
            }
         }
         dvLogger.info("accessToken  " + accessToken);
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
            DVPmsMain.getInstance().getDvPmsController().getBill(roomNumber,
                     guestId);
            
            try
            {
               File f = new File("/digivalet/pkg/cache/bill.json");
               dvLogger.info(" Exists "+f.exists() +" Directory "+ !f.isDirectory());
               if(f.exists() && !f.isDirectory()) 
               {
                  
                  Map<DVPmsBillData, Object> billData =
                           new HashMap<DVPmsBillData, Object>();
                  
                  billData.put(DVPmsBillData.guestId, guestId);
                  billData.put(DVPmsBillData.keyId, roomNumber);
                  dvLogger.info("Offline Bill ");
                  DVBillEvent dvEvent = new DVBillEvent(BillFeatureEventType.PMSI_BILL_EVENT,billData);
                  DVSendPmsBill dvSendPmsBill =new DVSendPmsBill(dvEvent, DVPmsMain.getInstance().getDVSettings(),
                           DVPmsMain.getInstance().getDvPmsDatabase(), DVPmsMain.getInstance().getCommunicationTokenManager());
                  
                  ExecutorService es = Executors.newSingleThreadExecutor();
                  es.submit(dvSendPmsBill);
               }

            }
            catch (Exception e)
            {
               dvLogger.error("Error in sending bill ", e);
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
            dvLogger.error("Error in getting bill details ", e);
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
