package com.digivalet.pmsi.settings;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.database.DVDatabaseConnector;
import com.google.gson.Gson;

public class DVParsedDBSettings
{
   private DVDatabaseConnector dvDatabaseConnector;
   private static DVLogger dvLogger = DVLogger.getInstance();

   public DVParsedDBSettings(DVDatabaseConnector dvDatabaseConnector)
   {
      this.dvDatabaseConnector = dvDatabaseConnector;
   }

   
   private enum ConfigurationValueType
   {
      common, environment, environment_substring;
   }
   
   
   DVDBModelSettings getData()
   {
      DVDBModelSettings dvdbModel = new DVDBModelSettings();
      try
      {
         HashMap<String, String> javaConfigMap = new HashMap<String, String>();

         String query = "SELECT * FROM `java_configs` WHERE `module`='PMSI' AND `is_deleted` = '0'";

         dvLogger.info("QUERY: " + query);

         Statement statement = dvDatabaseConnector.getconnection().createStatement();

         ResultSet rs = statement.executeQuery(query);

         while (rs.next())
         {
            try
            {
               String key = rs.getString("config_key");

               String value = rs.getString("config_val");
               
               String valueType = rs.getString("val_type");
               System.out.println("key: "+key+" ,value "+value+" ,valueType "+valueType);
               dvLogger.info("key: "+key+" ,value "+value+" ,valueType "+valueType);
               if(valueType.equalsIgnoreCase(ConfigurationValueType.common.toString()))
               {                  
                //no replacement
                  try
                  {
                     javaConfigMap.put(key, value);
                  }
                  catch(Exception e)
                  {
                     dvLogger.info("Key: " + key);
                     dvLogger.info("value: " + value);
                     dvLogger.error("Exception: ", e);
                  }
               }
               else if(valueType.equalsIgnoreCase(ConfigurationValueType.environment.toString()))
               {
                  //complete replacement

                  try
                  {
                     value = getEnvironmentValue(value);
                     dvLogger.info("After updating:: " + key + "=" + value);
                     javaConfigMap.put(key, value);
                  }
                  catch (Exception e)
                  {
                     dvLogger.info("Key: " + key);
                     dvLogger.info("value: " + value);
                     dvLogger.error("Exception: ", e);
                  }
               }
               else if(valueType.equalsIgnoreCase(ConfigurationValueType.environment_substring.toString()))
               {
                //partial replacement based on some logic
                  
                  try
                  {
                     String envSubstring = value.substring(value.indexOf("[") + 1, value.indexOf("]")); 
                     String envValue = getEnvironmentValue(envSubstring.replace("\\[", "").replace("\\]", ""));
                     
                     value = value.replaceFirst(envSubstring, envValue);
                     value = value.replaceAll("\\[", "").replaceAll("\\]", "");
                     dvLogger.info("After updating:: " + key + "=" + value);
                     javaConfigMap.put(key, value);
                  }
                  catch (Exception e)
                  {
                     dvLogger.info("Key: " + key);
                     dvLogger.info("value: " + value);
                     dvLogger.error("Exception: ", e);
                  }
               }
            }
            catch (Exception e)
            {
               dvLogger.error("Error in setting parsed setting ", e);
            }
         }

         rs.close();
         statement.close();

         dvLogger.info("Setting Data: " + javaConfigMap);

         String dvString = new JSONObject(javaConfigMap).toString();
         

         dvdbModel = parseToObject(dvString, dvdbModel);        

         return dvdbModel;
      }
      catch (Exception e)
      {
         dvLogger.error("Error in get settings....", e);
         return dvdbModel;
      }
   }

   private String getEnvironmentValue(String variableName)
   {
      try
      {
         String value = System.getenv(variableName);
         if(null==value)
         {
            value="";
         }
         return value;
      }
      catch(Exception e)
      {
         dvLogger.error("Error while replacing to value from the env variable: " + variableName + "\n", e);
         return "";
      }
   }
   
   
   private DVDBModelSettings parseToObject(String dvString, DVDBModelSettings dvdbModel)
   {
      try
      {
         System.out.println(" ------------ "+ dvString);
         Gson gson = new Gson();

         dvdbModel = gson.fromJson(dvString, DVDBModelSettings.class);

         return dvdbModel;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println("Exception while parsing\n" + e);
         dvLogger.error("Exception while parsing db config data to Object\n", e);
         return dvdbModel;
      }
   }
}
