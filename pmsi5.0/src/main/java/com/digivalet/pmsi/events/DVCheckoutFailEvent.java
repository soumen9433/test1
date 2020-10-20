package com.digivalet.pmsi.events;

import com.digivalet.core.DVLogger;

public class DVCheckoutFailEvent
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private String failMessage;
   private String roomNumber;
   private String guestId;

   public String getRoomNumber()
   {
      return roomNumber;
   }

   public void setRoomNumber(String roomNumber)
   {
      this.roomNumber = roomNumber;
   }

   public String getFailMessage()
   {
      return failMessage;
   }

   public void setFailMessage(String failMessage)
   {
      this.failMessage = failMessage;
   }

   public enum CheckoutFailEvent
   {
      PMSI_CHECKOUT_FAIL_EVENT;
   }


   public String getGuestId()
   {
      return guestId;
   }

   public void setGuestId(String guestId)
   {
      this.guestId = guestId;
   }

   public DVCheckoutFailEvent(String failMessage, String roomNumber,
            String guestId)
   {
      this.failMessage = failMessage;
      this.roomNumber = roomNumber;
      this.guestId = guestId;
      dvLogger.info("Creating new DV Event for checkout fail ");
   }


}
