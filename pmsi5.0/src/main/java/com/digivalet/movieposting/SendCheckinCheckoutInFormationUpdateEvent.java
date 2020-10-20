package com.digivalet.movieposting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import javax.xml.bind.JAXBException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.exceptions.DVFileException;
import com.digivalet.pmsi.model.CheckinCheckoutData;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SendCheckinCheckoutInFormationUpdateEvent extends Thread
{
   private String guestId;
   private String roomNumber;
   private CheckinCheckoutData details = new CheckinCheckoutData();
   String operation;
   private final String CONTENT_TYPE = "application/vnd.digivalet.v1+json";
   private String serviceUrl;
   private String oneAuthToken;
   private DVLogger dvLogger = DVLogger.getInstance();
   private HttpClient client;
   private int resendCounter = 0;
   private final int timeout = 20;
   private String guestType = "";
   private String hotelCode = "";

   public SendCheckinCheckoutInFormationUpdateEvent(String hotelCode,
            String guestType, String guestId, String roomNumber,
            CheckinCheckoutData details, String operation)
   {
      this.details = details;
      this.roomNumber = roomNumber;
      this.operation = operation;
      this.guestId = guestId;
      this.guestType = guestType;
      this.hotelCode = hotelCode;

   }

   public void run()
   {
      dvLogger.info("Inside send Checkin check out  request ");
      sanatize();
      sendToDigivaletService();
   }

   public void sanatize()
   {
      try
      {
         System.setProperty("jsse.enableSNIExtension", "false");
         serviceUrl = DVPmsMain.getInstance().getDVSettings()
                  .getDigivaletServiceUrl();
         if (serviceUrl.contains("postMovie?"))
         {
            serviceUrl = serviceUrl.replaceAll("postMovie?", "");
         }

         if (operation.equalsIgnoreCase("guestInformationUpdate"))
         {
            serviceUrl = serviceUrl + "/updateuser";
         }
         else if (operation.equalsIgnoreCase("checkin"))
         {
            serviceUrl = serviceUrl + "/checkin";
         }
         else
         {
            serviceUrl = serviceUrl + "/checkout";
         }
         oneAuthToken =
                  DVPmsMain.getInstance().getDvTokenValidation().getAuthToken();

         HttpClientContext context = HttpClientContext.create();
         CookieStore cookieStore = new BasicCookieStore();
         context.setCookieStore(cookieStore);

         RequestConfig.Builder requestBuilder = RequestConfig.custom();
         requestBuilder = requestBuilder.setConnectTimeout(timeout * 1000);
         requestBuilder =
                  requestBuilder.setConnectionRequestTimeout(timeout * 1000);

         this.client = HttpClientBuilder.create()
                  .setDefaultCookieStore(cookieStore)
                  .setRedirectStrategy(new LaxRedirectStrategy())
                  .setDefaultRequestConfig(requestBuilder.build()).build();

         dvLogger.info("URL for digivalet service : " + serviceUrl);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in init for check in check out event", e);
      }
   }

   private void sendToDigivaletService()
   {
      String body = "";
      ObjectMapper mapper = new ObjectMapper();
      try
      {
         if (null != serviceUrl && !"".equalsIgnoreCase(serviceUrl)
                  && !"na".equalsIgnoreCase(serviceUrl))
         {
            resendCounter++;
            if (operation.equalsIgnoreCase("guestInformationUpdate"))
            {
               body = "{\r\n  \"details\": [\r\n    "
                        +mapper.writeValueAsString(this.details)
                        + "   \r\n  ],\r\n  \"operation\": \"updateuser\",\r\n  \"feature\": \"CheckinCheckout\"\r\n}";
            }
            else if (operation.equalsIgnoreCase("checkin"))
            {
               body = "{\r\n  \"details\": [\r\n    "
                       +mapper.writeValueAsString(this.details)
                       + "   \r\n  ],\r\n  \"operation\": \"checkin\",\r\n  \"feature\": \"CheckinCheckout\"\r\n}";
            }
            else
            {
               body = "{\r\n  \"details\": [\r\n    "
                       +mapper.writeValueAsString(this.details)
                       + "   \r\n  ],\r\n  \"operation\": \"checkout\",\r\n  \"feature\": \"CheckinCheckout\"\r\n}";
            }
            dvLogger.info("Check new json is ok" + body);

            String queryParameter = "?hotelCode=" + hotelCode + "&roomNumber="
                     + roomNumber + "&guestId=" + guestId + "&guestType="
                     + guestType + "";
            // http://localhost:8080/digivalet-services/checkin?hotelCode=primary&roomNumber=&guestId=1111&guestType=3263
            serviceUrl = serviceUrl + queryParameter;
            dvLogger.info("in data parameter for url is " + serviceUrl);
            HttpPost post = new HttpPost(serviceUrl);

            post.setHeader("Content-Type", CONTENT_TYPE);
            post.setHeader("Access-Token", oneAuthToken);

            StringEntity requestEntity = new StringEntity(body);

            post.setEntity(requestEntity);

            HttpResponse response = client.execute(post);
            dvLogger.info("Response Code : from digivalet service"
                     + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 401
                     && resendCounter < 2)
            {
               dvLogger.info(
                        "Unauthorised Response from Digivalet service, creating new token and retrying");
               DVPmsMain.getInstance().getDvTokenValidation()
                        .init(DVPmsMain.getInstance().getDVSettings());
               sendToDigivaletService();
            }

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Data from digivalet  Service: " + result);
         }
         else
         {
            dvLogger.info("digivalet service  server is not configured ");
         }


      }
      catch (UnsupportedEncodingException e)
      {
         dvLogger.error("Encoding exception in Digivalet service Request", e);
      }
      catch (ClientProtocolException e)
      {
         dvLogger.error(
                  "Client Protocol exception in checkin checkout event Request",
                  e);
      }
      catch (IOException e)
      {
         dvLogger.error("IO exception in checkin and checkout Request", e);
      }
      catch (JAXBException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (DVFileException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}
