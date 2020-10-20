package com.digivalet.pmsi.operaserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import com.digivalet.core.AlertState;
import com.digivalet.core.DVLogger;
import com.digivalet.movies.DVMovieEvent;
import com.digivalet.pms.guestpreference.model.DVGuestPreferenceModel;
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
import com.digivalet.pmsi.model.GuestDetails;
import com.digivalet.pmsi.model.MovieData;
import com.digivalet.pmsi.model.MovieDetails;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPmsOpera extends DVPms implements DVPmsEventNotifier
{
   private DVLogger dvLogger = DVLogger.getInstance();

   /**
    * <B>Description:</B> PmsKeyMappingDetails will contain the Mapping details
    * of PMS Key with Digivalet Key. For example:A0 in PMS will be unique Key in
    * Digivalet,It should be configured in xml as: A0-uniqueKey,A1-email
    * <ul>
    * <li>pmsSettings</li>
    * </ul>
    * 
    */
   HashMap<String, DVPmsData> PmsKeyMappingDetails =
            new HashMap<String, DVPmsData>();
   /**
    * <B>Description:</B> DefaultDataMap will contain the Mapping details of PMS
    * Key with its default value if not received from PMS. For example:if
    * tvRights are received empty by default we can mark them as TU or
    * reservationNumber as NA,It should be configured in xml as:
    * tvRights-TU,reservationNumber-NA
    * <ul>
    * <li>pmsSettings</li>
    * </ul>
    * 
    */
   HashMap<DVPmsData, String> DefaultDataMap = new HashMap<DVPmsData, String>();
   /**
    * <B>Description:</B> PmsValueMappingDetails will contain the Mapping
    * details of Digivalet value with PMS Key value pairs for example; if
    * tvRights value is received as TU we can map it in Digivalet as
    * unlimited,It should be configured in xml as:
    * tvRights:TU-Unlimited,tvRights:TN-No Adult
    * <ul>
    * <li>pmsSettings</li>
    * </ul>
    * 
    */

   private HashMap<String, String> ServiceIdData =
            new HashMap<String, String>();

   protected HashMap<HashMap<DVPmsData, String>, String> PmsValueMappingDetails =
            new HashMap<HashMap<DVPmsData, String>, String>();
   private DVSettings dvSettings;
   public DVPmsConnectionManager connectionManager;
   private DVPmsDatabase dvPmsDatabase;
   private boolean connectionStatus = false;
   private final int BILL_AMOUNT_FEEDBACK_DELAY = 1000;
   public String errorMessage = "";
   public int lsTimeout = 10 * 1000;
   public int laTimeout = 10 * 1000;
   public int socketTimeout = 10 * 60 * 1000;
   public boolean syncInProgress = false;
   public boolean isFirstLSReceived = false;
   public boolean isLAReceived = false;
   public boolean isPollCommandSend = false;
   public int LsCount = -1;
   public int LaCount = -1;
   public int pollCount = -1;
   public AlertState pmsAlertState = AlertState.Na;
   public Map<String, Integer> roomGuestCount = new HashMap<String, Integer>();
   public SortedMap<String, Map<DVPmsData, Object>> syncCheckinRecords =
            new TreeMap<String, Map<DVPmsData, Object>>();
   public List<Map<DVPmsData, Object>> syncCheckoutRecords =
            new ArrayList<Map<DVPmsData, Object>>();

   public DVPmsOpera(DVSettings dvSettings, DVPmsDatabase dvPmsDatabase)
   {
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      loadData();
      init();
   }

   private void loadData()
   {
      try
      {
         String KeyMappingData = dvSettings.getKeyMappingData();
         if (null != KeyMappingData && !KeyMappingData.equalsIgnoreCase("")
                  && !KeyMappingData.equalsIgnoreCase("na"))
         {
            for (int i = 0; i < KeyMappingData.split(",").length; i++)
            {
               String data = KeyMappingData.split(",")[i];
               String key = data.split("\\-")[0];
               String value = data.split("\\-")[1];
               PmsKeyMappingDetails.put(key, DVPmsData.valueOf(value));
               dvLogger.info("Updated Pmsi Key " + key + " with Digivalet Key "
                        + DVPmsData.valueOf(value));
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in loading Key Mapping data ", e);
      }

      try
      {
         String defaultData = dvSettings.getDefaultFeildData();
         if (null != defaultData && !defaultData.equalsIgnoreCase("")
                  && !defaultData.equalsIgnoreCase("na"))
         {
            for (int i = 0; i < defaultData.split(",").length; i++)
            {
               String data = defaultData.split(",")[i];
               String key = data.split("\\-")[0];
               String value = data.split("\\-")[1];
               DefaultDataMap.put(DVPmsData.valueOf(key), value);
               dvLogger.info("Confgiured default data " + DVPmsData.valueOf(key)
                        + " for PMS feild " + (value));
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in loading Default Feild data ", e);
      }

      try
      {
         String valueData = dvSettings.getValueMappingData();
         if (null != valueData && !valueData.equalsIgnoreCase("")
                  && !valueData.equalsIgnoreCase("na"))
         {
            for (int i = 0; i < valueData.split(",").length; i++)
            {
               String data = valueData.split(",")[i];
               String key = data.split("\\-")[0];

               String pmsKey = key.split("\\:")[0];
               String pmsValue = key.split("\\:")[1];

               String value = data.split("\\-")[1];
               HashMap<DVPmsData, String> pmsKeyValue =
                        new HashMap<DVPmsData, String>();
               pmsKeyValue.put(DVPmsData.valueOf(pmsKey), pmsValue);
               PmsValueMappingDetails.put(pmsKeyValue, value);
               dvLogger.info("Confgiured PMS Key Value data " + pmsKeyValue
                        + " for PMS Key Value " + value);
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in loading Value Mapping Data data ", e);
      }

      try
      {
         String serviceData = dvSettings.getServiceIdData();
         if (null != serviceData && !serviceData.equalsIgnoreCase("")
                  && !serviceData.equalsIgnoreCase("na"))
         {
            for (int i = 0; i < serviceData.split(",").length; i++)
            {
               String data = serviceData.split(",")[i];
               String key = data.split(":")[0];
               String value = data.split(":")[1];
               ServiceIdData.put(key, value);
               dvLogger.info("putting in service data " + key + " , " + value);
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in loading service data ", e);
      }
      try
      {
         lsTimeout=dvSettings.getLsTimeout();
         laTimeout=dvSettings.getLaTimeout();
         socketTimeout=dvSettings.getSocketTimeout();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in loading timeouts ", e);
      }
   }

   private void init()
   {
      try
      {
         dvLogger.info("Initializing Opera server ");
         DVEncryptDecrypt dvEncryptDecrypt=new DVEncryptDecrypt();
         connectionManager =
                  new DVPmsConnectionManager(dvSettings, this, dvPmsDatabase,dvEncryptDecrypt);
         connectionManager.start();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in Init DVPmsOpera ", e);
      }
   }

   @Override
   public DVResult getBill(String roomId, String guestId)
   {
      try
      {
         String roomNumber = dvPmsDatabase.getPmsiRoomId(roomId);
         connectionManager
                  .writeSignal("XR|RN" + roomNumber + "|G#" + guestId + "||");
      }
      catch (Exception e)
      {
         dvLogger.error("Error in writing to pms", e);
      }

      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Sync Data Send TO PMS ");
   }

   @Override
   public DVResult getMessage(String roomId, String guestId)
   {
      // TODO Auto-generated method stub
      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Data Send TO PMS ");
   }

   @Override
   public DVResult synchronize()
   {
      Date now = new Date();
      SimpleDateFormat formatPattern = new SimpleDateFormat("yyMMdd");
      String date = formatPattern.format(now);
      formatPattern = new SimpleDateFormat("HHmmss");
      String time = formatPattern.format(now);
      String dateTime = "DA" + date + "|TI" + time + "|";
      connectionManager.writeSignal("DR|" + dateTime);
      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Sync Data Send TO PMS ");
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
   public DVResult setServiceState(String roomId, String serviceId,
            boolean state)
   {
      try
      {
         // "RE|RN" + intRoomId + "|RS" +
         // PmsClient.SrvcPriMap.get("serviceon").trim()+"|"
         if (ServiceIdData.containsKey(serviceId + "|" + state))
         {
            String serviceid = ServiceIdData.get(serviceId + "|" + state);
            connectionManager
                     .writeSignal("RE|RN" + roomId + "|RS" + serviceid + "|");
         }
         else
         {
            dvLogger.info("Services are not configured");
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending service ID data to PMS ", e);
      }
      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Service Data Send TO PMS ");
   }

   @Override
   public DVResult postWakeupCall(String roomId, String date, String time,
            boolean state)
   {
      try
      {

         // Set Wakeup
         // "WR|RN" + intRoomId + "|DA" + date + "|TI" + time + "|"
         //
         // Clear Wakeup:
         //
         // "WC|RN" + intRoomId + "|DA" + date + "|TI" + time + "|"

         if (state)
         {
            connectionManager.writeSignal(
                     "WR|RN" + roomId + "|DA" + date + "|TI" + time + "|");
         }
         else
         {
            connectionManager.writeSignal(
                     "WC|RN" + roomId + "|DA" + date + "|TI" + time + "|");
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending service ID data to PMS ", e);
      }
      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Service Data Send TO PMS ");
   }

   @Override
   public DVResult remoteCheckout(String roomId, String guestId,
            String targetDeviceId)
   {
      try
      {
         String roomNumber = dvPmsDatabase.getPmsiRoomId(roomId);
         dvPmsDatabase.deleteRemoteCheckoutTargetDeviceId(roomId, guestId);
         dvPmsDatabase.insertRemoteCheckoutGuestId(targetDeviceId, roomId, guestId);
         
         String billAmount = "0";
         try
         {
            if (connectionManager.writeFlag)
            {
               dvPmsDatabase.deleteGuestBillAmount(guestId, roomNumber);
               boolean ack = connectionManager.writeSignal(
                        "XR|RN" + roomNumber + "|G#" + guestId + "|");
               Thread.sleep(BILL_AMOUNT_FEEDBACK_DELAY);
               if (ack)
               {
                  int count = 0;
                  boolean check = true;
                  while (check)
                  {
                     try
                     {
                        String amt = dvPmsDatabase.getGuestBillAmount(guestId,
                                 roomNumber);
                        if (!amt.equalsIgnoreCase("na"))
                        {
                           billAmount = amt;
                           dvPmsDatabase.deleteGuestBillAmount(guestId,
                                    roomNumber);
                           break;
                        }
                        else
                        {
                           count = count + 1;
                           if (count == 11)
                           {
                              break;
                           }
                           Thread.sleep(BILL_AMOUNT_FEEDBACK_DELAY);
                        }

                     }
                     catch (Exception e)
                     {
                        dvLogger.error(
                                 "Error in gettnig bill amount before checkout request ",
                                 e);
                        break;
                     }
                  }
               }
               else
               {
                  return new DVResult(DVResult.DVERROR_PMS_DOWN,
                           "ERROR PMS IS Down");
               }

            }
            else
            {
               return new DVResult(DVResult.DVERROR_PMS_DOWN,
                        "ERROR PMS IS Down");
            }
         }
         catch (Exception e)
         {

         }
         Date now = new Date();
         SimpleDateFormat formatPattern = new SimpleDateFormat("yyMMdd");
         String date = formatPattern.format(now);
         formatPattern = new SimpleDateFormat("HHmmss");
         String time = formatPattern.format(now);
         String dateTime = "DA" + date + "|TI" + time + "|";
         dvLogger.info(
                  "connectionManager.writeFlag " + connectionManager.writeFlag);
         if (connectionManager.writeFlag)
         {
            boolean ack = connectionManager.writeSignal("XC|RN" + roomId + "|G#"
                     + guestId + "|BA" + billAmount + "|" + dateTime);
            dvLogger.info("Acnknowledgement from Express Checkout: " + ack);
            if (ack)
            {
               return new DVResult(DVResult.SUCCESS,
                        "Service Data Send TO PMS ");
            }
            else
            {
               return new DVResult(DVResult.DVERROR_PMS_DOWN,
                        "ERROR PMS IS Down");
            }
         }
         else
         {
            return new DVResult(DVResult.DVERROR_PMS_DOWN, "ERROR PMS IS Down");
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
      try
      {
         GuestData guestData = new GuestData();
         int keyId = dvPmsDatabase.getKeyIdFromRoomNumber(roomNumber);
         ArrayList<String> guestIds = dvPmsDatabase.getGuestIdsByKey(keyId);
         dvLogger.info("inside get guest details : " + keyId + "  "
                  + guestIds.toString());
         if (guestIds.isEmpty())
         {
            dvLogger.info("Room not checked in no guest ID found ");
            guestData.setMessage("No guest ID found Room not checked IN");
            guestData.responseTag(301L);
            guestData.setStatus(true);
            List<GuestDetails> data = new ArrayList<GuestDetails>();
            guestData.setData(data);
            List<String> activeGuests = new ArrayList<String>();
            guestData.setActiveGuestId(activeGuests);
         }
         else
         {
            guestData.setMessage("Guest Details");
            guestData.setStatus(true);
            guestData.responseTag(300L);
            List<GuestDetails> data = new ArrayList<GuestDetails>();
            for (int i = 0; i < guestIds.size(); i++)
            {
               Map<DVPmsData, Object> GuestData =
                        dvPmsDatabase.getDataByGuestId(guestIds.get(i), keyId);
               GuestDetails details = new GuestDetails();
               details.setAlternateName(
                        (String) GuestData.get(DVPmsData.alternateName));
               details.setArrivalDate(
                        (String) GuestData.get(DVPmsData.arrivalDate));
               details.setDepartureDate(
                        (String) GuestData.get(DVPmsData.departureDate));
               details.setEmailId((String) GuestData.get(DVPmsData.emailId));
               details.setGroupCode(
                        (String) GuestData.get(DVPmsData.groupCode));
               details.setGuestFirstName(
                        (String) GuestData.get(DVPmsData.guestFirstName));
               details.setGuestFullName(
                        (String) GuestData.get(DVPmsData.guestFullName));
               details.setGuestId(
                        (GuestData.get(DVPmsData.guestId).toString()));
               details.setGuestLanguage(
                        (String) GuestData.get(DVPmsData.guestLanguage));
               details.setGuestLastName(
                        (String) GuestData.get(DVPmsData.guestLastName));
               details.setGuestName(
                        (String) GuestData.get(DVPmsData.guestName));
               details.setGuestTitle(
                        (String) GuestData.get(DVPmsData.guestTitle));
               details.setGuestType(
                        (String) GuestData.get(DVPmsData.guestType));
               details.setIncognitoName(
                        (String) GuestData.get(DVPmsData.incognitoName));
               details.setPhoneNumber(
                        (String) GuestData.get(DVPmsData.phoneNumber));
               details.setRemoteCheckout(Boolean.parseBoolean(
                        GuestData.get(DVPmsData.remoteCheckout).toString()));
               details.setReservationId(
                        (String) GuestData.get(DVPmsData.reservationId));
               details.setRevisitFlag((Boolean) Boolean.parseBoolean(
                        GuestData.get(DVPmsData.revisitFlag).toString()));
               details.setRoomNumber(roomNumber);
               details.setSafeFlag((Boolean) Boolean.parseBoolean(
                        GuestData.get(DVPmsData.safeFlag).toString()));
               details.setTvRights((String) GuestData.get(DVPmsData.tvRights));
               details.isAdult((String) GuestData.get(DVPmsData.isAdult));
               details.setVipStatus(
                        (String) GuestData.get(DVPmsData.vipStatus));
               details.setVideoRights(
                        (String) GuestData.get(DVPmsData.videoRights));
               details.uniqueId((String) GuestData.get(DVPmsData.uniqueId));

               addGuestPreferences(details);        // add the guest-preferences
                                                    // data to info.

               data.add(details);
            }
            guestData.setData(data);
            List<String> activeGuests = dvPmsDatabase.getActiveGuestIds(keyId);
            dvLogger.info("activeGuests:  "+activeGuests.toString());
            guestData.setActiveGuestId(activeGuests);
         }
         return guestData;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in setting guest details ", e);
      }
      GuestData guestData = new GuestData();
      return guestData;
   }

   /*
    * public Map<DVPmsData,Object> ConvertValuedata(Map<DVPmsData,Object> data)
    * { Set<DVPmsData>dataFeilds =data.keySet(); for (DVPmsData keyName :
    * dataFeilds) { HashMap<DVPmsData,String> keyValueData=new
    * HashMap<DVPmsData,String>(); keyValueData.put(keyName, (String)
    * data.get(keyName));
    * if(dvPmsOpera.PmsValueMappingDetails.containsKey(keyValueData)) { String
    * mappedValue=dvPmsOpera.PmsValueMappingDetails.get(keyValueData);
    * data.put(keyName, mappedValue); dvLogger.info("Value for "+keyName+
    * " changed to: "+mappedValue); } } data=insertDefaultData(data); return
    * data;
    * 
    * }
    */

   @Override
   public DVResult shutDownPms()
   {
      Date now = new Date();
      SimpleDateFormat formatPattern = new SimpleDateFormat("yyMMdd");
      String date = formatPattern.format(now);
      formatPattern = new SimpleDateFormat("HHmmss");
      String time = formatPattern.format(now);
      String dateTime = "DA" + date + "|TI" + time + "|";
      connectionManager.writeSignal("LE|" + dateTime);
      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Sync Data Send TO PMS ");
   }

   @Override
   public DVResult postMovie(String roomNumber, String guestId,
            MovieDetails data, DVMovieEvent movieEvent)
   {
      try
      {
         if (movieEvent == DVMovieEvent.purchased)
         {
            dvLogger.info("Movie Purchased from Room " + roomNumber + "  "
                     + connectionManager.writeFlag);
            String pmsiKeyId = dvPmsDatabase.getPmsiRoomId(roomNumber);
            int keyId = dvPmsDatabase.getKeyId(roomNumber);
            int moviePostingId = dvPmsDatabase.getLastMoviePostingId();
            moviePostingId = moviePostingId + 1;

            MovieData movieData = data.getDetails().get(0);
            String movieId = movieData.getMovieId().trim();
            String price = dvPmsDatabase.getMoviePriceFromKeyIdMovieId(movieId,
                     keyId);
            price = price.substring(0, price.length() - 2);
            price = price + "00";
            int movieKeyDataId =
                     dvPmsDatabase.getMovieKeyDataId(movieId, keyId);
            // boolean alreadywait =PmsClient.postingToPMS("PS|RN" +
            // intRoomId + "|TA" + price + "|X1In House Movie|DA" + date +
            // "|TI" + time + "|P#" + pnumber + "|CT" + movieid +
            // "|PTC|PXODM|" , "vod");

            if (connectionManager.writeFlag)
            {
               boolean ack = connectionManager.writeSignal("PS|RN" + pmsiKeyId
                        + "|TA" + price + "|X1In House Movie|"
                        + connectionManager.getDateTime() + "P#"
                        + moviePostingId + "|CT" + movieKeyDataId
                        + "|PTC|PXODM|");
               dvLogger.info("Acnknowledgement from Post Movie: " + ack);
               if (ack)
               {
                  dvPmsDatabase.insertMoviePostingRecords(movieKeyDataId, 1,
                           keyId);
                  return new DVResult(DVResult.SUCCESS,
                           "Service Data Send TO PMS ");
               }
               else
               {
                  dvPmsDatabase.insertMoviePostingRecords(movieKeyDataId, 0,
                           keyId);
                  return new DVResult(DVResult.DVERROR_PMS_DOWN,
                           "ERROR PMS IS Down");
               }
            }
            else
            {
               dvPmsDatabase.insertMoviePostingRecords(movieKeyDataId, 0,
                        keyId);
               return new DVResult(DVResult.DVERROR_PMS_DOWN,
                        "ERROR PMS IS Down");
            }



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
         return pmsAlertState;
   }

   @Override
   public void notifyMessageEvent(MessageFeatureEvent featureEventType,
            Map<DVPMSMessageData, Object> messageData)
   {
      notifyMessageEvents(featureEventType, messageData);
   }

   @Override
   public DVResult postPendingMovie(int pendingId)
   {
      try
      {
         int movieKeyDataId =
                  dvPmsDatabase.getMovieKeyDataIdFromMoviePendingId(pendingId);
         String price =
                  dvPmsDatabase.getMoviePriceFromMovieKeyDataId(movieKeyDataId);
         int keyId = dvPmsDatabase.getKeyIdFromMovieKeyDataId(movieKeyDataId);
         String roomNumber = dvPmsDatabase.getDigivaletRoomNumber(keyId);
         String pmsiKeyId = dvPmsDatabase.getPmsiRoomId(roomNumber);
         if (connectionManager.writeFlag)
         {
            boolean ack = connectionManager.writeSignal("PS|RN" + pmsiKeyId
                     + "|TA" + price + "|X1In House Movie|"
                     + connectionManager.getDateTime() + "P#" + pendingId
                     + "|CT" + movieKeyDataId + "|PTC|PXODM|");
            dvLogger.info("Acnknowledgement from Post Movie: " + ack);
            if (ack)
            {
               dvPmsDatabase.updateMoviePostingRecords(pendingId, 1);
               return new DVResult(DVResult.SUCCESS,
                        "Service Data Send TO PMS ");
            }
            else
            {
               dvPmsDatabase.updateMoviePostingRecords(pendingId, 0);
               return new DVResult(DVResult.DVERROR_PMS_DOWN,
                        "ERROR PMS IS Down");
            }
         }
         else
         {
            dvPmsDatabase.updateMoviePostingRecords(pendingId, 0);
            return new DVResult(DVResult.DVERROR_PMS_DOWN, "ERROR PMS IS Down");
         }



      }
      catch (Exception e)
      {
         dvLogger.error("Error in posting movie to PMS ", e);
         return new DVResult(DVResult.DVERROR_PMS_DOWN, "ERROR PMS IS Down");
      }
   }

   private void addGuestPreferences(GuestDetails guestDetails)
   {
      try
      {
         int keyId = dvPmsDatabase.getKeyId(guestDetails.getRoomNumber());
         DVGuestPreferenceModel preferenceData =
                  dvPmsDatabase.getGuestPreferenceDataByKeyId(keyId);
         guestDetails.setWelcomeMoodId(preferenceData.getMoodId());
         guestDetails.setTemperature(preferenceData.getTemperature());
         guestDetails.setFragrance(preferenceData.getFragrance());
      }
      catch (Exception e)
      {
         dvLogger.error(
                  "Exception while adding guest preference in guest info\n", e);
      }
   }

   @Override
   public String getErrorLog()
   {
      // TODO Auto-generated method stub
      return null;
   }
}
