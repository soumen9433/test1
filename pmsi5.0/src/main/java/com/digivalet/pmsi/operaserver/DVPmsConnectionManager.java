package com.digivalet.pmsi.operaserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.settings.DVSettings;


public class DVPmsConnectionManager extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   protected boolean connectionFlag = false;
   protected boolean ackFlag = true;
   protected OutputStream osObj;
   protected Socket clientSock;
   private DVSettings dvSettings;
   private InetAddress address;
   private String hostIp;
   private int port = 7007;
   private int connectionDelay = 10000;
   protected boolean LEReqSent = false;
   protected boolean writeFlag = false;
   protected boolean isKeyExchanged= false;
   public InputStream isObj;
   private DVPmsOpera dvPmsOpera;
   private String stx;
   private String etx;
   private byte bytes[] = new byte[1];
   public final String defaultKey="d1i2g3i4v5a6l7et";
   public String exchangedKey="d1i2g3i4v5a6l7et";
   private DVPmsDatabase dvPmsDatabase;
   private ServerSocket server;
   private DVEncryptDecrypt dvEncryptDecrypt;
   public DVPmsConnectionManager(DVSettings dvSettings, DVPmsOpera dvPmsOpera,
            DVPmsDatabase dvPmsDatabase,DVEncryptDecrypt dvEncryptDecrypt)
   {
      this.dvSettings = dvSettings;
      this.dvPmsOpera = dvPmsOpera;
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvEncryptDecrypt=dvEncryptDecrypt;
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

         Thread.currentThread().setName("OPERA-SERVER-CONNECTION-MANAGER");
         port = dvSettings.getPmsPort();
         
//         connectionDelay = dvSettings.getConnectionDelay();
         server = new ServerSocket(port);
         while (true)
         {
            dvLogger.info("Waiting for client request");
            Socket client = server.accept();
            dvLogger.info("New Socket client connected "
                     + client.getRemoteSocketAddress());
            checkPreviousSocket();
            clientSock =client;
            try
            {
               osObj=clientSock.getOutputStream();
               isObj=clientSock.getInputStream();
               isKeyExchanged=false;
               DVReadDataFromPms readObj = new DVReadDataFromPms(client,
                        this, dvPmsOpera, dvPmsDatabase,dvEncryptDecrypt);
               Thread t1 = new Thread(readObj);
               t1.setPriority(Thread.MAX_PRIORITY);
               t1.start();
            }
            catch (Exception e)
            {
              dvLogger.error("Error in socket connection ",e);
            }
         }
      }
      catch (Exception e)
      {
         try
         {
            server.close();
            dvLogger.error("Erorr in listneing to server socket ", e);

         }
         catch (IOException e1)
         {
            dvLogger.error("Error ", e1);
         }
      }
   }

   
   private String generateKey(int len)
   {

      String lower = "abcdefghijklmnopqrstuvwxyz";
      String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      String digit = "0123456789";
      String special = "9876543210";

      StringBuilder sb;
      SecureRandom rnd = new SecureRandom();

      sb = new StringBuilder(len);
      for (int i = 0; i < len; i++)
      {
         sb.append(lower.charAt(rnd.nextInt(lower.length())));
         sb.append(upper.charAt(rnd.nextInt(upper.length())));
         sb.append(digit.charAt(rnd.nextInt(digit.length())));
         sb.append(special.charAt(rnd.nextInt(special.length())));

      }
      List<String> list = Arrays.asList(sb.toString().split(""));
      Collections.shuffle(list);
      StringBuffer stb = new StringBuffer();
      for (String c : list)
      {
         stb.append(c);
      }
      return stb.toString();
   }
   public String generateKey()
   {
      final int keyLength = 4;
      String secretKey = "";
      try
      {
        System.out.println("creating key of length:" + keyLength);
        System.out.println("creating key of length:" + keyLength);
         secretKey = generateKey(keyLength);
      }
      catch (Exception e)
      {
//         secretKey = dvRmsConnectionManager.defaultKey;
//         dvLogger.info("Error exception :" + e);
         e.printStackTrace();
      }
      return secretKey;
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
            isObj.close();
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
         dvLogger.info("Sending to PMS Decrypted is : " + signal);
         if(isKeyExchanged)
         {
            signal=dvEncryptDecrypt.encrypt(exchangedKey, signal);

         }else
         {
            signal=dvEncryptDecrypt.encrypt(defaultKey, signal);
         }
         dvLogger.info("Sending to PMS Encrypted is : " + signal);
         osObj.write((stx + signal + etx).getBytes());
         osObj.flush();
         return true;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in sending Data to PMS is : ", e);
         try
         {
            if (e.getMessage().contains("java.net.SocketException"))
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
