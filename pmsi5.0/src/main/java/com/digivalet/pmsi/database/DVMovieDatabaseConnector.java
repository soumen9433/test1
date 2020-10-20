package com.digivalet.pmsi.database;

import java.sql.Connection;
import java.sql.DriverManager;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.settings.DVParsedSetting.DVSETTINGSTAG;
import com.digivalet.pmsi.settings.DVSettings;

public class DVMovieDatabaseConnector extends Thread
{

   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   final int checkDbState=1000 * 60;
//   private boolean isDBFirstConnectionSuccess = false;
   private String DEFAULT_DB_PORT = "3306";

   public DVMovieDatabaseConnector(DVSettings dvSettings)
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
         dvLogger.error("Error in checking movie database connection ", err);
      }
   }

   public Connection getconnection()
   {
      try
      {
         checkconnection();
         return dbConnection;
      }
      catch (Exception err)
      {
         dvLogger.error("Error in connecting to movie database ", err);
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
            String username = System.getenv(dvSettings.getMovieDatabaseUserName());
            String password = System.getenv(dvSettings.getMovieDatabasePassword());
            String port = System.getenv(dvSettings.getMovieDatabasePort());            
            if (username == null || username.equalsIgnoreCase(""))
            {
               username = dvSettings.getMovieDatabaseUserName();
            }
            if (password == null || password.equalsIgnoreCase(""))
            {
               password = dvSettings.getMovieDatabasePassword();
            }
            if (port == null || port.equalsIgnoreCase(""))
            {
               port = dvSettings.getMovieDatabasePort();
               if(port.equalsIgnoreCase(DVSETTINGSTAG.DB_PORT.toString()))
               {
                  port = DEFAULT_DB_PORT;
               }
            }

            dvLogger.info("Trying to make connection with movie database......");
            dvLogger.info("username " + username + " password " + password
                     + " ip:" + dvSettings.getDatabaseIp());
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbConnection = DriverManager.getConnection(
                     "jdbc:mysql://" + dvSettings.getMovieDatabaseIp() + ":"+port+"/"
                              + dvSettings.getMovieDatabaseName()
                              + "?useUnicode=yes&characterEncoding=UTF-8&noAccessToProcedureBodies=true",
                     username, password);
            connectionflag = 1;
            dvLogger.info("Database Connection Successfully made with movie ");
         }
      }
      catch (Exception err)
      {
         dvLogger.error("Err ", err);
         try
         {
            dbConnection.close();
            dbConnection = null;
            connectionflag = 0;
         }
         catch (Exception ee)
         {
            dvLogger.error("Error in closing connection with movie database ", ee);
         }
      }
   }

}
