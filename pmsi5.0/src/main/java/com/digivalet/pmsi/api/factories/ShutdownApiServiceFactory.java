package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.ShutdownApiService;
import com.digivalet.pmsi.api.impl.ShutdownApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-12-14T23:29:50.546Z")
public class ShutdownApiServiceFactory {
    private final static ShutdownApiService service = new ShutdownApiServiceImpl();

    public static ShutdownApiService getShutdownApi() {
        return service;
    }
}
