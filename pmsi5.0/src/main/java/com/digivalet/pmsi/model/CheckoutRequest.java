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
 * CheckoutRequest
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-08-29T09:41:32.663Z")
public class CheckoutRequest   {
  @JsonProperty("details")
  private List<CheckoutRequestDetails> details = null;

  @JsonProperty("operation")
  private String operation = null;

  @JsonProperty("feature")
  private String feature = null;

  public CheckoutRequest details(List<CheckoutRequestDetails> details) {
    this.details = details;
    return this;
  }

  public CheckoutRequest addDetailsItem(CheckoutRequestDetails detailsItem) {
    if (this.details == null) {
      this.details = new ArrayList<CheckoutRequestDetails>();
    }
    this.details.add(detailsItem);
    return this;
  }

  /**
   * Get details
   * @return details
   **/
  @JsonProperty("details")
  @ApiModelProperty(value = "")
  public List<CheckoutRequestDetails> getDetails() {
    return details;
  }

  public void setDetails(List<CheckoutRequestDetails> details) {
    this.details = details;
  }

  public CheckoutRequest operation(String operation) {
    this.operation = operation;
    return this;
  }

  /**
   * Get operation
   * @return operation
   **/
  @JsonProperty("operation")
  @ApiModelProperty(value = "")
  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public CheckoutRequest feature(String feature) {
    this.feature = feature;
    return this;
  }

  /**
   * Get feature
   * @return feature
   **/
  @JsonProperty("feature")
  @ApiModelProperty(value = "")
  public String getFeature() {
    return feature;
  }

  public void setFeature(String feature) {
    this.feature = feature;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CheckoutRequest checkoutRequest = (CheckoutRequest) o;
    return Objects.equals(this.details, checkoutRequest.details) &&
        Objects.equals(this.operation, checkoutRequest.operation) &&
        Objects.equals(this.feature, checkoutRequest.feature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(details, operation, feature);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CheckoutRequest {\n");
    
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
    sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
    sb.append("    feature: ").append(toIndentedString(feature)).append("\n");
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

