package com.digivalet.pmsi.events;

import java.util.Map;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.datatypes.DVPmsData;

public class DVEvent
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private FeatureEventType featureEventType;
   private Map<DVPmsData, Object> data;

   public enum FeatureEventType
   {
      PMSI_CHECKIN_EVENT,
      PMSI_CHECKOUT_EVENT,
      PMSI_ROOMCHANGE_CHECKOUT_EVENT,
      PMSI_ROOMCHANGE_CHECKIN_EVENT,
      PMSI_GUEST_INFORMATION_UPDATE_EVENT,
      PMSI_ROOM_CHANGE_EVENT,
      PMSI_SAFE_CHECKIN_EVENT,
      PMSI_SAFE_CHECKOUT_EVENT,
   }

   public DVEvent(FeatureEventType featureEventType,
            Map<DVPmsData, Object> data)
   {
      this.featureEventType = featureEventType;
      this.data = data;
      dvLogger.info("Creating new DV Event with featureEventType "
               + featureEventType + " Data at dvevent " + data);
   }

   public FeatureEventType getFeatureEventType()
   {
      return featureEventType;
   }

   public void setFeatureEventType(FeatureEventType featureEventType)
   {
      this.featureEventType = featureEventType;
   }

   public Map<DVPmsData, Object> getData()
   {
      return data;
   }

   public void setData(Map<DVPmsData, Object> data)
   {
      this.data = data;
   }

   @Override
   public String toString()
   {
      return "DVEvent [featureEventType=" + featureEventType + ", data=" + data
               + "]";
   }
}
