package com.digivalet.core;

import java.io.File;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.digivalet.pmsi.exceptions.DVFileException;
import com.digivalet.pmsi.exceptions.DVFileException.DVExceptionCodes;
import com.digivalet.pmsi.settings.DVSettings;
/**
 * <B>Description:</B> This class is used as the base class for all classes to log info, error, warning messages to log file.
 *
 */
public class DVLogger
{
   private static Logger logger = Logger.getLogger("DVDecryptLogger");
   private static Logger analyticsLogger = Logger.getLogger("DVAnalyticsLogger");
   private static String LOG_PROPERTIES_FILE;
   private static DVCustomAppender dvCustomAppender;
   private static DVLogger instance = null;
   private static boolean encryptedLogger = false;
//   private static DVCustomClassAppender dvCustomClassAppender;

   
   public DVLogger()
   {
      System.out.println("Intializing Logger");
      System.out.println("Intialized Logger");
   }

   public DVLogger(Logger log)
   {
      this.logger = log;
   }
   
   /**
    * <B>Description:</B> This function is used to initialize Logging properties. 
    * DVSettings instance is passed to it as a parameter , using which log file path is initialized & used to initialize properties.
    * DVSettings instance is also used to get value of EncryptLogger  & accordingly logs are initialized.    
    * @param dvSettings
    * @throws DVFileException
    */
   public void init(DVSettings dvSettings) throws DVFileException
   {
      LOG_PROPERTIES_FILE = dvSettings.getLogfilepath();
      encryptedLogger = dvSettings.isEncryptedLogger();
      System.out.println("INIT Log");
      if(null != LOG_PROPERTIES_FILE && !"".equalsIgnoreCase(LOG_PROPERTIES_FILE))
      {
         File logfile = new File(LOG_PROPERTIES_FILE);
         if (logfile.exists() && !logfile.isDirectory())
         {
            DOMConfigurator.configureAndWatch(LOG_PROPERTIES_FILE);
            if (encryptedLogger)
            {
               dvCustomAppender = new DVCustomAppender();
               logger.addAppender(dvCustomAppender);
            }
            else 
            {
//               dvCustomClassAppender = new DVCustomClassAppender();
//               log.addAppender(dvCustomClassAppender);
            }
         }
         else
         {
            throw new DVFileException(DVExceptionCodes.FILE_NOT_FOUND_EXCEPTION, "Log directory exists but file not exists at " + logfile);
         }         
      }
      else
      {
         throw new DVFileException(DVExceptionCodes.BLANK_OR_NULL_FILEPATH, "LOG property file value is : " + LOG_PROPERTIES_FILE );
      }

   }


   /**
    * Code to add appender here. dvCustomAppender = new DVCustomAppender();
    * log.addAppender(dvCustomAppender);
    * 
    * Code to load log4j.properties file than to log4j.xml file LOG_PROPERTIES_FILE =
    * "/digivalet/config/Log4J.properties"; Properties logProperties = new Properties();
    * logProperties.load(new FileInputStream(LOG_PROPERTIES_FILE));
    * PropertyConfigurator.configureAndWatch(LOG_PROPERTIES_FILE);
    */



   public static DVLogger getInstance()
   {
      if (instance == null)
      {
         instance = new DVLogger();
      }
      return instance;
   }

   public void info(String msg)
   {      
//      String[] classNameLineNumber = getClassNameLineNumber(msg);
//      log.info(classNameLineNumber[1] + ":" + classNameLineNumber[0] +"|"+ msg);
      logger.info("" + msg);
   }
   
   public void debug(String... msg)
   {      
//      String[] classNameLineNumber = getClassNameLineNumber(msg);
//      log.info(classNameLineNumber[1] + ":" + classNameLineNumber[0] +"|"+ msg);
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode debugMessageObject = objectMapper.createObjectNode();
      for (String string : msg)
      {
         String[] pairs = string.split("::");
         try
         {
            debugMessageObject.put(pairs[0], pairs[1]);
         }
         catch (Exception e)
         {
            logger.error("Error while debug logging ", e);
         }
      }
      logger.debug("" + debugMessageObject.toString());
   }
   
   public void trace(String... msg)
   {
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode debugMessageObject = objectMapper.createObjectNode();
      for (String string : msg)
      {
         if (string.split("::").length < 2)
         {
            string = string + " ";
         }
         String[] pairs = string.split("::");
         try
         {
            if (isValidJson(pairs[1]))
            {
               JsonNode jsonNode = objectMapper.readTree(pairs[1]);
               debugMessageObject.put(pairs[0], jsonNode);
            }
            else
            {
               debugMessageObject.put(pairs[0], pairs[1]);
            }
         }
         catch (Exception e)
         {
            logger.error("Exception while debug logging. Exception:", e);
         }
      }
      logger.trace("" + debugMessageObject.toString().replaceAll("\\\\", ""));
   }

   public void error(String msg, Exception e)
   {
      String[] classNameLineNumber = getClassNameLineNumber(msg);
      logger.error(classNameLineNumber[1] + ":" + classNameLineNumber[0] +"|"+ msg, e);
   }

   public void warning(String msg)
   {
      String[] classNameLineNumber = getClassNameLineNumber(msg);
      logger.warn(classNameLineNumber[1] + ":" + classNameLineNumber[0] +"|"+ msg);   
   }

   public void info(String myclass, String msg)
   {
      logger.info("-" + myclass + "| " + msg);
   }

   public void error(String myclass, String msg, Exception e)
   {
      logger.error("-" + myclass + "| " + msg, e);
   }

   public void warning(String myclass, String msg)
   {
      logger.warn("-" + myclass + "| " + msg);
   }


   public void analytics(String... msg)
   {            
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode debugMessageObject = objectMapper.createObjectNode();
      for (String string : msg)
      {
         if(string.split("::").length < 2 )
         {
            string = string + "";
         }
         String[] pairs = string.split("::");
         try
         {
            if(isValidJson(pairs[1]))
            {
               JsonNode jsonNode = objectMapper.readTree(pairs[1]);
               debugMessageObject.put(pairs[0], jsonNode);
            }
            else
            {
               debugMessageObject.put(pairs[0], pairs[1]);   
            }
         }
         catch (Exception e)
         {
            logger.error("Error while debug logging ", e);
         }
      }   
      analyticsLogger.log(DVAnalyticsLogger.ANALYTICS,
               "" + debugMessageObject.toString().replaceAll("\\\\", ""));
   }
   
   
   
   public boolean isValidJson(String data)
   {
      try
      {
         new JSONObject(data);
      }
      catch (JSONException ex)
      {
         try
         {
            new JSONArray(data);
         }
         catch (JSONException ex1)
         {
            return false;
         }
      }
      return true;
   }
   
   public String[] getClassNameLineNumber(String message)
   {
      StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();      
      int i = 1;
      while (DVLogger.class.getName().equals(stackTraceElements[i].getClassName()))
      {
          i++; 
      }
      int lineNumber = stackTraceElements[i].getLineNumber();
      String className = stackTraceElements[i].getClassName();
      String methodName = stackTraceElements[i].getMethodName();     
      String[] arr = new String[4];
      arr[0] = String.valueOf(lineNumber);
      arr[1] = className;
      arr[2] = methodName;
      return arr;
   }
  
   

}
