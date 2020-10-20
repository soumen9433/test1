package com.digivalet.pmsi.serviceStatus.factories;

import com.digivalet.pmsi.serviceStatus.api.GetStatusApiService;
import com.digivalet.pmsi.serviceStatus.impl.GetStatusApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-08-27T07:13:54.980Z")
public class GetStatusApiServiceFactory {
    private final static GetStatusApiService service = new GetStatusApiServiceImpl();

    public static GetStatusApiService getGetStatusApi() {
        return service;
    }
}
