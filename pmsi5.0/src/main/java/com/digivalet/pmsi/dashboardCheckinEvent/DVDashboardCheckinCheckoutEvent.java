package com.digivalet.pmsi.dashboardCheckinEvent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.datatypes.DVPmsData;
import com.digivalet.pmsi.exceptions.DVFileException;

public class DVDashboardCheckinCheckoutEvent extends Thread
{
	DVLogger dvLogger = DVLogger.getInstance();
	private String dashboardCheckinUrl="";
	private String oneAuthToken;
	private final String CONTENT_TYPE = "application/vnd.digivalet.v1+json";
	private final int timeout = 20;
	private HttpClient client;
	private int resendCounter = 0;
	private Map<DVPmsData, Object> data = new HashMap<>();
	int hotelId=1;
	int keyId=0;
	String operation="";
	public DVDashboardCheckinCheckoutEvent(Map<DVPmsData, Object> data,String operation,int keyId) 
	{
		this.data=data;
		this.operation=operation;
		this.keyId=keyId;
		dvLogger.info("DVDashboardCheckinCheckoutEvent  ");
		
	}
	
	public void run() 
	{
		try 
		{
			dashboardCheckinUrl = DVPmsMain.getInstance().getDVSettings().getDashboardCheckinEventUrl();
			if(null!=dashboardCheckinUrl && !"".equalsIgnoreCase(dashboardCheckinUrl) && !"na".equalsIgnoreCase(dashboardCheckinUrl)) 
			{
				sanatize();
				hotelId=Integer.parseInt(DVPmsMain.getInstance().getDVSettings().getHotelId()) ;
//				sendToDashboradOther();
				sendToDashboard();
				
			}else 
			{
				dvLogger.info("dashboard Checkin Url is not defined ");
			}
		} catch (Exception e) {
			dvLogger.error("Error in sending Checkin Checkout event to dashboard service ", e);
		}
	}
	
	   private void disableSslVerification()
	   {
	      try
	      {
	         // Create a trust manager that does not validate certificate chains
	         TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers()
	            {
	               return null;
	            }

	            public void checkClientTrusted(X509Certificate[] certs, String authType)
	            {}

	            public void checkServerTrusted(X509Certificate[] certs, String authType)
	            {}
	         }};

	         // Install the all-trusting trust manager
	         SSLContext sc = SSLContext.getInstance("SSL");
	         sc.init(null, trustAllCerts, new java.security.SecureRandom());
	         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	         // Create all-trusting host name verifier
	         HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session)
	            {
	               return true;
	            }
	         };

	         // Install the all-trusting host verifier
	         HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	      }
	      catch (NoSuchAlgorithmException e)
	      {
	         dvLogger.error("No Such Algorithm Exception while disabling the certificate", e);
	      }
	      catch (KeyManagementException e)
	      {
	         dvLogger.error("Key Management Exception while disabling the certificate", e);
	      }
	   }

	   
	   private void sendToDashboradOther() 
	   {
		   try {
			   dvLogger.info("At get session ");
		         URL obj = new URL(dashboardCheckinUrl);
		         HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		         con.setRequestMethod("POST");
	        
		        
		         con.setRequestProperty("Content-Type", CONTENT_TYPE);
		         con.setRequestProperty("Access-Token", oneAuthToken);
		         
		         String body ="{\"hotel_id\":"+hotelId+",\"block_id\":\""+
				            "Microsoft"+"\",\"guest_id\":\""+
				            		"214214"+"\"}";
				            
		         
		         con.setDoOutput(true);

		         DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		         dvLogger.info("body: " + body);
		         wr.writeBytes(body);
		         wr.flush();
		         wr.close();

		         int responseCode = con.getResponseCode();
		         dvLogger.info("\nSending 'POST' request to URL : " + dashboardCheckinUrl);
		         dvLogger.info("Post parameters : " + body);
		         dvLogger.info("Response Code : " + responseCode);
		         dvLogger.info("Res message " + con.getResponseMessage() + " date " + con.getDate());


		} catch (Exception e) {
			// TODO: handle exception
		}
	   }
	
	   private void sendToDashboard() 
	   {
		      try
		      {
		    	  if(operation.equalsIgnoreCase("checkIn")) 
		    	  {
			            resendCounter++;
			            String body ="{\"hotel_id\":"+hotelId+",\"block_id\":\""+
			                     data.get(DVPmsData.groupCode)+"\",\"guest_id\":\""+
			            		data.get(DVPmsData.guestId)+"\"}";
			            
			            HttpPost post = new HttpPost(dashboardCheckinUrl);

			            post.setHeader("Content-Type", CONTENT_TYPE);
			            post.setHeader("Access-Token", oneAuthToken);

			            StringEntity requestEntity = new StringEntity(body);

			            post.setEntity(requestEntity);
			            dvLogger.info("Sending body:   "+body+" to url "+dashboardCheckinUrl);
			            disableSslVerification();
			            HttpResponse response = client.execute(post);
			            dvLogger.info("Response Code : "
			                     + response.getStatusLine().getStatusCode());

			            if (response.getStatusLine().getStatusCode() == 401
			                     && resendCounter < 2)
			            {
			               dvLogger.info(
			                        "Unauthorised Response from Dashboard, creating new token and retrying");
			               DVPmsMain.getInstance().getDvTokenValidation()
			                        .init(DVPmsMain.getInstance().getDVSettings());
			               sendToDashboard();
			            }

			            BufferedReader rd = new BufferedReader(
			                     new InputStreamReader(response.getEntity().getContent()));

			            StringBuffer result = new StringBuffer();
			            String line = "";
			            while ((line = rd.readLine()) != null)
			            {
			               result.append(line);
			            }

			            dvLogger.info("Data from dashboard Service: " + result);
			         		    		  
		    	  }
		    	  
		    	  



		      }
		      catch (UnsupportedEncodingException e)
		      {
		         dvLogger.error("Encoding exception in dashboard Request", e);
		      }
		      catch (ClientProtocolException e)
		      {
		         dvLogger.error("Client Protocol exception in dashboard Request", e);
		      }
		      catch (IOException e)
		      {
		         dvLogger.error("IO exception in dashboard Request", e);
		      }
		      catch (JAXBException e)
		      {
		         // TODO Auto-generated catch block
		         e.printStackTrace();
		      }
		      catch (DVFileException e)
		      {
		         // TODO Auto-generated catch block
		         e.printStackTrace();
		      }
		   
	   }

	public void sanatize()
	   {
	      try
	      {
	         System.setProperty("jsse.enableSNIExtension", "false");
	         oneAuthToken =
	                  DVPmsMain.getInstance().getDvTokenValidation().getAuthToken();

	         HttpClientContext context = HttpClientContext.create();
	         CookieStore cookieStore = new BasicCookieStore();
	         context.setCookieStore(cookieStore);

	         RequestConfig.Builder requestBuilder = RequestConfig.custom();
	         requestBuilder = requestBuilder.setConnectTimeout(timeout * 1000);
	         requestBuilder =
	                  requestBuilder.setConnectionRequestTimeout(timeout * 1000);

	         this.client = HttpClientBuilder.create()
	                  .setDefaultCookieStore(cookieStore)
	                  .setRedirectStrategy(new LaxRedirectStrategy())
	                  .setDefaultRequestConfig(requestBuilder.build()).build();

	         dvLogger.info("URL to send dashboard Checkin : " + dashboardCheckinUrl);
	      }
	      catch (Exception e)
	      {
	         dvLogger.error("Error in init for dashboard", e);
	      }
	   }


}

