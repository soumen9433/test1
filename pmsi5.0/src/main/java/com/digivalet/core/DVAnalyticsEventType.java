package com.digivalet.core;


public enum DVAnalyticsEventType
{
   checkin,
   checkout,
   guestInformationUpdate,
   roomChangeCheckin,
   roomChangeCheckout,
   novalue,
   serviceStatus;
   
   public static DVAnalyticsEventType fromString(String Str)
   {
      try
      {
         return valueOf(Str);
      }
      catch (Exception ex)
      {
         return novalue;
      }
   }
}
