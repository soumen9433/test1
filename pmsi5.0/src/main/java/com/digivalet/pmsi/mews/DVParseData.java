package com.digivalet.pmsi.mews;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.json.JSONArray;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVPmsBillData;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.events.DVBillEvent.BillFeatureEventType;
import com.digivalet.pmsi.events.DVEvent.FeatureEventType;
import com.digivalet.pmsi.exceptions.DVFileException;
import com.digivalet.pmsi.mews.models.MewsCustomerData;
import com.digivalet.pmsi.mews.models.MewsMasterReservationData;
import com.digivalet.pmsi.mews.models.MewsReservationData;
import com.digivalet.pmsi.settings.DVSettings;

/**
 * 
 * @author lavin
 * 
 * @description: The Thread in this class is triggered on event from MEWS and
 *               parses the data accordingly. Events: Checkin, Checkout,
 *               RoomChange, GuestInfo Change (As of now GuestInfoChange event
 *               is not available from MEWS)
 * 
 *               The class also parse the Sync, where it fetch all the
 *               reservations from MEWS, parse it, check if exist and insert it
 *               to digivalet system.
 * 
 */


public class DVParseData extends Thread
{
   private final int SLEEP_BETWEEN_OLDCHECKOUT_NEWCHECKIN = 3000;
   private static DVLogger dvLogger = DVLogger.getInstance();
   private JSONObject data;
   private DVPmsMews dvPmsMews;
   private DVPmsDatabase dvPmsDatabase;
   private DVSettings dvSettings;
   private Map<String, MewsCustomerData> customersMap = new HashMap<>();
   private Map<String, MewsReservationData> reservationsMap = new HashMap<>();
   private final int GUESTINFORMATIONUPDATEDELAY = 1000;

   public DVParseData(JSONObject data, DVPmsDatabase dvPmsDatabase,
            DVSettings dvSettings,DVPmsMews dvPmsMews)
   {
      this.data = data;
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvSettings = dvSettings;
      this.dvPmsMews=dvPmsMews;
   }

   public void sync(MewsMasterReservationData reservationData)
   {
      dvLogger.info("PARSE SYNC DATA CALL");
      try
      {
         getMappingAndProcessEvent(reservationData);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while processing sync..", e);
      }
   }

   @Override
   public void run()
   {

      try
      {
         //DVMewsClient dvMewsClient = new DVMewsClient(dvSettings);
         Thread.currentThread().setName("MEWS EVENT THREAD");
         dvLogger.info("Event Data received: " + data);

         String reservationId = data.getJSONArray(MewsKeyTags.EVENTS.toString())
                  .getJSONObject(0).getString(MewsKeyTags.ID.toString());
         String event = "";
         String spaceId = data.getJSONArray(MewsKeyTags.EVENTS.toString())
                  .getJSONObject(0)
                  .getString(MewsKeyTags.ASSIGNEDSPACEID.toString());
         String arrivalDate = data.getJSONArray(MewsKeyTags.EVENTS.toString())
                  .getJSONObject(0).getString(MewsKeyTags.STARTUTC.toString());
         String departureDate = data.getJSONArray(MewsKeyTags.EVENTS.toString())
                  .getJSONObject(0).getString(MewsKeyTags.ENDUTC.toString());

         MewsMasterReservationData masterData =
                  dvPmsMews.dvMewsClient.getReservationDetails(reservationId);

         String guestId = masterData.getReservations().get(0).getCustomerId();

         String groupCode = masterData.getReservations().get(0).getGroupId();

         boolean primaryCheckinOut = false;
         boolean secondaryCheckinOut = false;

         for (MewsCustomerData customer : masterData.getCustomers())
         {
               if (customer.getId().equalsIgnoreCase(guestId))
               {
                  try
                  {
                     Map<String, String> dataMap = parseCustomerData(customer);

                     dataMap.put("GS", GuestData.PRIMARY.toString());

                     dataMap.put("GuestId", guestId);
                     dataMap.put("GA", arrivalDate);
                     dataMap.put("GD", departureDate);
                     dataMap.put("RN",
                              (spaceId));
                     dataMap.put("ReservationId", reservationId);
                     dataMap.put("GroupCode", groupCode);

                     dvLogger.info("Prepared Guest Data Map: " + dataMap);

                     if (data.getJSONArray(MewsKeyTags.EVENTS.toString())
                              .getJSONObject(0)
                              .getString(MewsKeyTags.STATE.toString())
                              .equalsIgnoreCase(EventState.STARTED.toString())
                              && dvPmsDatabase.validateReservationIdExist(
                                       reservationId,
                                       spaceId))
                     {
                        event = "GC";
                     }
                     else if (data.getJSONArray(MewsKeyTags.EVENTS.toString())
                              .getJSONObject(0)
                              .getString(MewsKeyTags.STATE.toString())
                              .equalsIgnoreCase(EventState.STARTED.toString())
                              && dvPmsDatabase
                                       .validateIsNeedToCheckin(
                                                spaceId,
                                                guestId, reservationId))
                     {
                        event = "GI";
                     }
                     else if (data.getJSONArray(MewsKeyTags.EVENTS.toString())
                              .getJSONObject(0)
                              .getString(MewsKeyTags.STATE.toString())
                              .equalsIgnoreCase(
                                       EventState.PROCESSED.toString()))
                     {
                        event = "GO";
                     }


                     switch (Command.fromString(event))
                     {
                        case GI:
                           checkin(dataMap);
                           break;
                        case GC:
                           roomChange(dataMap);
                           break;
                        case GO:
                           checkout(dataMap);
                           break;
                        default:
                           dvLogger.info("No event to be performed");
                     }

                     primaryCheckinOut = true;
                  }
                  catch (Exception e)
                  {
                     dvLogger.error(
                              "Exception while parsing different guest data\n",
                              e);
                  }
               }

               /*
                * if (!customer.getId().equalsIgnoreCase(guestId) &&
                * !secondaryCheckinOut) { try { Map<String, String> dataMap =
                * parseCustomerData(customer);
                * 
                * dataMap.put("GS", GuestData.SECONDARY.toString());
                * 
                * dataMap.put("GuestId", guestId); dataMap.put("GA",
                * arrivalDate); dataMap.put("GD", departureDate);
                * dataMap.put("RN", (spaceId)); dataMap.put("ReservationId",
                * reservationId); dataMap.put("GroupCode", groupCode);
                * 
                * dvLogger.info("Prepared Guest Data Map: " + dataMap);
                * 
                * if (data.getJSONArray(MewsKeyTags.EVENTS.toString())
                * .getJSONObject(0) .getString(MewsKeyTags.STATE.toString())
                * .equalsIgnoreCase(EventState.STARTED.toString()) &&
                * dvPmsDatabase.validateReservationIdExist( reservationId,
                * (spaceId))) { event = "GC"; } else if
                * (data.getJSONArray(MewsKeyTags.EVENTS.toString())
                * .getJSONObject(0) .getString(MewsKeyTags.STATE.toString())
                * .equalsIgnoreCase(EventState.STARTED.toString()) &&
                * dvPmsDatabase .validateIsNeedToCheckin( spaceId, guestId,
                * reservationId)) { event = "GI"; } else if
                * (data.getJSONArray(MewsKeyTags.EVENTS.toString())
                * .getJSONObject(0) .getString(MewsKeyTags.STATE.toString())
                * .equalsIgnoreCase( EventState.PROCESSED.toString())) { event =
                * "GO"; }
                * 
                * 
                * switch (Command.fromString(event)) { case GI:
                * checkin(dataMap); break; case GC: roomChange(dataMap); break;
                * case GO: checkout(dataMap); break; default:
                * dvLogger.info("No event to be performed"); }
                * 
                * secondaryCheckinOut = true; } catch (Exception e) {
                * dvLogger.error(
                * "Exception while parsing different guest data\n", e); } }
                */

               if (primaryCheckinOut && secondaryCheckinOut)
               {
                  break;
               }
           // }
         }
      }

      catch (Exception e)
      {
         dvLogger.error("Exception in dvDataParse\n", e);
      }
   }

