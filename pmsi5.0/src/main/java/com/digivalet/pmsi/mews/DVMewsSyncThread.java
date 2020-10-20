package com.digivalet.pmsi.mews;

import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.mews.models.MewsMasterReservationData;
import com.digivalet.pmsi.settings.DVSettings;

public class DVMewsSyncThread extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private DVPmsMews dvPmsMews;
   
   public DVMewsSyncThread(DVSettings dvSettings, DVPmsDatabase dvPmsDatabase, DVPmsMews dvPmsMews)
   {
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvPmsMews = dvPmsMews;
   }
   
   @Override
   public void run()
   {
      try
      {
         
         MewsMasterReservationData data =dvPmsMews.dvMewsClient.sync();
         
         dvLogger.info("MewsMasterReservationData  ;;;;   "+ data);
         
         DVParseData dvParseData = new DVParseData(new JSONObject(), dvPmsDatabase, dvSettings,dvPmsMews);
         dvParseData.sync(data);
      }
      catch(Exception e)
      {
         dvLogger.error("Exception while executing sync request\n", e);
      }
   }
}
