package com.digivalet.pmsi.result;

public class Status
{
   private String message;

   private String responseTag;

   private String status;

   private Data data;

   public String getMessage ()
   {
       return message;
   }

   public void setMessage (String message)
   {
       this.message = message;
   }

   public String getResponseTag ()
   {
       return responseTag;
   }

   public void setResponseTag (String responseTag)
   {
       this.responseTag = responseTag;
   }

   public String getStatus ()
   {
       return status;
   }

   public void setStatus (String status)
   {
       this.status = status;
   }

   public Data getData ()
   {
       return data;
   }

   public void setData (Data data)
   {
       this.data = data;
   }

   @Override
   public String toString()
   {
       return "ClassPojo [message = "+message+", responseTag = "+responseTag+", status = "+status+", data = "+data+"]";
   }
}
