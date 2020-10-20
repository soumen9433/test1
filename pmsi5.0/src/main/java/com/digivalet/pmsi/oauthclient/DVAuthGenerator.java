package com.digivalet.pmsi.oauthclient;

import com.digivalet.core.DVLogger;

public class DVAuthGenerator extends Thread{
	
	private DVLogger dvLogger = DVLogger.getInstance();
	private DVAuthenticator dvAuthenticator;
	
	
	public DVAuthGenerator(DVAuthenticator dvAuthenticator)
	{
		this.dvAuthenticator= dvAuthenticator;
	}
	
	@Override
	public void run()
	{
	      try
	      {
	         while (true)
	         {
	            checkDVAccessTokenExpired();
	            Thread.sleep((long) 1000 * 120);
	         }
	      }
	      catch (Exception err)
	      {
	         dvLogger.info("Error in checking database connection " + err);
	      }
	   
	}
	
	 public void checkDVAccessTokenExpired()
	   {
	      dvLogger.info("In checkAccessTokenExpired");
	      try
	      {
	         if (dvAuthenticator.getAccessToken() == null)
	         {
	            dvLogger.info("accessToken is null, So genrating new access-token");
	            dvAuthenticator.generateDVAccessToken();
	         }
	         else
	         {
	            long currentTimeStamp = System.currentTimeMillis();
	            if (currentTimeStamp >= dvAuthenticator.getSomeMinAfterTokenGeneration().getTime())
	            {
	               dvLogger.info(
	                        "Current time is greater than 20 min after DV token genreation "
	                                 + currentTimeStamp
	                                 + "SomeMinAfterTokenGeneration : "
	                                 + dvAuthenticator.getSomeMinAfterTokenGeneration());
	               dvAuthenticator.generateDVAccessToken();
	            }
	         }

	      }
	      catch (Exception e)
	      {
	         dvLogger.info("Error in check AccessToken Expired " + e);
	      }
	   }
}