package com.digivalet.pmsi.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.settings.DVParsedSetting.DVSETTINGSTAG;
import com.digivalet.pmsi.settings.DVSettings;

public class DVDatabaseConnector extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   final int checkDbState=1000 * 60;
   public long lastConnectedTimeStamp;
   private boolean isDBFirstConnectionSuccess = false;
   private String DEFAULT_DB_PORT = "3306";

   public DVDatabaseConnector(DVSettings dvSettings)
   {
      this.dvSettings = dvSettings;
   }

   public Connection dbConnection;
   public int connectionflag = 0;

   public void run()
   {
      try
      {
         while (true)
         {
            try
            {
               checkconnection();
            }
            catch (Exception er)
            {
               dvLogger.error("Error in sleep of checking db connection", er);
            }
            Thread.sleep(checkDbState);
         }
      }
      catch (Exception err)
      {
         dvLogger.error("Error in checking database connection ", err);
      }
   }

   public Connection getconnection()
   {
      try
      {
         checkconnection();
         if(null!=dbConnection)
         {
            lastConnectedTimeStamp=getDate();
         }
         return dbConnection;
      }
      catch (Exception err)
      {
         dvLogger.error("Error in connecting to database ", err);
      }
      return dbConnection;
   }

   public void checkconnection()
   {
      try
      {
         if ((dbConnection == null) || (dbConnection.isClosed())
                  || (connectionflag == 0))
         {
            String username = System.getenv(dvSettings.getDatabaseUserName());
            String password = System.getenv(dvSettings.getDatabasePassword());
            String port = System.getenv(dvSettings.getDatabasePort());
            if (username == null || username.equalsIgnoreCase(""))
            {
               username = dvSettings.getDatabaseUserName();
            }
            if (password == null || password.equalsIgnoreCase(""))
            {
               password = dvSettings.getDatabasePassword();
            }
            if (port == null || port.equalsIgnoreCase(""))
            {
               port = dvSettings.getDatabasePort();
               if(port.equalsIgnoreCase(DVSETTINGSTAG.DB_PORT.toString()))
               {
                  port = DEFAULT_DB_PORT;
               }
            }

            dvLogger.info("Trying to make connection with database......");
            dvLogger.info("username " + username + " password " + password
                     + " ip:" + dvSettings.getDatabaseIp());
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbConnection = DriverManager.getConnection(
                     "jdbc:mysql://" + dvSettings.getDatabaseIp() + ":"+port+"/"
                              + dvSettings.getDatabaseName()
                              + "?useUnicode=yes&characterEncoding=UTF-8&noAccessToProcedureBodies=true",
                     username, password);
            connectionflag = 1;
            dvLogger.info("Connection Successfully made");
            isDBFirstConnectionSuccess = true;
         }
      }
      catch (Exception err)
      {
         dvLogger.error("Err ", err);
         try
         {
            if(!isDBFirstConnectionSuccess)
            { 
               dvLogger.info("Exit from PMS interface, because DB not connected at first time");
               System.exit(0);
            } 
            dbConnection.close();
            dbConnection = null;
            connectionflag = 0;
         }
         catch (Exception ee)
         {
            dvLogger.error("Error in closing connection with database ", ee);
         }
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

   public long getLastConnectedTimeStamp()
   {
      return lastConnectedTimeStamp;
   }

   public void setLastConnectedTimeStamp(long lastConnectedTimeStamp)
   {
      this.lastConnectedTimeStamp = lastConnectedTimeStamp;
   }
}

