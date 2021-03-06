/*
 * DigiValet-IDA This is DigiValet Server.
 *
 * OpenAPI spec version: 1.0.0 Contact: apisupport@digivalet.in
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git Do not edit the class
 * manually.
 */


package com.digivalet.pmsi.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * DetailsResponse
 */
@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2020-01-21T13:21:56.464Z")
public class DetailsResponse
{
   @JsonProperty("status")
   private Boolean status = null;

   @JsonProperty("message")
   private String message = null;

   @JsonProperty("opration")
   private String opration = null;

   @JsonProperty("data")
   private Map<String, List<Map<String, String>>> data = null;

   @JsonProperty("guestData")
   private Map<String, List<Map<String, String>>> guestData = null;

   @JsonProperty("response_tag")
   private Long responseTag = null;



   public DetailsResponse status(Boolean status)
   {
      this.status = status;
      return this;
   }

   /**
    * Get status
    * 
    * @return status
    **/
   @JsonProperty("status")
   @ApiModelProperty(value = "")
   public Boolean getStatus()
   {
      return status;
   }

   public void setStatus(Boolean status)
   {
      this.status = status;
   }

   public DetailsResponse message(String message)
   {
      this.message = message;
      return this;
   }

   /**
    * Get message
    * 
    * @return message
    **/
   @JsonProperty("message")
   @ApiModelProperty(value = "")
   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public DetailsResponse opration(String opration)
   {
      this.opration = opration;
      return this;
   }

   @JsonProperty("data")
   @ApiModelProperty(value = "")
   public Map<String, List<Map<String, String>>> getData()
   {
      return data;
   }

   public void setData(Map<String, List<Map<String, String>>> result)
   {
      this.data = result;
   }

   @JsonProperty("guestData")
   @ApiModelProperty(value = "")
   public Map<String, List<Map<String, String>>> getGuestData()
   {
      return guestData;
   }

   public void setGuestData(Map<String, List<Map<String, String>>> guestData)
   {
      this.guestData = guestData;
   }

   /**
    * Get opration
    * 
    * @return opration
    **/
   @JsonProperty("opration")
   @ApiModelProperty(value = "")
   public String getOpration()
   {
      return opration;
   }

   public void setOpration(String opration)
   {
      this.opration = opration;
   }

   public DetailsResponse responseTag(Long responseTag) {
     this.responseTag = responseTag;
     return this;
   }

   /**
    * Get responseTag
    * @return responseTag
    **/
   @JsonProperty("response_tag")
   @ApiModelProperty(value = "")
   public Long getResponseTag() {
     return responseTag;
   }

   public void setResponseTag(Long responseTag) {
     this.responseTag = responseTag;
   }



   @Override
   public boolean equals(java.lang.Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }
      DetailsResponse detailsResponse = (DetailsResponse) o;
      return Objects.equals(this.status, detailsResponse.status)
               && Objects.equals(this.message, detailsResponse.message)
               && Objects.equals(this.opration, detailsResponse.opration)
               && Objects.equals(this.data, detailsResponse.data)
               && Objects.equals(this.guestData, detailsResponse.guestData);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(status, message, opration, data, guestData);
   }


   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("class DetailsResponse {\n");

      sb.append("    status: ").append(toIndentedString(status)).append("\n");
      sb.append("    message: ").append(toIndentedString(message)).append("\n");
      sb.append("    opration: ").append(toIndentedString(opration))
               .append("\n");
      sb.append("    data: ").append(toIndentedString(data)).append("\n");
      sb.append("    guestData: ").append(toIndentedString(guestData)).append("\n");
      sb.append("}");
      return sb.toString();
   }

   /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
   private String toIndentedString(java.lang.Object o)
   {
      if (o == null)
      {
         return "null";
      }
      return o.toString().replace("\n", "\n    ");
   }
}

