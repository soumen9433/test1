package com.digivalet.pmsi.operaserver;

import java.io.InputStream;
import java.net.Socket;
import com.digivalet.core.AlertState;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.database.DVPmsDatabase;


public class DVReadDataFromPms implements Runnable {
  private DVLogger dvLogger = DVLogger.getInstance();

  private String readLine;
  private Socket socket;
  private DVPmsConnectionManager dvPmsConnectionManager;
  private DVPmsOpera dvPmsOpera;
  private DVPmsDatabase dvPmsDatabase;
  private InputStream isObj;
  private DVEncryptDecrypt dvEncryptDecrypt;

  public DVReadDataFromPms(Socket socket, DVPmsConnectionManager dvPmsConnectionManager,
      DVPmsOpera dvPmsOpera, DVPmsDatabase dvPmsDatabase, DVEncryptDecrypt dvEncryptDecrypt) {
    try {
      this.dvPmsConnectionManager = dvPmsConnectionManager;
      this.socket = socket;
      this.dvPmsOpera = dvPmsOpera;
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvEncryptDecrypt = dvEncryptDecrypt;
      isObj = dvPmsConnectionManager.isObj;
    } catch (Exception e) {
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
                        dvPmsConnectionManager.LEReqSent = true;
                        Thread.sleep(5000);
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
                  readLine = "";
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
                   /*  if (ex.toString().startsWith("java.net.SocketTimeoutException"))
                     {
                        if (dvPmsConnectionManager.clientSock.getSoTimeout() == 301000)
                        {
                           dvPmsConnectionManager.writeSignal("pong");
                           dvPmsConnectionManager.clientSock.setSoTimeout(11000);
                           readLine = "";
                           dataLength = isObj.read(data);
                           for (int i = 0; i < dataLength; i++)
                           {
                              readLine = readLine + Character.toString((char) data[i]);
                           }
                           dvLogger.info("Data from PMS is After Sending Pond is " + readLine);
                        }
                        else
                        {
                           
                            * Waiting for 3 sec otherwise initiate communication with LS
                            
                          dvPmsConnectionManager.writeSignal("pong");
                           dvPmsConnectionManager.clientSock.setSoTimeout(11000);
                           readLine = "";
                           dataLength = isObj.read(data);
                           for (int i = 0; i < dataLength; i++)
                           {
                              readLine = readLine + Character.toString((char) data[i]);
                           }
                           dvLogger.info("Data from PMS is After Sending pong is " + readLine);
                        }
                     }
                     else
                     {*/
                        if (isObj != null && socket != null)
                        {
                           dvLogger.info("Exception in read data from pms ");
                           dvPmsConnectionManager.LEReqSent = true;
                           Thread.sleep(5000);
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
                  catch (Exception ex12)
                  {
                     ex12.printStackTrace();
                     if (isObj != null && socket != null)
                     {
                        dvPmsConnectionManager.LEReqSent = true;
                        dvPmsConnectionManager.writeSignal("pong");
                        Thread.sleep(5000);
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
               dvPmsConnectionManager.clientSock.setSoTimeout(301000);
               // dvPmsConnectionManager.clientSock.setSoTimeout(0);
               readLine = readLine.trim();

               String spt[];
               spt = readLine.split(new String(new byte[] {2}));
               
               
               for (int g = 0; g < spt.length; g++)
               {
                  readLine = spt[g].trim();
                  try
                  {
                     readLine=readLine.substring(0, readLine.length());
                     if(dvPmsConnectionManager.isKeyExchanged)
                     {
                        readLine= dvEncryptDecrypt.decrypt(dvPmsConnectionManager.exchangedKey , readLine).trim();
                     }else
                     {
                        readLine= dvEncryptDecrypt.decrypt(dvPmsConnectionManager.defaultKey , readLine).trim();   
                     }
                    
                     dvLogger.info("Decrpyted Data: "+readLine);   
                  }
                  catch (Exception e)
                  {
                     dvLogger.error("Error in Decrpyting data", e);
                 
                  }

                  dvLogger.info("Readline: "+readLine+"  "+readLine.equalsIgnoreCase("PING"));
                  if (readLine.startsWith("LA"))
                  {
                     dvLogger.info(" Link alive recevied from Opera ");
                     dvPmsOpera.pmsAlertState=AlertState.Connected;
                     dvPmsConnectionManager.writeFlag = true;
                     String ackNumber = "";
                     try
                     {
                        ackNumber = readLine.split("AckDigiConnect")[1];
                        dvPmsOpera.connectionManager
                                 .writeSignal("AckDigiConnect" + ackNumber );
                     }
                     catch (Exception e)
                     {
                       dvLogger.error("Error in sending ack from PMS ", e);
                     }
                  }else if (readLine.equalsIgnoreCase("PING"))
                  {
                     dvPmsConnectionManager.writeSignal("pong");
                  }
                  else if (readLine.contains("key_exchange"))
                  {
                     String exchangedKey=dvPmsConnectionManager.generateKey();
                     exchangedKey=exchangedKey.toLowerCase();
                     dvPmsConnectionManager.writeSignal("key_exchange_"+exchangedKey);
                     dvPmsConnectionManager.exchangedKey=exchangedKey;
                     dvPmsConnectionManager.isKeyExchanged=true;
                  }
                  else if (readLine.startsWith("LS"))
                  {
                    dvPmsOpera.pmsAlertState=AlertState.LinkNotAlive;

                    String ackNumber = "";
                    try
                    {
                       ackNumber = readLine.split("AckDigiConnect")[1];
                       dvPmsOpera.connectionManager
                                .writeSignal("AckDigiConnect" + ackNumber );
                    }
                    catch (Exception e)
                    {
                      dvLogger.error("Error in sending ack from PMS ", e);
                    }
               
                  }
                  else if (readLine.startsWith("LE"))
                  {
                    dvPmsOpera.pmsAlertState=AlertState.LinkNotUp;
                    String ackNumber = "";
                    try
                    {
                       ackNumber = readLine.split("AckDigiConnect")[1];
                       dvPmsOpera.connectionManager
                                .writeSignal("AckDigiConnect" + ackNumber );
                    }
                    catch (Exception e)
                    {
                      dvLogger.error("Error in sending ack from PMS ", e);
                    }
                  }
                  else if (!readLine.equalsIgnoreCase(""))
                  {
                     dvPmsOpera.pmsAlertState=AlertState.Connected;
                     dvPmsConnectionManager.clientSock.setSoTimeout(301000);
                     DVParseData proDataObj = new DVParseData(readLine,dvPmsOpera,dvPmsDatabase,dvEncryptDecrypt);
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
                     dvPmsConnectionManager.LEReqSent = true;
                     Thread.sleep(5000);
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
         dvPmsOpera.pmsAlertState=AlertState.NotConnected;
      }catch(  Exception e)
  {
    dvLogger.error("Error Reading data from PMS ", e);
  }
}

}
