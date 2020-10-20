package com.digivalet.pmsi.mews;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DVMewsRemoteCheckoutRequest
{
   @SerializedName("ClientToken")
   @Expose
   private String clientToken;
   @SerializedName("AccessToken")
   @Expose
   private String accessToken;
   @SerializedName("ReservationId")
   @Expose
   private String reservationId;
   @SerializedName("CloseBills")
   @Expose
   private Boolean closeBills;
   @SerializedName("AllowOpenBalance")
   @Expose
   private Boolean allowOpenBalance;
   @SerializedName("Notes")
   @Expose
   private Object notes;

   public String getClientToken()
   {
      return clientToken;
   }

   public void setClientToken(String clientToken)
   {
      this.clientToken = clientToken;
   }

   public String getAccessToken()
   {
      return accessToken;
   }

   public void setAccessToken(String accessToken)
   {
      this.accessToken = accessToken;
   }

   public String getReservationId()
   {
      return reservationId;
   }

   public void setReservationId(String reservationId)
   {
      this.reservationId = reservationId;
   }

   public Boolean getCloseBills()
   {
      return closeBills;
   }

   public void setCloseBills(Boolean closeBills)
   {
      this.closeBills = closeBills;
   }

   public Boolean getAllowOpenBalance()
   {
      return allowOpenBalance;
   }

   public void setAllowOpenBalance(Boolean allowOpenBalance)
   {
      this.allowOpenBalance = allowOpenBalance;
   }

   public Object getNotes()
   {
      return notes;
   }

   public void setNotes(Object notes)
   {
      this.notes = notes;
   }
}
