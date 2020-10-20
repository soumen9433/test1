package com.digivalet.pmsi.mews;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DVGetAllReservationRequest
{
   @SerializedName("ClientToken")
   @Expose
   private String clientToken;
   @SerializedName("AccessToken")
   @Expose
   private String accessToken;
   @SerializedName("StartUtc")
   @Expose
   private String startUtc;
   @SerializedName("EndUtc")
   @Expose
   private String endUtc;
   @SerializedName("Extent")
   @Expose
   private DVReservationExtent extent;

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

   public String getEndUtc()
   {
      return endUtc;
   }

   public void setEndUtc(String endUtc)
   {
      this.endUtc = endUtc;
   }

   public DVReservationExtent getExtent()
   {
      return extent;
   }

   public void setExtent(DVReservationExtent extent)
   {
      this.extent = extent;
   }

   public String getStartUtc()
   {
      return startUtc;
   }

   public void setStartUtc(String startUtc)
   {
      this.startUtc = startUtc;
   }
}


class DVReservationExtent
{
   @SerializedName("Reservations")
   @Expose
   private boolean reservations;
   @SerializedName("ReservationsGroups")
   @Expose
   private boolean reservationGroups;
   @SerializedName("Customers")
   @Expose
   private boolean customers;

   public boolean isReservations()
   {
      return reservations;
   }

   public void setReservations(boolean reservations)
   {
      this.reservations = reservations;
   }

   public boolean isReservationGroups()
   {
      return reservationGroups;
   }

   public void setReservationGroups(boolean reservationGroups)
   {
      this.reservationGroups = reservationGroups;
   }

   public boolean isCustomers()
   {
      return customers;
   }

   public void setCustomers(boolean customers)
   {
      this.customers = customers;
   }

  @Override
  public String toString() {
    return "DVReservationExtent [reservations=" + reservations + ", reservationGroups="
        + reservationGroups + ", customers=" + customers + ", isReservations()=" + isReservations()
        + ", isReservationGroups()=" + isReservationGroups() + ", isCustomers()=" + isCustomers()
        + "]";
  }
   
}
