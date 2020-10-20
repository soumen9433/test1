package com.digivalet.movieposting;

import java.util.ArrayList;
import com.digivalet.core.DVLogger;
import com.digivalet.movies.MoviePlan;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.settings.DVSettings;

public class DVUpdatePurchaseState extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVPmsDatabase dvPmsDatabase;
   private DVSettings dvSettings;
   private final int sleepTime = 60 * 1000;

   public DVUpdatePurchaseState(DVSettings dvSettings,DVPmsDatabase dvPmsDatabase)
   {
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvSettings=dvSettings;
   }

   public void run()
   {
      try
      {
         MoviePlan moviePlan = MoviePlan.fromString(dvSettings.getMoviePlan());
         if(moviePlan == MoviePlan.oneDay)
         {
            while (true)
            {
               try
               {
                  ArrayList<Integer> keyIds =
                           dvPmsDatabase.getKeyIdsWithElapsedPurchaseTime();
                  if (null != keyIds && !keyIds.isEmpty())
                  {
                     
                     for (int keyId : keyIds)
                     {
                        dvPmsDatabase.updatePurchaseStatusByKeyId(keyId);
                        ArrayList<Integer> Dvcs = dvPmsDatabase.getAllDvcByKey(keyId);
                        for (int Dvc : Dvcs)
                        {
                           dvPmsDatabase.updateMovieRoomStatus(Dvc, "false");
                        }
                     }
                     
                    
                  }

                  Thread.sleep(sleepTime);
               }
               catch (Exception e)
               {
                  dvLogger.error("Error in updating movie status ", e);
                  try
                  {
                     Thread.sleep(sleepTime);
                  }
                  catch (Exception e2)
                  {
                     // TODO: handle exception
                  }

               }
            }
         }else
         {
            dvLogger.info("Won't start thread since movie plan is not 24 hours ");
         }
         
 
      }
      catch (Exception e)
      {
         dvLogger.error("Error in updating purchase status ", e);
      }
   }

}
