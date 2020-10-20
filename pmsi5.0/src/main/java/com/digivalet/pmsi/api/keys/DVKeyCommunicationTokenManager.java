package com.digivalet.pmsi.api.keys;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.settings.DVSettings;

public class DVKeyCommunicationTokenManager extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   HashMap<Integer, String> keyDetails = new HashMap<Integer, String>();
   DVPmsDatabase dvPmsDatabase;
   String api;
   DVSettings dvSettings;

   public DVKeyCommunicationTokenManager(DVSettings dvSettings, DVPmsDatabase dvPmsDatabase)
   {
      this.dvSettings=dvSettings;
      this.dvPmsDatabase=dvPmsDatabase;
      api = dvSettings.getSecretKeyUrl();
   }

   
   
   
   public void getAllEncryptionKey()
   {
      try
      {
         dvLogger.info("Sending request to server on API:" + api);
         CloseableHttpClient httpClient = HttpClients.createDefault();
         String url = api + "key_id=all";
         dvLogger.info("Sending request to server on Url : " + url);         
         HttpGet httpGet = new HttpGet(url);
         
         httpGet.addHeader("Content-Type", "application/vnd.digivalet.v1+json");
         httpGet.addHeader("Access-Token", dvSettings.getOneAuthToken());

         CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
         String response =
                  EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

         dvLogger.info("GET Response Status for API:" + httpGet.getURI() + "::"
                  + httpResponse.getStatusLine().getStatusCode());
         
         /*{
            "status": true,
            "message": "Found token data.",
            "data": {
                "101": "1b4d25c49c4d32ab18e77731b419159",
                "102": "b4e1a234fcc732ff66bfd0cd0a59f83",
         */
         
         httpClient.close();
         dvLogger.info("Response received from backend server for api:" + api
                  + ". Response:" + response.toString());                
        
         JSONObject jsonObj = new JSONObject(response.toString());         
         if(jsonObj.has("status") && jsonObj.getBoolean("status"))
         {
            if(jsonObj.has("data"))
            {
               JSONObject jsonDataObj = jsonObj.getJSONObject("data");
               for(Iterator iterator = jsonDataObj.keySet().iterator(); iterator.hasNext();) 
               {
                  String key = (String) iterator.next();
                  dvLogger.info(" Key : "+key+"  Value : "+jsonDataObj.get(key));
                  int key_id=dvPmsDatabase.getKeyId(key);
                  keyDetails.put(key_id, String.valueOf(jsonDataObj.get(key)));
                  dvLogger.info("putting in : "+key_id, String.valueOf(jsonDataObj.get(key)));
              }
            }
         }
         else
         {
            dvLogger.info(" Does not has key status and value is false ");
         }     
         httpResponse.close();
      }
      catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      catch (ClientProtocolException e)
      {
         e.printStackTrace();
      }
      catch (UnsupportedOperationException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }      
   }
   
   
   private synchronized String getEncryptionKey(int roomNumber)
   {
      try
      {
         dvLogger.info("Sending request to server on API:" + api);
         CloseableHttpClient httpClient = HttpClients.createDefault();
         String url = api + "key_id=all";
         dvLogger.info("Sending request to server on Url : " + url);         
         HttpGet httpGet = new HttpGet(url);
         
         httpGet.addHeader("Content-Type", "application/vnd.digivalet.v1+json");
         httpGet.addHeader("Access-Token", dvSettings.getOneAuthToken());

         CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
         String response =
                  EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

         dvLogger.info("GET Response Status for API:" + httpGet.getURI() + "::"
                  + httpResponse.getStatusLine().getStatusCode());
         
         /*{
            "status": true,
            "message": "Found token data.",
            "data": {
                "101": "1b4d25c49c4d32ab18e77731b419159",
                "102": "b4e1a234fcc732ff66bfd0cd0a59f83",
         */
         
         httpClient.close();
         dvLogger.info("Response received from backend server for api:" + api
                  + ". Response:" + response.toString());                
        
         JSONObject jsonObj = new JSONObject(response.toString());         
         if(jsonObj.has("status") && jsonObj.getBoolean("status"))
         {
            if(jsonObj.has("data"))
            {
               JSONObject jsonDataObj = jsonObj.getJSONObject("data");
               for(Iterator iterator = jsonDataObj.keySet().iterator(); iterator.hasNext();) 
               {
                  String key = (String) iterator.next();
                  dvLogger.info(" Key : "+key+"  Value : "+jsonDataObj.get(key));
                  int key_id=dvPmsDatabase.getKeyId(key);
                  keyDetails.put(key_id, String.valueOf(jsonDataObj.get(key)));
                  dvLogger.info("putting in : "+key_id, String.valueOf(jsonDataObj.get(key)));
              }
            }
         }
         else
         {
            dvLogger.info(" Does not has key status and value is false ");
         }     
         httpResponse.close();
      }
      catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      catch (ClientProtocolException e)
      {
         e.printStackTrace();
      }
      catch (UnsupportedOperationException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   public void run()
   {
      try
      {
        Thread.currentThread().setName("DVKeyCommunicationTokenManager");
         dvLogger.info("starting key communication manager thread ");
         if(dvSettings.isCommunicationEncryption())
         {
            getAllEncryptionKey();   
         }else
         {
            dvLogger.info("Communication Enryption is not enabled ");
         }
         
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting key manager ", e);
      }
   }

   public String getKey(int keyId)
   {
      if (keyDetails.containsKey(keyId))
      {
         return keyDetails.get(keyId);
      }
      else
      {
        dvLogger.info("keyDetails doesn't have the key id: "+keyId);
         return getEncryptionKey(keyId);
      }

   }
}
