package com.digivalet.core;

import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.result.DVResult;
import com.digivalet.pmsi.settings.DVSettings;

public class DVPmsValidation
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   private DVPmsDatabase dvPmsDatabase;

   public DVPmsValidation(DVSettings dvSettings, DVPmsDatabase dvPmsDatabase)
   {
      this.dvSettings = dvSettings;
      this.dvPmsDatabase = dvPmsDatabase;
   }

   public DVResult validateHotelCode(String hotelCode)
   {

      if (null == hotelCode || hotelCode.isEmpty()
               || hotelCode.equalsIgnoreCase(""))
      {
         return new DVResult(DVResult.DVERROR_HOTELCODE_NULL,
                  "Hotel Code is NULL");
      }
      else
      {
         DVResult validity = dvPmsDatabase.checkByHotelCode(hotelCode);
         return validity;
      }
   }

   public DVResult validateRoomNumber(String roomNumber)
   {
      if (null == roomNumber || roomNumber.isEmpty()
               || roomNumber.equalsIgnoreCase(""))
      {
         return new DVResult(DVResult.DVERROR_ROOMID_NULL,
                  "Room Number is NULL");
      }
      else
      {
         DVResult validity = dvPmsDatabase.checkByRoomNumber(roomNumber);
         return validity;
      }
   }

   public DVResult validateGuestId(String guestId)
   {
      if (null == guestId || guestId.isEmpty() || guestId.equalsIgnoreCase(""))
      {
         return new DVResult(DVResult.DVERROR_GUESTID_NULL, "Guest Id is NULL");
      }
      else
      {
         DVResult validity = dvPmsDatabase.checkByGuestId(guestId);
         return validity;
      }
   }

   public DVResult validateReservationNo(String reservationNumber)
   {
      if (null == reservationNumber || reservationNumber.isEmpty() || reservationNumber.equalsIgnoreCase(""))
      {
         return new DVResult(DVResult.DVERROR_INVALID_RESERVATION_NO, "Reservation Number is NULL");
      }
      else
      {
         return dvPmsDatabase.checkByReservationNo(reservationNumber);
      }
   }

   public DVResult validateMoodId(String moodId)
   {
      if (null == moodId || moodId.isEmpty() || moodId.equalsIgnoreCase(""))
      {
         return new DVResult(DVResult.DVERROR_INVALID_MOODID, "Mood Id is NULL");
      }
      else
      {
         return dvPmsDatabase.checkByMoodId(moodId);
      }
   }
}
