package com.digivalet.pmsi.mews;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DVMewsGetSpacesRequest
{
   @SerializedName("ClientToken")
   @Expose
   private String clientToken;
   @SerializedName("AccessToken")
   @Expose
   private String accessToken;
   @SerializedName("Extent")
   @Expose
   private Extent extent;

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

   public Extent getExtent()
   {
      return extent;
   }

   public void setExtent(Extent extent)
   {
      this.extent = extent;
   }
}


class Extent
{
   @SerializedName("Spaces")
   @Expose
   private Boolean spaces;
   @SerializedName("SpaceCategories")
   @Expose
   private Boolean spaceCategories;
   @SerializedName("SpaceFeatures")
   @Expose
   private Boolean spaceFeatures;
   @SerializedName("Inactive")
   @Expose
   private Boolean inactive;

   public Boolean getSpaces()
   {
      return spaces;
   }

   public void setSpaces(Boolean spaces)
   {
      this.spaces = spaces;
   }

   public Boolean getSpaceCategories()
   {
      return spaceCategories;
   }

   public void setSpaceCategories(Boolean spaceCategories)
   {
      this.spaceCategories = spaceCategories;
   }

   public Boolean getSpaceFeatures()
   {
      return spaceFeatures;
   }

   public void setSpaceFeatures(Boolean spaceFeatures)
   {
      this.spaceFeatures = spaceFeatures;
   }

   public Boolean getInactive()
   {
      return inactive;
   }

   public void setInactive(Boolean inactive)
   {
      this.inactive = inactive;
   }

}