   public void checkout(Map<String, String> dataMap)
   {
      try
      {
         String containSF, roomno, guestID, guestSF;

         if ((containSF = dataMap.get("SF")) != null);
         if ((roomno = dataMap.get("RN")) != null);
         if ((guestID = dataMap.get("GuestId")) != null);
         if ((guestSF = dataMap.get("GS")) != null);
         Thread.currentThread().setName("MEWS CHECKOUT " + roomno);

         if (null != guestSF
                  && !guestSF.equalsIgnoreCase(GuestData.PRIMARY.toString()))
         {
            guestSF = "secondary";
         }
         else
         {
            guestSF = "primary";
         }

         if (containSF != null && !("".equalsIgnoreCase(containSF)
                  || "false".equalsIgnoreCase(containSF)))
         {
            containSF = "true";
         }
         else
         {
            containSF = "false";
         }

         Map<DVPmsData, Object> checkinData = new HashMap<>();
         checkinData.put(DVPmsData.keyId, roomno);

         checkinData.put(DVPmsData.safeFlag, containSF);

         checkinData.put(DVPmsData.guestId, guestID);
         checkinData.put(DVPmsData.guestType, guestSF);

         dvPmsMews.notifyPmsEvent(FeatureEventType.PMSI_CHECKOUT_EVENT,
                  checkinData);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in parsing checkout ", e);
      }
   }

