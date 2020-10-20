package com.digivalet.pmsi.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.digivalet.core.DVLogger;

public class DVCommonUtility
{
   private DVLogger dvLogger = DVLogger.getInstance();
   public DVCommonUtility()
   {
      
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
}
