package com.digivalet.pmsi.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsiStatus;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVDeviceTypes;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.settings.DVSettings;
import com.digivalet.pmsi.util.DVDataToController;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DVNormalCheckout
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVPmsDatabase dvPmsDatabase;
   private int keyId = 0;
   private DVSettings dvSettings;
   private ArrayList<Integer> InRoomDevices;
   private ArrayList<Integer> XplayerUi;
   private int pmsiGuestID = 0;
   private String guestType;
   private Map<DVPmsData, Object> data;
   DVKeyCommunicationTokenManager communicationTokenManager;

   public DVNormalCheckout(DVPmsDatabase dvPmsDatabase, int keyId,
            DVSettings dvSettings, ArrayList<Integer> InRoomDevices,
            ArrayList<Integer> XplayerUi, int pmsiGuestID, String guestType,
            Map<DVPmsData, Object> data,DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.dvPmsDatabase=dvPmsDatabase;
      this.keyId=keyId;
      this.dvSettings=dvSettings;
      this.InRoomDevices=InRoomDevices;
      this.XplayerUi=XplayerUi;
      this.pmsiGuestID=pmsiGuestID;
      this.guestType=guestType;
      this.data=data;
      this.communicationTokenManager=communicationTokenManager;
   }
   
   private void checkout()
   {
      try
      {
         dvLogger.info("Checking out ");
         
         dvPmsDatabase.updateKeyStatusDigivaletStatus(
                  guestType , keyId,
                  dvPmsDatabase.getMasterStatusId(
                           DVPmsiStatus.SENDING_CHECKOUT.toString()));
         
         
         
         boolean SentToallDevices = true;

         ArrayList<Integer> controllers =
                  dvPmsDatabase.getDvcByKey(keyId);

         Iterator<Integer> dvcItrator = controllers.iterator();
         
         DVDataToController dataToController =
                  new DVDataToController(dvSettings,communicationTokenManager,keyId);
         while (dvcItrator.hasNext())
         {
            boolean status = false;
            int controllerId = dvcItrator.next();
            int dvsId = dvPmsDatabase.getDvsDeviceId(controllerId);
            String deviceIp = dvPmsDatabase.getIp(controllerId);
            String jsonRequest =
                     getRequestJson(DVDeviceTypes.dvc.toString(),
                              dvsId + "", DVPmsiStatus.checkout.toString());
            status = dataToController.SendData(deviceIp, jsonRequest,dvsId);

            if (status)
            {
               dvLogger.info(" Checkout  Success  for  IP  " + deviceIp);
               dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                        controllerId, pmsiGuestID,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.checkout.toString()),
                        guestType);
               Iterator<Integer> inRoomDeviceIt =
                        InRoomDevices.iterator();
               while (inRoomDeviceIt.hasNext())
               {
                  int inRoomDevice = inRoomDeviceIt.next();
                  int inRoomdvsId =
                           dvPmsDatabase.getDvsDeviceId(inRoomDevice);
                  jsonRequest = getRequestJson(DVDeviceTypes.ipad.toString(),
                           inRoomdvsId + "",
                           DVPmsiStatus.checkout.toString());
                  boolean InRoomstatus = false;

                  
                     InRoomstatus = dataToController.SendData(deviceIp,
                              jsonRequest,inRoomdvsId);
                  if (InRoomstatus)
                  {
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                              inRoomDevice, pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkout.toString()),
                              guestType);
                     inRoomDeviceIt.remove();
                  }
               }
            }
            else
            {
               dvLogger.info(" Checkout  Failed  for  IP  " + deviceIp);
               SentToallDevices = false;
               dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                        controllerId, pmsiGuestID,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_CHECKOUT.toString()),
                        guestType);
            }
         }
         for (int inRoomDevice : InRoomDevices)
         {
            SentToallDevices = false;
            dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, inRoomDevice,
                     pmsiGuestID,
                     dvPmsDatabase.getMasterStatusId(
                              DVPmsiStatus.PENDING_CHECKOUT.toString()),
                     guestType);
         }
         Iterator<Integer> xplayerUiIt = XplayerUi.iterator();
         while (xplayerUiIt.hasNext())
         {
            int xplayerUi = xplayerUiIt.next();
            String deviceIp = dvPmsDatabase.getIp(xplayerUi);
            int inRoomdvsId = dvPmsDatabase.getDvsDeviceId(xplayerUi);
            String jsonRequest = getRequestJson(DVDeviceTypes.tvui.toString(),
                     inRoomdvsId + "", DVPmsiStatus.checkout.toString());
            boolean status = false;

               status = dataToController.SendData(deviceIp, jsonRequest,inRoomdvsId);
            if (status)
            {
               dvLogger.info(" Checkout  Success  for Xplayer  IP  "
                        + deviceIp);
               dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                        pmsiGuestID,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.checkout.toString()),
                        guestType);
            }
            else
            {
               dvLogger.info(
                        " Checkout  Failed  for Xplayer IP  " + deviceIp);
               SentToallDevices = false;
               dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                        pmsiGuestID,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_CHECKOUT.toString()),
                        guestType);
            }
         }
         if (SentToallDevices)
         {
            dvPmsDatabase.updateKeyStatusDigivaletStatus(
                     guestType , keyId,
                     dvPmsDatabase.getMasterStatusId(
                              DVPmsiStatus.checkout.toString()));
            
            
         }
         else
         {
           
            dvPmsDatabase.updateKeyStatusDigivaletStatus(
                     guestType , keyId,
                     dvPmsDatabase.getMasterStatusId(
                              DVPmsiStatus.PENDING_CHECKOUT.toString()));
            
         }
      
      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending normal checkout ", e);
      }
   }
   
   
   private String getRequestJson(String deviceType, String device_id,
            String type)
   {
      if(type.equalsIgnoreCase("checkin"))
      {
         if(data.get(DVPmsData.safeFlag).toString().equalsIgnoreCase("true"))
         {
            type="safeCheckin";
         }
      }
      Gson gson = new Gson();
      String jsondet = gson.toJson(data);
      JsonParser parser = new JsonParser();
      JsonObject jsondeto = parser.parse(jsondet).getAsJsonObject();
      JsonObject jsonRequest = new JsonObject();
      JsonObject jsonResonse = new JsonObject();
      JsonObject jsonDetails = new JsonObject();
      JsonArray jsonArray = new JsonArray();
      JsonArray jsonResponseArray = new JsonArray();
      JsonArray activeGuestsArray = new JsonArray();
      ArrayList<String> activeGuest=dvPmsDatabase.getActiveGuestIds(keyId);
      for(int i=0;i<activeGuest.size();i++)
      {
         activeGuestsArray.add(activeGuest.get(i));
      }
      jsonDetails.add("activeGuestId", activeGuestsArray);
      jsonArray.add(jsondeto);

      // jsonDetails.addProperty("deviceType", deviceType);
      // jsonDetails.addProperty("deviceId", device_id);
      jsonDetails.add("guestDetails", jsonArray);
      jsonResonse.addProperty("feature", "room");
      jsonResonse.addProperty("type", type);
      jsonResonse.addProperty("targetDeviceType", deviceType);
      jsonResonse.addProperty("targetDeviceId", device_id);
      jsonResonse.add("details", jsonDetails);
      jsonResponseArray.add(jsonResonse);
      jsonRequest.add("response", jsonResponseArray);
      jsonRequest.addProperty("deviceId", "Pmsi");
//      jsonRequest.addProperty("timestamp", getDate() + "");
      return jsonRequest.toString();
   }
}
