package com.digivalet.movieposting;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import com.digivalet.core.DVLogger;
import com.digivalet.movies.DVMovieEvent;
import com.digivalet.movies.MoviePlan;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.model.MovieDetails;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.settings.DVSettings;

public class DVMoviePostingManager
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVPmsDatabase dvPmsDatabase;
   private DVSettings dvSettings;
   private DVKeyCommunicationTokenManager communicationTokenManager;

   public DVMoviePostingManager(DVPmsDatabase dvPmsDatabase,
            DVSettings dvSettings,
            DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.dvPmsDatabase = dvPmsDatabase;
      this.dvSettings = dvSettings;
      this.communicationTokenManager = communicationTokenManager;
   }

   public DVResult postMovie(String roomNumber, String guestId,
            MovieDetails data, DVMovieEvent event)
   {
      try
      {
         int keyId = dvPmsDatabase.getKeyIdFromRoomNumber(roomNumber);
         MoviePlan moviePlan = MoviePlan.fromString(dvSettings.getMoviePlan());
         boolean isNeedToPurchase = false;
         boolean purchasedEvent = false;



         for (int i = 0; i < data.getDetails().size(); i++)
         {
            String movieName = data.getDetails().get(i).getMovieId();
            if (event == DVMovieEvent.stopped)
            {
               if (moviePlan == MoviePlan.free)
               {
                  isNeedToPurchase = false;
               }
               else if (moviePlan == MoviePlan.oneDay)
               {
                  if (checkIsNeedToPurchaseByPrice(movieName, keyId))
                  {
                     isNeedToPurchase =
                              checkMovieFor24HourPlan(keyId, movieName);
                  }
               }
               else if (moviePlan == MoviePlan.perClick)
               {
                  if (checkIsNeedToPurchaseByPrice(movieName, keyId))
                  {
                     isNeedToPurchase = true;
                  }
               }
               else if (moviePlan == MoviePlan.perStay)
               {
                  isNeedToPurchase = false;
               }
            }
            else if (event == DVMovieEvent.purchased)
            {
               isNeedToPurchase = false;
               purchasedEvent = true;
            }

            long startTime = data.getDetails().get(i).getStartTime();
            long endTime = data.getDetails().get(i).getEndTime();
            if (startTime == 0)
            {
               startTime = getDate();
            }
            if (endTime == 0)
            {
               endTime = getDate();
            }
            BigDecimal seekPercent = data.getDetails().get(i).getSeek();
            String audioId = data.getDetails().get(i).getAudioId();
            if (null == audioId || "na".equalsIgnoreCase(audioId))
            {
               audioId = "0";
            }
            String subtitleId = data.getDetails().get(i).getSubtitleId();
            if (null == subtitleId)
            {
               subtitleId = "0";
            }
            String duration = data.getDetails().get(i).getDuration();
            if (null == duration)
            {
               duration = "00:00:00";
            }
            String dimension = data.getDetails().get(i).getDimension();
            if (null == dimension)
            {
               dimension = "NA";
            }
            String alignment = data.getDetails().get(i).getAlignment();
            if (null == alignment)
            {
               alignment = "NA";
            }
            boolean isFinished = data.getDetails().get(i).getIsFinished();
            float seek = Float.parseFloat(seekPercent + "");
            boolean isNeedToResume = false;
            if (seek < dvSettings.getSeekTimeForResume())
            {
               isNeedToResume = false;
            }
            else
            {
               isNeedToResume = true;
            }
            if (isFinished)
            {
               isNeedToResume = false;
               seek = 0;
               duration = "00:00:00";
            }
            dvPmsDatabase.updateMovieKeyData(movieName, keyId, seek,
                     getDateTime(startTime), getDateTime(endTime), audioId,
                     subtitleId, duration, dimension, alignment, isNeedToResume,
                     isNeedToPurchase, purchasedEvent);
            if (event == DVMovieEvent.stopped)
            {
               dvPmsDatabase.insertMoviePlayedRecords(movieName, keyId);
            }

         }
         ArrayList<Integer> Dvcs = dvPmsDatabase.getAllDvcByKey(keyId);
         for (int Dvc : Dvcs)
         {
            Thread thread = new Thread(
                     new DVSendPendingMovieToDevices(Dvc, dvPmsDatabase,
                              dvSettings, null, communicationTokenManager));
            thread.start();
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in posting movie ", e);
      }
      return new DVResult(DVResult.DVINFO_REQUEST_COMPLETED_SUCCESSFULLY,
               "Service Data Send TO PMS ");
   }

   private boolean checkIsNeedToPurchaseByPrice(String movieName, int key_id)
   {
      try
      {
         float price = dvPmsDatabase.getMovieRate(movieName, key_id);
         if (price > 0.0f)
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Error in checking for price ", e);
      }
      return false;
   }

   private boolean checkMovieFor24HourPlan(int keyId, String movieName)
   {
      return dvPmsDatabase.checkIsNeedToPurchaseMovieAvailable(keyId,
               movieName);
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
      epoch = (epoch / 1000);
      return epoch;
   }

   public String getDateTime(String timestamp)
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

   public String getDateTime(long timestamp)
   {
      try
      {
         Date date = new Date(timestamp * 1000);
         SimpleDateFormat simpleDateFormat =
                  new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
         return (simpleDateFormat.format(date));
      }
      catch (Exception e)
      {
         dvLogger.error("Error in getting timestamp ", e);
         return "2020-06-30 00:00:00";
      }
   }

}
