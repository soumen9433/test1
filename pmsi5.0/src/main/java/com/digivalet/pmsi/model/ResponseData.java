/*
 * DigiValet PMS
 * This is DigiValet PMS application services for fetching room status, bill details, guest information, setting up wake up call etc.
 *
 * OpenAPI spec version: 1.0.0
 * Contact: apisupport@digivalet.in
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.digivalet.pmsi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * ResponseData
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public class ResponseData   {
  @JsonProperty("status")
  private Boolean status = false;

  @JsonProperty("message")
  private String message = null;

  @JsonProperty("data")
  private List<SuccessResponse> data = null;

  @JsonProperty("response_tag")
  private Long responseTag = null;

  public ResponseData status(Boolean status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   **/
  @JsonProperty("status")
  @ApiModelProperty(value = "")
  public Boolean getStatus() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  public ResponseData message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
   **/
  @JsonProperty("message")
  @ApiModelProperty(value = "")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public ResponseData data(List<SuccessResponse> data) {
    this.data = data;
    return this;
  }

  public ResponseData addDataItem(SuccessResponse dataItem) {
    if (this.data == null) {
      this.data = new ArrayList<SuccessResponse>();
    }
    this.data.add(dataItem);
    return this;
  }

  /**
   * Get data
   * @return data
   **/
  @JsonProperty("data")
  @ApiModelProperty(value = "")
  public List<SuccessResponse> getData() {
    return data;
  }

  public void setData(List<SuccessResponse> data) {
    this.data = data;
  }

  public ResponseData responseTag(Long responseTag) {
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
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResponseData responseData = (ResponseData) o;
    return Objects.equals(this.status, responseData.status) &&
        Objects.equals(this.message, responseData.message) &&
        Objects.equals(this.data, responseData.data) &&
        Objects.equals(this.responseTag, responseData.responseTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, message, data, responseTag);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResponseData {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    responseTag: ").append(toIndentedString(responseTag)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

