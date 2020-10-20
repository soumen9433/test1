package com.digivalet.pmsi.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVDeviceTypes;
import com.digivalet.pmsi.datatypes.DVPmsBill;
import com.digivalet.pmsi.datatypes.DVPmsBillData;
import com.digivalet.pmsi.settings.DVSettings;
import com.digivalet.pmsi.util.DVDataToController;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
 
public class DVSendPmsBill implements Runnable
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private Map<DVPmsBillData, Object> data = new HashMap<>();
   private Map<String, String> folioNameIdMapping = new HashMap<>();
   private DVSettings dvSettings;
   private DVBillEvent dvEvent;
   private DVPmsDatabase dvPmsDatabase;
   private ArrayList<Integer> InRoomDevices;
   private ArrayList<Integer> XplayerUi;
   private ArrayList<Integer> NonDvcInRoomDevices = new ArrayList<Integer>();
   private int keyId;
   private String currency="";
   private DVKeyCommunicationTokenManager communicationTokenManager;
   private String offlineBillDateFormat = "dd/MM/yyyy";
   
   public DVSendPmsBill(DVBillEvent dvEvent,
            DVSettings dvSettings,
            DVPmsDatabase dvPmsDatabase,DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.dvEvent = dvEvent;
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      this.communicationTokenManager=communicationTokenManager;
   }
   @Override
   public void run()
   {
      String updatedJson = "";
      try
      {
         Thread.currentThread().setName("BILL KEY:"+keyId);
         dvLogger.info("Inside bill Event started ");
         this.data = this.dvEvent.getBillData();
         this.currency=dvSettings.getCurrencySymbol();
         offlineBillDateFormat = dvSettings.getOfflineBillDateFormat();
         updateFolioNameMApping();
         keyId= dvPmsDatabase.getKeyId(data.get(DVPmsBillData.keyId).toString());
         int DvKeyId=dvPmsDatabase.getKeyId(data.get(DVPmsBillData.keyId).toString());
         String roomNumber=dvPmsDatabase.getDigivaletRoomNumber(DvKeyId);
         populateXplayerUi(DvKeyId);
         populateInRoomDevices(DvKeyId);
         populateNonDvcInRoomDevices(DvKeyId);
         String guestId= data.get(DVPmsBillData.guestId).toString();
         double total=dvPmsDatabase.getBillTotal(DvKeyId, guestId);
         String checkinTime=dvPmsDatabase.getArrivalTime(guestId, DvKeyId);
         String checkOutTime=dvPmsDatabase.getDepartureTime(guestId, DvKeyId);
         List<DVPmsBill> pmsBills=dvPmsDatabase.getBillData(guestId, DvKeyId);
         ArrayList<String> folios=dvPmsDatabase.getFolio(guestId, DvKeyId);
         boolean dummyBill=false;
         try
         {
            File f = new File("/digivalet/pkg/cache/bill.json");
            if(f.exists() && !f.isDirectory()) 
            {
               dummyBill=true;
            }																		
         }
         catch (Exception e)
         {
            dvLogger.error("Error in sending bill ", e);
         }
         String json="";
         if(dummyBill)
         {
            File file = new File("/digivalet/pkg/cache/bill.json"); 
            
            BufferedReader br = new BufferedReader(new FileReader(file)); 
            
            String st; 
            while ((st = br.readLine()) != null) 
            {
               json=json+st;
            } 
            br.close();
            Date date= new Date();
            long time = date.getTime();
            Timestamp checkinTimeStamp = new Timestamp(time);
            date.setTime(checkinTimeStamp.getTime());
            String formattedCheckinTime = new SimpleDateFormat(offlineBillDateFormat).format(date);

            
            
            Calendar c = Calendar.getInstance(); 
            c.setTime(date); 
            c.add(Calendar.DATE, 3);
            date = c.getTime();
            time = date.getTime();
            Timestamp checkoutTimeStamp = new Timestamp(time);
            date.setTime(checkoutTimeStamp.getTime());            
            String formattedCheckoutTime = new SimpleDateFormat(offlineBillDateFormat).format(date);

            
            json=json.replaceAll("%guestName", dvPmsDatabase.getGuestName(guestId, DvKeyId));
            json=json.replaceAll("%CheckinDate", formattedCheckinTime+"");
            json=json.replaceAll("%CheckoutDate", formattedCheckoutTime+"");
            json=json.replaceAll("%roomNumber", data.get(DVPmsBillData.keyId).toString());
            json=json.replaceAll("%timestamp", getDate() + "");
            
            for (int inRoomDevice : InRoomDevices)
            {
               updatedJson=json.replaceAll("%targetDeviceId", inRoomDevice+"");
               dvLogger.info(" Offline bill Json  "+updatedJson);
               
               ArrayList<Integer> dvcs=dvPmsDatabase.getDvcByKey(DvKeyId);
               for (int dvc: dvcs)
               {
                  SendBillToDevices billToDevices=new SendBillToDevices(dvPmsDatabase.getIp(dvc), updatedJson,dvc);
                  billToDevices.start();
               }
               for (int nonInRoomDevices: NonDvcInRoomDevices)
               {
                  SendBillToDevices billToDevices=new SendBillToDevices(dvPmsDatabase.getIp(nonInRoomDevices), updatedJson,nonInRoomDevices);
                  billToDevices.start();
               }
               dvPmsDatabase.deleteBill(guestId, DvKeyId);
            }

            
         }else
         {
            updatedJson=getBillJson(guestId,roomNumber,pmsBills,folios,checkinTime,checkOutTime,total,DvKeyId);
            dvLogger.info(" updatedJson: "+updatedJson);
         }
         
         if(!dummyBill)
         {
            ArrayList<Integer> dvcs=dvPmsDatabase.getDvcByKey(DvKeyId);
            dvLogger.info(" dvcs: "+dvcs.toString());
            for (int dvc: dvcs)
            {
               SendBillToDevices billToDevices=new SendBillToDevices(dvPmsDatabase.getIp(dvc), updatedJson,dvc);
               billToDevices.start();
            }
            for (int nonInRoomDevices: NonDvcInRoomDevices)
            {
               SendBillToDevices billToDevices=new SendBillToDevices(dvPmsDatabase.getIp(nonInRoomDevices), updatedJson,nonInRoomDevices);
               billToDevices.start();
            }
            dvPmsDatabase.deleteBill(guestId, DvKeyId);
         }

         
      }
      catch (Exception e)
      
      {
         dvLogger.error("Error in sending bill details ", e);
      }
   }

   private void updateFolioNameMApping()
   {
      try
      {
         String folioMap=dvSettings.getFolioIdNameMapping();
         for(int i=0;i<folioMap.split(",").length;i++)
         {
            String data=folioMap.split(",")[i];
            folioNameIdMapping.put(data.split("\\:")[0], data.split("\\:")[1]);   
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating floio mapping ", e);
      }
   }

   class SendBillToDevices extends Thread
   {
      String deviceIp;
      String jsonRequest;
      int dvcId;
      public SendBillToDevices(String deviceIp, String jsonRequest,int dvcId)
      {
         this.deviceIp=deviceIp;
         this.jsonRequest=jsonRequest;
         this.dvcId=dvcId;
      }
      public void run()
      {
         try
         {
            DVDataToController dataToController =
                     new DVDataToController(dvSettings,communicationTokenManager,keyId);
            boolean status=dataToController.SendData(deviceIp, jsonRequest,dvcId);
            dvLogger.info("Status of bill for ip "+deviceIp+" is "+status);
         }
         catch (Exception e)
         {
            dvLogger.error("Error in sending bill", e);
         }
      }
      
   }
   
   private String getBillJson(String guestId, String dvKeyId,
            List<DVPmsBill> pmsBills,ArrayList<String> folios, String checkinTime,
            String checkOutTime,double total,int Key)
   {
      try
      {
         JsonObject jsonRequest = new JsonObject();
         JsonObject jsonDetails = new JsonObject();
         JsonArray jsonArray = new JsonArray();
         JsonArray jsonResponseArray = new JsonArray();

         
         if(null!=folios && !folios.isEmpty())
         {
            for (String folio : folios)
            {
               JsonObject commonData = new JsonObject();

               if(null!=folioNameIdMapping && !folioNameIdMapping.isEmpty())
               {
                  if(folioNameIdMapping.containsKey(folio))
                  {
                     commonData.addProperty("windowName", folioNameIdMapping.get(folio));   
                  }else
                  {
                     commonData.addProperty("windowName", (folio));
                  }
                     
               }else
               {
                  commonData.addProperty("windowName", "Personal");
               }
               commonData.addProperty("guestName",
                        dvPmsDatabase.getGuestName(guestId,Key));
               commonData.addProperty("guestId",guestId+"");
               commonData.addProperty("room", dvKeyId);
               commonData.addProperty("currencySymbol", currency);
               commonData.addProperty("total",  new DecimalFormat("#.##").format(total)+"");
//               commonData.addProperty("total",  new DecimalFormat("###,##,##,##0.00").format(total)+"");

               JsonArray itemsArray = new JsonArray();
               JsonObject roomData = new JsonObject();
               roomData.addProperty("title", "Room");
               roomData.addProperty("date", getDate() + "");
               roomData.addProperty("value", dvKeyId);
               roomData.addProperty("type", "roomNumber");
               roomData.addProperty("itemMetaData", "");
               itemsArray.add(roomData);
               
               if (!checkinTime.equalsIgnoreCase(""))
               {
                  JsonObject checkin = new JsonObject();
                  checkin.addProperty("title", "Checkin Date");
                  checkin.addProperty("date", getDate() + "");
                  checkin.addProperty("value", checkinTime.substring(0, 10));
                  checkin.addProperty("type", "CheckinDate");
                  checkin.addProperty("itemMetaData", "");
                  itemsArray.add(checkin);
               }

               if (!checkOutTime.equalsIgnoreCase(""))
               {
                  JsonObject checkout = new JsonObject();
                  checkout.addProperty("title", "Checkout Date");
                  checkout.addProperty("date", getDate() + "");
                  checkout.addProperty("value", checkOutTime.substring(0, 10));
                  checkout.addProperty("type", "CheckoutDate");
                  checkout.addProperty("itemMetaData", "");
                  itemsArray.add(checkout);
               }


               for (DVPmsBill pmsBill : pmsBills)
               {
                  JsonObject itemdata1 = new JsonObject();
                  itemdata1.addProperty("title", pmsBill.getDescription());
                  itemdata1.addProperty("date",
                           pmsBill.getDate() + pmsBill.getTime());
                  itemdata1.addProperty("value", new DecimalFormat("#.##").format(pmsBill.getAmount())+"");
//                  itemdata1.addProperty("value", new DecimalFormat("###,##,##,##0.00").format(pmsBill.getAmount())+"");
                  itemdata1.addProperty("type", "amount");
                  itemdata1.addProperty("itemMetaData", "");
                  String tempFolio=pmsBill.getFolio();
                  if(tempFolio.equalsIgnoreCase(folio))
                  {
                     itemsArray.add(itemdata1);   
                  }
               }
               commonData.add("items", itemsArray);
               jsonArray.add(commonData);
            }

         }else
         {

            //start
            JsonObject commonData = new JsonObject();

            if(null!=dvSettings.getDefaultWindowName() && !"".equalsIgnoreCase(dvSettings.getDefaultWindowName()))
            {
               commonData.addProperty("windowName", dvSettings.getDefaultWindowName());   
            }else
            {
               commonData.addProperty("windowName", "Personal");
            }
            commonData.addProperty("guestName",
                     dvPmsDatabase.getGuestName(guestId,Key));
            commonData.addProperty("guestId",guestId+"");
            commonData.addProperty("room", dvKeyId);
            commonData.addProperty("currencySymbol", currency);
            commonData.addProperty("total",  new DecimalFormat("#.##").format(total)+"");
//            commonData.addProperty("total",  new DecimalFormat("###,##,##,##0.00").format(total)+"");

            
            
            JsonArray itemsArray = new JsonArray();
            JsonObject roomData = new JsonObject();
            roomData.addProperty("title", "Room");
            roomData.addProperty("date", getDate() + "");
            roomData.addProperty("value", dvKeyId);
            roomData.addProperty("type", "roomNumber");
            roomData.addProperty("itemMetaData", "");
            itemsArray.add(roomData);
            
            if (!checkinTime.equalsIgnoreCase(""))
            {
               JsonObject checkin = new JsonObject();
               checkin.addProperty("title", "Checkin Date");
               checkin.addProperty("date", getDate() + "");
               checkin.addProperty("value", checkinTime.substring(0, 10));
               checkin.addProperty("type", "CheckinDate");
               checkin.addProperty("itemMetaData", "");
               itemsArray.add(checkin);
            }

            if (!checkOutTime.equalsIgnoreCase(""))
            {
               JsonObject checkout = new JsonObject();
               checkout.addProperty("title", "Checkout Date");
               checkout.addProperty("date", getDate() + "");
               checkout.addProperty("value", checkOutTime.substring(0, 10));
               checkout.addProperty("type", "CheckoutDate");
               checkout.addProperty("itemMetaData", "");
               itemsArray.add(checkout);
            }


            for (DVPmsBill pmsBill : pmsBills)
            {
               JsonObject itemdata1 = new JsonObject();
               itemdata1.addProperty("title", pmsBill.getDescription());
               itemdata1.addProperty("date",
                        pmsBill.getDate() + pmsBill.getTime());
               itemdata1.addProperty("value", new DecimalFormat("#.##").format(pmsBill.getAmount())+"");
//               itemdata1.addProperty("value", new DecimalFormat("###,##,##,##0.00").format(pmsBill.getAmount())+"");
               itemdata1.addProperty("type", "amount");
               itemdata1.addProperty("itemMetaData", "");
               itemsArray.add(itemdata1);
            }
            
            commonData.add("items", itemsArray);
            jsonArray.add(commonData);
            
            //end
         }
         

         
         jsonDetails.add("billDetails", jsonArray);

         dvLogger.info("jsonDetails "+jsonDetails.toString());
         dvLogger.info("InRoomDevices "+InRoomDevices.size()+"  "+XplayerUi.size());
         for (int inRoomDevice : InRoomDevices)
         {
        	 JsonObject jsonResonse = new JsonObject();
            jsonResonse.addProperty("feature", "room");
            jsonResonse.addProperty("type", "billDetails");
            jsonResonse.addProperty("targetDeviceType",
                     DVDeviceTypes.ipad.toString());
            jsonResonse.addProperty("targetDeviceId", inRoomDevice+"");
            jsonResonse.add("details", jsonDetails);
            jsonResponseArray.add(jsonResonse);
            dvLogger.info("jsonResonse inroom   "+jsonResonse.toString());
         }
         for (int inRoomDevice : NonDvcInRoomDevices)
         {
             JsonObject jsonResonse = new JsonObject();
            jsonResonse.addProperty("feature", "room");
            jsonResonse.addProperty("type", "billDetails");
            jsonResonse.addProperty("targetDeviceType",
                     DVDeviceTypes.ipad.toString());
            jsonResonse.addProperty("targetDeviceId", inRoomDevice+"");
            jsonResonse.add("details", jsonDetails);
            jsonResponseArray.add(jsonResonse);
            dvLogger.info("jsonResonse inroom   "+jsonResonse.toString());
         }
         for (int xplayerUi : XplayerUi)
         {
        	 JsonObject jsonResonse = new JsonObject();
            jsonResonse.addProperty("feature", "room");
            jsonResonse.addProperty("type", "billDetails");
            jsonResonse.addProperty("targetDeviceType",
                     DVDeviceTypes.tvui.toString());
            jsonResonse.addProperty("targetDeviceId", xplayerUi+"");
            jsonResonse.add("details", jsonDetails);
            jsonResponseArray.add(jsonResonse);
            dvLogger.info("XplayerUi inroom   "+jsonResonse.toString());
         }

         jsonRequest.add("response", jsonResponseArray);
         jsonRequest.addProperty("deviceId", "Pmsi");
         jsonRequest.addProperty("timestamp", getDate() + "");
         dvLogger.info("Bill Json " + jsonRequest.toString());
         return jsonRequest.toString();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in creating Bill json ", e);
      }
      return "";
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
   
   private void populateXplayerUi(int dvKeyId)
   {
      XplayerUi = dvPmsDatabase.populateDevices(dvKeyId, DVDeviceTypes.tvui.toString(),1);
   }

   private void populateInRoomDevices(int dvKeyId)
   {
      try
      {
         InRoomDevices = dvPmsDatabase.populateDevices(dvKeyId, DVDeviceTypes.ipad.toString(),1);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Room Devices ", e);
      }
   }
  
   private void populateNonDvcInRoomDevices(int dvKeyId)
   {
      try
      {
         NonDvcInRoomDevices = dvPmsDatabase.populateDevices(dvKeyId, DVDeviceTypes.ipad.toString(),0);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Room Devices ", e);
      }
   }
}
