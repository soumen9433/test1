package com.digivalet.pms.guestpreference.model;

public class DVGuestPreferenceModel
{
   private String temperature="";
   private String moodId="";
   private String reservationNumber="";
   private String roomNumber="";
   private String guestId="";
   private String fragrance="";

   public String getFragrance()
   {
      return fragrance;
   }

   public void setFragrance(String fragrance)
   {
      this.fragrance = fragrance;
   }

   public String getTemperature()
   {
      return temperature;
   }

   public void setTemperature(String temperature)
   {
      this.temperature = temperature;
   }

   public String getMoodId()
   {
      return moodId;
   }

   public void setMoodId(String moodId)
   {
      this.moodId = moodId;
   }

   public String getReservationNumber()
   {
      return reservationNumber;
   }

   public void setReservationNumber(String reservationNumber)
   {
      this.reservationNumber = reservationNumber;
   }

   public String getRoomNumber()
   {
      return roomNumber;
   }

   public void setRoomNumber(String roomNumber)
   {
      this.roomNumber = roomNumber;
   }

   public String getGuestId()
   {
      return guestId;
   }

   public void setGuestId(String guestId)
   {
      this.guestId = guestId;
   }

   @Override
   public String toString()
   {
      return "DVGuestPreferenceModel [temperature=" + temperature + ", moodId="
               + moodId + ", reservationNumber=" + reservationNumber
               + ", roomNumber=" + roomNumber + ", guestId=" + guestId
               + ", fragrance=" + fragrance + "]";
   }
   
   
}
