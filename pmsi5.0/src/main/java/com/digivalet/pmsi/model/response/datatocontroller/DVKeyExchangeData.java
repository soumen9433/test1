package com.digivalet.pmsi.model.response.datatocontroller;

public class DVKeyExchangeData
{
   private Response[] response;

   private String timestamp;

   private String targetDeviceId;

   public Response[] getResponse()
   {
      return response;
   }

   public void setResponse(Response[] response)
   {
      this.response = response;
   }

   public String getTimestamp()
   {
      return timestamp;
   }

   public void setTimestamp(String timestamp)
   {
      this.timestamp = timestamp;
   }

   public String getTargetDeviceId()
   {
      return targetDeviceId;
   }

   public void setTargetDeviceId(String targetDeviceId)
   {
      this.targetDeviceId = targetDeviceId;
   }

   @Override
   public String toString()
   {
      return "ClassPojo [response = " + response + ", timestamp = " + timestamp
               + ", targetDeviceId = " + targetDeviceId + "]";
   }

}
