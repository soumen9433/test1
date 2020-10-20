package com.digivalet.core;

import com.digivalet.pmsi.events.DVBillEvent;
import com.digivalet.pmsi.events.DVCheckoutFailEvent;
import com.digivalet.pmsi.events.DVEvent;
import com.digivalet.pmsi.events.DVMessageEvent;

public interface DVUpdateNotifier
{
   public void onEvent(DVEvent dvEvent);
   public void onEvent(DVBillEvent dvEvent);
   public void onEvent(DVMessageEvent dvEvent);
   public void onEvent(DVCheckoutFailEvent dvEvent);
}
