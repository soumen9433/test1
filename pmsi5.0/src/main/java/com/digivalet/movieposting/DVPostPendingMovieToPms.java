package com.digivalet.movieposting;

import java.util.ArrayList;
import com.digivalet.core.AlertState;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.DVPmsController;
import com.digivalet.pmsi.database.DVPmsDatabase;

public class DVPostPendingMovieToPms extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVPmsDatabase dvPmsDatabase;
   private DVPmsController dvPmsController;
   private final int moviePostingDelay = 10 * 1000;

   public DVPostPendingMovieToPms(DVPmsDatabase dvPmsDatabase,
            DVPmsController dvPmsController)
   {
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvPmsController = dvPmsController;
   }

   public void run()
   {
      try
      {
         Thread.currentThread().setName("PENDING-MOVIE-POSTING-PMS");
         dvLogger.info("Initializing Post Pending to PMS ");
         while (true)
         {
            try
            {
               ArrayList<Integer> pendingIds =
                        dvPmsDatabase.getPendingMoviesToPostToPMS();
               AlertState connnectionStatus =
                        dvPmsController.connectionStatus();
               dvLogger.info(" Pending Movies " + pendingIds.toString() + "  "
                        + connnectionStatus);
               if (null != pendingIds && !pendingIds.isEmpty()
                        && connnectionStatus == AlertState.Connected)
               {
                  for (int pendingid : pendingIds)
                  {
                     dvPmsController.postPendingMovie(pendingid);
                  }
               }
               else
               {
                  dvLogger.info("Nothing pending to post ");
                  Thread.sleep(moviePostingDelay);
               }
               Thread.sleep(moviePostingDelay);


            }
            catch (Exception e)
            {
               dvLogger.error("Error in sending pending movies to PMS ", e);
            }
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error ", e);
      }
   }

}
