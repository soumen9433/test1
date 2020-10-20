package com.digivalet.pmsi.datatypes;

public class DVPmsBill
{
   private String guestid;
   private int keyId;
   private String date;
   private String time;
   private double amount;
   private String description;
   private String folio;

   public DVPmsBill(String guestid, int keyId, String date, String time,
            double amount, String description, String folio)
   {
      this.guestid = guestid;
      this.keyId = keyId;
      this.date = date;
      this.time = time;
      this.amount = amount;
      this.description = description;
      this.folio = folio;
   }

   public String getGuestid()
   {
      return guestid;
   }

   public void setGuestid(String guestid)
   {
      this.guestid = guestid;
   }

   public int getKeyId()
   {
      return keyId;
   }

   public void setKeyId(int keyId)
   {
      this.keyId = keyId;
   }

   public String getDate()
   {
      return date;
   }

   public void setDate(String date)
   {
      this.date = date;
   }

   public String getTime()
   {
      return time;
   }

   public void setTime(String time)
   {
      this.time = time;
   }

   public double getAmount()
   {
      return amount;
   }

   public void setAmount(double amount)
   {
      this.amount = amount;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getFolio()
   {
      return folio;
   }

   public void setFolio(String folio)
   {
      this.folio = folio;
   }

}
