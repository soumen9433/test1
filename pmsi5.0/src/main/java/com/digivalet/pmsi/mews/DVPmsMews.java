package com.digivalet.pmsi.mews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import com.digivalet.core.AlertState;
import com.digivalet.core.DVLogger;
import com.digivalet.movies.DVMovieEvent;
import com.digivalet.pmsi.DVPms;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVPMSMessageData;
import com.digivalet.pmsi.datatypes.DVPmsBillData;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.events.DVBillEvent.BillFeatureEventType;
import com.digivalet.pmsi.events.DVEvent.FeatureEventType;
import com.digivalet.pmsi.events.DVMessageEvent.MessageFeatureEvent;
import com.digivalet.pmsi.events.DVPmsEventNotifier;
import com.digivalet.pmsi.model.GuestData;
import com.digivalet.pmsi.model.MovieData;
import com.digivalet.pmsi.model.MovieDetails;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPmsMews extends DVPms implements DVPmsEventNotifier
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private static final int GUESTDATA_THREAD_DELAY = 60;

   // Id (Mews), Room-Number(Hotel's)
   public Map<String, String> spaceKeyMapping = new HashMap<>();
   public Map<String, String> spaceKeyReverseMapping = new HashMap<>();
   public DVMewsClient dvMewsClient;

   public DVPmsMews(DVSettings dvSettings, DVPmsDatabase dvPmsDatabase)
   {
      Thread.currentThread().setName("MEWS");
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;

      init();
   }


   public void init()
   {
      try
      {

         WSClientHandler dvReadDataFromPms =
                  new WSClientHandler(dvSettings, dvPmsDatabase);
         dvReadDataFromPms.start();
         dvReadDataFromPms.setName("WSClientHandler");

      }
      catch (Exception e)
      {
         dvLogger.error(
                  "Error in init of DVPmsMews while starting WSClientHandler\n",
                  e);
      }



      try
      {
         dvMewsClient = new DVMewsClient(dvSettings,this);
         dvMewsClient.start();
         dvMewsClient.setName("DVMewsClient");
      }
      catch (Exception e)
      {
         dvLogger.error(
                  "Error in init of DVPmsMews while starting DVMewsClient\n",
                  e);
      }



      try
      {
         DVMewsGetGuestDataThread getGuestDataThread =
                  new DVMewsGetGuestDataThread(dvSettings, dvPmsDatabase,this);
         Thread thread = new Thread(
                  new DVMewsGetGuestDataThread(dvSettings, dvPmsDatabase,this));
         thread.start();
         ScheduledExecutorService executor =
                  Executors.newScheduledThreadPool(1);

         executor.scheduleAtFixedRate(getGuestDataThread,
                  GUESTDATA_THREAD_DELAY, GUESTDATA_THREAD_DELAY,
                  TimeUnit.MINUTES);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in init of DVMewsGetGuestDataThread\n", e);
      }

   }


   @Override
   public DVResult getBill(String roomNumber, String guestId)
   {
      try
      {
         DVParseData dvParseData =
                  new DVParseData(new JSONObject(), dvPmsDatabase, dvSettings,this);
         dvParseData.getBill(guestId, roomNumber);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while fetching bill from PMS-Mews\n", e);
      }

      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Get Bill API executed.");
   }

   @Override
   public DVResult getMessage(String roomNumber, String guestId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public DVResult synchronize()
   {
      dvLogger.info("Starting Sync Thread");

      DVMewsSyncThread dvMewsSyncThread =
               new DVMewsSyncThread(dvSettings, dvPmsDatabase, this);
      dvMewsSyncThread.start();

      dvLogger.info("Sync Thread Started");

      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Sync initiated with MEWS");
   }

   @Override
   public DVResult setServiceState(String roomNumber, String serviceId,
            boolean state)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public DVResult postWakeupCall(String roomNumber, String date, String time,
            boolean state)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public DVResult remoteCheckout(String roomNumber, String guestId,
            String targetDeviceId)
   {
      try
      {
         int keyId = dvPmsDatabase.getKeyId(roomNumber);
         String arrivalDate = dvPmsDatabase.getArrivalTime(guestId, keyId);
         String departureDate = dvPmsDatabase.getDepartureTime(guestId, keyId);

         dvPmsDatabase.deleteRemoteCheckoutTargetDeviceId(roomNumber, guestId);
         dvPmsDatabase.insertRemoteCheckoutGuestId(targetDeviceId, roomNumber,
                  guestId);

         DVParseData dvParseData =
                  new DVParseData(new JSONObject(), dvPmsDatabase, dvSettings,this);
         boolean checkoutResponseStatus = dvParseData.remoteCheckout(roomNumber,
                  guestId, arrivalDate, departureDate, targetDeviceId);

         if (checkoutResponseStatus)
         {
            return new DVResult(DVResult.SUCCESS, "Service Data Send TO PMS ");
         }
         else
         {
            return new DVResult(DVResult.DVERROR_REMOTE_GO_FAILED,
                     "ERROR, Either Billing is pending or error occurred");
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in doing remote checkout ", e);
      }
      return new DVResult(DVResult.SUCCESS, "Service Data Send TO PMS ");
   }

   @Override
   public GuestData getGuestInformation(String roomNumber)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public DVResult shutDownPms()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   @Override
   public DVResult postMovie(String roomNumber, String guestId,
            MovieDetails data, DVMovieEvent movieEvent)
   {
      dvLogger.info("Currency " + dvSettings.getCurrencySymbol());

      try
      {

         if (movieEvent == DVMovieEvent.purchased)
         {

            dvLogger.info("Movie Purchased from Room " + roomNumber + "  ");
            // String pmsiKeyId = dvPmsDatabase.getPmsiRoomId(roomNumber);

            int keyId = dvPmsDatabase.getKeyId(roomNumber);
            int moviePostingId = dvPmsDatabase.getLastMoviePostingId();
            moviePostingId = moviePostingId + 1;

            MovieData movieData = data.getDetails().get(0);
            // String consuptionUtc = movieData.getEndTime().toString();

            String movieId = movieData.getMovieId().trim();
            String price = dvPmsDatabase.getMoviePriceFromKeyIdMovieId(movieId,
                     keyId);

            Double priceInDouble = Double.parseDouble(price);

            dvLogger.info("price :  " + price);

            int movieKeyDataId =
                     dvPmsDatabase.getMovieKeyDataId(movieId, keyId);

            JSONObject requestJson = new JSONObject();
            requestJson.put("ClientToken", dvSettings.getPmsClientToken());
            requestJson.put("AccessToken", dvSettings.getPmsAccessToken());
            requestJson.put("CustomerId", guestId);
            requestJson.put("ServiceId", dvSettings.getServiceIdData());

            ArrayList itemsArray = new ArrayList();

            HashMap dataMap = new HashMap();

            dataMap.put("Name", "In House Movie");
            dataMap.put("UnitCount", 1);

            HashMap unitAmount = new HashMap();

            unitAmount.put("Currency", dvSettings.getCurrencySymbol());
            unitAmount.put("GrossValue", priceInDouble);
            dataMap.put("UnitAmount", unitAmount);
            itemsArray.add(dataMap);
            requestJson.put("Items", itemsArray);

            JSONObject Json = dvMewsClient.addNewBill(requestJson);

            dvLogger.info("Bill Response Mews : " + Json);

            dvPmsDatabase.insertMoviePostingRecords(movieKeyDataId, 1, keyId);

            return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
                     "Mews Bill Response Got Successfully");
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in posting movie ", e);
      }
      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Posted changes to PMS");
   }

   @Override
   public AlertState connectionStatus()
   {
      return AlertState.Na;
   }

   @Override
   public void notifyPmsEvent(FeatureEventType featureEventType,
            Map<DVPmsData, Object> data)
   {
      notifyPmsEvents(featureEventType, data);

   }

   @Override
   public void notifyPmsBillEvent(BillFeatureEventType featureEventType,
            Map<DVPmsBillData, Object> data)
   {
      notifyPmsBillEvents(featureEventType, data);
   }


   @Override
   public void notifyMessageEvent(MessageFeatureEvent featureEventType,
            Map<DVPMSMessageData, Object> messageData)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public DVResult postPendingMovie(int pendingId)
   {
      // TODO Auto-generated method stub
      return null;
   }


   @Override
   public String getErrorLog()
   {
      // TODO Auto-generated method stub
      return null;
   }
}
