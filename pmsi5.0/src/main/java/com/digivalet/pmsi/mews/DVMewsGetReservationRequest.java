package com.digivalet.pmsi.mews;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DVMewsGetReservationRequest
{
   @SerializedName("ClientToken")
   @Expose
   private String clientToken;
   @SerializedName("AccessToken")
   @Expose
   private String accessToken;
   @SerializedName("ReservationIds")
   @Expose
   private List<String> reservationIds = null;

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

   public List<String> getReservationIds()
   {
      return reservationIds;
   }

   public void setReservationIds(List<String> reservationIds)
   {
      this.reservationIds = reservationIds;
   }
}
