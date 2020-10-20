package com.digivalet.pmsi;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import com.digivalet.core.DVLogger;

public class DVPmsUtility
{
   private DVLogger dvLogger = DVLogger.getInstance();
   
   public String getDateTimeFromTimeStamp(String timestamp)
   {
      try
      {
         Date date =
                  Date.from(Instant.ofEpochSecond(Long.parseLong(timestamp)));
         SimpleDateFormat simpleDateFormat =
                  new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
         return (simpleDateFormat.format(date));
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting timestamp ", e);
         return timestamp;
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

}
