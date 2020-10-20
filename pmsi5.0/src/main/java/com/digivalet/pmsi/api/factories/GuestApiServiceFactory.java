package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.GuestApiService;
import com.digivalet.pmsi.api.impl.GuestApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2020-01-21T09:25:29.038Z")
public class GuestApiServiceFactory {
    private final static GuestApiService service = new GuestApiServiceImpl();

    public static GuestApiService getGuestApi() {
        return service;
    }
}
