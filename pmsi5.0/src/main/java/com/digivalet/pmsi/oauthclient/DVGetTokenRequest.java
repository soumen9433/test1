package com.digivalet.pmsi.oauthclient;

import com.google.gson.annotations.SerializedName;

public class DVGetTokenRequest
{
   @SerializedName("client_id")
   private String clientId;

   @SerializedName("client_secret")
   private String clientSecret;

   @SerializedName("grant_type")
   private String grantType;

   @SerializedName("scope")
   private String scope;

   @SerializedName("client_id")
   public String getClientId()
   {
      return clientId;
   }

   @SerializedName("client_secret")
   public String getClientSecret()
   {
      return clientSecret;
   }

   @SerializedName("grant_type")
   public String getGrantType()
   {
      return grantType;
   }

   @SerializedName("scope")
   public String getScope()
   {
      return scope;
   }

   @SerializedName("client_id")
   public void setClientId(String clientId)
   {
      this.clientId = clientId;
   }

   @SerializedName("client_secret")
   public void setClientSecret(String clientSecret)
   {
      this.clientSecret = clientSecret;
   }

   @SerializedName("grant_type")
   public void setGrantType(String grantType)
   {
      this.grantType = grantType;
   }

   @SerializedName("scope")
   public void setScope(String scope)
   {
      this.scope = scope;
   }
}
