package com.digivalet.pmsi.result;

public class Data
{
   private String operation;

   private Details[] details;

   private String feature;

   public String getOperation ()
   {
       return operation;
   }

   public void setOperation (String operation)
   {
       this.operation = operation;
   }

   public Details[] getDetails ()
   {
       return details;
   }

   public void setDetails (Details[] details)
   {
       this.details = details;
   }

   public String getFeature ()
   {
       return feature;
   }

   public void setFeature (String feature)
   {
       this.feature = feature;
   }

   @Override
   public String toString()
   {
       return "ClassPojo [operation = "+operation+", details = "+details+", feature = "+feature+"]";
   }
}
