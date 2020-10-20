package com.digivalet.pmsi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import org.json.JSONObject;
import com.digivalet.core.DVCipherText;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.model.response.datatocontroller.DVKeyExchangeData;
import com.digivalet.pmsi.result.Status;
import com.digivalet.pmsi.settings.DVSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class DVDataToController
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private int port = 9229;
   private final int socketTimeout = 10000;
   private final int connectiontTimeout = 10000;
   private String dvsKey="";
   boolean isEncryptionEnabled=false;
   private DVKeyCommunicationTokenManager communicationTokenManager;
   private int keyId;
   private DVSettings dvSettings;
   public DVDataToController(DVSettings dvSettings,
            DVKeyCommunicationTokenManager communicationTokenManager,
            int keyId)
   {
	  this.dvSettings = dvSettings;
      this.communicationTokenManager = communicationTokenManager;
      this.keyId = keyId;
      
      
   }

   public boolean SendData(String ip, String jsonData,int deviceId)
   {
      try
      {
         try
         {
            try
            {
               port = dvSettings.getSendDataToControllerPort();
               if(dvSettings.isCommunicationEncryption())
               {
                  isEncryptionEnabled=true;
                  dvsKey=communicationTokenManager.getKey(keyId);   
               }else
               {
                  dvLogger.info("Communication encryption "+isEncryptionEnabled);
               }
               
            }
            catch (Exception e)
            {
               dvLogger.error("Error in getting data ", e);
            }
            dvLogger.info("Communication encryption "+isEncryptionEnabled);
            boolean status = false;
            JSONObject responseObj = new JSONObject();
            responseObj.append("status", "failure");
            String response = responseObj.toString();

            dvLogger.info(
                     "DVC IP  " + ip + " and Data " + jsonData);
            if(null==ip)
            {
               dvLogger.info("error in sending data to Device Since its IP is null ");
               return false;
            }
            Socket clientSoc = null;
            BufferedReader brObj = null;
            BufferedWriter bwObj = null;
            try
            {
               SocketAddress sockaddr = new InetSocketAddress(ip, port);
               clientSoc = new Socket();
               clientSoc.setSoTimeout(socketTimeout);
               clientSoc.connect(sockaddr, connectiontTimeout);

               byte[] buffer = new byte[10000];

               bwObj = new BufferedWriter(
                        new OutputStreamWriter(clientSoc.getOutputStream()));
               InputStream inputStream = clientSoc.getInputStream();
               brObj = new BufferedReader(new InputStreamReader(inputStream));
               
               String key=doKeyExchange(brObj,bwObj,inputStream);
               
               
               jsonData=getEncryptedString(jsonData,key);
               dvLogger.info("sending jsonData "+jsonData);
               bwObj.write(jsonData + "\n");
               bwObj.flush();
               dvLogger.info("Successfully flushed data to DVC ");
               int read;
               if (brObj != null)
               {
                  while ((read = inputStream.read(buffer)) != -1)
                  {
                     response = new String(buffer, 0, read);
                     if (response != null)
                     {
                        response = response.trim();
                        
                        if(isEncryptionEnabled)
                        {
                           dvLogger.info("data from controller Enc: " + response);
                           DVCipherText dvCipherText = new DVCipherText();
                           response = dvCipherText.decryptData(key, response, isEncryptionEnabled);   
                        }
                        
                        dvLogger.info("data from controller Dec: " + response);
                        responseObj = new JSONObject(response);
                        if (responseObj.has("status") && responseObj.has("data"))
                        {
                           Status statusdata = new Status();
                           Gson gson = new GsonBuilder().create();
                           statusdata = gson.fromJson(responseObj.toString(),
                                    Status.class);
                           dvLogger.info("Status success for device ID "
                                    + statusdata.getData().getDetails()[0]
                                             .getTargetDeviceId());
                           String Id = statusdata.getData().getDetails()[0]
                                    .getTargetDeviceId();
                           if (Integer.parseInt(Id) == deviceId)
                           {
                              dvLogger.info("Status success ACK for device ID "
                                       + deviceId);
                              return true;
                           }
                        }
                     }
                  }
               }
            }
            catch (Exception x)
            {
               dvLogger.error("error in sending data to Device ", x);
            }
            finally
            {
               if (brObj != null)
               {
                  brObj.close();
               }
               if (bwObj != null)
               {
                  bwObj.close();
               }
               if (clientSoc != null)
               {
                  clientSoc.close();
               }
            }
            {
               return status;
            }
         }
         catch (Exception e)
         {
            dvLogger.error("Error in sending data to device ", e);
            return false;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending data to device ", e);
         return false;
      }
      

   }

   private String doKeyExchange(BufferedReader brObj,BufferedWriter bwObj,InputStream inputStream)
   {
      try
      {
         if (isEncryptionEnabled)
         {
            String data =
                     "{\"command\":{\"feature\":\"dvKey\",\"details\":{\"sessionId\":\""
                              + dvsKey
                              + "\"}},\"deviceId\":\"PMS\",\"timestamp\":\""
                              + System.currentTimeMillis() + "\"}";

            DVCipherText dvCipherText = new DVCipherText();
            dvLogger.info(
                     "Encrypting using key :" + dvsKey + " , data:" + data);

            String cipherText = dvCipherText.encryptData(dvsKey, data,
                     isEncryptionEnabled);
            dvLogger.info("Request sent to client. Request:" + cipherText);
            bwObj.write(cipherText + "\n");
            bwObj.flush();

            byte[] buffer = new byte[10000];
            String response;
            JSONObject responseObj = new JSONObject();
            int read;
            if (brObj != null)
            {
               while ((read = inputStream.read(buffer)) != -1)
               {
                  response = new String(buffer, 0, read);
                  dvLogger.info("response :" + response);
                  if (response != null)
                  {
                     response = response.trim();
                     dvLogger.info("data from controller Enc: " + response);
                     response = dvCipherText.decryptData(dvsKey, response,
                              isEncryptionEnabled);
                     dvLogger.info("data from controller Dec: " + response);
                     responseObj = new JSONObject(response);
                     if (responseObj.has("response"))
                     {
                        DVKeyExchangeData dvKeyExchangeData =
                                 new DVKeyExchangeData();
                        Gson gson = new GsonBuilder().create();
                        dvKeyExchangeData =
                                 gson.fromJson(responseObj.toString(),
                                          DVKeyExchangeData.class);
                        String Id = dvKeyExchangeData.getResponse()[0]
                                 .getDetails().getSessionKey();
                        dvLogger.info("Returning Session Id "+Id);
                        return Id;
                     }
                     else
                     {
                        dvLogger.info("  " + responseObj.has("response"));
                     }
                  }
               }
            }
            else
            {
               dvLogger.error("Buffered reader is null ", null);
            }
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting key", e);
      }
      return "";

   }
   
   String getEncryptedString(String data,String key)
   {
      try
      {
         if(isEncryptionEnabled)
         {
            DVCipherText dvCipherText = new DVCipherText();
            dvLogger.info("Encrypting using key :"
                     + key
                     + " , data:" + data);
            
            String encryptedText
            = dvCipherText.encryptData(key, data, isEncryptionEnabled);
            return encryptedText;
         }else
         {
            return data;   
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting encrypted string ", e);
         return data;
      }

   }


}
