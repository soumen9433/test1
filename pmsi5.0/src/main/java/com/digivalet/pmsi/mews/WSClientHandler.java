package com.digivalet.pmsi.mews;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.java_websocket.enums.ReadyState;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.settings.DVSettings;


public class WSClientHandler extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private String webSocketUrl;
   private String clientToken;
   private String accessToken;
   private DVWebSocketClient dvWebSocketClient;

   public WSClientHandler(DVSettings dvSettings, DVPmsDatabase dvPmsDatabase)
   {

      this.dvPmsDatabase = dvPmsDatabase;
      this.dvSettings = dvSettings;
      accessToken = dvSettings.getPmsAccessToken();
      clientToken = dvSettings.getPmsClientToken();
      webSocketUrl = dvSettings.getPmsWebSocketUrl();
      webSocketUrl = webSocketUrl + "ClientToken=" + clientToken
               + "&AccessToken=" + accessToken;

      dvLogger.info("MEWS Web Socket URL: " + webSocketUrl);
   }

   public WSClientHandler()
   {
      /**
       * Let this constructor be here, At runtime it is used by the library, to
       * create the default instance of this class.
       */
   }

   @Override
   public void run()
   {
      try
      {
         initConnection();
         checkWSConnection();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception in websocket thread\n", e);
      }
   }

   public void initConnection()
   {
      try
      {

         disableSslVerification();

         dvWebSocketClient = new DVWebSocketClient(new URI(webSocketUrl),
                  dvSettings, dvPmsDatabase);

         boolean isWebSocketConnected = dvWebSocketClient
                  .connectBlocking(10 * 1000, TimeUnit.MILLISECONDS);

         dvLogger.info("!!! Is Web Socket Connected: " + isWebSocketConnected
                  + " !!!");

      }
      catch (Exception ex)
      {
         dvLogger.error(
                  "@@@@@@@@@ Error while making connection with Web Socket @@@@@@@@@",
                  ex);
      }

   }

   /**
    * checkConnection() will continuously monitor the connection so it should be
    * call on a seperate thread.
    */
   public void checkWSConnection()
   {
      try
      {

         while (true)
         {
            try
            {
               dvLogger.info("--------- WEB_SOCKET_CLIENT_STATUS : "
                        + dvWebSocketClient.getReadyState());
               if (dvWebSocketClient.getReadyState() != ReadyState.OPEN)
               {
                  dvLogger.info(
                           "--------- WEB_SOCKET_CLIENT : Trying to Connect ----------- ");
                  dvWebSocketClient.reconnectBlocking();
                  dvLogger.info("--------- WEB_SOCKET_CLIENT_STATUS : "
                           + dvWebSocketClient.getReadyState());
               }

            }
            catch (Exception ex)
            {
               dvLogger.error(
                        "Error while checking websocket connection status", ex);
            }

            Thread.sleep(10000);
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Exception in WEB_SOCKET_CLIENT Check Connection: ", e);
      }
   }


   private void disableSslVerification()
   {
      try
      {
         dvLogger.info("Disabling ssl verification ");
         // Create a trust manager that does not validate certificate chains
         TrustManager[] trustAllCerts =
                  new TrustManager[] {new X509TrustManager() {
                     public java.security.cert.X509Certificate[] getAcceptedIssuers()
                     {
                        return null;
                     }

                     public void checkClientTrusted(X509Certificate[] certs,
                              String authType)
                     {}

                     public void checkServerTrusted(X509Certificate[] certs,
                              String authType)
                     {}
                  }};

         // Install the all-trusting trust manager
         SSLContext sc = SSLContext.getInstance("SSL");
         sc.init(null, trustAllCerts, new java.security.SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

         // Create all-trusting host name verifier
         HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session)
            {
               return true;
            }
         };

         // Install the all-trusting host verifier
         HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      }
      catch (NoSuchAlgorithmException e)
      {
         dvLogger.error(
                  "No Such Algorithm Exception while disabling the certificate",
                  e);
      }
      catch (KeyManagementException e)
      {
         dvLogger.error(
                  "Key Management Exception while disabling the certificate",
                  e);
      }
   }
}