   public void checkin(Map<String, String> dataMap)
   {
      try
      {
         
         dvLogger.info("Data Map On check In ::::: "+ dataMap);
         String guestName, guestemail, guestLan = "EN", containSF = "false",
                  roomno, guestID, guestSF = "N", birthDate = "",
                  authorization = "no", guestArrivalDate, guestDepartureDate,
                  guestfirstname, guestlastname, salutation, guestvipstatus,
                  groupCode = "NA", uniqeKey = "NA", tvrights = "TU",
                  reservationno = "NA", videorights = "VA", guestrevisit, guestFullName ="";

         if ((guestName = (String) dataMap.get("guestName")) != null);
         if ((guestlastname = (String) dataMap.get("guestLastName")) != null);
         if ((guestLan = (String) dataMap.get("languageCode")) != null);
         if ((containSF = (String) dataMap.get("SF")) != null);
         if ((roomno = (String) dataMap.get("RN")) != null);
         if ((guestID = (String) dataMap.get("GuestId")) != null);
         if ((guestSF = (String) dataMap.get("GS")) != null);
         if ((tvrights = (String) dataMap.get("TV")) != null);
         if ((guestArrivalDate = (String) dataMap.get("GA")) != null);
         if ((guestDepartureDate = (String) dataMap.get("GD")) != null);
         if ((guestfirstname = (String) dataMap.get("guestfirstname")) != null);
         if ((videorights = (String) dataMap.get("VR")) != null);
         if ((salutation = (String) dataMap.get("guestTitle")) != null);
         if ((guestvipstatus = (String) dataMap.get("GV")) != null);
         if ((guestemail = (String) dataMap.get("guestEmail")) != null);
         if ((guestrevisit = (String) dataMap.get("Revisit")) != null);
         if ((groupCode = (String) dataMap.get("GroupCode")) != null);
         if ((birthDate = (String) dataMap.get("guestBirthDate")) != null);

            reservationno = dataMap.get("ReservationId");

         Thread.currentThread().setName("MEWS CHECKIN " + roomno);

         if (null != guestSF
                  && !guestSF.equalsIgnoreCase(GuestData.PRIMARY.toString()))
         {
            guestSF = "secondary";
         }
         else
         {
            guestSF = "primary";
         }

         if (containSF != null && !("".equalsIgnoreCase(containSF)
                  || "false".equalsIgnoreCase(containSF)))
         {
            containSF = "true";
         }
         else
         {
            containSF = "false";
         }

         Map<DVPmsData, Object> data = new HashMap<>();
         if (null != guestArrivalDate)
         {
            data.put(DVPmsData.arrivalDate,
                     parseIso8601DateTime(guestArrivalDate));
         }
         if (null != guestDepartureDate)
         {
            data.put(DVPmsData.departureDate,
                     parseIso8601DateTime(guestDepartureDate));
         }
         if (null != salutation) {
            data.put(DVPmsData.guestTitle, salutation);
         }else {
            data.put(DVPmsData.guestTitle, "");
         }
         if (null != guestfirstname) {
            data.put(DVPmsData.guestFirstName, guestfirstname);
            guestFullName = guestFullName+" ";
         }else {
            data.put(DVPmsData.guestFirstName, "");
         }
         if (null != guestlastname) {
            data.put(DVPmsData.guestLastName, guestlastname);
            guestFullName = guestlastname;
         }else {
            data.put(DVPmsData.guestLastName, "");
         }
         
         
         data.put(DVPmsData.guestId, guestID);
         data.put(DVPmsData.guestLanguage, guestLan);
         data.put(DVPmsData.guestType, guestSF);
         data.put(DVPmsData.guestName, guestName);
         data.put(DVPmsData.keyId, roomno);
         data.put(DVPmsData.emailId, guestemail);
         data.put(DVPmsData.revisitFlag, guestrevisit);
         data.put(DVPmsData.reservationId, reservationno);
         data.put(DVPmsData.guestFullName, guestFullName);
         data.put(DVPmsData.alternateName, "");
         data.put(DVPmsData.incognitoName, "");
         data.put(DVPmsData.phoneNumber, "");
         data.put(DVPmsData.previousVisitDate, "");
         data.put(DVPmsData.nationality, "");
         data.put(DVPmsData.dateOfBirth, birthDate);
         data.put(DVPmsData.groupCode, groupCode);


         data.put(DVPmsData.tvRights, "TU");
         data.put(DVPmsData.videoRights, "VA");
         
         dvLogger.info("Data Map On check In MAPPPPP ::::: "+ data);

         if (null != containSF)
         {
            data.put(DVPmsData.safeFlag, containSF);
         }

         if (null != tvrights)
         {
            data.put(DVPmsData.tvRights, tvrights);
         }

         if (null != videorights)
         {
            data.put(DVPmsData.videoRights, videorights);
         }

         if (null != guestvipstatus)
         {
            data.put(DVPmsData.vipStatus, guestvipstatus);
         }

         dvPmsMews.notifyPmsEvent(FeatureEventType.PMSI_CHECKIN_EVENT, data);

      }
      catch (Exception e)
      {
         dvLogger.error("Error in sendin checkin ", e);
      }
   }

