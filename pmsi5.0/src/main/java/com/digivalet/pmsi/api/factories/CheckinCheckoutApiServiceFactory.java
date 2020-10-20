package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.CheckinCheckoutApiService;
import com.digivalet.pmsi.api.impl.CheckinCheckoutApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-06-06T16:23:07.286Z")
public class CheckinCheckoutApiServiceFactory {
    private final static CheckinCheckoutApiService service = new CheckinCheckoutApiServiceImpl();

    public static CheckinCheckoutApiService getCheckinCheckoutApi() {
        return service;
    }
}
