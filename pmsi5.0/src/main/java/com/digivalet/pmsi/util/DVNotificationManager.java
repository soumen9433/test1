package com.digivalet.pmsi.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;

public class DVNotificationManager
{
   private static DVLogger dvLogger = DVLogger.getInstance();   
   private static PoolingHttpClientConnectionManager cm;
   public static final String requiredKeyRoomNumber = "roomNumber";   
   public static final String requiredKeyHotelCode = "hotelCode";   
   public static final String requiredKeyDeviceId = "deviceId";   

   
   public DVNotificationManager()
   {
      
   }

   
   private static CloseableHttpClient getHttpClient()
   {
      CloseableHttpClient httpclient = null;

      try
      {
         SSLContextBuilder builder = new SSLContextBuilder();
         builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
         SSLConnectionSocketFactory sslConnectionSocketFactory =
                  new SSLConnectionSocketFactory(builder.build(),
                           NoopHostnameVerifier.INSTANCE);
         Registry<ConnectionSocketFactory> registry = RegistryBuilder
                  .<ConnectionSocketFactory>create()
                  .register("http", new PlainConnectionSocketFactory())
                  .register("https", sslConnectionSocketFactory).build();

         cm = new PoolingHttpClientConnectionManager(registry);
         cm.setMaxTotal(100);
         httpclient = HttpClients.custom()
                  .setSSLSocketFactory(sslConnectionSocketFactory)
                  .setConnectionManager(cm).build();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while creating CloseableHttpClient\n", e);
      }

      return httpclient;
   }

   public static void postRequestToNotificationEngine(String eventType, Object notifiedObject)
   {
      try
      {

         if (DVPmsMain.getInstance().getDVSettings().isNotificationEnabled())
         {
            dvLogger.info("Message::Event posting to Notification Engine is enabled");
            Runnable task = () -> {
               CloseableHttpClient httpclient = null;

               try
               {
                  
                  if (null != DVPmsMain.getInstance().getDVSettings()
                           .getNotificationEngineBaseURL()
                           && null != DVPmsMain.getInstance().getDVSettings()
                                    .getNotificationEngineActionEndPoint())
                  {
                     String url = DVPmsMain.getInstance().getDVSettings()
                              .getNotificationEngineBaseURL()
                              + DVPmsMain.getInstance().getDVSettings()
                                       .getNotificationEngineActionEndPoint();
                     
                     dvLogger.info("Message::Posting event to Notification Engine, Event:"+eventType);

                     
                     for(int i = 0; i<=3; i++)
                     {
                        
                        try
                        {
              
                           String accessToken = DVPmsMain.getInstance()
                                    .getDvTokenValidation().getAuthToken();

                           dvLogger.info(
                                    "Posting Order Details to Notification Engine: "
                                             + url);


                           httpclient = getHttpClient();
                           HttpPost httpPost = new HttpPost(url);

                           dvLogger.info("Event Data : " + notifiedObject);
                           dvLogger.info("Formate String "
                                    + new String(notifiedObject.toString().replace("\"", "")));
                           dvLogger.info("Json ::::: :   " + new JSONObject(notifiedObject));
                           dvLogger.info("Remove  ::::: :   "
                                    + convertObjectToJsonString(notifiedObject));

                           String data = convertObjectToJsonString(notifiedObject);
                           
                           String json = "{\n" + "  \"feature\": \"event\",\n"
                                    + "  \"operation\": \"action\",\n"
                                    + "  \"eventType\": \"" + eventType + "\",\n"
                                    + "  \"data\": "
                                    + data + "\n" + "}";

                           HttpEntity entity = new StringEntity(json);

                           httpPost.setEntity(entity);

                           httpPost.setHeader(DVPMSIConstants.HEADER_ACCESS_TOKEN,
                                    accessToken);
                           httpPost.setHeader(DVPMSIConstants.HEADER_CONTENT_TYPE,
                                    DVPMSIConstants.HEADER_CONTENT_TYPE_VALUE);
                           httpPost.setHeader(DVPMSIConstants.HEADER_ACCEPT,
                                    DVPMSIConstants.HEADER_ACCEPT_VALUE);
                           httpPost.setHeader(DVPMSIConstants.HEADER_LANGUAGE_CODE,
                                    DVPMSIConstants.HEADER_LANGUAGE_CODE_VALUE);


                           dvLogger.info(
                                    "Sending post request to notification Engine:: "
                                             + json);

                           String res = EntityUtils.toString(
                                    httpclient.execute(httpPost).getEntity());
                           dvLogger.info(
                                    "Response fron Notification Engine :: " + res);
                           
                           JSONObject resultJson = new JSONObject(res);
                           if (resultJson.getBoolean("status"))
                           {
                              dvLogger.info("Successfully sent notification to Notification Engine");
                              break;
                           }
                           else
                           {
                              dvLogger.info("Registration Failed");
                           }
                           
                           httpclient.close();
                        }
                        catch (Exception e)
                        {
                           dvLogger.error("Error in calling POST Ok Http method ", e);
                        }
                     }
                  }
               }
               catch (Exception e)
               {
                  dvLogger.error("Exception while posting notification. Exception:", e);
               }
            };
            Thread t = new Thread(task);
            t.setName("SEND_NOTIFICATION");
            t.start();
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while sending notification. Exception:", e);
      }

   }

   public static String convertObjectToJsonString(Object notifiedObject)
   {
      String[] filteredProperties = new String[10];
      ObjectMapper mapper = new ObjectMapper();
      FilterProvider filters = new SimpleFilterProvider().addFilter("",
               SimpleBeanPropertyFilter.serializeAllExcept(filteredProperties));
      String responseJson = "";
      try
      {
         responseJson =
                  mapper.writer(filters).writeValueAsString(notifiedObject);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while converting object into json. Object:"
                  + notifiedObject + ". Exception : ", e);
      }
      return responseJson;
   }
   
}
