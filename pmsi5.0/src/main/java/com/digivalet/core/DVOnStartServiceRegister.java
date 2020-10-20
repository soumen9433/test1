package com.digivalet.core;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import com.digivalet.pmsi.settings.DVSettings;

public class DVOnStartServiceRegister {
   private DVSettings dvSettings;
   private static DVLogger dvLogger = DVLogger.getInstance();
   private String oneAuthToken = null;
  private static final String serviceRegistrationDataFile =
           "/digivalet/pkg/config/serviceRegistrationRequest.json";
  private static final String serviceRegistrationRequestPathResource =
           "serviceRegistrationRequest.json";   
//  private static final String IRD_REQUEST_URL_PLACEHOLDER = "POSI_URL";
  private String serviceRegistrationEndPoint;
  
   public DVOnStartServiceRegister(DVSettings dvSettings) {
       this.dvSettings = dvSettings;       
   }

  public void registerService()
  {
     
     
     try
     {
        
        
        Runnable task = () -> {
           
           if(null != dvSettings.getNotificationEngineBaseURL()
                    && null != dvSettings.getNotificationEngineRegistrationEndPoint()
                    )
           {
              
              for(int i = 1;i<=3;i++)
              {
                 try
                 {
                    
                    serviceRegistrationEndPoint = dvSettings.getNotificationEngineBaseURL()
                             + dvSettings.getNotificationEngineRegistrationEndPoint();
                    
                    
                    dvLogger.info("Message::Register Service Initiated");
                    HttpClient client = new HttpClient();
                    
                    String registrationData = loadServiceRegistrationData();
                    
//                    registrationData = registrationData.replaceAll(
//                             IRD_REQUEST_URL_PLACEHOLDER, dvSettings.getIrdRequestURL());      

                    JSONObject jsonObject = new JSONObject(registrationData);
                    dvLogger.info("Request JSON :   ", jsonObject + "");
                    
                    if(dvSettings.isOneAuthEnabled())
                    {
                       oneAuthToken = DVPmsMain.getInstance().getDvTokenValidation().getAuthToken();
                    }
                    
                    String requestCall = client.callHttpPostClient(jsonObject.toString(),
                             serviceRegistrationEndPoint, oneAuthToken);
                    
                    dvLogger.info("Response received from notification Engine:"+requestCall);
                    JSONObject resultJson = new JSONObject(requestCall);
                    if (resultJson.getBoolean("status"))
                    {
                       dvLogger.info("Successfully Registered");
                       break;
                    }
                    else
                    {
                       dvLogger.info("Registration Failed");
                    }
                 }
                 catch (Exception e)
                 {
                    dvLogger.error("Exception while retrying API registration call. Exception:", e);
                 }
 
                 
                 try
                 {
                    Thread.sleep(5*60*1000);
                 }
                 catch (Exception e)
                 {
                    dvLogger.error("Exception while waiting in between registration API calls. Exception:", e);
                 }
                 
              }
              
           }
        };
        Thread t = new Thread(task);
        t.setName("SERVICE_REGISTRATION");
        t.start();
        

     }
     catch (Exception e)
     {
        dvLogger.error("Exception while posting registration data. Exception:", e);
     }
  }

  private String loadServiceRegistrationData()
  {    
     String requestData = "";
     try
     {
        dvLogger.info("Message::Loading configuration for service request registration",
                 "File::" + serviceRegistrationDataFile);

        
        if (isOverrideFileExists(serviceRegistrationDataFile))
        {
           dvLogger.warning("Implementation not provided for loading request json from file");
        }
        else
        {
           InputStream inputstream = null;
           inputstream = getStreamByResourcePath(serviceRegistrationRequestPathResource);
           dvLogger.info(
                    "Message:: Fetching input stream to load resource file",
                    "Inputstream::" + inputstream);
           
           if(null!=inputstream)
           {
              requestData = getDataFromInputStream(inputstream);
           }
           
           if(inputstream!= null)
           {
              inputstream.close();
              inputstream = null;
           }
           
        }
        dvLogger.info("Message::Configuration loaded for service registration",
                 "Config::" + requestData);

     }
     catch (Exception e)
     {
        dvLogger.error(
                 "Error while loading entertainment configuration for spotify. Exception : ",
                 e);
     }
     return requestData;
  }
  
  private boolean isOverrideFileExists(String filepath)
  {
     if (filepath != null)
     {
        File f = new File(filepath);
        if (f.exists() && !f.isDirectory())
        {
           return true;
        }
        else
        {
           return false;
        }
     }
     else
     {
        return false;
     }
  }
  
  private String getDataFromInputStream(InputStream inputStream)
  {
     String content = null;
     try
     {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        final StringBuilder out = new StringBuilder();
        int charsRead;
        while ((charsRead = in.read(buffer, 0, buffer.length)) > 0)
        {
           out.append(buffer, 0, charsRead);
        }
        if (out != null)
        {
           content = out.toString();
        }
     }
     catch (Exception e)
     {
        dvLogger.error(
                 "Exception while reading data from inputstream. Exception:",
                 e);
     }
     return content;
  }
  
  public InputStream getStreamByResourcePath(String fileName)            
  {
     return Thread.currentThread().getContextClassLoader()
              .getResourceAsStream(fileName);
  }
  
  
}
