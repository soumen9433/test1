package com.digivalet.core;

import java.util.ArrayList;
import java.util.Map;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVDeviceTypes;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.events.DVSendPendingCheckin;
import com.digivalet.pmsi.events.DVSendPendingCheckout;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPendingCheckinCheckout implements Runnable
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVPmsDatabase dvPmsDatabase;
   private DVSettings dvSettings;
   private int keyId = 0;
   private String hotelId = "";
   private int pmsiGuestID = 0;
   private ArrayList<Integer> InRoomDevices = new ArrayList<Integer>();
   private ArrayList<Integer> XplayerUi = new ArrayList<Integer>();
   private ArrayList<Integer> NonDvcInRoomDevices = new ArrayList<Integer>();
   private String guestType;
   private int event;
   private int pendingKeyId;
   private Map<DVPmsData, Object> data;
   private ArrayList<Integer> dvPendingEventKeyIds;
   private final int safeEventTime = 60 * 1000;
   private DVKeyCommunicationTokenManager communicationTokenManager;

   public DVPendingCheckinCheckout(int pendingKeyId,
            DVPmsDatabase dvPmsDatabase, DVSettings dvSettings, int keyId,
            String guestType, int pmsiGuestID, int event,
            ArrayList<Integer> dvPendingEventKeyIds,
            DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.pendingKeyId = pendingKeyId;
      this.dvSettings = dvSettings;
      this.keyId = keyId;
      this.guestType = guestType;
      this.pmsiGuestID = pmsiGuestID;
      this.event = event;
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvPendingEventKeyIds = dvPendingEventKeyIds;
      this.communicationTokenManager = communicationTokenManager;
   }

   public void run()
   {
      try
      {
         dvLogger.info("keyId: " + keyId);
         dvLogger.info("guestType: " + guestType);
         dvLogger.info("pmsiGuestID: " + pmsiGuestID);
         dvLogger.info("event: " + event);
         if (keyId != 0)
         {
            populateInRoomDevices();
            populateXplayerUi();
            populateNonDvcInRoomDevices();
            data = dvPmsDatabase.getDataByPmsiGuestId(pmsiGuestID);
            dvLogger.info("Guest Data: " + data);
            if (event == dvPmsDatabase.getMasterStatusId(
                     DVPmsiStatus.PENDING_CHECKIN.toString()))
            {

               /*
                * dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkin.toString(),
                * DVPmsiStatus.PENDING_CHECKIN.toString(), keyId,
                * this.data.get(DVPmsData.guestType).toString(), pmsiGuestID);
                */

               DVSendPendingCheckin checkin = new DVSendPendingCheckin(
                        dvSettings, keyId, hotelId, pmsiGuestID, dvPmsDatabase,
                        data, InRoomDevices, XplayerUi,
                        communicationTokenManager,NonDvcInRoomDevices);
               checkin.sendCheckinToDevices();

            }
            else if (event == dvPmsDatabase.getMasterStatusId(
                     DVPmsiStatus.PENDING_CHECKOUT.toString()))
            {
               /*
                * dvPmsDatabase.updateKeyStatus(DVPmsiStatus.checkout.toString()
                * , DVPmsiStatus.PENDING_CHECKOUT.toString(), keyId,
                * this.data.get(DVPmsData.guestType).toString(), pmsiGuestID);
                */

               DVSendPendingCheckout sendCheckout = new DVSendPendingCheckout(
                        dvSettings, keyId, hotelId, pmsiGuestID, dvPmsDatabase,
                        data, InRoomDevices, XplayerUi,
                        communicationTokenManager,NonDvcInRoomDevices);
               sendCheckout.sendCheckoutToDevices();

            }
            else if (event == dvPmsDatabase.getMasterStatusId(
                     DVPmsiStatus.PENDING_GUEST_INFO_UPDATE.toString()))
            {
               dvLogger.info("DIGIVALET STATUS CAN NOT BE PENDING_GUEST_INFO_UPDATE ");
               DVSendPendingCheckin checkin = new DVSendPendingCheckin(
                        dvSettings, keyId, hotelId, pmsiGuestID, dvPmsDatabase,
                        data, InRoomDevices, XplayerUi,
                        communicationTokenManager,NonDvcInRoomDevices);
               checkin.sendCheckinToDevices();
            }
         }
         else
         {
            dvLogger.info(" Room " + keyId
                     + " does not exist in digivalet database ");
         }
         dvLogger.info("Event processing is done for " + this.pendingKeyId
                  + " will remove it after safe time " + safeEventTime);
         Thread.sleep(safeEventTime);
         dvLogger.info(" Removing Key  " + dvPendingEventKeyIds.toString()
                  + "  this.pendingKeyId " + this.pendingKeyId);
         dvPendingEventKeyIds.remove(Integer.valueOf(this.pendingKeyId));
      }
      catch (Exception e)
      {
         dvLogger.error("Error ", e);
      }
      Thread.currentThread().destroy();
   }

   private void populateXplayerUi()
   {
      XplayerUi = dvPmsDatabase.populateDevices(keyId, DVDeviceTypes.tvui.toString(),1);
   }

   private void populateInRoomDevices()
   {
      try
      {
         InRoomDevices = dvPmsDatabase.populateDevices(keyId, DVDeviceTypes.ipad.toString(),1);
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
         NonDvcInRoomDevices = dvPmsDatabase.populateDevices(keyId, DVDeviceTypes.ipad.toString(),0);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Room Devices ", e);
      }
   }
}
