/*
 * DigiValet Notification
 * This is DigiValet Notification services used to post guest preference to Digivalet
 *
 * OpenAPI spec version: 1.0.0
 * Contact: apisupport@digivalet.in
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.digivalet.pmsi.model;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * GuestPreferenceResponse
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-12-21T11:32:48.219Z")
public class GuestPreferenceResponse   {
  @JsonProperty("status")
  private boolean status = false;

  @JsonProperty("message")
  private String message = null;

  @JsonProperty("response_tag")
  private Long responseTag = null;

  public GuestPreferenceResponse status(boolean status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   **/
  @JsonProperty("status")
  @ApiModelProperty(value = "")
  public boolean getStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public GuestPreferenceResponse message(String message) {
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

  public GuestPreferenceResponse responseTag(Long responseTag) {
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
    GuestPreferenceResponse guestPreferenceResponse = (GuestPreferenceResponse) o;
    return Objects.equals(this.status, guestPreferenceResponse.status) &&
        Objects.equals(this.message, guestPreferenceResponse.message) &&
        Objects.equals(this.responseTag, guestPreferenceResponse.responseTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, message, responseTag);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GuestPreferenceResponse {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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
