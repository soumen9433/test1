package com.digivalet.pmsi.api;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Swagger;

public class Bootstrap extends HttpServlet {
  @Override
  public void init(ServletConfig config) throws ServletException {
    Info info = new Info()
      .title("Swagger Server")
      .description("This is DigiValet PMS application services for fetching room status, bill details, guest information, setting up wake up call etc.")
      .termsOfService("https://digivalet.in/terms/")
      .contact(new Contact()
        .email("apisupport@digivalet.in"))
      .license(new License()
        .name("Digivalet License v1.0")
        .url("https://www.digivalet.com/licenses/LICENSE-1.0.html"));

    config.getServletContext();
    Swagger swagger = new Swagger().info(info);

    new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
  }
}
