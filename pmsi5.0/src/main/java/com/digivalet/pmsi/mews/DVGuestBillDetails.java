package com.digivalet.pmsi.mews;

import javax.xml.bind.JAXBException;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.exceptions.DVFileException;
import com.digivalet.pmsi.settings.DVSettings;

public class DVGuestBillDetails extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVPmsDatabase dvPmsDatabase;
   private DVSettings dvSettings;
   private DVPmsMews dvPmsMews;
   private String guestId;
   private String roomNumber;


   public DVGuestBillDetails(DVPmsDatabase dvPmsDatabase, DVSettings dvSettings,
            DVPmsMews dvPmsMews, String guestId, String roomNumber)
   {
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvPmsMews=dvPmsMews;
      this.guestId = guestId;
      this.roomNumber = roomNumber;
   }

   @Override
   public void run()
   {
      try
      {
         DVParseData dvParseData = new DVParseData(new JSONObject(),
                  dvPmsDatabase, dvSettings,dvPmsMews);
         dvParseData.getBill(guestId, roomNumber);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while fetching ", e);
      }
   }
}
