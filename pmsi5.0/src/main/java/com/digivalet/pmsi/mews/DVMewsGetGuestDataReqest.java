package com.digivalet.pmsi.mews;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DVMewsGetGuestDataReqest
{
   @SerializedName("ClientToken")
   @Expose
   private String clientToken;
   @SerializedName("AccessToken")
   @Expose
   private String accessToken;
   @SerializedName("CustomerIds")
   @Expose
   private List<String> customerIds = null;

   public String getClientToken()
   {
      return clientToken;
   }

   public void setClientToken(String clientToken)
   {
      this.clientToken = clientToken;
   }

   public String getAccessToken()
   {
      return accessToken;
   }

   public void setAccessToken(String accessToken)
   {
      this.accessToken = accessToken;
   }

   public List<String> getCustomerIds()
   {
      return customerIds;
   }

   public void setCustomerIds(List<String> customerIds)
   {
      this.customerIds = customerIds;
   }
}
