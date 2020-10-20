package com.digivalet.core;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class DVAnalyticsLogAppender extends AppenderSkeleton
{
   static Logger dvLogger = Logger.getLogger("DVAnalyticsLogger");

   @Override
   public void close()
   {

   }

   @Override
   public boolean requiresLayout()
   {
      return false;
   }

   @Override
   protected void append(LoggingEvent loggedEvent)
   {
      Level level = loggedEvent.getLevel();
      String encodedMessage = "\""+encode(loggedEvent.getMessage().toString())+"\"";
      
      if (Level.INFO == level)
      {         
         dvLogger.info(encodedMessage);
      }
      else if (Level.WARN == level)
      {
         dvLogger.warn(encodedMessage);
      }
      else if (Level.ERROR == level)
      {
         dvLogger.error(encodedMessage);
      }
      else if (Level.DEBUG == level)
      {
         dvLogger.debug(encodedMessage);
      }
      else if (Level.TRACE == level)
      {
         dvLogger.trace(encodedMessage);
      }
      else if (DVAnalyticsLogger.ANALYTICS == level)
      {
         dvLogger.log(DVAnalyticsLogger.ANALYTICS, encodedMessage);
      }
      else
      {
         dvLogger.info(encodedMessage);
      }
   }
   
   public String encode(String message)
   {
      return java.util.Base64.getEncoder().encodeToString(message.getBytes());
   }

}