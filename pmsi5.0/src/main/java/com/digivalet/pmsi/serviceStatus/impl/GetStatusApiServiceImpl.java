package com.digivalet.pmsi.serviceStatus.impl;


import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.apache.http.HttpStatus;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.api.NotFoundException;
import com.digivalet.pmsi.serviceStatus.api.GetStatusApiService;
import com.digivalet.pmsi.serviceStatus.model.GetStatusResponse;
import com.digivalet.pmsi.serviceStatus.model.Status;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-08-27T07:13:54.980Z")
public class GetStatusApiServiceImpl extends GetStatusApiService
{
   private DVLogger dvLogger = DVLogger.getInstance();

   @Override
   public Response getStatus(SecurityContext securityContext) throws NotFoundException
   {
      Thread.currentThread().setName("GET-SERVICE-STATUS");
      
      GetStatusResponse response = new GetStatusResponse();

      try
      {
         List<Status> statusList = DVPmsMain.getInstance().getDVHealthMonitorThread().getStatus();
         response.setStatus(statusList);
         
         dvLogger.info("Response to client: " + response);
         
         return Response.status(HttpStatus.SC_OK).entity(response).build();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while processing Get Service Status Request\n", e);
         
         return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(response).build();
      }
   }
}
