package com.digivalet.pmsi.opera;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsiGuestTypes;
import com.digivalet.core.DVPmsiStatus;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVPMSMessageData;
import com.digivalet.pmsi.datatypes.DVPmsBillData;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.events.DVBillEvent.BillFeatureEventType;
import com.digivalet.pmsi.events.DVEvent.FeatureEventType;
import com.digivalet.pmsi.events.DVMessageEvent.MessageFeatureEvent;

public class DVParseData implements Runnable
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private String data;
   private DVPmsOpera dvPmsOpera;
   private DVPmsDatabase dvPmsDatabase;
   private final int SLEEP_BETWEEN_OLDCHECKOUT_NEWCHECKIN = 3000;
   private final int GUESTINFORMATIONUPDATEDELAY = 1000;

   public DVParseData(String data, DVPmsOpera dvPmsOpera,
            DVPmsDatabase dvPmsDatabase)
   {
      this.dvPmsOpera = dvPmsOpera;
      this.data = data;
      this.dvPmsDatabase = dvPmsDatabase;
   }

   @Override
   public void run()
   {
      try
      {
         Thread.currentThread().setName("OPERA_PARSE_READDATA");
         String event = "";
         HashMap<String, String> dataMap = parseRequest(data);
         event = (String) dataMap.get("event");

         switch (Command.fromString(event))
         {
            case GI:
               checkin(dataMap);
               break;
            case GC:
               if (dataMap.containsKey("RO"))
               {
                  roomChange(dataMap);
               }
               else
               {
                  GuestInformationUpdate(dataMap);
               }
               break;
            case GO:
               checkout(dataMap);
               break;
            case LA:
               break;
            case LS:
               break;
            case XI:
               parseBillItem(dataMap);
               break;
            case XB:
               sendBillData(dataMap);
               break;
            case XC:
               parseCheckoutResponse(dataMap);
               break;
            case XL:
            case XT:
               String guestid, roomno, mid, mtext, date, time;
               if ((guestid = (String) dataMap.get("G#")) != null) ;
               if ((roomno = (String) dataMap.get("RN")) != null) ;
               if ((mid = (String) dataMap.get("MI")) != null) ;
               if ((mtext = (String) dataMap.get("MT")) != null)
               {
               }

               if ((date = (String) dataMap.get("DA")) != null)
               {
               }
               if ((time = (String) dataMap.get("TI")) != null)
               {
               }

               Map<DVPMSMessageData, Object> messageData =
                        new HashMap<DVPMSMessageData, Object>();

               messageData.put(DVPMSMessageData.guestId, guestid);
               messageData.put(DVPMSMessageData.short_description, mtext);
               messageData.put(DVPMSMessageData.key_id, roomno);
               messageData.put(DVPMSMessageData.title, mtext);
               messageData.put(DVPMSMessageData.start_date, date);
               messageData.put(DVPMSMessageData.sent_time, time);
               messageData.put(DVPMSMessageData.until_date, time);
               // dvPmsOpera.notifyMessageEvent(mtext, mtext, date, time, time);
               dvPmsOpera.notifyMessageEvents(
                        MessageFeatureEvent.PMSI_MESSAGE_EVENT, messageData);
               // dvPmsOpera.notifyPmsBillEvents(BillFeatureEventType.PMSI_BILL_EVENT,
               // billData);
               break;
            case PA:
               break;
            case LE:

               break;
            case DS:
               dvLogger.info("     Data Sync Start   ");
               dvPmsOpera.syncInProgress = true;
               break;
            case DE:
               dvLogger.info("     Data Sync End    ");
               processSyncRecords();
               dvPmsOpera.syncInProgress = false;
               break;
            default:
               break;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in parsing data from PMS ", e);
      }

   }

   private void processSyncRecords()
   {
      try
      {
         try
         {
            dvLogger.info("Processing sync records ");
            if (!dvPmsOpera.syncCheckinRecords.isEmpty())
            {
               Set<String> checkinRecords =
                        dvPmsOpera.syncCheckinRecords.keySet();
               for (String checkinRecord : checkinRecords)
               {
                  try
                  {
                     dvLogger.info("checkinRecord:  " + checkinRecord);
                     Map<DVPmsData, Object> data =
                              dvPmsOpera.syncCheckinRecords.get(checkinRecord);
                     dvLogger.info("data:  " + data.toString());
                     String roomnumber = data.get(DVPmsData.keyId).toString();
                     int count = dvPmsOpera.roomGuestCount.get(roomnumber);
                     if (count == 1)
                     {
                        dvLogger.info(
                                 "In Sync single guest is checked-in in this key so removing the secodnary guest from this key ");
                        int key = dvPmsDatabase.getKeyId(roomnumber);
                        int pmsiGuestID =
                                 dvPmsDatabase.getPmsiGuestIdFromKeyStatus(key,
                                          DVPmsiGuestTypes.secondary
                                                   .toString());
                        if (pmsiGuestID != 0)
                        {
                           dvPmsDatabase.updateKeyStatus(
                                    DVPmsiStatus.checkout.toString(),
                                    DVPmsiStatus.PENDING_CHECKOUT.toString(),
                                    key, DVPmsiGuestTypes.secondary.toString(),
                                    pmsiGuestID);
                           dvPmsDatabase.deleteGuestId(key,
                                    DVPmsiGuestTypes.secondary.toString());

                           dvPmsDatabase.updateKeyStatusDigivaletStatus(
                                    (DVPmsiGuestTypes.secondary).toString(),
                                    key,
                                    dvPmsDatabase.getMasterStatusId(
                                             DVPmsiStatus.PENDING_CHECKOUT
                                                      .toString()));
                        }
                     }
                     dvPmsOpera.notifyPmsEvent(
                              FeatureEventType.PMSI_SAFE_CHECKIN_EVENT, data);

                  }
                  catch (Exception e)
                  {
                     dvLogger.error("Error in sending checkin event ", e);

                  }
               }
            }
            dvPmsOpera.syncCheckinRecords.clear();
         }
         catch (Exception e)
         {
            dvLogger.error("Error in checkin records ", e);
         }

         try
         {
            Iterator<Map<DVPmsData, Object>> iterator =
                     dvPmsOpera.syncCheckoutRecords.iterator();
            while (iterator.hasNext())
            {
               Map<DVPmsData, Object> data = iterator.next();

               String roomNumber = data.get(DVPmsData.keyId).toString();
               int keyId = dvPmsDatabase.getKeyId(roomNumber);
               String guestType = data.get(DVPmsData.guestType).toString();
               if (!dvPmsDatabase.checkKeyPmsiCheckedInStatus(keyId, guestType))
               {
                  iterator.remove();
                  dvLogger.info(
                           "Removing this record as this is already checked out in digivalet "
                                    + roomNumber);
               }
            }
            for (Map<DVPmsData, Object> syncCheckoutRecord : dvPmsOpera.syncCheckoutRecords)
            {
               dvPmsOpera.notifyPmsEvent(
                        FeatureEventType.PMSI_SAFE_CHECKOUT_EVENT,
                        syncCheckoutRecord);
            }
            dvPmsOpera.syncCheckoutRecords.clear();
            dvPmsOpera.roomGuestCount.clear();
         }
         catch (Exception e)
         {
            dvLogger.error("Error in removing already checked out record ", e);
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing sync records ", e);
      }
   }

   private void parseCheckoutResponse(HashMap<String, String> dataMap)
   {
      try
      {
         // XC|RN06008|G#5182531|DA190416|TI182348|ASCD|CTCheckout date is not
         // today.|
         String guestid, roomno, answer, date, time, text;
         if ((guestid = (String) dataMap.get("G#")) != null) ;
         if ((roomno = (String) dataMap.get("RN")) != null) ;
         if ((answer = (String) dataMap.get("AS")) != null) ;
         if ((date = (String) dataMap.get("DA")) != null) ;
         if ((time = (String) dataMap.get("TI")) != null) ;
         if ((text = (String) dataMap.get("CT")) != null) ;
         if (!answer.equalsIgnoreCase("OK"))
         {
            if (null == text || "".equalsIgnoreCase(text))
            {
               text = "Something went wrong";
            }
            dvPmsOpera.notifyCheckoutFailEvent(text, roomno, guestid);
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in parsing checkout response ", e);
      }

   }

   private void sendBillData(HashMap<String, String> dataMap)
   {
      try
      {
         String guestid, roomno, balance, date, time, sharestatus = "primary",
                  windowDetail, balance_int = "0";
         if ((guestid = (String) dataMap.get("G#")) != null) ;
         if ((roomno = (String) dataMap.get("RN")) != null) ;
         if ((balance = (String) dataMap.get("BA")) != null) ;
         if ((date = (String) dataMap.get("DA")) != null) ;
         if ((time = (String) dataMap.get("TI")) != null) ;
         Thread.currentThread().setName("OPERA BILLDATA " + roomno);
         String amount = "";
         amount = balance;
         if (balance == null || balance.equalsIgnoreCase("null"))
         {
            balance = "0.00";
            amount = balance;
         }
         else
         {
            DecimalFormat df = new DecimalFormat("###,##,##,##0.00");
            balance = "" + df.format(Double.parseDouble(balance) / 100);
            dvLogger.info("Bill Balance in decimal: " + balance);
         }
         if (balance_int == null || balance_int.equalsIgnoreCase("null"))
         {
            balance_int = "0.00";
         }

         dvPmsDatabase.insertGuestBillAmount(guestid, roomno, date, time,
                  amount);

         Map<DVPmsBillData, Object> billData =
                  new HashMap<DVPmsBillData, Object>();
         billData.put(DVPmsBillData.balance, balance);
         billData.put(DVPmsBillData.date, date);
         billData.put(DVPmsBillData.guestId, guestid);
         billData.put(DVPmsBillData.keyId, roomno);
         billData.put(DVPmsBillData.time, time);

         dvPmsOpera.notifyPmsBillEvents(BillFeatureEventType.PMSI_BILL_EVENT,
                  billData);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in Sending Bill data ", e);
      }

   }

   private void parseBillItem(HashMap<String, String> dataMap)
   {
      try
      {
         String guestid, roomno, itemDesciption, itemAmmount, itemDisplay, date,
                  time, folioNO;
         if ((guestid = (String) dataMap.get("G#")) != null) ;
         if ((roomno = (String) dataMap.get("RN")) != null) ;
         if ((itemDesciption = (String) dataMap.get("BD")) != null) ;
         if ((itemAmmount = (String) dataMap.get("BI")) != null) ;
         if ((itemDisplay = (String) dataMap.get("FD")) != null) ;
         if ((date = (String) dataMap.get("DA")) != null) ;
         if ((time = (String) dataMap.get("TI")) != null) ;
         if ((folioNO = (String) dataMap.get("F#")) != null) ;
         Thread.currentThread().setName("OPERA BILLITEM " + roomno);

         if (itemDisplay == null || itemDisplay.equalsIgnoreCase("Y"))
         {
            double itemAmt = Double.parseDouble(itemAmmount) / 100;
            dvLogger.info("guestid " + guestid);
            dvLogger.info("roomno " + roomno);
            dvLogger.info("itemDesciption " + itemDesciption);
            dvLogger.info("itemAmt " + itemAmt);
            dvLogger.info("itemDisplay " + itemDisplay);
            dvLogger.info("date " + date);
            dvLogger.info("time " + time);
            dvLogger.info("folioNO " + folioNO);
            dvPmsDatabase.insertGuestItemData(guestid, roomno, itemDesciption,
                     itemAmt, itemDisplay, date, time, folioNO);
         }
         else
         {
            dvLogger.info("Item is not Visible on folio display ");
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in parding bill Items ", e);
      }
   }

   private enum Command
   {

      GI,
      GC,
      GO,
      XL,
      LA,
      LS,
      XI,
      XB,
      XC,
      XT,
      PA,
      LE,
      DS,
      DE,
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
   };

   public void roomChange(HashMap<String, String> dataMap)
   {
      try
      {

         String tvrights = "TU", roomno, guestID, guestSF, guestSFO,
                  guestArrivalDate, guestDepartureDate, guestfirstname,
                  guestFullname = "", salutation, guestvipstatus = "na",
                  guestName = "", guestLan = "", authorization = "false",
                  groupCode = "NA", uniqeKeyOldRoom = "NA",
                  newsppracceptat = null, reservationno = "NA",
                  videorights = "NA", containSF = "false";;
         if ((roomno = (String) dataMap.get("RN")) != null) ;
         if ((guestID = (String) dataMap.get("G#")) != null) ;
         if ((guestSF = (String) dataMap.get("GS")) != null) ;
         if ((guestSFO = (String) dataMap.get("GSO")) != null) ;
         if ((guestName = (String) dataMap.get("GN")) != null) ;
         if ((guestLan = (String) dataMap.get("GL")) != null) ;
         if ((tvrights = (String) dataMap.get("TV")) != null) ;
         if ((guestArrivalDate = (String) dataMap.get("GA")) != null) ;
         if ((guestDepartureDate = (String) dataMap.get("GD")) != null) ;
         if ((guestfirstname = (String) dataMap.get("GF")) != null) ;
         if ((videorights = (String) dataMap.get("VR")) != null) ;
         if ((salutation = (String) dataMap.get("GT")) != null) ;
         if ((guestvipstatus = (String) dataMap.get("GV")) != null) ;
         String oldRoom = (String) dataMap.get("RO");
         if ((containSF = (String) dataMap.get("SF")) != null) ;
         Thread.currentThread()
                  .setName("OPERA ROOMCHANGE " + oldRoom + " " + roomno);
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
         if (null == salutation)
         {
            salutation = "";
         }
         if (null == guestName)
         {
            guestName = "";
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

         Map<DVPmsData, Object> data = new HashMap<>();
         Map<DVPmsData, Object> data2 = new HashMap<>();
         ConvertKeyData(data, dataMap);
         ConvertKeyData(data2, dataMap);

         if (null != guestArrivalDate)
         {
            data.put(DVPmsData.arrivalDate, guestArrivalDate);
            data2.put(DVPmsData.arrivalDate, guestArrivalDate);
         }
         if (null != guestDepartureDate)
         {
            data.put(DVPmsData.departureDate, guestDepartureDate);
            data2.put(DVPmsData.departureDate, guestDepartureDate);
         }
         data.put(DVPmsData.guestFirstName, guestfirstname);
         data2.put(DVPmsData.guestFirstName, guestfirstname);
         data.put(DVPmsData.guestId, guestID);
         data2.put(DVPmsData.guestId, guestID);
         data.put(DVPmsData.guestLanguage, guestLan);
         data2.put(DVPmsData.guestLanguage, guestLan);
         data.put(DVPmsData.guestTitle, salutation);
         data2.put(DVPmsData.guestTitle, salutation);

         data.put(DVPmsData.guestFirstName, guestfirstname);
         data2.put(DVPmsData.guestFirstName, guestfirstname);
         data.put(DVPmsData.guestName, guestName);
         data.put(DVPmsData.guestLastName, guestName);
         data2.put(DVPmsData.guestName, guestName);
         data2.put(DVPmsData.guestLastName, guestName);
         data.put(DVPmsData.remoteCheckout, authorization);
         data2.put(DVPmsData.remoteCheckout, authorization);
         data2.put(DVPmsData.temperature, "");
         data2.put(DVPmsData.welcomeMoodId, "");
         data.put(DVPmsData.fragrance, "");

         if (null != containSF)
         {
            data.put(DVPmsData.safeFlag, containSF);
            data2.put(DVPmsData.safeFlag, containSF);
         }
         if (null != tvrights)
         {
            data.put(DVPmsData.tvRights, tvrights);
            data2.put(DVPmsData.tvRights, tvrights);
         }
         if (null != videorights)
         {
            data.put(DVPmsData.videoRights, videorights);
            data2.put(DVPmsData.videoRights, videorights);
         }
         if (null != guestvipstatus)
         {
            data.put(DVPmsData.vipStatus, guestvipstatus);
            data2.put(DVPmsData.vipStatus, guestvipstatus);
         }

         data = ConvertValuedata(data);
         data2 = ConvertValuedata(data2);
         dvLogger.info("oldRoom  " + oldRoom + "  new Room " + roomno);
         dvLogger.info(
                  "guestSFO  " + guestSFO + "  new Room guestSF " + guestSF);
         data.put(DVPmsData.guestType, guestSFO);
         data.put(DVPmsData.keyId, oldRoom);
         data = checkRevisitFlag(data);
         data2 = checkRevisitFlag(data2);
         dvLogger.info("data at parse " + data.toString());
         dvPmsOpera.notifyPmsEvent(
                  FeatureEventType.PMSI_ROOMCHANGE_CHECKOUT_EVENT, data);
         // send checkout to old

         Thread.sleep(SLEEP_BETWEEN_OLDCHECKOUT_NEWCHECKIN);

         data2.put(DVPmsData.guestType, guestSF);
         data2.put(DVPmsData.keyId, roomno);
         data2.put(DVPmsData.oldRoom, oldRoom);

         dvLogger.info("data at parse " + data2.toString());

         dvPmsOpera.notifyPmsEvent(
                  FeatureEventType.PMSI_ROOMCHANGE_CHECKIN_EVENT, data2);
         // send checkin to new
      }
      catch (Exception e)
      {
         dvLogger.error("Error in Room Change ", e);
      }

   }

   private Map<DVPmsData, Object> checkRevisitFlag(Map<DVPmsData, Object> data2)
   {
      try
      {
         String previousVisitDate =
                  data2.get(DVPmsData.previousVisitDate).toString();
         if (null != previousVisitDate
                  && !"".equalsIgnoreCase(previousVisitDate)
                  && previousVisitDate.length() > 0)
         {
            data2.put(DVPmsData.revisitFlag, "true");
            return data2;
         }
         else
         {
            data2.put(DVPmsData.revisitFlag, "false");
            return data2;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in checking revisit flag ", e);
      }
      return data2;
   }

   public void GuestInformationUpdate(HashMap<String, String> dataMap)
   {
      try
      {
         String tvrights = "TU", oldRoom, roomno, guestID, guestSF, guestSFO,
                  guestArrivalDate, guestDepartureDate, guestfirstname,
                  guestFullname = "", salutation, guestvipstatus = "na",
                  guestName = "", guestLan = "", authorization = "false",
                  groupCode = "NA", videorights = "NA", containSF = "false";
         if ((roomno = (String) dataMap.get("RN")) != null) ;
         if ((guestID = (String) dataMap.get("G#")) != null) ;
         if ((guestSF = (String) dataMap.get("GS")) != null) ;
         if ((guestSFO = (String) dataMap.get("GSO")) != null) ;
         if ((guestName = (String) dataMap.get("GN")) != null) ;
         if ((guestLan = (String) dataMap.get("GL")) != null) ;
         if ((tvrights = (String) dataMap.get("TV")) != null) ;
         if ((guestArrivalDate = (String) dataMap.get("GA")) != null) ;
         if ((guestDepartureDate = (String) dataMap.get("GD")) != null) ;
         if ((guestfirstname = (String) dataMap.get("GF")) != null) ;
         if ((videorights = (String) dataMap.get("VR")) != null) ;
         if ((salutation = (String) dataMap.get("GT")) != null) ;
         if ((guestvipstatus = (String) dataMap.get("GV")) != null) ;
         if ((containSF = (String) dataMap.get("SF")) != null) ;
         Thread.currentThread()
                  .setName("OPERA GUESTINFORMATION CHANGE " + roomno);
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

         Map<DVPmsData, Object> data = new HashMap<DVPmsData, Object>();
         if (null != guestArrivalDate)
         {
            data.put(DVPmsData.arrivalDate, guestArrivalDate);
         }
         if (null != guestDepartureDate)
         {
            data.put(DVPmsData.departureDate, guestDepartureDate);
         }
         data.put(DVPmsData.guestFirstName, guestfirstname);
         data.put(DVPmsData.guestId, guestID);
         data.put(DVPmsData.guestLanguage, guestLan);
         data.put(DVPmsData.guestTitle, salutation);

         data.put(DVPmsData.guestFirstName, guestfirstname);
         data.put(DVPmsData.guestName, guestName);
         data.put(DVPmsData.guestLastName, guestName);
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
         ConvertKeyData(data, dataMap);
         data = ConvertValuedata(data);
         data = checkRevisitFlag(data);
         Thread.sleep(GUESTINFORMATIONUPDATEDELAY);
         dvPmsOpera.notifyPmsEvent(
                  FeatureEventType.PMSI_GUEST_INFORMATION_UPDATE_EVENT, data);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating guest information ", e);
      }
   }

   public void checkout(HashMap<String, String> dataMap)
   {
      try
      {
         String containSF, roomno, guestID, guestSF;
         if ((containSF = (String) dataMap.get("SF")) != null) ;
         if ((roomno = (String) dataMap.get("RN")) != null) ;
         if ((guestID = (String) dataMap.get("G#")) != null) ;
         if ((guestSF = (String) dataMap.get("GS")) != null) ;
         Thread.currentThread().setName("OPERA CHECKOUT " + roomno);
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
         Map<DVPmsData, Object> data = new HashMap<DVPmsData, Object>();
         data.put(DVPmsData.keyId, roomno);
         if (null != containSF)
         {
            data.put(DVPmsData.safeFlag, containSF);
         }
         data.put(DVPmsData.guestId, guestID);
         data.put(DVPmsData.guestType, guestSF);
         ConvertKeyData(data, dataMap);
         data = ConvertValuedata(data);
         if (dvPmsOpera.syncInProgress)
         {
            dvPmsOpera.syncCheckoutRecords.add(data);
         }
         else
         {
            dvPmsOpera.notifyPmsEvent(FeatureEventType.PMSI_CHECKOUT_EVENT,
                     data);
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in parsing checkout ", e);
      }
   }

   public void checkin(HashMap<String, String> dataMap)
   {
      try
      {
         String guestName, guestLan = "EN", containSF, roomno, guestID,
                  guestSF = "N", authorization = "false", guestArrivalDate,
                  guestDepartureDate, guestfirstname, salutation,
                  guestvipstatus, groupCode = "NA", uniqeKey = "NA",
                  tvrights = "TU", reservationno = "NA", videorights = "NA";
         if ((guestName = (String) dataMap.get("GN")) != null) ;
         if ((guestLan = (String) dataMap.get("GL")) != null) ;
         if ((containSF = (String) dataMap.get("SF")) != null) ;
         if ((roomno = (String) dataMap.get("RN")) != null) ;
         if ((guestID = (String) dataMap.get("G#")) != null) ;
         if ((guestSF = (String) dataMap.get("GS")) != null) ;
         if ((tvrights = (String) dataMap.get("TV")) != null) ;
         if ((guestArrivalDate = (String) dataMap.get("GA")) != null) ;
         if ((guestDepartureDate = (String) dataMap.get("GD")) != null) ;
         if ((guestfirstname = (String) dataMap.get("GF")) != null) ;
         if ((videorights = (String) dataMap.get("VR")) != null) ;
         if ((salutation = (String) dataMap.get("GT")) != null) ;
         if ((guestvipstatus = (String) dataMap.get("GV")) != null) ;
         Thread.currentThread().setName("OPERA CHECKIN " + roomno);
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
         dvLogger.info("safeFlag " + containSF);
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

         if (null != containSF)
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
         Map<DVPmsData, Object> data = new HashMap<>();
         if (null != guestArrivalDate)
         {
            data.put(DVPmsData.arrivalDate, guestArrivalDate);
         }
         if (null != guestDepartureDate)
         {
            data.put(DVPmsData.departureDate, guestDepartureDate);
         }
         if (null == salutation)
         {
            salutation = "";
         }
         if (null == guestName)
         {
            guestName = "";
         }
         data.put(DVPmsData.guestFirstName, guestfirstname);
         data.put(DVPmsData.guestLastName, guestName);
         data.put(DVPmsData.remoteCheckout, authorization);
         data.put(DVPmsData.guestId, guestID);
         data.put(DVPmsData.guestLanguage, guestLan);
         data.put(DVPmsData.guestTitle, salutation);
         data.put(DVPmsData.guestType, guestSF);
         data.put(DVPmsData.guestFirstName, guestfirstname);
         data.put(DVPmsData.guestName, guestName);
         data.put(DVPmsData.guestLastName, guestName);
         data.put(DVPmsData.keyId, roomno);
         data.put(DVPmsData.temperature, "");
         data.put(DVPmsData.welcomeMoodId, "");
         data.put(DVPmsData.fragrance, "");

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
         ConvertKeyData(data, dataMap);
         data = ConvertValuedata(data);
         data = checkRevisitFlag(data);
         if (dvPmsOpera.syncInProgress)
         {
            if (!dvPmsOpera.roomGuestCount.containsKey(roomno))
            {
               dvPmsOpera.roomGuestCount.put(roomno, Integer.valueOf(1));
               data.put(DVPmsData.guestType, "primary");
            }
            else
            {
               int guestCount = dvPmsOpera.roomGuestCount.get(roomno);
               dvLogger.info("guestCount  : " + guestCount);
               guestCount = guestCount + 1;
               dvPmsOpera.roomGuestCount.put(roomno,
                        Integer.valueOf(guestCount));
               if (guestCount == 2)
               {
                  data.put(DVPmsData.guestType, "secondary");
               }
               else if (guestCount > 2)
               {
                  data.put(DVPmsData.guestType, "secondary");
                  removePreviousSecondary(roomno);
               }
            }
            dvPmsOpera.syncCheckinRecords.put(guestID, data);
         }
         else
         {
            dvPmsOpera.notifyPmsEvent(FeatureEventType.PMSI_CHECKIN_EVENT,
                     data);
         }


      }
      catch (Exception e)
      {
         dvLogger.error("Error in sendin checkin ", e);
      }
   }

   private void removePreviousSecondary(String roomno)
   {
      try
      {
         if (!dvPmsOpera.syncCheckinRecords.isEmpty())
         {
            Iterator<String> iterator =
                     dvPmsOpera.syncCheckinRecords.keySet().iterator();

            while (iterator.hasNext())
            {
               String checkinRecord = iterator.next();

               Map<DVPmsData, Object> data =
                        dvPmsOpera.syncCheckinRecords.get(checkinRecord);
               String roomNumber = data.get(DVPmsData.keyId).toString();
               String guestType = data.get(DVPmsData.guestType).toString();
               if (roomNumber.equalsIgnoreCase(roomno)
                        && guestType.equalsIgnoreCase(
                                 DVPmsiGuestTypes.secondary.toString()))
               {
                  iterator.remove();
                  dvLogger.info("Removing this record as this is secondary ");
               }
            }



         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in removing secondary ", e);
      }

   }

   public HashMap<String, String> parseRequest(String pmsCommand)
   {
      HashMap<String, String> dataMap = new HashMap<String, String>();
      try
      {
         int i = 0;
         pmsCommand.trim();
         if (pmsCommand.endsWith("|"))
         {
            pmsCommand = pmsCommand.substring(0, pmsCommand.length() - 1);
         }
         boolean check = false;
         String data[] = pmsCommand.split("\\|");
         dataMap.put("event", data[0].trim());
         for (i = 1; i < data.length - 1; i++)
         {
            dataMap.put(data[i].substring(0, 2).trim(),
                     data[i].substring(2, data[i].length()).trim());
            if (data[i].startsWith("RO"))
            {
               i = i + 1;
               check = true;
               dataMap.put(data[i].substring(0, 2).trim() + "O",
                        data[i].substring(2, data[i].length()).trim());
               dvLogger.info(
                        "putting in : " + data[i].substring(0, 2).trim() + "O");
            }
         }
         if (check == true)
         {
            i = i - 1;
         }
         if (data[i].startsWith("SF"))
         {
            dataMap.put(data[i].trim(), "True");
         }
         else
         {
            if (dataMap.get("GS") == null || !data[i].startsWith("GS"))
            {
               dataMap.put(data[i].substring(0, 2).trim(),
                        data[i].substring(2, data[i].length()).trim());
            }
            dataMap.put("SF", "False");
         }
         return dataMap;
      }
      catch (Exception e)
      {
         dvLogger.error("error in parse req ", e);
         e.printStackTrace();
      }
      return dataMap;
   }

   public Map<DVPmsData, Object> ConvertKeyData(Map<DVPmsData, Object> data,
            HashMap<String, String> dataMap)
   {
      Set<String> keyNames = dataMap.keySet();
      for (String keyName : keyNames)
      {
         if (dvPmsOpera.PmsKeyMappingDetails.containsKey(keyName))
         {
            DVPmsData mappedvalue =
                     dvPmsOpera.PmsKeyMappingDetails.get(keyName);
            data.put(mappedvalue, dataMap.get(keyName));
            dvLogger.info("Pmsi Key updated to " + mappedvalue
                     + " from pmsi key " + keyName);
         }
      }
      return data;

   }

   public Map<DVPmsData, Object> ConvertValuedata(Map<DVPmsData, Object> data)
   {
      Set<DVPmsData> dataFeilds = data.keySet();
      for (DVPmsData keyName : dataFeilds)
      {
         HashMap<DVPmsData, String> keyValueData =
                  new HashMap<DVPmsData, String>();
         keyValueData.put(keyName, (String) data.get(keyName));
         if (dvPmsOpera.PmsValueMappingDetails.containsKey(keyValueData))
         {
            String mappedValue =
                     dvPmsOpera.PmsValueMappingDetails.get(keyValueData);
            data.put(keyName, mappedValue);
            dvLogger.info(
                     "Value for " + keyName + " changed to: " + mappedValue);
         }
      }
      data = insertDefaultData(data);
      return data;

   }

   public Map<DVPmsData, Object> insertDefaultData(Map<DVPmsData, Object> data)
   {
      try
      {
         for (DVPmsData dvPmsData : DVPmsData.values())
         {
            if (!data.containsKey(dvPmsData))
            {
               if (dvPmsOpera.DefaultDataMap.containsKey(dvPmsData))
               {
                  data.put(dvPmsData, dvPmsOpera.DefaultDataMap.get(dvPmsData));
                  dvLogger.info("Setting default value of feild " + dvPmsData
                           + " to " + dvPmsOpera.DefaultDataMap.get(dvPmsData));
               }
               else
               {
                  dvLogger.info("Setting default value of feild " + dvPmsData
                           + " to blank");
                  data.put(dvPmsData, "");
               }
            }
         }
      }
      catch (Exception e)
      {
         return data;
      }
      return data;
   }


}
