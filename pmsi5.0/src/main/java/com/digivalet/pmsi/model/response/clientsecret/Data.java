package com.digivalet.pmsi.model.response.clientsecret;

public class Data
{
   private String communication_token;

   public String getCommunication_token()
   {
      return communication_token;
   }

   public void setCommunication_token(String communication_token)
   {
      this.communication_token = communication_token;
   }

   @Override
   public String toString()
   {
      return "ClassPojo [communication_token = " + communication_token + "]";
   }
}
