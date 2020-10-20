package com.digivalet.pmsi.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVDeviceTypes;
import com.digivalet.pmsi.settings.DVSettings;
import com.digivalet.pmsi.util.DVDataToController;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DVSendCheckoutFailMessage implements Runnable
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private String message;
   private String roomNumber;
   private String guestId;
   private ArrayList<Integer> NonDvcInRoomDevices = new ArrayList<Integer>();
   private DVKeyCommunicationTokenManager communicationTokenManager;
   int keyId = 0;

   public DVSendCheckoutFailMessage(DVSettings dvSettings,
            DVPmsDatabase dvPmsDatabase, String message, String roomNumber,
            String guestId,
            DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.message = message;
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      this.roomNumber = roomNumber;
      this.guestId = guestId;
      this.communicationTokenManager = communicationTokenManager;
   }

   @Override
   public void run()
   {
      try
      {
         keyId = dvPmsDatabase.getKeyId(roomNumber);
         populateNonDvcInRoomDevices(keyId);
         int targetDeviceId = dvPmsDatabase
                  .getRemoteCheckoutTargetDeviceId(roomNumber, guestId);
         String json = getRequestJson(DVDeviceTypes.ipad.toString(),
                  targetDeviceId+"", "expressCheckoutFailed", message);
         dvLogger.info("Fail Json: "+json);
         ArrayList<Integer> dvcs = dvPmsDatabase.getDvcByKey(keyId);
         for (int dvc : dvcs)
         {
            SendCheckoutToDevices checkoutToDevices = new SendCheckoutToDevices(
                     dvPmsDatabase.getIp(dvc), json, dvc);
            checkoutToDevices.start();

         }
         for (int nonInRoomDevices : NonDvcInRoomDevices)
         {
            SendCheckoutToDevices checkoutToDevices = new SendCheckoutToDevices(
                     dvPmsDatabase.getIp(nonInRoomDevices), json,
                     nonInRoomDevices);
            checkoutToDevices.start();
         }
         Thread.sleep(20*1000);
         dvPmsDatabase.deleteRemoteCheckoutTargetDeviceId(roomNumber, guestId);

      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending failed bill message event ", e);
      }
   }

   class SendCheckoutToDevices extends Thread
   {
      String deviceIp;
      String jsonRequest;
      int dvcId;

      public SendCheckoutToDevices(String deviceIp, String jsonRequest,
               int dvcId)
      {
         this.deviceIp = deviceIp;
         this.jsonRequest = jsonRequest;
         this.dvcId = dvcId;
      }

      public void run()
      {
         try
         {
            DVDataToController dataToController = new DVDataToController(
                     dvSettings, communicationTokenManager, keyId);
            boolean status =
                     dataToController.SendData(deviceIp, jsonRequest, dvcId);
            dvLogger.info(
                     "Status of bill for ip " + deviceIp + " is " + status);
         }
         catch (Exception e)
         {
            dvLogger.error("Error in sending bill", e);
         }
      }

   }


   private String getRequestJson(String deviceType, String device_id, String type,
            String message)
   {
      Gson gson = new Gson();
      HashMap<String, String> dataMap = new HashMap<String, String>();

      dataMap.put("message", message);
      String jsondet = gson.toJson(dataMap);
      JsonParser parser = new JsonParser();
      JsonObject jsondeto = parser.parse(jsondet).getAsJsonObject();
      JsonObject jsonRequest = new JsonObject();
      JsonObject jsonResonse = new JsonObject();
      JsonObject jsonDetails = new JsonObject();
      JsonArray jsonArray = new JsonArray();
      JsonArray jsonResponseArray = new JsonArray();
      jsonArray.add(jsondeto);

      jsonDetails.add("data", jsonArray);

      jsonResonse.addProperty("feature", "room");
      jsonResonse.addProperty("type", type);
      jsonResonse.addProperty("targetDeviceType", deviceType);
      jsonResonse.addProperty("targetDeviceId", device_id);

      jsonResonse.add("details", jsonDetails);
      jsonResponseArray.add(jsonResonse);
      jsonRequest.add("response", jsonResponseArray);
      jsonRequest.addProperty("deviceId", "Pmsi");
      jsonRequest.addProperty("timestamp", getDate() + "");
      return jsonRequest.toString();
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

   private void populateNonDvcInRoomDevices(int dvKeyId)
   {
      try
      {
         NonDvcInRoomDevices = dvPmsDatabase.populateDevices(dvKeyId,
                  DVDeviceTypes.ipad.toString(), 0);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Room Devices ", e);
      }
   }
}
