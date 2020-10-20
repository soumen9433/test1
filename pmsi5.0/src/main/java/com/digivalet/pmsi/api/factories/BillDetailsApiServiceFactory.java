package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.BillDetailsApiService;
import com.digivalet.pmsi.api.impl.BillDetailsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-11-03T08:52:10.265Z")
public class BillDetailsApiServiceFactory {
    private final static BillDetailsApiService service = new BillDetailsApiServiceImpl();

    public static BillDetailsApiService getBillDetailsApi() {
        return service;
    }
}
