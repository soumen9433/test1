package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.SynchronizeApiService;
import com.digivalet.pmsi.api.impl.SynchronizeApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-12-14T23:09:33.161Z")
public class SynchronizeApiServiceFactory {
    private final static SynchronizeApiService service = new SynchronizeApiServiceImpl();

    public static SynchronizeApiService getSynchronizeApi() {
        return service;
    }
}
