package com.digivalet.core;

import com.digivalet.pmsi.DVPms;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.mews.DVPmsMews;
import com.digivalet.pmsi.opera.DVPmsOpera;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPmsFactory
{
   static DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;

   public DVPmsFactory(DVSettings dvSettings, DVPmsDatabase dvPmsDatabase)
   {
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
   }

   public DVPms getPms()
   {
      DVPms dvPms = null;
      try
      {
         dvLogger.info("PMS VENDOR: " + dvSettings.getPmsVendor());
         System.out.println("PMS VENDOR: " + dvSettings.getPmsVendor());

         switch (dvSettings.getPmsVendor().toUpperCase())
         {
            case "OPERA":
               dvPms = new DVPmsOpera(dvSettings, dvPmsDatabase);
               dvLogger.info("PMSI Adapter initialized for driver name : "
                        + dvSettings.getPmsVendor().toUpperCase());
               break;

            case "MEWS":
               dvPms = new DVPmsMews(dvSettings, dvPmsDatabase);
               dvLogger.info("IRD Adapter initialized for driver name : "
                        + dvSettings.getPmsVendor().toUpperCase());
               break;

            case "OPERASERVER":
               dvPms = new com.digivalet.pmsi.operaserver.DVPmsOpera(dvSettings,
                        dvPmsDatabase);
               dvLogger.info("PMSI Adapter initialized for driver name : "
                        + dvSettings.getPmsVendor().toUpperCase());
               break;
         }

         return dvPms;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in initializing PMS adapter ", e);
      }
      return dvPms = new DVPmsOpera(dvSettings, dvPmsDatabase);

   }
}
