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
import com.digivalet.pmsi.model.MovieData;

public class DVSendMovieEvent extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private String guestId;
   private String roomNumber;
   private final String CONTENT_TYPE = "application/vnd.digivalet.v1+json";
   private String digivaletServiceUrl;
   private String oneAuthToken;
   private int resendCounter = 0;
   private final int timeout = 20;
   private HttpClient client;
   private MovieData movieData;
   private String inRoomDeviceId;
   // private DVSettings dvSettings;

   public DVSendMovieEvent(String roomNumber, String guestId,MovieData movieData,String inRoomDeviceId)
   {
      this.guestId = guestId;
      this.roomNumber = roomNumber;
      this.movieData=movieData;
      this.inRoomDeviceId=inRoomDeviceId;
   }

   public void run()
   {
      dvLogger.info("Inside movie post event run ");
      sanatize();
      postEvent();
   }

   public void sanatize()
   {
      try
      {
         System.setProperty("jsse.enableSNIExtension", "false");
         digivaletServiceUrl = DVPmsMain.getInstance().getDVSettings().getDigivaletServiceUrl();
         if(digivaletServiceUrl.contains("postMovie?"))
         {
            digivaletServiceUrl=digivaletServiceUrl.replaceAll("postMovie?", "");
         }
         digivaletServiceUrl = digivaletServiceUrl+"/postMovie?";
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

         dvLogger.info("URL for Digivalet Service : " + digivaletServiceUrl);



      }
      catch (Exception e)
      {
         dvLogger.error("Error in init for movie popcorn", e);
      }
   }


   private void postEvent()
   {
      try
      {
         
         if (null != digivaletServiceUrl && !"na".equalsIgnoreCase(digivaletServiceUrl))
         {
            resendCounter++;
            String body =
                     "{\"details\":"
                     + "["
                     + "{\"data\":"
                     + "["
                     + "{\"movieId\":"+"\""+movieData.getMovieId()+"\" "
                     + ",\"startTime\":"+"\""+movieData.getStartTime()+"\" "
                     + ",\"endTime\":"+"\""+movieData.getEndTime()+"\" "
                     + ",\"seek\":"+"\""+movieData.getSeek()+"\" "
                     + ",\"audioId\":"+"\""+movieData.getAudioId()+"\" "
                     + ",\"subtitleId\":"+"\""+movieData.getSubtitleId()+"\" "
                     + ",\"duration\":"+"\""+movieData.getDuration()+"\" "
                     + ",\"dimension\":"+"\""+movieData.getDimension()+"\" "
                     + ",\"alignment\":"+"\""+movieData.getAlignment()+"\" "
                     + ",\"isFinished\":"+movieData.getIsFinished()
                     + ",\"state\":"+"\""+movieData.getState()+"\" "
                     + "}"
                     + "]"
                     + ",\"dvcId\":\"\","
                     + "\"inRoomDeviceId\":"+inRoomDeviceId
                     + "}"
                     + "]"
                     + ","
                     + "\"operation\":\"movieEvent\","
                     + "\"feature\":\"movie\""
                     + "}";
            
            
            HttpPost post = new HttpPost(digivaletServiceUrl + "roomNumber="
                     + roomNumber + "&guestId=" + guestId);
            dvLogger.info("URL " + digivaletServiceUrl + "roomNumber="
                     + roomNumber + "&guestId=" + guestId + " body:" + body);

            post.setHeader("Content-Type", CONTENT_TYPE);
            post.setHeader("Access-Token", oneAuthToken);

            StringEntity requestEntity = new StringEntity(body);

            post.setEntity(requestEntity);

            HttpResponse response = client.execute(post);
            dvLogger.info("Response Code : "
                     + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 401
                     && resendCounter < 2)
            {
               dvLogger.info(
                        "Unauthorised Response from Digivalet Service, creating new token and retrying");
               DVPmsMain.getInstance().getDvTokenValidation()
                        .init(DVPmsMain.getInstance().getDVSettings());
               postEvent();
            }

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Data from digivalet Service: " + result);
         }
         else
         {
            dvLogger.info("Movie posting event is disabled");
         }
      }
      catch (UnsupportedEncodingException e)
      {
         dvLogger.error("Encoding exception in movie posting event Request", e);
      }
      catch (ClientProtocolException e)
      {
         dvLogger.error("Client Protocol exception in digivalet service Request",
                  e);
      }
      catch (IOException e)
      {
         dvLogger.error("IO exception in digivalet service Request", e);
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
