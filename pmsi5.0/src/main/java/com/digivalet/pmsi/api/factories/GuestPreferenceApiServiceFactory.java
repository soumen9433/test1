package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.GuestPreferenceApiService;
import com.digivalet.pmsi.api.impl.GuestPreferenceApiServiceImpl;

@javax.annotation.Generated(
         value = "io.swagger.codegen.languages.JavaJerseyServerCodegen",
         date = "2018-12-21T11:32:48.219Z")
public class GuestPreferenceApiServiceFactory
{
   private final static GuestPreferenceApiService service =
            new GuestPreferenceApiServiceImpl();

   public static GuestPreferenceApiService getGuestPreferenceApi()
   {
      return service;
   }
}
