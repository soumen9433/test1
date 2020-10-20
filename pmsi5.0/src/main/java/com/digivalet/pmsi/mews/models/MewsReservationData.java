package com.digivalet.pmsi.mews.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MewsReservationData
{
   @SerializedName("Id")
   @Expose
   private String id;
   @SerializedName("ServiceId")
   @Expose
   private String serviceId;
   @SerializedName("GroupId")
   @Expose
   private String groupId;
   @SerializedName("Number")
   @Expose
   private String number;
   @SerializedName("ChannelNumber")
   @Expose
   private String channelNumber;
   @SerializedName("ChannelManagerNumber")
   @Expose
   private String channelManagerNumber;
   @SerializedName("ChannelManagerGroupNumber")
   @Expose
   private String channelManagerGroupNumber;
   @SerializedName("ChannelManager")
   @Expose
   private String channelManager;
   @SerializedName("State")
   @Expose
   private String state;
   @SerializedName("Origin")
   @Expose
   private String origin;
   @SerializedName("CreatedUtc")
   @Expose
   private String createdUtc;
   @SerializedName("UpdatedUtc")
   @Expose
   private String updatedUtc;
   @SerializedName("CancelledUtc")
   @Expose
   private String cancelledUtc;
   @SerializedName("StartUtc")
   @Expose
   private String startUtc;
   @SerializedName("EndUtc")
   @Expose
   private String endUtc;
   @SerializedName("ReleasedUtc")
   @Expose
   private String releasedUtc;
   @SerializedName("RequestedCategoryId")
   @Expose
   private String requestedCategoryId;
   @SerializedName("AssignedSpaceId")
   @Expose
   private String assignedSpaceId;
   @SerializedName("AssignedSpaceLocked")
   @Expose
   private Boolean assignedSpaceLocked;
   @SerializedName("BusinessSegmentId")
   @Expose
   private String businessSegmentId;
   @SerializedName("CompanyId")
   @Expose
   private String companyId;
   @SerializedName("TravelAgencyId")
   @Expose
   private String travelAgencyId;
   @SerializedName("RateId")
   @Expose
   private String rateId;
   @SerializedName("AdultCount")
   @Expose
   private Integer adultCount;
   @SerializedName("ChildCount")
   @Expose
   private Integer childCount;
   @SerializedName("CustomerId")
   @Expose
   private String customerId;
   @SerializedName("CompanionIds")
   @Expose
   private List<String> companionIds = null;
   @SerializedName("ChannelManagerId")
   @Expose
   private String channelManagerId;

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getServiceId()
   {
      return serviceId;
   }

   public void setServiceId(String serviceId)
   {
      this.serviceId = serviceId;
   }

   public String getGroupId()
   {
      return groupId;
   }

   public void setGroupId(String groupId)
   {
      this.groupId = groupId;
   }

   public String getNumber()
   {
      return number;
   }

   public void setNumber(String number)
   {
      this.number = number;
   }

   public String getChannelNumber()
   {
      return channelNumber;
   }

   public void setChannelNumber(String channelNumber)
   {
      this.channelNumber = channelNumber;
   }

   public String getChannelManagerNumber()
   {
      return channelManagerNumber;
   }

   public void setChannelManagerNumber(String channelManagerNumber)
   {
      this.channelManagerNumber = channelManagerNumber;
   }

   public String getChannelManagerGroupNumber()
   {
      return channelManagerGroupNumber;
   }

   public void setChannelManagerGroupNumber(String channelManagerGroupNumber)
   {
      this.channelManagerGroupNumber = channelManagerGroupNumber;
   }

   public String getChannelManager()
   {
      return channelManager;
   }

   public void setChannelManager(String channelManager)
   {
      this.channelManager = channelManager;
   }

   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   public String getOrigin()
   {
      return origin;
   }

   public void setOrigin(String origin)
   {
      this.origin = origin;
   }

   public String getCreatedUtc()
   {
      return createdUtc;
   }

   public void setCreatedUtc(String createdUtc)
   {
      this.createdUtc = createdUtc;
   }

   public String getUpdatedUtc()
   {
      return updatedUtc;
   }

   public void setUpdatedUtc(String updatedUtc)
   {
      this.updatedUtc = updatedUtc;
   }

   public String getCancelledUtc()
   {
      return cancelledUtc;
   }

   public void setCancelledUtc(String cancelledUtc)
   {
      this.cancelledUtc = cancelledUtc;
   }

   public String getStartUtc()
   {
      return startUtc;
   }

   public void setStartUtc(String startUtc)
   {
      this.startUtc = startUtc;
   }

   public String getEndUtc()
   {
      return endUtc;
   }

   public void setEndUtc(String endUtc)
   {
      this.endUtc = endUtc;
   }

   public String getReleasedUtc()
   {
      return releasedUtc;
   }

   public void setReleasedUtc(String releasedUtc)
   {
      this.releasedUtc = releasedUtc;
   }

   public String getRequestedCategoryId()
   {
      return requestedCategoryId;
   }

   public void setRequestedCategoryId(String requestedCategoryId)
   {
      this.requestedCategoryId = requestedCategoryId;
   }

   public String getAssignedSpaceId()
   {
      return assignedSpaceId;
   }

   public void setAssignedSpaceId(String assignedSpaceId)
   {
      this.assignedSpaceId = assignedSpaceId;
   }

   public Boolean getAssignedSpaceLocked()
   {
      return assignedSpaceLocked;
   }

   public void setAssignedSpaceLocked(Boolean assignedSpaceLocked)
   {
      this.assignedSpaceLocked = assignedSpaceLocked;
   }

   public String getBusinessSegmentId()
   {
      return businessSegmentId;
   }

   public void setBusinessSegmentId(String businessSegmentId)
   {
      this.businessSegmentId = businessSegmentId;
   }

   public String getCompanyId()
   {
      return companyId;
   }

   public void setCompanyId(String companyId)
   {
      this.companyId = companyId;
   }

   public String getTravelAgencyId()
   {
      return travelAgencyId;
   }

   public void setTravelAgencyId(String travelAgencyId)
   {
      this.travelAgencyId = travelAgencyId;
   }

   public String getRateId()
   {
      return rateId;
   }

   public void setRateId(String rateId)
   {
      this.rateId = rateId;
   }

   public Integer getAdultCount()
   {
      return adultCount;
   }

   public void setAdultCount(Integer adultCount)
   {
      this.adultCount = adultCount;
   }

   public Integer getChildCount()
   {
      return childCount;
   }

   public void setChildCount(Integer childCount)
   {
      this.childCount = childCount;
   }

   public String getCustomerId()
   {
      return customerId;
   }

   public void setCustomerId(String customerId)
   {
      this.customerId = customerId;
   }

   public List<String> getCompanionIds()
   {
      return companionIds;
   }

   public void setCompanionIds(List<String> companionIds)
   {
      this.companionIds = companionIds;
   }

   public String getChannelManagerId()
   {
      return channelManagerId;
   }

   public void setChannelManagerId(String channelManagerId)
   {
      this.channelManagerId = channelManagerId;
   }


}
