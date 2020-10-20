package com.digivalet.pmsi.model.response.datatocontroller;

public class Details
{
   private String sessionKey;

   public String getSessionKey()
   {
      return sessionKey;
   }

   public void setSessionKey(String sessionKey)
   {
      this.sessionKey = sessionKey;
   }

   @Override
   public String toString()
   {
      return "ClassPojo [sessionKey = " + sessionKey + "]";
   }

}
