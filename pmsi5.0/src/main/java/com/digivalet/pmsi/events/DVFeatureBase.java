package com.digivalet.pmsi.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVUpdateNotifier;


public class DVFeatureBase
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private List<DVUpdateNotifier> dvUpdateNotifiers = new ArrayList<>();
   
   public void attachEventListener(DVUpdateNotifier dvUpdateNotifier)
   {
      try
      {
         if( dvUpdateNotifier != null )
         {
            dvLogger.info(" Listener :"+ Arrays.toString(dvUpdateNotifiers.toArray()) + " , size "+dvUpdateNotifiers.size());
            dvLogger.info("Attaching listener for events. Listener :"+ dvUpdateNotifier);
            dvUpdateNotifiers.add(dvUpdateNotifier);
            dvLogger.info("Listener is attached successfully for events. Listener :"+ Arrays.toString(dvUpdateNotifiers.toArray()) + " , size "+dvUpdateNotifiers.size());            
         }
         else
         {
            dvLogger.info("Feature base can not attach because Update Notifier is null");
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error while attaching listener. Error : ",e);
      }
   }
   
   public void detachEventListener(DVUpdateNotifier dvUpdateNotifier)
   {
      if( dvUpdateNotifier != null && dvUpdateNotifiers.contains(dvUpdateNotifier))
      {
         dvLogger.info("Detaching Update Notifier from Feature Base");
         dvUpdateNotifiers.remove(dvUpdateNotifier);
      }
      else
      {
         dvLogger.info("Feature base can not detach because Update Notifer is Null");
      }
   }
   
   public void notifyAllListeners(DVEvent dvEvent) 
   {
      if( dvEvent != null )
      {        
         try
         {
            dvLogger.info("Feature Base Notfiying event to Listeners. Listeneres : "+dvUpdateNotifiers.size()+" for event data "+dvEvent.getData().toString());
            for (DVUpdateNotifier dvUpdateNotifier : dvUpdateNotifiers)
            {
               dvUpdateNotifier.onEvent(dvEvent);
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Error while notifying event from feature base. Error:", e);
         }
      }
      else
      {
         dvLogger.info("Feature Base can not notify because Event is null");
      }
   }
   public void notifyAllListeners(DVBillEvent dvEvent) 
   {

      if( dvEvent != null )
      {        
         try
         {
            dvLogger.info("Feature Base Notfiying bill event to Listeners. Listeners : "+dvUpdateNotifiers.size()+" for event data "+dvEvent.getBillData().toString());
            for (DVUpdateNotifier dvUpdateNotifier : dvUpdateNotifiers)
            {
               dvUpdateNotifier.onEvent(dvEvent);
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Error while notifying event from feature base. Error:", e);
         }
      }
      else
      {
         dvLogger.info("Feature Base can not notify because Event is null");
      }
   
   }
   
   
   public void notifyAllListeners(DVMessageEvent dvEvent) 
   {

      if( dvEvent != null )
      {        
         try
         {
            //dvLogger.info("Feature Base Notfiying bill event to Listeners. Listeners : "+dvUpdateNotifiers.size()+" for event data "+dvEvent.getBillData().toString());
            for (DVUpdateNotifier dvUpdateNotifier : dvUpdateNotifiers)
            {
               dvUpdateNotifier.onEvent(dvEvent);
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Error while notifying event from feature base. Error:", e);
         }
      }
      else
      {
         dvLogger.info("Feature Base can not notify because Event is null");
      }
   
   }
   

   public void notifyAllListeners(DVCheckoutFailEvent dvEvent) 
   {

      if( null!=dvEvent  )
      {        
         try
         {
            //dvLogger.info("Feature Base Notfiying bill event to Listeners. Listeners : "+dvUpdateNotifiers.size()+" for event data "+dvEvent.getBillData().toString());
            for (DVUpdateNotifier dvUpdateNotifier : dvUpdateNotifiers)
            {
               dvUpdateNotifier.onEvent(dvEvent);
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Error while notifying event from feature base. Error:", e);
         }
      }
      else
      {
         dvLogger.info("Feature Base can not notify because Event is null");
      }
   
   }
}
