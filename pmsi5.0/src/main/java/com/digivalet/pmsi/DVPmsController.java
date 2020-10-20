package com.digivalet.pmsi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import com.digivalet.core.AlertState;
import com.digivalet.core.DVCheckinCheckoutEvent;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsFactory;
import com.digivalet.core.DVPmsMain;
import com.digivalet.core.DVPmsValidation;
import com.digivalet.core.DVUpdateNotifier;
import com.digivalet.movieposting.DVMoviePostingManager;
import com.digivalet.movies.DVMovieEvent;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.events.DVEvent;
import com.digivalet.pmsi.events.DVEvent.FeatureEventType;
import com.digivalet.pmsi.model.CheckoutRequest;
import com.digivalet.pmsi.model.GuestData;
import com.digivalet.pmsi.model.GuestDetailRequest;
import com.digivalet.pmsi.model.GuestPreferenceRequest;
import com.digivalet.pmsi.model.Items;
import com.digivalet.pmsi.model.MovieDetails;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPmsController implements DVPmsConnector
{
   static DVLogger dvLogger = DVLogger.getInstance();

   private DVPms dvPms;
   private DVUpdateNotifier dvUpdateNotifier;
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private DVPmsValidation dvPmsValidation;
   private DVMoviePostingManager dvMoviePostingManager;
   

   public DVPmsController(DVSettings dvSettings,
            DVUpdateNotifier dvUpdateNotifier, DVPmsDatabase dvPmsDatabase,
            DVPmsValidation dvPmsValidation,
            DVMoviePostingManager dvMoviePostingManager)
   {
      this.dvSettings = dvSettings;
      this.dvUpdateNotifier = dvUpdateNotifier;
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvPmsValidation = dvPmsValidation;
      this.dvMoviePostingManager = dvMoviePostingManager;
      init();
   }

   private void init()
   {
      dvPms = new DVPmsFactory(dvSettings, dvPmsDatabase).getPms();
      dvPms.attachEventListener(dvUpdateNotifier);
   }


   @Override
   public DVResult getBill(String roomId, String guestId)
   {
      return dvPms.getBill(dvPmsDatabase.getPmsiRoomId(roomId), guestId);
   }

   @Override
   public DVResult getMessage(String roomId, String guestId)
   {
      return dvPms.getMessage(roomId, guestId);
   }

   @Override
   public DVResult synchronize()
   {
      return dvPms.synchronize();
   }

   @Override
   public DVResult setServiceState(String roomId, String serviceId,
            boolean state)
   {
      return dvPms.setServiceState(dvPmsDatabase.getPmsiRoomId(roomId),
               serviceId, state);
   }

   @Override
   public DVResult postWakeupCall(String roomId, String date, String time,
            boolean state)
   {
      return dvPms.postWakeupCall(dvPmsDatabase.getPmsiRoomId(roomId), date,
               time, state);
   }

   @Override
   public DVResult remoteCheckout(String roomId, String guestId,String targetDeviceId)
   {
      return dvPms.remoteCheckout(dvPmsDatabase.getPmsiRoomId(roomId), guestId,targetDeviceId);
   }

   public DVResult validateRequest(JSONObject request)
   {
      if (request.has("hotelCode"))
      {
         DVResult result = dvPmsValidation
                  .validateHotelCode(request.getString("hotelCode"));
         dvLogger.info("Result Log: code=" + result.getCode()
                  + ",  description=" + result.getDescription());
         if (result.getCode() != DVResult.SUCCESS)
         {
            return result;
         }
      }
      if (request.has("roomNumber"))
      {
         DVResult result = dvPmsValidation
                  .validateRoomNumber(request.getString("roomNumber"));
         dvLogger.info("Result Log: code=" + result.getCode()
                  + ",  description=" + result.getDescription());
         if (result.getCode() != DVResult.SUCCESS)
         {
            return result;
         }
      }
      if (request.has("guestId"))
      {
         DVResult result =
                  dvPmsValidation.validateGuestId(request.getString("guestId"));
         dvLogger.info("Result Log: code=" + result.getCode()
                  + ",  description=" + result.getDescription());
         if (result.getCode() != DVResult.SUCCESS)
         {
            return result;
         }
      }
      if (request.has("hotelShortName"))
      {
         String hotelId = dvPmsDatabase
                  .getHotelIdByCode(request.getString("hotelShortName"));

         DVResult result = dvPmsValidation.validateHotelCode(hotelId);
         dvLogger.info("Result Log: code=" + result.getCode()
                  + ",  description=" + result.getDescription());
         if (result.getCode() != DVResult.SUCCESS)
         {
            return result;
         }
      }

      /**
       * Reservation Number will be validated, when the guest preference is
       * filled post reservation.
       */

      if (request.has("reservationNumber"))
      {
         String reservationNo = request.getString("reservationNumber");

         DVResult result = dvPmsValidation.validateReservationNo(reservationNo);
         dvLogger.info("Result Log: code=" + result.getCode()
                  + ",  description=" + result.getDescription());
         if (result.getCode() != DVResult.SUCCESS)
         {
            return result;
         }

      }

      if (request.has("moodId"))
      {
         DVResult result = dvPmsValidation
                  .validateMoodId(request.getString("moodId"));
         dvLogger.info("Result Log: code=" + result.getCode()
                  + ",  description=" + result.getDescription());
         
         if (result.getCode() != DVResult.SUCCESS)
         {
            return result;
         }
      }

      return new DVResult(DVResult.SUCCESS, "Validation successful");
   }

   @Override
   public GuestData getGuestInformation(String roomNumber)
   {
      return dvPms.getGuestInformation(roomNumber);
   }

   @Override
   public DVResult shutDownPms()
   {
      return dvPms.shutDownPms();
   }

   @Override
   public DVResult postMovie(String roomNumber, String guestId,
            MovieDetails data, DVMovieEvent movieEvent)
   {
      dvPms.postMovie(roomNumber, guestId, data, movieEvent);
      return dvMoviePostingManager.postMovie(roomNumber, guestId, data,
               movieEvent);
   }

   @Override
   public AlertState connectionStatus()
   {
      return dvPms.connectionStatus();
   }

   @Override
   public DVResult postPendingMovie(int pendingId)
   {
      return dvPms.postPendingMovie(pendingId);
   }
   
   public DVPms getPms()
   {
      return dvPms;
   }
   
   public DVResult guestArrivalPreference(GuestPreferenceRequest preference)
   {
      boolean checkinStatus = false;
      int keyId=dvPmsDatabase.getKeyId(preference.getDetails().get(0).getPreferenceDetails()
               .get(0).getRoomNumber());
      
      String guestId = preference.getDetails().get(0).getPreferenceDetails()
               .get(0).getGuestId();
      String reservationNumber = preference.getDetails().get(0)
               .getPreferenceDetails().get(0).getReservationNumber();
      
      if (null != reservationNumber
               && !"null".equalsIgnoreCase(reservationNumber)
               && !"".equalsIgnoreCase(reservationNumber))
      {
         checkinStatus = dvPmsDatabase.checkCheckinStatusByReservationId(reservationNumber);
      }
      else if (null != guestId && !"null".equalsIgnoreCase(guestId)
               && !"".equalsIgnoreCase(guestId))
      {
         checkinStatus = dvPmsDatabase.checkCheckinStatusByGuestId(guestId);
      }
      else
      {
         checkinStatus = dvPmsDatabase.checkKeyPmsiCheckedInStatus(keyId, "primary");
      }
      
      if (!checkinStatus)
      {
         return preCheckinGuestPreference(preference);
      }
      else
      {
         return postCheckinGuestPreference(preference);
      }
   }

   private DVResult preCheckinGuestPreference(GuestPreferenceRequest preference)
   {
      return dvPmsDatabase.addGuestPreference(
               preference.getDetails().get(0).getPreferenceDetails().get(0),
               dvPmsDatabase.getUserId(dvSettings.getUserCode()));
   }
   
   private DVResult postCheckinGuestPreference(GuestPreferenceRequest preference)
   {
      boolean process = false;

      String guestId = preference.getDetails().get(0).getPreferenceDetails()
               .get(0).getGuestId();
      String reservationNumber = preference.getDetails().get(0)
               .getPreferenceDetails().get(0).getReservationNumber();
      int keyId = dvPmsDatabase.getKeyId(preference.getDetails().get(0).getPreferenceDetails().get(0).getRoomNumber());

      Map<DVPmsData, Object> data = new HashMap<>();

      if (null != guestId && !"null".equalsIgnoreCase(guestId)
               && !"".equalsIgnoreCase(guestId))
      {
         data = dvPmsDatabase.getDataByGuestId(guestId, keyId);
         process = true;
      }
      else if (null != reservationNumber
               && !"null".equalsIgnoreCase(reservationNumber)
               && !"".equalsIgnoreCase(reservationNumber))
      {
         data = dvPmsDatabase.getDataByReservationId(reservationNumber);
         process = true;
      }
      else if(0 != keyId) 
      {
         guestId = dvPmsDatabase.getGuestIdByKey(keyId, "primary");
         
         data = dvPmsDatabase.getDataByGuestId(guestId,keyId);
         process = true;
      }

      if (process)
      {
         dvPmsDatabase.addGuestPreference(
                  preference.getDetails().get(0).getPreferenceDetails().get(0),
                  dvPmsDatabase.getUserId(dvSettings.getUserCode()));
         String dvcMoodId = dvPmsDatabase.getDvcMoodByInterfaceMoodId(preference
                  .getDetails().get(0).getPreferenceDetails().get(0).getMood());

         data.put(DVPmsData.welcomeMoodId, dvcMoodId);
         data.put(DVPmsData.temperature, preference.getDetails().get(0)
                  .getPreferenceDetails().get(0).getTemperature());
         data.put(DVPmsData.fragrance, preference.getDetails().get(0)
                  .getPreferenceDetails().get(0).getFragrance());

         DVEvent dvEvent = new DVEvent(
                  FeatureEventType.PMSI_GUEST_INFORMATION_UPDATE_EVENT, data);
         try
         {
            DVCheckinCheckoutEvent dvCheckinCheckoutEvent =
                     new DVCheckinCheckoutEvent(dvEvent,
                              DVPmsMain.getInstance().getDVSettings(),
                              DVPmsMain.getInstance().getDvPmsDatabase(),
                              DVPmsMain.getInstance()
                                       .getCommunicationTokenManager(),false);
            BlockingQueue<Runnable> threadPool = new LinkedBlockingQueue<>();
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1,
                     10000L, TimeUnit.MILLISECONDS, threadPool);
            executorService.submit(dvCheckinCheckoutEvent);
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while postCheckinGuestPreference \n", e);
         }
      }

      return new DVResult(DVResult.SUCCESS,
               "Preferences Added Successfully");
   }

   @Override
   public String getErrorLog()
   {
      return dvPms.getErrorLog();
   }
   
   public void sendMail(CheckoutRequest body)
   {
      try
      {
         String token= DVPmsMain.getInstance().getDvTokenValidation().getAuthToken();
         
         String guestId = body.getDetails().get(0).getGuestId();
         String roomNumber= body.getDetails().get(0).getRoomNumber();
         int keyId=dvPmsDatabase.getKeyId(roomNumber);
         String guestName = dvPmsDatabase.getGuestName(guestId, keyId);
         
         String url = dvSettings.getPrinterMailerUrl();
         String mailSendTo = dvSettings.getMailSendTo();
         String mailSubject= dvSettings.getMailSubject();
         mailSubject= mailSubject.replace("%(roomnum)", roomNumber);
         String mailBody = dvSettings.getMailBody();
         mailBody = mailBody.replace("%(guestName)", guestName);
         mailBody = mailBody.replace("%(roomnum)", roomNumber);
         
         
         
         String requestBody="{\n" + 
                  "    \"details\": [\n" + 
                  "        {\n" + 
                  "            \"postDetails\": [\n" + 
                  "                {\n" + 
                  "                    \"orderId\": \"\",\n" + 
                  "                    \"orderType\": \"PMSI\",\n" + 
                  "                    \"source\": \"\",\n" + 
                  "                    \"to\": \""+mailSendTo+"\",\n" + 
                  "                    \"cc\": \"\",\n" + 
                  "                    \"bcc\": \"\",\n" + 
                  "                    \"subject\": \""+mailSubject+"\",\n" + 
                  "                    \"body\": \""+mailBody+"\"\n" + 
                  "                }\n" + 
                  "            ]\n" + 
                  "        }\n" + 
                  "    ],\n" + 
                  "    \"feature\": \"Check-Out\",\n" + 
                  "    \"operation\": \"email\"\n" + 
                  "}";
         dvLogger.info("url "+url+" "+requestBody);
         HttpClient httpclient = HttpClients.createDefault();
         HttpPost httppost = new HttpPost(url);
         httppost.setHeader("Content-type", "application/vnd.digivalet.v1+json");
         httppost.setHeader("Access-Token", token);
         
         StringEntity stringEntity = new StringEntity(requestBody);
         httppost.getRequestLine();
         httppost.setEntity(stringEntity);

         
         HttpResponse response = httpclient.execute(httppost);
         dvLogger.info("Response On Remote Check-Out sendMail : "+ response.toString());
      }
      catch (Exception e)
      {
         dvLogger.error("Error At Remote Check-Out sendMail", e);
      }
      
   }

   public List<Map<String, String>> getDetails(GuestDetailRequest guestDetailRequest, List<Map<String, String>> guestResult)
   { 
    String request_params=guestDetailRequest.getRequestParams();  
    List<Items> condition_params= guestDetailRequest.getConditionParams();
    String conditional_operator=guestDetailRequest.getConditionalOperator();     
    
      return dvPmsDatabase.getUserDetails(request_params,condition_params,conditional_operator, guestResult);
   }
}
