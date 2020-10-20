package com.digivalet.pmsi.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
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

public class DVSendGuestInformationUpdate
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private int keyId = 0;
   private String hotelId = "";
   private int pmsiGuestID = 0;
   private DVPmsDatabase dvPmsDatabase;
   private Map<DVPmsData, Object> data;
   private ArrayList<Integer> InRoomDevices;
   private ArrayList<Integer> XplayerUi;
   private ArrayList<Integer> NonDvcInRoomDevices;
   private final int reCheckDelay = 5000;
   private DVKeyCommunicationTokenManager communicationTokenManager;

   public DVSendGuestInformationUpdate(DVSettings dvSettings, int keyId,
            String hotelId, int pmsiGuestID, DVPmsDatabase dvPmsDatabase,
            Map<DVPmsData, Object> data, ArrayList<Integer> InRoomDevices,
            ArrayList<Integer> XplayerUi,
            DVKeyCommunicationTokenManager communicationTokenManager,
            ArrayList<Integer> NonDvcInRoomDevices)
   {
      this.dvSettings = dvSettings;
      this.keyId = keyId;
      this.hotelId = hotelId;
      this.pmsiGuestID = pmsiGuestID;
      this.dvPmsDatabase = dvPmsDatabase;
      this.data = data;
      this.InRoomDevices = InRoomDevices;
      this.XplayerUi = XplayerUi;
      this.communicationTokenManager = communicationTokenManager;
      this.NonDvcInRoomDevices = NonDvcInRoomDevices;
   }



   public void sendGuestInfoUpdateToDevices()
   {
      try
      {
         Thread.currentThread().setName("GUESTINFOUPDATE KEY:" + keyId);
         dvLogger.info("Now guest info update to Devices ");
         boolean sendCheckin = false;
         boolean discardCheckin = false;
         String pmsi_status = "";
         while (sendCheckin != true)
         {
            try
            {
               String status = "";
               status = dvPmsDatabase.getDigivaletKeyStatusByGuestType(keyId,
                        data.get(DVPmsData.guestType).toString());
               pmsi_status = dvPmsDatabase.getPmsiKeyStatusByGuestType(keyId,
                        data.get(DVPmsData.guestType).toString());

               if (!pmsi_status
                        .equalsIgnoreCase(dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.checkin.toString()) + "")
                        && !pmsi_status.equalsIgnoreCase(dvPmsDatabase
                                 .getMasterStatusId(DVPmsiStatus.PENDING_CHECKIN
                                          .toString())
                                 + "")
                        && !pmsi_status.equalsIgnoreCase(
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                   .toString())
                                          + ""))
               {
                  discardCheckin = true;
                  break;
               }

               if (!status.equalsIgnoreCase(dvPmsDatabase.getMasterStatusId(
                        DVPmsiStatus.SENDING_CHECKIN.toString()) + "")
                        && !status.equalsIgnoreCase(
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.SENDING_CHECKOUT
                                                   .toString())
                                          + "")
                        && !status.equalsIgnoreCase(
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.SENDING_GUEST_INFO_UPDATE
                                                   .toString())
                                          + ""))
               {
                  sendCheckin = true;
                  dvLogger.info(
                           "Got the Green flag to send guest Info Update ");
                  break;
               }
               if (!sendCheckin)
               {
                  Thread.sleep(reCheckDelay);
               }
            }
            catch (Exception e)
            {
               dvLogger.error("Error in guestInfo sending state ", e);
               break;
            }
         }
         if (!discardCheckin)
         {
            String Updatequery =
                     "UPDATE `pmsi_key_status` SET `digivalet_status`="
                              + dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.SENDING_GUEST_INFO_UPDATE
                                                .toString())
                              + " where `key_id`=" + keyId
                              + " and `guest_type`='"
                              + data.get(DVPmsData.guestType).toString() + "'";
            dvLogger.info("Update: " + Updatequery);

            ArrayList<Integer> controllers = dvPmsDatabase.getDvcByKey(keyId);

            Iterator<Integer> dvcItrator = controllers.iterator();


            DVDataToController dataToController = new DVDataToController(
                     dvSettings, communicationTokenManager, keyId);
            boolean SentToallDevices = true;
            while (dvcItrator.hasNext())
            {
               boolean status = false;
               int controller_id = dvcItrator.next();
               int dvsId = dvPmsDatabase.getDvsDeviceId(controller_id);
               String deviceIp = dvPmsDatabase.getIp(controller_id);
               String jsonRequest = "";
               if (dvPmsDatabase.isDeviceCheckedIn(controller_id,
                        data.get(DVPmsData.guestType).toString()))
               {

                  jsonRequest = getRequestJson(DVDeviceTypes.dvc.toString(),
                           dvsId + "", DVPmsiStatus.guestInfoUpdate.toString());
               }
               else
               {
                  dvLogger.info("Device is Not Checked in will send Checkin ");
                  jsonRequest = getRequestJson(DVDeviceTypes.dvc.toString(),
                           dvsId + "", DVPmsiStatus.checkin.toString());
               }

               status = dataToController.SendData(deviceIp, jsonRequest, dvsId);
               if (status)
               {
                  dvLogger.info(
                           " guestinfo Update  Success  for  IP  " + deviceIp);

                  dvPmsDatabase.updatePmsiGuestDeviceStatus(controller_id,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.checkin.toString()),
                           data.get(DVPmsData.guestType).toString().toString(),
                           keyId + "");
                  Iterator<Integer> inRoomDeviceIt = InRoomDevices.iterator();
                  while (inRoomDeviceIt.hasNext())
                  {
                     int inRoomDevice = inRoomDeviceIt.next();
                     int inRoomdvsId =
                              dvPmsDatabase.getDvsDeviceId(inRoomDevice);

                     if (dvPmsDatabase.isDeviceCheckedIn(inRoomDevice,
                              data.get(DVPmsData.guestType).toString()))
                     {
                        jsonRequest = getRequestJson(
                                 DVDeviceTypes.ipad.toString(),
                                 inRoomdvsId + "",
                                 DVPmsiStatus.guestInfoUpdate.toString());
                     }
                     else
                     {
                        jsonRequest =
                                 getRequestJson(DVDeviceTypes.ipad.toString(),
                                          inRoomdvsId + "",
                                          DVPmsiStatus.checkin.toString());
                     }


                     boolean InRoomstatus = dataToController.SendData(deviceIp,
                              jsonRequest, inRoomdvsId);
                     if (InRoomstatus)
                     {
                        dvPmsDatabase.updatePmsiGuestDeviceStatus(inRoomDevice,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.checkin.toString()),
                                 data.get(DVPmsData.guestType).toString()
                                          .toString(),
                                 keyId + "");

                        inRoomDeviceIt.remove();
                     }
                  }

               }
               else
               {
                  dvLogger.info(" Checkin  Failed  for  IP  " + deviceIp);
                  SentToallDevices = false;
                  if (dvPmsDatabase.isDeviceCheckedIn(controller_id,
                           data.get(DVPmsData.guestType).toString()))
                  {
                     dvPmsDatabase.updatePmsiGuestDeviceStatus(controller_id,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                .toString()),
                              data.get(DVPmsData.guestType).toString()
                                       .toString(),
                              keyId + "");
                  }
                  else
                  {
                     dvPmsDatabase.updatePmsiGuestDeviceStatus(controller_id,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKIN.toString()),
                              data.get(DVPmsData.guestType).toString()
                                       .toString(),
                              keyId + "");
                  }
               }
            }
            Iterator<Integer> xplayerUiIt = XplayerUi.iterator();
            while (xplayerUiIt.hasNext())
            {
               int xplayerUi = xplayerUiIt.next();
               String deviceIp = dvPmsDatabase.getIp(xplayerUi);
               int inRoomdvsId = dvPmsDatabase.getDvsDeviceId(xplayerUi);
               String jsonRequest = "";
               if (dvPmsDatabase.isDeviceCheckedIn(xplayerUi,
                        data.get(DVPmsData.guestType).toString()))
               {
                  jsonRequest = getRequestJson(DVDeviceTypes.tvui.toString(),
                           inRoomdvsId + "",
                           DVPmsiStatus.guestInfoUpdate.toString());
               }
               else
               {
                  jsonRequest = getRequestJson(DVDeviceTypes.tvui.toString(),
                           inRoomdvsId + "", DVPmsiStatus.checkin.toString());
               }

               boolean status = dataToController.SendData(deviceIp, jsonRequest,
                        inRoomdvsId);
               if (status)
               {
                  dvLogger.info(" Guest Info update  Success  for Xplayer  IP  "
                           + deviceIp);
                  dvPmsDatabase.updatePmsiGuestDeviceStatus(xplayerUi,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.checkin.toString()),
                           data.get(DVPmsData.guestType).toString().toString(),
                           keyId + "");
                  xplayerUiIt.remove();
               }
               else
               {
                  dvLogger.info(" Guest Info update  Failed  for Xplayer IP  "
                           + deviceIp);
                  SentToallDevices = false;
                  if (dvPmsDatabase.isDeviceCheckedIn(xplayerUi,
                           data.get(DVPmsData.guestType).toString()))
                  {
                     dvPmsDatabase.updatePmsiGuestDeviceStatus(xplayerUi,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                .toString()),
                              data.get(DVPmsData.guestType).toString()
                                       .toString(),
                              keyId + "");
                  }
                  else
                  {
                     dvPmsDatabase.updatePmsiGuestDeviceStatus(xplayerUi,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKIN.toString()),
                              data.get(DVPmsData.guestType).toString()
                                       .toString(),
                              keyId + "");
                  }

               }

            }

            try
            {
               Iterator<Integer> inRoomDeviceIt = InRoomDevices.iterator();
               while (inRoomDeviceIt.hasNext())
               {
                  int inRoomDevice = inRoomDeviceIt.next();
                  String deviceIp = dvPmsDatabase.getIp(inRoomDevice);;
                  String jsonRequest = "";
                  int inRoomdvsId = dvPmsDatabase.getDvsDeviceId(inRoomDevice);

                  if (dvPmsDatabase.isDeviceCheckedIn(inRoomDevice,
                           data.get(DVPmsData.guestType).toString()))
                  {
                     jsonRequest = getRequestJson(DVDeviceTypes.ipad.toString(),
                              inRoomdvsId + "",
                              DVPmsiStatus.guestInfoUpdate.toString());
                  }
                  else
                  {
                     jsonRequest = getRequestJson(DVDeviceTypes.ipad.toString(),
                              inRoomdvsId + "",
                              DVPmsiStatus.checkin.toString());
                  }


                  boolean InRoomstatus = dataToController.SendData(deviceIp,
                           jsonRequest, inRoomdvsId);
                  if (InRoomstatus)
                  {
                     dvPmsDatabase.updatePmsiGuestDeviceStatus(inRoomDevice,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkin.toString()),
                              data.get(DVPmsData.guestType).toString()
                                       .toString(),
                              keyId + "");

                     inRoomDeviceIt.remove();
                  }
               }
            }
            catch (Exception e)
            {
               dvLogger.error(
                        "Error in sending guest info update to non DVC ipad's ",
                        e);
            }

            try
            {
               Iterator<Integer> inRoomDeviceIt = InRoomDevices.iterator();
               while (inRoomDeviceIt.hasNext())
               {
                  int inRoomDevice = inRoomDeviceIt.next();
                  SentToallDevices = false;
                  if (dvPmsDatabase.isDeviceCheckedIn(inRoomDevice,
                           data.get(DVPmsData.guestType).toString()))
                  {
                     dvPmsDatabase.updatePmsiGuestDeviceStatus(inRoomDevice,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                .toString()),
                              data.get(DVPmsData.guestType).toString()
                                       .toString(),
                              keyId + "");
                  }
                  else
                  {

                     dvPmsDatabase.updatePmsiGuestDeviceStatus(inRoomDevice,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKIN.toString()),
                              data.get(DVPmsData.guestType).toString()
                                       .toString(),
                              keyId + "");

                  }
                  inRoomDeviceIt.remove();
               }
            }
            catch (Exception e)
            {
               dvLogger.error("Error ", e);
            }

            try
            {
               Iterator<Integer> inRoomDeviceIt =
                        NonDvcInRoomDevices.iterator();
               while (inRoomDeviceIt.hasNext())
               {
                  int inRoomDevice = inRoomDeviceIt.next();
                  SentToallDevices = false;
                  if (dvPmsDatabase.isDeviceCheckedIn(inRoomDevice,
                           data.get(DVPmsData.guestType).toString()))
                  {
                     dvPmsDatabase.updatePmsiGuestDeviceStatus(inRoomDevice,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                .toString()),
                              data.get(DVPmsData.guestType).toString()
                                       .toString(),
                              keyId + "");
                  }
                  else
                  {

                     dvPmsDatabase.updatePmsiGuestDeviceStatus(inRoomDevice,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKIN.toString()),
                              data.get(DVPmsData.guestType).toString()
                                       .toString(),
                              keyId + "");

                  }
                  inRoomDeviceIt.remove();
               }
            }
            catch (Exception e)
            {
               dvLogger.error("Error ", e);
            }
            if (SentToallDevices)
            {
               dvPmsDatabase.updateKeyStatusDigivaletStatus(
                        data.get(DVPmsData.guestType).toString(), keyId,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.checkin.toString()));
            }
            else
            {
               dvPmsDatabase.updateKeyStatusDigivaletStatus(
                        data.get(DVPmsData.guestType).toString(), keyId,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_CHECKIN.toString()));
            }
         }
         else
         {
            dvLogger.info(
                     "Will not do anything since room state now changed to "
                              + pmsi_status);
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending guest info update", e);
      }
   }

   private String getRequestJson(String deviceType, String device_id,
            String type)
   {

      if (type.equalsIgnoreCase("checkin"))
      {
         if (data.get(DVPmsData.safeFlag).toString().equalsIgnoreCase("true"))
         {
            type = "safeCheckin";
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

      jsonDetails.add("guestDetails", jsonArray);

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
}
