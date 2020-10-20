package com.digivalet.pmsi.mews.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MewsMasterReservationData
{
   @SerializedName("Reservations")
   @Expose
   private List<MewsReservationData> reservations = new ArrayList<>();

   @SerializedName("Customers")
   @Expose
   private List<MewsCustomerData> customers = new LinkedList<>();

   public List<MewsReservationData> getReservations()
   {
      return reservations;
   }

   public void setReservations(List<MewsReservationData> reservations)
   {
      this.reservations = reservations;
   }

   public List<MewsCustomerData> getCustomers()
   {
      return customers;
   }

   public void setCustomers(List<MewsCustomerData> customers)
   {
      this.customers = customers;
   }
}
