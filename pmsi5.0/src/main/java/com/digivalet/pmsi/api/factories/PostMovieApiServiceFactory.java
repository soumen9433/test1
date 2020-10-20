package com.digivalet.pmsi.api.factories;

import com.digivalet.pmsi.api.PostMovieApiService;
import com.digivalet.pmsi.api.impl.PostMovieApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-12-21T14:08:15.130Z")
public class PostMovieApiServiceFactory {
    private final static PostMovieApiService service = new PostMovieApiServiceImpl();

    public static PostMovieApiService getPostMovieApi() {
        return service;
    }
}