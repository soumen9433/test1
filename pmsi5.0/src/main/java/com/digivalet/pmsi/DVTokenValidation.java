package com.digivalet.pmsi;

import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.oneauth.client.OneAuthClient;
import com.digivalet.oneauth.client.Response;
import com.digivalet.pmsi.settings.DVSettings;


/**
 * @author JAVA Token validation class. Initialise the object to allow calling
 *         verification of third party and allow to generate new token, also to
 *         verify the authenticity client request
 *
 */

public class DVTokenValidation extends Thread
{

   private DVSettings dvSettings;
   private String tokenValidationUrl;
   private DVLogger dvLogger = DVLogger.getInstance();
   private OneAuthClient authClient;
   private boolean oneAuthEnabled;
   private String checkInAppOneAuthToken;
   private String checkInAppRefreshToken;
   private long authTokenExpiryTime = 60 * 60 * 1000;

   public DVTokenValidation()
   {
   }

   public boolean isOneAuthEnabled()
   {
      return oneAuthEnabled;
   }

   public JSONObject validateToken(String accessToken)
   {
      if (oneAuthEnabled)
      {
         if (null != accessToken && !("".equalsIgnoreCase(accessToken)))
         {
            dvLogger.info(" One Auth enabled, Validating the client");
            com.digivalet.oneauth.client.Response validateClient =
                     getAuthClient().validateViaProxy(accessToken, checkInAppOneAuthToken);

            int httpStatusCode = validateClient.getStatusCode();

            if (httpStatusCode == 401)
            {
               dvLogger.info(
                        "Unauthorised:401, Requesting new One-Auth Token for Digivalet-checkin-Service");
               init(dvSettings);

               dvLogger.info("Again validating the client's access token");
               validateClient =
                        getAuthClient().validateViaProxy(accessToken, checkInAppOneAuthToken);
            }

            dvLogger.info("Response Http Status Code = " + httpStatusCode);
            JSONObject tokenVerify = new JSONObject(validateClient.getBody());
            dvLogger.info("token verification body: " + tokenVerify);
            return tokenVerify;
         }
         else
         {
            String errorBody =
                     "{\"status\":false,\"message\":\"Required parameter 'auth_token' doesn't have valid data!\",\"data\":[],\"response_tag\":203}";
            JSONObject tokenVerify = new JSONObject(errorBody);
            dvLogger.info("token verification body: " + tokenVerify);
            return tokenVerify;
         }
      }
      else
      {
         dvLogger.info("One Auth NOT enabled, By passing the validation..");
         JSONObject object = new JSONObject();
         object.put("status", true);
         object.put("message", "Validation by passes, due to OneAuth disabled");
         dvLogger.info("Validation By passed JSON data : " + object.toString());
         return object;
      }
   }


   public void init(DVSettings dvSettings)
   {
      this.dvSettings = dvSettings;

      dvLogger.info("TOKEN VALIDATION URL from settings file : "
               + this.dvSettings.getTokenValidationUrl());

      tokenValidationUrl = this.dvSettings.getTokenValidationUrl();

      authClient = new OneAuthClient(tokenValidationUrl, this.dvSettings.getOneAuthClientId(),
               this.dvSettings.getOneAuthClientSecret());

      com.digivalet.oneauth.client.Response authenticateResponse = authClient.authenticate();


      dvLogger.info("Self Authenticate Response: " + authenticateResponse.getBody());

      checkInAppOneAuthToken = authenticateResponse.getToken();

      checkInAppRefreshToken = authenticateResponse.getRefreshToken();
//      authTokenExpiryTime = authenticateResponse.getExpires();
      
      System.out.println(" Body ::: "+ authenticateResponse.getBody());

      dvLogger.info("CheckInApp Auth TOKEN: " + checkInAppOneAuthToken);
      dvLogger.info("CheckInApp Refresh TOKEN: " + checkInAppRefreshToken);

      if (this.dvSettings.isOneAuthEnabled())
      {
         dvLogger.info("One Auth Enabled = " + this.dvSettings.isOneAuthEnabled());
         oneAuthEnabled = true;
      }
      else
      {
         dvLogger.info("One Auth Enabled = " + this.dvSettings.isOneAuthEnabled());
         oneAuthEnabled = false;
      }
   }

   public String getAuthToken()
   {
      return checkInAppOneAuthToken;
   }

   private OneAuthClient getAuthClient()
   {
      return authClient;
   }


   public void run()
   {
      while (true)
      {
         try
         {
            refreshToken();
            Thread.sleep(authTokenExpiryTime);
         }
         catch (Exception e)
         {
            dvLogger.error("Error while calling to get new refreshToken.", e);
         }
      }
   }

   public void refreshToken()
   {
      try
      {

         Response response =
                  authClient.refreshToken(checkInAppOneAuthToken, checkInAppRefreshToken);

         int httpStatusCode = response.getStatusCode();

         if (httpStatusCode == 200)
         {
            /*
             * com.digivalet.oneauth.client.Response refreshToken =
             * authClient.refreshToken(this.dvSettings.getOneAuthClientId(),
             * this.dvSettings.getOneAuthClientSecret());
             * 
             * posiOneAuthToken = refreshToken.getRefreshToken();
             */

            JSONObject refreshTokenBody = new JSONObject(response.getBody());
            checkInAppOneAuthToken =
                     refreshTokenBody.getJSONObject("data").getString("access_token");

            dvLogger.info(
                     "New One-Auth token, after refresh token called:  " + checkInAppOneAuthToken);
         }
         else
         {
            Response authenticateResponse = authClient.authenticate();
            checkInAppOneAuthToken = authenticateResponse.getToken();
            checkInAppRefreshToken = authenticateResponse.getRefreshToken();
            dvLogger.info(
                     "New One-Auth token, after refresh token expired:  " + checkInAppOneAuthToken);
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Exception in self token validation thread", e);
      }
   }

}
