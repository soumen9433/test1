package com.digivalet.pmsi.opera;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.digivalet.core.AlertState;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.settings.DVSettings;


public class DVPmsConnectionManager extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   protected boolean connectionFlag = false;
   protected boolean ackFlag = true;
   protected OutputStream osObj;
   protected InputStream inObj;
   protected Socket clientSock;
   private DVSettings dvSettings;
   private InetAddress address;
   private String hostIp;
   private int port = 0;
   private int connectionDelay = 10000;
//   protected boolean LEReqSent = false;
   protected boolean writeFlag = false;
   private DVPmsOpera dvPmsOpera;
   private String stx;
   private String etx;
   private byte bytes[] = new byte[1];
   private DVPmsDatabase dvPmsDatabase;
   public DVPmsConnectionManager(DVSettings dvSettings, DVPmsOpera dvPmsOpera,DVPmsDatabase dvPmsDatabase)
   {
      this.dvSettings = dvSettings;
      this.dvPmsOpera = dvPmsOpera;
      this.dvPmsDatabase=dvPmsDatabase;
      hostIp = new String();
      bytes[0] = 2;
      stx = new String(bytes);
      bytes[0] = 3;
      etx = new String(bytes);
   }

   public void run()
   {
      try
      {
         Thread.currentThread().setName("OPERA-CONNECTION-MANAGER");
         dvLogger.info("dvSettings.getPmsIp() " + dvSettings.getPmsIp());

         hostIp = dvSettings.getPmsIp();
         port = dvSettings.getPmsPort();
         connectionDelay = dvSettings.getConnectionDelay();

         while (true)
         {
            if (connectionFlag == false)
            {
               checkPreviousSocket();
               if (clientSock != null)
               {
                  try
                  {
                     clientSock.close();
                  }
                  catch (Exception ex)
                  {
                     dvLogger.error(
                              "Socket was priviously open and now it is closed ",
                              ex);
                  }
               }

               try
               {
                  // PmsClient.writeFlag = false;
                  address = InetAddress.getByName(hostIp);
                  dvLogger.info(
                           " Pmsi address-port=:::" + address + "-" + port);
                  clientSock = new Socket();
                  clientSock.connect(new InetSocketAddress(address, port),
                           3000);
                  dvLogger.info("After creating socket with " + address
                           + "& port " + port);
                  dvLogger.info("After setting SO timeout ");
                  dvPmsOpera.LaCount=-1;
                  dvPmsOpera.LsCount=-1;
                  dvPmsOpera.pollCount=-1;
                  dvPmsOpera.isFirstLSReceived=false;
                  dvPmsOpera.isLAReceived=false;
                  dvPmsOpera.isPollCommandSend=false;
                  osObj = clientSock.getOutputStream();
                  inObj = clientSock.getInputStream();
                  DVReadDataFromPms readObj =
                           new DVReadDataFromPms(clientSock, this, dvPmsOpera,dvPmsDatabase);
                  Thread t1 = new Thread(readObj);
                  t1.setPriority(Thread.MAX_PRIORITY);
                  t1.start();
                  connectionFlag = true;
                  writeFlag = false;
               }
               catch (Exception e)
               {
            	   dvPmsOpera.pmsAlertState=AlertState.NotConnected;
                  dvPmsOpera.errorMessage=e.getMessage();
                  dvLogger.error("Error in Connection with pms Failed", e);
                  connectionFlag = false;
               }
            }
            try
            {
               Thread.sleep(connectionDelay);
            }
            catch (Exception e)
            {
               dvLogger.error("Error in connecting PMS is ", e);
            }
         }

      }
      catch (Exception e)
      {
         dvLogger.error("Error in connection manager ", e);
      }
   }

   private void checkPreviousSocket()
   {
      try
      {
         try
         {
            clientSock.close();
         }
         catch (Exception e)
         {
            // TODO: handle exception
         }
         try
         {
            osObj.close();
         }
         catch (Exception e)
         {
            // TODO: handle exception
         }
         try
         {
            inObj.close();
         }
         catch (Exception e)
         {
            // TODO: handle exception
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in closing previous socket", e);
      }
   }

   public String getDateTime()
   {
      Date now = new Date();
      SimpleDateFormat formatPattern = new SimpleDateFormat("yyMMdd");
      String date = formatPattern.format(now);
      formatPattern = new SimpleDateFormat("HHmmss");
      String time = formatPattern.format(now);
      String dateTime = "DA" + date + "|TI" + time + "|";
      return dateTime;
   }

   public synchronized boolean writeSignal(String signal)
   {
      try
      {
         signal = signal.trim();
         dvLogger.info("Sending to PMS is : " + signal);
         osObj.write((stx + signal + etx).getBytes());
         osObj.flush();
         return true;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending Data to PMS is : ", e);
         try
         {
            if(e.getMessage().contains("java.net.SocketException"))
            {
               
            }
              
         }
         catch (Exception e2)
         {
            // TODO: handle exception
         }
         return false;
      }
   }
}
