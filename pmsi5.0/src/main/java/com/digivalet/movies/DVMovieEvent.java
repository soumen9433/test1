package com.digivalet.movies;

public enum DVMovieEvent
{
   played,
   paused,
   stopped,
   purchased,
   novalue;
   public static DVMovieEvent fromString(String Str)
   {
      try
      {
         return valueOf(Str);
      }
      catch (Exception ex)
      {
         return novalue;
      }
   }

}
