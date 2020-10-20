package com.digivalet.pmsi.events;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.datatypes.DVPMSMessageData;
import com.digivalet.pmsi.settings.DVSettings;

public class DvSendMessage implements Runnable
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVMessageEvent dvEvent;
   private DVPmsDatabase dvPmsDatabase;

   public DvSendMessage(DVMessageEvent dvEvent, DVSettings dvSettings,
            DVPmsDatabase dvPmsDatabase)
   {
      this.dvEvent = dvEvent;
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
   }

   @Override
   public void run()
   {
      try
      {
         String URL = dvSettings.getMessageAlertUrl();
         if (null != URL && !"".equalsIgnoreCase(URL)
                  && !"na".equalsIgnoreCase(URL))
         {

            CloseableHttpClient client = HttpClientBuilder.create().build();

            HttpPost post = new HttpPost(URL);
            String oneAuthToken = DVPmsMain.getInstance().getDvTokenValidation()
                     .getAuthToken();

            dvLogger.info("token " + oneAuthToken);
            post.setHeader("Content-Type", "application/vnd.digivalet.v1+json");
            post.setHeader("Access-Token", oneAuthToken);
            Map<DVPMSMessageData, Object> data = dvEvent.getMessageData();
            // int keyId=Integer.parseInt(
            // data.get(DVPMSMessageData.key_id).toString());
            int DvKeyId = dvPmsDatabase
                     .getKeyId(data.get(DVPMSMessageData.key_id).toString());
            data.put(DVPMSMessageData.key_id, DvKeyId);
            String messageTitle = dvPmsDatabase.getGuestName(
                     data.get(DVPMSMessageData.guestId).toString(), DvKeyId);
            messageTitle = messageTitle + " : Message From Front Desk";
            data.put(DVPMSMessageData.title, messageTitle);
            ObjectMapper mapper = new ObjectMapper();
            String body = mapper.writeValueAsString(data);
            dvLogger.info("request body for service " + body);
            StringEntity requestEntity = new StringEntity(body, "UTF-8");
            dvLogger.info("request set entity is " + requestEntity.toString());
            post.setEntity(requestEntity);

            CloseableHttpResponse response = client.execute(post);
            dvLogger.info("Response Code : "
                     + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }
            response.close();
            client.close();
         }
         else
         {
            dvLogger.info("Message URL is not configured  ");
         }
      }
      catch (Exception e)

      {
         dvLogger.error("Error in sending bill details ", e);
      }
   }

}
