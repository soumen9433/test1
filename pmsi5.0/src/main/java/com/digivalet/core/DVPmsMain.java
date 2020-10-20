package com.digivalet.core;

import javax.xml.bind.JAXBException;
import com.digivalet.movieposting.DVMoviePostingManager;
import com.digivalet.movieposting.DVPendingMovies;
import com.digivalet.movieposting.DVPostPendingMovieToPms;
import com.digivalet.movieposting.DVUpdatePurchaseState;
import com.digivalet.pmsi.DVPmsController;
import com.digivalet.pmsi.DVTokenValidation;
import com.digivalet.pmsi.api.keys.DVKeyCommunicationTokenManager;
import com.digivalet.pmsi.database.DVDatabaseConnector;
import com.digivalet.pmsi.database.DVPmsDatabase;
import com.digivalet.pmsi.events.DVPMSEventHandler;
import com.digivalet.pmsi.events.DVPmsPendingEvents;
import com.digivalet.pmsi.exceptions.DVFileException;
import com.digivalet.pmsi.oauthclient.DVAuthGenerator;
import com.digivalet.pmsi.oauthclient.DVAuthenticator;
import com.digivalet.pmsi.oauthclient.OAuthTokenValidation;
import com.digivalet.pmsi.settings.DVSettings;
import com.digivalet.pmsialert.AlertMonitringService;

public class DVPmsMain
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private DVSettings dvSettings;
   public static DVPmsMain dvPmsMain;
   private DVPmsController dvPmsController;
   private DVTokenValidation dvTokenValidation;
   private DVPmsDatabase dvPmsDatabase;
   private DVKeyCommunicationTokenManager communicationTokenManager;
   private DVHealthMonitorThread dvHealthMonitorThread;
   private OAuthTokenValidation oAuthTokenValidation;

   public DVPmsController getDvPmsController()
   {
      return dvPmsController;
   }

   public DVHealthMonitorThread getDVHealthMonitorThread()
   {
      return this.dvHealthMonitorThread;
   }

   public DVTokenValidation getDvTokenValidation()
   {
      return dvTokenValidation;
   }

   public DVSettings getDVSettings()
   {
      return dvSettings;
   }

   public DVPmsMain() throws JAXBException, DVFileException
   {
      init();
   }

   public void init() throws JAXBException, DVFileException
   {

      dvSettings = new DVSettings();
      dvLogger.init(dvSettings);
      dvLogger.info("   Version Details:" + "\n   Version = " + Version.VERSION
               + "\n     GIT_COMMIT = " + Version.GIT_COMMIT
               + "\n     GIT_AUTHOR = " + Version.GIT_AUTHOR
               + "\n     GIT_BRANCH = " + Version.GIT_BRANCH
               + "\n     GIT_COMMITTER = " + Version.GIT_COMMITTER
               + "\n     GIT_AUTHOR_EMAIL = " + Version.GIT_AUTHOR_EMAIL
               + "\n     GIT_COMMITTER_EMAIL = " + Version.GIT_COMMITTER_EMAIL);

      System.out.println("Calling ");
      DVDatabaseConnector dvDatabaseConnector =
               new DVDatabaseConnector(dvSettings);
      dvDatabaseConnector.start();
      dvSettings.setDvDbSettings(dvDatabaseConnector);
      dvLogger.info("All Configuration Data Initialised :\n"
               + dvSettings.toString());

      dvPmsDatabase = new DVPmsDatabase(dvDatabaseConnector, dvSettings);
      try
      {
         dvTokenValidation = new DVTokenValidation();
         dvTokenValidation.init(dvSettings);
         dvTokenValidation.start();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while initialising the token object\n", e);
      }


      communicationTokenManager =
               new DVKeyCommunicationTokenManager(dvSettings, dvPmsDatabase);
      communicationTokenManager.start();

      DVUpdateNotifier dvpmsEventHandler = new DVPMSEventHandler(dvPmsDatabase,
               dvSettings, communicationTokenManager);
      DVPmsValidation dvPmsValidation =
               new DVPmsValidation(dvSettings, dvPmsDatabase);
      DVMoviePostingManager dvMoviePostingManager = new DVMoviePostingManager(
               dvPmsDatabase, dvSettings, communicationTokenManager);

      dvPmsController = new DVPmsController(dvSettings, dvpmsEventHandler,
               dvPmsDatabase, dvPmsValidation, dvMoviePostingManager);
      DVPmsPendingEvents dvPmsPendingEvents = new DVPmsPendingEvents(dvSettings,
               dvPmsDatabase, communicationTokenManager);
      dvPmsPendingEvents.start();

      DVPendingMovies dvPendingMovies = new DVPendingMovies(dvSettings,
               dvPmsDatabase, communicationTokenManager);
      dvPendingMovies.start();



      DVPostPendingMovieToPms dvPostPendingMovieToPms =
               new DVPostPendingMovieToPms(dvPmsDatabase, dvPmsController);
      dvPostPendingMovieToPms.start();


      AlertMonitringService alertMonitringService =
               new AlertMonitringService(dvSettings);
      alertMonitringService.start();

      try
      {
         {
            if (dvSettings.isOauthEnabled())
            {
               dvLogger.info("Initialising OAuth2.0");

               DVAuthenticator dvAuthenticator =
                        new DVAuthenticator(dvSettings);

               DVAuthGenerator dvAuthGenerator =
                        new DVAuthGenerator(dvAuthenticator);
               dvAuthGenerator.start();

               oAuthTokenValidation =
                        new OAuthTokenValidation(dvSettings, dvAuthenticator);
            }

         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while initialising OAuth2.0\n", e);
      }

      try
      {
         dvHealthMonitorThread =
                  new DVHealthMonitorThread(dvSettings, dvDatabaseConnector);
         dvHealthMonitorThread.start();
      }
      catch (Exception e)
      {
         dvLogger.error(
                  "Error in initialising DVHealthMonitorThread of DVpmsMain ",
                  e);
      }


      DVUpdatePurchaseState dvUpdatePurchaseState =
               new DVUpdatePurchaseState(dvSettings, dvPmsDatabase);
      dvUpdatePurchaseState.start();

      try
      {
         if (dvSettings.isNotificationEnabled())
         {
            DVOnStartServiceRegister dvOnStartServiceRegister =
                     new DVOnStartServiceRegister(dvSettings);
            dvOnStartServiceRegister.registerService();
         }
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while initialising Health-Monitor Thread\n",
                  e);
      }
      dvLogger.info("All classes initialized ");
   }

   public static DVPmsMain getInstance() throws JAXBException, DVFileException
   {
      return dvPmsMain;
   }

   public DVPmsDatabase getDvPmsDatabase()
   {
      return dvPmsDatabase;
   }

   public void setDvPmsDatabase(DVPmsDatabase dvPmsDatabase)
   {
      this.dvPmsDatabase = dvPmsDatabase;
   }

   public DVKeyCommunicationTokenManager getCommunicationTokenManager()
   {
      return communicationTokenManager;
   }

   public void setCommunicationTokenManager(
            DVKeyCommunicationTokenManager communicationTokenManager)
   {
      this.communicationTokenManager = communicationTokenManager;
   }

   public OAuthTokenValidation getOAuthInstance()
   {
      return oAuthTokenValidation;
   }

   public static void initialize()
   {
      if (dvPmsMain == null)
      {
         try
         {
            dvPmsMain = new DVPmsMain();
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
   }
}
