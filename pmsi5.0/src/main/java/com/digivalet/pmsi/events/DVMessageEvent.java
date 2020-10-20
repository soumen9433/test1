package com.digivalet.pmsi.events;

import java.util.Map;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.datatypes.DVPMSMessageData;


public class DVMessageEvent {


	   private DVLogger dvLogger = DVLogger.getInstance();
	   private MessageFeatureEvent featureEventType;
	   private Map<DVPMSMessageData, Object> messageData;
	   public enum MessageFeatureEvent
	   {
	      PMSI_MESSAGE_EVENT;
	   }

	   public DVMessageEvent(MessageFeatureEvent featureEventType, Map<DVPMSMessageData, Object> messageData)
	   {
	      this.featureEventType=featureEventType;
	      this.messageData=messageData;
	      dvLogger.info("Creating new DV Event with featureEventType "+featureEventType+" Data at dvevent "+messageData);
	   }
	   
	   
	   public MessageFeatureEvent getFeatureEventType()
	   {
	      return featureEventType;
	   }
	   public void setFeatureEventType(MessageFeatureEvent featureEventType)
	   {
	      this.featureEventType = featureEventType;
	   }

	   public Map<DVPMSMessageData, Object> getMessageData()
	   {
	      return messageData;
	   }

	   public void setMessageData(Map<DVPMSMessageData, Object> messageData)
	   {
	      this.messageData = messageData;
	   }

	   
	   



}
