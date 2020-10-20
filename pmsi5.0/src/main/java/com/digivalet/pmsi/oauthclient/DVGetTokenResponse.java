package com.digivalet.pmsi.oauthclient;

import com.google.gson.annotations.SerializedName;

public class DVGetTokenResponse
{

   @SerializedName("status")
   private String status;

   @SerializedName("token_type")
   private String tokenType;

   @SerializedName("access_token")
   private String accessToken;

   @SerializedName("refresh_token")
   private String refreshToken;

   @SerializedName("expires_in")
   private String expiresIn;

   @SerializedName("message")
   private String message;

   @SerializedName("status")
   public String getStatus()
   {
      return status;
   }

   @SerializedName("token_type")
   public String getTokenType()
   {
      return tokenType;
   }

   @SerializedName("access_token")
   public String getAccessToken()
   {
      return accessToken;
   }

   @SerializedName("refresh_token")
   public String getRefreshToken()
   {
      return refreshToken;
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

   public void setStatus(String status)
   {
      this.status = status;
   }

   public void setTokenType(String tokenType)
   {
      this.tokenType = tokenType;
   }

   public void setAccessToken(String accessToken)
   {
      this.accessToken = accessToken;
   }

   public void setRefreshToken(String refreshToken)
   {
      this.refreshToken = refreshToken;
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
