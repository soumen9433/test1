package com.digivalet.pmsi.model.response.clientsecret;

public class DVCommunicationToken
{
   private String message;

   private String status;

   private String response_tag;

   private Data data;

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public String getStatus()
   {
      return status;
   }

   public void setStatus(String status)
   {
      this.status = status;
   }

   public String getResponse_tag()
   {
      return response_tag;
   }

   public void setResponse_tag(String response_tag)
   {
      this.response_tag = response_tag;
   }

   public Data getData()
   {
      return data;
   }

   public void setData(Data data)
   {
      this.data = data;
   }

   @Override
   public String toString()
   {
      return "ClassPojo [message = " + message + ", status = " + status
               + ", response_tag = " + response_tag + ", data = " + data + "]";
   }
}
