package com.digivalet.movieposting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import com.digivalet.core.DVLogger;
import com.digivalet.movies.DVMovieData;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.settings.DVSettings;
import com.digivalet.pmsi.util.DVDataToController;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DVSendPendingMovieToDevices implements Runnable
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private ArrayList<Integer> dvPendingEventDvcIds;
   private DVKeyCommunicationTokenManager communicationTokenManager;
   private int keyId;
   private int deviceId;
   private final int delay = 10 * 1000;

   public DVSendPendingMovieToDevices(int deviceId, DVPmsDatabase dvPmsDatabase,
            DVSettings dvSettings, ArrayList<Integer> dvPendingEventDvcIds,
            DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.deviceId = deviceId;
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvSettings = dvSettings;
      this.dvPendingEventDvcIds = dvPendingEventDvcIds;
      this.communicationTokenManager = communicationTokenManager;
   }

   @Override
   public void run()
   {
      try
      {
         Thread.currentThread().setName("MOVIE-DATA DEVICEID:" + deviceId);
         keyId = dvPmsDatabase.getKeyIdFromDvcID(deviceId);
         boolean checkIfKeyinSendingState =
                  dvPmsDatabase.isKeyInSendingState(keyId);
         Boolean status = false;
         if (!checkIfKeyinSendingState)
         {
            int deviceTypeId =
                     dvPmsDatabase.getDeviceTypeIdFromDeviceID(deviceId);
            String deviceType = dvPmsDatabase.getDeviceTypeFromId(deviceTypeId);
            String deviceIp = dvPmsDatabase.getIp(deviceId);
            ArrayList<Map<DVMovieData, Object>> movieData =
                     dvPmsDatabase.getMovieData(keyId);
            DVDataToController dataToController = new DVDataToController(
                     dvSettings, communicationTokenManager, keyId);
            status = dataToController.SendData(deviceIp,
                     getRequestJson(deviceType, deviceId + "", movieData),
                     deviceId);
         }
         else
         {
            dvLogger.info(
                     "Will not post movie since Checkin Checkout is in sending state ");
         }
         if (status)
         {
            dvPmsDatabase.updateMovieRoomStatus(deviceId, "true");
         }
         else
         {
            dvPmsDatabase.updateMovieRoomStatus(deviceId, "false");
         }
         if (null != dvPendingEventDvcIds)
         {
            Thread.sleep(delay);
            dvPendingEventDvcIds.remove(Integer.valueOf(this.deviceId));
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending pending movies to controller ", e);
      }
      dvLogger.info(" Finished Processing for pending movie for deviceId "+deviceId);
   }

   private String getRequestJson(String deviceType, String device_id,
            ArrayList<Map<DVMovieData, Object>> movieData)
   {
      try
      {
         JsonObject jsonRequest = new JsonObject();
         JsonObject jsonResonse = new JsonObject();
         JsonObject jsonDetails = new JsonObject();
         JsonArray jsonArray = new JsonArray();
         JsonArray jsonResponseArray = new JsonArray();
         String xml = "<movies>";

         if (!movieData.isEmpty())
         {
            for (Map<DVMovieData, Object> mvd : movieData)
            {
               String xmlMovie = "<movie>";
               JsonObject jsonMovieData = new JsonObject();

               jsonMovieData.addProperty(DVMovieData.alignment.toString(),
                        mvd.get(DVMovieData.alignment).toString());
               xmlMovie = xmlMovie + "<alignment>"
                        + mvd.get(DVMovieData.alignment).toString()
                        + "</alignment>";


               jsonMovieData.addProperty(DVMovieData.audioId.toString(),
                        mvd.get(DVMovieData.audioId).toString());
               xmlMovie = xmlMovie + "<audioId>"
                        + mvd.get(DVMovieData.audioId).toString()
                        + "</audioId>";


               jsonMovieData.addProperty(DVMovieData.dimension.toString(),
                        mvd.get(DVMovieData.dimension).toString());
               xmlMovie = xmlMovie + "<dimension>"
                        + mvd.get(DVMovieData.dimension).toString()
                        + "</dimension>";


               jsonMovieData.addProperty(DVMovieData.duration.toString(),
                        mvd.get(DVMovieData.duration).toString());
               xmlMovie = xmlMovie + "<duration>"
                        + mvd.get(DVMovieData.duration).toString()
                        + "</duration>";


               jsonMovieData.addProperty(DVMovieData.endTime.toString(),
                        (mvd.get(DVMovieData.endTime).toString()));
               xmlMovie = xmlMovie + "<endTime>"
                        + mvd.get(DVMovieData.endTime).toString()
                        + "</endTime>";


               jsonMovieData.addProperty(DVMovieData.isChargeable.toString(),
                        Boolean.parseBoolean(
                                 mvd.get(DVMovieData.isChargeable).toString()));
               xmlMovie = xmlMovie + "<isChargeable>"
                        + mvd.get(DVMovieData.isChargeable).toString()
                        + "</isChargeable>";


               jsonMovieData.addProperty(DVMovieData.isNeedToResume.toString(),
                        Boolean.parseBoolean(mvd.get(DVMovieData.isNeedToResume)
                                 .toString()));
               xmlMovie = xmlMovie + "<isNeedToResume>"
                        + mvd.get(DVMovieData.isNeedToResume).toString()
                        + "</isNeedToResume>";


               jsonMovieData.addProperty(DVMovieData.movieId.toString(),
                        mvd.get(DVMovieData.movieId).toString());
               xmlMovie = xmlMovie + "<movieId>"
                        + mvd.get(DVMovieData.movieId).toString()
                        + "</movieId>";


               jsonMovieData.addProperty(DVMovieData.seekPercent.toString(),
                        Float.parseFloat(
                                 mvd.get(DVMovieData.seekPercent).toString()));
               xmlMovie = xmlMovie + "<seekPercent>"
                        + mvd.get(DVMovieData.seekPercent).toString()
                        + "</seekPercent>";


               jsonMovieData.addProperty(DVMovieData.price.toString(), Float
                        .parseFloat(mvd.get(DVMovieData.price).toString()));
               xmlMovie = xmlMovie + "<price>"
                        + mvd.get(DVMovieData.price).toString() + "</price>";


               jsonMovieData.addProperty(DVMovieData.startTime.toString(),
                        (mvd.get(DVMovieData.startTime).toString()));
               xmlMovie = xmlMovie + "<startTime>"
                        + mvd.get(DVMovieData.startTime).toString()
                        + "</startTime>";


               jsonMovieData.addProperty(DVMovieData.subtitleId.toString(),
                        mvd.get(DVMovieData.subtitleId).toString());
               xmlMovie = xmlMovie + "<subtitleId>"
                        + mvd.get(DVMovieData.subtitleId).toString()
                        + "</subtitleId>";
               jsonArray.add(jsonMovieData);
               xmlMovie = xmlMovie + "</movie>";
               xml = xml + xmlMovie;
            }
         }
         xml = xml + "</movies>";

         jsonDetails.add("movieDetails", jsonArray);
         jsonDetails.addProperty("movieData", xml);


         jsonResonse.addProperty("feature", "movie");
         jsonResonse.addProperty("type", "moviedata");
         jsonResonse.addProperty("targetDeviceType", deviceType);
         jsonResonse.addProperty("targetDeviceId", device_id);


         jsonResonse.add("details", jsonDetails);
         jsonResponseArray.add(jsonResonse);
         jsonRequest.add("response", jsonResponseArray);
         jsonRequest.addProperty("deviceId", "Pmsi");
         jsonRequest.addProperty("timestamp", getDate() + "");
         return jsonRequest.toString();
      }
      catch (Exception e)
      {
         dvLogger.error("Error ", e);
         return "";
      }


   }

   public long getDate()
   {
      long epoch = 1499347012205L;
      try
      {
         SimpleDateFormat df =
                  new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
         df.setTimeZone(TimeZone.getTimeZone("UTC"));
         String date = df.format(new Date());
         epoch = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz")
                  .parse(date).getTime();
      }
      catch (Exception e)
      {

         dvLogger.error("ERROR OCCURRED while Parsing DATE ", e);
      }

      return epoch;
   }
}
