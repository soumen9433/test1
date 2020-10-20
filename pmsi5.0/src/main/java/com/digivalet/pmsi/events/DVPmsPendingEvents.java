package com.digivalet.pmsi.events;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPendingCheckinCheckout;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPmsPendingEvents extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private ArrayList<Integer> dvPendingEventKeyIds = new ArrayList<Integer>();
   private final int coreSize = 20;
   private final int MaximumSize = 20;
   private final int delay = 10000;
   private DVKeyCommunicationTokenManager communicationTokenManager;

   public DVPmsPendingEvents(DVSettings dvSettings, DVPmsDatabase dvPmsDatabase,
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
         Thread.currentThread().setName("PENDING-PMS-EVENTS");
         dvLogger.info("Starting Dv Pending PMS event thread  ");
         try
         {
            BlockingQueue<Runnable> threadPool =
                     new LinkedBlockingQueue<Runnable>();
            ThreadPoolExecutor executorService =
                     new ThreadPoolExecutor(coreSize, MaximumSize, 0L,
                              TimeUnit.MILLISECONDS, threadPool);
            ThreadMonitor threadMonitor =
                     new ThreadMonitor(executorService, dvPendingEventKeyIds);
            threadMonitor.start();
            while (true)
            {
               try
               {
                  ArrayList<Integer> Key_Ids = dvPmsDatabase.getPendingEvents();
                  if (null != Key_Ids)
                  {
                     if (Key_Ids.size() > 0)
                     {
                        for (int keyid : Key_Ids)
                        {
                           if (!dvPendingEventKeyIds.contains(keyid))
                           {
                              Map<String, String> data = dvPmsDatabase
                                       .getPendingDataByPmsiKeyStatusId(keyid);
                              int Key = Integer.parseInt(data.get("key_id"));
                              String guestType = data.get("guest_type");
                              int pmsiId = Integer
                                       .parseInt(data.get("pmsi_guest_id"));
                              int status = Integer
                                       .parseInt(data.get("digivalet_status"));
                              try
                              {
                                 DVPendingCheckinCheckout pendingEvent =
                                          new DVPendingCheckinCheckout(keyid,
                                                   dvPmsDatabase, dvSettings,
                                                   Key, guestType, pmsiId,
                                                   status, dvPendingEventKeyIds,
                                                   communicationTokenManager);
                                 dvLogger.info("Submitting Pending Event for "
                                          + Key);
                                 executorService.submit(pendingEvent);
                                 dvPendingEventKeyIds.add(keyid);
                              }
                              catch (Exception e)
                              {
                                 dvLogger.error("Error in adding events ", e);
                              }

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

   class ThreadMonitor extends Thread
   {
      ThreadPoolExecutor executorService;
      ArrayList<Integer> dvPendingEventKeyIds;

      public ThreadMonitor(ThreadPoolExecutor executorService,
               ArrayList<Integer> dvPendingEventKeyIds)
      {
         this.executorService = executorService;
         this.dvPendingEventKeyIds = dvPendingEventKeyIds;
      }

      public void run()
      {
         try
         {
            Thread.currentThread().setName("THREAD_MONITOR");
            while (true)
            {
               try
               {
                  dvLogger.info("Active " + executorService.getActiveCount()
                           + "  total task " + executorService.getTaskCount()
                           + "Completed Task "
                           + executorService.getCompletedTaskCount() + " "
                           + executorService.getQueue().size());
                  dvLogger.info(" ArrayList<Integer> dvPendingEventKeyIds "
                           + dvPendingEventKeyIds.toString());
                  Set<Thread> threads = Thread.getAllStackTraces().keySet();

                  for (Thread t : threads)
                  {
                     String name = t.getName();
                     Thread.State state = t.getState();
                     int priority = t.getPriority();
                     String type = t.isDaemon() ? "Daemon" : "Normal";
                     dvLogger.info(("ACTIVE THREAD:  " + name + "," + state
                              + "," + priority + "," + type));
                  }

                  Runtime.getRuntime().freeMemory();
                  Thread.sleep(60 * 1000);
               }
               catch (Exception e)
               {
                  try
                  {
                     Thread.sleep(60 * 1000);
                  }
                  catch (Exception e2)
                  {
                     // TODO: handle exception
                  }
                  dvLogger.error("Error in thread monitor ", e);
               }
            }

         }
         catch (Exception e)
         {
            dvLogger.error("Error in checking ", e);
         }
      }
   }
}
