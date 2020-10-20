package com.digivalet.pmsi.model.response.datatocontroller;

public class Response
{
   private Details details;

   private String feature;

   public Details getDetails()
   {
      return details;
   }

   public void setDetails(Details details)
   {
      this.details = details;
   }

   public String getFeature()
   {
      return feature;
   }

   public void setFeature(String feature)
   {
      this.feature = feature;
   }

   @Override
   public String toString()
   {
      return "ClassPojo [details = " + details + ", feature = " + feature + "]";
   }
}
