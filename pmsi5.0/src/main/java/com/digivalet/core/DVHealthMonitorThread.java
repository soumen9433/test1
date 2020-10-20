package com.digivalet.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.digivalet.pmsi.database.DVDatabaseConnector;
import com.digivalet.pmsi.serviceStatus.model.ModuleStatus;
import com.digivalet.pmsi.serviceStatus.model.ModuleStatus.StatusEnum;
import com.digivalet.pmsi.serviceStatus.model.Status;
import com.digivalet.pmsi.serviceStatus.model.Status.DatabaseConnectivityEnum;
import com.digivalet.pmsi.serviceStatus.model.Status.ServiceStatusEnum;
import com.digivalet.pmsi.settings.DVSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class DVHealthMonitorThread extends Thread
{
   private static final String SERVICE_NAME = "PMSI-Service";
   private long SLEEP_TIME;
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVDatabaseConnector dvDatabaseConnector;
   private DVSettings dvSettings;
   private HashMap<RemarksKey, String> remarksMap = new HashMap<RemarksKey, String>();
   private final int InitialSleep = 30000;

   public DVHealthMonitorThread(DVSettings dvSettings, DVDatabaseConnector dvDatabaseConnector)
   {
      this.dvSettings = dvSettings;
      this.dvDatabaseConnector = dvDatabaseConnector;

      init();
   }

   private enum RemarksKey
   {
      Success,

      DB,

      Socket,

      Link,

      Module;
   }

   private void init()
   {
      SLEEP_TIME = dvSettings.getMonitorLogSeconds();

      dvLogger.info("Health-Monitoring Started");

      remarksMap.put(RemarksKey.Success, "Everything is working fine.");
      remarksMap.put(RemarksKey.DB, "Could not establish connection with Database.");
      remarksMap.put(RemarksKey.Socket, "Could not establish connection with %s.");
      remarksMap.put(RemarksKey.Link, "Link is not live with %s.");
      remarksMap.put(RemarksKey.Module, "Could not reach vendor: %s.");
   }


   public List<Status> getStatus()
   {
      /**
       * pmsInterfaceStatus = 1 (GREEN, All good) 
       * pmsInterfaceStatus = 2 (RED, Connection break)
       * pmsInterfaceStatus = 3 (Amber, Link not alive)
       */

      List<Status> statusList = new ArrayList<Status>();
      Status statusPmsi = new Status();

      try
      {
         statusPmsi.serviceName("PMSI");
         statusPmsi.serviceStatus(ServiceStatusEnum.GREEN);

         DatabaseConnectivityEnum databaseStatus = DatabaseConnectivityEnum.RED;
         String remarks = remarksMap.get(RemarksKey.DB);

         if (dvDatabaseConnector.connectionflag == 1)
         {
            databaseStatus = DatabaseConnectivityEnum.GREEN;
            remarks = remarksMap.get(RemarksKey.Success);
         }

         statusPmsi.setDatabaseConnectivity(databaseStatus);
         statusPmsi.setLastSuccessDBTimestamp(dvDatabaseConnector.getLastConnectedTimeStamp());
         statusPmsi.setRemarks(remarks);

         //Module-1
         List<ModuleStatus> moduleList = new ArrayList<ModuleStatus>();

         ModuleStatus moduleStatusPms = new ModuleStatus();

         moduleStatusPms.setModuleName("PMS");
         moduleStatusPms.setVendorName(dvSettings.getPmsVendor());

         AlertState pmsInterfaceStatus = DVPmsMain.getInstance().getDvPmsController().connectionStatus();

         if (pmsInterfaceStatus ==AlertState.Na)
         {
            moduleStatusPms.setStatus(StatusEnum.GREEN);
            moduleStatusPms.setRemarks(remarksMap.get(RemarksKey.Success));
         }
         else
            if (pmsInterfaceStatus ==AlertState.NotConnected)
            {
               moduleStatusPms.setStatus(StatusEnum.RED);
               moduleStatusPms.setRemarks(remarksMap.get(RemarksKey.Socket).replace("%s", dvSettings.getPmsVendor()));
            }
            else
               if (pmsInterfaceStatus ==AlertState.LinkNotAlive)
               {
                  moduleStatusPms.setStatus(StatusEnum.AMBER);
                  moduleStatusPms.setRemarks(remarksMap.get(RemarksKey.Link).replace("%s", dvSettings.getPmsVendor()));
               }
               else
                  if (pmsInterfaceStatus ==AlertState.LinkNotUp)
                  {
                     moduleStatusPms.setStatus(StatusEnum.AMBER);
                     moduleStatusPms.setRemarks(remarksMap.get(RemarksKey.Link).replace("%s", dvSettings.getPmsVendor()));
                  }
         moduleList.add(moduleStatusPms);
         statusPmsi.setModules(moduleList);
         statusList.add(statusPmsi);
         
      }
      catch (Exception e)
      {
         dvLogger.error("Exception in get status method\n", e);
      }
      
      return statusList;      
      
   }


   public void run()
   {
      try
      {
         Thread.sleep(InitialSleep);
      }
      catch (Exception e)
      {
         // TODO: handle exception
      }
      while (true)
      {
         try
         {
            boolean dbStatus =
                     dvDatabaseConnector.connectionflag == 1 ? true : false;
            long currentTimestamp = System.currentTimeMillis();

            DVHealthMonitorModel model = new DVHealthMonitorModel();
            AlertState pmsInterfaceStatus = DVPmsMain.getInstance()
                     .getDvPmsController().connectionStatus();

            model.setDatabaseStatus(dbStatus);
            model.setServiceStatus(true);
            model.setTimestamp(currentTimestamp);

            if (pmsInterfaceStatus == AlertState.Na)
            {
               model.setPmsConnectionStatus(true);
               model.setPmsLinkStatus(true);
            }
            else if (pmsInterfaceStatus == AlertState.Connected)
            {
               model.setPmsConnectionStatus(true);
               model.setPmsLinkStatus(true);
            }
            else if (pmsInterfaceStatus == AlertState.LinkNotAlive)
            {
               model.setPmsLinkStatus(false);
               model.setPmsConnectionStatus(true);
            }
            else if (pmsInterfaceStatus == AlertState.LinkNotUp)
            {
               model.setPmsConnectionStatus(true);
               model.setPmsLinkStatus(false);
            }

            dvLogger.info("Posting Object: " + model.toString());

            dvLogger.analytics(
                     "operation::" + DVAnalyticsEventType.serviceStatus,
                     "serviceName::" + SERVICE_NAME,
                     "details::" + convertObjectToJsonString(model));

            Thread.sleep(SLEEP_TIME);
         }
         catch (Exception e)
         {
            dvLogger.error("Exception in Health-Monitor Thread\n", e);

            try
            {
               Thread.sleep(SLEEP_TIME);
            }
            catch (InterruptedException e1)
            {
               e1.printStackTrace();
            }
         }
      }
   }


   public String convertObjectToJsonString(Object notifiedObject)
   {
      String[] filteredProperties = new String[10];
      ObjectMapper mapper = new ObjectMapper();
      FilterProvider filters = new SimpleFilterProvider().addFilter("", SimpleBeanPropertyFilter.serializeAllExcept(filteredProperties));
      String responseJson = "";
      try
      {
         responseJson = mapper.writer(filters).writeValueAsString(notifiedObject);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while converting object into json. Object:" + notifiedObject + ". Exception : ", e);
      }
      return responseJson;
   }

}
