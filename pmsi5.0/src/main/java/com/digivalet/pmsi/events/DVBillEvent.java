package com.digivalet.pmsi.events;

import java.util.Map;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.datatypes.DVPmsBillData;

public class DVBillEvent
{

   private DVLogger dvLogger = DVLogger.getInstance();
   private BillFeatureEventType featureEventType;
   private Map<DVPmsBillData, Object> billData;
   public enum BillFeatureEventType
   {
      PMSI_BILL_EVENT;
   }

   public DVBillEvent(BillFeatureEventType featureEventType, Map<DVPmsBillData, Object> billData)
   {
      this.featureEventType=featureEventType;
      this.billData=billData;
      dvLogger.info("Creating new DV Event with featureEventType "+featureEventType+" Data at dvevent "+billData);
   }
   
   
   public BillFeatureEventType getFeatureEventType()
   {
      return featureEventType;
   }
   public void setFeatureEventType(BillFeatureEventType featureEventType)
   {
      this.featureEventType = featureEventType;
   }

   public Map<DVPmsBillData, Object> getBillData()
   {
      return billData;
   }

   public void setBillData(Map<DVPmsBillData, Object> billData)
   {
      this.billData = billData;
   }

   
   

}
