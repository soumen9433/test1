package com.digivalet.pmsi.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsiStatus;
import com.digivalet.movies.DVMovieData;
import com.digivalet.pms.guestpreference.model.DVGuestPreferenceModel;
import com.digivalet.pmsi.datatypes.DVDeviceTypes;
import com.digivalet.pmsi.datatypes.DVPmsBill;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.datatypes.DVPmsGuestTypes;
import com.digivalet.pmsi.model.Items;
import com.digivalet.pmsi.model.PreferenceData;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPmsDatabase
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVDatabaseConnector dvDatabaseConnector;
   private DVMovieDatabaseConnector dvMovieDatabaseConnector;
   private DVSettings dvSettings;
   private String hotelId = "";
   private HashMap<String, Integer> masterStatus =
            new HashMap<String, Integer>();

   public DVPmsDatabase(DVDatabaseConnector dvDatabaseConnector,
            DVSettings dvSettings)
   {
      try
      {
         this.dvDatabaseConnector = dvDatabaseConnector;
         this.dvSettings = dvSettings;
         hotelId = dvSettings.getHotelId();
         try
         {
            if (!dvSettings.isMovieId())
            {
               dvMovieDatabaseConnector =
                        new DVMovieDatabaseConnector(dvSettings);
               dvMovieDatabaseConnector.start();
            }

         }
         catch (Exception e)
         {
            // TODO: handle exception
         }

         resetPmsiGuestDeviceStatus();
         resetPmsiGuests();
         getUpdatedMovieList();
         populatePendingMovieDatabase();
         updatePrice();
         SyncMovieRecords movieRecords = new SyncMovieRecords();
         movieRecords.start();
      }
      catch (Exception e)
      {
         dvLogger.error("Erorr in constructor of pms database ", e);
      }
   }


   public void getUpdatedMovieList()
   {
      try
      {
         if (!dvSettings.isMovieId())
         {
            populateMovieDatabase();
         }
         else
         {
            populateMovieIdDatabase();
         }


      }
      catch (Exception e)
      {
         dvLogger.error("Error in init on movie database ", e);
      }
   }

   public boolean getDatabaseConnectionFlag()
   {
      if (dvDatabaseConnector.connectionflag == 0)
      {
         return false;
      }
      else
      {
         return true;
      }

   }

   public long getLastConnectedTimeStamp()
   {
      return dvDatabaseConnector.getLastConnectedTimeStamp();
   }

   private void resetPmsiGuests()
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "UPDATE `pmsi_guests` set `is_deleted`=1 WHERE `pmsi_guest_id` NOT IN (SELECT `pmsi_guest_id` FROM `pmsi_key_status` WHERE `pmsi_status`="
                           + getMasterStatusId(DVPmsiStatus.checkin.toString())
                           + ")";
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in reseting pmsi guests ", e);
      }
   }


   public void resetPmsiGuestDeviceStatus()
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "UPDATE `pmsi_guest_device_status` "
                  + "SET `pmsi_status_master_id`="
                  + getMasterStatusId(DVPmsiStatus.PENDING_CHECKIN.toString())
                  + " where `pmsi_status_master_id`="
                  + getMasterStatusId(DVPmsiStatus.SENDING_CHECKIN.toString());
         dvLogger.info(query);
         stmt.executeUpdate(query);


         query = "UPDATE `pmsi_key_status` " + "SET `digivalet_status`="
                  + getMasterStatusId(DVPmsiStatus.PENDING_CHECKIN.toString())
                  + " where `digivalet_status`="
                  + getMasterStatusId(DVPmsiStatus.SENDING_CHECKIN.toString());
         dvLogger.info(query);
         stmt.executeUpdate(query);

         query = "UPDATE `pmsi_guest_device_status` "
                  + "SET `pmsi_status_master_id`="
                  + getMasterStatusId(DVPmsiStatus.PENDING_CHECKOUT.toString())
                  + " where `pmsi_status_master_id`="
                  + getMasterStatusId(DVPmsiStatus.SENDING_CHECKOUT.toString());
         dvLogger.info(query);
         stmt.executeUpdate(query);

         query = "UPDATE `pmsi_key_status` " + "SET `digivalet_status`="
                  + getMasterStatusId(DVPmsiStatus.PENDING_CHECKIN.toString())
                  + " where `digivalet_status`="
                  + getMasterStatusId(DVPmsiStatus.SENDING_CHECKIN.toString());
         dvLogger.info(query);
         stmt.executeUpdate(query);

         query = "UPDATE `pmsi_key_status` " + "SET `digivalet_status`="
                  + getMasterStatusId(DVPmsiStatus.PENDING_CHECKOUT.toString())
                  + " where `digivalet_status`="
                  + getMasterStatusId(DVPmsiStatus.SENDING_CHECKOUT.toString());
         dvLogger.info(query);
         stmt.executeUpdate(query);


         query = "UPDATE `pmsi_key_status` " + "SET `digivalet_status`="
                  + getMasterStatusId(DVPmsiStatus.PENDING_CHECKIN.toString())
                  + " where `digivalet_status`=" + getMasterStatusId(
                           DVPmsiStatus.SENDING_GUEST_INFO_UPDATE.toString());
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating device status ", e);
      }

   }

   /*
    * public String getUserByArrivalDeparture(int keyId,Date arrival,Date
    * departure) { Map<String , String > data = new HashMap<>(); String
    * JSONObject =null; try { Statement stmt =
    * dvDatabaseConnector.getconnection().createStatement(); ResultSet rs =
    * null;
    * 
    * String
    * query="select pm.guest_id, pm.key_id, pm.guest_name, ird.in_room_device_id from pmsi_guests pm inner join in_room_devices ird on pm."
    * +keyId+" = "+"ird."+keyId+" where pm.guest_departure= '"
    * +departure+"'and pm.guets_departure>='"+arrival+"'";
    * 
    * dvLogger.info("Query getUserByArrivalDeparture"+query);
    * 
    * rs = stmt.executeQuery(query);
    * 
    * while(rs.next()) { data.put("guest_id", rs.getString("guest_id"));
    * data.put("key_id", rs.getInt("key_id")+""); data.put("guest_name",
    * rs.getString("guest_name")); data.put("in_room_devices",
    * rs.getString("in_room_devices"));
    * 
    * dvLogger.info("datamap : "+data);
    * 
    * GsonBuilder builder = new GsonBuilder();
    * 
    * Gson gsonObject = builder.create();
    * 
    * JSONObject = gsonObject.toJson(data);
    * 
    * dvLogger.info("Converted Json : "+JSONObject); }
    * 
    * } catch (Exception e) { e.printStackTrace(); } return JSONObject;
    * 
    * 
    * 
    * } public String convertToObject(Map<String, String> data) { ObjectMapper
    * mapper = new ObjectMapper(); String responseJson = ""; try { responseJson
    * = mapper.writeValueAsString(data); } catch (Exception e) {
    * dvLogger.error("Exception while converting object into json. Object:" +
    * data + ". Exception : ", e); } return responseJson; }
    */

   public void updateKeyStatus(String status, String digivalet_status,
            int keyId, String guestType, int guestId)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id` = "
                  + keyId + " and `guest_type`='" + guestType + "' ";
         rs = stmt.executeQuery(query);
         dvLogger.info(query + " result row: " + rs.getFetchSize());
         if (rs.next())
         {
            query = "UPDATE `pmsi_key_status` SET `pmsi_status`="
                     + this.getMasterStatusId(status) + ", `pmsi_guest_id`="
                     + guestId + " where `key_id`=" + keyId
                     + " and `guest_type`='" + guestType + "' ";
            dvLogger.info(query);
            stmt.executeUpdate(query);
         }
         else
         {
            query = "INSERT INTO `pmsi_key_status`(`pmsi_key_status_id`, `hotel_id`, `key_id`,"
                     + " `digivalet_status`, `pmsi_status`, `created_on`, `modified_on`,`guest_type`,`pmsi_guest_id`)"
                     + "VALUES (NULL," + hotelId + "," + keyId + ","
                     + this.getMasterStatusId(digivalet_status) + ","
                     + this.getMasterStatusId(status) + "," + "NOW()" + ","
                     + "NOW()" + ",'" + guestType + "'" + ", " + guestId
                     + "  )";
            dvLogger.info(query);
            stmt.executeUpdate(query);
         }

         stmt.close();
         rs.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating Key status checkin checkout event ",
                  e);
      }
   }

   public String getPmsiKeyStatusByGuestType(int keyId, String guestType)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id` = "
                  + keyId + "  and `guest_type`='" + guestType + "' ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         String status = "";

         if (rs.next())
         {
            status = rs.getString("pmsi_status");
         }
         rs.close();
         stmt.close();
         return status;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting pmsi status of key ", e);
      }
      return "";
   }

   public String getDigivaletKeyStatusByGuestType(int keyId, String guestType)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id` = "
                  + keyId + "  and `guest_type`='" + guestType + "' ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         String status = "";

         if (rs.next())
         {
            status = rs.getString("digivalet_status");
         }
         rs.close();
         stmt.close();
         return status;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Digivalet status of key ", e);
      }
      return "";
   }

   public int getMasterStatusId(String event)
   {
      try
      {
         if (masterStatus.containsKey(event))
         {
            return masterStatus.get(event);
         }
         else
         {
            ResultSet rs = null;
            int id = 0;
            Statement stmt =
                     dvDatabaseConnector.getconnection().createStatement();
            String query = "SELECT * FROM `pmsi_status_master` WHERE `name` = "
                     + "'" + event + "'";
            dvLogger.info(query);
            rs = stmt.executeQuery(query);
            if (rs.next())
            {
               id = rs.getInt(1);
               dvLogger.info("pmsi_status_master event Id " + id + " for event "
                        + event);
               masterStatus.put(event, id);
            }
            rs.close();
            stmt.close();
            return id;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("ERROR while getting master status ID ", e);
      }
      return 0;
   }


   public int getDeviceTypeId(String device)
   {
      try
      {
         ResultSet rs = null;
         int id = 0;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         rs = stmt.executeQuery(
                  "SELECT * FROM `device_types`  WHERE `device_category` = "
                           + "'" + device + "'");
         if (rs.next())
         {
            id = rs.getInt(1);
         }
         rs.close();
         stmt.close();
         return id;
      }
      catch (Exception e)
      {
         dvLogger.error("ERROR while getting device_types ID ", e);
      }
      return 0;
   }

   public String getControllerTypeId(String device)
   {
      String typeId = "";
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `device_types`  WHERE `device_category` = "
                           + "'" + device + "'" + " and device_type NOT IN ('"
                           + dvSettings.getDiscardDeviceTypes() + "'   ) ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);

         while (rs.next())
         {
            if (typeId.length() < 1)
            {
               typeId = rs.getInt(1) + "";
            }
            else
            {
               typeId = typeId + "," + rs.getInt(1) + "";
            }

         }
         rs.close();
         stmt.close();
         dvLogger.info("Type Id's : " + typeId);
         return typeId;
      }
      catch (Exception e)
      {
         dvLogger.error("ERROR while getting device_types ID ", e);
      }
      return typeId;
   }


   public String getAllControllerTypeId(String device)
   {
      String typeId = "";
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `device_types`  WHERE `device_category` = "
                           + "'" + device + "'";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);

         while (rs.next())
         {
            if (typeId.length() < 1)
            {
               typeId = rs.getInt(1) + "";
            }
            else
            {
               typeId = typeId + "," + rs.getInt(1) + "";
            }

         }
         rs.close();
         stmt.close();
         dvLogger.info("Type Id's : " + typeId);
         return typeId;
      }
      catch (Exception e)
      {
         dvLogger.error("ERROR while getting device_types ID ", e);
      }
      return typeId;
   }

   public void updatePmsiGuestDeviceStatus(int deviceId, int status,
            String guestType, String keyId)
   {

      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "UPDATE `pmsi_guest_device_status` "
                  + "SET `pmsi_status_master_id`=" + (status)
                  + " where `in_room_device_id`=" + deviceId
                  + " AND `guest_type`='" + guestType + "'";  // TODO: added
                                                              // guest_type
                                                              // field
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating device status ", e);
      }

   }

   public void insertPmsiGuestDeviceStatus(int keyId, int deviceId, int guestId,
            int status, String guestType)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;


         String select =
                  "SELECT * FROM `pmsi_guest_device_status` WHERE `key_id` = "
                           + keyId + " AND `in_room_device_id` =  " + deviceId
                           + "  and guest_type='" + guestType + "'";
         rs = stmt.executeQuery(select);

         if (rs.next())
         {
            String query =
                     "UPDATE `pmsi_guest_device_status` SET `pmsi_status_master_id`="
                              + status + " WHERE `key_id` = " + keyId
                              + " AND `in_room_device_id` =  " + deviceId
                              + "  and guest_type='" + guestType + "'";
            dvLogger.info(query);
            dvLogger.info("Status after update  " + stmt.executeUpdate(query));

         }
         else
         {
            String query = "INSERT into `pmsi_guest_device_status` "
                     + "(`pmsi_guest_device_status_id`, `hotel_id`, `key_id`, `in_room_device_id`,"
                     + " `pmsi_guest_id`,`guest_type`, `pmsi_status_master_id`, `created_on`, `modified_on`) "
                     + "VALUES (NULL," + hotelId + "," + keyId + "," + deviceId
                     + ", " + guestId + ",'" + guestType + "'," + status
                     + ",NOW(),NOW())";
            dvLogger.info(query);
            stmt.executeUpdate(query);
         }
         rs.close();
         stmt.close();

      }
      catch (Exception e)
      {
         dvLogger.error("Error in inserting device status ", e);
      }
   }


   public int getDvsDeviceId(int inRoomId)
   {
      try
      {
         int id = 0;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `in_room_devices` WHERE `in_room_device_id` = "
                           + inRoomId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);

         if (rs.next())
         {
            id = rs.getInt("in_room_device_id");
         }
         if (null != rs)
         {
            rs.close();
         }
         stmt.close();
         return id;

      }
      catch (Exception e)
      {
         dvLogger.error("ERROR in getting in_room_device_id ", e);
      }
      return 0;

   }

   public String getIp(int device_id)
   {
      String ip = "";
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `in_room_devices` WHERE `in_room_device_id` = "
                           + device_id;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            ip = rs.getString("ip");
         }
         rs.close();
         stmt.close();
         return ip;

      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting in room device Ip ", e);
      }
      return ip;
   }

   public ArrayList<Integer> populateDevices(int keyId, String type, int viaDvc)
   {
      ArrayList<Integer> devices = new ArrayList<Integer>();;
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT * FROM `in_room_devices` WHERE `key_id` = "
                  + keyId + " and `device_type_id` ="
                  + this.getDeviceTypeId(type) + " and `via_dvc`=" + viaDvc;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            devices.add(rs.getInt(1));
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Room Devices ", e);
      }
      return devices;

   }

   public ArrayList<String> getGuestIdsByKey(int keyId)
   {
      ArrayList<String> guestIds = new ArrayList<String>();;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT * FROM `pmsi_guests` WHERE `key_id` = " + keyId
                  + " and `is_deleted` =0";

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            guestIds.add(rs.getString("guest_id"));
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating guestId's ", e);
      }
      return guestIds;

   }


   public boolean checkKeyPmsiCheckedInStatus(int keyId, String guestType)
   {
      boolean status = false;
      try
      {
         int id = 0;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id` = "
                  + keyId + " and `guest_type`='" + guestType + "' ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_status");
         }
         rs.close();
         stmt.close();
         if (id == getMasterStatusId(DVPmsiStatus.checkin.toString()))
         {
            status = true;
         }
         else
         {
            status = false;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("ERROR while getting device_types ID ", e);
      }
      return status;

   }

   public boolean checkKeyDigivaletCheckedInStatus(int keyId, String guestType)
   {
      boolean status = false;
      try
      {
         int id = 0;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id` = "
                  + keyId + " and `guest_type`='" + guestType + "' ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("digivalet_status");
         }
         rs.close();
         stmt.close();
         if (id == getMasterStatusId(DVPmsiStatus.checkin.toString()))
         {
            status = true;
         }
         else
         {
            status = false;
         }
      }
      catch (Exception e)
      {
         dvLogger.error(
                  "ERROR while getting check Key Digivalet Checked In Status ",
                  e);
      }
      return status;

   }

   public String getDeviceTypeFromId(int deviceTypeID)
   {
      String deviceType = "";
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         rs = stmt.executeQuery(
                  "SELECT `device_type` FROM `device_types` WHERE `device_type_id`="
                           + deviceTypeID);
         if (rs.next())
         {
            deviceType = rs.getString("device_type");
         }
         rs.close();
         stmt.close();
         if (deviceType.equalsIgnoreCase("Master")
                  || deviceType.equalsIgnoreCase("AV")
                  || deviceType.equalsIgnoreCase("Bathroom")
                  || deviceType.equalsIgnoreCase("CommonArea"))
         {
            deviceType = DVDeviceTypes.dvc.toString();
         }
         else if (deviceType.equalsIgnoreCase("XplayerUI"))
         {
            deviceType = DVDeviceTypes.tvui.toString();
         }
         dvLogger.info(" deviceType: " + deviceType + " for deviceTypeID "
                  + deviceTypeID);
         return deviceType;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting get Device Type From Id ", e);
      }
      return deviceType;
   }

   public boolean isDeviceCheckedIn(int deviceId, String guestType)
   {
      boolean status = false;
      try
      {
         int id = 0;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `pmsi_guest_device_status` WHERE `in_room_device_id` = "
                           + deviceId + " and guest_type='" + guestType + "'";;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_status_master_id");
         }
         rs.close();
         stmt.close();
         dvLogger.info("Status " + id + "  "
                  + getMasterStatusId(DVPmsiStatus.checkin.toString()));
         if (id == getMasterStatusId(DVPmsiStatus.checkin.toString()))
         {
            status = true;
         }
         else
         {
            status = false;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("ERROR while getting device_types ID ", e);
      }
      return status;

   }

   public int getPmsiDeviceStatus(int deviceId, String guestType)
   {
      int id = 0;
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `pmsi_guest_device_status` WHERE `in_room_device_id` = "
                           + deviceId + " and guest_type='" + guestType + "'";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_status_master_id");
            dvLogger.info("pmsi_status_master_id  " + id);
         }
         rs.close();
         stmt.close();
         return id;
      }
      catch (Exception e)
      {
         dvLogger.error("ERROR while getting device_types ID ", e);
      }
      return id;

   }


   public boolean checkKeyDigivaletPendingCheckin(int keyId, String guestType)
   {
      boolean status = false;
      try
      {
         int id = 0;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id` = "
                  + keyId + " and `guest_type`='" + guestType + "' ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("digivalet_status");
         }
         rs.close();
         stmt.close();
         if (id == getMasterStatusId(DVPmsiStatus.PENDING_CHECKIN.toString()))
         {
            status = true;
         }
         else
         {
            status = false;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("ERROR while getting check Pending Checkin ID ", e);
      }
      return status;

   }

   public int getPmsiGuestId(String guest)// TODO Danish to review function
   {
      int id = 0;
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` = '"
                  + guest + "'";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_guest_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest ID ", e);
      }
      return id;
   }

   public int getPmsiGuestId(String guestId, int key)
   {
      int id = 0;
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` = '"
                  + guestId + "' and key_id=" + key + "  and `is_deleted`=0 ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_guest_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting pmsi guest ID ", e);
      }
      return id;
   }

   public int getPmsiGuestIdWithKey(String guest, int keyId)
   {
      int id = 0;
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` = '"
                  + guest + "' and key_id=" + keyId + " and is_deleted=0";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_guest_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest ID ", e);
      }
      return id;
   }

   public int getPmsiGuestIdFromKeyStatus(int keyId, String guestType)
   {
      int id = 0;
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id`="
                  + keyId + "  and guest_type='" + guestType + "'";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_guest_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest ID ", e);
      }
      return id;
   }


   public String getGuestType(int pmsiGuestId, int keyId)
   {
      String type = "";
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `pmsi_guest_id` = "
                  + pmsiGuestId + " and `key_id`=" + keyId
                  + " and is_deleted=0";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            type = rs.getString("guest_type");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest ID ", e);
      }
      return type;
   }

   public boolean isRoomCheckedIn(int keyId)
   {
      boolean status = true;
      try
      {
         int state = 0;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id`="
                  + keyId + "  and guest_type='primary'";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            state = rs.getInt("pmsi_status");
         }
         rs.close();
         stmt.close();
         if (state == getMasterStatusId(DVPmsiStatus.checkin.toString()))
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in checking room state ", e);
      }
      return status;
   }

   public void deleteGuestIdOnCheckout(String guestId, int keyId)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "UPDATE `pmsi_guests` SET "
                  + " `is_deleted`= 1 , `checkout_time`=NOW()"
                  + " WHERE `key_id` = " + keyId + " and " + "  `guest_id` = "
                  + "'" + guestId + "' and is_deleted=0";
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Erro in deleting guest Id ", e);
      }
   }

   public void deleteGuestId(int keyId, String guestType)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "UPDATE `pmsi_guests` SET "
                  + " `is_deleted`= 1 , `checkout_time`=NOW()"
                  + " WHERE `key_id` = " + keyId + "  and `guest_type`='"
                  + guestType + "' and `is_deleted` !=1 ";
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in deleting guest Id ", e);
      }
   }

   public ArrayList<Map<DVMovieData, Object>> getMovieData(int keyId)
   {
      ArrayList<Map<DVMovieData, Object>> movielist =
               new ArrayList<Map<DVMovieData, Object>>();
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `movie_key_data` WHERE `key_id`=" + keyId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            Map<DVMovieData, Object> data = new HashMap<DVMovieData, Object>();
            data.put(DVMovieData.alignment, rs.getString("alignment"));
            data.put(DVMovieData.audioId, rs.getString("audio_id"));
            data.put(DVMovieData.dimension, rs.getString("dimension"));
            data.put(DVMovieData.duration, rs.getString("duration"));
            data.put(DVMovieData.endTime, rs.getString("end_time"));
            data.put(DVMovieData.isChargeable, rs.getString("is_chargable"));
            data.put(DVMovieData.isNeedToResume,
                     rs.getString("is_need_to_resume"));
            data.put(DVMovieData.movieId, rs.getString("movie_name"));
            data.put(DVMovieData.seekPercent, rs.getFloat("seek_percent"));
            data.put(DVMovieData.startTime, rs.getString("start_time"));
            data.put(DVMovieData.subtitleId, rs.getString("subtitle_id"));
            data.put(DVMovieData.price, rs.getFloat("price"));
            movielist.add(data);
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting movie data ", e);
      }
      return movielist;

   }


   public ArrayList<Map<DVMovieData, Object>> getMovieData(int keyId,
            String movieId)
   {
      ArrayList<Map<DVMovieData, Object>> movielist =
               new ArrayList<Map<DVMovieData, Object>>();
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `movie_key_data` WHERE `key_id`=" + keyId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            Map<DVMovieData, Object> data = new HashMap<DVMovieData, Object>();
            data.put(DVMovieData.alignment, rs.getString("alignment"));
            data.put(DVMovieData.audioId, rs.getString("audio_id"));
            data.put(DVMovieData.dimension, rs.getString("dimension"));
            data.put(DVMovieData.duration, rs.getString("duration"));
            data.put(DVMovieData.endTime, rs.getString("end_time"));
            data.put(DVMovieData.isChargeable, rs.getString("is_chargable"));
            data.put(DVMovieData.isNeedToResume,
                     rs.getString("is_need_to_resume"));
            data.put(DVMovieData.movieId, rs.getString("movie_name"));
            data.put(DVMovieData.seekPercent, rs.getFloat("seek_percent"));
            data.put(DVMovieData.startTime, rs.getString("start_time"));
            data.put(DVMovieData.subtitleId, rs.getString("subtitle_id"));
            data.put(DVMovieData.price, rs.getFloat("price"));
            movielist.add(data);
         }
         if (null != rs)
         {
            rs.close();
         }
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting movie data ", e);
      }
      return movielist;

   }

   public Map<DVPmsData, Object> getDataByGuestId(String guestId, int keyId)
   {
      try
      {
         Map<DVPmsData, Object> data = new HashMap<DVPmsData, Object>();
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` = '"
                  + guestId + "' and `key_id`=" + keyId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            data.put(DVPmsData.alternateName,
                     rs.getString("guest_alternate_name"));
            data.put(DVPmsData.arrivalDate, rs.getString("guest_arrival"));
            data.put(DVPmsData.departureDate, rs.getString("guest_departure"));
            data.put(DVPmsData.emailId, rs.getString("email"));
            data.put(DVPmsData.groupCode, rs.getString("group_code"));
            data.put(DVPmsData.guestFirstName,
                     rs.getString("guest_first_name"));
            data.put(DVPmsData.guestFullName, rs.getString("guest_full_name"));
            data.put(DVPmsData.guestId, rs.getString("guest_id"));
            data.put(DVPmsData.guestLanguage, rs.getString("guest_language"));
            data.put(DVPmsData.guestLastName, rs.getString("guest_last_name"));
            data.put(DVPmsData.guestName, rs.getString("guest_name"));
            data.put(DVPmsData.guestTitle, rs.getString("guest_title"));
            data.put(DVPmsData.guestType, rs.getString("guest_type"));
            data.put(DVPmsData.incognitoName,
                     rs.getString("guest_incognito_name"));
            data.put(DVPmsData.keyId, rs.getString("key_id"));
            data.put(DVPmsData.phoneNumber, rs.getString("phone_number"));
            data.put(DVPmsData.remoteCheckout, rs.getString("remote_checkout"));
            data.put(DVPmsData.reservationId, rs.getString("reservation_id"));
            data.put(DVPmsData.revisitFlag, rs.getString("revisit_flag"));
            data.put(DVPmsData.safeFlag, rs.getString("safe_flag"));
            data.put(DVPmsData.tvRights, rs.getString("tv_rights"));
            data.put(DVPmsData.uniqueId, rs.getString("unique_id"));
            data.put(DVPmsData.videoRights, rs.getString("video_rights"));
            data.put(DVPmsData.vipStatus, rs.getString("vip_status"));
            data.put(DVPmsData.dateOfBirth, rs.getString("date_of_birth"));
            data.put(DVPmsData.nationality, rs.getString("nationality"));
            data.put(DVPmsData.previousVisitDate,
                     rs.getString("previous_visit_date"));
            data.put(DVPmsData.isAdult, rs.getString("is_adult"));
            data.put(DVPmsData.welcomeMoodId,
                     getGuestPreferenceDataByKeyId(keyId).getMoodId());
         }
         rs.close();
         stmt.close();
         dvLogger.info("Guest Details for Guest id " + guestId + " keyId: "
                  + keyId + " are: " + data.toString());
         return data;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest details ", e);
      }
      return null;

   }

   public Map<DVPmsData, Object> getDataByGuestId(String guestId)
   {

      Map<DVPmsData, Object> data = new HashMap<DVPmsData, Object>();
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` = '"
                  + guestId + "' ORDER BY `checkout_time` ASC";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            int keyId = rs.getInt("key_id");

            data.put(DVPmsData.alternateName,
                     rs.getString("guest_alternate_name"));
            data.put(DVPmsData.arrivalDate, rs.getString("guest_arrival"));
            data.put(DVPmsData.departureDate, rs.getString("guest_departure"));
            data.put(DVPmsData.emailId, rs.getString("email"));
            data.put(DVPmsData.groupCode, rs.getString("group_code"));
            data.put(DVPmsData.guestFirstName,
                     rs.getString("guest_first_name"));
            data.put(DVPmsData.guestFullName, rs.getString("guest_full_name"));
            data.put(DVPmsData.guestId, rs.getString("guest_id"));
            data.put(DVPmsData.guestLanguage, rs.getString("guest_language"));
            data.put(DVPmsData.guestLastName, rs.getString("guest_last_name"));
            data.put(DVPmsData.guestName, rs.getString("guest_name"));
            data.put(DVPmsData.guestTitle, rs.getString("guest_title"));
            data.put(DVPmsData.guestType, rs.getString("guest_type"));
            data.put(DVPmsData.incognitoName,
                     rs.getString("guest_incognito_name"));
            data.put(DVPmsData.keyId, getDigivaletRoomNumber(keyId));
            data.put(DVPmsData.phoneNumber, rs.getString("phone_number"));
            data.put(DVPmsData.remoteCheckout, rs.getString("remote_checkout"));
            data.put(DVPmsData.reservationId, rs.getString("reservation_id"));
            data.put(DVPmsData.revisitFlag, rs.getString("revisit_flag"));
            data.put(DVPmsData.safeFlag, rs.getString("safe_flag"));
            data.put(DVPmsData.tvRights, rs.getString("tv_rights"));
            data.put(DVPmsData.uniqueId, rs.getString("unique_id"));
            data.put(DVPmsData.videoRights, rs.getString("video_rights"));
            data.put(DVPmsData.vipStatus, rs.getString("vip_status"));
            data.put(DVPmsData.dateOfBirth, rs.getString("date_of_birth"));
            data.put(DVPmsData.nationality, rs.getString("nationality"));
            data.put(DVPmsData.previousVisitDate,
                     rs.getString("previous_visit_date"));
            data.put(DVPmsData.isAdult, rs.getString("is_adult"));
            data.put(DVPmsData.welcomeMoodId,
                     getGuestPreferenceDataByKeyId(keyId).getMoodId());

         }
         rs.close();
         stmt.close();
         dvLogger.info("Guest Details for Guest id " + guestId + " are: "
                  + data.toString());
         return data;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest details ", e);
      }
      return data;

   }

   public Map<DVPmsData, Object> getDataByPmsiGuestId(int guestId)
   {
      try
      {
         Map<DVPmsData, Object> data = new HashMap<DVPmsData, Object>();
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `pmsi_guest_id` = "
                  + guestId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            int keyId = rs.getInt("key_id");

            data.put(DVPmsData.alternateName,
                     rs.getString("guest_alternate_name"));
            data.put(DVPmsData.arrivalDate, rs.getString("guest_arrival"));
            data.put(DVPmsData.departureDate, rs.getString("guest_departure"));
            data.put(DVPmsData.emailId, rs.getString("email"));
            data.put(DVPmsData.groupCode, rs.getString("group_code"));
            data.put(DVPmsData.guestFirstName,
                     rs.getString("guest_first_name"));
            data.put(DVPmsData.guestFullName, rs.getString("guest_full_name"));
            data.put(DVPmsData.guestId, rs.getString("guest_id"));
            data.put(DVPmsData.guestLanguage, rs.getString("guest_language"));
            data.put(DVPmsData.guestLastName, rs.getString("guest_last_name"));
            data.put(DVPmsData.guestName, rs.getString("guest_name"));
            data.put(DVPmsData.guestTitle, rs.getString("guest_title"));
            data.put(DVPmsData.guestType, rs.getString("guest_type"));
            data.put(DVPmsData.incognitoName,
                     rs.getString("guest_incognito_name"));
            data.put(DVPmsData.keyId, keyId);
            data.put(DVPmsData.phoneNumber, rs.getString("phone_number"));
            data.put(DVPmsData.remoteCheckout, rs.getString("remote_checkout"));
            data.put(DVPmsData.reservationId, rs.getString("reservation_id"));
            data.put(DVPmsData.revisitFlag, rs.getString("revisit_flag"));
            data.put(DVPmsData.safeFlag, rs.getString("safe_flag"));
            data.put(DVPmsData.tvRights, rs.getString("tv_rights"));
            data.put(DVPmsData.uniqueId, rs.getString("unique_id"));
            data.put(DVPmsData.videoRights, rs.getString("video_rights"));
            data.put(DVPmsData.vipStatus, rs.getString("vip_status"));
            data.put(DVPmsData.dateOfBirth, rs.getString("date_of_birth"));
            data.put(DVPmsData.nationality, rs.getString("nationality"));
            data.put(DVPmsData.previousVisitDate,
                     rs.getString("previous_visit_date"));
            data.put(DVPmsData.isAdult, rs.getString("is_adult"));
            data.put(DVPmsData.welcomeMoodId,
                     getGuestPreferenceDataByKeyId(keyId).getMoodId());

         }
         else
         {
            String pmsi_guest_history =
                     "SELECT * FROM `pmsi_guest_history` WHERE `pmsi_guest_id` = "
                              + guestId;
            Statement stmt2 =
                     dvDatabaseConnector.getconnection().createStatement();
            ResultSet rs2 = null;
            dvLogger.info(pmsi_guest_history);
            rs2 = stmt2.executeQuery(pmsi_guest_history);
            if (rs2.next())
            {
               int keyId = rs2.getInt("key_id");
               data.put(DVPmsData.alternateName,
                        rs2.getString("guest_alternate_name"));
               data.put(DVPmsData.arrivalDate, rs2.getString("guest_arrival"));
               data.put(DVPmsData.departureDate,
                        rs2.getString("guest_departure"));
               data.put(DVPmsData.emailId, rs2.getString("email"));
               data.put(DVPmsData.groupCode, rs2.getString("group_code"));
               data.put(DVPmsData.guestFirstName,
                        rs2.getString("guest_first_name"));
               data.put(DVPmsData.guestFullName,
                        rs2.getString("guest_full_name"));
               data.put(DVPmsData.guestId, rs2.getString("guest_id"));
               data.put(DVPmsData.guestLanguage,
                        rs2.getString("guest_language"));
               data.put(DVPmsData.guestLastName,
                        rs2.getString("guest_last_name"));
               data.put(DVPmsData.guestName, rs2.getString("guest_name"));
               data.put(DVPmsData.guestTitle, rs2.getString("guest_title"));
               data.put(DVPmsData.guestType, rs2.getString("guest_type"));
               data.put(DVPmsData.incognitoName,
                        rs2.getString("guest_incognito_name"));
               data.put(DVPmsData.keyId, keyId);
               data.put(DVPmsData.phoneNumber, rs2.getString("phone_number"));
               data.put(DVPmsData.remoteCheckout,
                        rs2.getString("remote_checkout"));
               data.put(DVPmsData.reservationId,
                        rs2.getString("reservation_id"));
               data.put(DVPmsData.revisitFlag, rs2.getString("revisit_flag"));
               data.put(DVPmsData.safeFlag, rs2.getString("safe_flag"));
               data.put(DVPmsData.tvRights, rs2.getString("tv_rights"));
               data.put(DVPmsData.uniqueId, rs2.getString("unique_id"));
               data.put(DVPmsData.videoRights, rs2.getString("video_rights"));
               data.put(DVPmsData.vipStatus, rs2.getString("vip_status"));
               data.put(DVPmsData.dateOfBirth, rs2.getString("date_of_birth"));
               data.put(DVPmsData.nationality, rs2.getString("nationality"));
               data.put(DVPmsData.previousVisitDate,
                        rs2.getString("previous_visit_date"));
               data.put(DVPmsData.isAdult, rs2.getString("is_adult"));
               data.put(DVPmsData.welcomeMoodId,
                        getGuestPreferenceDataByKeyId(keyId).getMoodId());

            }
            rs2.close();
            stmt2.close();
         }
         rs.close();
         stmt.close();
         return data;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest details ", e);
      }
      return null;

   }

   public boolean isPrimaryguestCheckedIn(int inRoomDeviceId)
   {
      try
      {
         int id = 0;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `pmsi_guest_device_status` WHERE `in_room_device_id` = "
                           + inRoomDeviceId + " and guest_type='"
                           + DVPmsGuestTypes.primary.toString() + "'";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_status_master_id");
         }
         rs.close();
         stmt.close();
         if (id == getMasterStatusId(DVPmsiStatus.checkin.toString())
                  || id == getMasterStatusId(
                           DVPmsiStatus.PENDING_GUEST_INFO_UPDATE.toString()))
         {
            return true;
         }
         else
         {
            return false;
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Checkin Status for primary guest ",
                  e);
      }
      return false;
   }

   public boolean isPrimaryguestCheckedInCheckedOut(int inRoomDeviceId)
   {
      try
      {
         int id = 0;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `pmsi_guest_device_status` WHERE `in_room_device_id` = "
                           + inRoomDeviceId + " and guest_type='"
                           + DVPmsGuestTypes.primary.toString() + "'";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_status_master_id");
         }
         rs.close();
         stmt.close();
         if (id == getMasterStatusId(DVPmsiStatus.checkout.toString())
                  || id == getMasterStatusId(DVPmsiStatus.checkin.toString()))
         {
            return true;
         }
         else
         {
            return false;
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Checkout Status for primary guest ",
                  e);
      }
      return false;
   }

   public void removeDevicesFromGuestDevice(int keyId, String guestType)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "DELETE FROM `pmsi_guest_device_status` WHERE `key_id` = "
                           + "'" + keyId + "' and guest_type='" + guestType
                           + "'  ";
         dvLogger.info("Status: " + stmt.executeUpdate(query));
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in Removing Room Devices from device table ", e);
      }
   }

   public HashMap<Integer, Integer> getAllInRoomDevicesStatus(int keyId,
            String guestType)
   {
      HashMap<Integer, Integer> oldInRoomDeviceStatus =
               new HashMap<Integer, Integer>();
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `pmsi_guest_device_status` WHERE `key_id` = "
                           + keyId + " and `guest_type`='" + guestType + "' ";
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            oldInRoomDeviceStatus.put(rs.getInt("in_room_device_id"),
                     rs.getInt("pmsi_status_master_id"));
         }
         dvLogger.info(query + " result " + oldInRoomDeviceStatus.toString());
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting in room device status ", e);
      }

      return oldInRoomDeviceStatus;

   }

   public int getGuestIdFromKey(int keyId, String guestType)
   {
      int id = 0;
      try
      {

         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id` = "
                  + keyId + " and `guest_type`='" + guestType + "' ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getInt("pmsi_guest_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest ID ", e);
      }
      return id;
   }

   public ArrayList<Integer> getDvcByKey(int keyId)
   {
      ArrayList<Integer> list = new ArrayList<Integer>();
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query1 = "SELECT * FROM `in_room_devices` WHERE `key_id` = "
                  + keyId + " and `device_type_id` in ("
                  + this.getControllerTypeId("controller") + ") ";
         dvLogger.info(query1);
         rs = stmt.executeQuery(query1);
         while (rs.next())
         {
            list.add(rs.getInt("in_room_device_id"));
         }
         rs.close();
         stmt.close();
         dvLogger.info("In Room Device ID's  " + list);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting controller list ", e);
      }
      return list;
   }

   public ArrayList<Integer> getAllDvcByKey(int keyId)
   {

      ArrayList<Integer> list = new ArrayList<Integer>();
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query1 = "SELECT * FROM `in_room_devices` WHERE `key_id` = "
                  + keyId + " and `device_type_id` in ("
                  + this.getAllControllerTypeId("controller") + ") ";
         dvLogger.info(query1);
         rs = stmt.executeQuery(query1);
         while (rs.next())
         {
            list.add(rs.getInt("in_room_device_id"));
         }
         rs.close();
         stmt.close();
         dvLogger.info("In Room Device ID's  " + list);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting controller list ", e);
      }
      return list;
   }

   public void updateKeyStatusDigivaletStatus(String guestType, int keyId,
            int status)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String Updatequery = "UPDATE `pmsi_key_status` SET `digivalet_status`="
                  + status + " where `key_id`=" + keyId + " and `guest_type`='"
                  + guestType + "'";
         dvLogger.info("Update: " + Updatequery);
         stmt.executeUpdate(Updatequery);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating  ", e);
      }
   }



   public void updateKeyStatusGuestTypeByGuestId(String guestType, int guestId)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String Updatequery = "UPDATE `pmsi_key_status` SET `guest_type`='"
                  + guestType + "' " + " where `pmsi_guest_id`=" + guestId;
         dvLogger.info("Update: " + Updatequery);
         stmt.executeUpdate(Updatequery);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating  ", e);
      }
   }

   public void updatePmsiGuestTypeByGuestId(String guestType, int guestId)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String Updatequery = "UPDATE `pmsi_guests` SET `guest_type`='"
                  + guestType + "' " + " where `pmsi_guest_id`=" + guestId;
         dvLogger.info("Update: " + Updatequery);
         stmt.executeUpdate(Updatequery);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating  ", e);
      }
   }

   public ArrayList<Integer> getPendingEvents()
   {
      ArrayList<Integer> Key_Id = new ArrayList<Integer>();;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `pmsi_key_status` WHERE `digivalet_status` IN ("
                           + getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKIN.toString())
                           + " , "
                           + getMasterStatusId(
                                    DVPmsiStatus.PENDING_CHECKOUT.toString())
                           + " , "
                           + getMasterStatusId(
                                    DVPmsiStatus.PENDING_GUEST_INFO_UPDATE
                                             .toString())
                           + ") ";
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            Key_Id.add(rs.getInt(1));
         }
         rs.close();
         stmt.close();

         dvLogger.info(query + " Result: " + Key_Id);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Pending Key IDs ", e);
      }
      return Key_Id;

   }

   public ArrayList<Integer> getPendingMovieEvents()
   {
      ArrayList<Integer> device_id = new ArrayList<Integer>();;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `movie_room_status` WHERE `status`='false'";

         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            device_id.add(rs.getInt("device_id"));
         }
         rs.close();
         stmt.close();
         dvLogger.info(query + " Result:" + device_id);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Pending Key movie IDs ", e);
      }
      return device_id;

   }

   public Map<String, String> getPendingDataByPmsiKeyStatusId(int id)
   {

      try
      {
         Map<String, String> data = new HashMap<String, String>();
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `pmsi_key_status` WHERE `pmsi_key_status_id` = "
                           + id;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            data.put("key_id", rs.getString("key_id"));
            data.put("digivalet_status", rs.getString("digivalet_status"));
            data.put("guest_type", rs.getString("guest_type"));
            data.put("pmsi_guest_id", rs.getString("pmsi_guest_id"));

         }
         rs.close();
         stmt.close();
         return data;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest details ", e);
      }
      return null;
   }


   public int insertUpdateGuestDetails(Map<DVPmsData, Object> data, int keyId)// Danish
   {
      int pmsiGuestID = 0;
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` = " + "'"
                  + data.get(DVPmsData.guestId) + "' and `key_id`=" + keyId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         String guestTitle = "";
         if (null != data.get(DVPmsData.guestTitle))
         {
            guestTitle = data.get(DVPmsData.guestTitle).toString();
         }

         if (rs.next())
         {
            pmsiGuestID = rs.getInt("pmsi_guest_id");



            if (null == data.get(DVPmsData.guestFirstName)
                     || null == data.get(DVPmsData.guestName)
                     || null == data.get(DVPmsData.guestLastName)
                     || null == data.get(DVPmsData.guestLanguage))
            {
               query = "UPDATE `pmsi_guests` SET " + " `guest_title`= '"
                        + dbValidation(guestTitle) + "' , " + "`key_id`="
                        + keyId + " , "
                        /* + "`checkin_time`=NOW()" + getDate() + "," */
                        + "`remote_checkout`='"
                        + data.get(DVPmsData.remoteCheckout) + "',"
                        + "`group_code`='" + data.get(DVPmsData.groupCode)
                        + "'," + "`unique_id`='" + data.get(DVPmsData.uniqueId)
                        + "'," + "`tv_rights`='" + data.get(DVPmsData.tvRights)// TODO
                                                                               // add
                                                                               // TV
                                                                               // rights
                        + "'," + "`reservation_id`='"
                        + data.get(DVPmsData.reservationId) + "',"
                        + "`video_rights`='" + data.get(DVPmsData.videoRights)
                        + "'," + "`vip_status`='"
                        + data.get(DVPmsData.vipStatus) + "',"
                        + "`guest_type`='" + data.get(DVPmsData.guestType)
                        + "'," + "`revisit_flag`='"
                        + data.get(DVPmsData.revisitFlag) + "',"
                        + "`guest_arrival`='" + data.get(DVPmsData.arrivalDate)
                        + "'," + " `guest_departure`='"
                        + data.get(DVPmsData.departureDate)
                        + "',`is_deleted`=0 " + ",`safe_flag`= '"
                        + data.get(DVPmsData.safeFlag) + "' ,"
                        + "`date_of_birth`='" + data.get(DVPmsData.dateOfBirth)
                        + "' ," + "`nationality`='"
                        + data.get(DVPmsData.nationality) + "' ,"
                        + "`previous_visit_date`='"
                        + data.get(DVPmsData.previousVisitDate) + "' "
                        + " Where `guest_id`='" + data.get(DVPmsData.guestId)
                        + "'  and `key_id`=" + keyId;
            }
            else
            {
               query = "UPDATE `pmsi_guests` SET " + " `guest_title`= '"
                        + dbValidation(guestTitle) + "' , "
                        + "`guest_first_name`= '"
                        + dbValidation(
                                 data.get(DVPmsData.guestFirstName).toString())
                        + "'," + "`guest_last_name`='"
                        + dbValidation(
                                 data.get(DVPmsData.guestLastName).toString())
                        + "'," + "`guest_name`='"
                        + dbValidation(data.get(DVPmsData.guestName).toString())
                        + "'," + "`guest_language`='"
                        + data.get(DVPmsData.guestLanguage) + "',"
                        /* + "`checkin_time`=NOW()" + getDate() + "," */
                        + "`remote_checkout`='"
                        + data.get(DVPmsData.remoteCheckout) + "',"
                        + "`group_code`='" + data.get(DVPmsData.groupCode)
                        + "'," + "`unique_id`='" + data.get(DVPmsData.uniqueId)
                        + "'," + "`tv_rights`='" + data.get(DVPmsData.tvRights)
                        + "'," + "`is_adult`='" + data.get(DVPmsData.isAdult)
                        + "'," + "`reservation_id`='"
                        + data.get(DVPmsData.reservationId) + "',"
                        + "`video_rights`='" + data.get(DVPmsData.videoRights)
                        + "'," + "`vip_status`='"
                        + data.get(DVPmsData.vipStatus) + "',"
                        + "`guest_type`='" + data.get(DVPmsData.guestType)
                        + "'," + "`revisit_flag`='"
                        + data.get(DVPmsData.revisitFlag) + "',"
                        + "`guest_arrival`='" + data.get(DVPmsData.arrivalDate)
                        + "'," + " `guest_departure`='"
                        + data.get(DVPmsData.departureDate)
                        + "',`is_deleted`=0 " + ",`guest_alternate_name`= '"
                        + dbValidation(
                                 data.get(DVPmsData.alternateName).toString())
                        + "'" + ",`guest_incognito_name`= '"
                        + dbValidation(
                                 data.get(DVPmsData.incognitoName).toString())
                        + "'" + ",`email`= '" + data.get(DVPmsData.emailId)
                        + "'" + ",`guest_full_name`= '"
                        + dbValidation(
                                 data.get(DVPmsData.guestFullName).toString())
                        + "'" + ",`phone_number`= '"
                        + data.get(DVPmsData.phoneNumber) + "'"
                        + ",`safe_flag`= '" + data.get(DVPmsData.safeFlag)
                        + "' ," + "`date_of_birth`='"
                        + data.get(DVPmsData.dateOfBirth) + "' " + " ,"
                        + "`is_adult`='" + data.get(DVPmsData.isAdult) + "' ,"

                        + "`nationality`='" + data.get(DVPmsData.nationality)
                        + "' ," + "`previous_visit_date`='"
                        + data.get(DVPmsData.previousVisitDate) + "' "
                        + " Where `guest_id`='" + data.get(DVPmsData.guestId)
                        + "'  and `key_id`=" + keyId;
            }
            dvLogger.info("query: " + query);
            int result = stmt.executeUpdate(query);
            dvLogger.info("Query: " + query + " Result " + result);
         }
         else
         {
            dvLogger.info("Un Validated guest name "
                     + (data.get(DVPmsData.guestName).toString()));
            dvLogger.info("Validated guest name "
                     + dbValidation(data.get(DVPmsData.guestName).toString()));
            query = "INSERT INTO `pmsi_guests` (`pmsi_guest_id`, `hotel_id`, `key_id`, `guest_id`,"
                     + " `guest_title`, `guest_first_name`, `guest_last_name`, `guest_name`, "
                     + "`guest_language`, `checkin_time`, " + "`bill_amount`, "
                     + "`remote_checkout`, `group_code`, `unique_id`, `tv_rights`,`is_adult`,"
                     + "`reservation_id`, `video_rights`, `vip_status`, `guest_type`, "
                     + "`revisit_flag`, `guest_arrival`,"
                     + " `guest_departure`, `created_on`,`is_deleted`,`guest_alternate_name`"
                     + ",`guest_incognito_name`,`email`,`guest_full_name`,`phone_number`"
                     + ",`safe_flag`,`date_of_birth`,`nationality`,`previous_visit_date`,`guest_count`)"
                     + "VALUES (NULL, '" + hotelId + "', '" + keyId + "', '"
                     + data.get(DVPmsData.guestId) + "', " + "'" + guestTitle
                     + "', '"
                     + dbValidation(
                              data.get(DVPmsData.guestFirstName).toString())
                     + "', '"
                     + dbValidation(
                              data.get(DVPmsData.guestLastName).toString())
                     + "', '"
                     + dbValidation(data.get(DVPmsData.guestName).toString())
                     + "'," + "'"
                     + dbValidation(
                              data.get(DVPmsData.guestLanguage).toString())
                     + "', " + "NOW()" + ", '0', " + "'"
                     + data.get(DVPmsData.remoteCheckout) + "', '"
                     + data.get(DVPmsData.groupCode) + "', '"
                     + data.get(DVPmsData.uniqueId) + "', '"
                     + data.get(DVPmsData.tvRights) + "'," + "'"
                     + data.get(DVPmsData.isAdult) + "'," + "'"
                     + data.get(DVPmsData.reservationId) + "', '"
                     + data.get(DVPmsData.videoRights) + "', '"
                     + data.get(DVPmsData.vipStatus) + "', '"
                     + data.get(DVPmsData.guestType) + "', " + "'"
                     + data.get(DVPmsData.revisitFlag) + "', '"
                     + (String)data.get(DVPmsData.arrivalDate) + "', '"
                     + (String)data.get(DVPmsData.departureDate) + "', " + "NOW()"
                     + ",0" + ", '"
                     + dbValidation(
                              data.get(DVPmsData.alternateName).toString())
                     + "', " + "'"
                     + dbValidation(
                              data.get(DVPmsData.incognitoName).toString())
                     + "', " + "'" + data.get(DVPmsData.emailId) + "', " + "'"
                     + dbValidation(
                              data.get(DVPmsData.guestFullName).toString())
                     + "', " + "'" + data.get(DVPmsData.phoneNumber) + "', "
                     + "'" + data.get(DVPmsData.safeFlag) + "', " + "'"
                     + data.get(DVPmsData.dateOfBirth) + "', " + "'"
                     + data.get(DVPmsData.nationality) + "', " + "'"
                     + data.get(DVPmsData.previousVisitDate) + "', '"
                     + data.get(DVPmsData.guestCount) + "' " + ")";
            dvLogger.info(query);
            int result = stmt.executeUpdate(query);

            ResultSet id = stmt.getGeneratedKeys();
            if (id.next())
            {
               pmsiGuestID = id.getInt(1);

               dvLogger.info("Genrated Guest ID " + pmsiGuestID);
            }
            dvLogger.info("Result " + result);
            if (null != id)
            {
               id.close();
            }

         }
         if (null != rs)
         {
            rs.close();
         }

         stmt.close();
         return pmsiGuestID;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing checkin checkout event ", e);
      }
      return 0;

   }


   public boolean UpdateGuestDetails(Map<DVPmsData, Object> data, int keyId)
   {
      int result = 0;
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` = " + "'"
                  + data.get(DVPmsData.guestId) + "' and `key_id`=" + keyId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);

         if (rs.next())
         {
            query = "UPDATE `pmsi_guests` SET " + " `guest_title`= '"
                     + dbValidation(data.get(DVPmsData.guestTitle).toString())
                     + "' , " + "`guest_first_name`= '"
                     + dbValidation(
                              data.get(DVPmsData.guestFirstName).toString())
                     + "'," + "`guest_last_name`='"
                     + dbValidation(
                              data.get(DVPmsData.guestLastName).toString())
                     + "'," + "`guest_name`='"
                     + dbValidation(data.get(DVPmsData.guestName).toString())
                     + "'," + "`guest_language`='"
                     + data.get(DVPmsData.guestLanguage) + "',"
                     /* + "`checkin_time`=NOW()" + getDate() + "," */
                     + "`remote_checkout`='"
                     + data.get(DVPmsData.remoteCheckout) + "',"
                     + "`group_code`='" + data.get(DVPmsData.groupCode) + "',"
                     + "`unique_id`='" + data.get(DVPmsData.uniqueId) + "',"
                     + "`tv_rights`='" + data.get(DVPmsData.tvRights) + "',"
                     + "`is_adult`='" + data.get(DVPmsData.isAdult) + "',"
                     + "`reservation_id`='" + data.get(DVPmsData.reservationId)
                     + "'," + "`video_rights`='"
                     + data.get(DVPmsData.videoRights) + "'," + "`vip_status`='"
                     + data.get(DVPmsData.vipStatus) + "'," + "`guest_type`='"
                     + data.get(DVPmsData.guestType) + "'," + "`revisit_flag`='"
                     + data.get(DVPmsData.revisitFlag) + "',"
                     + "`guest_arrival`='" + data.get(DVPmsData.arrivalDate)
                     + "'," + " `guest_departure`='"
                     + data.get(DVPmsData.departureDate) + "',`is_deleted`=0 "
                     + ",`guest_alternate_name`= '"
                     + dbValidation(
                              data.get(DVPmsData.alternateName).toString())
                     + "'" + ",`guest_incognito_name`= '"
                     + dbValidation(
                              data.get(DVPmsData.incognitoName).toString())
                     + "'" + ",`email`= '" + data.get(DVPmsData.emailId) + "'"
                     + ",`guest_full_name`= '"
                     + dbValidation(
                              data.get(DVPmsData.guestFullName).toString())
                     + "'" + ",`phone_number`= '"
                     + data.get(DVPmsData.phoneNumber) + "'" + ",`safe_flag`= '"
                     + data.get(DVPmsData.safeFlag) + "' ,"
                     + "`date_of_birth`='" + data.get(DVPmsData.dateOfBirth)
                     + "' " + " ," + "`is_adult`='"
                     + data.get(DVPmsData.isAdult) + "' ,"

                     + "`nationality`='" + data.get(DVPmsData.nationality)
                     + "' ," + "`previous_visit_date`='"
                     + data.get(DVPmsData.previousVisitDate) + "' " + " ,"
                     + "`guest_count`='" + data.get(DVPmsData.guestCount) + "' "
                     + " Where `guest_id`='" + data.get(DVPmsData.guestId)
                     + "'  and `key_id`=" + keyId;
            
            dvLogger.info("Query: " + query);

            result = stmt.executeUpdate(query);
            dvLogger.info("Query: " + query + " Result " + result);
         }
         else
         {
            query = "INSERT INTO `pmsi_guests` (`pmsi_guest_id`, `hotel_id`, `key_id`, `guest_id`,"
                     + " `guest_title`, `guest_first_name`, `guest_last_name`, `guest_name`, "
                     + "`guest_language`, `checkin_time`, " + "`bill_amount`, "
                     + "`remote_checkout`, `group_code`, `unique_id`, `tv_rights`,`is_adult`,"
                     + "`reservation_id`, `video_rights`, `vip_status`, `guest_type`, "
                     + "`revisit_flag`, `guest_arrival`,"
                     + " `guest_departure`, `created_on`,`is_deleted`,`guest_alternate_name`"
                     + ",`guest_incognito_name`,`email`,`guest_full_name`,`phone_number`"
                     + ",`safe_flag`,`date_of_birth`,`nationality`,`previous_visit_date`,`guest_count`)"
                     + "VALUES (NULL, '" + hotelId + "', '" + keyId + "', '"
                     + data.get(DVPmsData.guestId).toString() + "', " + "'"
                     + data.get(DVPmsData.guestTitle).toString() + "', '"
                     + dbValidation(
                              data.get(DVPmsData.guestFirstName).toString())
                     + "', '"
                     + dbValidation(
                              data.get(DVPmsData.guestLastName).toString())
                     + "', '"
                     + dbValidation(data.get(DVPmsData.guestName).toString())
                     + "'," + "'"
                     + dbValidation(
                              data.get(DVPmsData.guestLanguage).toString())
                     + "', " + "NOW()" + ", '0', " + "'"
                     + data.get(DVPmsData.remoteCheckout) + "', '"
                     + data.get(DVPmsData.groupCode) + "', '"
                     + data.get(DVPmsData.uniqueId) + "', '"
                     + data.get(DVPmsData.tvRights) + "'," + "'"
                     + data.get(DVPmsData.isAdult) + "'," + "'"
                     + data.get(DVPmsData.reservationId) + "', '"
                     + data.get(DVPmsData.videoRights) + "', '"
                     + data.get(DVPmsData.vipStatus) + "', '"
                     + data.get(DVPmsData.guestType) + "', " + "'"
                     + data.get(DVPmsData.revisitFlag) + "', '"
                     + data.get(DVPmsData.arrivalDate) + "', '"
                     + data.get(DVPmsData.departureDate) + "', " + "NOW()"
                     + ",0" + ", '"
                     + dbValidation(
                              data.get(DVPmsData.alternateName).toString())
                     + "', " + "'"
                     + dbValidation(
                              data.get(DVPmsData.incognitoName).toString())
                     + "', " + "'" + data.get(DVPmsData.emailId) + "', " + "'"
                     + dbValidation(
                              data.get(DVPmsData.guestFullName).toString())
                     + "', " + "'" + data.get(DVPmsData.phoneNumber) + "', "
                     + "'" + data.get(DVPmsData.safeFlag) + "', " + "'"
                     + data.get(DVPmsData.dateOfBirth) + "', " + "'"
                     + data.get(DVPmsData.nationality) + "', " + "'"
                     + data.get(DVPmsData.previousVisitDate) + "' ,'"
                     + data.get(DVPmsData.guestCount) + "' "


                     + ")";
            dvLogger.info(query);
            result = stmt.executeUpdate(query);
         }
         stmt.close();
         rs.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing checkin checkout event ", e);
      }
      return (result < 1) ? false : true;

   }

   public void UpdateExistingGuestDetails(Map<DVPmsData, Object> data,
            int keyId)
   {
      int pmsiGuestID = 0;
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         rs = stmt.executeQuery(
                  "SELECT * FROM `pmsi_guests` WHERE `guest_id` = " + "'"
                           + data.get(DVPmsData.guestId) + "' ");

         dvLogger.info(
                  "Query: " + "SELECT * FROM `pmsi_guests` WHERE `guest_id` = "
                           + "'" + data.get(DVPmsData.guestId) + "' ");


         if (rs.next())
         {
            pmsiGuestID = rs.getInt("pmsi_guest_id");

            try
            {
               if (null != data.get(DVPmsData.guestFirstName).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.guestFirstName).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + " `guest_first_name`= '"
                           + dbValidation(data.get(DVPmsData.guestFirstName)
                                    .toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.guestLastName).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.guestLastName).toString()))
               {

                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`guest_last_name`='"
                           + dbValidation(data.get(DVPmsData.guestLastName)
                                    .toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);


               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.guestName).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.guestName).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`guest_name`='"
                           + dbValidation(
                                    data.get(DVPmsData.guestName).toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }

            try
            {
               if (null != data.get(DVPmsData.guestLanguage).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.guestLanguage).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`guest_language`='"
                           + data.get(DVPmsData.guestLanguage).toString()
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);

               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.remoteCheckout).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.remoteCheckout).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`remote_checkout`='"
                           + data.get(DVPmsData.remoteCheckout)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);

               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }

            try
            {
               if (null != data.get(DVPmsData.groupCode).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.groupCode).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`group_code`='"
                           + data.get(DVPmsData.groupCode)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);

               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.uniqueId).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.uniqueId).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`unique_id`='"
                           + data.get(DVPmsData.uniqueId)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);

               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }

            try
            {
               if (null != data.get(DVPmsData.tvRights).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.tvRights).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`tv_rights`='"
                           + data.get(DVPmsData.tvRights)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);

               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }

            try
            {
               if (null != data.get(DVPmsData.isAdult).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.isAdult).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`is_adult`='"
                           + data.get(DVPmsData.isAdult)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);

               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }

            try
            {
               if (null != data.get(DVPmsData.reservationId).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.reservationId).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`reservation_id`='"
                           + data.get(DVPmsData.reservationId)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.videoRights).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.videoRights).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`video_rights`='"
                           + data.get(DVPmsData.videoRights)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.vipStatus).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.vipStatus).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`vip_status`='"
                           + data.get(DVPmsData.vipStatus)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }

            try
            {
               if (null != data.get(DVPmsData.guestType).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.guestType).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`guest_type`='"
                           + data.get(DVPmsData.guestType)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.revisitFlag).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.revisitFlag).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`revisit_flag`='"
                           + data.get(DVPmsData.revisitFlag)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.arrivalDate).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.arrivalDate).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`guest_arrival`='"
                           + data.get(DVPmsData.arrivalDate)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.departureDate).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.departureDate).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`guest_departure`='"
                           + data.get(DVPmsData.departureDate)
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.alternateName).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.alternateName).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET "
                           + "`guest_alternate_name`='"
                           + dbValidation(data.get(DVPmsData.alternateName)
                                    .toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.guestFullName).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.guestFullName).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`guest_full_name`='"
                           + dbValidation(data.get(DVPmsData.guestFullName)
                                    .toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.incognitoName).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.incognitoName).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET "
                           + "`guest_incognito_name`='"
                           + dbValidation(data.get(DVPmsData.incognitoName)
                                    .toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.emailId).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.emailId).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`email`='"
                           + dbValidation(
                                    data.get(DVPmsData.emailId).toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.phoneNumber).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.phoneNumber).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`phone_number`='"
                           + dbValidation(
                                    data.get(DVPmsData.phoneNumber).toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.safeFlag).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.safeFlag).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`safe_flag`='"
                           + dbValidation(
                                    data.get(DVPmsData.safeFlag).toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.dateOfBirth).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.dateOfBirth).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`date_of_birth`='"
                           + dbValidation(
                                    data.get(DVPmsData.dateOfBirth).toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.nationality).toString()
                        && !"".equalsIgnoreCase(
                                 data.get(DVPmsData.nationality).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET " + "`nationality`='"
                           + dbValidation(
                                    data.get(DVPmsData.nationality).toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               if (null != data.get(DVPmsData.previousVisitDate).toString()
                        && !"".equalsIgnoreCase(data
                                 .get(DVPmsData.previousVisitDate).toString()))
               {
                  String query = "";
                  query = "UPDATE `pmsi_guests` SET "
                           + "`previous_visit_date`='"
                           + dbValidation(data.get(DVPmsData.previousVisitDate)
                                    .toString())
                           + " WHERE =`pmsi_guest_id`=" + pmsiGuestID
                           + " and `key_id`=" + keyId;
                  dvLogger.info(query);
                  stmt.executeUpdate(query);
               }
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }

         }

         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing checkin checkout event ", e);
      }


   }

   public int getKeyIdFromGuestId(String guestId)
   {
      int key = 0;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();

         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` = '"
                  + guestId + "'";
         dvLogger.info("Query : " + query);
         rs = stmt.executeQuery(query);

         if (rs.next())
         {
            key = rs.getInt("key_id");
         }
         rs.close();
         stmt.close();
         dvLogger.info("Got Key: " + key + " from GuestId " + guestId);
      }
      catch (Exception e)
      {
         dvLogger.info("Error in getting keyId From GuestId " + e);
      }
      return key;
   }

   // public void UpdateExistingGuestDetails(Map<DVPmsData, Object> data)
   // {
   //
   // int pmsiGuestID = 0;
   // dvLogger.info("Updating Guest Details : "+data.entrySet());
   // int keyId = Integer.parseInt(data.get(DVPmsData.keyId).toString());
   // try
   // {
   // ResultSet rs = null;
   // Statement stmt = dvDatabaseConnector.getconnection().createStatement();
   // rs = stmt.executeQuery(
   // "SELECT * FROM `pmsi_guests` WHERE `guest_id` = " + "'"
   // + data.get(DVPmsData.guestId) + "' ");
   //
   // dvLogger.info(
   // "Query: " + "SELECT * FROM `pmsi_guests` WHERE `guest_id` = "
   // + "'" + data.get(DVPmsData.guestId) + "' ");
   //
   //
   // if (rs.next())
   // {
   // pmsiGuestID = rs.getInt("pmsi_guest_id");
   //
   // try
   // {
   // if (null != data.get(DVPmsData.guestFirstName).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.guestFirstName).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + " `guest_first_name`= '"
   // + dbValidation(data.get(DVPmsData.guestFirstName)
   // .toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.guestLastName).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.guestLastName).toString()))
   // {
   //
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`guest_last_name`='"
   // + dbValidation(data.get(DVPmsData.guestLastName)
   // .toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   //
   //
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.guestName).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.guestName).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`guest_name`='"
   // + dbValidation(
   // data.get(DVPmsData.guestName).toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   //
   // try
   // {
   // if (null != data.get(DVPmsData.guestLanguage).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.guestLanguage).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`guest_language`='"
   // + data.get(DVPmsData.guestLanguage).toString()
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   //
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.remoteCheckout).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.remoteCheckout).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`remote_checkout`='"
   // + data.get(DVPmsData.remoteCheckout)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   //
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   //
   // try
   // {
   // if (null != data.get(DVPmsData.groupCode).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.groupCode).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`group_code`='"
   // + data.get(DVPmsData.groupCode)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   //
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.uniqueId).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.uniqueId).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`unique_id`='"
   // + data.get(DVPmsData.uniqueId)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   //
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   //
   // try
   // {
   // if (null != data.get(DVPmsData.tvRights).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.tvRights).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`tv_rights`='"
   // + data.get(DVPmsData.tvRights)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   //
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   //
   // try
   // {
   // if (null != data.get(DVPmsData.isAdult).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.isAdult).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`is_adult`='"
   // + data.get(DVPmsData.isAdult)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   //
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   //
   // try
   // {
   // if (null != data.get(DVPmsData.reservationId).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.reservationId).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`reservation_id`='"
   // + data.get(DVPmsData.reservationId)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.videoRights).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.videoRights).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`video_rights`='"
   // + data.get(DVPmsData.videoRights)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.vipStatus).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.vipStatus).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`vip_status`='"
   // + data.get(DVPmsData.vipStatus)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   //
   // try
   // {
   // if (null != data.get(DVPmsData.guestType).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.guestType).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`guest_type`='"
   // + data.get(DVPmsData.guestType)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.revisitFlag).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.revisitFlag).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`revisit_flag`='"
   // + data.get(DVPmsData.revisitFlag)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.arrivalDate).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.arrivalDate).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`guest_arrival`='"
   // + data.get(DVPmsData.arrivalDate)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.departureDate).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.departureDate).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`guest_departure`='"
   // + data.get(DVPmsData.departureDate)
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.alternateName).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.alternateName).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET "
   // + "`guest_alternate_name`='"
   // + dbValidation(data.get(DVPmsData.alternateName)
   // .toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.guestFullName).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.guestFullName).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`guest_full_name`='"
   // + dbValidation(data.get(DVPmsData.guestFullName)
   // .toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.incognitoName).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.incognitoName).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET "
   // + "`guest_incognito_name`='"
   // + dbValidation(data.get(DVPmsData.incognitoName)
   // .toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.emailId).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.emailId).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`email`='"
   // + dbValidation(
   // data.get(DVPmsData.emailId).toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.phoneNumber).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.phoneNumber).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`phone_number`='"
   // + dbValidation(
   // data.get(DVPmsData.phoneNumber).toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.safeFlag).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.safeFlag).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`safe_flag`='"
   // + dbValidation(
   // data.get(DVPmsData.safeFlag).toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.dateOfBirth).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.dateOfBirth).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`date_of_birth`='"
   // + dbValidation(
   // data.get(DVPmsData.dateOfBirth).toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.nationality).toString()
   // && !"".equalsIgnoreCase(
   // data.get(DVPmsData.nationality).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET " + "`nationality`='"
   // + dbValidation(
   // data.get(DVPmsData.nationality).toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   // try
   // {
   // if (null != data.get(DVPmsData.previousVisitDate).toString()
   // && !"".equalsIgnoreCase(data
   // .get(DVPmsData.previousVisitDate).toString()))
   // {
   // String query = "";
   // query = "UPDATE `pmsi_guests` SET "
   // + "`previous_visit_date`='"
   // + dbValidation(data.get(DVPmsData.previousVisitDate)
   // .toString())
   // + " WHERE `pmsi_guest_id`=" + pmsiGuestID
   // + " and `key_id`=" + keyId;
   // dvLogger.info(query);
   // stmt.executeUpdate(query);
   // }
   // }
   // catch (Exception e)
   // {
   // // TODO: handle exception
   // }
   //
   // }
   //
   // rs.close();
   // stmt.close();
   // }
   // catch (Exception e)
   // {
   // dvLogger.error("Error in processing checkin checkout event ", e);
   // }
   //
   //
   //
   // }


   public int getKeyId(String roomNumber)
   {
      int Key = 0;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `pmsi_key_mapping` WHERE `pmsi_key_id` = '"
                           + roomNumber + "'";
         dvLogger.info("Query : " + query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            Key = rs.getInt("key_id");
         }
         rs.close();

         ResultSet rs2 = null;
         if (Key == 0)
         {
            dvLogger.info(
                     "Key ID not found in mapping table checking in digivalet Key table");

            rs2 = stmt.executeQuery("SELECT * FROM `keys` WHERE `number` ='"
                     + roomNumber + "' and is_deleted=0 AND is_active=1 ");

            if (rs2.next())
            {
               Key = rs2.getInt("key_id");
            }
         }
         dvLogger.info(
                  "Digivalet Key: " + Key + " from Pmsi Key " + roomNumber);
         if (null != rs2)
         {
            rs2.close();
         }

         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Key ", e);
      }
      return Key;
   }

   public int getDeviceTypeIdFromDeviceID(int deviceId)
   {
      int deviceType = 0;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         rs = stmt.executeQuery(
                  "SELECT `device_type_id` FROM `in_room_devices` WHERE `in_room_device_id`="
                           + deviceId);
         if (rs.next())
         {
            deviceType = rs.getInt("device_type_id");
         }
         rs.close();
         stmt.close();
         dvLogger.info(" device_type_id: " + deviceType + " for deviceId "
                  + deviceId);
         return deviceType;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Key from dvcid ", e);
      }
      return deviceType;

   }

   public int getKeyIdFromDvcID(int dvcId)
   {
      int Key = 0;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         rs = stmt.executeQuery(
                  "SELECT * FROM `in_room_devices` WHERE `in_room_device_id`="
                           + dvcId);

         if (rs.next())
         {
            Key = rs.getInt("key_id");
         }

         rs.close();
         stmt.close();
         dvLogger.info(" Key: " + Key + " for dvc ID " + dvcId);
         return Key;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Key from dvcid ", e);
      }
      return Key;
   }

   public String getDigivaletRoomNumber(int keyId)
   {

      String Key = keyId + "";
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT * FROM `keys` WHERE key_id=" + keyId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            Key = rs.getString("number");
         }
         rs.close();
         stmt.close();
         return Key;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Key ", e);
      }
      return Key;

   }


   public String getGuestTypeFromPmsiKey(int keyId, String guestId)
   {

      String Key = keyId + "";
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT * FROM `keys` WHERE key_id=" + keyId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            Key = rs.getString("number");
         }
         rs.close();
         stmt.close();
         return Key;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Key ", e);
      }
      return Key;

   }

   public void insertGuestItemData(String guestid, String roomno,
            String itemDisciption, double itemAmt, String itemDisplay,
            String date, String time, String folioNO)

   {
      try
      {
         int keyId = getKeyId(roomno);
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String Updatequery = "INSERT into pmsi_bill"
                  + " (`key_id`, `guest_id`, `item_description`,`item_amount`,`date`,`time`,`folio_number`) values('"
                  + keyId + "','" + guestid + "','"
                  + dbValidation(itemDisciption) + "','" + itemAmt + "','"
                  + date + "','" + time + "','" + folioNO + "')";
         dvLogger.info("INSERT: " + Updatequery);
         int i = stmt.executeUpdate(Updatequery);
         dvLogger.info("Insert response : "+ i);
         stmt.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         dvLogger.error("Error in updating  ", e);
      }
   }

   public String getLanguageCode(String langId)
   {
      String langCode = langId;
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `pmsi_language_mapping` WHERE `pmsi_lang_code` = '"
                           + langId + "' ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            langCode = rs.getString("lang_code");
         }
         dvLogger.info(
                  "Language Code " + langCode + " for Pmsi Language " + langId);
         rs.close();
         stmt.close();
         return langCode;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Key ", e);
      }
      return langCode;
   }

   public void deleteBill(String guestId, int keyId)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "delete FROM `pmsi_bill` WHERE `guest_id` = '" + guestId
                  + "' and `key_id`=" + keyId;
         dvLogger.info(query);
         dvLogger.info(
                  "Number of bill rows deleted " + stmt.executeUpdate(query));
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in deleting bill ", e);
      }
   }

   public List<DVPmsBill> getBillData(String guestId, int keyId)
   {
      List<DVPmsBill> pmsBills = new LinkedList<>();


      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_bill` WHERE `guest_id` = '"
                  + guestId + "' and `key_id`=" + keyId
                  + " ORDER BY `date` DESC, `time` DESC";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            String description = rs.getString("item_description");
            double amount = rs.getDouble("item_amount");
            String date = rs.getString("date");
            String time = rs.getString("time");
            String folio = rs.getString("folio_number");

            DVPmsBill pmsBill = new DVPmsBill(guestId, keyId, date, time,
                     amount, description, folio);
            pmsBills.add(pmsBill);
         }

         rs.close();
         stmt.close();
         return pmsBills;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting bill data ", e);
      }
      return null;
   }

   public ArrayList<String> getFolio(String guestId, int keyId)
   {
      ArrayList<String> pmsFolios = new ArrayList<String>();
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT DISTINCT `folio_number` FROM `pmsi_bill` WHERE `guest_id` = '"
                           + guestId + "' and `key_id`=" + keyId;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            String folio = rs.getString("folio_number");
            pmsFolios.add(folio);
         }

         rs.close();
         stmt.close();
         return pmsFolios;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Folios ", e);
      }
      return null;
   }


   public String getArrivalTime(String guestId, int keyId)
   {
      String time = "";
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT `guest_arrival` FROM `pmsi_guests`  WHERE `guest_id`='"
                           + guestId + "' AND `key_id`=" + keyId;

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            time = rs.getString("guest_arrival");
         }
         rs.close();
         stmt.close();
         return time;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Arrival Time ", e);
      }
      return time;
   }

   public String getGuestName(String guestId, int keyId)
   {
      String guest_email = "";
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT `guest_first_name` FROM `pmsi_guests`  WHERE `guest_id`='"
                           + guestId + "' AND `key_id`=" + keyId;

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            guest_email = rs.getString("guest_first_name");
         }
         rs.close();
         stmt.close();
         return guest_email;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest_email ", e);
      }
      return guest_email;
   }
   
   public String getGuestEmail(String guestId, int keyId)
   {
      String guest_name = "";
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT `guest_name` FROM `pmsi_guests`  WHERE `guest_id`='"
                           + guestId + "' AND `key_id`=" + keyId;

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            guest_name = rs.getString("guest_name");
         }
         rs.close();
         stmt.close();
         return guest_name;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest_name ", e);
      }
      return guest_name;
   }

   public String getDepartureTime(String guestId, int keyId)
   {
      String time = "";
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT `guest_departure` FROM `pmsi_guests`  WHERE `guest_id`='"
                           + guestId + "' AND `key_id`=" + keyId;

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            time = rs.getString("guest_departure");
         }
         rs.close();
         stmt.close();
         return time;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Arrival Time ", e);
      }
      return time;
   }

   public float getBillTotal(int keyId, String guestId)
   {
      float total = 0;
      try
      {
         Statement stmt5 =
                  dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT SUM(item_amount) as total FROM `pmsi_bill`"
                  + " where key_id='" + keyId + "' and guest_id='" + guestId
                  + "'";
         ResultSet rsTotal = stmt5.executeQuery(query);
         String balance = "0";
         if (rsTotal.next())
         {
            balance = rsTotal.getString("total");
            if (balance == null || balance.equalsIgnoreCase("null"))
            {
               balance = "0.00";
            }
         }
         rsTotal.close();
         stmt5.close();
         total = Float.parseFloat(balance);
         dvLogger.info("bill total calculated is " + total);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting bill total ", e);
      }
      return total;
   }


   String dbValidation(String str)
   {
      str = str.replaceAll("'", "''");
      str = str.replaceAll(";", "");
      return str;
   }


   public String getPmsiRoomId(String roomNumber)
   {
      String pmsRoom = roomNumber;
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         int key = 0;
         rs = stmt.executeQuery(
                  "SELECT * FROM `keys` WHERE `number` ='" + roomNumber + "'");
         if (rs.next())
         {
            key = rs.getInt("key_id");
         }
         if (null != rs)
         {
            rs.close();
         }

         ResultSet rs2 = null;
         if (key != 0)
         {
            rs2 = stmt.executeQuery(
                     "SELECT * FROM `pmsi_key_mapping` WHERE `key_id` = "
                              + key);

            if (rs2.next())
            {
               pmsRoom = rs2.getString("pmsi_key_id");
            }
         }
         if (null != rs2)
         {
            rs2.close();
         }

         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting pmsi Room ID ", e);
      }
      return pmsRoom;
   }

   public int getKeyIdFromRoomNumber(String roomNumber)
   {
      int key = 0;
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT * FROM `keys` WHERE `number` ='" + roomNumber + "'";
         dvLogger.info("QUERY  :: "+ query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            key = rs.getInt("key_id");
         }

         rs.close();
         stmt.close();
         return key;

      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting Key ID ", e);
      }
      return key;
   }


   public void updateMovieKeyData(String movieName, int keyId, float seek,
            String start, String end, String audio, String subtitle,
            String duration, String dimention, String alignment,
            boolean isNeedToResume, boolean isChargable, boolean purchased)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `movie_key_data` WHERE `movie_name` = '"
                  + movieName + "' AND `key_id` = " + keyId;
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            if (purchased)
            {
               query = "UPDATE `movie_key_data` SET" + "`start_time`='" + start
                        + "'," + "`end_time`='" + end + "'," + "`seek_percent`="
                        + seek + "," + "`audio_id`='" + audio + "',"
                        + "`subtitle_id`='" + subtitle + "'," + "`duration`='"
                        + duration + "'," + "`dimension`='" + dimention + "',"
                        + "`alignment`='" + alignment + "',"
                        + "`is_need_to_resume`='" + isNeedToResume + "',"
                        + "`is_chargable`='" + isChargable + "'"
                        + ", `purchase_time`=NOW()" + " WHERE `movie_name`='"
                        + movieName + "' and `key_id`=" + keyId;
            }
            else
            {
               query = "UPDATE `movie_key_data` SET" + "`start_time`='" + start
                        + "'," + "`end_time`='" + end + "'," + "`seek_percent`="
                        + seek + "," + "`audio_id`='" + audio + "',"
                        + "`subtitle_id`='" + subtitle + "'," + "`duration`='"
                        + duration + "'," + "`dimension`='" + dimention + "',"
                        + "`alignment`='" + alignment + "',"
                        + "`is_need_to_resume`='" + isNeedToResume + "',"
                        + "`is_chargable`='" + isChargable
                        + "' WHERE `movie_name`='" + movieName
                        + "' and `key_id`=" + keyId;
            }
            dvLogger.info(query);
            stmt.executeUpdate(query);

         }
         else
         {
            if (!dvSettings.isMovieId())
            {
               populateMovieDatabase();
            }
            else
            {
               populateMovieIdDatabase();
            }
            dvLogger.info(query);
            ResultSet rs2 = null;
            rs2 = stmt.executeQuery(query);
            if (rs2.next())
            {
               if (purchased)
               {
                  query = "UPDATE `movie_key_data` SET" + "`start_time`='"
                           + start + "'," + "`end_time`='" + end + "',"
                           + "`seek_percent`=" + seek + "," + "`audio_id`='"
                           + audio + "'," + "`subtitle_id`='" + subtitle + "',"
                           + "`duration`='" + duration + "'," + "`dimension`='"
                           + dimention + "'," + "`alignment`='" + alignment
                           + "'," + "`is_need_to_resume`='" + isNeedToResume
                           + "'," + "`is_chargable`='" + isChargable + "'"
                           + ", `purchase_time`=NOW()" + " WHERE `movie_name`='"
                           + movieName + "' and `key_id`=" + keyId;
               }
               else
               {
                  query = "UPDATE `movie_key_data` SET" + "`start_time`='"
                           + start + "'," + "`end_time`='" + end + "',"
                           + "`seek_percent`=" + seek + "," + "`audio_id`='"
                           + audio + "'," + "`subtitle_id`='" + subtitle + "',"
                           + "`duration`='" + duration + "'," + "`dimension`='"
                           + dimention + "'," + "`alignment`='" + alignment
                           + "'," + "`is_need_to_resume`='" + isNeedToResume
                           + "'," + "`is_chargable`='" + isChargable
                           + "' WHERE `movie_name`='" + movieName
                           + "' and `key_id`=" + keyId;
               }
               dvLogger.info(query);
               stmt.executeUpdate(query);
            }
            else
            {
               query = "INSERT INTO `movie_key_data`"
                        + "(`movie_key_data_id`, `movie_name`, `key_id`"
                        + ",`price`) " + "VALUES " + "(NULL," + "'" + movieName
                        + "'," + keyId + "," + 0 + ")";
               dvLogger.info(query);
               stmt.executeUpdate(query);

               if (purchased)
               {
                  query = "UPDATE `movie_key_data` SET" + "`start_time`='"
                           + start + "'," + "`end_time`='" + end + "',"
                           + "`seek_percent`=" + seek + "," + "`audio_id`='"
                           + audio + "'," + "`subtitle_id`='" + subtitle + "',"
                           + "`duration`='" + duration + "'," + "`dimension`='"
                           + dimention + "'," + "`alignment`='" + alignment
                           + "'," + "`is_need_to_resume`='" + isNeedToResume
                           + "'," + "`is_chargable`='" + isChargable + "'"
                           + ", `purchase_time`=NOW()" + " WHERE `movie_name`='"
                           + movieName + "' and `key_id`=" + keyId;
               }
               else
               {
                  query = "UPDATE `movie_key_data` SET" + "`start_time`='"
                           + start + "'," + "`end_time`='" + end + "',"
                           + "`seek_percent`=" + seek + "," + "`audio_id`='"
                           + audio + "'," + "`subtitle_id`='" + subtitle + "',"
                           + "`duration`='" + duration + "'," + "`dimension`='"
                           + dimention + "'," + "`alignment`='" + alignment
                           + "'," + "`is_need_to_resume`='" + isNeedToResume
                           + "'," + "`is_chargable`='" + isChargable
                           + "' WHERE `movie_name`='" + movieName
                           + "' and `key_id`=" + keyId;
               }

               dvLogger.info(query);
               stmt.executeUpdate(query);
            }
            if (null != rs2)
            {
               rs2.close();
            }

         }
         if (null != rs)
         {
            rs.close();
         }
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating movie room status ", e);
      }
   }

   public void updateMovieRoomStatus(int deviceId, String status)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `movie_room_status` WHERE `device_id` = "
                  + deviceId;
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            query = "UPDATE `movie_room_status` SET `status`='" + status
                     + "' WHERE `device_id`=" + deviceId;
            dvLogger.info(query);
            stmt.executeUpdate(query);
         }
         else
         {
            query = "INSERT INTO `movie_room_status`(`movie_room_status_id`, `device_id`, `status`) "
                     + "VALUES (NULL," + deviceId + ",'false')";
            dvLogger.info(query);
            stmt.executeUpdate(query);

            query = "UPDATE `movie_room_status` SET `status`='" + status
                     + "' WHERE `device_id`=" + deviceId;
            dvLogger.info(query);
            stmt.executeUpdate(query);
         }

         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating movie room status ", e);
      }
   }

   public void updateMovieRoomStatusOnCheckinCheckout(int keyId)
   {
      try
      {
         String query = "UPDATE `movie_key_data` SET" + "`start_time`='"
                  + getDateTime(getDate()) + "'," + "`end_time`='"
                  + getDateTime(getDate()) + "'," + "`seek_percent`=" + 0 + ","
                  + "`audio_id`='" + 0 + "'," + "`subtitle_id`='" + 0 + "',"
                  + "`duration`='" + "00:00:00" + "'," + "`dimension`='" + "NA"
                  + "'," + "`alignment`='" + "NA" + "',"
                  + "`is_need_to_resume`='" + false + "'," + "`is_chargable`='"
                  + false + "' WHERE `key_id`=" + keyId;
         dvLogger.info(query);
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         stmt.executeUpdate(query);

         query = "UPDATE `movie_key_data` SET `is_chargable`='true' WHERE `price`>0 and `key_id`="
                  + keyId;
         dvLogger.info(query);
         stmt.executeUpdate(query);

         ArrayList<Integer> Dvcs = getAllDvcByKey(keyId);
         for (int deviceId : Dvcs)
         {
            updateMovieRoomStatus(deviceId, "false");
         }

         try
         {
            query = "UPDATE `pmsi_movie_posting_records` SET `posting_status`=2 WHERE `posting_status`=0 AND `key_id`="
                     + keyId;
            dvLogger.info(query);
            stmt.executeUpdate(query);
         }
         catch (Exception e)
         {
            dvLogger.error("Error in removing pending movies ", e);
         }
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating movie room status on checkout ", e);
      }
   }

   public boolean checkIsNewCheckin(int keyId, String guestId)
   {
      try
      {
         boolean sameGuest = false;
         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` ='"
                  + (guestId) + "'";
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         dvLogger.info(query);
         rs = stmt.executeQuery(query);

         int pmsiGuestId = 0;
         if (rs.next())
         {
            pmsiGuestId = rs.getInt("pmsi_guest_id");
         }
         query = "SELECT * FROM `pmsi_key_status` WHERE `key_id`=" + keyId
                  + " and `pmsi_guest_id`=" + pmsiGuestId;

         ResultSet rs2 = null;
         rs2 = stmt.executeQuery(query);
         if (rs2.next())
         {
            sameGuest = true;
         }
         if (null != rs2)
         {
            rs2.close();
         }
         if (null != rs)
         {
            rs.close();
         }

         stmt.close();

         return sameGuest;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in checkin for new guest", e);
         return false;
      }
   }

   public DVResult checkByRoomNumber(String roomNo)
   {
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         rs = stmt.executeQuery(
                  "SELECT * FROM `keys` WHERE `number` ='" + (roomNo) + "'");
         boolean flag = false;

         if (rs.next())
         {
            flag = true;
         }

         rs.close();
         stmt.close();

         if (flag)
         {
            return new DVResult(DVResult.SUCCESS, "Room Number is valid");
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting category id details ", e);
         return new DVResult(DVResult.DVERROR_BEFORE_PROCESSING,
                  "Something caused, nothing happened.");
      }
      return new DVResult(DVResult.DVERROR_ROOMID_NOT_FOUND,
               "Room Number is invalid");
   }

   public DVResult checkByGuestId(String guestId)
   {
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         rs = stmt.executeQuery(
                  "SELECT * FROM `pmsi_guests` WHERE `guest_id` ='" + guestId
                           + "'");
         dvLogger.info(
                  "Query: " + "SELECT * FROM `pmsi_guests` WHERE `guest_id` ="
                           + (guestId));

         boolean flag = false;

         if (rs.next())
         {
            flag = true;
         }

         rs.close();
         stmt.close();

         if (flag)
         {
            return new DVResult(DVResult.SUCCESS, "Guest Id is valid");
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting category id details ", e);
         return new DVResult(DVResult.DVERROR_BEFORE_PROCESSING,
                  "Something caused, nothing happened.");
      }
      return new DVResult(DVResult.DVERROR_GUESTID_NOT_FOUND,
               "Guest Id is invalid");
   }


   public ArrayList<Integer> getAllKeys()
   {
      ArrayList<Integer> keys = new ArrayList<Integer>();;
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         rs = stmt.executeQuery("SELECT * FROM `keys` WHERE `is_deleted`=0");
         while (rs.next())
         {
            keys.add(rs.getInt("key_id"));
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting all key id details ", e);
         return null;
      }
      return keys;
   }


   private void populatePendingMovieDatabase()
   {
      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            ResultSet rs1 = null;
            Statement stmt1 =
                     dvDatabaseConnector.getconnection().createStatement();
            String query =
                     "SELECT * FROM `in_room_devices` WHERE `device_type_id` in ("
                              + this.getAllControllerTypeId("controller")
                              + ") ";
            dvLogger.info(query);
            rs1 = stmt1.executeQuery(query);

            while (rs1.next())
            {
               int dvcId = rs1.getInt("in_room_device_id");
               String query5 =
                        "SELECT * FROM `movie_room_status` WHERE `device_id`="
                                 + dvcId;
               try
               {
                  Statement stmt2 = dvDatabaseConnector.getconnection()
                           .createStatement();
                  ResultSet rs2 = null;
                  rs2 = stmt2.executeQuery(query5);
                  if (rs2.next())
                  {

                  }
                  else
                  {
                     String query6 =
                              "INSERT INTO `movie_room_status`(`movie_room_status_id`, `device_id`, `status`) "
                                       + "VALUES (NULL," + dvcId + ",'false')";
                     dvLogger.info(query6);
                     stmt2.executeUpdate(query6);
                  }
                  rs2.close();
                  stmt2.close();
               }
               catch (Exception e)
               {
                  dvLogger.error("Error in inner query ", e);
               }
            }
            String query1 =
                     "SELECT * FROM `in_room_devices` WHERE `device_type_id` in ("
                              + this.getDeviceTypeId("tvui") + ") ";
            dvLogger.info(query1);

            ResultSet rs3 = null;
            Statement stmt3 =
                     dvDatabaseConnector.getconnection().createStatement();
            rs3 = stmt3.executeQuery(query1);

            while (rs3.next())
            {
               int xplayerId = rs3.getInt("in_room_device_id");
               String query2 =
                        "SELECT * FROM `movie_room_status` WHERE `device_id`="
                                 + xplayerId;

               dvLogger.info(query2);

               try
               {
                  Statement stmt4 = dvDatabaseConnector.getconnection()
                           .createStatement();
                  ResultSet rs4 = stmt4.executeQuery(query2);
                  if (rs4.next())
                  {
                  }
                  else
                  {
                     String query3 =
                              "INSERT INTO `movie_room_status`(`movie_room_status_id`, `device_id`, `status`) "
                                       + "VALUES (NULL," + xplayerId
                                       + ",'false')";
                     dvLogger.info(query3);
                     stmt4.executeUpdate(query3);
                  }

                  rs4.close();
                  stmt4.close();

               }
               catch (Exception e)
               {
                  dvLogger.error("Error in inner query ", e);
               }
            }
            rs3.close();
            stmt3.close();
            rs1.close();
            stmt1.close();

         }
         catch (Exception e)
         {
            dvLogger.error("Error in populating movie database ", e);
         }
      }
      else
      {
         dvLogger.info("Database connection lost!!!");
      }
   }



   public void insertMoviePlayedRecords(String movieId, int keyId)
   {
      try
      {

         Statement movieStmt =
                  dvDatabaseConnector.getconnection().createStatement();
         ResultSet movieRs = null;
         String query = "SELECT * FROM `movie_key_data` WHERE `key_id`=" + keyId
                  + " and `movie_name`='" + movieId + "'";
         dvLogger.info(query);
         movieRs = movieStmt.executeQuery(query);
         Map<DVMovieData, Object> data = new HashMap<DVMovieData, Object>();
         if (movieRs.next())
         {
            try
            {
               data.put(DVMovieData.alignment, movieRs.getString("alignment"));
               data.put(DVMovieData.audioId, movieRs.getString("audio_id"));
               data.put(DVMovieData.dimension, movieRs.getString("dimension"));
               data.put(DVMovieData.duration, movieRs.getString("duration"));
               data.put(DVMovieData.endTime, movieRs.getString("end_time"));
               data.put(DVMovieData.isChargeable,
                        movieRs.getString("is_chargable"));
               data.put(DVMovieData.isNeedToResume,
                        movieRs.getString("is_need_to_resume"));
               data.put(DVMovieData.movieId, movieRs.getString("movie_name"));
               data.put(DVMovieData.seekPercent,
                        movieRs.getFloat("seek_percent"));
               data.put(DVMovieData.startTime, movieRs.getString("start_time"));
               data.put(DVMovieData.subtitleId,
                        movieRs.getString("subtitle_id"));
               data.put(DVMovieData.price, movieRs.getFloat("price"));
               String purchaseTime = "";
               try
               {
                  purchaseTime = movieRs.getString("purchase_time");
               }
               catch (Exception e)
               {
                  // TODO: handle exception
               }

               if (null == purchaseTime || "".equalsIgnoreCase(purchaseTime))
               {
                  data.put(DVMovieData.purchaseTime, getDateTime(getDate()));
               }
               else
               {
                  data.put(DVMovieData.purchaseTime, purchaseTime);
               }
            }
            catch (Exception e)
            {
               dvLogger.error("Error in getting movie details", e);
            }
         }
         movieRs.close();
         movieStmt.close();

         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         query = "INSERT INTO " + "`movie_played_data`"
                  + "(`movie_played_data_id`, `movie_id`, `key_id`, `start_time`,"
                  + " `end_time`, `purchase_time`, `seek_percent`, `audio_id`, `subtitle_id`, "
                  + "`duration`, `dimension`, `alignment`, `is_need_to_resume`, `is_chargable`, "
                  + "`price`,`created_on`,`created_by`) VALUES " + "(NULL,'"
                  + data.get(DVMovieData.movieId).toString() + "','" + keyId
                  + "','" + data.get(DVMovieData.startTime).toString() + "','"
                  + data.get(DVMovieData.endTime).toString() + "','"
                  + data.get(DVMovieData.purchaseTime).toString() + "','"
                  + data.get(DVMovieData.seekPercent).toString() + "','"
                  + data.get(DVMovieData.audioId).toString() + "','"
                  + data.get(DVMovieData.subtitleId).toString() + "','"
                  + data.get(DVMovieData.duration).toString() + "','"
                  + data.get(DVMovieData.dimension).toString() + "','"
                  + data.get(DVMovieData.alignment).toString() + "','"
                  + data.get(DVMovieData.isNeedToResume).toString() + "','"
                  + data.get(DVMovieData.isChargeable).toString() + "',"
                  + data.get(DVMovieData.price).toString() + ",NOW(),"
                  + getUserId(dvSettings.getUserCode()) + ")";
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();


      }
      catch (Exception e)
      {
         dvLogger.error("Error in inserting movie played records ", e);
      }
   }



   public void populateMovieIdDatabase()
   {
      try
      {
         ResultSet rs1 = null;
         ResultSet rs2 = null;

         Statement stmt2 =
                  dvDatabaseConnector.getconnection().createStatement();
         Statement stmt1 =
                  dvDatabaseConnector.getconnection().createStatement();
         rs1 = stmt1.executeQuery(
                  "SELECT * FROM `movie_master` WHERE `is_deleted`=0");
         while (rs1.next())
         {
            boolean is_chargable = false;
            String movieName = rs1.getString("movie_id");
            float price = rs1.getFloat("rates");
            String query =
                     "SELECT * FROM `movie_key_data` WHERE `movie_name` LIKE '"
                              + movieName + "'";
            dvLogger.info(query);
            rs2 = stmt2.executeQuery(query);
            if (rs2.next())
            {

            }
            else
            {
               ArrayList<Integer> keys = getAllKeys();
               if (null != keys)
               {
                  if (price > 0.0)
                  {
                     is_chargable = true;
                  }
                  else
                  {
                     is_chargable = false;
                  }
                  for (int key : keys)
                  {
                     query = "INSERT INTO `movie_key_data`"
                              + "(`movie_key_data_id`, `movie_name`, `key_id`"
                              + ",`price`,`is_chargable`) " + "VALUES "
                              + "(NULL," + "'" + movieName + "'," + key + ","
                              + price + ",'" + is_chargable + "'  )";
                     dvLogger.info(query);
                     stmt2.executeUpdate(query);
                  }
               }
            }
         }
         rs1.close();
         stmt1.close();
         rs2.close();
         stmt2.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting all key id details ", e);
      }
   }

   public void populateMovieDatabase()
   {
      try
      {
         ResultSet rs1 = null;
         ResultSet rs2 = null;

         Statement stmt2 =
                  dvDatabaseConnector.getconnection().createStatement();
         Statement stmt1 =
                  dvMovieDatabaseConnector.getconnection().createStatement();
         rs1 = stmt1.executeQuery(
                  "SELECT * FROM `vd_movie` WHERE `is_deleted`=0");
         while (rs1.next())
         {
            boolean is_chargable = false;
            String movieName = rs1.getString("vd_movie_filename");
            float price = rs1.getFloat("rates");
            String query =
                     "SELECT * FROM `movie_key_data` WHERE `movie_name` LIKE '"
                              + movieName + "'";
            dvLogger.info(query);
            rs2 = stmt2.executeQuery(query);
            if (rs2.next())
            {

            }
            else
            {
               ArrayList<Integer> keys = getAllKeys();
               if (null != keys)
               {
                  if (price > 0.0)
                  {
                     is_chargable = true;
                  }
                  else
                  {
                     is_chargable = false;
                  }
                  for (int key : keys)
                  {
                     query = "INSERT INTO `movie_key_data`"
                              + "(`movie_key_data_id`, `movie_name`, `key_id`"
                              + ",`price`,`is_chargable`) " + "VALUES "
                              + "(NULL," + "'" + movieName + "'," + key + ","
                              + price + ",'" + is_chargable + "'  )";
                     dvLogger.info(query);
                     stmt2.executeUpdate(query);
                  }
               }
            }
         }
         rs1.close();
         stmt1.close();
         rs2.close();
         stmt2.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting all key id details ", e);
      }
   }


   public DVResult checkByHotelCode(String hotelCode)
   {
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         rs = stmt.executeQuery(
                  "SELECT * FROM `master_configs` WHERE `config_key`='hotel_id' AND `config_val`='"
                           + hotelCode + "'");
         dvLogger.info("Query: "
                  + "SELECT * FROM `master_configs` WHERE `config_key`='hotel_id' AND `config_val`='"
                  + hotelCode + "'");

         boolean flag = false;

         if (rs.next())
         {
            flag = true;
         }

         rs.close();
         stmt.close();

         if (flag)
         {
            return new DVResult(DVResult.SUCCESS, "Hotel code is valid");
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in check Hotel Code ", e);
         return new DVResult(DVResult.DVERROR_BEFORE_PROCESSING,
                  "Something caused, nothing happened.");
      }

      return new DVResult(DVResult.DVERROR_HOTELCODE_NOT_FOUND,
               "Hotel code is invalid");
   }

   public String getDateTime(String timestamp)
   {
      try
      {
         Date date =
                  Date.from(Instant.ofEpochSecond(Long.parseLong(timestamp)));
         SimpleDateFormat simpleDateFormat =
                  new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
         return (simpleDateFormat.format(date));
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting timestamp ", e);
         return timestamp;
      }
   }

   public String getDateTime(long timestamp)
   {
      try
      {
         Date date = new Date(timestamp);
         SimpleDateFormat simpleDateFormat =
                  new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
         return (simpleDateFormat.format(date));
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting timestamp ", e);
         return "2017-12-31 00:00:00";
      }
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

   public boolean isKeyInSendingState(int keyId)
   {
      boolean status = false;
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT * FROM `pmsi_key_status` WHERE `key_id` = " + keyId;

         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            int dvStatus = rs.getInt("digivalet_status");
            if (dvStatus == masterStatus
                     .get(DVPmsiStatus.SENDING_CHECKIN.toString()))
            {
               status = true;
               break;
            }
            else if (dvStatus == masterStatus
                     .get(DVPmsiStatus.SENDING_CHECKOUT.toString()))
            {
               status = true;
               break;
            }
            else if (dvStatus == masterStatus
                     .get(DVPmsiStatus.SENDING_GUEST_INFO_UPDATE.toString()))
            {
               status = true;
               break;
            }
         }
         rs.close();
         stmt.close();
         return status;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in checking key status in sending state ", e);
         return false;
      }
   }

   public boolean checkIsNeedToPurchaseMovieAvailable(int keyId,
            String movieName)
   {
      try
      {
         String purchasedTimeStamp = "";
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT `purchase_time` FROM `movie_key_data` WHERE `key_id`="
                           + keyId + " and `movie_name` LIKE '" + movieName
                           + "' ";
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            purchasedTimeStamp = rs.getString("purchase_time");
         }
         rs.close();
         stmt.close();

         Date timenow = new Date();
         Minutes minutes =
                  Minutes.minutesBetween(
                           new DateTime(
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                             .parse(purchasedTimeStamp)),
                           new DateTime(timenow));
         int minutesDiffrence = minutes.getMinutes();
         if (minutesDiffrence > 1440)
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in checking movie availablity ", e);
         return false;
      }
   }

   public String getGuestDepartureDate(String guestId)
   {
      String departureDate = "";
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT guest_departure FROM `pmsi_guests` WHERE `guest_id` = '"
                           + guestId + "' and `is_deleted` =0";

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            departureDate = rs.getString("guest_departure");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in get departure date ", e);
      }
      return departureDate;

   }

   public String getGuestArrivalDate(String guestId)
   {
      String arrivalDate = "";
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT guest_arrival FROM `pmsi_guests` WHERE `guest_id` = '"
                           + guestId + "' and `is_deleted` =0";

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            arrivalDate = rs.getString("guest_arrival");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in get arrival Date", e);
      }
      return arrivalDate;
   }


   public String getMoviePriceFromKeyIdMovieId(String movieId, int keyId)
   {
      float Price = 0.00f;
      String finalPrice = "0.00";
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `movie_key_data` WHERE `movie_name` LIKE '"
                           + movieId + "' AND `key_id` = " + keyId;

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            Price = rs.getFloat("price");
         }
         finalPrice = Price + "";
         try
         {
            if (finalPrice.contains("."))
            {
               String numberOfDigitAfterZero = finalPrice.split("\\.")[1];
               if (numberOfDigitAfterZero.length() == 1)
               {
                  finalPrice = finalPrice + "0";
               }
            }
            else
            {
               finalPrice = finalPrice + ".00";
            }


         }
         catch (Exception err)
         {

         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in get arrival Date", e);
      }
      return finalPrice;

   }


   public String getMoviePriceFromMovieKeyDataId(int id)
   {
      float Price = 0.00f;
      String finalPrice = "0.00";
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `movie_key_data` WHERE `movie_key_data_id` =  "
                           + id;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            Price = rs.getFloat("price");
         }
         finalPrice = Price + "";
         try
         {
            if (finalPrice.contains("."))
            {
               String numberOfDigitAfterZero = finalPrice.split("\\.")[1];
               if (numberOfDigitAfterZero.length() == 1)
               {
                  finalPrice = finalPrice + "0";
               }
            }
            else
            {
               finalPrice = finalPrice + ".00";
            }
         }
         catch (Exception e)
         {
            // TODO: handle exception
         }

         rs.close();
         stmt.close();
         dvLogger.info("finalPrice: " + finalPrice);
      }
      catch (Exception e)
      {
         dvLogger.error("error in geting movie price Date", e);
      }
      return finalPrice;

   }

   public int getKeyIdFromMovieKeyDataId(int id)
   {
      int key_id = 0;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `movie_key_data` WHERE `movie_key_data_id` =  "
                           + id;
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            key_id = rs.getInt("key_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in geting key_id ", e);
      }
      return key_id;

   }

   public int getLastMoviePostingId()
   {
      int postingId = 1;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT MAX(`pmsi_movie_posting_id`) as `pmsi_movie_posting_id`  FROM `pmsi_movie_posting_records` ";

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            postingId = rs.getInt("pmsi_movie_posting_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in geting movie posting id ", e);
      }
      return postingId;

   }


   public int getMovieKeyDataId(String movie, int keyId)
   {
      int movieId = 1;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `movie_key_data` WHERE `movie_name` LIKE '"
                           + movie + "' AND `key_id`=" + keyId;

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            movieId = rs.getInt("movie_key_data_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in geting movie key data id ", e);
      }
      return movieId;

   }

   public void insertMoviePostingRecords(int movieId, int postingStatus,
            int keyId)
   {

      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "INSERT INTO `pmsi_movie_posting_records`(`pmsi_movie_posting_id`, `movie_key_data_id`, "
                           + "`posting_status` , `key_id`,`created_on`, `modified_on`  )"
                           + "VALUES (NULL," + movieId + "," + postingStatus
                           + "," + keyId + ",NOW(),NOW()" + " )";
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in geting movie key data id ", e);
      }
   }


   public void updateMoviePostingRecords(int postingId, int postingStatus)
   {

      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "UPDATE `pmsi_movie_posting_records` "
                  + "SET `posting_status`=" + postingStatus
                  + " WHERE `pmsi_movie_posting_id`=" + postingId;
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in geting movie key data id ", e);
      }
   }

   public ArrayList<Integer> getPendingMoviesToPostToPMS()
   {
      ArrayList<Integer> devices = new ArrayList<Integer>();;
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `pmsi_movie_posting_records` WHERE `posting_status` = 0 ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            devices.add(rs.getInt(1));
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating pending movie id's ", e);
      }
      return devices;

   }

   public int getMovieKeyDataIdFromMoviePendingId(int pmsiMoviePostingId)
   {
      int movieKeyDataId = 1;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT * FROM `pmsi_movie_posting_records` WHERE `pmsi_movie_posting_id` = "
                           + pmsiMoviePostingId;

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            movieKeyDataId = rs.getInt("movie_key_data_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in geting movie key data id ", e);
      }
      return movieKeyDataId;

   }

   public ArrayList<Integer> getKeyIdsWithElapsedPurchaseTime()
   {
      ArrayList<Integer> devices = new ArrayList<Integer>();;
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT DISTINCT(key_id) FROM `movie_key_data` WHERE TIMESTAMPDIFF(MINUTE, purchase_time, NOW()) > 1440 and `is_chargable` LIKE 'false' and `price`>0 ";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            devices.add(rs.getInt(1));
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating pending movie id's ", e);
      }
      return devices;

   }

   public void deleteGuestPreference(int keyId)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "UPDATE `pmsi_guest_preferences` SET `is_deleted`=1 WHERE key_id="
                           + +keyId;
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in deleting guest preference  ", e);
      }
   }

   public void updatePurchaseStatusByKeyId(int keyId)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "UPDATE `movie_key_data` SET `is_chargable`='true' WHERE "
                           + "TIMESTAMPDIFF(MINUTE, purchase_time, NOW()) > 1440 and `is_chargable` LIKE 'false' and `price`>0.0 and `key_id`="
                           + keyId;
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("error in geting movie key data id ", e);
      }
   }


   public DVResult addGuestPreference(PreferenceData data, int userCode)
   {
      DVResult dvResult = new DVResult();

      if (null != dvDatabaseConnector.getconnection())
      {
         int keyId = getKeyId(data.getRoomNumber());
         int moodId = getMoodIdByInterfaceId(data.getMood());

         try
         {
            StringBuilder query = new StringBuilder();

            if (null != data.getReservationNumber()
                     && !"".equalsIgnoreCase(data.getReservationNumber())
                     && checkPreferenceExistByReservation(
                              data.getReservationNumber()))
            {
               query.append("UPDATE `pmsi_guest_preferences` SET `mood_id`='");
               query.append(moodId);
               query.append("', `temperature`='");
               query.append(data.getTemperature());
               query.append("', `fragrance`='");
               query.append(data.getFragrance());
               query.append("', `modified_by`=");
               query.append(userCode);
               query.append(", `modified_on`=NOW()");
               query.append(" WHERE `reservation_number`='");
               query.append(data.getReservationNumber());
               query.append(" and `is_deleted`=0");
               query.append("'");
            }
            else if (null != data.getGuestId()
                     && !"".equalsIgnoreCase(data.getGuestId())
                     && checkPreferenceExistByGuestId(data.getGuestId()))
            {
               query.append("UPDATE `pmsi_guest_preferences` SET `mood_id`='");
               query.append(moodId);
               query.append("', `temperature`='");
               query.append(data.getTemperature());
               query.append("', `fragrance`='");
               query.append(data.getFragrance());
               query.append("', `modified_by`=");
               query.append(userCode);
               query.append(", `modified_on`=NOW()");
               query.append(" WHERE `guest_id`='");
               query.append(data.getGuestId());
               query.append("'");
               query.append(" and `is_deleted`=0");
            }
            else if (checkPreferenceExistByKeyId(keyId))
            {
               query.append("UPDATE `pmsi_guest_preferences` SET `mood_id`='");
               query.append(moodId);
               query.append("', `temperature`='");
               query.append(data.getTemperature());
               query.append("', `fragrance`='");
               query.append(data.getFragrance());
               query.append("', `modified_by`=");
               query.append(userCode);
               query.append(", `modified_on`=NOW()");
               query.append(" WHERE `key_id`=");
               query.append(keyId);
               query.append(" and `is_deleted`=0");

            }
            else
            {
               query.append(
                        "INSERT INTO `pmsi_guest_preferences`(`key_id`, `lang_code`, `reservation_number`, `guest_id` ,`mood_id`, `temperature`, `special_instructions`,`fragrance`,`is_deleted`, `created_by`, `created_on`) VALUES (");
               query.append(keyId);
               query.append(",'");
               query.append(data.getLanguage());
               query.append("','");
               query.append(data.getReservationNumber());
               query.append("','");
               query.append(data.getGuestId());
               query.append("',");
               query.append(moodId);
               query.append(",'");
               query.append(data.getTemperature());
               query.append("','");
               query.append(data.getSpecialInstructions());
               query.append("','");
               query.append(data.getFragrance());
               query.append("',0,");
               query.append(userCode);
               query.append(",NOW())");
            }

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query.toString());

            int addStatus = statement.executeUpdate(query.toString());

            statement.close();

            dvLogger.info("Guest preference add status: " + addStatus);

            if (addStatus > 0)
            {
               dvResult = new DVResult(DVResult.SUCCESS,
                        "Preferences Added Successfully");
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while adding guest preference\n", e);
            dvResult = new DVResult(DVResult.DVERROR_ADDING_PREFERENCE,
                     "Error occurred while adding Preferences to DB");
         }
      }

      return dvResult;
   }

   public boolean validateReservationIdExist(String reservationId,
            String pmsiKeyId)
   {
      boolean idExist = false;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();
            StringBuilder query = new StringBuilder();
            query.append(
                     "SELECT * FROM `pmsi_guests` WHERE `reservation_id`='");
            query.append(reservationId);
            query.append(
                     "' AND `key_id` NOT IN (SELECT `key_id` FROM `pmsi_key_mapping` WHERE `pmsi_key_id` = '");
            query.append(pmsiKeyId);
            query.append("')");

            dvLogger.info("QUERY: " + query.toString());

            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               idExist = true;
            }

            dvLogger.info("Reservation Id exist: " + idExist);

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while validating, if reservation id exist\n",
                     e);
         }

         return idExist;
      }
      else
      {
         dvLogger.info(
                  "Could not connect to database while validating reservation id");
         return idExist;
      }
   }

   public String getPmsRoomNumberByReservationId(String reservationId)
   {
      String oldRoomNumber = "";

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();
            StringBuilder query = new StringBuilder();
            query.append(
                     "SELECT * FROM `pmsi_key_mapping` WHERE `key_id` IN (SELECT `key_id` FROM `pmsi_guests` WHERE `reservation_id`='");
            query.append(reservationId);
            query.append("')");

            dvLogger.info("QUERY: " + query.toString());

            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               oldRoomNumber = rs.getString("pmsi_key_id");
            }

            dvLogger.info(
                     "Existing Room Number in db before RC: " + oldRoomNumber);

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while validating, if reservation id exist\n",
                     e);
         }

         return oldRoomNumber;
      }
      else
      {
         dvLogger.info(
                  "Could not connect to database while fetching pms_key_id by reservation id");
         return oldRoomNumber;
      }
   }

   public boolean validateIsNeedToCheckin(String pmsiKeyId, String guestId)
   {
      boolean needToChekin = true;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `pmsi_guests` WHERE `guest_id`='");
            query.append(guestId);
            query.append(
                     "' AND `key_id` IN (SELECT `key_id` FROM `pmsi_key_mapping` WHERE `pmsi_key_id` = '");
            query.append(pmsiKeyId);
            query.append("')");

            dvLogger.info("QUERY: " + query.toString());

            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               needToChekin = false;
            }

            dvLogger.info("Reservation Id exist: " + needToChekin);

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while validating, if reservation id exist\n",
                     e);
         }

         return needToChekin;
      }
      else
      {
         dvLogger.info(
                  "Could not connect to database while validating reservation id");
         return needToChekin;
      }
   }
   
   public boolean validateIsNeedToCheckin(String pmsiKeyId, String guestId, String reservationId)
   {
      boolean needToChekin = true;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `pmsi_guests` WHERE `guest_id`='");
            query.append(guestId);
            query.append(
                     "' AND `key_id` IN (SELECT `key_id` FROM `pmsi_key_mapping` WHERE `pmsi_key_id` = '");
            query.append(pmsiKeyId);
            query.append("') AND `reservation_id`='"+reservationId);
            query.append("'");

            dvLogger.info("QUERY: " + query.toString());

            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               needToChekin = false;
            }

            dvLogger.info("Reservation Id exist: " + needToChekin);

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while validating, if reservation id exist\n",
                     e);
         }

         return needToChekin;
      }
      else
      {
         dvLogger.info(
                  "Could not connect to database while validating reservation id");
         return needToChekin;
      }
   }

   public String getReservationIdByGuest(String guestId)
   {
      String reservationId = "";

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `pmsi_guests` WHERE `guest_id`='");
            query.append(guestId);
            query.append("'");

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();
            dvLogger.info("QUERY: " + query);

            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               reservationId = rs.getString("reservation_id");
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while fetching reservation id by guest\n",
                     e);
         }
      }

      return reservationId;
   }

   public boolean checkPreferenceExistByReservation(String reservationId)
   {
      boolean preferenceExist = false;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            StringBuilder query = new StringBuilder();
            query.append(
                     "SELECT * FROM `pmsi_guest_preferences` WHERE `reservation_number`='");
            query.append(reservationId);
            query.append("'");

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query.toString());
            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               preferenceExist = true;
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while checking preference exist!", e);
         }
      }

      return preferenceExist;
   }

   public boolean checkPreferenceExistByGuestId(String guestId)
   {
      boolean preferenceExist = false;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            StringBuilder query = new StringBuilder();
            query.append(
                     "SELECT * FROM `pmsi_guest_preferences` WHERE `guest_id`='");
            query.append(guestId);
            query.append("'");

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query.toString());
            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               preferenceExist = true;
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while checking preference exist!", e);
         }
      }

      return preferenceExist;
   }

   public boolean checkPreferenceExistByKeyId(int key_id)
   {
      boolean preferenceExist = false;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            StringBuilder query = new StringBuilder();
            query.append(
                     "SELECT * FROM `pmsi_guest_preferences` WHERE `key_id`=");
            query.append(key_id);
            query.append(" and `is_deleted`=0");


            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query.toString());
            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               preferenceExist = true;
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while checking preference exist!", e);
         }
      }

      return preferenceExist;
   }

   public DVGuestPreferenceModel getGuestPreferenceDataByGuestId(String guestId)
   {
      DVGuestPreferenceModel guestPreferenceData = new DVGuestPreferenceModel();

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query =
                     "SELECT * FROM `pmsi_guest_preferences` WHERE `guest_id`='"
                              + guestId + "'";

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query);

            ResultSet rs = statement.executeQuery(query);

            if (rs.next())
            {
               guestPreferenceData.setTemperature(rs.getString("temperature"));
               guestPreferenceData
                        .setMoodId(getDvcMoodByMoodId(rs.getInt("mood_id")));
               guestPreferenceData.setFragrance(rs.getString("fragrance"));

            }

            rs.close();
            statement.close();

            dvLogger.info("Guest preference add status: "
                     + guestPreferenceData.toString());
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while adding guest preference\n", e);
         }
      }

      return guestPreferenceData;
   }


   public DVGuestPreferenceModel getGuestPreferenceDataByKeyId(int keyId)
   {
      DVGuestPreferenceModel guestPreferenceData = new DVGuestPreferenceModel();

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query =
                     "SELECT * FROM `pmsi_guest_preferences` WHERE `key_id`='"
                              + keyId + "' and `is_deleted`=0 ";

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query);

            ResultSet rs = statement.executeQuery(query);

            if (rs.next())
            {
               guestPreferenceData.setTemperature(rs.getString("temperature"));
               guestPreferenceData
                        .setMoodId(getDvcMoodByMoodId(rs.getInt("mood_id")));
               guestPreferenceData.setFragrance(rs.getString("fragrance"));
            }

            rs.close();
            statement.close();

            dvLogger.info("Guest preference add status: "
                     + guestPreferenceData.toString());
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while adding guest preference\n", e);
         }
      }

      return guestPreferenceData;
   }

   public int getUserId(String userCode)
   {
      if (null != dvDatabaseConnector.getconnection())
      {
         int userId = 0;
         try
         {
            ResultSet rs = null;
            Statement stmt =
                     dvDatabaseConnector.getconnection().createStatement();
            String query = "SELECT * FROM `uac_users` WHERE `username`='"
                     + userCode + "'";

            dvLogger.info("QUERY: " + query);

            rs = stmt.executeQuery(query);

            if (rs.next())
            {
               userId = rs.getInt("user_id");
            }

            dvLogger.info("user_id: " + userId);
            rs.close();
            stmt.close();
            return userId;
         }
         catch (Exception e)
         {
            dvLogger.error("Error in get db user id from uac_users", e);
            return userId;
         }
      }
      return 0;

   }

   public String getHotelIdByCode(String hotelCode)
   {
      String hotelId = "";

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query = "SELECT * FROM `hotels` WHERE `hotel_code`='"
                     + hotelCode + "'";

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query);

            ResultSet rs = statement.executeQuery(query);

            if (rs.next())
            {
               hotelId = rs.getString("hotel_id");
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while fetch hotelId by hotel code\n", e);
         }
      }
      return hotelId;
   }

   public DVResult checkByReservationNo(String reservationNumber)
   {
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();

         String query = "SELECT * FROM `pmsi_guests` WHERE `reservation_id`='"
                  + reservationNumber + "'";

         dvLogger.info("Query: " + query);

         rs = stmt.executeQuery(query);

         boolean flag = false;

         if (rs.next())
         {
            flag = true;
         }

         rs.close();
         stmt.close();

         if (flag)
         {
            return new DVResult(DVResult.SUCCESS,
                     "Reservation Number is valid");
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in validating reservation number\n", e);
         return new DVResult(DVResult.DVERROR_BEFORE_PROCESSING,
                  "Something caused, nothing happened.");
      }
      return new DVResult(DVResult.DVERROR_INVALID_RESERVATION_NO,
               "Reservation Number is invalid");
   }

   public DVResult checkByMoodId(String moodId)
   {
      try
      {
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();

         String query = "SELECT * FROM `pmsi_moods` WHERE `interface_mood_id`='"
                  + moodId + "' AND `is_active`=1";

         dvLogger.info("Query: " + query);

         rs = stmt.executeQuery(query);

         boolean flag = false;

         if (rs.next())
         {
            flag = true;
         }

         rs.close();
         stmt.close();

         if (flag)
         {
            return new DVResult(DVResult.SUCCESS, "Mood Id is valid");
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in validating Mood Id\n", e);
         return new DVResult(DVResult.DVERROR_BEFORE_PROCESSING,
                  "Something caused, nothing happened.");
      }
      return new DVResult(DVResult.DVERROR_INVALID_MOODID,
               "Mood Id is invalid");
   }

   public String getDvcMoodByMoodId(int moodId)
   {
      String mood = moodId + "";

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query = "SELECT * FROM `pmsi_moods` WHERE `mood_id`="
                     + moodId + " AND `is_active`=1";

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query);

            ResultSet rs = statement.executeQuery(query);

            if (rs.next())
            {
               mood = rs.getString("dvc_mood_id");
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while fetching dvc mood string by mood id\n",
                     e);
         }
      }

      return mood;
   }

   public Map<DVPmsData, Object> getDataByUniqueId(String uniqueId)
   {
      Map<DVPmsData, Object> data = new HashMap<>();
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `unique_id` = '"
                  + uniqueId + "' ORDER BY `checkout_time` ASC";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            data.put(DVPmsData.alternateName,
                     rs.getString("guest_alternate_name"));
            data.put(DVPmsData.arrivalDate, rs.getString("guest_arrival"));
            data.put(DVPmsData.departureDate, rs.getString("guest_departure"));
            data.put(DVPmsData.emailId, rs.getString("email"));
            data.put(DVPmsData.groupCode, rs.getString("group_code"));
            data.put(DVPmsData.guestFirstName,
                     rs.getString("guest_first_name"));
            data.put(DVPmsData.guestFullName, rs.getString("guest_full_name"));
            data.put(DVPmsData.guestId, rs.getString("guest_id"));
            data.put(DVPmsData.guestLanguage, rs.getString("guest_language"));
            data.put(DVPmsData.guestLastName, rs.getString("guest_last_name"));
            data.put(DVPmsData.guestName, rs.getString("guest_name"));
            data.put(DVPmsData.guestTitle, rs.getString("guest_title"));
            data.put(DVPmsData.guestType, rs.getString("guest_type"));
            data.put(DVPmsData.incognitoName,
                     rs.getString("guest_incognito_name"));
            data.put(DVPmsData.keyId, rs.getString("key_id"));
            data.put(DVPmsData.phoneNumber, rs.getString("phone_number"));
            data.put(DVPmsData.remoteCheckout, rs.getString("remote_checkout"));
            data.put(DVPmsData.reservationId, rs.getString("reservation_id"));
            data.put(DVPmsData.revisitFlag, rs.getString("revisit_flag"));
            data.put(DVPmsData.safeFlag, rs.getString("safe_flag"));
            data.put(DVPmsData.tvRights, rs.getString("tv_rights"));// TODO add
                                                                    // isadult
            data.put(DVPmsData.isAdult, rs.getString("is_adult"));
            data.put(DVPmsData.uniqueId, rs.getString("unique_id"));
            data.put(DVPmsData.videoRights, rs.getString("video_rights"));
            data.put(DVPmsData.vipStatus, rs.getString("vip_status"));
            data.put(DVPmsData.dateOfBirth, rs.getString("date_of_birth"));
            data.put(DVPmsData.nationality, rs.getString("nationality"));
            data.put(DVPmsData.previousVisitDate,
                     rs.getString("previous_visit_date"));
         }
         rs.close();
         stmt.close();
         dvLogger.info("Guest Details for unique id " + uniqueId + " are: "
                  + data.toString());
         return data;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest details ", e);
      }
      return data;
   }

   public String getDvcMoodByInterfaceMoodId(String interfaceMoodId)
   {
      String dvcMoodId = interfaceMoodId;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query =
                     "SELECT * FROM `pmsi_moods` WHERE `interface_mood_id`='"
                              + interfaceMoodId + "' AND `is_active`=1";

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query);

            ResultSet rs = statement.executeQuery(query);

            if (rs.next())
            {
               dvcMoodId = rs.getString("dvc_mood_id");
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Error while fetching Dvc Mood By Interface MoodId\n", e);
         }
      }

      return dvcMoodId;
   }

   public void updateGuestIdToPreferences(String reservationNumber,
            String guestId)
   {
      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            StringBuilder query = new StringBuilder();
            query.append("UPDATE `pmsi_guest_preferences` SET `guest_id`='");
            query.append(guestId);
            query.append("' WHERE `reservation_number`='");
            query.append(reservationNumber);
            query.append("'");

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query.toString());

            int status = statement.executeUpdate(query.toString());

            statement.close();

            dvLogger.info("Update status: " + status);
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while updating guest id in preference table\n",
                     e);
         }
      }
   }

   public int getMoodIdByInterfaceId(String interfaceMoodId)
   {
      int moodId = 0;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query =
                     "SELECT * FROM `pmsi_moods` WHERE `interface_mood_id`='"
                              + interfaceMoodId + "'";

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query);

            ResultSet rs = statement.executeQuery(query);

            if (rs.next())
            {
               moodId = rs.getInt("mood_id");
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Error while fetching Dvc Mood By Interface MoodId\n", e);
         }
      }

      return moodId;
   }

   public Map<DVPmsData, Object> getDataByReservationId(
            String reservationNumber)
   {
      Map<DVPmsData, Object> data = new HashMap<>();
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `reservation_id` = '"
                  + reservationNumber
                  + "' AND `is_deleted`=0 ORDER BY `checkout_time` ASC";

         dvLogger.info("QUERY: " + query);

         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            data.put(DVPmsData.alternateName,
                     rs.getString("guest_alternate_name"));
            data.put(DVPmsData.arrivalDate, rs.getString("guest_arrival"));
            data.put(DVPmsData.departureDate, rs.getString("guest_departure"));
            data.put(DVPmsData.emailId, rs.getString("email"));
            data.put(DVPmsData.groupCode, rs.getString("group_code"));
            data.put(DVPmsData.guestFirstName,
                     rs.getString("guest_first_name"));
            data.put(DVPmsData.guestFullName, rs.getString("guest_full_name"));
            data.put(DVPmsData.guestId, rs.getString("guest_id"));
            data.put(DVPmsData.guestLanguage, rs.getString("guest_language"));
            data.put(DVPmsData.guestLastName, rs.getString("guest_last_name"));
            data.put(DVPmsData.guestName, rs.getString("guest_name"));
            data.put(DVPmsData.guestTitle, rs.getString("guest_title"));
            data.put(DVPmsData.guestType, rs.getString("guest_type"));
            data.put(DVPmsData.incognitoName,
                     rs.getString("guest_incognito_name"));
            data.put(DVPmsData.keyId,
                     getDigivaletRoomNumber(rs.getInt("key_id")));
            data.put(DVPmsData.phoneNumber, rs.getString("phone_number"));
            data.put(DVPmsData.remoteCheckout, rs.getString("remote_checkout"));
            data.put(DVPmsData.reservationId, rs.getString("reservation_id"));
            data.put(DVPmsData.revisitFlag, rs.getString("revisit_flag"));
            data.put(DVPmsData.safeFlag, rs.getString("safe_flag"));
            data.put(DVPmsData.tvRights, rs.getString("tv_rights"));
            data.put(DVPmsData.isAdult, rs.getString("is_adult"));
            data.put(DVPmsData.uniqueId, rs.getString("unique_id"));
            data.put(DVPmsData.videoRights, rs.getString("video_rights"));
            data.put(DVPmsData.vipStatus, rs.getString("vip_status"));
            data.put(DVPmsData.dateOfBirth, rs.getString("date_of_birth"));
            data.put(DVPmsData.nationality, rs.getString("nationality"));
            data.put(DVPmsData.previousVisitDate,
                     rs.getString("previous_visit_date"));
         }
         rs.close();
         stmt.close();
         dvLogger.info("Guest Details for reservation id " + reservationNumber
                  + " are: " + data.toString());
         return data;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest details ", e);
      }
      return data;
   }

   public boolean checkCheckinStatusByReservationId(String reservationNumber)
   {
      boolean status = false;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            StringBuilder query = new StringBuilder();

            query.append("SELECT * FROM `pmsi_key_status` WHERE `key_id` IN ");
            query.append(
                     "(SELECT `key_id` FROM `pmsi_guests` WHERE `reservation_id`='");
            query.append(reservationNumber);
            query.append("' AND `is_deleted`=0) AND `pmsi_status` IN ");
            query.append(
                     "(SELECT `pmsi_status_master_id` FROM `pmsi_status_master` WHERE `name`='checkin' AND `is_active`=1)");

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query.toString());

            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               status = true;
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while validating checkin status by reservation id\n",
                     e);
         }
      }

      return status;
   }

   public boolean checkCheckinStatusByGuestId(String guestId)
   {
      boolean status = false;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            StringBuilder query = new StringBuilder();

            query.append("SELECT * FROM `pmsi_key_status` WHERE `key_id` IN ");
            query.append(
                     "(SELECT `key_id` FROM `pmsi_guests` WHERE `guest_id`='");
            query.append(guestId);
            query.append("' AND `is_deleted`=0) AND `pmsi_status` IN ");
            query.append(
                     "(SELECT `pmsi_status_master_id` FROM `pmsi_status_master` WHERE `name`='checkin' AND `is_active`=1)");

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query.toString());

            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               status = true;
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while validating checkin status by reservation id\n",
                     e);
         }
      }

      return status;
   }

   // public boolean checkIsGuestCheckedInKey(int keyId)
   // {
   // boolean status = false;
   //
   // if (null != dvDatabaseConnector.getconnection())
   // {
   // try
   // {
   // String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id` = "
   // + keyId;
   //
   // Statement statement =
   // dvDatabaseConnector.getconnection().createStatement();
   //
   // dvLogger.info("QUERY: " + query.toString());
   //
   // ResultSet rs = statement.executeQuery(query.toString());
   //
   // if (rs.next())
   // {
   // status = true;
   // }
   //
   // rs.close();
   // statement.close();
   // }
   // catch (Exception e)
   // {
   // dvLogger.error(
   // "Exception while validating checkin status by reservation id\n",
   // e);
   // }
   // }
   //
   // return status;
   // }


   public boolean checkIsGuestCheckedInKey(int pmsiId, int keyId)
   {
      boolean status = false;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query = "SELECT * FROM `pmsi_key_status` WHERE `key_id` = "
                     + keyId + " and `pmsi_guest_id`=" + pmsiId;
            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query.toString());

            ResultSet rs = statement.executeQuery(query.toString());

            if (rs.next())
            {
               status = true;
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while validating checkin status by reservation id\n",
                     e);
         }
      }

      return status;
   }

   public DVGuestPreferenceModel getGuestPreferenceDataByReservation(
            String reservationId)
   {
      DVGuestPreferenceModel guestPreferenceData = new DVGuestPreferenceModel();

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query =
                     "SELECT * FROM `pmsi_guest_preferences` WHERE `reservation_number`='"
                              + reservationId + "'";

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            dvLogger.info("QUERY: " + query);

            ResultSet rs = statement.executeQuery(query);

            if (rs.next())
            {
               guestPreferenceData.setTemperature(rs.getString("temperature"));
               guestPreferenceData
                        .setMoodId(getDvcMoodByMoodId(rs.getInt("mood_id")));
               guestPreferenceData.setFragrance(rs.getString("fragrance"));
            }

            rs.close();
            statement.close();

            dvLogger.info("Guest preference add status: "
                     + guestPreferenceData.toString());
         }
         catch (Exception e)
         {
            dvLogger.error("Exception while adding guest preference\n", e);
         }
      }

      return guestPreferenceData;
   }

   public List<String> getAllActiveGuestIds()
   {
      List<String> guestIds = new ArrayList<>();

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query =
                     "SELECT `guest_id` FROM `pmsi_guests` WHERE `is_deleted`=0";

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            ResultSet rs = statement.executeQuery(query);

            while (rs.next())
            {
               guestIds.add(rs.getString("guest_id"));
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error("Error while fetching the active guest ids\n", e);
         }
      }

      return guestIds;
   }

   public boolean isNeedToUpdateGuestInfo(String guestId, String guestName,
            String guestTitle, String guestFirstName, String guestLastName,
            String guestFullName, String guestLanguage, String emailId,
            String phoneNumber, String groupCode, String dateOfBirth,
            String nationality, String departureDate, String arrivalDate,
            boolean revisitFlag, boolean vipStatus)
   {

      boolean isNeedToUpdate = false;

      if (null != dvDatabaseConnector.getconnection())
      {
         try
         {
            String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id`='"
                     + guestId + "' AND `is_deleted`=0";

            Statement statement =
                     dvDatabaseConnector.getconnection().createStatement();

            ResultSet rs = statement.executeQuery(query);

            while (rs.next())
            {
               String guestTitleDb = rs.getString("guest_title");
               String guestFirstNameDb = rs.getString("guest_first_name");
               String guestLastNameDb = rs.getString("guest_last_name");
               String guestNameDb = rs.getString("guest_name");
               String guestFullNameDb = rs.getString("guest_full_name");
               String guestLanguageDb = rs.getString("guest_language");
               String groupCodeDb = rs.getString("group_code");
               String dateOfBirthDb = rs.getString("date_of_birth");
               String guestDepartureDateDb = rs.getString("guest_departure");
               /*
                * rs.getString("vip_status"); rs.getString("nationality");
                */

               // dvLogger.info("guestTitleDb " + guestTitleDb);
               // dvLogger.info("guestFirstNameDb " + guestFirstNameDb);
               // dvLogger.info("guestLastNameDb " + guestLastNameDb);
               // dvLogger.info("guestNameDb " + guestNameDb);
               // dvLogger.info("guestFullNameDb " + guestFullNameDb);
               // dvLogger.info("guestLanguageDb " + guestLanguageDb);
               // dvLogger.info("groupCodeDb " + groupCodeDb);
               // dvLogger.info("dateOfBirthDb " + dateOfBirthDb);
               // dvLogger.info("guestDepartureDateDb " + guestDepartureDateDb);

               if (null != guestTitle && !"".equalsIgnoreCase(guestTitle)
                        && !guestTitle.equalsIgnoreCase(guestTitleDb))
               {
                  dvLogger.info("True from guestTitle");
                  isNeedToUpdate = true;
                  break;
               }

               if (null != guestFirstName
                        && !"".equalsIgnoreCase(guestFirstName)
                        && !guestFirstName.equalsIgnoreCase(guestFirstNameDb))
               {
                  dvLogger.info("True from guestFirstName");
                  isNeedToUpdate = true;
                  break;
               }

               if (null != guestLastName && !"".equalsIgnoreCase(guestLastName)
                        && !guestLastName.equalsIgnoreCase(guestLastNameDb))
               {
                  dvLogger.info("True from guestLastName");
                  isNeedToUpdate = true;
                  break;
               }

               if (null != guestName && !"".equalsIgnoreCase(guestName)
                        && !guestName.equalsIgnoreCase(guestNameDb))
               {
                  dvLogger.info("True from guestName");
                  isNeedToUpdate = true;
                  break;
               }

               if (null != guestFullName && !"".equalsIgnoreCase(guestFullName)
                        && !guestFullName.equalsIgnoreCase(guestFullNameDb))
               {
                  dvLogger.info("True from guestFullName");
                  isNeedToUpdate = true;
                  break;
               }

               if (null != guestLanguage && !"".equalsIgnoreCase(guestLanguage)
                        && !guestLanguage.equalsIgnoreCase(guestLanguageDb))
               {
                  dvLogger.info("True from guestLanguage");
                  isNeedToUpdate = true;
                  break;
               }

               if (null != groupCode && !"".equalsIgnoreCase(groupCode)
                        && !groupCode.equalsIgnoreCase(groupCodeDb))
               {
                  dvLogger.info("True from guestFirstName");
                  isNeedToUpdate = true;
                  break;
               }

               if (null != dateOfBirth && !"".equalsIgnoreCase(dateOfBirth)
                        && !dateOfBirth.equalsIgnoreCase(dateOfBirthDb))
               {
                  dvLogger.info("True from dateOfBirth");
                  isNeedToUpdate = true;
                  break;
               }

               if (null != departureDate && !"".equalsIgnoreCase(departureDate)
                        && departureDate.equalsIgnoreCase(guestDepartureDateDb))
               {
                  dvLogger.info("True from departureDate");
                  isNeedToUpdate = true;
                  break;
               }
            }

            rs.close();
            statement.close();
         }
         catch (Exception e)
         {
            dvLogger.error(
                     "Exception while checking if guest info update required\n",
                     e);
         }
      }

      return isNeedToUpdate;
   }

   public float getMovieRate(String movieName, int key_id)
   {
      float price = 0.0f;
      try
      {

         String query =
                  "SELECT * FROM `movie_key_data` WHERE `movie_name` LIKE '"
                           + movieName + "' AND `key_id` = " + key_id;

         Statement statement =
                  dvDatabaseConnector.getconnection().createStatement();

         dvLogger.info("QUERY: " + query);

         ResultSet rs = statement.executeQuery(query);

         if (rs.next())
         {
            price = rs.getFloat("price");
         }

         rs.close();
         statement.close();

      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting movie rate ", e);
      }
      return price;
   }

   public String getGuestIdByKey(int keyId, String guestType)
   {
      String id = "";
      try
      {

         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_guests` WHERE `key_id` = " + keyId
                  + " and `guest_type`='" + guestType
                  + "' and `is_deleted`='0' ";

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            id = rs.getString("guest_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting guest ID ", e);
      }
      return id;
   }

   public void insertGuestBillAmount(String guestid, String roomno, String date,
            String time, String amount)
   {
      try
      {
         // guestid, roomno,
         // date, time, amount
         String roomId = getPmsiRoomId(roomno);
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String Updatequery = "INSERT into pmsi_bill_amount"
                  + " (`guest_id`, `room_number`,`amount`,`date`,`time`,`created_on`) "
                  + "values('" + guestid + "','" + roomId + "','" + amount
                  + "','" + date + "','" + time + "',NOW() )";
         dvLogger.info("INSERT: " + Updatequery + "  "
                  + stmt.executeUpdate(Updatequery));

         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating  ", e);
      }

   }

   public String getGuestBillAmount(String guestid, String roomno)
   {
      String amount = "na";
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "select `amount` from pmsi_bill_amount"
                  + " where `guest_id` LIKE '" + guestid + "' and "
                  + "`room_number` LIKE " + roomno;

         ResultSet rs = null;
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            amount = rs.getString("amount");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting bill amount  ", e);
      }
      return amount;
   }


   public void deleteGuestBillAmount(String guestid, String roomno)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "DELETE from pmsi_bill_amount"
                  + " where `guest_id` LIKE '" + guestid + "' and "
                  + "`room_number` LIKE " + roomno;

         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting bill amount  ", e);
      }
   }

   public void insertRemoteCheckoutGuestId(String targedDeviceId,
            String guestId, String room_number)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "INSERT INTO `pmsi_remote_checkout_devices`"
                  + "(`device_id`, `target_device_id`, `guest_id`"
                  + ",`room_number`) " + "VALUES " + "(NULL," + "'"
                  + targedDeviceId + "','" + room_number + "','" + guestId
                  + "')";
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in checkout request ", e);
      }
   }

   public int getRemoteCheckoutTargetDeviceId(String roomNumber, String guestId)
   {
      int targetDeviceId = 0;

      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "SELECT `target_device_id` from `pmsi_remote_checkout_devices`"
                           + " where `guest_id` LIKE '" + guestId + "' and "
                           + "`room_number` LIKE '" + roomNumber + "'";
         dvLogger.info(query);
         ResultSet rs = null;
         rs = stmt.executeQuery(query);
         if (rs.next())
         {
            targetDeviceId = rs.getInt("target_device_id");
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in remote checkout request ", e);
      }
      return targetDeviceId;

   }



   public void deleteRemoteCheckoutTargetDeviceId(String roomNumber,
            String guestId)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "DELETE from pmsi_remote_checkout_devices"
                  + " where `guest_id` LIKE '" + guestId + "' and "
                  + "`room_number` LIKE '" + roomNumber + "'";
         dvLogger.info("Query ::"+ query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in remote checkout request ", e);
      }

   }

   public ArrayList<String> getActiveGuestIds(int keyId)
   {
      ArrayList<String> guestIds = new ArrayList<String>();
      try
      {
         dvLogger.info("get Active Guest Ids");
         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT * FROM `pmsi_guests` WHERE `key_id` = " + keyId
                  + " and `is_deleted` =0";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            guestIds.add(rs.getString("guest_id"));
         }
         rs.close();
         stmt.close();
         dvLogger.info("guestIds:  " + guestIds);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating In Room Devices ", e);
      }
      return guestIds;

   }

   public void setInterfaceStatus(int connectionStatus, int linkStaus,
            String logMessage)
   {
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query =
                  "INSERT INTO `troubleshooting_interface` (`log_id`, `interface_type`, `status`, `link_status`, `log_notes`) "
                           + "VALUES " + "(NULL, 'PMSI', '" + connectionStatus
                           + "', '" + linkStaus + "', '" + logMessage + "' )";
         dvLogger.info(query);
         stmt.executeUpdate(query);
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in inserting in trouble shooting interface ", e);
      }

   }

   public void closeResultSet(ResultSet resultSet)
   {
      try
      {
         if (null != resultSet)
         {
            resultSet.close();
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in closing result set ", e);
      }
   }

   public void getUnSyncedKeysForMovie()
   {
      try
      {
         ArrayList<Integer> keys = new ArrayList<Integer>();
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query =
                  "SELECT DISTINCT `key_id` FROM `keys` WHERE `key_id` NOT IN (SELECT DISTINCT `key_id` from `movie_key_data`)";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            keys.add(rs.getInt("key_id"));
         }
         rs.close();
         stmt.close();
         for (int key : keys)
         {
            try
            {
               Statement stmt1 =
                        dvDatabaseConnector.getconnection().createStatement();
               ResultSet rs1 = null;
               rs1 = stmt1.executeQuery(
                        "SELECT * FROM `movie_master` WHERE `is_deleted`=0");
               while (rs1.next())
               {
                  boolean is_chargable = false;
                  String movieName = rs1.getString("movie_id");
                  float price = rs1.getFloat("rates");
                  if (price > 0.0)
                  {
                     is_chargable = true;
                  }
                  else
                  {
                     is_chargable = false;
                  }
                  Statement stmt2 = dvDatabaseConnector.getconnection()
                           .createStatement();
                  query = "INSERT INTO `movie_key_data`"
                           + "(`movie_key_data_id`, `movie_name`, `key_id`"
                           + ",`price`,`is_chargable`) " + "VALUES " + "(NULL,"
                           + "'" + movieName + "'," + key + "," + price + ",'"
                           + is_chargable + "'  )";
                  dvLogger.info(query);
                  stmt2.executeUpdate(query);
                  stmt2.close();
               }
               rs1.close();
               stmt1.close();
            }
            catch (Exception e)
            {
               dvLogger.error("Error in inserting movies into database ", e);
            }
         }


      }
      catch (Exception e)
      {
         dvLogger.error("Error in inserting movies into database ", e);
      }
   }

   private void updatePrice()
   {
      try
      {
         Statement stmt1 =
                  dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs1 = null;
         rs1 = stmt1.executeQuery(
                  "SELECT * FROM `movie_master` WHERE `is_deleted`=0");
         while (rs1.next())
         {
            boolean is_chargable = false;
            String movieName = rs1.getString("movie_id");
            float price = rs1.getFloat("rates");
            if (price > 0.0)
            {
               is_chargable = true;
            }
            else
            {
               is_chargable = false;
            }
            String query = "UPDATE `movie_key_data` SET `price`='" + price
                     + "',`is_chargable`='" + is_chargable
                     + "' WHERE `movie_name` LIKE '" + movieName + "'";
            Statement stmt2 =
                     dvDatabaseConnector.getconnection().createStatement();
            dvLogger.info("Updated rows:  " + stmt2.executeUpdate(query));
            stmt2.close();
         }
         rs1.close();
         stmt1.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in inserting movies into database ", e);
      }
   }

   class SyncMovieRecords extends Thread
   {
      public void run()
      {
         try
         {
          
            while (true)
            {
               try
               {
                  Thread.sleep(60 * 1000 * 60);
                  getUpdatedMovieList();
                  getUnSyncedKeysForMovie();
                  populatePendingMovieDatabase();
                  updatePrice();
               }
               catch (Exception e)
               {
                  dvLogger.error("Error in syncing movie ", e);
                  Thread.sleep(60 * 1000 * 60);
               }
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Error in syncing movie records ", e);
         }
      }
   }

   private String createSqlQuery(String request_params,
            List<Items> condition_params, String conditional_operator)
   {
      String str = "Select * from `pmsi_guests` where ";
      String query = "";
      for (Items i : condition_params)
      {
         if (conditional_operator != null
                  && !"".equalsIgnoreCase(conditional_operator))
         {
            if (i.getConditionalColumn().equals("guest_arrival")
                     || i.getConditionalColumn().equals("guest_departure")
                     || i.getConditionalColumn().equals("checkin_time")
                     || i.getConditionalColumn().equals("checkout_time"))
            {
               query = query + "`" + i.getConditionalColumn() + "` "
                        + i.getConditionalOperator() + " CAST('"
                        + i.getConditionalValue() + "' AS DATE) "
                        + conditional_operator + " ";
            }
            else
            {
               query = query + "`" + i.getConditionalColumn() + "` "
                        + i.getConditionalOperator() + " '"
                        + i.getConditionalValue() + "' " + conditional_operator
                        + " ";
            }
         }
         else
         {
            if (i.getConditionalColumn().equals("guest_arrival")
                     || i.getConditionalColumn().equals("guest_departure")
                     || i.getConditionalColumn().equals("checkin_time")
                     || i.getConditionalColumn().equals("checkout_time"))
            {
               query = query + "`" + i.getConditionalColumn() + "` "
                        + i.getConditionalOperator() + " CAST('"
                        + i.getConditionalValue() + "' AS DATE) ";
            }
            else
            {
               query = query + "`" + i.getConditionalColumn() + "` "
                        + i.getConditionalOperator() + " '"
                        + i.getConditionalValue() + "' ";
            }
         }
         dvLogger.info("After Condition Applied : " + query);
      }
      if (query.endsWith("AND "))
      {
         dvLogger.info("Final Query " + str
                  + query.substring(0, query.length() - 4));
         query = str + query.substring(0, query.length() - 4);
      }
      else if (query.endsWith("OR "))
      {
         dvLogger.info("Final Query " + str
                  + query.substring(0, query.length() - 3));
         query = str + query.substring(0, query.length() - 3);
      }
      else
      {
         dvLogger.info("Final Query " + str + query);
         query = str + query;
      }
      return query;
   }

   public List<Map<String, String>> getUserDetails(String request_params,
            List<Items> condition_params, String conditional_operator,
            List<Map<String, String>> guestResult)
   {
      List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
      try
      {
         String query = createSqlQuery(request_params, condition_params,
                  conditional_operator);
         Statement stmt1 =
                  dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs1 = null;
         rs1 = stmt1.executeQuery(query);
         while (rs1.next())
         {
            Map<String, String> data = new HashMap<>();
            data.put("pmsi_guest_id", rs1.getInt("pmsi_guest_id") + "");
            data.put("key_id", rs1.getInt("key_id") + "");
            data.put("guest_id", rs1.getString("guest_id"));
            data.put("guest_title", rs1.getString("guest_title"));
            data.put("guest_first_name", rs1.getString("guest_first_name"));
            data.put("guest_last_name", rs1.getString("guest_last_name"));
            data.put("guest_name", rs1.getString("guest_name"));
            data.put("guest_full_name", rs1.getString("guest_full_name"));
            data.put("guest_incognito_name",
                     rs1.getString("guest_incognito_name"));
            data.put("guest_alternate_name",
                     rs1.getString("guest_alternate_name"));
            data.put("guest_language", rs1.getString("guest_language"));
            data.put("safe_flag", rs1.getString("safe_flag"));
            data.put("checkin_time", rs1.getString("checkin_time"));
            data.put("checkout_time", rs1.getString("checkout_time"));
            data.put("bill_amount", rs1.getString("bill_amount"));
            data.put("remote_checkout", rs1.getString("remote_checkout"));
            data.put("group_code", rs1.getString("group_code"));
            data.put("unique_id", rs1.getString("unique_id"));
            data.put("tv_rights", rs1.getString("tv_rights"));
            data.put("is_adult", rs1.getString("is_adult"));
            data.put("email", rs1.getString("email"));
            data.put("phone_number", rs1.getString("phone_number"));
            data.put("reservation_id", rs1.getString("reservation_id"));
            data.put("video_rights", rs1.getString("video_rights"));
            data.put("vip_status", rs1.getString("vip_status"));
            data.put("guest_type", rs1.getString("guest_type"));
            data.put("revisit_flag", rs1.getString("revisit_flag"));
            data.put("guest_arrival", rs1.getString("guest_arrival"));
            data.put("guest_departure", rs1.getString("guest_departure"));
            data.put("date_of_birth", rs1.getString("date_of_birth"));
            data.put("nationality", rs1.getString("nationality"));
            data.put("previous_visit_date",
                     rs1.getString("previous_visit_date"));
            data.put("hotel_id", rs1.getString("hotel_id"));
            data.put("is_deleted", rs1.getString("is_deleted"));
            data.put("created_on", rs1.getString("created_on"));
            data.put("modified_on", rs1.getString("modified_on"));
            dvLogger.info("Data is Here : " + data.entrySet());
            listData.add(data);

            fatchGuestData(guestResult, rs1);

         }
         stmt1.close();
         rs1.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception fetching guest details: ", e);
      }
      return listData;
   }


   private void fatchGuestData(List<Map<String, String>> guestResult,
            ResultSet rs1) throws SQLException
   {
      Map<String, String> guestData = new HashMap<>();
      guestData.put("pmsiGuestId", rs1.getInt("pmsi_guest_id") + "");
      guestData.put(DVPmsData.keyId.toString(), rs1.getInt("key_id") + "");
      guestData.put(DVPmsData.guestId.toString(), rs1.getString("guest_id"));
      guestData.put(DVPmsData.guestTitle.toString(),
               rs1.getString("guest_title"));
      guestData.put(DVPmsData.guestFirstName.toString(),
               rs1.getString("guest_first_name"));
      guestData.put(DVPmsData.guestLastName.toString(),
               rs1.getString("guest_last_name"));
      guestData.put(DVPmsData.guestName.toString(),
               rs1.getString("guest_name"));
      guestData.put(DVPmsData.guestFullName.toString(),
               rs1.getString("guest_full_name"));
      guestData.put(DVPmsData.incognitoName.toString(),
               rs1.getString("guest_incognito_name"));
      guestData.put(DVPmsData.alternateName.toString(),
               rs1.getString("guest_alternate_name"));
      guestData.put(DVPmsData.guestLanguage.toString(),
               rs1.getString("guest_language"));
      guestData.put(DVPmsData.safeFlag.toString(), rs1.getString("safe_flag"));
      guestData.put("checkinTime", rs1.getString("checkin_time"));
      guestData.put("checkoutTime", rs1.getString("checkout_time"));
      guestData.put("billAmount", rs1.getString("bill_amount"));
      guestData.put(DVPmsData.remoteCheckout.toString(),
               rs1.getString("remote_checkout"));
      guestData.put(DVPmsData.groupCode.toString(),
               rs1.getString("group_code"));
      guestData.put(DVPmsData.uniqueId.toString(), rs1.getString("unique_id"));
      guestData.put(DVPmsData.tvRights.toString(), rs1.getString("tv_rights"));
      guestData.put(DVPmsData.isAdult.toString(), rs1.getString("is_adult"));
      guestData.put(DVPmsData.emailId.toString(), rs1.getString("email"));
      guestData.put(DVPmsData.phoneNumber.toString(),
               rs1.getString("phone_number"));
      guestData.put(DVPmsData.reservationId.toString(),
               rs1.getString("reservation_id"));
      guestData.put(DVPmsData.videoRights.toString(),
               rs1.getString("video_rights"));
      guestData.put(DVPmsData.vipStatus.toString(),
               rs1.getString("vip_status"));
      guestData.put(DVPmsData.guestType.toString(),
               rs1.getString("guest_type"));
      guestData.put(DVPmsData.revisitFlag.toString(),
               rs1.getString("revisit_flag"));
      guestData.put(DVPmsData.arrivalDate.toString(),
               rs1.getString("guest_arrival"));
      guestData.put(DVPmsData.departureDate.toString(),
               rs1.getString("guest_departure"));
      guestData.put(DVPmsData.dateOfBirth.toString(),
               rs1.getString("date_of_birth"));
      guestData.put(DVPmsData.nationality.toString(),
               rs1.getString("nationality"));
      guestData.put(DVPmsData.previousVisitDate.toString(),
               rs1.getString("previous_visit_date"));
      guestData.put("hotelId", rs1.getString("hotel_id"));
      guestData.put("isDeleted", rs1.getString("is_deleted"));
      guestData.put("createdOn", rs1.getString("created_on"));
      guestData.put("modifiedOn", rs1.getString("modified_on"));
      guestResult.add(guestData);
   }


   public boolean fetchUniqueIdByGuestId(String id)
   {
      boolean result = false;
      try
      {

         ResultSet rs = null;
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         String query = "SELECT * FROM `pmsi_guests` WHERE `guest_id` = '" + id
                  + "' and `is_deleted` =0";

         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            result = true;
         }
         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in populating guestId's ", e);
      }
      return result;
   }


   public boolean fetchBillByUniqueId(String pmsi_bill_id)
   {
      boolean result = false;
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query = "SELECT * FROM `pmsi_bill` WHERE `pmsi_bill_id` = '"
                  + pmsi_bill_id + "'";
         dvLogger.info(query);
         rs = stmt.executeQuery(query);
         while (rs.next())
         {
            result = true;
         }

         rs.close();
         stmt.close();
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting bill data ", e);
      }
      return result;
   }



   public int getDeviceIdByDvcKey(int keyId)
   {

      int deviceId = 0;
      try
      {
         Statement stmt = dvDatabaseConnector.getconnection().createStatement();
         ResultSet rs = null;
         String query1 = "SELECT * FROM `in_room_devices` WHERE `key_id` = "
                  + keyId + " and `device_type_id` in ("
                  + this.getAllControllerTypeId("ipad") + ") ";
         dvLogger.info(query1);
         rs = stmt.executeQuery(query1);
         if (rs.next())
         {
            deviceId = rs.getInt("in_room_device_id");
         }
         rs.close();
         stmt.close();
         dvLogger.info("In Room Device ID's  " + deviceId);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting controller list ", e);
      }
      return deviceId;
   }

}
