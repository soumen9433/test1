package com.digivalet.movies;

public enum MoviePlan
{
   free,
   perClick,
   perStay,
   oneDay,
   novalue;
   public static MoviePlan fromString(String Str)
   {
      try
      {
         return valueOf(Str);
      }
      catch (Exception ex)
      {
         return free;
      }
   }

}
