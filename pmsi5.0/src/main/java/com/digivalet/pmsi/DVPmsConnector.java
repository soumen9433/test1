package com.digivalet.pmsi;

import com.digivalet.core.AlertState;
import com.digivalet.movies.DVMovieEvent;
import com.digivalet.pmsi.model.GuestData;
import com.digivalet.pmsi.model.MovieDetails;
import com.digivalet.pmsi.result.DVResult;

public interface DVPmsConnector
{
   DVResult getBill(String roomNumber,String guestId);
   DVResult getMessage(String roomNumber,String guestId);
   DVResult synchronize();
   DVResult setServiceState(String roomNumber,String serviceId,boolean state);
   DVResult postWakeupCall(String roomNumber,String date, String time, boolean state);
   DVResult remoteCheckout(String roomNumber,String guestId,String targetDeviceId);
   GuestData getGuestInformation(String roomNumber);
   DVResult shutDownPms();
   DVResult postMovie(String roomNumber,String guestId,MovieDetails data, DVMovieEvent movieEvent);
   AlertState connectionStatus();
   String getErrorLog();
   DVResult postPendingMovie(int pendingId);
}
