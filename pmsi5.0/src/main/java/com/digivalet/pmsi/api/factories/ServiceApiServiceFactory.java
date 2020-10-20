package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.ServiceApiService;
import com.digivalet.pmsi.api.impl.ServiceApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public class ServiceApiServiceFactory {
    private final static ServiceApiService service = new ServiceApiServiceImpl();

    public static ServiceApiService getServiceApi() {
        return service;
    }
}
