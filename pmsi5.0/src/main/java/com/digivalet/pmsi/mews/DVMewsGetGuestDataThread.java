package com.digivalet.pmsi.mews;

import java.util.List;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.mews.models.MewsCustomerData;
import com.digivalet.pmsi.settings.DVSettings;

/**
 * 
 * @author lavin
 * 
 * @description Thread to get all the Guests Data from MEWS -- by all the
 *              guest-ids in the digivalet system.
 * 
 *              All the "active" guest-ids from the pmsi_guests are fetched and,
 *              a GET customer detail request is executed with the List of all
 *              those Ids in it.
 * 
 *              On MEWS Response the data is parsed and is updated in the
 *              database.
 * 
 */

public class DVMewsGetGuestDataThread implements Runnable
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private DVPmsMews dvPmsMews;

   public DVMewsGetGuestDataThread(DVSettings dvSettings,
            DVPmsDatabase dvPmsDatabase,DVPmsMews dvPmsMews)
   {
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvPmsMews=dvPmsMews;
   }

   @Override
   public void run()
   {
      dvLogger.info("Inside DVMewsGetGuestDataThread For Info Update");

      while (true)
      {
         try
         {
            List<String> guestIds = dvPmsDatabase.getAllActiveGuestIds();
            dvLogger.info("List Of Guest Ids : " + guestIds);
            if (null != guestIds && !guestIds.isEmpty())
            {
               List<MewsCustomerData> guestDataList =
                        dvPmsMews.dvMewsClient.getGuestDetailsById(guestIds);

               DVParseData dvParseData = new DVParseData(new JSONObject(),
                        dvPmsDatabase, dvSettings,dvPmsMews);

               dvLogger.info("Checking if need to update info or Not");
               for (MewsCustomerData customerData : guestDataList)
               {
                  if (dvPmsDatabase
                           .fetchUniqueIdByGuestId(customerData.getId()))
                  {
                     dvLogger.info("classifications :"
                              + customerData.getClassifications());
                     if (dvParseData.needToUpdateGuestInfo(customerData))
                     {
                        dvLogger.info("Event init for GuestInfoUpdate");
                        dvParseData.guestInformationUpdate(
                                 dvParseData.parseCustomerData(customerData));
                     }
                     else
                     {
                        dvLogger.info("No need to update Guest Information ");
                     }
                  }
               }
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Exception in Get Guest Details Thread\n", e);
         }

         try
         {
            Thread.sleep(1000 * 60);
         }
         catch (InterruptedException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

      }
   }
}
