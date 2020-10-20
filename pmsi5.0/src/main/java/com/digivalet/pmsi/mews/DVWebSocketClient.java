package com.digivalet.pmsi.mews;

import java.net.URI;
import javax.xml.bind.JAXBException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.exceptions.DVFileException;
import com.digivalet.pmsi.settings.DVSettings;

public class DVWebSocketClient extends WebSocketClient
{

   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;
   private DVPmsMews dvPmsMews;

   public DVWebSocketClient(URI webSocketURI, DVSettings dvSettings,
            DVPmsDatabase dvPmsDatabase)
   {
      this(webSocketURI);
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;

   }

   private DVWebSocketClient(URI uri)
   {
      super(uri);
   }


   @Override
   public void onOpen(ServerHandshake handshakedata)
   {
      dvLogger.info("Connection Opened");

      try
      {
         this.dvPmsMews = (DVPmsMews) DVPmsMain.getInstance()
                  .getDvPmsController().getPms();

         DVMewsSyncThread syncThread =
                  new DVMewsSyncThread(dvSettings, dvPmsDatabase, dvPmsMews);
         syncThread.start();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while initiating Sync Thread\n", e);
      }

   }

   @Override
   public void onMessage(String message)
   {
      try
      {
         this.dvPmsMews = (DVPmsMews) DVPmsMain.getInstance()
                  .getDvPmsController().getPms();
      }
      catch (JAXBException | DVFileException e)
      {
         dvLogger.error("Error while fetching the DVPms object\n", e);
      }

      try
      {
         dvLogger.info("Got msg:" + message);
         JSONObject json = new JSONObject(message);

         if (json.getJSONArray(MewsKeyTags.EVENTS.toString()).getJSONObject(0)
                  .has(MewsKeyTags.TYPE.toString())
                  && json.getJSONArray(MewsKeyTags.EVENTS.toString())
                           .getJSONObject(0)
                           .getString(MewsKeyTags.TYPE.toString())
                           .equalsIgnoreCase(EventEnums.RESERVATION.toString()))
         {
            if (json.getJSONArray(MewsKeyTags.EVENTS.toString())
                     .getJSONObject(0).getString(MewsKeyTags.STATE.toString())
                     .equalsIgnoreCase(EventState.STARTED.toString())
                     || json.getJSONArray(MewsKeyTags.EVENTS.toString())
                              .getJSONObject(0)
                              .getString(MewsKeyTags.STATE.toString())
                              .equalsIgnoreCase(
                                       EventState.PROCESSED.toString()))
            {
               DVParseData dvParseData =
                        new DVParseData(json, dvPmsDatabase, dvSettings,dvPmsMews);

               dvParseData.start();
            }
         } 

      }
      catch (JSONException e)
      {
         dvLogger.error("Exception while capturing events at websocket\n", e);
      }

   }

   @Override
   public void onClose(int code, String reason, boolean remote)
   {

      dvLogger.info("!!! WEB SOCKET CLOSED- Reason: " + reason + ", Code: "
               + code + ", Remote: " + remote + " !!!");

   }

   @Override
   public void onError(Exception ex)
   {

      dvLogger.info("!!! WEB SOCKET ERROR !!!"+ex);

   }

}