   public void roomChange(Map<String, String> dataMap)
   {
      try
      {
         dvLogger.info("Data Map On ROOM CHANGE Call ::::: "+ dataMap);
         String guestName, guestemail, guestLan = "EN", containSF = "false",
                  roomno, guestID, guestSF = "N", authorization = "no",
                  guestArrivalDate, guestDepartureDate, guestfirstname,
                  guestlastname, salutation, guestvipstatus, groupCode = "NA",
                  uniqeKey = "NA", tvrights = "TU", reservationno = "NA",
                  videorights = "VA", guestrevisit, guestFullName ="";

         if ((guestName = (String) dataMap.get("guestName")) != null);
         if ((guestlastname = (String) dataMap.get("guestLastName")) != null);
         if ((guestLan = (String) dataMap.get("languageCode")) != null);
         if ((containSF = (String) dataMap.get("SF")) != null);
         if ((roomno = (String) dataMap.get("RN")) != null);
         if ((guestID = (String) dataMap.get("GuestId")) != null);
         if ((guestSF = (String) dataMap.get("GS")) != null);
         if ((tvrights = (String) dataMap.get("TV")) != null);
         if ((guestArrivalDate = (String) dataMap.get("GA")) != null);
         if ((guestDepartureDate = (String) dataMap.get("GD")) != null);
         if ((guestfirstname = (String) dataMap.get("guestfirstname")) != null);
         if ((videorights = (String) dataMap.get("VR")) != null);
         if ((salutation = (String) dataMap.get("guestTitle")) != null);
         if ((guestvipstatus = (String) dataMap.get("GV")) != null);
         if ((guestemail = (String) dataMap.get("guestEmail")) != null);
         if ((guestrevisit = (String) dataMap.get("Revisit")) != null);
         if ((groupCode = (String) dataMap.get("GroupCode")) != null);

         reservationno = dataMap.get("ReservationId");

         Thread.currentThread().setName("MEWS ROOM CHANGE " + roomno);

         if (null != guestSF
                  && !guestSF.equalsIgnoreCase(GuestData.PRIMARY.toString()))
         {
            guestSF = "secondary";
         }
         else
         {
            guestSF = "primary";
         }

         if (containSF != null && !("".equalsIgnoreCase(containSF)
                  || "false".equalsIgnoreCase(containSF)))
         {
            containSF = "true";
         }
         else
         {
            containSF = "false";
         }

         Map<DVPmsData, Object> data = new HashMap<>();
         if (null != guestArrivalDate)
         {
            data.put(DVPmsData.arrivalDate, parseIso8601DateTime(guestArrivalDate));
         }
         if (null != guestDepartureDate)
         {
            data.put(DVPmsData.departureDate, parseIso8601DateTime(guestDepartureDate));
         }
         if (null != salutation) {
            data.put(DVPmsData.guestTitle, salutation);
         }else {
            data.put(DVPmsData.guestTitle, "");
         }
         if (null != guestfirstname) {
            data.put(DVPmsData.guestFirstName, guestfirstname);
            guestFullName = guestFullName+" ";
         }else {
            data.put(DVPmsData.guestFirstName, "");
         }
         if (null != guestlastname) {
            data.put(DVPmsData.guestLastName, guestlastname);
            guestFullName = guestlastname;
         }else {
            data.put(DVPmsData.guestLastName, "");
         }
         data.put(DVPmsData.guestId, guestID);
         data.put(DVPmsData.guestLanguage, guestLan);
         data.put(DVPmsData.guestType, guestSF);
         data.put(DVPmsData.guestName, guestName);
         data.put(DVPmsData.keyId,
                  dvPmsDatabase.getPmsRoomNumberByReservationId(reservationno));
         data.put(DVPmsData.emailId, guestemail);
         data.put(DVPmsData.revisitFlag, guestrevisit);
         data.put(DVPmsData.reservationId, reservationno);
         data.put(DVPmsData.guestFullName, guestFullName);
         data.put(DVPmsData.alternateName, "");
         data.put(DVPmsData.incognitoName, "");
         data.put(DVPmsData.phoneNumber, "");
         data.put(DVPmsData.previousVisitDate, "");
         data.put(DVPmsData.nationality, "");
         data.put(DVPmsData.groupCode, groupCode);


         data.put(DVPmsData.tvRights, "TU");
         data.put(DVPmsData.videoRights, "VA");
         
         dvLogger.info("Data Map On CHANGE-ROOM MAPPPPP ::::: "+ data);

         if (null != containSF)
         {
            data.put(DVPmsData.safeFlag, containSF);
         }

         if (null != tvrights)
         {
            data.put(DVPmsData.tvRights, tvrights);
         }

         if (null != videorights)
         {
            data.put(DVPmsData.videoRights, videorights);
         }

         if (null != guestvipstatus)
         {
            data.put(DVPmsData.vipStatus, guestvipstatus);
         }

         dvPmsMews.notifyPmsEvent(
                  FeatureEventType.PMSI_ROOMCHANGE_CHECKOUT_EVENT, data);

         Thread.sleep(SLEEP_BETWEEN_OLDCHECKOUT_NEWCHECKIN);

         data.put(DVPmsData.guestType, guestSF);
         data.put(DVPmsData.keyId, roomno);
         dvLogger.info("data at parse " + data.toString());

         dvPmsMews.notifyPmsEvent(
                  FeatureEventType.PMSI_ROOMCHANGE_CHECKIN_EVENT, data);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in sendin checkin ", e);
      }
   }

