package com.digivalet.pmsi.events;

import java.util.Map;
import com.digivalet.pmsi.datatypes.DVPMSMessageData;
import com.digivalet.pmsi.datatypes.DVPmsBillData;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.events.DVBillEvent.BillFeatureEventType;
import com.digivalet.pmsi.events.DVEvent.FeatureEventType;
import com.digivalet.pmsi.events.DVMessageEvent.MessageFeatureEvent;

public interface DVPmsEventNotifier
{
   public void notifyPmsEvent(FeatureEventType featureEventType,Map<DVPmsData, Object> data);
   
   public void notifyPmsBillEvent(BillFeatureEventType featureEventType,Map<DVPmsBillData, Object> data);
   
   public void notifyMessageEvent(MessageFeatureEvent featureEventType ,Map<DVPMSMessageData, Object> messageData);
}
