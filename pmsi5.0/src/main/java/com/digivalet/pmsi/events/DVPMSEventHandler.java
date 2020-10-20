package com.digivalet.pmsi.events;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.digivalet.core.DVCheckinCheckoutEvent;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVUpdateNotifier;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.events.DVBillEvent.BillFeatureEventType;
import com.digivalet.pmsi.events.DVEvent.FeatureEventType;
import com.digivalet.pmsi.events.DVMessageEvent.MessageFeatureEvent;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPMSEventHandler implements DVUpdateNotifier
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVPmsDatabase dvPmsDatabase;
   private DVSettings dvSettings;
   private DVKeyCommunicationTokenManager communicationTokenManager;
   private HashMap<String, ThreadPoolExecutor> executorPool =
            new HashMap<String, ThreadPoolExecutor>();

   public DVPMSEventHandler(DVPmsDatabase dvPmsDatabase, DVSettings dvSettings,
            DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
      this.communicationTokenManager = communicationTokenManager;
      removeGarbage garbage=new removeGarbage(this);
      garbage.start();
   }

   @Override
   public void onEvent(DVEvent dvEvent)
   {
      try
      {
         
         DVCheckinCheckoutEvent checkinCheckoutEvent = null;
         ThreadPoolExecutor executorService = null;
         if (executorPool.containsKey(
                  dvEvent.getData().get(DVPmsData.keyId).toString()))
         {
            dvLogger.info(
                     "*******************************  Using Executor ***********************************");
            executorService = executorPool
                     .get(dvEvent.getData().get(DVPmsData.keyId).toString());
         }
         else
         {
            dvLogger.info(
                     "*******************************  Creating new Executor ***********************************");
            BlockingQueue<Runnable> threadPool =
                     new LinkedBlockingQueue<Runnable>();
            executorService = new ThreadPoolExecutor(1, 1, 10000L,
                     TimeUnit.MILLISECONDS, threadPool);
            executorPool.put(dvEvent.getData().get(DVPmsData.keyId).toString(),
                     executorService);

         }
         if (dvEvent
                  .getFeatureEventType() == FeatureEventType.PMSI_CHECKIN_EVENT || dvEvent
                  .getFeatureEventType() == FeatureEventType.PMSI_SAFE_CHECKIN_EVENT)
         {
            dvLogger.info("PMSI Checkin Event Data: "
                     + dvEvent.getData().toString());
            checkinCheckoutEvent = new DVCheckinCheckoutEvent(dvEvent,
                     dvSettings, dvPmsDatabase, communicationTokenManager);
            executorService.submit(checkinCheckoutEvent);
         }
         else if (dvEvent
                  .getFeatureEventType() == FeatureEventType.PMSI_CHECKOUT_EVENT|| dvEvent
                  .getFeatureEventType() == FeatureEventType.PMSI_SAFE_CHECKOUT_EVENT)
         {
            dvLogger.info("PMSI Checkout Event Data: "
                     + dvEvent.getData().toString());
            checkinCheckoutEvent = new DVCheckinCheckoutEvent(dvEvent,
                     dvSettings, dvPmsDatabase, communicationTokenManager);
            executorService.submit(checkinCheckoutEvent);
         }
         else if (dvEvent
                  .getFeatureEventType() == FeatureEventType.PMSI_GUEST_INFORMATION_UPDATE_EVENT)
         {
            dvLogger.info("PMSI guest info update  Event");
            checkinCheckoutEvent = new DVCheckinCheckoutEvent(dvEvent,
                     dvSettings, dvPmsDatabase, communicationTokenManager);
            executorService.submit(checkinCheckoutEvent);

         }
         else if (dvEvent
                  .getFeatureEventType() == FeatureEventType.PMSI_ROOMCHANGE_CHECKIN_EVENT)
         {
            dvLogger.info("PMSI Room Change Checkin Event");
            checkinCheckoutEvent = new DVCheckinCheckoutEvent(dvEvent,
                     dvSettings, dvPmsDatabase, communicationTokenManager);
            executorService.submit(checkinCheckoutEvent);

         }
         else if (dvEvent
                  .getFeatureEventType() == FeatureEventType.PMSI_ROOMCHANGE_CHECKOUT_EVENT)
         {
            dvLogger.info("PMSI Room Change Checkout Event");
            checkinCheckoutEvent = new DVCheckinCheckoutEvent(dvEvent,
                     dvSettings, dvPmsDatabase, communicationTokenManager);
            executorService.submit(checkinCheckoutEvent);

         }
         
      }
      catch (Exception e)
      {
         dvLogger.error("Error in processing event ", e);
      }
   }

   @Override
   public void onEvent(DVBillEvent dvEvent)
   {
      try
      {
         if (dvEvent
                  .getFeatureEventType() == BillFeatureEventType.PMSI_BILL_EVENT)
         {
            dvLogger.info("PMSI Bill Event");
            ExecutorService es = Executors.newSingleThreadExecutor();
            DVSendPmsBill bill = new DVSendPmsBill(dvEvent, dvSettings,
                     dvPmsDatabase, communicationTokenManager);
            es.submit(bill);
            es.shutdown();
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error ", e);
      }

   }

   @Override
   public void onEvent(DVMessageEvent dvEvent)
   {
      try
      {
         if (dvEvent
                  .getFeatureEventType() == MessageFeatureEvent.PMSI_MESSAGE_EVENT)
         {
            dvLogger.info("PMSI message Event");
            ExecutorService es = Executors.newSingleThreadExecutor();
            DvSendMessage message =
                     new DvSendMessage(dvEvent, dvSettings, dvPmsDatabase);
            es.submit(message);
            es.shutdown();
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error ", e);
      }
   }

   @Override
   public void onEvent(DVCheckoutFailEvent dvEvent)
   {
      try
      {
         if (null != dvEvent)
         {
            dvLogger.info("PMSI Checkout fail Event");
            ExecutorService es = Executors.newSingleThreadExecutor();
            DVSendCheckoutFailMessage message =
                     new DVSendCheckoutFailMessage(dvSettings, dvPmsDatabase,
                              dvEvent.getFailMessage(), dvEvent.getRoomNumber(),
                              dvEvent.getGuestId(), communicationTokenManager);
            es.submit(message);
            es.shutdown();
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error ", e);
      }
   }

   class removeGarbage extends Thread
   {
//      HashMap<String, ThreadPoolExecutor> executorPool;
      private DVPMSEventHandler dvpmsEventHandler;
      public removeGarbage(DVPMSEventHandler dvpmsEventHandler)
      {
         this.dvpmsEventHandler = dvpmsEventHandler;
      }

      public void run()
      {
         Thread.currentThread().setName("REMOVE_UNUSED_THREAD_PMS_EVENT" );
         try
         {
            while (true)
            {
               try
               {
                  dvLogger.info("executorPool:  "+dvpmsEventHandler.executorPool.toString());
                  for (Map.Entry<String, ThreadPoolExecutor> entry : dvpmsEventHandler.executorPool
                           .entrySet())
                  {
                     String key = entry.getKey();
                     try
                     {
                        if (dvpmsEventHandler.executorPool.get(key).getActiveCount() == 0)
                        {
                           dvLogger.info("Shutting down Executor for Key: "+key);
                           dvpmsEventHandler.executorPool.get(key).shutdown();
                           dvpmsEventHandler.executorPool.remove(key);
                        }
                     }
                     catch (Exception e)
                     {
                        dvLogger.error("Error in shuttingdown Thread ", e);
                     }

                  }
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
                  // TODO: handle exception
               }
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Error ", e);
         }
      }

   }


}
