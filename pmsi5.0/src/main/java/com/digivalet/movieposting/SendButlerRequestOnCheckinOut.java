package com.digivalet.movieposting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
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

public class SendButlerRequestOnCheckinOut extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private String guestId;
   private String roomNumber;
   private final String CONTENT_TYPE = "application/vnd.digivalet.v1+json";
   private String butlerUrl;
   private String oneAuthToken;
   private int resendCounter = 0;
   private final int timeout = 20;
   private HttpClient client;
   String operation;
   // private DVSettings dvSettings;

   public SendButlerRequestOnCheckinOut(String roomNumber, String guestId,
            String operation)
   {
      this.guestId = guestId;
      this.roomNumber = roomNumber;
      this.operation = operation;
   }

   public void run()
   {
      dvLogger.info("Inside send butler request ");
      sanatize();
      sendToButler();
   }

   public void sanatize()
   {
      try
      {
         System.setProperty("jsse.enableSNIExtension", "false");
         butlerUrl = DVPmsMain.getInstance().getDVSettings().getButlerUrl();
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

         dvLogger.info("URL for butlerUrl : " + butlerUrl);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in init for butler", e);
      }
   }


   private void sendToButler()
   {
      try
      {
         if (null != butlerUrl && !"".equalsIgnoreCase(butlerUrl)
                  && !"na".equalsIgnoreCase(butlerUrl))
         {
            resendCounter++;
            String body =
                     "{\"details\":[{\"data\":[{\"roomNumber\":\"" + roomNumber
                              + "\",\"requestId\":\"\",\"specialstruction\":\"\",\"serviceid\":\"\"}]}],\"operation\":\""
                              + operation + "\",\"feature\":\"Butler\"}";
            String tokenType=DVPmsMain.getInstance().getDVSettings().getButlerAcceestokenType();  
            dvLogger.info(" tokenType "  +tokenType);
            HttpPost post = new HttpPost(butlerUrl);
            post.setHeader("Content-Type", CONTENT_TYPE);
            post.setHeader(tokenType, oneAuthToken);

            StringEntity requestEntity = new StringEntity(body);

            post.setEntity(requestEntity);

            HttpResponse response = client.execute(post);
            dvLogger.info("Response Code : "
                     + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 401
                     && resendCounter < 2)
            {
               dvLogger.info(
                        "Unauthorised Response from Butler, creating new token and retrying");
               DVPmsMain.getInstance().getDvTokenValidation()
                        .init(DVPmsMain.getInstance().getDVSettings());
               sendToButler();
            }

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Data from Butler Service: " + result);
         }
         else
         {
            dvLogger.info("Bulter server is not configured ");
         }


      }
      catch (UnsupportedEncodingException e)
      {
         dvLogger.error("Encoding exception in Butler Request", e);
      }
      catch (ClientProtocolException e)
      {
         dvLogger.error("Client Protocol exception in Butler Request", e);
      }
      catch (IOException e)
      {
         dvLogger.error("IO exception in Butler Request", e);
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

   public long getDate()
   {
      long epoch = 1499347012205L;
      try
      {
         SimpleDateFormat df =
                  new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
         df.setTimeZone(TimeZone.getTimeZone("UTC"));
         String date = df.format(new Date());
         epoch = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz")
                  .parse(date).getTime();
      }
      catch (Exception e)
      {

         dvLogger.error("ERROR OCCURRED while Parsing DATE ", e);
      }

      return epoch;
   }
}
