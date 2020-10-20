package com.digivalet.pmsi.api.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.api.GuestPreferenceApiService;
import com.digivalet.pmsi.api.NotFoundException;
import com.digivalet.pmsi.model.GuestPreferenceRequest;
import com.digivalet.pmsi.model.GuestPreferenceResponse;
import com.digivalet.pmsi.oauthclient.DVTokenValidateResponse;
import com.digivalet.pmsi.result.DVResult;

@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2018-12-21T11:32:48.219Z")
public class GuestPreferenceApiServiceImpl extends GuestPreferenceApiService
{
   private static DVLogger dvLogger = DVLogger.getInstance();
   private final String GUEST_PREFERENCE_URL = "/guestPreference";
   private final String METHOD_TYPE = "POST";
   private final Long UNAUTHORISED_RESPONSE_TAG = 1L;

   @Override
   public Response guestPreference(String accessToken,
            GuestPreferenceRequest preference, SecurityContext securityContext)
            throws NotFoundException
   {
      Thread.currentThread().setName("GUEST_PREFERENCE");
      GuestPreferenceResponse guestPreferenceResponse =
               new GuestPreferenceResponse();
      dvLogger.info("Guest Preference request "
               + preference.toString());
      
      if (null != accessToken && !"".equalsIgnoreCase(accessToken))
      {
         try
         {
            DVTokenValidateResponse validationResponse =
                     DVPmsMain.getInstance().getOAuthInstance().validateToken(
                              accessToken, GUEST_PREFERENCE_URL, METHOD_TYPE);
            
            if (!validationResponse.isStatus())
            {
               dvLogger.info("Un-authorised client");

               guestPreferenceResponse.setResponseTag(UNAUTHORISED_RESPONSE_TAG);
               guestPreferenceResponse.setMessage(validationResponse.getMessage());
               guestPreferenceResponse.setStatus(false);

               return Response.status(HttpStatus.SC_UNAUTHORIZED)
                        .entity(guestPreferenceResponse).build();
            }
         }
         catch (Exception e)
         {
            guestPreferenceResponse.setResponseTag(UNAUTHORISED_RESPONSE_TAG);
            guestPreferenceResponse.setMessage("Internal Server Error!");
            guestPreferenceResponse.setStatus(false);

            dvLogger.error("Exception occurred", e);
            return Response.status(HttpStatus.SC_UNAUTHORIZED).entity(guestPreferenceResponse)
                     .build();
         }}
      else
      {
         guestPreferenceResponse.setMessage("Invalid Access Token");
         guestPreferenceResponse.setResponseTag(401L);
         guestPreferenceResponse.setStatus(false);
         return Response.status(HttpStatus.SC_BAD_REQUEST)
                  .entity(guestPreferenceResponse).build();
      }

      try
      {
         if (null != preference.getFeature())
         {
            if (GuestPreferenceRequest.FeatureEnum.GUESTPREFERENCE
                     .equals(preference.getFeature()))
            {
               if (null != preference.getOperation())
               {
                  if (GuestPreferenceRequest.OperationEnum.POSTPREFERENCE
                           .equals(preference.getOperation()))
                  {

                     /*
                      * "guestId": "string", "mood": "string", "temperature": 0,
                      * "hotelCode": "string", "roomNumber": "string",
                      * "language": "string", "specialInstructions": "string"
                      */

                     /*
                      * String guestId = preference.getDetails().get(0)
                      * .getPreferenceDetails().get(0).getGuestId(); String
                      * language = preference.getDetails().get(0)
                      * .getPreferenceDetails().get(0).getLanguage(); String
                      * specialInstructions = preference.getDetails().get(0)
                      * .getPreferenceDetails().get(0)
                      * .getSpecialInstructions();
                      */

                     String reservationNumber = preference.getDetails().get(0)
                              .getPreferenceDetails().get(0)
                              .getReservationNumber();

                     String mood = preference.getDetails().get(0)
                              .getPreferenceDetails().get(0).getMood();
                     String hotelCode = preference.getDetails().get(0)
                              .getPreferenceDetails().get(0).getHotelCode();
                     String roomNumber = preference.getDetails().get(0)
                              .getPreferenceDetails().get(0).getRoomNumber();

                     if (null == mood || "".equalsIgnoreCase(mood))
                     {
                        guestPreferenceResponse.setMessage(
                                 "Mood Parameter in API is Mandatory");
                        guestPreferenceResponse.setResponseTag(401L);
                        guestPreferenceResponse.setStatus(false);
                        return Response.status(HttpStatus.SC_BAD_REQUEST)
                                 .entity(guestPreferenceResponse).build();
                     }
                     if (null == reservationNumber
                              || "".equalsIgnoreCase(reservationNumber))
                     {
                        reservationNumber="";
                        /*guestPreferenceResponse.setMessage(
                                 "Reservation Number in API is Mandatory");
                        guestPreferenceResponse.setResponseTag(401L);
                        guestPreferenceResponse.setStatus(false);
                        return Response.status(HttpStatus.SC_BAD_REQUEST)
                                 .entity(guestPreferenceResponse).build();*/
                     }


                     if (null == roomNumber || "".equalsIgnoreCase(roomNumber))
                     {
                        guestPreferenceResponse
                                 .setMessage("Room Number in API is Mandatory");
                        guestPreferenceResponse.setResponseTag(401L);
                        guestPreferenceResponse.setStatus(false);
                        return Response.status(HttpStatus.SC_BAD_REQUEST)
                                 .entity(guestPreferenceResponse).build();
                     }

                     if (null == hotelCode || "".equalsIgnoreCase(hotelCode))
                     {
                        guestPreferenceResponse
                                 .setMessage("Hotel Code in API is Mandatory");
                        guestPreferenceResponse.setResponseTag(401L);
                        guestPreferenceResponse.setStatus(false);
                        return Response.status(HttpStatus.SC_BAD_REQUEST)
                                 .entity(guestPreferenceResponse).build();
                     }

                     JSONObject validationJson = new JSONObject();
                     validationJson.put("roomNumber", roomNumber);
                     validationJson.put("hotelShortName", hotelCode);
                     validationJson.put("moodId", mood);

                     DVResult dvResult =
                              DVPmsMain.getInstance().getDvPmsController()
                                       .validateRequest(validationJson);

                     if (dvResult.getCode() == DVResult.SUCCESS)
                     {
                        dvLogger.info("Process Successfull");

                        DVResult preferenceResult =
                                 DVPmsMain.getInstance().getDvPmsController()
                                          .guestArrivalPreference(preference);

                        if (preferenceResult.getCode() == DVResult.SUCCESS)
                        {
                           guestPreferenceResponse.setMessage("Success");
                           guestPreferenceResponse.setResponseTag(200L);
                           guestPreferenceResponse.setStatus(true);

                           return Response.status(HttpStatus.SC_OK)
                                    .entity(guestPreferenceResponse).build();
                        }
                        else
                        {
                           guestPreferenceResponse.setMessage("Failed");
                           guestPreferenceResponse.setResponseTag(205L);
                           guestPreferenceResponse.setStatus(false);

                           return Response.status(HttpStatus.SC_OK)
                                    .entity(guestPreferenceResponse).build();
                        }
                     }
                     else if (dvResult
                              .getCode() == DVResult.DVERROR_ROOMID_NOT_FOUND)
                     {
                        guestPreferenceResponse
                                 .setMessage("Invalid Room number");
                        guestPreferenceResponse.setResponseTag(401L);
                        guestPreferenceResponse.setStatus(false);
                        return Response.status(HttpStatus.SC_BAD_REQUEST)
                                 .entity(guestPreferenceResponse).build();
                     }
                     else if (dvResult
                              .getCode() == DVResult.DVERROR_HOTELCODE_NOT_FOUND)
                     {
                        guestPreferenceResponse
                                 .setMessage("Invalid Hotel Code");
                        guestPreferenceResponse.setResponseTag(401L);
                        guestPreferenceResponse.setStatus(false);
                        return Response.status(HttpStatus.SC_BAD_REQUEST)
                                 .entity(guestPreferenceResponse).build();

                     }
                     else if (dvResult
                              .getCode() == DVResult.DVERROR_INVALID_MOODID)
                     {
                        guestPreferenceResponse.setMessage("Invalid Mood Id");
                        guestPreferenceResponse.setResponseTag(401L);
                        guestPreferenceResponse.setStatus(false);
                        return Response.status(HttpStatus.SC_BAD_REQUEST)
                                 .entity(guestPreferenceResponse).build();

                     }
                     else if (dvResult
                              .getCode() == DVResult.DVERROR_INVALID_RESERVATION_NO)
                     {
                        guestPreferenceResponse
                                 .setMessage("Invalid Reservation Number");
                        guestPreferenceResponse.setResponseTag(401L);
                        guestPreferenceResponse.setStatus(false);
                        return Response.status(HttpStatus.SC_BAD_REQUEST)
                                 .entity(guestPreferenceResponse).build();
                     }
                  }
                  else
                  {
                     guestPreferenceResponse.setMessage("Invalid Operation");
                     guestPreferenceResponse.setResponseTag(401L);
                     guestPreferenceResponse.setStatus(false);
                     return Response.status(HttpStatus.SC_BAD_REQUEST)
                              .entity(guestPreferenceResponse).build();
                  }
               }
               else
               {
                  guestPreferenceResponse.setMessage("Invalid Operation");
                  guestPreferenceResponse.setResponseTag(401L);
                  guestPreferenceResponse.setStatus(false);
                  return Response.status(HttpStatus.SC_BAD_REQUEST)
                           .entity(guestPreferenceResponse).build();
               }
            }
            else
            {
               guestPreferenceResponse.setMessage("Invalid Feature");
               guestPreferenceResponse.setResponseTag(401L);
               guestPreferenceResponse.setStatus(false);
               return Response.status(HttpStatus.SC_BAD_REQUEST)
                        .entity(guestPreferenceResponse).build();
            }
         }
         else
         {
            guestPreferenceResponse.setMessage("Invalid Feature");
            guestPreferenceResponse.setResponseTag(501L);
            guestPreferenceResponse.setStatus(false);
            return Response.status(HttpStatus.SC_BAD_REQUEST)
                     .entity(guestPreferenceResponse).build();
         }
      }
      catch (Exception e)
      {
         dvLogger.error(
                  "Exceptiopn while processing guest preference request\n", e);

         guestPreferenceResponse.setMessage("Something went wrong!");
         guestPreferenceResponse.setResponseTag(500L);
         guestPreferenceResponse.setStatus(false);
         return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                  .entity(guestPreferenceResponse).build();
      }

      dvLogger.info("Something went wrong, nothing happened!");

      guestPreferenceResponse.setMessage("Something went wrong!");
      guestPreferenceResponse.setResponseTag(500L);
      guestPreferenceResponse.setStatus(false);
      return Response.status(HttpStatus.SC_NO_CONTENT)
               .entity(guestPreferenceResponse).build();
   }
}
