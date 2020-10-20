package com.digivalet.pmsi.api.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.json.JSONObject;
import com.digivalet.core.DVCheckinCheckoutEvent;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.api.CheckinCheckoutApiService;
import com.digivalet.pmsi.api.NotFoundException;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.events.DVEvent;
import com.digivalet.pmsi.events.DVEvent.FeatureEventType;
import com.digivalet.pmsi.model.CheckinCheckoutDetails;
import com.digivalet.pmsi.model.GuestDetails;
import com.digivalet.pmsi.model.ResponseData;
import com.digivalet.pmsi.model.SuccessResponse;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.result.DVResultMapper;

@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2018-06-06T16:23:07.286Z")
public class CheckinCheckoutApiServiceImpl extends CheckinCheckoutApiService
{
   static DVLogger dvLogger = DVLogger.getInstance();

   @Override
   public Response checkinCheckout(String accessToken,
            @NotNull String hotelCode, @NotNull String roomNumber,
            CheckinCheckoutDetails body, SecurityContext securityContext)
            throws NotFoundException
   {
      Thread.currentThread().setName("CHECKIN_CHECKOUT_API");
      JSONObject tokenVerify = null;
      dvLogger.info("CHECKIN_CHECKOUT_API");
      dvLogger.info("Body "+body.toString()+"  roomNumber "+roomNumber);
      try
      {
         if(null==roomNumber ||"".equalsIgnoreCase(roomNumber))
         {
            roomNumber=body.getGuestDetails().get(0).getRoomNumber();
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
            
            Map<DVPmsData, Object> data = new HashMap<DVPmsData, Object>();
            data.put(DVPmsData.keyId, roomNumber);
            DVEvent dvEvent = null;
            dvLogger.info(" OPERATION "+body.getOperation());
            if(body.getOperation().equalsIgnoreCase("CHECKIN"))
            {
               List<GuestDetails> guestDetails= body.getGuestDetails();
               data.put(DVPmsData.alternateName, guestDetails.get(0).getAlternateName());
               DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
               if(null==guestDetails.get(0).getArrivalDate() || "".equalsIgnoreCase(guestDetails.get(0).getArrivalDate()))
               {
                  Date date= new Date();
                  data.put(DVPmsData.arrivalDate,dateFormat.format(date));   
               }else
               {
                  data.put(DVPmsData.arrivalDate, guestDetails.get(0).getArrivalDate());
                  
               }
               
               if(null==guestDetails.get(0).getDepartureDate() || "".equalsIgnoreCase(guestDetails.get(0).getDepartureDate()))
               {
                  Date date= new Date();
                  Calendar c = Calendar.getInstance(); 
                  c.setTime(date); 
                  c.add(Calendar.DATE, 3);
                  date = c.getTime();
                  dvLogger.info("Departure Date  "+dateFormat.format(date));
                  data.put(DVPmsData.departureDate,dateFormat.format(date));
                  
               }else
               {
                  dvLogger.info("Departure Date  "+guestDetails.get(0).getDepartureDate());
                  data.put(DVPmsData.departureDate, guestDetails.get(0).getDepartureDate());
               }
               
               
               data.put(DVPmsData.emailId, guestDetails.get(0).getEmailId());
               data.put(DVPmsData.groupCode, guestDetails.get(0).getGroupCode());
               data.put(DVPmsData.guestFirstName, guestDetails.get(0).getGuestFirstName());
               data.put(DVPmsData.guestFullName, guestDetails.get(0).getGuestFullName());
               if(null==guestDetails.get(0).getGuestId() || "".equalsIgnoreCase(guestDetails.get(0).getGuestId()))
               {
                  Random rand = new Random(); 
                  int rand_int1 = rand.nextInt(1000000);
                  data.put(DVPmsData.guestId, rand_int1+"");
               }else
               {
                  data.put(DVPmsData.guestId, guestDetails.get(0).getGuestId()+"");   
               }
               data.put(DVPmsData.guestLanguage, guestDetails.get(0).getGuestLanguage());
               data.put(DVPmsData.guestLastName, guestDetails.get(0).getGuestLastName());
               if(null==guestDetails.get(0).getGuestName() || "".equalsIgnoreCase(guestDetails.get(0).getGuestName()))
               {
                  String guestName="";
                  try
                  {
                     
                     File file = new File("/digivalet/pkg/cache/guestName"); 
                     
                     BufferedReader br = new BufferedReader(new FileReader(file)); 
                     
                     String st; 
                     while ((st = br.readLine()) != null) 
                     {
                        guestName=guestName+st;
                     } 
                     br.close();
                  }
                  catch (Exception e)
                  {
                     // TODO: handle exception
                  }
                  if(null==guestName || "".equalsIgnoreCase(guestName))
                  {
                     data.put(DVPmsData.guestName, "Mr Kent Campbell");   
                  }else
                  {
                     data.put(DVPmsData.guestName, guestName);
                  }
                  
               }else
               {
                  data.put(DVPmsData.guestName, guestDetails.get(0).getGuestName());
               }
               
               
               data.put(DVPmsData.guestTitle, guestDetails.get(0).getGuestTitle());
               try
               {
                  if(null==guestDetails.get(0).getGuestType() || "".equalsIgnoreCase(guestDetails.get(0).getGuestType()))
                  {
                     data.put(DVPmsData.guestType, "primary");      
                  }else
                  {
                     data.put(DVPmsData.guestType, guestDetails.get(0).getGuestType());   
                  }
               }
               catch (Exception e)
               {
                  dvLogger.error("Error in getting guest details in chekcin chekcout api ", e);
               }
               
               data.put(DVPmsData.incognitoName, guestDetails.get(0).getIncognitoName());
               data.put(DVPmsData.videoRights, guestDetails.get(0).getVideoRights());
               data.put(DVPmsData.phoneNumber, guestDetails.get(0).getPhoneNumber());
               data.put(DVPmsData.tvRights,  guestDetails.get(0).getTvRights());
               data.put(DVPmsData.reservationId, guestDetails.get(0).getReservationId());
               data.put(DVPmsData.revisitFlag, guestDetails.get(0).getRevisitFlag());
               data.put(DVPmsData.remoteCheckout, guestDetails.get(0).getRemoteCheckout()+"");
               try
               {
                  if(null==guestDetails.get(0).getSafeFlag())
                  {
                     data.put(DVPmsData.safeFlag,"false");
                  }else
                  {
                     data.put(DVPmsData.safeFlag, guestDetails.get(0).getSafeFlag());   
                  }
               }
               catch (Exception e)
               {
                  dvLogger.error("Error in getting guest details ", e);
               }
               
//               data.put(DVPmsData.tvRights, roomNumber);
               data.put(DVPmsData.uniqueId, guestDetails.get(0).getUniqueId());
//               data.put(DVPmsData.videoRights, roomNumber);
               data.put(DVPmsData.vipStatus, guestDetails.get(0).getVipStatus());
               data.put(DVPmsData.keyId, roomNumber);
               data=   initializeByBlank(data);
               dvLogger.info("Guest Data for Checkin : "+data.toString());
               
               dvEvent=new DVEvent(FeatureEventType.PMSI_CHECKIN_EVENT, data);
            }else if(body.getOperation().equalsIgnoreCase("CHECKOUT"))
            {
               List<GuestDetails> guestDetails= body.getGuestDetails();
               data.put(DVPmsData.alternateName, guestDetails.get(0).getAlternateName());
               data.put(DVPmsData.arrivalDate, guestDetails.get(0).getArrivalDate());
//               data.put(DVPmsData.dateOfBirth, guestDetails.get(0).getd);
               data.put(DVPmsData.departureDate, guestDetails.get(0).getDepartureDate());
//               data.put(DVPmsData.emailId, roomNumber);
//               data.put(DVPmsData.groupCode, roomNumber);
               data.put(DVPmsData.guestFirstName, guestDetails.get(0).getGuestFirstName());
               data.put(DVPmsData.guestFullName, guestDetails.get(0).getGuestFullName());
               data.put(DVPmsData.guestId, guestDetails.get(0).getGuestId()+"");
               data.put(DVPmsData.guestLanguage, guestDetails.get(0).getGuestLanguage());
               data.put(DVPmsData.guestLastName, guestDetails.get(0).getGuestLastName());
               data.put(DVPmsData.guestName, guestDetails.get(0).getGuestName());
               data.put(DVPmsData.guestTitle, guestDetails.get(0).getGuestTitle());
               data.put(DVPmsData.guestType, guestDetails.get(0).getGuestType());
               data.put(DVPmsData.incognitoName, guestDetails.get(0).getIncognitoName());
//               data.put(DVPmsData.nationality, guestDetails.get(0).ge);
//               data.put(DVPmsData.phoneNumber, roomNumber);
//               data.put(DVPmsData.previousVisitDate, roomNumber);
//               data.put(DVPmsData.remoteCheckout, roomNumber);
//               data.put(DVPmsData.reservationId, roomNumber);
//               data.put(DVPmsData.revisitFlag, roomNumber);
//               data.put(DVPmsData.safeFlag, roomNumber);
//               data.put(DVPmsData.tvRights, roomNumber);
//               data.put(DVPmsData.uniqueId, roomNumber);
//               data.put(DVPmsData.videoRights, roomNumber);
//               data.put(DVPmsData.vipStatus, roomNumber);
               data=   initializeByBlank(data);
               dvEvent=new DVEvent(FeatureEventType.PMSI_CHECKOUT_EVENT, data);
            }
            DVCheckinCheckoutEvent dvCheckinCheckoutEvent =
                     new DVCheckinCheckoutEvent(dvEvent,
                              DVPmsMain.getInstance().getDVSettings(), DVPmsMain.getInstance().getDvPmsDatabase(),
                              DVPmsMain.getInstance().getCommunicationTokenManager());
            BlockingQueue<Runnable> threadPool =
                     new LinkedBlockingQueue<Runnable>();
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 10000L,
                     TimeUnit.MILLISECONDS, threadPool);
            executorService.submit(dvCheckinCheckoutEvent);
            
//            dvLogger.info(" Before sending response  ");
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
   
   
   
   private Map<DVPmsData, Object> initializeByBlank(
            Map<DVPmsData, Object> guestData)
   {
      Map<DVPmsData, Object> updatedGuestDate=guestData;
      try
      {
         for(Map.Entry<DVPmsData, Object> entry : guestData.entrySet()) 
         {
            DVPmsData key = entry.getKey();
            Object value = entry.getValue();
            
            if(null==value)
            {
               updatedGuestDate.put(key, "");
            }
        }
         DVPmsData[] yourEnums = DVPmsData.values();
         for(int i=0;i<yourEnums.length;i++)
         {
            if(!updatedGuestDate.containsKey(yourEnums[i]))
            {
               updatedGuestDate.put(yourEnums[i], "");   
            }
            
         }
         
      }
      catch (Exception e)
      {
         dvLogger.error("Error in initializing guest data ", e);
         return guestData;
      }
      
      return guestData;
   }
}
