package com.digivalet.pmsi.opera;

import java.io.InputStream;
import java.net.Socket;
import com.digivalet.core.AlertState;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.database.DVPmsDatabase;


public class DVReadDataFromPms implements Runnable
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private InputStream isObj;
   private String readLine;
   private Socket socket;
   private DVPmsConnectionManager dvPmsConnectionManager;
   private DVPmsOpera dvPmsOpera;
   private DVPmsDatabase dvPmsDatabase;

   public DVReadDataFromPms(Socket socket, DVPmsConnectionManager dvPmsConnectionManager,DVPmsOpera dvPmsOpera,DVPmsDatabase dvPmsDatabase)
   {
      try
      {
         this.dvPmsConnectionManager = dvPmsConnectionManager;
         this.socket = socket;
         this.dvPmsOpera=dvPmsOpera;
         this.dvPmsDatabase=dvPmsDatabase;
         isObj = socket.getInputStream();
         dvPmsConnectionManager.clientSock.setSoTimeout(dvPmsOpera.lsTimeout);
      }
      catch (Exception e)
      {
         dvLogger.error("Error in Constructor ", e);
      }

   }

   public void run()
   {
      try
      {
         Thread.currentThread().setName("OPERA_READ");
         byte data[] = new byte[10240];
         while (true)
         {
            try
            {
               readLine = "";
               dvLogger.info("Waiting for read in client");
               int dataLength;
               try
               {
                  dataLength = isObj.read(data);
                  if (dataLength == -1)
                  {
                     if (isObj != null && socket != null)
                     {
                        dvLogger.info("Sending LE if get -1");
                        dvPmsConnectionManager.writeSignal("LE|" + dvPmsConnectionManager.getDateTime());
                        Thread.sleep(1000);
                     }

                     if (isObj != null)
                     {
                        isObj.close();
                     }
                     if (socket != null)
                     {
                        socket.close();
                     }
                     dvPmsConnectionManager.connectionFlag = false;
                     break;
                  }
                  
                  for (int i = 0; i < dataLength; i++)
                  {
                     readLine = readLine + Character.toString((char) data[i]);
                  }
                  dvLogger.info("Data from PMS is " + readLine);
               }
               catch (Exception ex)
               {
                  try
                  {
                     dvLogger.error("error in Client run ", ex);
                     dvLogger.info(" dvPmsOpera.isFirstLSReceived "+dvPmsOpera.isFirstLSReceived+"  "+dvPmsOpera.isLAReceived);
                     
                     if(!dvPmsOpera.isFirstLSReceived) 
                     {
                    	 dvPmsOpera.LsCount++;
                    	 dvPmsConnectionManager.writeSignal("LS|" + dvPmsConnectionManager.getDateTime());
                         dvPmsConnectionManager.clientSock.setSoTimeout(dvPmsOpera.lsTimeout);
                         dvLogger.info("dvPmsOpera.LsCount  "+dvPmsOpera.LsCount);
                         if(dvPmsOpera.LsCount==5) 
                         {
                        	 dvLogger.info("Will Break connection with Opera and retry ");
                        	 dvPmsOpera.pmsAlertState=AlertState.LinkNotUp;
                        	 break;
                         }
                     }
                     else if(!dvPmsOpera.isLAReceived) 
                     {
                    	 dvPmsOpera.LaCount++;
                    	 dvPmsConnectionManager.writeSignal("LS|" + dvPmsConnectionManager.getDateTime());
                         dvPmsConnectionManager.clientSock.setSoTimeout(dvPmsOpera.laTimeout);
                         dvLogger.info("dvPmsOpera.LaCount  "+dvPmsOpera.LaCount);
                         if(dvPmsOpera.LaCount==5) 
                         {
                        	 dvLogger.info("Will Break connection with Opera and retry ");
                        	 dvPmsOpera.pmsAlertState=AlertState.LinkNotAlive;
                        	 break;
                         }
                     }
                     else if(dvPmsOpera.isPollCommandSend) 
                     {
                    	 dvPmsOpera.pollCount++;
                    	 dvPmsConnectionManager.writeSignal("LA|" + dvPmsConnectionManager.getDateTime());
                         dvPmsConnectionManager.clientSock.setSoTimeout(dvPmsOpera.laTimeout);
                         if(dvPmsOpera.pollCount==5) 
                         {
                        	 dvLogger.info("Will Break connection with Opera and retry ");
                        	 dvPmsOpera.pmsAlertState=AlertState.LinkNotAlive;
                        	 break;
                         }
                     }
                     else if (ex.toString().startsWith("java.net.SocketTimeoutException"))
                     {
                        if (dvPmsConnectionManager.clientSock.getSoTimeout() == dvPmsOpera.socketTimeout)
                        {
                        	dvLogger.info("Sending Link Alive Poll command ");
                           dvPmsConnectionManager.writeSignal("LS|" + dvPmsConnectionManager.getDateTime());
                           dvPmsConnectionManager.clientSock.setSoTimeout(dvPmsOpera.laTimeout);
                          /* readLine = "";
                           dataLength = isObj.read(data);
                           for (int i = 0; i < dataLength; i++)
                           {
                              readLine = readLine + Character.toString((char) data[i]);
                           }
                           dvLogger.info("Data from PMS is After Sending LA is " + readLine);*/
                        }
                        /*else
                        {
                           
                            * Waiting for 3 sec otherwise initiate communication with LS
                            
                           dvPmsConnectionManager.writeSignal("LS|" + dvPmsConnectionManager.getDateTime());
                           dvPmsConnectionManager.clientSock.setSoTimeout(11000);
                           readLine = "";
                           dataLength = isObj.read(data);
                           for (int i = 0; i < dataLength; i++)
                           {
                              readLine = readLine + Character.toString((char) data[i]);
                           }
                           dvLogger.info("Data from PMS is After Sending LS is " + readLine);
                        }*/
                     }
                     else
                     {
                        if (isObj != null && socket != null)
                        {
                           dvLogger.info("Sending LE if any other exception socketetTimeoutException");
                           dvPmsConnectionManager.writeSignal("LE|" + dvPmsConnectionManager.getDateTime());
                           Thread.sleep(1000);
                        }

                        if (isObj != null)
                        {
                           isObj.close();
                        }
                        if (socket != null)
                        {
                           socket.close();
                        }
                        dvPmsConnectionManager.connectionFlag = false;
                        dvLogger.info("ex is not java.net.socketetTimeoutException");
                        break;
                     }
                  }
                  catch (Exception ex12)
                  {
                     ex12.printStackTrace();
                     if (isObj != null && socket != null)
                     {
                        dvPmsConnectionManager.writeSignal("LE|" + dvPmsConnectionManager.getDateTime());
                        Thread.sleep(dvPmsOpera.laTimeout);
                     }

                     if (isObj != null)
                     {
                        isObj.close();
                     }
                     if (socket != null)
                     {
                        socket.close();
                     }
                     // Error Need to restablish the connection
                     dvPmsConnectionManager.connectionFlag = false;
                     dvLogger.info("can't read First LS from pms " + ex12);
                     break;
                  }
               }
               readLine = readLine.trim();
               String spt[];
               spt = readLine.split(new String(new byte[] {2}));
               for (int g = 0; g < spt.length; g++)
               {
                  dvLogger.info("at 11 ");
                  readLine = spt[g].trim();
                  if ((readLine.contains("ASNG")))
                  {
                     // if we got error then resend same command to PMS and report error to our
                     // system
                     dvPmsConnectionManager.ackFlag = false;
                     dvLogger.info("-ive read " + readLine);
                  }
                  else
                  {
                     dvPmsConnectionManager.ackFlag = true;
                  }

                  if (readLine.startsWith("LS"))
                  {
                     dvPmsConnectionManager.writeFlag = false;
                     dvPmsOpera.isFirstLSReceived=true;
                     dvLogger.info("at LS ");
                     // dvPmsConnectionManager.clientSock.setSoTimeout(3000);
                     String lSequence[] = {
                           // "LD|" + dvPmsConnectionManager.getDateTime() + "V#1.01|IFVI|", //old
                           "LD|" + dvPmsConnectionManager.getDateTime() + "V#2.01|IFVI|",  // New
                           "LR|RIGI|FLRNG#GSGNGLGTA0A1A2A3A4A5A6A7A8A9GAGDGFTVVRGVSF|",  // New


                           // "LR|RIGI|FLRNG#GSGNGLSF|", // OLD
                           // "LR|RIGI|FLRNA0G#GSGNGLSF|",// changes for Armani
                           "LR|RIGO|FLRNG#GSSF|", "LR|RIGC|FLG#GSRNGNGLGTA0A1A2A3A4A5A6A7A8A9GAGDGFTVVRGVRO|", // New
                           // "LR|RIGC|FLG#GSRNGNGLRO|",// OLD

                           // "LR|RIGC|FLG#GSRNA0GNGLRO|",// changes for Armani
                           "LR|RIXL|FLG#MIMTRNDATI|", "LR|RIXM|FLRNG#|", "LR|RIXT|FLG#MIMTRNDATI|", "LR|RIXD|FLG#MIRNDATI|", "LR|RIXR|FLRNG#|",
                           "LR|RIXI|FLRNF#G#DCBIBDDATIFD|", "LR|RIXB|FLRNG#BADATI|", "LR|RIXC|FLRNG#BADATIASCT|",
                           // "LR|RIPS|FLRNTAX1DATIP#CTPT|",
                           "LR|RIPS|FLRNTAX1CTPTMAM#DATIP#PX|", "LR|RIPA|FLRNASP#DATI|", "LR|RIWR|FLRNDATI|", "LR|RIWC|FLRNDATI|", "LR|RIDR|FLDATI|",
                           "LR|RIDS|FLDATI|", "LR|RIDE|FLDATI|",
                           /*
                            * For Armani Change from
                            */
                           // "LR|RIRE|FLRNRS|", //Armani
                           "LR|RIRE|FLRNRS|", // New
                           /*
                            * For Armani to
                            */
                           "LA|" + dvPmsConnectionManager.getDateTime()};

                     for (int i = 0; i < lSequence.length; i++)
                     {
                        dvPmsConnectionManager.writeSignal(lSequence[i]);
                        Thread.sleep(500);
                        /*
                         * We should add this delay Recommend by opera
                         */
                     }
                     dvPmsConnectionManager.clientSock.setSoTimeout(dvPmsOpera.laTimeout);
                     dvPmsConnectionManager.writeFlag = false;
                     dvLogger.info("LD LR LA Sequence is been Sent now Waiting for LA Sequence from Pms ");
                  }
                  else if (readLine.startsWith("LA"))
                  {
                     dvPmsOpera.pmsAlertState=AlertState.Connected;
                     dvPmsConnectionManager.writeFlag = true;
                     dvPmsOpera.isLAReceived=true;
                     dvPmsOpera.isPollCommandSend=false;
                     dvPmsConnectionManager.clientSock.setSoTimeout(dvPmsOpera.socketTimeout);
                  }
                  else if (readLine.startsWith("LE"))
                  {
                	  dvPmsOpera.isLAReceived=false;
                	  dvPmsConnectionManager.writeFlag = true; 
                	  dvPmsOpera.LaCount=-1;
                	  dvPmsConnectionManager.clientSock.setSoTimeout(dvPmsOpera.laTimeout);
                	  dvPmsConnectionManager.writeSignal("LS|" + dvPmsConnectionManager.getDateTime());
                  }
                  else if (!readLine.equalsIgnoreCase(""))
                  {
                	  dvPmsOpera.pmsAlertState=AlertState.Connected;
                     dvPmsConnectionManager.clientSock.setSoTimeout(dvPmsOpera.socketTimeout);
                     DVParseData proDataObj = new DVParseData(readLine,dvPmsOpera,dvPmsDatabase);
                     Thread process = new Thread(proDataObj);
                     process.setPriority(Thread.MAX_PRIORITY);
                     process.start();
                  }
               }
            }
            catch (Exception e12)
            {
               try
               {
                  if (isObj != null && socket != null)
                  {
                     dvPmsConnectionManager.writeSignal("LE|" + dvPmsConnectionManager.getDateTime());
                     Thread.sleep(1000);
                  }

                  if (isObj != null)
                  {
                     isObj.close();
                  }
                  if (socket != null)
                  {
                     socket.close();
                  }
               }
               catch (Exception e)
               {
               }
               dvPmsConnectionManager.connectionFlag = false;
               dvLogger.error("Error in Read ", e12);
               break;
            }
         }

         try
         {
            if (isObj != null && socket != null)
            {
               dvPmsConnectionManager.writeSignal("LE|" + dvPmsConnectionManager.getDateTime());
               Thread.sleep(1000);
            }

            if (isObj != null)
            {
               isObj.close();
            }
            if (socket != null)
            {
               socket.close();
            }
         }
         catch (Exception e)
         {
         }
         dvPmsConnectionManager.connectionFlag = false;
         dvPmsOpera.pmsAlertState=AlertState.NotConnected;
      }
      catch (Exception e)
      {
         dvLogger.error("Error Reading data from PMS ", e);
      }
   }
   
   
}
