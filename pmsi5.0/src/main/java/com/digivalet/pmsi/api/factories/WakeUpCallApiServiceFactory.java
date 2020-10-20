package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.WakeUpCallApiService;
import com.digivalet.pmsi.api.impl.WakeUpCallApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public class WakeUpCallApiServiceFactory {
    private final static WakeUpCallApiService service = new WakeUpCallApiServiceImpl();

    public static WakeUpCallApiService getWakeUpCallApi() {
        return service;
    }
}