   public Map<String, String> parseGuestData(JSONObject jsonObject)
   {
      Map<String, String> guestDataMap = new HashMap<>();

      try
      {
         JSONObject customerObject =
                  jsonObject.getJSONArray("Customers").getJSONObject(0);

         guestDataMap.put("Email", customerObject.getString("Email"));
         guestDataMap.put("Gender", customerObject.getString("Gender"));
         guestDataMap.put("LanguageCode",
                  customerObject.getString("LanguageCode"));
         guestDataMap.put("FirstName", customerObject.getString("FirstName"));
         guestDataMap.put("Title", customerObject.getString("Title"));
         guestDataMap.put("SecondLastName",
                  (null != customerObject.get("SecondLastName")
                           ? customerObject.get("SecondLastName").toString()
                           : ""));
         guestDataMap.put("LastName", customerObject.getString("LastName"));
         guestDataMap.put("BirthDate", customerObject.getString("BirthDate"));

         // Classifications
         if (customerObject.has("Classifications"))
         {
            JSONArray guestClassifications =
                     customerObject.getJSONArray("Classifications");

            if (null != guestClassifications
                     && guestClassifications.length() > 0)
            {
               for (int i = 0; i < guestClassifications.length(); i++)
               {
                  guestDataMap.put("Revisit", "false");
                  guestDataMap.put("GV", "false");

                  if (guestClassifications.getString(i).equalsIgnoreCase(
                           ClassificationTags.RETURNING.toString()))
                  {
                     guestDataMap.put("Revisit", "true");
                  }

                  if (guestClassifications.getString(i).equalsIgnoreCase(
                           ClassificationTags.VeryImportant.toString()))
                  {
                     guestDataMap.put("GV", "true");
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while parsing guest json data to map\n", e);
      }

      return guestDataMap;
   }

   public Map<String, String> parseCustomerData(MewsCustomerData customerData)
   {
      Map<String, String> guestDataMap = new HashMap<>();
      int key = dvPmsDatabase.getKeyIdFromGuestId(customerData.getId());
      String keyId = Integer.toString(key);
      dvLogger.info("Got room key :  " + key);
      try
      {

         guestDataMap.put("guestName",
                  customerData.getTitle() + " " + customerData.getFirstName()
                           + " " + customerData.getLastName());
         guestDataMap.put("guestId", customerData.getId());
         guestDataMap.put("keyId", keyId);
         guestDataMap.put("guestEmail", customerData.getEmail());
         guestDataMap.put("guestGender", customerData.getGender());
         guestDataMap.put("languageCode", customerData.getLanguageCode());
         guestDataMap.put("guestfirstname", customerData.getFirstName());
         guestDataMap.put("guestTitle", customerData.getTitle());
         guestDataMap.put("guestSecondLastName",
                  (null != customerData.getSecondLastName()
                           ? customerData.getSecondLastName()
                           : ""));
         guestDataMap.put("guestLastName", customerData.getLastName());
         guestDataMap.put("guestBirthDate", customerData.getBirthDate());
         guestDataMap.put("guestPhoneNumber", customerData.getPhone());
         guestDataMap.put("guestNationality",
                  customerData.getNationalityCode());
         guestDataMap.put("guestArrival",
                  dvPmsDatabase.getGuestArrivalDate(customerData.getId()));
         // Classifications
         if (!customerData.getClassifications().isEmpty())
         {
            guestDataMap.put("Revisit", "false");
            guestDataMap.put("GV", "false");

            for (String guestClassification : customerData.getClassifications())
            {
               if (guestClassification.equalsIgnoreCase(
                        ClassificationTags.RETURNING.toString()))
               {
                  guestDataMap.put("Revisit", "true");
               }

               if (guestClassification.equalsIgnoreCase(
                        ClassificationTags.VeryImportant.toString()))
               {
                  guestDataMap.put("GV", "true");
               }
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while parsing guest json data to map\n", e);
      }

      return guestDataMap;
   }

   public void getBill(String guestId, String roomNumber)
   {
      try
      {

         JSONObject jsonObject = dvPmsMews.dvMewsClient.getBill(guestId);

         parseAndSendBill(guestId, roomNumber, jsonObject);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getBill DVParseData\n", e);
      }
   }


   private void parseAndSendBill(String guestId, String roomNumber,
            JSONObject jsonObject)
   {
      try
      {
         dvLogger.info("get bill: " + jsonObject.toString());

         if (jsonObject.has(BillTags.Bills.toString())
                  && null != jsonObject.get(BillTags.Bills.toString())
                  && jsonObject.getJSONArray(BillTags.Bills.toString())
                           .length() > 0)
         {
            for (int i = 0; i < jsonObject
                     .getJSONArray(BillTags.Bills.toString()).length(); i++)
            {
               JSONObject billObject =
                        jsonObject.getJSONArray(BillTags.Bills.toString())
                                 .getJSONObject(i);

               if (null != billObject
                        && billObject.has(BillTags.Revenue.toString()))
               {
                  JSONArray revenueArray =
                           billObject.getJSONArray(BillTags.Revenue.toString());

                  if (null != revenueArray && revenueArray.length() > 0)
                  {
                     for (int j = 0; j < revenueArray.length(); j++)
                     {
                        JSONObject revenueObject =
                                 revenueArray.getJSONObject(j);

                        if (null != revenueObject)
                        {
                           String folioNO = revenueObject
                                    .getString(BillTags.BillId.toString());

                           double itemAmt = 0.0;

                           if (null != revenueObject
                                    .get(BillTags.Amount.toString()))
                           {
                              try
                              {
                                 itemAmt = revenueObject
                                          .getJSONObject(
                                                   BillTags.Amount.toString())
                                          .getDouble(BillTags.Value.toString());
                              }
                              catch (Exception e)
                              {
                                 dvLogger.error(
                                          "Exception while casting amount string to double\n",
                                          e);
                              }
                           }
                           String itemDisplay = revenueObject
                                    .getString(BillTags.Name.toString());
                           String itemDesciption = "";

                           if (null != revenueObject
                                    .get(BillTags.Notes.toString()))
                           {
                              itemDesciption = revenueObject
                                       .get(BillTags.Notes.toString())
                                       .toString();
                           }

                           String[] dateTime =
                                    parseIso8601ToDateAndTimeSeparate(
                                             revenueObject.getString(
                                                      BillTags.ConsumptionUtc
                                                               .toString()));

                           dvPmsDatabase.insertGuestItemData(guestId,
                                    roomNumber, itemDisplay, itemAmt,
                                    itemDisplay, dateTime[0], dateTime[1],
                                    folioNO);
                        }
                     }
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while parsing the bill data", e);
      }

      sendBill(guestId, roomNumber);
   }

   private void sendBill(String guestId, String roomNumber)
   {
      /**
       * Sendin bill notiy event
       */

      try
      {
         Map<DVPmsBillData, Object> billData = new HashMap<>();

         billData.put(DVPmsBillData.guestId, guestId);
         billData.put(DVPmsBillData.keyId, roomNumber);

         DVPmsMews dvPmsMewsLocal = (DVPmsMews) DVPmsMain.getInstance()
                  .getDvPmsController().getPms();

         dvPmsMewsLocal.notifyPmsBillEvents(
                  BillFeatureEventType.PMSI_BILL_EVENT, billData);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while sending bill notify event\n", e);
      }
   }


   public boolean remoteCheckout(String roomNumber, String guestId,
            String arrivalDate, String departure, String targetDeviceId)
   {
      boolean checkoutStatus = false;

      dvLogger.info("Received params:\nRoom:" + roomNumber + " GuestId: "
               + guestId + " Arrival: " + arrivalDate + " Departure "
               + departure);

      try
      {
         String reservationId = dvPmsDatabase.getReservationIdByGuest(guestId);
         dvPmsDatabase.deleteRemoteCheckoutTargetDeviceId(roomNumber, guestId);
         dvPmsDatabase.insertRemoteCheckoutGuestId(targetDeviceId, roomNumber,
                  guestId);
         dvLogger.info("RESERVATIONID  :: "+reservationId);
         dvLogger.info("dvPmsMews "+dvPmsMews);
         dvLogger.info("dvMewsClient "+dvPmsMews.dvMewsClient);
         checkoutStatus =dvPmsMews.dvMewsClient.remoteCheckout(reservationId, roomNumber, guestId);         
         
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while processing remote checkout\n", e);
      }

      return checkoutStatus;
   }

   public synchronized void guestInformationUpdate(Map<String, String> dataMap)
   {
      try
      {
         dvLogger.info("keys : " + dataMap.keySet());
         dvLogger.info("values : " + dataMap.values());


         String tvrights = "TU", oldRoom, roomno, guestID, guestSF, guestSFO,
                  guestGender, guestEmail, GuestRevisit, guestArrivalDate,
                  guestDepartureDate, guestfirstname, guestlastname,
                  guestSecondLastName, guestPhoneNumber, guestFullname = "",
                  salutation, guestvipstatus = "na", guestBirthday, guestTitle,
                  guestNationality, guestName = "", guestLan = "",
                  authorization = "false", groupCode = "NA", videorights = "NA",
                  containSF = "false";

         if ((guestTitle = (String) dataMap.get("guestTitle")) != null);
         if ((roomno = (String) dataMap.get("keyId")) != null);
         if ((guestID = (String) dataMap.get("guestId")) != null);
         if ((guestSF = (String) dataMap.get("GS")) != null);
         if ((guestSFO = (String) dataMap.get("GSO")) != null);
         if ((guestName = (String) dataMap.get("guestName")) != null);
         if ((guestLan = (String) dataMap.get("languageCode")) != null);
         if ((tvrights = (String) dataMap.get("TV")) != null);
         if ((guestArrivalDate = (String) dataMap.get("guestArrival")) != null);
         if ((guestDepartureDate = (String) dataMap.get("GD")) != null);
         if ((guestfirstname = (String) dataMap.get("guestfirstname")) != null);
         if ((guestlastname = (String) dataMap.get("guestLastName")) != null);
         if ((guestSecondLastName =
                  (String) dataMap.get("guestSecondLastName")) != null)
            ;
         if ((videorights = (String) dataMap.get("VR")) != null);
         if ((salutation = (String) dataMap.get("GT")) != null);
         if ((guestvipstatus = (String) dataMap.get("GV")) != null);
         if ((containSF = (String) dataMap.get("SF")) != null);
         if ((guestBirthday = (String) dataMap.get("guestBirthDate")) != null);
         if ((guestGender = (String) dataMap.get("guestGender")) != null);
         if ((guestEmail = (String) dataMap.get("guestEmail")) != null);
         if ((guestPhoneNumber =
                  (String) dataMap.get("guestPhoneNumber")) != null)
            ;
         if ((guestNationality =
                  (String) dataMap.get("guestNationality")) != null)
            ;

         Thread.currentThread()
                  .setName("MEWS GUESTINFORMATION CHANGE " + roomno);
         dvLogger.info("safeFlag " + containSF);

         if (containSF != null)
         {
            if ("".equalsIgnoreCase(containSF)
                     || "false".equalsIgnoreCase(containSF))
            {
               containSF = "false";
            }
            else
            {
               containSF = "true";
            }
         }
         else
         {
            containSF = "false";
         }
         if (null != guestSF)
         {
            if (guestSF.equalsIgnoreCase("N"))
            {
               guestSF = "primary";
            }
            else
            {
               guestSF = "secondary";
            }
         }
         else
         {
            guestSF = "primary";
         }

         if (null != guestSFO)
         {
            if (guestSFO.equalsIgnoreCase("N"))
            {
               guestSFO = "primary";
            }
            else
            {
               guestSFO = "secondary";
            }
         }
         else
         {
            guestSFO = "primary";
         }
         try
         {
            if (null != videorights && !"NA".equalsIgnoreCase(videorights)
                     && !"".equalsIgnoreCase(videorights))
            {
               if (videorights.equalsIgnoreCase("VA"))
               {
                  authorization = "true";
               }
               else
               {
                  authorization = "false";
               }
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Error in ", e);
         }


         if (null == guestName)
         {
            guestName = "";
         }
         if (null == salutation)
         {
            salutation = "";
         }

         Map<DVPmsData, Object> data = new HashMap<>();
         if (null != guestArrivalDate)
         {
            data.put(DVPmsData.arrivalDate, guestArrivalDate);
         }
         if (null != guestDepartureDate)
         {
            data.put(DVPmsData.departureDate, guestDepartureDate);
         }else {
            data.put(DVPmsData.departureDate, "2020-01-01 00:00:00");
         }
         data.put(DVPmsData.guestFirstName, guestfirstname);
         data.put(DVPmsData.guestId, guestID);
         data.put(DVPmsData.guestLanguage, guestLan);
         data.put(DVPmsData.guestTitle, guestTitle);
         // data.put(DVPmsData.dateOfBirth, );
         // data.put(DVPmsData.guestGender, );
         // data.put(DVPmsData.dateOfBirth, );
         data.put(DVPmsData.guestFirstName, guestfirstname);
         data.put(DVPmsData.guestName, guestName);
         data.put(DVPmsData.guestLastName, guestlastname);
         data.put(DVPmsData.remoteCheckout, authorization);
         if (null != containSF)
         {
            data.put(DVPmsData.safeFlag, containSF);
         }
         if (null != tvrights)
         {
            data.put(DVPmsData.tvRights, tvrights);
         }
         if (null != tvrights)
         {
            data.put(DVPmsData.isAdult, tvrights);
         }
         if (null != videorights)
         {
            data.put(DVPmsData.videoRights, videorights);
         }
         if (null != guestvipstatus)
         {
            data.put(DVPmsData.vipStatus, guestvipstatus);
         }

         data.put(DVPmsData.temperature, "");
         data.put(DVPmsData.welcomeMoodId, "");
         data.put(DVPmsData.fragrance, "");

         data.put(DVPmsData.guestType, guestSF);
         data.put(DVPmsData.keyId, roomno);

         Thread.sleep(GUESTINFORMATIONUPDATEDELAY);

         dvLogger.info("DataMap for GuestInfoChange : " + data.entrySet());

         dvPmsDatabase.UpdateExistingGuestDetails(data,
                  Integer.parseInt(roomno));

         dvPmsMews.notifyPmsEvent(
                  FeatureEventType.PMSI_GUEST_INFORMATION_UPDATE_EVENT, data);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating guest information ", e);
      }
   }

   private enum Command
   {
      GI,
      GC,
      GO,
      BILL,
      XL,
      LA,
      LS,
      XI,
      XB,
      XC,
      XT,
      PA,
      LE,
      novalue;

      public static Command fromString(String Str)
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

   private String parseIso8601DateTime(String isoDate)
   {
      String dateTime = "";

      try
      {
         DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         Date result1 = df1.parse(isoDate);

         dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(result1);

         dvLogger.info("Date-Time" + dateTime);
      }
      catch (ParseException e)
      {
         dvLogger.error("Error while parsing the ISO8601 date to DateTime\n",
                  e);
      }

      return dateTime;
   }

   private String[] parseIso8601ToDateAndTimeSeparate(String isoDate)
   {
      String dateTime[] = new String[2];

      try
      {
         DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         Date result1 = df1.parse(isoDate);

         String date = new SimpleDateFormat("yyyy-MM-dd").format(result1);

         String time = new SimpleDateFormat("HH:mm:ss").format(result1);

         dvLogger.info("Date: " + date);
         dvLogger.info("Time: " + time);

         dateTime[0] = date;
         dateTime[1] = time;
      }
      catch (ParseException e)
      {
         dvLogger.error(
                  "Error while parsing the ISO8601 date to Date and Time separately\n",
                  e);
      }
      return dateTime;
   }

   private void getMappingAndProcessEvent(MewsMasterReservationData data)
   {
      dvLogger.info("Starting to update the Key Status through PMS SYNC...");

      try
      {
         for (MewsCustomerData customer : data.getCustomers())
         {
            if (dvPmsDatabase.fetchUniqueIdByGuestId(customer.getId()))
            {
               customersMap.put(customer.getId(), customer);
            }
         }

         for (MewsReservationData reservation : data.getReservations())
         {
            if (dvPmsDatabase
                     .fetchUniqueIdByGuestId(reservation.getCustomerId()))
            {

               String arrivalDate = reservation.getStartUtc();
               String departureDate = reservation.getEndUtc();
               String spaceId = reservation.getAssignedSpaceId();
               String reservationId = reservation.getId();
               String groupCode = reservation.getGroupId();

               boolean primaryCheckinOut = false;
               boolean secondaryCheckinOut = false;

               if (reservation.getState()
                        .equalsIgnoreCase(EventState.STARTED.toString()))
               {
                  for (MewsCustomerData customer : data.getCustomers())
                  {
                     if (customer.getId()
                              .equalsIgnoreCase(reservation.getCustomerId()))
                     {
                        try
                        {
                           String event = "";

                           Map<String, String> dataMap =
                                    parseCustomerData(customer);

                           dataMap.put("GS", GuestData.PRIMARY.toString());

                           dataMap.put("GuestId", customer.getId());
                           dataMap.put("GA", arrivalDate);
                           dataMap.put("GD", departureDate);
                           dataMap.put("RN", (spaceId));
                           dataMap.put("ReservationId", reservationId);
                           dataMap.put("GroupCode", groupCode);
                           
                           
                           dataMap.put("LanguageCode",customer.getLanguageCode());
                           dataMap.put("FirstName", customer.getFirstName());
                           dataMap.put("LastName",customer.getLastName());
                           dataMap.put("Email", customer.getEmail());
                           dataMap.put("Title", customer.getTitle());
                           dataMap.put("BirthDate", customer.getBirthDate());
                           
                           

                           dataMap.put("SF", "true");          // Safe Checkin

                           dvLogger.info("Prepared Guest Data Map: " + dataMap);

                           if (reservation.getState().equalsIgnoreCase(
                                    EventState.STARTED.toString())
                                    && dvPmsDatabase.validateReservationIdExist(
                                             reservationId,(spaceId)))
                           {
                              event = "GC";
                           }
                           else if (reservation.getState().equalsIgnoreCase(
                                    EventState.STARTED.toString())
                                    && dvPmsDatabase.validateIsNeedToCheckin(
                                             (spaceId),
                                             customer.getId()))
                           {
                              event = "GI";
                           }
                           else if (reservation.getState().equalsIgnoreCase(
                                    EventState.PROCESSED.toString()))
                           {
                              event = "GO";
                           }


                           switch (Command.fromString(event))
                           {
                              case GI:
                                 checkin(dataMap);
                                 break;
                              case GC:
                                 roomChange(dataMap);
                                 break;
                              case GO:
                                 checkout(dataMap);
                                 break;
                              default:
                                 dvLogger.info("No event to be performed");
                           }

                           primaryCheckinOut = true;
                        }
                        catch (Exception e)
                        {
                           dvLogger.error(
                                    "Exception while parsing different guest data\n",
                                    e);
                        }
                     }

                     /*
                      * if (!customer.getId()
                      * .equalsIgnoreCase(reservation.getCustomerId()) &&
                      * !secondaryCheckinOut) { String event = "";
                      * 
                      * try { Map<String, String> dataMap =
                      * parseCustomerData(customer);
                      * 
                      * dataMap.put("GS", GuestData.SECONDARY.toString());
                      * 
                      * dataMap.put("GuestId", customer.getId());
                      * dataMap.put("GA", arrivalDate); dataMap.put("GD",
                      * departureDate); dataMap.put("RN", (spaceId));
                      * dataMap.put("ReservationId", reservationId);
                      * dataMap.put("GroupCode", groupCode);
                      * 
                      * dataMap.put("SF", "true"); // Safe Checkin
                      * 
                      * dvLogger.info("Prepared Guest Data Map: " + dataMap);
                      * 
                      * if (reservation.getState().equalsIgnoreCase(
                      * EventState.STARTED.toString()) &&
                      * dvPmsDatabase.validateReservationIdExist( reservationId,
                      * (spaceId))) { event = "GC"; } else if
                      * (reservation.getState().equalsIgnoreCase(
                      * EventState.STARTED.toString()) &&
                      * dvPmsDatabase.validateIsNeedToCheckin( (spaceId),
                      * customer.getId())) { event = "GI"; } else if
                      * (reservation.getState().equalsIgnoreCase(
                      * EventState.PROCESSED.toString())) { event = "GO"; }
                      * 
                      * 
                      * switch (Command.fromString(event)) { case GI:
                      * checkin(dataMap); break; case GC: roomChange(dataMap);
                      * break; case GO: checkout(dataMap); break; default:
                      * dvLogger.info("No event to be performed"); }
                      * 
                      * secondaryCheckinOut = true; } catch (Exception e) {
                      * dvLogger.error(
                      * "Exception while parsing different guest data\n", e); }
                      * }
                      */

                     if (primaryCheckinOut && secondaryCheckinOut)
                     {
                        break;
                     }
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while setting map from object\n", e);
      }
   }

   public boolean needToUpdateGuestInfo(MewsCustomerData customerData)
   {
      boolean isNeedToUpdate = false;

      try
      {
         String guestId = customerData.getId();

         /*
          * String guestId, String guestName, String guestTitle, String
          * guestFirstName, String guestLastName, String guestFullName, String
          * guestLanguage, String emailId, String phoneNumber, String groupCode,
          * String dateOfBirth, String nationality, String departureDate, String
          * arrivalDate, boolean revisitFlag, boolean vipStatus
          */

         isNeedToUpdate = dvPmsDatabase.isNeedToUpdateGuestInfo(
                  customerData.getId(), "", customerData.getTitle(),
                  customerData.getFirstName(), customerData.getLastName(), "",
                  customerData.getLanguageCode(), customerData.getEmail(),
                  customerData.getPhone(), "", customerData.getBirthDate(),
                  customerData.getNationalityCode(), "", "", false, false);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while checkin, need to update guest info\n",
                  e);
      }

      return isNeedToUpdate;
   }
}
