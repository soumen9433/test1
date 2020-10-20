package com.digivalet.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.codehaus.jackson.node.ObjectNode;
import com.digivalet.movieposting.DVSendPendingMovieToDevices;
import com.digivalet.movieposting.SendButlerRequestOnCheckinOut;
import com.digivalet.movieposting.SendCheckinCheckoutInFormationUpdateEvent;
import com.digivalet.pms.guestpreference.model.DVGuestPreferenceModel;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.dashboardCheckinEvent.DVDashboardCheckinCheckoutEvent;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVDeviceTypes;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.datatypes.DVPmsGuestTypes;
import com.digivalet.pmsi.events.DVEvent;
import com.digivalet.pmsi.events.DVEvent.FeatureEventType;
import com.digivalet.pmsi.events.DVSendCheckin;
import com.digivalet.pmsi.events.DVSendCheckout;
import com.digivalet.pmsi.events.DVSendGuestInformationUpdate;
import com.digivalet.pmsi.model.CheckinCheckoutData;
import com.digivalet.pmsi.settings.DVSettings;
import com.digivalet.pmsi.util.DVNotificationManager;
import com.digivalet.pmsi.util.DVPMSIConstants;

public class DVCheckinCheckoutEvent implements Runnable
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private Map<DVPmsData, Object> data = new HashMap<>();
   private DVSettings dvSettings;
   private DVEvent dvEvent;
   private int keyId = 0;
   private String hotelId = "";
   private int pmsiGuestID = 0;
   private DVPmsDatabase dvPmsDatabase;
   private ArrayList<Integer> InRoomDevices = new ArrayList();
   private ArrayList<Integer> NonDvcInRoomDevices = new ArrayList();
   private ArrayList<Integer> XplayerUi = new ArrayList();
   private DVKeyCommunicationTokenManager communicationTokenManager;
   private final int safeEventTimeForKey = 5 * 1000;
   private boolean hasGuestNameUpdate = true;
   

   public DVCheckinCheckoutEvent(DVEvent dvEvent, DVSettings dvSettings,
            DVPmsDatabase dvPmsDatabase,
            DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.dvEvent = dvEvent;
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      this.data = dvEvent.getData();
      this.communicationTokenManager = communicationTokenManager;
      init();

   }


   public DVCheckinCheckoutEvent(DVEvent dvEvent, DVSettings dvSettings,
            DVPmsDatabase dvPmsDatabase,
            DVKeyCommunicationTokenManager communicationTokenManager,
            boolean hasGuestNameUpdate)
   {
      this.dvEvent = dvEvent;
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      this.data = dvEvent.getData();
      this.communicationTokenManager = communicationTokenManager;
      this.hasGuestNameUpdate = hasGuestNameUpdate;
      init();
   }

   private void init()
   {
      try
      {
         hotelId = dvSettings.getHotelId();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in init DVCheckinCheckoutEvent ", e);
      }
   }


   public String convertObjectToJsonString(Object notifiedObject)
   {
      String[] filteredProperties = new String[10];
      ObjectMapper mapper = new ObjectMapper();
      FilterProvider filters = new SimpleFilterProvider().addFilter("",
               SimpleBeanPropertyFilter.serializeAllExcept(filteredProperties));
      String responseJson = "";
      try
      {
         responseJson =
                  mapper.writer(filters).writeValueAsString(notifiedObject);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while converting object into json. Object:"
                  + notifiedObject + ". Exception : ", e);
      }
      return responseJson;
   }

   public void run()
   {
      try
      {
         dvLogger.info("Recevied "
                  + this.dvEvent.getFeatureEventType().toString() + "  For Key "
                  + this.data.get(DVPmsData.keyId).toString() + " data: "
                  + this.data.toString());
         keyId = dvPmsDatabase.getKeyId(data.get(DVPmsData.keyId).toString());
         
         if(keyId==0)
         {
            keyId = Integer.parseInt(this.data.get(DVPmsData.keyId).toString());
         }         
         data = initializeByBlank(data);
         if (keyId != 0)
         {
            populateInRoomDevices();
            populateXplayerUi();
            populateNonDvcInRoomDevices();
            if (dvEvent
                     .getFeatureEventType() == FeatureEventType.PMSI_CHECKIN_EVENT)
            {
               Thread.currentThread().setName(
                        FeatureEventType.PMSI_CHECKIN_EVENT + " KEY: " + keyId);
               processCheckinEvent();
            }
            else if (dvEvent
                     .getFeatureEventType() == FeatureEventType.PMSI_CHECKOUT_EVENT)
            {
               Thread.currentThread()
                        .setName(FeatureEventType.PMSI_CHECKOUT_EVENT + " KEY: "
                                 + keyId);
               processCheckoutEvent();
            }
            else if (dvEvent
                     .getFeatureEventType() == FeatureEventType.PMSI_GUEST_INFORMATION_UPDATE_EVENT)
            {
               Thread.currentThread()
                        .setName(FeatureEventType.PMSI_GUEST_INFORMATION_UPDATE_EVENT
                                 + " KEY: " + keyId);
               dataManuplation();
               pmsiGuestID = dvPmsDatabase.getPmsiGuestId(
                        data.get(DVPmsData.guestId).toString(), keyId);
               if (dvPmsDatabase.checkIsGuestCheckedInKey(pmsiGuestID,keyId))
               {
                  getUpdatedGuestType();
                  getGuestPreference();
                  if (dvPmsDatabase.UpdateGuestDetails(data, keyId))
                  {

                     dvLogger.analytics(
                              "operation::"
                                       + DVAnalyticsEventType.guestInformationUpdate,
                              "details::"
                                       + convertObjectToJsonString(this.data));
                     DVSendGuestInformationUpdate guestInformationUpdate =
                              new DVSendGuestInformationUpdate(dvSettings,
                                       keyId, hotelId, pmsiGuestID,
                                       dvPmsDatabase, data, InRoomDevices,
                                       XplayerUi, communicationTokenManager,
                                       NonDvcInRoomDevices);
                     guestInformationUpdate.sendGuestInfoUpdateToDevices();
                     sendGuestCheckinCheckoutEvent(
                              InformationUpdate.guestInformationUpdate.name());
                  }
                  else
                  {
                     dvLogger.info("No Guest information change ");
                  }



               }
               else
               {
                  dvLogger.info(
                           "Room is not checkin with this guest so  won't update the guest details ");
               }
            }
            else if (dvEvent
                     .getFeatureEventType() == FeatureEventType.PMSI_ROOMCHANGE_CHECKIN_EVENT)
            {
               Thread.currentThread()
                        .setName(FeatureEventType.PMSI_ROOMCHANGE_CHECKIN_EVENT
                                 + " KEY: " + keyId);
               Map<DVPmsData, Object> data2 = null;
               try
               {
                  int oldKey = dvPmsDatabase.getKeyId(
                           this.data.get(DVPmsData.oldRoom).toString());
                  data2 = dvPmsDatabase.getDataByGuestId(
                           this.data.get(DVPmsData.guestId).toString(), oldKey);
                  data2.put(DVPmsData.keyId,
                           data.get(DVPmsData.keyId).toString());

               }
               catch (Exception e)
               {
                  dvLogger.error("Error in getting previous guest details  ",
                           e);
               }
               try
               {
                  if (null == data2.get(DVPmsData.guestId))
                  {
                     dvLogger.info(
                              "Setting guest details to current one in absence of value ");
                     data2 = data;
                  }
               }
               catch (Exception e)
               {
                  dvLogger.error("Error in getting guest details ", e);
               }
               data2 = initializeByBlank(data2);
               dvLogger.info("Data from DB :  " + data2.toString() + " "
                        + ", data from PMS :  " + data.toString());
               data2.put(DVPmsData.guestId,
                        data.get(DVPmsData.guestId).toString());
               data2.put(DVPmsData.guestType,
                        data.get(DVPmsData.guestType).toString());
               data2.put(DVPmsData.keyId, data.get(DVPmsData.keyId).toString());
               data2 = updateDataByNewData(data2, data);

               processCheckinEvent();

               /*
                * pmsiGuestID = dvPmsDatabase.insertUpdateGuestDetails(data2,
                * keyId); dvPmsDatabase.UpdateExistingGuestDetails(data, keyId);
                * data = dvPmsDatabase.getDataByPmsiGuestId(pmsiGuestID);
                * 
                * 
                * dvLogger.info("UPDATED DATA " + data.toString());
                * 
                *//**
                  * Update Guest preference in data
                  *//*
                    * getGuestPreference();
                    * 
                    * dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkin.
                    * toString(), DVPmsiStatus.PENDING_CHECKIN.toString(),
                    * keyId, this.data.get(DVPmsData.guestType).toString(),
                    * pmsiGuestID); dvLogger.analytics( "operation::" +
                    * DVAnalyticsEventType.roomChangeCheckin, "details::" +
                    * convertObjectToJsonString(this.data));
                    * sendGuestCheckinCheckoutEvent(InformationUpdate.checkin.
                    * name()); DVSendCheckin checkin = new
                    * DVSendCheckin(dvSettings, keyId, hotelId, pmsiGuestID,
                    * dvPmsDatabase, data, InRoomDevices, XplayerUi,
                    * communicationTokenManager, NonDvcInRoomDevices);
                    * checkin.sendCheckinToDevices();
                    */

            }
            else if (dvEvent
                     .getFeatureEventType() == FeatureEventType.PMSI_ROOMCHANGE_CHECKOUT_EVENT)
            {
               Thread.currentThread()
                        .setName(FeatureEventType.PMSI_ROOMCHANGE_CHECKOUT_EVENT
                                 + " KEY: " + keyId);
               processCheckoutEvent();
            }
            else if (dvEvent
                     .getFeatureEventType() == FeatureEventType.PMSI_SAFE_CHECKIN_EVENT)
            {
               Thread.currentThread()
                        .setName(FeatureEventType.PMSI_SAFE_CHECKIN_EVENT
                                 + " KEY: " + keyId);
               processSafeCheckin();
            }
            else if (dvEvent
                     .getFeatureEventType() == FeatureEventType.PMSI_SAFE_CHECKOUT_EVENT)
            {
               Thread.currentThread()
                        .setName(FeatureEventType.PMSI_SAFE_CHECKOUT_EVENT
                                 + " KEY: " + keyId);
               processSafeCheckout();
            }
         }
         else
         {
            dvLogger.info(" Room " + DVPmsData.keyId.toString()
                     + " does not exist in digivalet database ");
         }

         Thread.sleep(safeEventTimeForKey);
         dvLogger.info("Event Processed "
                  + this.dvEvent.getFeatureEventType().toString() + "  For Key "
                  + this.data.get(DVPmsData.keyId).toString());
      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing checkin checkout event ", e);
      }

   }



   private void processSafeCheckin()
   {
      try
      {
         boolean sendGuestInformationUpdate = false;
         String guestType = data.get(DVPmsData.guestType).toString();
         if (DVPmsGuestTypes.primary.toString().equalsIgnoreCase(guestType))
         {
            pmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                     data.get(DVPmsData.guestType).toString());
            int tempPmsiId = dvPmsDatabase.getPmsiGuestId(
                     data.get(DVPmsData.guestId).toString(), keyId);
            if (pmsiGuestID == 0 && data.get(DVPmsData.guestType).toString()
                     .equalsIgnoreCase(DVPmsGuestTypes.primary.toString()))
            {
               dvLogger.info(
                        "Key is not checked In Procced for normal primary checkin  ");
            }
            else if (pmsiGuestID != 0 && pmsiGuestID != tempPmsiId)
            {
               dvLogger.info(
                        "Removing Previous guests as it looks to be previous guests are not checkout out yet ");

               dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                        DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                        DVPmsiGuestTypes.primary.toString(), tempPmsiId);
               dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                        DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                        DVPmsiGuestTypes.secondary.toString(), pmsiGuestID);

               dvPmsDatabase.deleteGuestId(keyId,
                        DVPmsiGuestTypes.primary.toString());
               dvPmsDatabase.deleteGuestId(keyId,
                        DVPmsiGuestTypes.secondary.toString());
            }
            else if (pmsiGuestID == tempPmsiId)
            {

               dvLogger.info(
                        " Same primary guest is there so will check for guest information update  ");

               sendGuestInformationUpdate = true;
            }

         }
         else if (DVPmsGuestTypes.secondary.toString()
                  .equalsIgnoreCase(guestType))
         {
            pmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                     data.get(DVPmsData.guestType).toString());

            if (dvPmsDatabase.checkKeyPmsiCheckedInStatus(keyId,
                     DVPmsGuestTypes.secondary.toString()))
            {
               int tempPmsiId = dvPmsDatabase.getPmsiGuestId(
                        data.get(DVPmsData.guestId).toString(), keyId);

               if (pmsiGuestID != 0 && pmsiGuestID != tempPmsiId)
               {
                  dvLogger.info(
                           "Room is already checked in with secondary guest will remove it and update with new ");
                  pmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                           DVPmsiGuestTypes.secondary.toString());
                  dvPmsDatabase.updateKeyStatus(
                           DVPmsiStatus.checkout.toString(),
                           DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                           DVPmsiGuestTypes.secondary.toString(), pmsiGuestID);

                  dvPmsDatabase.deleteGuestId(keyId,
                           DVPmsiGuestTypes.secondary.toString());
               }
               else if (pmsiGuestID == tempPmsiId)
               {
                  sendGuestInformationUpdate = true;
                  dvLogger.info(
                           " Same secondary guest is there so will check for guest information update  ");
               }
            }
            else
            {
               dvLogger.info("Normal secondary Safe checkin ");
            }

         }
         dataManuplation();
         if (!sendGuestInformationUpdate)
         {
            removeMovieDataOnCheckin();
            getGuestPreference();

            dvLogger.analytics("operation::" + DVAnalyticsEventType.checkin,
                     "details::" + convertObjectToJsonString(this.data));
            pmsiGuestID = dvPmsDatabase.insertUpdateGuestDetails(data, keyId);
            dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkin.toString(),
                     DVPmsiStatus.PENDING_CHECKIN.toString(), keyId,
                     this.data.get(DVPmsData.guestType).toString(),
                     pmsiGuestID);
            sendGuestCheckinCheckoutEvent(InformationUpdate.checkin.name());
            DVSendCheckin checkin = new DVSendCheckin(dvSettings, keyId,
                     hotelId, pmsiGuestID, dvPmsDatabase, data, InRoomDevices,
                     XplayerUi, communicationTokenManager, NonDvcInRoomDevices);
            checkin.sendCheckinToDevices();
         }
         else
         {
            getGuestPreference();
            if (dvPmsDatabase.UpdateGuestDetails(data, keyId))
            {
               pmsiGuestID = dvPmsDatabase.getPmsiGuestId(
                        data.get(DVPmsData.guestId).toString(), keyId);
               dvLogger.analytics(
                        "operation::"
                                 + DVAnalyticsEventType.guestInformationUpdate,
                        "details::" + convertObjectToJsonString(this.data));
               DVSendGuestInformationUpdate guestInformationUpdate =
                        new DVSendGuestInformationUpdate(dvSettings, keyId,
                                 hotelId, pmsiGuestID, dvPmsDatabase, data,
                                 InRoomDevices, XplayerUi,
                                 communicationTokenManager,
                                 NonDvcInRoomDevices);
               guestInformationUpdate.sendGuestInfoUpdateToDevices();
               sendGuestCheckinCheckoutEvent(
                        InformationUpdate.guestInformationUpdate.name());
            }
            else
            {
               dvLogger.info("No Guest information change ");
            }

         }


      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing safe checkin ", e);
      }
   }


   private void deleteGuestPreference()
   {
      try
      {
         if (null != data.get(DVPmsData.guestType)
                  && DVPmsGuestTypes.primary.toString().equalsIgnoreCase(
                           data.get(DVPmsData.guestType).toString()))
         {
            dvPmsDatabase.deleteGuestPreference(keyId);
         }

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void processCheckoutEvent()
   {
      try
      {
         popuplateGuestID();
         getUpdatedGuestType();
         deleteGuestPreference();
         boolean ignoreCheckout = false;
         boolean checkoutWholeKey = false;
         if (pmsiGuestID == 0)
         {
            dvLogger.info(
                     "Guest doesn't exist in digivalet db will check if this the case with missing checkout/checkin ");
            pmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                     data.get(DVPmsData.guestType).toString());
            if (pmsiGuestID == 0 && data.get(DVPmsData.guestType).toString()
                     .equalsIgnoreCase(DVPmsGuestTypes.primary.toString()))
            {
               dvLogger.info("Key is not checked In ");
               ignoreCheckout = true;// Key is not checked in not need to
                                     // check for secondary guest
            }
            else if (pmsiGuestID != 0 && data.get(DVPmsData.guestType)
                     .toString()
                     .equalsIgnoreCase(DVPmsGuestTypes.primary.toString()))
            {
               dvLogger.info(
                        "Checkout whole key as primary guest is checked in and we recevied the checkout ");
               checkoutWholeKey = true;
            }
         }

         if (!checkoutWholeKey && !ignoreCheckout)
         {
            dvLogger.analytics("operation::" + DVAnalyticsEventType.checkout,
                     "details::" + convertObjectToJsonString(this.data));
            dvLogger.info(
                     " pmsiGuestID: " + pmsiGuestID + " for checkout event ");
            dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                     DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                     this.data.get(DVPmsData.guestType).toString(),
                     pmsiGuestID);
            removeGuestDetails(keyId);
            sendGuestCheckinCheckoutEvent(InformationUpdate.checkout.name());
            DVSendCheckout sendCheckout = new DVSendCheckout(dvSettings, keyId,
                     hotelId, pmsiGuestID, dvPmsDatabase, data, InRoomDevices,
                     XplayerUi, communicationTokenManager, NonDvcInRoomDevices);
            sendCheckout.sendCheckoutToDevices();
         }
         else if (checkoutWholeKey)
         {
            dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                     DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                     DVPmsiGuestTypes.primary.toString(), pmsiGuestID);
            int spmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                     DVPmsiGuestTypes.secondary.toString());
            dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                     DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                     DVPmsiGuestTypes.secondary.toString(), spmsiGuestID);

            dvPmsDatabase.deleteGuestId(keyId,
                     DVPmsiGuestTypes.primary.toString());
            dvPmsDatabase.deleteGuestId(keyId,
                     DVPmsiGuestTypes.secondary.toString());
            data = dvPmsDatabase
                     .getDataByGuestId(
                              dvPmsDatabase.getGuestIdByKey(keyId,
                                       DVPmsiGuestTypes.secondary.toString()),
                              keyId);
            sendGuestCheckinCheckoutEvent(InformationUpdate.checkout.name());
            DVSendCheckout sendCheckout = new DVSendCheckout(dvSettings, keyId,
                     hotelId, spmsiGuestID, dvPmsDatabase, data, InRoomDevices,
                     XplayerUi, communicationTokenManager, NonDvcInRoomDevices);
            sendCheckout.sendCheckoutToDevices();

            data = dvPmsDatabase
                     .getDataByGuestId(
                              dvPmsDatabase.getGuestIdByKey(keyId,
                                       DVPmsiGuestTypes.primary.toString()),
                              keyId);
            sendCheckout = new DVSendCheckout(dvSettings, keyId, hotelId,
                     spmsiGuestID, dvPmsDatabase, data, InRoomDevices,
                     XplayerUi, communicationTokenManager, NonDvcInRoomDevices);
            sendCheckout.sendCheckoutToDevices();

         }
         removeMovieDataOnCheckout();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing chekout event ", e);
      }
   }

   public void processCheckinEvent()
   {
      try
      {

         checkAndUpdateGuestTypeToPrimary();
         String guestType = data.get(DVPmsData.guestType).toString();
         if (DVPmsGuestTypes.primary.toString().equalsIgnoreCase(guestType))
         {
            pmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                     data.get(DVPmsData.guestType).toString());
            int tempPmsiId = dvPmsDatabase.getPmsiGuestId(
                     data.get(DVPmsData.guestId).toString(), keyId);
            if (pmsiGuestID == 0 && data.get(DVPmsData.guestType).toString()
                     .equalsIgnoreCase(DVPmsGuestTypes.primary.toString()))
            {
               dvLogger.info(
                        "Key is not checked In Procced for normal primary checkin  ");
            }
            else if (pmsiGuestID != 0 && pmsiGuestID != tempPmsiId)
            {
               dvLogger.info(
                        "Removing Previous guests as it looks to be previous guests are not checkout out yet ");

               dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                        DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                        DVPmsiGuestTypes.primary.toString(), tempPmsiId);
               dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                        DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                        DVPmsiGuestTypes.secondary.toString(), pmsiGuestID);

               dvPmsDatabase.deleteGuestId(keyId,
                        DVPmsiGuestTypes.primary.toString());
               dvPmsDatabase.deleteGuestId(keyId,
                        DVPmsiGuestTypes.secondary.toString());

               dvPmsDatabase.updateKeyStatusDigivaletStatus(
                        (DVPmsiGuestTypes.secondary).toString(), keyId,
                        dvPmsDatabase.getMasterStatusId(
                                 DVPmsiStatus.PENDING_CHECKOUT.toString()));
            }

         }
         else if (DVPmsGuestTypes.secondary.toString()
                  .equalsIgnoreCase(guestType))
         {
            pmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                     data.get(DVPmsData.guestType).toString());

            if (dvPmsDatabase.checkKeyPmsiCheckedInStatus(keyId,
                     DVPmsGuestTypes.secondary.toString()))
            {
               dvLogger.info(
                        "Room is already checked in with secondary guest will remove it and update with new ");
               pmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                        DVPmsiGuestTypes.secondary.toString());
               dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                        DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                        DVPmsiGuestTypes.secondary.toString(), pmsiGuestID);

               dvPmsDatabase.deleteGuestId(keyId,
                        DVPmsiGuestTypes.secondary.toString());


            }
            else
            {
               dvLogger.info("Normal secondary checkin ");
            }

         }

         dataManuplation();
         removeMovieDataOnCheckin();
         getGuestPreference();

         dvLogger.analytics("operation::" + DVAnalyticsEventType.checkin,
                  "details::" + convertObjectToJsonString(this.data));
         pmsiGuestID = dvPmsDatabase.insertUpdateGuestDetails(data, keyId);
         dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkin.toString(),
                  DVPmsiStatus.PENDING_CHECKIN.toString(), keyId,
                  this.data.get(DVPmsData.guestType).toString(), pmsiGuestID);
         sendGuestCheckinCheckoutEvent(InformationUpdate.checkin.name());
         DVSendCheckin checkin = new DVSendCheckin(dvSettings, keyId, hotelId,
                  pmsiGuestID, dvPmsDatabase, data, InRoomDevices, XplayerUi,
                  communicationTokenManager, NonDvcInRoomDevices);
         checkin.sendCheckinToDevices();

      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing chekin event ", e);
      }
   }


   private void processSafeCheckout()
   {
      try
      {
         deleteGuestPreference();
         {
            boolean isSecondaryCheckedIn =
                     dvPmsDatabase.checkKeyPmsiCheckedInStatus(keyId,
                              DVPmsiGuestTypes.secondary.toString());
            dvLogger.info(" is Secondary Checked In: " + isSecondaryCheckedIn);
            if (isSecondaryCheckedIn)
            {
               pmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                        DVPmsiGuestTypes.secondary.toString());

               data = dvPmsDatabase
                        .getDataByGuestId(
                                 dvPmsDatabase
                                          .getGuestIdByKey(keyId,
                                                   DVPmsiGuestTypes.secondary
                                                            .toString()),
                                 keyId);
               dvLogger.info("Secondary safe checkout pmsi guest id" + pmsiGuestID
                        + "  data: " + data.toString());
               dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                        DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                        DVPmsiGuestTypes.secondary.toString(), pmsiGuestID);
               dvPmsDatabase.deleteGuestId(keyId,
                        DVPmsiGuestTypes.secondary.toString());
               DVSendCheckout sendCheckout = new DVSendCheckout(dvSettings,
                        keyId, hotelId, pmsiGuestID, dvPmsDatabase, data,
                        InRoomDevices, XplayerUi, communicationTokenManager,
                        NonDvcInRoomDevices);
               sendCheckout.sendCheckoutToDevices();
            }

            pmsiGuestID = dvPmsDatabase.getPmsiGuestIdFromKeyStatus(keyId,
                     data.get(DVPmsData.guestType).toString());
            data = dvPmsDatabase
                     .getDataByGuestId(
                              dvPmsDatabase.getGuestIdByKey(keyId,
                                       DVPmsiGuestTypes.primary.toString()),
                              keyId);
            dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString(),
                     DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                     DVPmsiGuestTypes.primary.toString(), pmsiGuestID);
            dvPmsDatabase.deleteGuestId(keyId,
                     DVPmsiGuestTypes.primary.toString());
            dvLogger.info("Secondary safe checkout pmsi guest id " + pmsiGuestID + "  data: "
                     + data.toString());
            DVSendCheckout sendCheckout = new DVSendCheckout(dvSettings, keyId,
                     hotelId, pmsiGuestID, dvPmsDatabase, data, InRoomDevices,
                     XplayerUi, communicationTokenManager, NonDvcInRoomDevices);
            sendCheckout.sendCheckoutToDevices();
         }
         removeMovieDataOnCheckout();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing chekout event ", e);
      }

   }

   private Map<DVPmsData, Object> initializeByBlank(
            Map<DVPmsData, Object> guestData)
   {
      Map<DVPmsData, Object> updatedGuestDate = guestData;
      try
      {
         for (Map.Entry<DVPmsData, Object> entry : guestData.entrySet())
         {
            DVPmsData key = entry.getKey();
            Object value = entry.getValue();

            if (null == value)
            {
               updatedGuestDate.put(key, "");
            }
         }
         DVPmsData[] yourEnums = DVPmsData.values();
         for (int i = 0; i < yourEnums.length; i++)
         {
            if (!updatedGuestDate.containsKey(yourEnums[i]))
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
      try
      {
         String guestCount = guestData.get(DVPmsData.guestCount).toString();
         if (null == guestCount || "".equalsIgnoreCase(guestCount))
         {
            guestData.put(DVPmsData.guestCount, "1");
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Erorr in setting default guest count", e);
      }
      return guestData;
   }



   private Map<DVPmsData, Object> updateDataByNewData(
            Map<DVPmsData, Object> data2, Map<DVPmsData, Object> data)
   {
      Map<DVPmsData, Object> updatedGuestData = data2;
      try
      {
         for (Map.Entry<DVPmsData, Object> entry : data.entrySet())
         {
            DVPmsData key = entry.getKey();
            Object value = entry.getValue();

            if (null != value && !"".equalsIgnoreCase(value.toString()))
            {
               updatedGuestData.put(key, data.get(key));
            }
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating new guest data from old data ", e);
         return updatedGuestData;
      }

      return updatedGuestData;
   }


   public void sendGuestCheckinCheckoutEvent(String operation)
   {

      try
      {

         dvLogger.info("In side new method check it");
         CheckinCheckoutData detail = new CheckinCheckoutData();
         try
         {
            detail.setGuestName(this.data.get(DVPmsData.guestName).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setGuestTitle(
                     this.data.get(DVPmsData.guestTitle).toString());
         }
         catch (Exception e)
         {
            detail.setGuestTitle("");
            e.printStackTrace();
         }
         try
         {
            detail.setGuestFirstName(
                     this.data.get(DVPmsData.guestFirstName).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setGuestLastName(
                     this.data.get(DVPmsData.guestLastName).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setGuestFullName(
                     this.data.get(DVPmsData.guestFullName).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }

         try
         {
            detail.setKeyId(this.data.get(DVPmsData.keyId).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setRemoteCheckout(
                     this.data.get(DVPmsData.remoteCheckout).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setSafeFlag(this.data.get(DVPmsData.safeFlag).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setRevisitFlag(
                     this.data.get(DVPmsData.revisitFlag).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setTvRights(this.data.get(DVPmsData.tvRights).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setVideoRights(
                     this.data.get(DVPmsData.videoRights).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setVipStatus(this.data.get(DVPmsData.vipStatus).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setAlternateName(
                     this.data.get(DVPmsData.alternateName).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setIncognitoName(
                     this.data.get(DVPmsData.incognitoName).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }

         try
         {
            detail.setGuestLanguage(
                     this.data.get(DVPmsData.guestLanguage).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setGuestId(this.data.get(DVPmsData.guestId).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setReservationId(
                     this.data.get(DVPmsData.reservationId).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setEmailId(this.data.get(DVPmsData.emailId).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setPhoneNumber(
                     this.data.get(DVPmsData.phoneNumber).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setGroupCode(this.data.get(DVPmsData.groupCode).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setUniqueId(this.data.get(DVPmsData.uniqueId).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setGuestType(this.data.get(DVPmsData.guestType).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setDateOfBirth(
                     this.data.get(DVPmsData.dateOfBirth).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setNationality(
                     this.data.get(DVPmsData.nationality).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setPreviousVisitDate(
                     this.data.get(DVPmsData.previousVisitDate).toString());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {
            detail.setDepartureDate(
                     this.data.get(DVPmsData.departureDate).toString());
            String departureDate =
                     dvPmsDatabase.getGuestDepartureDate(detail.getGuestId());
            detail.setDepartureDate(departureDate);

         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         try
         {

            detail.setArrivalDate(
                     this.data.get(DVPmsData.arrivalDate).toString());
            String arrivalDate =
                     dvPmsDatabase.getGuestArrivalDate(detail.getGuestId());
            detail.setArrivalDate(arrivalDate);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }

         SendCheckinCheckoutInFormationUpdateEvent SendCheckinCheckoutInFormationUpdateEvent =
                  new SendCheckinCheckoutInFormationUpdateEvent(hotelId,
                           detail.getGuestType(), detail.getGuestId(),
                           dvPmsDatabase.getDigivaletRoomNumber(keyId), detail,
                           operation);


         SendCheckinCheckoutInFormationUpdateEvent.start();
         
         
         ObjectNode root  = null;
         try
         {
            
            String roomNumber = this.data.get(DVPmsData.keyId).toString();
            String hotelCode = dvSettings.getHotelId();
            int keyId =   dvPmsDatabase.getKeyIdFromRoomNumber(roomNumber);
            int deviceId = dvPmsDatabase.getDeviceIdByDvcKey(keyId);
            
            ObjectMapper objectMapper = new ObjectMapper();
            root = objectMapper.convertValue(detail, ObjectNode.class);
            
            ((ObjectNode) root).put(DVNotificationManager.requiredKeyRoomNumber,
                     roomNumber); // add new    
            ((ObjectNode) root).put(DVNotificationManager.requiredKeyHotelCode,
                     hotelCode); // add new  
            ((ObjectNode) root).put(DVNotificationManager.requiredKeyDeviceId,
                     deviceId); // add new  
            
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while setting room number. Exception:", e);
         }
         
         if (operation.equalsIgnoreCase("guestInformationUpdate"))
         {           
            DVNotificationManager.postRequestToNotificationEngine(
                     DVPMSIConstants.GUEST_INFORMATION_UPDATE_SUCCESS, root);
         }
         else if (operation.equalsIgnoreCase("checkin"))
         {
            DVNotificationManager.postRequestToNotificationEngine(
                     DVPMSIConstants.CHECKIN_SUCCESS, root);
         }
         else
         {
            DVNotificationManager.postRequestToNotificationEngine(
                     DVPMSIConstants.CHECKOUT_SUCCESS, root);
         }

      }


      catch (Exception e)
      {
         dvLogger.error("Error in checkin ", e);
      }

   }


   private void removeMovieDataOnCheckout()
   {
      try
      {
         if (this.data.get(DVPmsData.guestType).toString()
                  .equalsIgnoreCase(DVPmsGuestTypes.primary.toString()))
         {
            dvPmsDatabase.updateMovieRoomStatusOnCheckinCheckout(keyId);
            SendButlerRequestOnCheckinOut dvSendButlerRequestOnCheckinOut =
                     new SendButlerRequestOnCheckinOut(
                              dvPmsDatabase.getDigivaletRoomNumber(keyId),
                              this.data.get(DVPmsData.guestId).toString(),
                              "checkOut");
            ArrayList<Integer> Dvcs = dvPmsDatabase.getAllDvcByKey(keyId);
            for (int Dvc : Dvcs)
            {
               Thread thread = new Thread(
                        new DVSendPendingMovieToDevices(Dvc, dvPmsDatabase,
                                 dvSettings, null, communicationTokenManager));
               thread.start();
            }


            dvSendButlerRequestOnCheckinOut.start();
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in removing movie details on checkin ", e);
      }
   }

   private void removeMovieDataOnCheckin()
   {
      try
      {
         if (this.data.get(DVPmsData.guestType).toString()
                  .equalsIgnoreCase(DVPmsGuestTypes.primary.toString()))
         {

            if (null != this.data.get(DVPmsData.safeFlag).toString())
            {
               if (data.get(DVPmsData.safeFlag).toString()
                        .equalsIgnoreCase("false"))
               {
                  dvPmsDatabase.updateMovieRoomStatusOnCheckinCheckout(keyId);
                  SendButlerRequestOnCheckinOut dvSendButlerRequestOnCheckinOut =
                           new SendButlerRequestOnCheckinOut(
                                    dvPmsDatabase.getDigivaletRoomNumber(keyId),
                                    this.data.get(DVPmsData.guestId).toString(),
                                    "checkIn");
                  dvSendButlerRequestOnCheckinOut.start();
                  DVDashboardCheckinCheckoutEvent dvDashboardCheckinCheckoutEvent =
                           new DVDashboardCheckinCheckoutEvent(data, "checkIn",
                                    keyId);
                  dvDashboardCheckinCheckoutEvent.start();
               }
               else
               {
                  dvLogger.info(
                           "Won't delete movie since this is safe checkin");
               }
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in removing movie details on checkout ", e);
      }
   }

   /*
    * private void markPreviousGuestCheckout() { try {
    * dvPmsDatabase.deleteGuestId(keyId,
    * this.data.get(DVPmsData.guestType).toString()); } catch (Exception e) {
    * dvLogger.error("Error in marking previous guest checked out ", e); }
    * 
    * }
    */
   /*
    * private void checkAndUpdateGuestForCheckin() { try { if
    * (this.data.get(DVPmsData.guestType).toString()
    * .equalsIgnoreCase(DVPmsGuestTypes.secondary.toString())) { if
    * (dvPmsDatabase.isRoomCheckedIn(keyId)) { data.put(DVPmsData.guestType,
    * DVPmsGuestTypes.secondary.toString()); dvLogger.info(
    * "Updating the guest type to secondary "); } else {
    * data.put(DVPmsData.guestType, DVPmsGuestTypes.primary.toString());
    * dvLogger.info("Updating the guest type to primary "); } } } catch
    * (Exception e) { dvLogger.info("Error in updating guest type "); } }
    */

   private void checkAndUpdateGuestTypeToPrimary()
   {
      try
      {
         if (this.data.get(DVPmsData.guestType).toString()
                  .equalsIgnoreCase(DVPmsGuestTypes.secondary.toString()))
         {
            if (!dvPmsDatabase.isRoomCheckedIn(keyId))
            {
               data.put(DVPmsData.guestType,
                        DVPmsGuestTypes.primary.toString());
               dvLogger.info(
                        "Updating the guest type to primary since room is not checked in");
            }

         }
      }
      catch (Exception e)
      {
         dvLogger.info("Error in updating guest type ");
      }
   }

   private void getUpdatedGuestType()
   {
      try
      {
         int guestId = dvPmsDatabase.getPmsiGuestIdWithKey(
                  this.data.get(DVPmsData.guestId).toString(), keyId);
         String guestType = dvPmsDatabase.getGuestType(guestId, keyId);
         if (!guestType.equalsIgnoreCase(""))
         {
            dvLogger.info("updating Guest type to " + guestType);
            data.put(DVPmsData.guestType, guestType);
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating guest Type ", e);
      }
   }

   private void checkAndUpdateGuestType()
   {
      try
      {
         int guestId = dvPmsDatabase
                  .getPmsiGuestId(this.data.get(DVPmsData.guestId).toString());
         String guestType = dvPmsDatabase.getGuestType(guestId, keyId);
         if (!guestType.equalsIgnoreCase(""))
         {
            dvLogger.info("updating Guest type to " + guestType);
            data.put(DVPmsData.guestType, guestType);
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating guest Type ", e);
      }
   }

   private void popuplateGuestID()
   {
      pmsiGuestID = dvPmsDatabase.getPmsiGuestIdWithKey(
               this.data.get(DVPmsData.guestId).toString(), keyId);

   }

   private void populateXplayerUi()
   {
      XplayerUi = dvPmsDatabase.populateDevices(keyId,
               DVDeviceTypes.tvui.toString(), 1);
   }

   private void populateInRoomDevices()
   {
      try
      {
         InRoomDevices = dvPmsDatabase.populateDevices(keyId,
                  DVDeviceTypes.ipad.toString(), 1);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Room Devices ", e);
      }
   }

   private void populateNonDvcInRoomDevices()
   {
      try
      {
         NonDvcInRoomDevices = dvPmsDatabase.populateDevices(keyId,
                  DVDeviceTypes.ipad.toString(), 0);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Room Devices ", e);
      }
   }


   private void dataManuplation()
   {
      try
      {
         data.put(DVPmsData.guestLanguage, dvPmsDatabase.getLanguageCode(
                  this.data.get(DVPmsData.guestLanguage).toString()));
         if (null != dvSettings.getGuestNameFormat()
                  && !dvSettings.getGuestNameFormat().equalsIgnoreCase(""))
         {
            String format = dvSettings.getGuestNameFormat();
            String name = "";
            try
            {
               for (int i = 0; i < format.split("\\,").length; i++)
               {
                  try
                  {
                     if (this.data.containsKey(
                              DVPmsData.valueOf(format.split("\\,")[i])))
                     {
                        if (null != this.data
                                 .get(DVPmsData.valueOf(format.split("\\,")[i]))
                                 .toString())
                        {
                           name = name +this.data
                                    .get(DVPmsData
                                             .valueOf(format.split("\\,")[i]))
                                    .toString() + " ";
                        }
                     }
                     else
                     {
                        dvLogger.info("Key not found for pms Data ");
                     }
                  }
                  catch (Exception e)
                  {
                     e.printStackTrace();
                  }

               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }


            dvLogger.trace("message::Updating guest name",
                     "hasGuestNameUpdate::" + hasGuestNameUpdate, "guestName::"
                              + this.data.get(DVPmsData.guestName).toString());
            if (hasGuestNameUpdate)
            {
               if (null != name && !name.equalsIgnoreCase(""))
               {
                  dvLogger.info("Updating guest name to " + name);
                  data.put(DVPmsData.guestName, name.trim());
               }
            }
            else
            {
               data.put(DVPmsData.guestName,
                        this.data.get(DVPmsData.guestName).toString());
            }


            if (null == data.get(DVPmsData.guestName))
            {
               data.put(DVPmsData.guestName, "");
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in Data Manuplation ", e);
      }

   }



   public void removeGuestDetails(int keyId)
   {
      try
      {
         if (null == this.data.get(DVPmsData.guestId) || "".equalsIgnoreCase(
                  this.data.get(DVPmsData.guestId).toString()))
         {
            dvPmsDatabase.deleteGuestId(keyId,
                     this.data.get(DVPmsData.guestType).toString());// TO handle
                                                                    // case when
                                                                    // we do not
                                                                    // rec guest
                                                                    // id
         }
         else
         {
            dvPmsDatabase.deleteGuestIdOnCheckout(
                     this.data.get(DVPmsData.guestId).toString(), keyId);
         }


      }
      catch (Exception e)
      {
         dvLogger.error("Error in removing guest details ", e);
      }
   }


   public enum InformationUpdate
   {
      checkin,
      checkout,
      guestInformationUpdate,
      novalue;
      public static InformationUpdate fromString(String Str)
      {
         try
         {
            return valueOf(Str);
         }
         catch (Exception ex)
         {
            return novalue;
         }
      }
   }

   private void getGuestPreference()
   {

      DVGuestPreferenceModel preferenceData = new DVGuestPreferenceModel();

      if (null != data.get(DVPmsData.reservationId)
               && !"".equalsIgnoreCase(
                        data.get(DVPmsData.reservationId).toString())
               && !"null".equalsIgnoreCase(
                        data.get(DVPmsData.reservationId).toString())
               && dvPmsDatabase.checkPreferenceExistByReservation(
                        data.get(DVPmsData.reservationId).toString()))
      {

         preferenceData = dvPmsDatabase.getGuestPreferenceDataByReservation(
                  data.get(DVPmsData.reservationId).toString());
      }
      else if (dvPmsDatabase.checkPreferenceExistByGuestId(
               data.get(DVPmsData.guestId).toString()))
      {
         preferenceData = dvPmsDatabase.getGuestPreferenceDataByGuestId(
                  data.get(DVPmsData.guestId).toString());
      }
      else if (dvPmsDatabase.checkPreferenceExistByKeyId(keyId))
      {
         preferenceData = dvPmsDatabase.getGuestPreferenceDataByKeyId(keyId);
      }

      try
      {
         if (null != preferenceData.getTemperature()
                  && !"null".equalsIgnoreCase(preferenceData.getTemperature())
                  && !"".equalsIgnoreCase(preferenceData.getTemperature()))
         {
            data.put(DVPmsData.temperature, preferenceData.getTemperature());
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while setting preferredTemp\n", e);
      }

      try
      {
         if (null != preferenceData.getMoodId()
                  && !"null".equalsIgnoreCase(preferenceData.getMoodId())
                  && !"".equalsIgnoreCase(preferenceData.getMoodId()))
         {
            data.put(DVPmsData.welcomeMoodId, preferenceData.getMoodId());
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while setting welcome mood\n", e);
      }

      try
      {
         if (null != preferenceData.getFragrance()
                  && !"null".equalsIgnoreCase(preferenceData.getFragrance())
                  && !"".equalsIgnoreCase(preferenceData.getFragrance()))
         {
            data.put(DVPmsData.fragrance, preferenceData.getFragrance());
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while setting welcome mood\n", e);
      }

   }
}
