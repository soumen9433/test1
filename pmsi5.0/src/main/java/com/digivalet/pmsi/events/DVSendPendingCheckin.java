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
import com.digivalet.pmsi.datatypes.DVPmsGuestTypes;
import com.digivalet.pmsi.settings.DVSettings;
import com.digivalet.pmsi.util.DVDataToController;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DVSendPendingCheckin
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
   private final int reCheckDelay=5000;
   private DVKeyCommunicationTokenManager communicationTokenManager;
   
   public DVSendPendingCheckin(DVSettings dvSettings, int keyId, String hotelId,
            int pmsiGuestID, DVPmsDatabase dvPmsDatabase,
            Map<DVPmsData, Object> data, ArrayList<Integer> InRoomDevices,
            ArrayList<Integer> XplayerUi,
            DVKeyCommunicationTokenManager communicationTokenManager,ArrayList<Integer> NonDvcInRoomDevices)
   {
      this.dvSettings = dvSettings;
      this.keyId = keyId;
      this.hotelId = hotelId;
      this.pmsiGuestID = pmsiGuestID;
      this.dvPmsDatabase = dvPmsDatabase;
      this.data = data;
      this.InRoomDevices = InRoomDevices;
      this.XplayerUi = XplayerUi;
      this.communicationTokenManager=communicationTokenManager;
      this.NonDvcInRoomDevices=NonDvcInRoomDevices;
   }

   public void sendCheckinToDevices()
   {
      try
      {
         Thread.currentThread().setName("PENDING-CHECKIN "+keyId);
         dvLogger.info("Now sending checkin to Devices ");
         boolean sendCheckin = false;
         boolean discardCheckin = false;
         String pmsi_status = "";
         while (sendCheckin != true)
         {
            try
            {
               dvLogger.info("Checking if it is safe to send checking ");
               String status = dvPmsDatabase.getDigivaletKeyStatusByGuestType(
                        keyId, data.get(DVPmsData.guestType).toString());
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
                  dvLogger.info("Got the Green flag to send checkin ");
                  break;
               }else
               {
                  dvLogger.info(" Red flag for checkin for this key  ");
               }
               if (!sendCheckin)
               {
                  Thread.sleep(reCheckDelay);
               }
            }
            catch (Exception e)
            {
               dvLogger.error("Error in checkin sending state ", e);
               break;
            }
         }
         if (!discardCheckin)
         {
            
            /*String Updatequery =
                     "UPDATE `pmsi_key_status` SET `digivalet_status`="
                              + dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.SENDING_CHECKIN.toString())
                              + " where `key_id`=" + keyId
                              + " and `guest_type`='"
                              + data.get(DVPmsData.guestType).toString() + "'";
            dvLogger.info("Update: " + Updatequery);
            stmt.executeUpdate(Updatequery);*/
            dvPmsDatabase.updateKeyStatusDigivaletStatus(
                     data.get(DVPmsData.guestType).toString(), keyId,
                     dvPmsDatabase.getMasterStatusId(
                              DVPmsiStatus.SENDING_CHECKIN.toString()));
            
            
            
            ArrayList<Integer> controllers =
                     dvPmsDatabase.getDvcByKey(keyId);

            Iterator<Integer> dvcItrator = controllers.iterator();
            
            DVDataToController dataToController =
                     new DVDataToController(dvSettings,communicationTokenManager,keyId);
            boolean SentToallDevices = true;
            while (dvcItrator.hasNext())
            {
               boolean status = false;
               int controller_id = dvcItrator.next();
               int dvsId = dvPmsDatabase.getDvsDeviceId(controller_id);
               String deviceIp = dvPmsDatabase.getIp(controller_id);
               
               if (dvPmsDatabase.getMasterStatusId(
                        DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                                 .getPmsiDeviceStatus(controller_id,
                                          data.get(DVPmsData.guestType)
                                                   .toString())
                        || dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                          .toString()) == dvPmsDatabase
                                                   .getPmsiDeviceStatus(
                                                            controller_id,
                                                            data.get(DVPmsData.guestType)
                                                                     .toString()))
               {
                  
                  String jsonRequest;
                  if(dvPmsDatabase.getMasterStatusId(
                           DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                           .getPmsiDeviceStatus(controller_id,
                                    data.get(DVPmsData.guestType)
                                             .toString()))
                  {
                     jsonRequest = getRequestJson(DVDeviceTypes.dvc.toString(), dvsId + "",
                              DVPmsiStatus.checkin.toString());
                  }else
                  {
                     jsonRequest = getRequestJson(DVDeviceTypes.dvc.toString(), dvsId + "",
                              DVPmsiStatus.guestInfoUpdate.toString());
                  }
                  
                  

                  if (data.get(DVPmsData.guestType).toString()
                           .equalsIgnoreCase(DVPmsGuestTypes.secondary.toString()))
                  {
                     dvLogger.info("first checking if guest type is secondary");
                     if (dvPmsDatabase.isPrimaryguestCheckedIn(controller_id))
                     {
                        dvLogger.info(
                                 "Primary guest is checked in will send checkin ");
                        status = dataToController.SendData(deviceIp, jsonRequest,dvsId);
                     }else
                     {
                        dvLogger.info(
                                 "Primary guest is not checked In will not send checkin ");
                     }
                  }
                  else
                  {
                     dvLogger.info("Guest is primary ");
                     status = dataToController.SendData(deviceIp, jsonRequest,dvsId);
                  }
                  if (status)
                  {
                     dvLogger.info(" Checkin  Success  for  IP  " + deviceIp);
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                              controller_id, pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkin.toString()),
                              data.get(DVPmsData.guestType).toString());
                     Iterator<Integer> inRoomDeviceIt = InRoomDevices.iterator();
                     while (inRoomDeviceIt.hasNext())
                     {
                        int inRoomDevice = inRoomDeviceIt.next();
                        if (dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                                          .getPmsiDeviceStatus(inRoomDevice,
                                                   data.get(DVPmsData.guestType)
                                                            .toString())
                                 || dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                   .toString()) == dvPmsDatabase
                                                            .getPmsiDeviceStatus(
                                                                     inRoomDevice,
                                                                     data.get(DVPmsData.guestType)
                                                                              .toString()))
                        {
                           int inRoomdvsId =
                                    dvPmsDatabase.getDvsDeviceId(inRoomDevice);
                           
                           if(dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                                    .getPmsiDeviceStatus(inRoomDevice,
                                             data.get(DVPmsData.guestType)
                                                      .toString()))
                           {
                              jsonRequest = getRequestJson(DVDeviceTypes.ipad.toString(),
                                       inRoomdvsId + "", DVPmsiStatus.checkin.toString());   
                           }else
                           {
                              jsonRequest = getRequestJson(DVDeviceTypes.ipad.toString(),
                                       inRoomdvsId + "", DVPmsiStatus.guestInfoUpdate.toString());
                           }
                           
                           boolean InRoomstatus = false;

                           if (data.get(DVPmsData.guestType).toString()
                                    .equalsIgnoreCase(DVPmsGuestTypes.secondary.toString()))
                           {
                              if (dvPmsDatabase.isPrimaryguestCheckedIn(inRoomDevice))
                              {
                                 InRoomstatus = dataToController.SendData(deviceIp,
                                          jsonRequest,inRoomdvsId);
                              }
                           }
                           else
                           {
                              InRoomstatus = dataToController.SendData(deviceIp,
                                       jsonRequest,inRoomdvsId);
                           }

                           if (InRoomstatus)
                           {
                              dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                       inRoomDevice, pmsiGuestID,
                                       dvPmsDatabase.getMasterStatusId(
                                                DVPmsiStatus.checkin.toString()),
                                       data.get(DVPmsData.guestType).toString());
                              inRoomDeviceIt.remove();
                           }
                        }else
                        {
                           dvLogger.info("Device is already checked in ");
                           inRoomDeviceIt.remove();
                        }
                     }

                  }
                  else
                  {
                     dvLogger.info(" Checkin  Failed  for  IP  " + deviceIp);
                     SentToallDevices = false;
                     if(dvPmsDatabase.getMasterStatusId(
                              DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                              .getPmsiDeviceStatus(controller_id,
                                       data.get(DVPmsData.guestType)
                                                .toString()))
                     {
                        /**
                         * If checkin is pending update to pending checkin
                         */
                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 controller_id, pmsiGuestID,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.PENDING_CHECKIN.toString()),
                                 data.get(DVPmsData.guestType).toString());   
                     }
                     else
                     {
                        /**
                         * If guest info update is pending update to pending guest info update
                         */
                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 controller_id, pmsiGuestID,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.PENDING_GUEST_INFO_UPDATE.toString()),
                                 data.get(DVPmsData.guestType).toString());
                     }
                  }
               }else
               {
                  dvLogger.info("DVC is already checked in will only send to in Room devices ");
                  Iterator<Integer> inRoomDeviceIt = InRoomDevices.iterator();
                  while (inRoomDeviceIt.hasNext())
                  {
                     int inRoomDevice = inRoomDeviceIt.next();
                     
                     if (dvPmsDatabase.getMasterStatusId(
                              DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                                       .getPmsiDeviceStatus(inRoomDevice,
                                                data.get(DVPmsData.guestType)
                                                         .toString())
                              || dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                .toString()) == dvPmsDatabase
                                                         .getPmsiDeviceStatus(
                                                                  inRoomDevice,
                                                                  data.get(DVPmsData.guestType)
                                                                           .toString()))
                     {
                     
                        int inRoomdvsId =
                                 dvPmsDatabase.getDvsDeviceId(inRoomDevice);
                        String jsonRequest;
                        if(dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                                 .getPmsiDeviceStatus(inRoomDevice,
                                          data.get(DVPmsData.guestType)
                                                   .toString()))
                        {
                           jsonRequest  = getRequestJson(DVDeviceTypes.ipad.toString(),
                                    inRoomdvsId + "", DVPmsiStatus.checkin.toString());   
                        }else
                        {
                           jsonRequest = getRequestJson(DVDeviceTypes.ipad.toString(),
                                    inRoomdvsId + "", DVPmsiStatus.guestInfoUpdate.toString());
                        }
                        
                        
                        boolean InRoomstatus = false;

                        if (data.get(DVPmsData.guestType).toString()
                                 .equalsIgnoreCase(DVPmsGuestTypes.secondary.toString()))
                        {
                           if (dvPmsDatabase.isPrimaryguestCheckedIn(inRoomDevice))
                           {
                              InRoomstatus = dataToController.SendData(deviceIp,
                                       jsonRequest,inRoomdvsId);
                           }
                        }
                        else
                        {
                           InRoomstatus = dataToController.SendData(deviceIp,
                                    jsonRequest,inRoomdvsId);
                        }

                        if (InRoomstatus)
                        {
                           dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                    inRoomDevice, pmsiGuestID,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.checkin.toString()),
                                    data.get(DVPmsData.guestType).toString());
                           inRoomDeviceIt.remove();
                        }
                     }else
                     {
                        dvLogger.info("Device is already checked in ");
                        inRoomDeviceIt.remove();
                     }
                  }
               }
               
            }
            Iterator<Integer> xplayerUiIt = XplayerUi.iterator();
            while (xplayerUiIt.hasNext())
            {
               int xplayerUi = xplayerUiIt.next();
               if (dvPmsDatabase.getMasterStatusId(
                        DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                        .getPmsiDeviceStatus(xplayerUi,
                                 data.get(DVPmsData.guestType)
                                          .toString())
               || dvPmsDatabase.getMasterStatusId(
                        DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                 .toString()) == dvPmsDatabase
                                          .getPmsiDeviceStatus(
                                                   xplayerUi,
                                                   data.get(DVPmsData.guestType)
                                                            .toString()))
               {
                  String deviceIp = dvPmsDatabase.getIp(xplayerUi);
                  int inRoomdvsId = dvPmsDatabase.getDvsDeviceId(xplayerUi);
                  String jsonRequest="";
                  if(dvPmsDatabase.getMasterStatusId(
                           DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                           .getPmsiDeviceStatus(xplayerUi,
                                    data.get(DVPmsData.guestType)
                                             .toString()))
                  {
                     jsonRequest = getRequestJson(
                              DVDeviceTypes.tvui.toString(), inRoomdvsId + "",
                              DVPmsiStatus.checkin.toString());
                  }else
                  {
                     jsonRequest = getRequestJson(
                              DVDeviceTypes.tvui.toString(), inRoomdvsId + "",
                              DVPmsiStatus.guestInfoUpdate.toString());   
                  }
                  
                  
                  boolean status = false;
                  
                  if (data.get(DVPmsData.guestType).toString()
                           .equalsIgnoreCase(DVPmsGuestTypes.secondary.toString()))
                  {
                     if (dvPmsDatabase.isPrimaryguestCheckedIn(xplayerUi))
                     {
                        status = dataToController.SendData(deviceIp, jsonRequest,inRoomdvsId);
                     }else
                     {
                        dvLogger.info("wont send checking since primary guest is not checked in ");
                     }   
                  }else
                  {
                     status = dataToController.SendData(deviceIp, jsonRequest,inRoomdvsId);
                  }
                  
                  if (status)
                  {
                     dvLogger.info(" Checkin  Success  for Xplayer  IP  "
                              + deviceIp);
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkin.toString()),
                              data.get(DVPmsData.guestType).toString());
                     xplayerUiIt.remove();
                  }
                  else
                  {
                     dvLogger.info(
                              " Checkin  Failed  for Xplayer IP  " + deviceIp);
                     SentToallDevices = false;
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKIN.toString()),
                              data.get(DVPmsData.guestType).toString());
                  }
               }else 
               {
                  dvLogger.info("Xplayer UI is already Checked In ");
               }


            }
            
            Iterator<Integer> NonDvcInRoomDevicesItr = NonDvcInRoomDevices.iterator();
            while (NonDvcInRoomDevicesItr.hasNext())
            {
               int inRoomDevice = NonDvcInRoomDevicesItr.next();
               String deviceIp = dvPmsDatabase.getIp(inRoomDevice);
               if (dvPmsDatabase.getMasterStatusId(
                        DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                                 .getPmsiDeviceStatus(inRoomDevice,
                                          data.get(DVPmsData.guestType)
                                                   .toString())
                        || dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                          .toString()) == dvPmsDatabase
                                                   .getPmsiDeviceStatus(
                                                            inRoomDevice,
                                                            data.get(DVPmsData.guestType)
                                                                     .toString()))
               {
                  int inRoomdvsId =
                           dvPmsDatabase.getDvsDeviceId(inRoomDevice);
                  String jsonRequest;
                  if(dvPmsDatabase.getMasterStatusId(
                           DVPmsiStatus.PENDING_CHECKIN.toString()) == dvPmsDatabase
                           .getPmsiDeviceStatus(inRoomDevice,
                                    data.get(DVPmsData.guestType)
                                             .toString()))
                  {
                     jsonRequest = getRequestJson(DVDeviceTypes.ipad.toString(),
                              inRoomdvsId + "", DVPmsiStatus.checkin.toString());   
                  }else
                  {
                     jsonRequest = getRequestJson(DVDeviceTypes.ipad.toString(),
                              inRoomdvsId + "", DVPmsiStatus.guestInfoUpdate.toString());
                  }
                  
                  boolean InRoomstatus = false;

                  if (data.get(DVPmsData.guestType).toString()
                           .equalsIgnoreCase(DVPmsGuestTypes.secondary.toString()))
                  {
                     if (dvPmsDatabase.isPrimaryguestCheckedIn(inRoomDevice))
                     {
                        InRoomstatus = dataToController.SendData(deviceIp,
                                 jsonRequest,inRoomdvsId);
                     }
                  }
                  else
                  {
                     InRoomstatus = dataToController.SendData(deviceIp,
                              jsonRequest,inRoomdvsId);
                  }

                  if (InRoomstatus)
                  {
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                              inRoomDevice, pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkin.toString()),
                              data.get(DVPmsData.guestType).toString());
                     NonDvcInRoomDevicesItr.remove();
                  }
               }else
               {
                  dvLogger.info("Device is already checked in ");
                  NonDvcInRoomDevicesItr.remove();
               }
            
            }

            for (int inRoomDevice : InRoomDevices)
            {
               SentToallDevices = false;
               dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, inRoomDevice,
                        pmsiGuestID,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_CHECKIN.toString()),
                        data.get(DVPmsData.guestType).toString());
            }
            for (int inRoomDevice : NonDvcInRoomDevices)
            {
               SentToallDevices = false;
               dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, inRoomDevice,
                        pmsiGuestID,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_CHECKIN.toString()),
                        data.get(DVPmsData.guestType).toString());
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
         dvLogger.error("Error in sending Checkin to guest", e);
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
