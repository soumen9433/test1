package com.digivalet.pmsialert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import com.digivalet.core.AlertState;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.settings.DVSettings;

public class AlertMonitringService extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private AlertState current_status = AlertState.Na;
   private final int Sleep = 10000;
   private final int InitialSleep = 30000;


   public AlertMonitringService(DVSettings dvSettings)
   {
      this.dvSettings = dvSettings;
   }
   @Override
   public void run()
   {
      try
      {
         Thread.currentThread().setName("ALERT-MONITORING");
         Thread.sleep(InitialSleep);
         dvLogger.info("Starting PMS monitoring thread ");
         while (true)
         {
            try
            {
               dvLogger.info("current_status: " + current_status + "  "
                        + DVPmsMain.getInstance().getDvPmsController()
                                 .connectionStatus());
               if (DVPmsMain.getInstance().getDvPmsController()
                        .connectionStatus() != current_status)
               {
                  current_status = DVPmsMain.getInstance().getDvPmsController()
                           .connectionStatus();


                  if (current_status == AlertState.Connected)
                  {
                     DVPmsMain.getInstance().getDvPmsDatabase()
                              .setInterfaceStatus(1, 1, "");
                  }
                  else if (current_status == AlertState.NotConnected)
                  {
                     String errorLog = DVPmsMain.getInstance()
                              .getDvPmsController().getErrorLog();
                     dvLogger.info("Error Message : " + errorLog);
                     DVPmsMain.getInstance().getDvPmsDatabase()
                              .setInterfaceStatus(0, 0, errorLog);
                  }
                  else if (current_status == AlertState.LinkNotAlive)
                  {
                     DVPmsMain.getInstance().getDvPmsDatabase()
                              .setInterfaceStatus(1, 0, "");
                  }
                  else if (current_status == AlertState.LinkNotUp)
                  {
                     DVPmsMain.getInstance().getDvPmsDatabase()
                              .setInterfaceStatus(1, 0, "");
                  }
                  String mailsmsUrl = dvSettings.getPrinterMailerUrl();
                  String oneAuthToken = DVPmsMain.getInstance()
                           .getDvTokenValidation().getAuthToken();
                  String mailTo = dvSettings.getAlertMailId();
                  String time = "";
                  DateFormat dateFormat =
                           new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                  Date date = new Date();
                  time = (dateFormat.format(date));
                  if (mailsmsUrl != null && !mailsmsUrl.equalsIgnoreCase("na"))
                  {

                     dvLogger.info("printer mailer method call" + mailsmsUrl);

                     if (current_status == AlertState.Connected)
                     {
                        String dataToSend =
                                 "{ \"details\": [ { \"postDetails\": "
                                          + "[ { \"orderId\": 0, \"orderType\": \"PMSI\", \"source\": \"PMS\","
                                          + " \"to\":\"" + mailTo
                                          + "\", \"subject\": \"Hotel's PMS connected\", "
                                          + "\"body\": \"Hotel's PMS is now connected at:"
                                          + time + " \" } ] } ], "
                                          + "\"feature\": \"PMSI\", \"operation\": \"PmsMail\"}";

                        System.setProperty("jsse.enableSNIExtension", "false");

                        HttpClientContext context = HttpClientContext.create();
                        CookieStore cookieStore = new BasicCookieStore();
                        context.setCookieStore(cookieStore);

                        RequestConfig.Builder requestBuilder =
                                 RequestConfig.custom();
                        requestBuilder =
                                 requestBuilder.setConnectTimeout(30 * 1000);
                        requestBuilder = requestBuilder
                                 .setConnectionRequestTimeout(30 * 1000);

                        CloseableHttpClient client = HttpClientBuilder.create()
                                 .setDefaultCookieStore(cookieStore)
                                 .setRedirectStrategy(new LaxRedirectStrategy())
                                 .setDefaultRequestConfig(
                                          requestBuilder.build())
                                 .build();


                        HttpPost post = new HttpPost(mailsmsUrl);

                        post.setHeader("Content-Type",
                                 "application/vnd.digivalet.v1+json");
                        post.setHeader("Access-Token", oneAuthToken);

                        StringEntity requestEntity =
                                 new StringEntity(dataToSend);

                        post.setEntity(requestEntity);

                        CloseableHttpResponse response = client.execute(post);
                        dvLogger.info("Response Code : "
                                 + response.getStatusLine().getStatusCode());

                        BufferedReader rd =
                                 new BufferedReader(new InputStreamReader(
                                          response.getEntity().getContent()));

                        StringBuffer result = new StringBuffer();
                        String line = "";
                        while ((line = rd.readLine()) != null)
                        {
                           result.append(line);
                        }

                        dvLogger.info(
                                 "Data from PrinterMailer Service: " + result);
                        client.close();
                        response.close();
                     }
                     else if (current_status == AlertState.NotConnected)
                     {
                        dvLogger.info(
                                 "printer mailer method call" + mailsmsUrl);
                        String errorLog = DVPmsMain.getInstance()
                                 .getDvPmsController().getErrorLog();
                        String dataToSend =
                                 "{ \"details\": [ { \"postDetails\": "
                                          + "[ { \"orderId\": 0, \"orderType\": \"PMSI\", \"source\": \"PMS\","
                                          + " \"to\":\"" + mailTo
                                          + "\", \"subject\": \"Unable to connect Hotel's PMS\", "
                                          + "\"body\": \"Unable to connect Hotel's PMS at:"
                                          + time + " ,Error:"+errorLog+" \" } ] } ], "
                                          + "\"feature\": \"PMSI\", \"operation\": \"PmsMail\"}";
                        System.setProperty("jsse.enableSNIExtension", "false");
                        CloseableHttpClient client =
                                 HttpClientBuilder.create().build();
                        HttpPost post = new HttpPost(mailsmsUrl);
                        dvLogger.info("In send mail token " + oneAuthToken);
                        post.setHeader("Content-Type",
                                 "application/vnd.digivalet.v1+json");
                        post.setHeader("Access-Token", oneAuthToken);

                        StringEntity requestEntity =
                                 new StringEntity(dataToSend);

                        post.setEntity(requestEntity);

                        CloseableHttpResponse response = client.execute(post);
                        dvLogger.info("Response Code : "
                                 + response.getStatusLine().getStatusCode());

                        BufferedReader rd =
                                 new BufferedReader(new InputStreamReader(
                                          response.getEntity().getContent()));

                        StringBuffer result = new StringBuffer();
                        String line = "";
                        while ((line = rd.readLine()) != null)
                        {
                           result.append(line);
                        }

                        String responses = result.toString();
                        if (responses.contains("true"))
                        {
                           dvLogger.info(
                                    "Successfully sent data to Printer Mailer ");

                        }
                        dvLogger.info(
                                 "Data from PrinterMailer Service: " + result);

                        client.close();
                        response.close();
                     }
                     else if (current_status == AlertState.LinkNotAlive || current_status == AlertState.LinkNotUp)
                     {

                        dvLogger.info(
                                 "printer mailer method call" + mailsmsUrl);

                        String dataToSend =
                                 "{ \"details\": [ { \"postDetails\": "
                                          + "[ { \"orderId\": 0, \"orderType\": \"PMSI\", \"source\": \"PMS\","
                                          + " \"to\":\"" + mailTo
                                          + "\", \"subject\": \"Link not Alive with Hotel's PMS\", "
                                          + "\"body\": \"Link not Alive with Hotel's PMS at:"
                                          + time + " \" } ] } ], "
                                          + "\"feature\": \"PMSI\", \"operation\": \"PmsMail\"}";
                        System.setProperty("jsse.enableSNIExtension", "false");
                        CloseableHttpClient client =
                                 HttpClientBuilder.create().build();
                        HttpPost post = new HttpPost(mailsmsUrl);
                        dvLogger.info("In send mail token " + oneAuthToken);
                        post.setHeader("Content-Type",
                                 "application/vnd.digivalet.v1+json");
                        post.setHeader("Access-Token", oneAuthToken);

                        StringEntity requestEntity =
                                 new StringEntity(dataToSend);

                        post.setEntity(requestEntity);

                        CloseableHttpResponse response = client.execute(post);
                        dvLogger.info("Response Code : "
                                 + response.getStatusLine().getStatusCode());

                        BufferedReader rd =
                                 new BufferedReader(new InputStreamReader(
                                          response.getEntity().getContent()));

                        StringBuffer result = new StringBuffer();
                        String line = "";
                        while ((line = rd.readLine()) != null)
                        {
                           result.append(line);
                        }

                        String responses = result.toString();
                        if (responses.contains("true"))
                        {
                           dvLogger.info(
                                    "Successfully sent data to Printer Mailer ");

                        }
                        dvLogger.info(
                                 "Data from PrinterMailer Service: " + result);
                        client.close();
                        response.close();
                     }


                  }
               }

               Thread.sleep(Sleep);
            }
            catch (Exception e)
            {
               try
               {
                  Thread.sleep(Sleep);
               }
               catch (Exception e2)
               {
                  e.printStackTrace();
               }
            }

         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in checking PMS status ", e);

      }
   }

}
