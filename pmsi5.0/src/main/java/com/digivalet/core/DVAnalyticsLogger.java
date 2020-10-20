package com.digivalet.core;

import org.apache.log4j.Level;

public class DVAnalyticsLogger extends Level
{
   private static final long serialVersionUID = 1L;
   /**
    * Value of analytics level. This value is slightly higher than
    * {@link org.apache.log4j.Priority#FATAL_INT}.
    */
   public static final int ANALYTICS_LEVEL_INT = Level.FATAL_INT + 1;

   /**
    * {@link Level} representing my log level
    */
   public static final Level ANALYTICS =
            new DVAnalyticsLogger(ANALYTICS_LEVEL_INT, "ANALYTICS", 7);

   protected DVAnalyticsLogger(int level, String levelStr, int syslogEquivalent)
   {
      super(level, levelStr, syslogEquivalent);
   }



}
