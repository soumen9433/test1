package com.digivalet.pmsi.oauthclient;

import com.google.gson.annotations.SerializedName;

public class DVTokenValidateRequest
{

   @SerializedName("access_token")
   private String accessToken;

   @SerializedName("url")
   private String url;

   @SerializedName("method")
   private String method;

   @SerializedName("access_token")
   public String getAccessToken()
   {
      return accessToken;
   }

   @SerializedName("url")
   public String getUrl()
   {
      return url;
   }

   @SerializedName("method")
   public String getMethod()
   {
      return method;
   }

   public void setAccessToken(String accessToken)
   {
      this.accessToken = accessToken;
   }

   public void setUrl(String url)
   {
      this.url = url;
   }

   public void setMethod(String method)
   {
      this.method = method;
   }


}
