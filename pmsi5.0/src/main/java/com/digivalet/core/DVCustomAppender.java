package com.digivalet.core;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class DVCustomAppender extends AppenderSkeleton
{
   static Logger logger = Logger.getLogger("DVEncryptLogger");

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
   protected void append(LoggingEvent event)
   {
      DVCustomAppender.callMethod(event.getMessage().toString() + " " + event.getFQNOfLoggerClass());
   }

   private static void callMethod(String string)
   {
      logger.info(string);
   }

}
