package com.digivalet.pmsi.oauthclient;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.settings.DVSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DVAuthenticator
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private static final long ONE_MIN_MILLI = 60000;// millisecs
   private static final String CONTENT_TYPE_VALUE = "application/json";
   private static final String GET_TOKEN_URL_PATH = "token";
   private static final MediaType JSON = MediaType.parse(CONTENT_TYPE_VALUE);
   private static final String CONTENT_TYPE_KEY = "Content-Type";
   private Date someMinAfterTokenGeneration;
   private DVSettings dvSettings;
   private String accessToken;
   private String expiryTime;

   public DVAuthenticator(DVSettings dvSettings)
   {
      this.dvSettings = dvSettings;
   }

   public Date getSomeMinAfterTokenGeneration()
   {
      return someMinAfterTokenGeneration;
   }

   public void setSomeMinAfterTokenGeneration(Date someMinAfterTokenGeneration)
   {
      this.someMinAfterTokenGeneration = someMinAfterTokenGeneration;
   }

   public String getAccessToken()
   {
      return accessToken;
   }

   public void setAccessToken(String accessToken)
   {
      this.accessToken = accessToken;
   }

   public String getExpiryTime()
   {
      return expiryTime;
   }

   public void setExpiryTime(String expiryTime)
   {
      this.expiryTime = expiryTime;
   }

   public void generateDVAccessToken()
   {
      Response response = null;
      
      try
      {
         StringBuilder getTokenUrl = new StringBuilder();
         getTokenUrl.append(dvSettings.getOauthServerUrl());
         getTokenUrl.append("/");
         getTokenUrl.append(GET_TOKEN_URL_PATH);

         dvLogger.info("FInal Get Token URL: " + getTokenUrl.toString());

         DVGetTokenRequest request = new DVGetTokenRequest();
         request.setClientId(dvSettings.getOauthServerClientId());
         request.setClientSecret(dvSettings.getOauthServerClientSecret());
         request.setScope(dvSettings.getOauthServerScope());
         request.setGrantType(dvSettings.getOauthServerGrantType());
         Gson gson = new Gson();
         Type type = new TypeToken<DVGetTokenRequest>() {}.getType();
         
         String json = gson.toJson(request, type);

         OkHttpClient client = new OkHttpClient.Builder()
                  .connectTimeout(60, TimeUnit.SECONDS)
                  .writeTimeout(10, TimeUnit.SECONDS)
                  .readTimeout(60, TimeUnit.SECONDS).build();

         RequestBody body = RequestBody.create(JSON, json);

         dvLogger.info("Sending OAuth cleintCredentials post request :: "
                  + json);
         
         Request okhttpRequest = new Request.Builder()
                  .url(getTokenUrl.toString()).post(body)
                  .header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE).build();

         response = client.newCall(okhttpRequest).execute();

         String res = response.body().string();
         dvLogger.info("Oauth Response: " + res);
         DVGetTokenResponse clientResponse =
                  gson.fromJson(res, DVGetTokenResponse.class);

         accessToken = clientResponse.getAccessToken();
         expiryTime = clientResponse.getExpiresIn();
         setAccessToken(accessToken);
         Calendar date = Calendar.getInstance();
         long timeStamp = date.getTimeInMillis();
         int oauthTokenUpdateTime =
                  Integer.parseInt(dvSettings.getOauthServerTokenFetchTime());
         someMinAfterTokenGeneration =
                  new Date(timeStamp + (oauthTokenUpdateTime * ONE_MIN_MILLI));
         setExpiryTime(expiryTime);

      }
      catch (Exception e)
      {
         dvLogger.error(
                  "Error in getting oauth token for this resource service. ",
                  e);
      }
      finally {
         if(null != response)
         {
            response.close();
         }
      }
   }
}
