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

public class DVSendPendingCheckout
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

   public DVSendPendingCheckout(DVSettings dvSettings, int keyId,
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
      this.communicationTokenManager=communicationTokenManager;
      this.NonDvcInRoomDevices=NonDvcInRoomDevices;

   }

   public void sendCheckoutToDevices()
   {
      try
      {
         Thread.currentThread().setName("PENDING-CHECKOUT KEY:"+keyId);
         boolean sendCheckout = false;
         boolean discardCheckout = false;
         String pmsi_status = "";
         while (sendCheckout != true)
         {
            try
            {
               dvLogger.info("Checking if it is safe to send checkout ");
               String status = "";
               status = dvPmsDatabase.getDigivaletKeyStatusByGuestType(keyId,
                        data.get(DVPmsData.guestType).toString());
               pmsi_status = dvPmsDatabase.getPmsiKeyStatusByGuestType(keyId,
                        data.get(DVPmsData.guestType).toString());
               
               if (!pmsi_status
                        .equalsIgnoreCase(dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.checkout.toString()) + ""))
               {
                  discardCheckout = true;
                  break;
               }

               if (!status.equalsIgnoreCase(dvPmsDatabase.getMasterStatusId(
                        DVPmsiStatus.SENDING_CHECKIN.toString()) + "")
                        && !status.equalsIgnoreCase(dvPmsDatabase
                                 .getMasterStatusId(DVPmsiStatus.SENDING_CHECKOUT
                                          .toString())
                                 + "")
                        && !status.equalsIgnoreCase(
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.SENDING_GUEST_INFO_UPDATE
                                                   .toString())
                                          + ""))
               {
                  sendCheckout = true;
                  dvLogger.info("Got the Green flag to send checkout ");
                  break;
               }else
               {
                  dvLogger.info(" Red flag for Checkout for this key  ");
               }
               if (!sendCheckout)
               {
                  Thread.sleep(reCheckDelay);
               }
            }
            catch (Exception e)
            {
               dvLogger.error("Error in checkout sending state ", e);
               break;
            }
         }
         if (!discardCheckout)
         {
            dvLogger.info("Guest Type " + data.get(DVPmsData.guestType).toString()
                     + " Secondary Checked In Status "
                     + dvPmsDatabase.checkKeyDigivaletCheckedInStatus(keyId,
                              DVPmsGuestTypes.secondary.toString())
                     + " Secondary Pending Checked In Status  "
                     + dvPmsDatabase.checkKeyDigivaletPendingCheckin(keyId,
                              DVPmsGuestTypes.secondary.toString()));

            {

               dvLogger.info("Normal Pending Checkout ");
/*               String Updatequery =
                        "UPDATE `pmsi_key_status` SET `digivalet_status`="
                                 + dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.SENDING_CHECKOUT
                                                   .toString())
                                 + " where `key_id`=" + keyId
                                 + " and `guest_type`='"
                                 + data.get(DVPmsData.guestType) + "' ";;
               dvLogger.info("Update: " + Updatequery);
               stmt.executeUpdate(Updatequery);*/
               dvPmsDatabase.updateKeyStatusDigivaletStatus(
                        data.get(DVPmsData.guestType).toString(), keyId,
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

                  if (dvPmsDatabase
                           .getMasterStatusId(DVPmsiStatus.PENDING_CHECKOUT
                                    .toString()) == dvPmsDatabase
                                             .getPmsiDeviceStatus(controllerId,
                                                      data.get(DVPmsData.guestType)
                                                               .toString()))
                  {
                     status = dataToController.SendData(deviceIp, jsonRequest,dvsId);

                     if (status)
                     {
                        dvLogger.info(
                                 " Checkout  Success  for  IP  " + deviceIp);
                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 controllerId, pmsiGuestID,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.checkout.toString()),
                                 data.get(DVPmsData.guestType).toString());
                        Iterator<Integer> inRoomDeviceIt =
                                 InRoomDevices.iterator();
                        while (inRoomDeviceIt.hasNext())
                        {
                           int inRoomDevice = inRoomDeviceIt.next();
                           if (dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKOUT
                                             .toString()) == dvPmsDatabase
                                                      .getPmsiDeviceStatus(
                                                               inRoomDevice,
                                                               data.get(DVPmsData.guestType)
                                                                        .toString()))
                           {
                              int inRoomdvsId = dvPmsDatabase
                                       .getDvsDeviceId(inRoomDevice);
                              jsonRequest = getRequestJson(
                                       DVDeviceTypes.ipad.toString(),
                                       inRoomdvsId + "",
                                       DVPmsiStatus.checkout.toString());
                              boolean InRoomstatus = false;

                              if (data.get(DVPmsData.guestType).toString()
                                       .equalsIgnoreCase(
                                                DVPmsGuestTypes.secondary
                                                         .toString()))
                              {
                                 if (dvPmsDatabase.isPrimaryguestCheckedInCheckedOut(
                                          inRoomDevice))
                                 {
                                    InRoomstatus = dataToController
                                             .SendData(deviceIp, jsonRequest,inRoomdvsId);
                                 }
                                 else
                                 {
                                    dvLogger.info(
                                             "Will not checkout deivce since Primary guest is not cheked out ");
                                 }
                              }
                              else
                              {
                                 InRoomstatus = dataToController
                                          .SendData(deviceIp, jsonRequest,inRoomdvsId);
                              }

                              if (InRoomstatus)
                              {
                                 dvPmsDatabase.insertPmsiGuestDeviceStatus(
                                          keyId, inRoomDevice, pmsiGuestID,
                                          dvPmsDatabase.getMasterStatusId(
                                                   DVPmsiStatus.checkout
                                                            .toString()),
                                          data.get(DVPmsData.guestType)
                                                   .toString());
                                 inRoomDeviceIt.remove();
                              }
                           }
                           else
                           {
                              dvLogger.info("In Room device " + inRoomDevice
                                       + " is already checked in ");
                              inRoomDeviceIt.remove();
                           }


                        }
                     }
                     else
                     {
                        dvLogger.info(
                                 " Checkout  Failed  for  IP  " + deviceIp);
                        SentToallDevices = false;
                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 controllerId, pmsiGuestID,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.PENDING_CHECKOUT
                                                   .toString()),
                                 data.get(DVPmsData.guestType).toString());
                     }
                  }
                  else
                  {
                     dvLogger.info(
                              "Dvc Already Checked out Will Send to in room devices only");
                     Iterator<Integer> inRoomDeviceIt =
                              InRoomDevices.iterator();
                     while (inRoomDeviceIt.hasNext())
                     {
                        int inRoomDevice = inRoomDeviceIt.next();
                        if (dvPmsDatabase
                                 .getMasterStatusId(DVPmsiStatus.PENDING_CHECKOUT
                                          .toString()) == dvPmsDatabase
                                                   .getPmsiDeviceStatus(
                                                            inRoomDevice,
                                                            data.get(DVPmsData.guestType)
                                                                     .toString()))
                        {
                           int inRoomdvsId =
                                    dvPmsDatabase.getDvsDeviceId(inRoomDevice);
                           jsonRequest = getRequestJson(
                                    DVDeviceTypes.ipad.toString(),
                                    inRoomdvsId + "",
                                    DVPmsiStatus.checkout.toString());
                           boolean InRoomstatus = false;

                           if (data.get(DVPmsData.guestType).toString()
                                    .equalsIgnoreCase(DVPmsGuestTypes.secondary
                                             .toString()))
                           {
                              if (dvPmsDatabase
                                       .isPrimaryguestCheckedInCheckedOut(inRoomDevice))
                              {
                                 InRoomstatus = dataToController
                                          .SendData(deviceIp, jsonRequest,inRoomdvsId);
                              }
                              else
                              {
                                 dvLogger.info(
                                          "Will not checkout deivce since Primary guest is not cheked out ");
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
                                                DVPmsiStatus.checkout.toString()),
                                       data.get(DVPmsData.guestType).toString());
                              inRoomDeviceIt.remove();
                           }
                        }
                        else
                        {
                           dvLogger.info("In Room device " + inRoomDevice
                                    + " is already checked in ");
                           inRoomDeviceIt.remove();
                        }


                     }
                  }
               }

               Iterator<Integer> xplayerUiIt = XplayerUi.iterator();
               while (xplayerUiIt.hasNext())
               {
                  int xplayerUi = xplayerUiIt.next();

                  if (dvPmsDatabase
                           .getMasterStatusId(DVPmsiStatus.PENDING_CHECKOUT
                                    .toString()) == dvPmsDatabase
                                             .getPmsiDeviceStatus(xplayerUi,
                                                      data.get(DVPmsData.guestType)
                                                               .toString()))
                  {
                     String deviceIp = dvPmsDatabase.getIp(xplayerUi);
                     int inRoomdvsId = dvPmsDatabase.getDvsDeviceId(xplayerUi);
                     String jsonRequest = getRequestJson(
                              DVDeviceTypes.tvui.toString(),
                              inRoomdvsId + "", DVPmsiStatus.checkout.toString());
                     boolean status = false;
                     if (data.get(DVPmsData.guestType).toString()
                              .equalsIgnoreCase(
                                       DVPmsGuestTypes.secondary.toString()))
                     {
                        if (dvPmsDatabase.isPrimaryguestCheckedInCheckedOut(xplayerUi))
                        {
                           status = dataToController.SendData(deviceIp,
                                    jsonRequest,inRoomdvsId);
                        }
                        else
                        {
                           dvLogger.info(
                                    "Will not checkout deivce since Primary guest is not cheked out ");
                        }
                     }
                     else
                     {
                        status = dataToController.SendData(deviceIp,
                                 jsonRequest,inRoomdvsId);
                     }
                     if (status)
                     {
                        dvLogger.info(" Checkout  Success  for Xplayer  IP  "
                                 + deviceIp);
                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 xplayerUi, pmsiGuestID,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.checkout.toString()),
                                 data.get(DVPmsData.guestType).toString());
                     }
                     else
                     {
                        dvLogger.info(" Checkout  Failed  for Xplayer IP  "
                                 + deviceIp);
                        SentToallDevices = false;
                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 xplayerUi, pmsiGuestID,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.PENDING_CHECKOUT
                                                   .toString()),
                                 data.get(DVPmsData.guestType).toString());
                     }
                  }
                  else
                  {
                     dvLogger.info(
                              "Xplayer " + xplayerUi + " already checked IN ");
                  }
               }
               
               try
               {
                  Iterator<Integer> nonDvcInRoomDevicesitr = NonDvcInRoomDevices.iterator();
                  while (nonDvcInRoomDevicesitr.hasNext())
                  {
                     int inRoomDevice = nonDvcInRoomDevicesitr.next();
                     String deviceIp = dvPmsDatabase.getIp(inRoomDevice);
                     if (dvPmsDatabase.getMasterStatusId(
                              DVPmsiStatus.PENDING_CHECKOUT
                                       .toString()) == dvPmsDatabase
                                                .getPmsiDeviceStatus(
                                                         inRoomDevice,
                                                         data.get(DVPmsData.guestType)
                                                                  .toString()))
                     {
                        String jsonRequest="";
                        int inRoomdvsId = dvPmsDatabase
                                 .getDvsDeviceId(inRoomDevice);
                        jsonRequest = getRequestJson(
                                 DVDeviceTypes.ipad.toString(),
                                 inRoomdvsId + "",
                                 DVPmsiStatus.checkout.toString());
                        boolean InRoomstatus = false;

                        if (data.get(DVPmsData.guestType).toString()
                                 .equalsIgnoreCase(
                                          DVPmsGuestTypes.secondary
                                                   .toString()))
                        {
                           if (dvPmsDatabase.isPrimaryguestCheckedInCheckedOut(
                                    inRoomDevice))
                           {
                              InRoomstatus = dataToController
                                       .SendData(deviceIp, jsonRequest,inRoomdvsId);
                           }
                           else
                           {
                              dvLogger.info(
                                       "Will not checkout deivce since Primary guest is not cheked out ");
                           }
                        }
                        else
                        {
                           InRoomstatus = dataToController
                                    .SendData(deviceIp, jsonRequest,inRoomdvsId);
                        }

                        if (InRoomstatus)
                        {
                           dvPmsDatabase.insertPmsiGuestDeviceStatus(
                                    keyId, inRoomDevice, pmsiGuestID,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.checkout
                                                      .toString()),
                                    data.get(DVPmsData.guestType)
                                             .toString());
                           nonDvcInRoomDevicesitr.remove();
                        }
                     }
                     else
                     {
                        dvLogger.info("In Room device " + inRoomDevice
                                 + " is already checked in ");
                        nonDvcInRoomDevicesitr.remove();
                     }


                  }
                       
               }
               catch (Exception e)
               {
                  dvLogger.error("Error in sending to non dvc ipad", e);
               }
       
               
                  for (int inRoomDevice : InRoomDevices)
                  {
                     SentToallDevices = false;
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, inRoomDevice,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKOUT.toString()),
                              data.get(DVPmsData.guestType).toString());
                  }
               
                  for (int inRoomDevice : NonDvcInRoomDevices)
                  {
                     SentToallDevices = false;
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, inRoomDevice,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKOUT.toString()),
                              data.get(DVPmsData.guestType).toString());
                  }
               
               if (SentToallDevices)
               {
                  /*String query =
                           "UPDATE `pmsi_key_status` SET `digivalet_status`="
                                    + dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.checkout.toString())
                                    + " where `key_id`=" + keyId
                                    + " and `guest_type`='"
                                    + data.get(DVPmsData.guestType).toString()
                                    + "' ";
                  dvLogger.info(query);
                  stmt.executeUpdate(query);*/
                  
                  dvPmsDatabase.updateKeyStatusDigivaletStatus(
                           data.get(DVPmsData.guestType).toString(), keyId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.checkout.toString()));
                  
               }
               else
               {
                  /*String query =
                           "UPDATE `pmsi_key_status` SET `digivalet_status`="
                                    + dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.PENDING_CHECKOUT
                                                      .toString())
                                    + " where `key_id`=" + keyId
                                    + " and `guest_type`='"
                                    + data.get(DVPmsData.guestType).toString()
                                    + "' ";;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);*/
                  
                  dvPmsDatabase.updateKeyStatusDigivaletStatus(
                           data.get(DVPmsData.guestType).toString(), keyId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKOUT.toString()));
                  
               }
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in checkout to devices state ", e);
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
