package com.digivalet.pmsi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.settings.DVSettings;
import com.google.gson.JsonObject;

public class DVDataToInRoomDevice
{

   private DVLogger dvLogger = DVLogger.getInstance();
   DVSettings dvSettings;
   int port=0;
   public DVDataToInRoomDevice(DVSettings dvSettings)
   {
      this.dvSettings=dvSettings;
   }
   public boolean SendData(String jsonData,String ip)
   {
      try
      {
         try 
         {
            JsonObject responseObj=new JsonObject();
            responseObj.addProperty("status", "failure");
             String response = responseObj.toString();
                      
             dvLogger.info("Sending Checkin to Controller IP  " + ip + " and room number  ");
             Socket clientSoc = null;
             BufferedReader brObj = null;
             BufferedWriter bwObj = null;
             try 
             {
                 SocketAddress sockaddr = new InetSocketAddress(ip, port);
                 clientSoc = new Socket();
                 int timeoutMs = 5000; // 2.5 seconds
                 clientSoc.connect(sockaddr, timeoutMs);

                 clientSoc.setSoTimeout(10000);
                 brObj = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
                 bwObj = new BufferedWriter(new OutputStreamWriter(clientSoc.getOutputStream()));
                 bwObj.write(jsonData+ "\n");
                 bwObj.flush();

                 if ((response = brObj.readLine()) == null) 
                 {
                    response = responseObj.toString();
                 }
                 else
                 {
                    
                 }
             } catch (Exception x) {
                dvLogger.error("error in sending checkin to controller " , x);
             } finally {
                 if (brObj != null) {
                     brObj.close();
                 }
                 if (bwObj != null) {
                     bwObj.close();
                 }
                 if (clientSoc != null) {
                     clientSoc.close();
                 }
             }
             if(responseObj.get("status").toString().equalsIgnoreCase("success"));
             return true;
         } catch (Exception e) {
            dvLogger.error("Error in sending checkin to controller ",e);
            return false;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending data to controller ", e);
         return false;
      }
   }

}
