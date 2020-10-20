package com.digivalet.pmsi;

import java.util.Map;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.datatypes.DVPMSMessageData;
import com.digivalet.pmsi.datatypes.DVPmsBillData;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.events.DVBillEvent;
import com.digivalet.pmsi.events.DVBillEvent.BillFeatureEventType;
import com.digivalet.pmsi.events.DVCheckoutFailEvent;
import com.digivalet.pmsi.events.DVEvent;
import com.digivalet.pmsi.events.DVEvent.FeatureEventType;
import com.digivalet.pmsi.events.DVFeatureBase;
import com.digivalet.pmsi.events.DVMessageEvent;
import com.digivalet.pmsi.events.DVMessageEvent.MessageFeatureEvent;


public abstract class DVPms extends DVFeatureBase implements DVPmsConnector
{
   private DVLogger dvLogger = DVLogger.getInstance();
   
   public void notifyPmsEvents(FeatureEventType featureEventType, Map<DVPmsData, Object> data)
   {
      DVEvent dvEvent = new DVEvent(featureEventType,data);
      dvLogger.info("Notifying PMS events to Update Notifiers for event type "+featureEventType.toString());
      dvLogger.info("Notifying PMS events to Update Notifiers for Data "+data.toString());
      dvLogger.info("Notifying PMS events to Update Notifiers for dvEvent "+dvEvent.toString());
      notifyAllListeners(dvEvent);
   }
   
   public void notifyPmsBillEvents(BillFeatureEventType featureEventType,Map<DVPmsBillData, Object> data)
   {
      DVBillEvent dvEvent = new DVBillEvent(featureEventType,data);
      dvLogger.info("Notifying Bill events to Update Notifiers for event type "+featureEventType.toString());
      dvLogger.info("Notifying Bill events to Update Notifiers for Data "+data.toString());
      dvLogger.info("Notifying Bill events to Update Notifiers for dvEvent "+dvEvent.toString());
      notifyAllListeners(dvEvent);
   }
   
   public void notifyMessageEvents(MessageFeatureEvent featureEventType ,Map<DVPMSMessageData, Object> messageData)
   {
	   DVMessageEvent dvEvent = new DVMessageEvent(featureEventType,messageData);
	    dvLogger.info("Notifying PMS events to message Notifiers for event type "+featureEventType.toString());
	      dvLogger.info("Notifying PMS events to message Notifiers for Data "+messageData.toString());
	      dvLogger.info("Notifying PMS events to message Notifiers for dvEvent "+dvEvent.toString());
	      notifyAllListeners(dvEvent);
   }
   
   public void notifyCheckoutFailEvent(String message,String roomNumber,String guestId)
   {
      DVCheckoutFailEvent dvEvent = new DVCheckoutFailEvent(message,roomNumber,guestId);

          dvLogger.info("Notifying checkout fail events to message Notifiers for dvEvent "+dvEvent.toString());
          notifyAllListeners(dvEvent);
   }
   
   
}
