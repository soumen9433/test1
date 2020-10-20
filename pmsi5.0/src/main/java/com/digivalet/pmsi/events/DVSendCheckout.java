package com.digivalet.pmsi.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

public class DVSendCheckout
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


   public DVSendCheckout(DVSettings dvSettings, int keyId, String hotelId,
            int pmsiGuestID, DVPmsDatabase dvPmsDatabase,
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

   public void sendCheckoutToDevices()
   {
      try
      {
         Thread.currentThread().setName("CHECKOUT KEY:" + keyId);
         boolean sendCheckout = false;
         boolean discardCheckout = false;
         String pmsi_status = "";
         while (sendCheckout != true)
         {
            try
            {
               String status = "";
               status = dvPmsDatabase.getDigivaletKeyStatusByGuestType(keyId,
                        data.get(DVPmsData.guestType).toString());
               pmsi_status = dvPmsDatabase.getPmsiKeyStatusByGuestType(keyId,
                        data.get(DVPmsData.guestType).toString());
               int tempPmsiGuestId=dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,data.get(DVPmsData.guestType).toString());
               if (!pmsi_status
                        .equalsIgnoreCase(dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.checkout.toString()) + "") || pmsiGuestID!=tempPmsiGuestId )
               {
                  dvLogger.info("Need to discard this checkout pmsiGuestID!=tempPmsiGuestId "+(pmsiGuestID!=tempPmsiGuestId)+"  "+pmsi_status);
                  discardCheckout = true;
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
                  sendCheckout = true;
                  dvLogger.info("Got the Green flag to send checkout ");
                  break;
               }
               else
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

            dvPmsDatabase.removeDevicesFromGuestDevice(keyId,
                     data.get(DVPmsData.guestType).toString());

            dvLogger.info("Guest Type "
                     + data.get(DVPmsData.guestType).toString()
                     + " Secondary Cheked In Status "
                     + dvPmsDatabase.checkKeyDigivaletCheckedInStatus(keyId,
                              DVPmsGuestTypes.secondary.toString())
                     + " Secondary Pending Checked In Status  "
                     + dvPmsDatabase.checkKeyDigivaletPendingCheckin(keyId,
                              DVPmsGuestTypes.secondary.toString()));



            if (data.get(DVPmsData.guestType).toString()
                     .equalsIgnoreCase(DVPmsGuestTypes.primary.toString())
                     && dvPmsDatabase.checkKeyPmsiCheckedInStatus(keyId,
                              DVPmsGuestTypes.secondary.toString()))
            {
               
               dvLogger.info(
                        "Received primary checkout when secondary is checked in");
               dvLogger.info(
                        "Checkout Primary and update secondary to primary");

               if (!pmsi_status
                        .equalsIgnoreCase(dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.checkout.toString()) + ""))
               {
                  dvLogger.info(
                           "Discarding this checkout since room is now checked In");
                  discardCheckout = true;

               }
               else
               {
                  dvPmsDatabase.updateKeyStatusDigivaletStatus(
                           data.get(DVPmsData.guestType).toString(), keyId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.SENDING_CHECKOUT.toString()));
                  boolean sendSecondaryInfoUdate = false;
                  boolean isSecondaryCheckedout = false;
                  dvLogger.info(
                           "Checking if it is safe to send info update to secondary guest ");
                  while (sendSecondaryInfoUdate != true)
                  {
                     try
                     {
                        String secondaryStatus = "";

                        secondaryStatus = dvPmsDatabase
                                 .getDigivaletKeyStatusByGuestType(keyId,
                                          DVPmsGuestTypes.secondary.toString());
                        String pmsi_secondary_status = dvPmsDatabase
                                 .getPmsiKeyStatusByGuestType(keyId,
                                          DVPmsGuestTypes.secondary.toString());
                        if (pmsi_secondary_status.equalsIgnoreCase(
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.checkout.toString())
                                          + ""))
                        {
                           isSecondaryCheckedout = true;
                           break;
                        }

                        if (!pmsi_secondary_status.equalsIgnoreCase(
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.SENDING_CHECKIN
                                                   .toString())
                                          + "")
                                 && !pmsi_secondary_status.equalsIgnoreCase(
                                          dvPmsDatabase.getMasterStatusId(
                                                   DVPmsiStatus.SENDING_CHECKOUT
                                                            .toString())
                                                   + "")
                                 && !pmsi_secondary_status.equalsIgnoreCase(
                                          dvPmsDatabase.getMasterStatusId(
                                                   DVPmsiStatus.SENDING_GUEST_INFO_UPDATE
                                                            .toString())
                                                   + ""))
                        {
                           sendSecondaryInfoUdate = true;
                           dvLogger.info(
                                    "Got the Green flag to send secondary guest info update to primary ");
                           break;
                        }
                        if (!sendSecondaryInfoUdate)
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

                  if (!isSecondaryCheckedout)
                  {
                     if (!pmsi_status
                              .equalsIgnoreCase(dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkout.toString()) + ""))
                     {
                        dvLogger.info(
                                 "Primary guest is not in checkout state will ignore this checkout ");

                        dvPmsDatabase.updateKeyStatusDigivaletStatus(
                                 data.get(DVPmsData.guestType).toString(),
                                 keyId, dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.checkout.toString()));
                     }
                     else
                     {
                        dvLogger.info(
                                 "Secondary guest is not checked out will send info update to him and checkout to primary");

                        dvPmsDatabase.removeDevicesFromGuestDevice(keyId,
                                 DVPmsGuestTypes.primary.toString());

                        dvPmsDatabase.updateKeyStatusDigivaletStatus(
                                 DVPmsGuestTypes.secondary.toString(), keyId,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.SENDING_GUEST_INFO_UPDATE
                                                   .toString()));
                        dvPmsDatabase.removeDevicesFromGuestDevice(keyId,
                                 DVPmsGuestTypes.secondary.toString());

                        int secondaryGuestId = dvPmsDatabase.getGuestIdFromKey(
                                 keyId, DVPmsGuestTypes.secondary.toString());
                        dvLogger.info("pmsi_guest_id of Secondary guest "
                                 + secondaryGuestId);

                        // Swapping guest type in pmsi_key_status
                        dvPmsDatabase.updateKeyStatusGuestTypeByGuestId(
                                 DVPmsGuestTypes.primary.toString(),
                                 secondaryGuestId);

                        dvPmsDatabase.updateKeyStatusGuestTypeByGuestId(
                                 DVPmsGuestTypes.secondary.toString(),
                                 pmsiGuestID);

                        // Swapping guest type in pmsi_guest
                        dvPmsDatabase.updatePmsiGuestTypeByGuestId(
                                 DVPmsGuestTypes.primary.toString(),
                                 secondaryGuestId);

                        dvPmsDatabase.updatePmsiGuestTypeByGuestId(
                                 DVPmsGuestTypes.secondary.toString(),
                                 pmsiGuestID);

                        Map<DVPmsData, Object> secondary_guest_data =
                                 dvPmsDatabase.getDataByPmsiGuestId(
                                          secondaryGuestId);
                        dvLogger.info("Secondary Guest Data : "
                                 + secondary_guest_data.toString());

                        ArrayList<Integer> controllers =
                                 dvPmsDatabase.getDvcByKey(keyId);

                        Iterator<Integer> dvcItrator = controllers.iterator();
                        boolean SentToallDevices = true;

                        DVDataToController dataToController =
                                 new DVDataToController(dvSettings,
                                          communicationTokenManager, keyId);
                        while (dvcItrator.hasNext())
                        {
                           boolean status = false;
                           int controllerId = dvcItrator.next();
                           int dvsId =
                                    dvPmsDatabase.getDvsDeviceId(controllerId);
                           String deviceIp = dvPmsDatabase.getIp(controllerId);

                           String jsonRequest = getCheckoutGuestInfoJson(
                                    DVDeviceTypes.dvc.toString(), dvsId + "",
                                    DVPmsiStatus.checkout.toString(),
                                    DVPmsiStatus.guestInfoUpdate.toString(),
                                    secondary_guest_data);
                           status = dataToController.SendData(deviceIp,
                                    jsonRequest, dvsId);

                           if (status)
                           {
                              dvLogger.info(" Checkout  Success  for  IP  "
                                       + deviceIp);
                              dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                       controllerId, secondaryGuestId,
                                       dvPmsDatabase.getMasterStatusId(
                                                DVPmsiStatus.checkin
                                                         .toString()),
                                       DVPmsGuestTypes.primary.toString());

                              dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                       controllerId, pmsiGuestID,
                                       dvPmsDatabase.getMasterStatusId(
                                                DVPmsiStatus.checkout
                                                         .toString()),
                                       DVPmsGuestTypes.secondary.toString());


                              Iterator<Integer> inRoomDeviceIt =
                                       InRoomDevices.iterator();
                              while (inRoomDeviceIt.hasNext())
                              {
                                 int inRoomDevice = inRoomDeviceIt.next();
                                 int inRoomdvsId = dvPmsDatabase
                                          .getDvsDeviceId(inRoomDevice);
                                 jsonRequest = getCheckoutGuestInfoJson(
                                          DVDeviceTypes.ipad.toString(),
                                          inRoomdvsId + "",
                                          DVPmsiStatus.checkout.toString(),
                                          DVPmsiStatus.guestInfoUpdate
                                                   .toString(),
                                          secondary_guest_data);
                                 boolean InRoomstatus = false;

                                 InRoomstatus = dataToController.SendData(
                                          deviceIp, jsonRequest, inRoomdvsId);
                                 if (InRoomstatus)
                                 {
                                    dvPmsDatabase.insertPmsiGuestDeviceStatus(
                                             keyId, inRoomDevice, pmsiGuestID,
                                             dvPmsDatabase.getMasterStatusId(
                                                      DVPmsiStatus.checkout
                                                               .toString()),
                                             DVPmsGuestTypes.secondary
                                                      .toString());

                                    dvPmsDatabase.insertPmsiGuestDeviceStatus(
                                             keyId, inRoomDevice,
                                             secondaryGuestId,
                                             dvPmsDatabase.getMasterStatusId(
                                                      DVPmsiStatus.checkin
                                                               .toString()),
                                             DVPmsGuestTypes.primary
                                                      .toString());
                                    inRoomDeviceIt.remove();

                                 }
                              }
                           }
                           else
                           {
                              dvLogger.info(" Checkout  Failed  for  IP  "
                                       + deviceIp);
                              SentToallDevices = false;
                              dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                       controllerId, secondaryGuestId,
                                       dvPmsDatabase.getMasterStatusId(
                                                DVPmsiStatus.PENDING_CHECKIN
                                                         .toString()),
                                       DVPmsGuestTypes.primary.toString());

                              dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                       controllerId, pmsiGuestID,
                                       dvPmsDatabase.getMasterStatusId(
                                                DVPmsiStatus.PENDING_CHECKOUT
                                                         .toString()),
                                       DVPmsGuestTypes.secondary.toString());

                           }
                        }
                        for (int inRoomDevice : InRoomDevices)
                        {
                           SentToallDevices = false;

                           dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                    inRoomDevice, pmsiGuestID,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.PENDING_CHECKOUT
                                                      .toString()),
                                    DVPmsGuestTypes.secondary.toString());

                           dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                    inRoomDevice, secondaryGuestId,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                      .toString()),
                                    DVPmsGuestTypes.primary.toString());



                        }
                        try
                        {
                           Iterator<Integer> inRoomDeviceIt =
                                    NonDvcInRoomDevices.iterator();
                           while (inRoomDeviceIt.hasNext())
                           {
                              int inRoomDevice = inRoomDeviceIt.next();
                              String deviceIp = dvPmsDatabase.getIp(inRoomDevice);
                              String jsonRequest="";
                              int inRoomdvsId = dvPmsDatabase
                                       .getDvsDeviceId(inRoomDevice);
                              jsonRequest = getCheckoutGuestInfoJson(
                                       DVDeviceTypes.ipad.toString(),
                                       inRoomdvsId + "",
                                       DVPmsiStatus.checkout.toString(),
                                       DVPmsiStatus.guestInfoUpdate
                                                .toString(),
                                       secondary_guest_data);
                              boolean InRoomstatus = false;

                              InRoomstatus = dataToController.SendData(
                                       deviceIp, jsonRequest, inRoomdvsId);
                              if (InRoomstatus)
                              {
                                 dvPmsDatabase.insertPmsiGuestDeviceStatus(
                                          keyId, inRoomDevice, pmsiGuestID,
                                          dvPmsDatabase.getMasterStatusId(
                                                   DVPmsiStatus.checkout
                                                            .toString()),
                                          DVPmsGuestTypes.secondary
                                                   .toString());

                                 dvPmsDatabase.insertPmsiGuestDeviceStatus(
                                          keyId, inRoomDevice,
                                          secondaryGuestId,
                                          dvPmsDatabase.getMasterStatusId(
                                                   DVPmsiStatus.checkin
                                                            .toString()),
                                          DVPmsGuestTypes.primary
                                                   .toString());
                                 inRoomDeviceIt.remove();

                              }
                           }
                        }
                        catch (Exception e)
                        {
                           dvLogger.error("Error in sending checkout to ipad ", e);
                        }
                        for (int inRoomDevice : NonDvcInRoomDevices)
                        {
                           SentToallDevices = false;

                           dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                    inRoomDevice, pmsiGuestID,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.PENDING_CHECKOUT
                                                      .toString()),
                                    DVPmsGuestTypes.secondary.toString());

                           dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                    inRoomDevice, secondaryGuestId,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                      .toString()),
                                    DVPmsGuestTypes.primary.toString());
                        }
                        
                        Iterator<Integer> xplayerUiIt = XplayerUi.iterator();
                        while (xplayerUiIt.hasNext())
                        {
                           int xplayerUi = xplayerUiIt.next();
                           String deviceIp = dvPmsDatabase.getIp(xplayerUi);
                           int inRoomdvsId =
                                    dvPmsDatabase.getDvsDeviceId(xplayerUi);

                           String jsonRequest = getCheckoutGuestInfoJson(
                                    DVDeviceTypes.tvui.toString(),
                                    inRoomdvsId + "",
                                    DVPmsiStatus.checkout.toString(),
                                    DVPmsiStatus.guestInfoUpdate.toString(),
                                    secondary_guest_data);

                           boolean status = false;


                           status = dataToController.SendData(deviceIp,
                                    jsonRequest, inRoomdvsId);

                           if (status)
                           {
                              dvLogger.info(
                                       " Checkout  Success  for Xplayer  IP  "
                                                + deviceIp);
                              dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                       xplayerUi, pmsiGuestID,
                                       dvPmsDatabase.getMasterStatusId(
                                                DVPmsiStatus.checkout
                                                         .toString()),
                                       DVPmsGuestTypes.secondary.toString());

                              dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                       xplayerUi, pmsiGuestID,
                                       dvPmsDatabase.getMasterStatusId(
                                                DVPmsiStatus.checkin
                                                         .toString()),
                                       DVPmsGuestTypes.primary.toString());
                              xplayerUiIt.remove();
                           }
                           else
                           {
                              dvLogger.info(
                                       " Checkout  Failed  for Xplayer IP  "
                                                + deviceIp);
                              SentToallDevices = false;

                              dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                       xplayerUi, pmsiGuestID,
                                       dvPmsDatabase.getMasterStatusId(
                                                DVPmsiStatus.PENDING_CHECKOUT
                                                         .toString()),
                                       DVPmsGuestTypes.secondary.toString());

                              dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                       xplayerUi, pmsiGuestID,
                                       dvPmsDatabase.getMasterStatusId(
                                                DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                         .toString()),
                                       DVPmsGuestTypes.primary.toString());
                           }

                        }

                        if (SentToallDevices)
                        {

                           dvPmsDatabase.updateKeyStatusDigivaletStatus(
                                    DVPmsGuestTypes.secondary.toString(), keyId,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.checkout.toString()));


                           dvPmsDatabase.updateKeyStatusDigivaletStatus(
                                    DVPmsGuestTypes.primary.toString(), keyId,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.checkin.toString()));

                        }
                        else
                        {
                           dvPmsDatabase.updateKeyStatusDigivaletStatus(
                                    DVPmsGuestTypes.secondary.toString(), keyId,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.PENDING_CHECKOUT
                                                      .toString()));

                           dvPmsDatabase.updateKeyStatusDigivaletStatus(
                                    DVPmsGuestTypes.primary.toString(), keyId,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                      .toString()));


                        }
                     }
                  }
                  else
                  {
                     dvLogger.info("Sending primary guest a normal checkout ");
                     dvLogger.info(
                              "Normal Checkout Secondary thread will send a normal checkout");

                     dvPmsDatabase.removeDevicesFromGuestDevice(keyId,
                              DVPmsGuestTypes.primary.toString());

                     dvPmsDatabase.updateKeyStatusDigivaletStatus(
                              data.get(DVPmsData.guestType).toString(), keyId,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.SENDING_CHECKOUT
                                                .toString()));
                     boolean SentToallDevices = true;
                     ArrayList<Integer> controllers =
                              dvPmsDatabase.getDvcByKey(keyId);

                     Iterator<Integer> dvcItrator = controllers.iterator();

                     DVDataToController dataToController =
                              new DVDataToController(dvSettings,
                                       communicationTokenManager, keyId);
                     while (dvcItrator.hasNext())
                     {
                        boolean status = false;
                        int controllerId = dvcItrator.next();
                        int dvsId = dvPmsDatabase.getDvsDeviceId(controllerId);
                        String deviceIp = dvPmsDatabase.getIp(controllerId);
                        String jsonRequest = getRequestJson(
                                 DVDeviceTypes.dvc.toString(), dvsId + "",
                                 DVPmsiStatus.checkout.toString());
                        status = dataToController.SendData(deviceIp,
                                 jsonRequest, dvsId);

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
                                 if (dvPmsDatabase
                                          .isPrimaryguestCheckedInCheckedOut(
                                                   inRoomDevice))
                                 {
                                    InRoomstatus =
                                             dataToController.SendData(deviceIp,
                                                      jsonRequest, inRoomdvsId);
                                 }
                                 else
                                 {
                                    dvLogger.info(
                                             "Will not checkout deivce since Primary guest is not cheked out ");
                                 }
                              }
                              else
                              {
                                 InRoomstatus = dataToController.SendData(
                                          deviceIp, jsonRequest, inRoomdvsId);
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
                     for (int inRoomDevice : InRoomDevices)
                     {
                        SentToallDevices = false;
                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 inRoomDevice, pmsiGuestID,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.PENDING_CHECKOUT
                                                   .toString()),
                                 data.get(DVPmsData.guestType).toString());
                     }
                     try
                     {
                        Iterator<Integer> inRoomDeviceIt =
                                 NonDvcInRoomDevices.iterator();
                        while (inRoomDeviceIt.hasNext())
                        {
                           int inRoomDevice = inRoomDeviceIt.next();
                           String deviceIp = dvPmsDatabase.getIp(inRoomDevice);
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
                              if (dvPmsDatabase
                                       .isPrimaryguestCheckedInCheckedOut(
                                                inRoomDevice))
                              {
                                 InRoomstatus =
                                          dataToController.SendData(deviceIp,
                                                   jsonRequest, inRoomdvsId);
                              }
                              else
                              {
                                 dvLogger.info(
                                          "Will not checkout deivce since Primary guest is not cheked out ");
                              }
                           }
                           else
                           {
                              InRoomstatus = dataToController.SendData(
                                       deviceIp, jsonRequest, inRoomdvsId);
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
                     }
                     catch (Exception e)
                     {
                        dvLogger.error("Error in sending checkout to non controller devices ", e);
                     }
                     for (int inRoomDevice : NonDvcInRoomDevices)
                     {
                        SentToallDevices = false;
                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 inRoomDevice, pmsiGuestID,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.PENDING_CHECKOUT
                                                   .toString()),
                                 data.get(DVPmsData.guestType).toString());
                     }
                     
                     
                     Iterator<Integer> xplayerUiIt = XplayerUi.iterator();
                     while (xplayerUiIt.hasNext())
                     {
                        int xplayerUi = xplayerUiIt.next();
                        String deviceIp = dvPmsDatabase.getIp(xplayerUi);
                        int inRoomdvsId =
                                 dvPmsDatabase.getDvsDeviceId(xplayerUi);
                        String jsonRequest =
                                 getRequestJson(DVDeviceTypes.tvui.toString(),
                                          inRoomdvsId + "",
                                          DVPmsiStatus.checkout.toString());
                        boolean status = false;
                        if (data.get(DVPmsData.guestType).toString()
                                 .equalsIgnoreCase(
                                          DVPmsGuestTypes.secondary.toString()))
                        {
                           if (dvPmsDatabase.isPrimaryguestCheckedInCheckedOut(
                                    xplayerUi))
                           {
                              status = dataToController.SendData(deviceIp,
                                       jsonRequest, inRoomdvsId);
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
                                    jsonRequest, inRoomdvsId);
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
                     if (SentToallDevices)
                     {

                        dvPmsDatabase.updateKeyStatusDigivaletStatus(
                                 data.get(DVPmsData.guestType).toString(),
                                 keyId, dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.checkout.toString()));


                     }
                     else
                     {

                        dvPmsDatabase.updateKeyStatusDigivaletStatus(
                                 data.get(DVPmsData.guestType).toString(),
                                 keyId,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.PENDING_CHECKOUT
                                                   .toString()));
                     }



                  }
               }
            }
            else if (data.get(DVPmsData.guestType).toString()
                     .equalsIgnoreCase(DVPmsGuestTypes.primary.toString())
                     && dvPmsDatabase.checkKeyDigivaletPendingCheckin(keyId,
                              DVPmsGuestTypes.secondary.toString()))
            {

               dvLogger.info(
                        "Received primary checkout when secondary is Pending checkin");
               dvLogger.info(
                        "Checkout Primary and update secondary to primary");
               HashMap<Integer, Integer> oldInRoomDeviceStatus =
                        dvPmsDatabase.getAllInRoomDevicesStatus(keyId,
                                 DVPmsGuestTypes.secondary.toString());

               dvPmsDatabase.removeDevicesFromGuestDevice(keyId,
                        DVPmsGuestTypes.secondary.toString());

               dvPmsDatabase.updateKeyStatusDigivaletStatus(
                        data.get(DVPmsData.guestType).toString(), keyId,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.SENDING_CHECKOUT.toString()));

               dvPmsDatabase.updateKeyStatusDigivaletStatus(
                        DVPmsGuestTypes.secondary.toString(), keyId,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.SENDING_CHECKIN.toString()));



               int secondaryGuestId = dvPmsDatabase.getGuestIdFromKey(keyId,
                        DVPmsGuestTypes.secondary.toString());
               dvPmsDatabase.updateKeyStatusGuestTypeByGuestId(
                        DVPmsGuestTypes.primary.toString(), pmsiGuestID);
               dvPmsDatabase.updateKeyStatusGuestTypeByGuestId(
                        DVPmsGuestTypes.secondary.toString(), secondaryGuestId);
               dvPmsDatabase.updatePmsiGuestTypeByGuestId(
                        DVPmsGuestTypes.primary.toString(), secondaryGuestId);
               Map<DVPmsData, Object> secondary_guest_data =
                        dvPmsDatabase.getDataByPmsiGuestId(secondaryGuestId);
               ArrayList<Integer> controllers =
                        dvPmsDatabase.getDvcByKey(keyId);

               Iterator<Integer> dvcItrator = controllers.iterator();
               boolean SentToallDevices = true;

               DVDataToController dataToController = new DVDataToController(
                        dvSettings, communicationTokenManager, keyId);
               while (dvcItrator.hasNext())
               {
                  boolean status = false;
                  int controllerId = dvcItrator.next();
                  int dvsId = dvPmsDatabase.getDvsDeviceId(controllerId);
                  String deviceIp = dvPmsDatabase.getIp(controllerId);

                  String jsonCheckoutCheckin = getCheckoutGuestInfoJson(
                           DVDeviceTypes.dvc.toString(), dvsId + "",
                           DVPmsiStatus.checkout.toString(),
                           DVPmsiStatus.checkin.toString(),
                           secondary_guest_data);

                  String jsonCheckoutGuestinfo = getCheckoutGuestInfoJson(
                           DVDeviceTypes.dvc.toString(), dvsId + "",
                           DVPmsiStatus.checkout.toString(),
                           DVPmsiStatus.guestInfoUpdate.toString(),
                           secondary_guest_data);


                  if (oldInRoomDeviceStatus.containsKey(controllerId))
                  {
                     if (oldInRoomDeviceStatus.get(
                              controllerId) == dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkin.toString()))
                     {
                        status = dataToController.SendData(deviceIp,
                                 jsonCheckoutGuestinfo, dvsId);
                     }
                     else
                     {
                        status = dataToController.SendData(deviceIp,
                                 jsonCheckoutCheckin, dvsId);
                     }
                  }
                  else
                  {
                     status = dataToController.SendData(deviceIp,
                              jsonCheckoutCheckin, dvsId);
                  }
                  if (status)
                  {
                     dvLogger.info(" Checkout  Success  for  IP  " + deviceIp);
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                              controllerId, secondaryGuestId,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkin.toString()),
                              DVPmsGuestTypes.primary.toString());

                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                              controllerId, pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkout.toString()),
                              DVPmsGuestTypes.secondary.toString());


                     Iterator<Integer> inRoomDeviceIt =
                              InRoomDevices.iterator();
                     while (inRoomDeviceIt.hasNext())
                     {
                        int inRoomDevice = inRoomDeviceIt.next();
                        int inRoomdvsId =
                                 dvPmsDatabase.getDvsDeviceId(inRoomDevice);
                        jsonCheckoutCheckin = getCheckoutGuestInfoJson(
                                 DVDeviceTypes.ipad.toString(),
                                 inRoomdvsId + "",
                                 DVPmsiStatus.checkout.toString(),
                                 DVPmsiStatus.checkin.toString(),
                                 secondary_guest_data);

                        jsonCheckoutGuestinfo = getCheckoutGuestInfoJson(
                                 DVDeviceTypes.dvc.toString(), inRoomdvsId + "",
                                 DVPmsiStatus.checkout.toString(),
                                 DVPmsiStatus.guestInfoUpdate.toString(),
                                 secondary_guest_data);
                        boolean InRoomstatus = false;

                        if (oldInRoomDeviceStatus.containsKey(inRoomDevice))
                        {
                           if (oldInRoomDeviceStatus
                                    .get(inRoomDevice) == dvPmsDatabase
                                             .getMasterStatusId(
                                                      DVPmsiStatus.checkin
                                                               .toString()))
                           {
                              InRoomstatus = dataToController.SendData(deviceIp,
                                       jsonCheckoutGuestinfo, inRoomdvsId);
                           }
                           else
                           {
                              InRoomstatus = dataToController.SendData(deviceIp,
                                       jsonCheckoutCheckin, inRoomdvsId);
                           }
                        }
                        else
                        {
                           InRoomstatus = dataToController.SendData(deviceIp,
                                    jsonCheckoutCheckin, inRoomdvsId);
                        }


                        if (InRoomstatus)
                        {
                           dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                    inRoomDevice, pmsiGuestID,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.checkout.toString()),
                                    DVPmsGuestTypes.secondary.toString());

                           dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                    inRoomDevice, secondaryGuestId,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.checkin.toString()),
                                    DVPmsGuestTypes.primary.toString());
                           inRoomDeviceIt.remove();

                        }
                     }
                  }
                  else
                  {
                     dvLogger.info(" Checkout  Failed  for  IP  " + deviceIp);
                     SentToallDevices = false;
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                              controllerId, secondaryGuestId,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKIN.toString()),
                              DVPmsGuestTypes.primary.toString());

                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                              controllerId, pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKOUT
                                                .toString()),
                              DVPmsGuestTypes.secondary.toString());

                  }
               }
               for (int inRoomDevice : InRoomDevices)
               {
                  SentToallDevices = false;
                  dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, inRoomDevice,
                           pmsiGuestID,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKOUT.toString()),
                           DVPmsGuestTypes.secondary.toString());

                  dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, inRoomDevice,
                           secondaryGuestId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKIN.toString()),
                           DVPmsGuestTypes.primary.toString());
               }
               try
               {
                  Iterator<Integer> inRoomDeviceIt =
                           NonDvcInRoomDevices.iterator();
                  while (inRoomDeviceIt.hasNext())
                  {
                     int inRoomDevice = inRoomDeviceIt.next();
                     int inRoomdvsId =
                              dvPmsDatabase.getDvsDeviceId(inRoomDevice);
                     String deviceIp = dvPmsDatabase.getIp(inRoomDevice);
                     String jsonCheckoutGuestinfo="";
                     String jsonCheckoutCheckin="";
                     jsonCheckoutCheckin = getCheckoutGuestInfoJson(
                              DVDeviceTypes.ipad.toString(),
                              inRoomdvsId + "",
                              DVPmsiStatus.checkout.toString(),
                              DVPmsiStatus.checkin.toString(),
                              secondary_guest_data);

                     jsonCheckoutGuestinfo = getCheckoutGuestInfoJson(
                              DVDeviceTypes.dvc.toString(), inRoomdvsId + "",
                              DVPmsiStatus.checkout.toString(),
                              DVPmsiStatus.guestInfoUpdate.toString(),
                              secondary_guest_data);
                     boolean InRoomstatus = false;

                     if (oldInRoomDeviceStatus.containsKey(inRoomDevice))
                     {
                        if (oldInRoomDeviceStatus
                                 .get(inRoomDevice) == dvPmsDatabase
                                          .getMasterStatusId(
                                                   DVPmsiStatus.checkin
                                                            .toString()))
                        {
                           InRoomstatus = dataToController.SendData(deviceIp,
                                    jsonCheckoutGuestinfo, inRoomdvsId);
                        }
                        else
                        {
                           InRoomstatus = dataToController.SendData(deviceIp,
                                    jsonCheckoutCheckin, inRoomdvsId);
                        }
                     }
                     else
                     {
                        InRoomstatus = dataToController.SendData(deviceIp,
                                 jsonCheckoutCheckin, inRoomdvsId);
                     }


                     if (InRoomstatus)
                     {
                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 inRoomDevice, pmsiGuestID,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.checkout.toString()),
                                 DVPmsGuestTypes.secondary.toString());

                        dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                                 inRoomDevice, secondaryGuestId,
                                 dvPmsDatabase.getMasterStatusId(
                                          DVPmsiStatus.checkin.toString()),
                                 DVPmsGuestTypes.primary.toString());
                        inRoomDeviceIt.remove();

                     }
                  }
               }
               catch (Exception e)
               {
                  dvLogger.error("Error in sending to non controller devices ", e);
               }
               for (int inRoomDevice : NonDvcInRoomDevices)
               {
                  SentToallDevices = false;
                  dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, inRoomDevice,
                           pmsiGuestID,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKOUT.toString()),
                           DVPmsGuestTypes.secondary.toString());

                  dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, inRoomDevice,
                           secondaryGuestId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKIN.toString()),
                           DVPmsGuestTypes.primary.toString());
               }
               Iterator<Integer> xplayerUiIt = XplayerUi.iterator();
               while (xplayerUiIt.hasNext())
               {
                  int xplayerUi = xplayerUiIt.next();
                  String deviceIp = dvPmsDatabase.getIp(xplayerUi);
                  int inRoomdvsId = dvPmsDatabase.getDvsDeviceId(xplayerUi);
                  String jsonCheckoutCheckin = getCheckoutGuestInfoJson(
                           DVDeviceTypes.tvui.toString(), inRoomdvsId + "",
                           DVPmsiStatus.checkout.toString(),
                           DVPmsiStatus.checkin.toString(),
                           secondary_guest_data);

                  String jsonCheckoutGuestinfo = getCheckoutGuestInfoJson(
                           DVDeviceTypes.dvc.toString(), inRoomdvsId + "",
                           DVPmsiStatus.checkout.toString(),
                           DVPmsiStatus.guestInfoUpdate.toString(),
                           secondary_guest_data);
                  boolean status = false;
                  if (oldInRoomDeviceStatus.containsKey(xplayerUi))
                  {
                     if (oldInRoomDeviceStatus.get(xplayerUi) == dvPmsDatabase
                              .getMasterStatusId(
                                       DVPmsiStatus.checkin.toString()))
                     {
                        status = dataToController.SendData(deviceIp,
                                 jsonCheckoutGuestinfo, inRoomdvsId);
                     }
                     else
                     {
                        status = dataToController.SendData(deviceIp,
                                 jsonCheckoutCheckin, inRoomdvsId);
                     }
                  }
                  else
                  {
                     status = dataToController.SendData(deviceIp,
                              jsonCheckoutCheckin, inRoomdvsId);
                  }

                  if (status)
                  {
                     dvLogger.info(" Checkout  Success  for Xplayer  IP  "
                              + deviceIp);
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkout.toString()),
                              DVPmsGuestTypes.secondary.toString());

                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkin.toString()),
                              DVPmsGuestTypes.primary.toString());
                     xplayerUiIt.remove();
                  }
                  else
                  {
                     dvLogger.info(
                              " Checkout  Failed  for Xplayer IP  " + deviceIp);
                     SentToallDevices = false;

                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKOUT
                                                .toString()),
                              DVPmsGuestTypes.secondary.toString());

                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                                .toString()),
                              DVPmsGuestTypes.primary.toString());
                  }

               }

               if (SentToallDevices)
               {
                  dvPmsDatabase.updateKeyStatusDigivaletStatus(
                           DVPmsGuestTypes.secondary.toString(), keyId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.checkout.toString()));

                  dvPmsDatabase.updateKeyStatusDigivaletStatus(
                           DVPmsGuestTypes.primary.toString(), keyId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.checkin.toString()));

               }
               else
               {
                  dvPmsDatabase.updateKeyStatusDigivaletStatus(
                           DVPmsGuestTypes.secondary.toString(), keyId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKOUT.toString()));

                  dvPmsDatabase.updateKeyStatusDigivaletStatus(
                           DVPmsGuestTypes.primary.toString(), keyId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                             .toString()));

               }
               // END

            }
            else
            {

               dvLogger.info("Normal Checkout ");

               dvPmsDatabase.updateKeyStatusDigivaletStatus(
                        data.get(DVPmsData.guestType).toString(), keyId,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.SENDING_CHECKOUT.toString()));



               boolean SentToallDevices = true;

               ArrayList<Integer> controllers =
                        dvPmsDatabase.getDvcByKey(keyId);

               Iterator<Integer> dvcItrator = controllers.iterator();

               DVDataToController dataToController = new DVDataToController(
                        dvSettings, communicationTokenManager, keyId);
               while (dvcItrator.hasNext())
               {
                  boolean status = false;
                  int controllerId = dvcItrator.next();
                  int dvsId = dvPmsDatabase.getDvsDeviceId(controllerId);
                  String deviceIp = dvPmsDatabase.getIp(controllerId);
                  String jsonRequest = getRequestJson(
                           DVDeviceTypes.dvc.toString(), dvsId + "",
                           DVPmsiStatus.checkout.toString());
                  status = dataToController.SendData(deviceIp, jsonRequest,
                           dvsId);

                  if (status)
                  {
                     dvLogger.info(" Checkout  Success  for  IP  " + deviceIp);
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
                        int inRoomdvsId =
                                 dvPmsDatabase.getDvsDeviceId(inRoomDevice);
                        jsonRequest =
                                 getRequestJson(DVDeviceTypes.ipad.toString(),
                                          inRoomdvsId + "",
                                          DVPmsiStatus.checkout.toString());
                        boolean InRoomstatus = false;

                        if (data.get(DVPmsData.guestType).toString()
                                 .equalsIgnoreCase(
                                          DVPmsGuestTypes.secondary.toString()))
                        {
                           if (dvPmsDatabase.isPrimaryguestCheckedInCheckedOut(
                                    inRoomDevice))
                           {
                              InRoomstatus = dataToController.SendData(deviceIp,
                                       jsonRequest, inRoomdvsId);
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
                                    jsonRequest, inRoomdvsId);
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
                  }
                  else
                  {
                     dvLogger.info(" Checkout  Failed  for  IP  " + deviceIp);
                     SentToallDevices = false;
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId,
                              controllerId, pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKOUT
                                                .toString()),
                              data.get(DVPmsData.guestType).toString());
                  }
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
               try
               {
                  Iterator<Integer> inRoomDeviceIt =
                           NonDvcInRoomDevices.iterator();

                  while (inRoomDeviceIt.hasNext())
                  {
                     int inRoomDevice = inRoomDeviceIt.next();
                     String deviceIp = dvPmsDatabase.getIp(inRoomDevice);
                     String jsonRequest="";
                     int inRoomdvsId =
                              dvPmsDatabase.getDvsDeviceId(inRoomDevice);
                     jsonRequest =
                              getRequestJson(DVDeviceTypes.ipad.toString(),
                                       inRoomdvsId + "",
                                       DVPmsiStatus.checkout.toString());
                     boolean InRoomstatus = false;

                     if (data.get(DVPmsData.guestType).toString()
                              .equalsIgnoreCase(
                                       DVPmsGuestTypes.secondary.toString()))
                     {
                        if (dvPmsDatabase.isPrimaryguestCheckedInCheckedOut(
                                 inRoomDevice))
                        {
                           InRoomstatus = dataToController.SendData(deviceIp,
                                    jsonRequest, inRoomdvsId);
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
                                 jsonRequest, inRoomdvsId);
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
               }
               catch (Exception e)
               {
                  dvLogger.error("Error in sending checkout to non controller devices ", e);
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
               Iterator<Integer> xplayerUiIt = XplayerUi.iterator();
               while (xplayerUiIt.hasNext())
               {
                  int xplayerUi = xplayerUiIt.next();
                  String deviceIp = dvPmsDatabase.getIp(xplayerUi);
                  int inRoomdvsId = dvPmsDatabase.getDvsDeviceId(xplayerUi);
                  String jsonRequest = getRequestJson(
                           DVDeviceTypes.tvui.toString(), inRoomdvsId + "",
                           DVPmsiStatus.checkout.toString());
                  boolean status = false;
                  if (data.get(DVPmsData.guestType).toString().equalsIgnoreCase(
                           DVPmsGuestTypes.secondary.toString()))
                  {
                     if (dvPmsDatabase
                              .isPrimaryguestCheckedInCheckedOut(xplayerUi))
                     {
                        status = dataToController.SendData(deviceIp,
                                 jsonRequest, inRoomdvsId);
                     }
                     else
                     {
                        dvLogger.info(
                                 "Will not checkout deivce since Primary guest is not cheked out ");
                     }
                  }
                  else
                  {
                     status = dataToController.SendData(deviceIp, jsonRequest,
                              inRoomdvsId);
                  }
                  if (status)
                  {
                     dvLogger.info(" Checkout  Success  for Xplayer  IP  "
                              + deviceIp);
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.checkout.toString()),
                              data.get(DVPmsData.guestType).toString());
                  }
                  else
                  {
                     dvLogger.info(
                              " Checkout  Failed  for Xplayer IP  " + deviceIp);
                     SentToallDevices = false;
                     dvPmsDatabase.insertPmsiGuestDeviceStatus(keyId, xplayerUi,
                              pmsiGuestID,
                              dvPmsDatabase.getMasterStatusId(
                                       DVPmsiStatus.PENDING_CHECKOUT
                                                .toString()),
                              data.get(DVPmsData.guestType).toString());
                  }
               }
               if (SentToallDevices)
               {
                  dvPmsDatabase.updateKeyStatusDigivaletStatus(
                           data.get(DVPmsData.guestType).toString(), keyId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.checkout.toString()));
               }
               else
               {
                  dvPmsDatabase.updateKeyStatusDigivaletStatus(
                           data.get(DVPmsData.guestType).toString(), keyId,
                           dvPmsDatabase.getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKOUT.toString()));
               }
            }
         }else
         {
            dvLogger.info(" Discarding this checkout "+pmsiGuestID);
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
      JsonArray activeGuestsArray = new JsonArray();
      JsonArray jsonResponseArray = new JsonArray();
      jsonArray.add(jsondeto);
      ArrayList<String> activeGuest=dvPmsDatabase.getActiveGuestIds(keyId);
      for(int i=0;i<activeGuest.size();i++)
      {
         activeGuestsArray.add(activeGuest.get(i));
      }

      // jsonDetails.addProperty("deviceType", deviceType);
      // jsonDetails.addProperty("deviceId", device_id);
      jsonDetails.add("guestDetails", jsonArray);
      jsonDetails.add("activeGuestId", activeGuestsArray);
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


   private String getCheckoutGuestInfoJson(String deviceType, String device_id,
            String type1, String type2, Map<DVPmsData, Object> data2)
   {
      Gson gson = new Gson();
      String jsondata1 = gson.toJson(data);
      String jsondata2 = gson.toJson(data2);
      JsonParser parser = new JsonParser();
      JsonObject jsondeto = parser.parse(jsondata1).getAsJsonObject();
      JsonObject jsondeto2 = parser.parse(jsondata2).getAsJsonObject();
      JsonObject jsonRequest = new JsonObject();
      JsonObject jsonResonse1 = new JsonObject();
      JsonObject jsonResonse2 = new JsonObject();
      JsonObject jsonDetails = new JsonObject();
      JsonObject jsonDetails2 = new JsonObject();
      JsonArray jsonArray = new JsonArray();
      JsonArray jsonArray2 = new JsonArray();

      JsonArray jsonResponseArray = new JsonArray();

      jsonArray.add(jsondeto);
      jsonArray2.add(jsondeto2);
      JsonArray activeGuestsArray = new JsonArray();
      ArrayList<String> activeGuest=dvPmsDatabase.getActiveGuestIds(keyId);
      for(int i=0;i<activeGuest.size();i++)
      {
         activeGuestsArray.add(activeGuest.get(i));
      }
      // jsonDetails.addProperty("deviceType", deviceType);
      // jsonDetails.addProperty("deviceId", device_id);
      jsonDetails.add("guestDetails", jsonArray);
      jsonDetails.add("activeGuestId", activeGuestsArray);
      jsonResonse1.addProperty("feature", "room");
      jsonResonse1.addProperty("type", type1);
      jsonResonse1.addProperty("targetDeviceType", deviceType);
      jsonResonse1.addProperty("targetDeviceId", device_id);
      jsonResonse1.add("details", jsonDetails);
      jsonResponseArray.add(jsonResonse1);


      jsonDetails2.add("guestDetails", jsonArray2);
      jsonDetails2.add("activeGuestId", activeGuestsArray);
      jsonResonse2.addProperty("feature", "room");
      jsonResonse2.addProperty("type", type2);
      jsonResonse2.addProperty("targetDeviceType", deviceType);
      jsonResonse2.addProperty("targetDeviceId", device_id);
      jsonResonse2.add("details", jsonDetails2);
      jsonResponseArray.add(jsonResonse2);



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
