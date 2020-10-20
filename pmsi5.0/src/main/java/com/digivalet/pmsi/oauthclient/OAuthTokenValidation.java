package com.digivalet.pmsi.oauthclient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.settings.DVSettings;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OAuthTokenValidation
{

   private DVSettings dvSettings;
   private DVAuthenticator dvAuthenticator;
   private DVLogger dvLogger = DVLogger.getInstance();
   private int retryOneAuth = 0;
   private static final String CONTENT_TYPE = "Content-Type";
   private static final String CONTENT_TYPE_VALUE = "application/json";
   private static final MediaType JSON = MediaType.parse(CONTENT_TYPE_VALUE);

   public OAuthTokenValidation(DVSettings dvSettings,
            DVAuthenticator dvAuthenticator)
   {
      this.dvSettings = dvSettings;
      this.dvAuthenticator = dvAuthenticator;
   }


   public DVTokenValidateResponse validateToken(String accessToken, String url,
            String method)
   {
      String validateTokenUrl =
               dvSettings.getOauthServerUrl() + "/validateToken";

      DVTokenValidateResponse tokenValidateResponse =
               new DVTokenValidateResponse();
      if (dvSettings.isOauthEnabled())
      {
         if (null != accessToken && !("".equalsIgnoreCase(accessToken)))
         {
            Response response = null;
            
            try
            {
               retryOneAuth++;
               dvLogger.info(" One Auth enabled, Validating the client");
               DVTokenValidateRequest tokenObject =
                        new DVTokenValidateRequest();
               tokenObject.setAccessToken(accessToken);
               tokenObject.setUrl(url);
               tokenObject.setMethod(method);
               Gson gson = new Gson();
               Type type = new TypeToken<DVTokenValidateRequest>() {}.getType();
               String json = gson.toJson(tokenObject, type);
               OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS).build();
               RequestBody body = RequestBody.create(JSON, json);
               String dvAccessToken = dvAuthenticator.getAccessToken();
               Request request = new Request.Builder().url(validateTokenUrl)
                        .post(body).header("Access-Token", dvAccessToken)
                        .header(CONTENT_TYPE, CONTENT_TYPE_VALUE).build();
               response = client.newCall(request).execute();
               String res = response.body().string();
               dvLogger.info(
                        "Sending post request:: " + request.toString());
               tokenValidateResponse =
                        gson.fromJson(res, DVTokenValidateResponse.class);
               
               dvLogger.info("OAuth Validation Response: " + tokenValidateResponse.isStatus());
               
               return tokenValidateResponse;
            }
            catch (JsonSyntaxException e)
            {
               dvLogger.error("JsonSyntaxException in validateToken method:  ",
                        e);
            }
            catch (IOException e)
            {
               dvLogger.error("IOException in validateToken method: ", e);
            }
            finally {
               if(null != response)
               {
                  response.close();
               }
            }
         }
         else
         {
            tokenValidateResponse.setStatus(false);
            tokenValidateResponse.setMessage(
                     "Required parameter 'auth_token' doesn't have valid data!");
         }
      }
      else
      {
         tokenValidateResponse.setStatus(true);
         tokenValidateResponse.setMessage("Oauth is disabled");
      }
      return tokenValidateResponse;
   }

}
