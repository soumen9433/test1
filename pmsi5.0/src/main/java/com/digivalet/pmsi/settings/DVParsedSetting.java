package com.digivalet.pmsi.settings;

import javax.xml.bind.annotation.XmlRootElement;
import com.digivalet.pmsi.annotations.DVSettingsAnnotations;

@XmlRootElement(name = DVSettingsAnnotations.DVPARSEDSETTINGCONFIG_ROOT)
public class DVParsedSetting
{
   private String logfilepath = "";
   private boolean isencryptedlogger = false;
   
   private String databaseIp = "mds";
   private String databaseUserName = "DB_PMSI_USER";
   private String databasePassword = "DB_PMSI_PASS";
   private String databaseName = "digivalet";
   private String movieDatabaseIp = "mds";
   private String movieDatabaseUserName = "DB_PMSI_USER";
   private String movieDatabasePassword = "DB_PMSI_PASS";
   private String movieDatabaseName = "mymovie";   
   private String databasePort = DVSETTINGSTAG.DB_PORT.toString();
   private String movieDatabasePort = DVSETTINGSTAG.DB_PORT.toString();

   public enum DVSETTINGSTAG 
   {
      DB_PORT;
   }   
   
   public String getMovieDatabaseIp()
   {
      return movieDatabaseIp;
   }

   public void setMovieDatabaseIp(String movieDatabaseIp)
   {
      this.movieDatabaseIp = movieDatabaseIp;
   }

   public String getMovieDatabaseUserName()
   {
      return movieDatabaseUserName;
   }

   public void setMovieDatabaseUserName(String movieDatabaseUserName)
   {
      this.movieDatabaseUserName = movieDatabaseUserName;
   }

   public String getMovieDatabasePassword()
   {
      return movieDatabasePassword;
   }

   public void setMovieDatabasePassword(String movieDatabasePassword)
   {
      this.movieDatabasePassword = movieDatabasePassword;
   }

   public String getMovieDatabaseName()
   {
      return movieDatabaseName;
   }

   public void setMovieDatabaseName(String movieDatabaseName)
   {
      this.movieDatabaseName = movieDatabaseName;
   }

   public String getLogfilepath()
   {
      return logfilepath;
   }

   public void setLogfilepath(String logfilepath)
   {
      this.logfilepath = logfilepath;
   }

   public boolean isIsencryptedlogger()
   {
      return isencryptedlogger;
   }

   public void setIsencryptedlogger(boolean isencryptedlogger)
   {
      this.isencryptedlogger = isencryptedlogger;
   }   

   public String getDatabaseIp()
   {
      return databaseIp;
   }

   public void setDatabaseIp(String databaseIp)
   {
      this.databaseIp = databaseIp;
   }

   public String getDatabaseUserName()
   {
      return databaseUserName;
   }

   public void setDatabaseUserName(String databaseUserName)
   {
      this.databaseUserName = databaseUserName;
   }

   public String getDatabasePassword()
   {
      return databasePassword;
   }

   public void setDatabasePassword(String databasePassword)
   {
      this.databasePassword = databasePassword;
   }

   public String getDatabaseName()
   {
      return databaseName;
   }

   public void setDatabaseName(String databaseName)
   {
      this.databaseName = databaseName;
   }
   
   public String getDatabasePort()
   {
      return databasePort;
   }

   public void setDatabasePort(String databasePort)
   {
      this.databasePort = databasePort;
   }
   
   public String getMovieDatabasePort()
   {
      return movieDatabasePort;
   }

   public void setMovieDatabasePort(String movieDatabasePort)
   {
      this.movieDatabasePort = movieDatabasePort;
   }

   @Override
   public String toString()
   {
      return "DVParsedSetting [getMovieDatabaseIp()=" + getMovieDatabaseIp() + ", getMovieDatabaseUserName()=" + getMovieDatabaseUserName()
               + ", getMovieDatabasePassword()=" + getMovieDatabasePassword() + ", getMovieDatabaseName()=" + getMovieDatabaseName()
               + ", getLogfilepath()=" + getLogfilepath() + ", isIsencryptedlogger()=" + isIsencryptedlogger() + ", getDatabaseIp()="
               + getDatabaseIp() + ", getDatabaseUserName()=" + getDatabaseUserName() + ", getDatabasePassword()=" + getDatabasePassword()
               + ", getDatabaseName()=" + getDatabaseName() 
               + ", getDatabasePort()=" + getDatabasePort()
               + ", getMovieDatabasePort()=" + getMovieDatabasePort()+"]";
   }
}
