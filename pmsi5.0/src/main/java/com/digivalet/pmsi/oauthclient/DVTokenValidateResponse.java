package com.digivalet.pmsi.oauthclient;

import com.google.gson.annotations.SerializedName;

public class DVTokenValidateResponse
{

   @SerializedName("status")
   private boolean status;

   @SerializedName("scope")
   private String scope;

   @SerializedName("expires_in")
   private String expiresIn;

   @SerializedName("message")
   private String message;

   @SerializedName("status")
   public boolean isStatus()
   {
      return status;
   }

   @SerializedName("scope")
   public String getScope()
   {
      return scope;
   }

   @SerializedName("expires_in")
   public String getExpiresIn()
   {
      return expiresIn;
   }

   @SerializedName("message")
   public String getMessage()
   {
      return message;
   }

   public void setStatus(boolean status)
   {
      this.status = status;
   }

   public void setScope(String scope)
   {
      this.scope = scope;
   }

   public void setExpiresIn(String expiresIn)
   {
      this.expiresIn = expiresIn;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }


}
