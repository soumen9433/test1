package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.GuestInformationApiService;
import com.digivalet.pmsi.api.impl.GuestInformationApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public class GuestInformationApiServiceFactory {
    private final static GuestInformationApiService service = new GuestInformationApiServiceImpl();

    public static GuestInformationApiService getGuestInformationApi() {
        return service;
    }
}
