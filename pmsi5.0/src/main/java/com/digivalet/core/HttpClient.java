package com.digivalet.core;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient
{
   private static final String CONTENT_TYPE = "application/vnd.digivalet.v1+json; charset=UTF-8";
   private static final String LANGUAGE_HEADER = "LanguageCode";
   private static final String AUTHORIZATION = "Authorization";
   private static final String TOKEN_TYPE = "Bearer ";
   private static final Logger dvLogger =
            LoggerFactory.getLogger(HttpClient.class);
   private static final String CONTENT_TYPE_HEADER = "Content-Type";
   private static final String MEMBERSHIP_HEADER_KEY = "Membership";
   private PoolingHttpClientConnectionManager cm;
   
   
   
   
   
   
   
 
   
   public String callHttpPostClient(String json,
            String requestUrl,String accessToken)
   {
      
      CloseableHttpClient httpclient =null;

      try
      {
    	 httpclient = getHttpClient();
         HttpPost httpPost = new HttpPost(requestUrl);


         HttpEntity entity = new StringEntity(json);

         httpPost.setEntity(entity);   
         httpPost.setHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE);
         httpPost.setHeader("Accept", CONTENT_TYPE);
         httpPost.setHeader("Access-Token", accessToken);
         

         dvLogger.debug("Sending post request:: " + json);
         
         String res =EntityUtils.toString(httpclient.execute(httpPost).getEntity());
         dvLogger.info("Response : json ", res);

         return res;
      }
      catch (Exception e)
      {
    	  e.printStackTrace();
         dvLogger.error("Error in calling POST Ok Http method ", e);
         return null;
      }finally {
		try {
			httpclient.close();
		} catch (Exception e2) {
			dvLogger.error("Error At finally", e2);
		}
	}
   }
   
   
   private CloseableHttpClient getHttpClient()
   {
      CloseableHttpClient httpclient = null;

      try
      {
         SSLContextBuilder builder = new SSLContextBuilder();
         builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
         SSLConnectionSocketFactory sslConnectionSocketFactory =
                  new SSLConnectionSocketFactory(builder.build(),
                           NoopHostnameVerifier.INSTANCE);
         Registry<ConnectionSocketFactory> registry = RegistryBuilder
                  .<ConnectionSocketFactory>create()
                  .register("http", new PlainConnectionSocketFactory())
                  .register("https", sslConnectionSocketFactory).build();

         cm = new PoolingHttpClientConnectionManager(registry);
         cm.setMaxTotal(100);
         httpclient = HttpClients.custom()
                  .setSSLSocketFactory(sslConnectionSocketFactory)
                  .setConnectionManager(cm).build();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while creating CloseableHttpClient\n", e);
      }

      return httpclient;
   }
}

class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
   public static final String METHOD_NAME = "DELETE";

   public String getMethod() {
       return METHOD_NAME;
   }

   public HttpDeleteWithBody(final String uri) {
       super();
       setURI(URI.create(uri));
   }

   public HttpDeleteWithBody(final URI uri) {
       super();
       setURI(uri);
   }

   public HttpDeleteWithBody() {
       super();
   }
}
