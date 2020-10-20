package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.CheckoutApiService;
import com.digivalet.pmsi.api.impl.CheckoutApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public class CheckoutApiServiceFactory {
    private final static CheckoutApiService service = new CheckoutApiServiceImpl();

    public static CheckoutApiService getCheckoutApi() {
        return service;
    }
}
