package com.digivalet.movieposting;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPendingMovies extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private ArrayList<Integer> dvPendingEventDvcIds=new ArrayList<Integer>();
   private final int coreSize=20;
   private final int MaximumSize=20;
   private final int delay=10000;
   private DVKeyCommunicationTokenManager communicationTokenManager;
   public DVPendingMovies(DVSettings dvSettings,
            DVPmsDatabase dvPmsDatabase,
            DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      this.communicationTokenManager = communicationTokenManager;
   }

   public void run()
   {
      try
      {
         Thread.currentThread().setName("PENDING-MOVIE-EVENTS");
          dvLogger.info("Starting Dv Pending PMS Movie event thread  ");
         try
         {
             BlockingQueue<Runnable> threadPool = new LinkedBlockingQueue<Runnable>();
             ThreadPoolExecutor executorService = new 
                    ThreadPoolExecutor(coreSize, MaximumSize, 0L, TimeUnit.MILLISECONDS, threadPool);
             while(true)
            {
               try
               {
                  ArrayList<Integer> DVC_Ids =dvPmsDatabase.getPendingMovieEvents();
                  if(null!=DVC_Ids)
                  {
                     if(DVC_Ids.size()>0)
                     {
                        for (int dvcid : DVC_Ids)
                        {
                           if(!dvPendingEventDvcIds.contains(dvcid))
                           {
                               try 
                               {
                                  dvPendingEventDvcIds.add(dvcid); 
                                  DVSendPendingMovieToDevices pendingEvent =
                                           new DVSendPendingMovieToDevices(dvcid,dvPmsDatabase,
                                                    dvSettings,dvPendingEventDvcIds,communicationTokenManager);   
                                  dvLogger.info("Submitting Pending movie Event for "+dvcid);
                                  executorService.submit(pendingEvent);
                               } catch (Exception e) {
                                   dvLogger.error("Error in adding movie events ", e);
                               }
                                   
                           }else
                           {
//                               dvLogger.info("DVCID  "+dvcid+" Already added ");
                           }
                           
                        }
                     } 
                  }
                  Thread.sleep(delay);
               }
               catch (Exception e)
               {
                   dvLogger.error("Error in Pending Pms events", e);
                  try
                  {
                     Thread.sleep(delay);
                  }
                  catch (Exception e2)
                  {
                     dvLogger.error("Error in Sleep of error", e2);
                  }
               }
            }
            
            
         }
         catch (Exception e)
         {
            dvLogger.error("Error in getting processing pending PMS Events", e);
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in Pending PMS Events", e);
      }
   }

}
