package com.digivalet.pmsi.settings;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import com.digivalet.pmsi.database.DVDatabaseConnector;

/**
 * <B>Description:</B> This class load the settings.xml in DVParsesSetting which
 * contains name and path of XML resources as mentioned below :
 * <ul>
 * <li>tvconfig</li>
 * <li>lightconfig</li>
 * <li>acconfig</li>
 * </ul>
 * This will also contain port on which package will listen , metadata server IP
 * and port.</br>
 * <B>Note:</B> Do not add logger in this class as logger is not initialized
 */
public class DVSettings
{

   private static final String DV_CONFIG_DEFAULT_FILE =
            "/digivalet/pkg/config/pmsSettings.xml";
   private String dVConfigFile;
   private DVParsedSetting dvParsedSetting;
   private DVParsedDBSettings dvParsedDBSettings;
   private DVDBModelSettings dvdbModel;

   /**
    * <B>Description:</B> Constructor is used to load settings from path & file
    * provided to it by Main Thread. If Parameters passed to it are null or
    * blank , it loads settings from default path & default filename. Default
    * filename & file-path constants are defined inside class as <ui>
    * <li>{@link #DV_CONFIG_DEFAULT_FILE} = {@value #DV_CONFIG_DEFAULT_FILE}
    * </ui>
    * 
    * @param dVConfigFile
    * @throws JAXBException
    */
   public DVSettings() throws JAXBException
   {
      System.out.println("dvConfigFile is " + dVConfigFile
               + ". Loading from default path : " + DV_CONFIG_DEFAULT_FILE);
      this.dVConfigFile = DV_CONFIG_DEFAULT_FILE;
      init(this.dVConfigFile);
   }

   /**
    * Description: Function is used to initialize settings like IP, Port,
    * fileName & filePath from where we need to load configuration for TV,
    * Light, AC & other devices.
    * 
    * @param dVConfigFile
    * @throws JAXBException
    */
   public void init(String dVConfigFile) throws JAXBException
   {
      System.out.println("Loading settings from File : " + dVConfigFile);
      File file = new File(dVConfigFile);
      JAXBContext jaxbContext = JAXBContext.newInstance(DVParsedSetting.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      JAXBElement<DVParsedSetting> root = jaxbUnmarshaller
               .unmarshal(new StreamSource(file), DVParsedSetting.class);
      dvParsedSetting = root.getValue();
      System.out.println("Settings loaded as :" + dvParsedSetting.toString());
   }

   public void setDvDbSettings(DVDatabaseConnector dvDatabaseConnector)
   {
      // service initialise call
      dvParsedDBSettings = new DVParsedDBSettings(dvDatabaseConnector);
      dvdbModel = dvParsedDBSettings.getData();
   }

   public void getDvDbSettings()
   {
      // run time call through configuration refresh API request
      dvdbModel = dvParsedDBSettings.getData();
   }

   public String getLogfilepath()
   {
      return dvParsedSetting.getLogfilepath();
   }

   public boolean isEncryptedLogger()
   {
      return dvParsedSetting.isIsencryptedlogger();
   }

   public String getPmsVendor()
   {
      return dvdbModel.getPmsAdapter();
   }

   public String getDatabaseIp()
   {
      return dvParsedSetting.getDatabaseIp();
   }

   public String getDatabaseUserName()
   {
      return dvParsedSetting.getDatabaseUserName();
   }

   public String getDatabasePassword()
   {
      return dvParsedSetting.getDatabasePassword();
   }

   public String getDatabasePort()
   {
      return dvParsedSetting.getDatabasePort();
   }

   public String getMovieDatabasePort()
   {
      return dvParsedSetting.getMovieDatabasePort();
   }

   public String getDatabaseName()
   {
      return dvParsedSetting.getDatabaseName();
   }

   public String getPmsIp()
   {
      return dvdbModel.getPmsIp();
   }

   public int getPmsPort()
   {
      return dvdbModel.getPmsPort();
   }

   public int getConnectionDelay()
   {
      return dvdbModel.getConnectionDelay();
   }

   public String getHotelId()
   {
      return dvdbModel.getHotelId();
   }

   public int getSendDataToControllerPort()
   {
      return dvdbModel.getSendDataToControllerPort();
   }

   public int getControllerDeviceType()
   {
      return dvdbModel.getControllerDeviceType();
   }

   public int getIpadDeviceType()
   {
      return dvdbModel.getIpadDeviceType();
   }

   public String getDiscardDeviceTypes()
   {
      return dvdbModel.getDiscardDeviceTypes();
   }

   public String getKeyMappingData()
   {
      return dvdbModel.getKeyMappingData();
   }

   public String getDefaultFeildData()
   {
      return dvdbModel.getDefaultFeildData();
   }

   public String getValueMappingData()
   {
      return dvdbModel.getValueMappingData();
   }

   public int getDvcDataPort()
   {
      return dvdbModel.getDvcDataPort();
   }

   public int getXplayerDataPort()
   {
      return dvdbModel.getXplayerDataPort();
   }

   public String getGuestNameFormat()
   {
      return dvdbModel.getGuestNameFormat();
   }

   public String getGuestFullNameFormat()
   {
      return dvdbModel.getGuestFullNameFormat();
   }

   public String getCurrencySymbol()
   {
      return dvdbModel.getCurrencySymbol();
   }

   public String getTokenValidationUrl()
   {
      return dvdbModel.getTokenValidationUrl();
   }

   public String getOneAuthClientId()
   {
      return dvdbModel.getOneAuthClientId();
   }

   public String getOneAuthClientSecret()
   {
      return dvdbModel.getOneAuthClientSecret();
   }

   public String getOneAuthToken()
   {
      return dvdbModel.getOneAuthToken();
   }

   public boolean isOneAuthEnabled()
   {
      return dvdbModel.getOneAuthEnabled();
   }

   public String getServiceIdData()
   {
      return dvdbModel.getServiceIdData();
   }

   public boolean isCommunicationEncryption()
   {
      return dvdbModel.isCommunicationEncryption();
   }

   public String getSecretKeyUrl()
   {
      return dvdbModel.getSecretKeyUrl();
   }

   public String getDefaultWindowName()
   {
      return dvdbModel.getDefaultWindowName();
   }

   public String getFolioIdNameMapping()
   {
      return dvdbModel.getFolioIdNameMapping();
   }

   public String getMovieDatabaseIp()
   {
      return dvParsedSetting.getMovieDatabaseIp();
   }

   public String getMovieDatabaseUserName()
   {
      return dvParsedSetting.getMovieDatabaseUserName();
   }

   public String getMovieDatabasePassword()
   {
      return dvParsedSetting.getMovieDatabasePassword();
   }

   public String getMovieDatabaseName()
   {
      return dvParsedSetting.getMovieDatabaseName();
   }

   public float getSeekTimeForResume()
   {
      return dvdbModel.getSeekTimeForResume();
   }

   public String getButlerUrl()
   {
      return dvdbModel.getButlerUrl();
   }

   public String getPrinterMailerUrl()
   {
      return dvdbModel.getPrinterMailerUrl();
   }

   public String getAlertMailId()
   {
      return dvdbModel.getAlertMailId();
   }

   public String getDigivaletServiceUrl()
   {
      return dvdbModel.getDigivaletServiceUrl();
   }

   public String getMoviePlan()
   {
      return dvdbModel.getMoviePlan();
   }

   public String getPmsUrl()
   {
      return dvdbModel.getPmsUrl();
   }

   public String getMessageAlertUrl()
   {
      return dvdbModel.getMessageAlertUrl();
   }

   public Long getMonitorLogSeconds()
   {
      return dvdbModel.getMonitorLogSeconds();
   }

   public String getPmsClientSecret()
   {
      return dvdbModel.getPmsClientSecret();
   }

   public String getExpressCheckoutFailureMessage()
   {
      return dvdbModel.getExpressCheckoutFailureMessage();
   }

   public String getPmsClientToken()
   {
      return dvdbModel.getPmsClientToken();
   }

   public String getPmsAccessToken()
   {
      return dvdbModel.getPmsAccessToken();
   }

   public String getPmsWebSocketUrl()
   {
      return dvdbModel.getPmsWebSocketUrl();
   }

   public String getExpressCheckoutSuccessMessage()
   {
      return dvdbModel.getExpressCheckoutSuccessMessage();
   }

   public String getUserCode()
   {
      return dvdbModel.getUserCode();
   }

   public String getOauthServerUrl()
   {
      return dvdbModel.getOauthServerUrl();
   }

   public String getOauthServerClientId()
   {
      return dvdbModel.getOauthServerClientId();
   }

   public String getOauthServerClientSecret()
   {
      return dvdbModel.getOauthServerClientSecret();
   }

   public String getOauthServerScope()
   {
      return dvdbModel.getOauthServerScope();
   }

   public String getOauthServerGrantType()
   {
      return dvdbModel.getOauthServerGrantType();
   }

   public String getOauthServerTokenFetchTime()
   {
      return dvdbModel.getOauthServerTokenFetchTime();
   }

   public boolean isOauthEnabled()
   {
      return dvdbModel.isOauthEnabled();
   }

   public boolean isMovieId()
   {
      return dvdbModel.isMovieId();
   }

   public String getDashboardCheckinEventUrl()
   {
      return dvdbModel.getDashboardCheckinEventUrl();
   }

   public String getButlerAcceestokenType()
   {
      return dvdbModel.getButlerAcceestokenType();
   }

   public int getLsTimeout()
   {
      return dvdbModel.getLsTimeout();
   }

   public int getLaTimeout()
   {
      return dvdbModel.getLaTimeout();
   }

   public int getSocketTimeout()
   {
      return dvdbModel.getSocketTimeout();
   }

   public String getOfflineBillDateFormat()
   {
      return dvdbModel.getOfflineBillDateFormat();
   }

   public String getCheckInRequestURL()
   {
      return dvdbModel.getCheckInRequestURL();
   }

   public boolean isNotificationEnabled()
   {
      return dvdbModel.isNotificationEnabled();
   }

   public String getNotificationEngineBaseURL()
   {
      return dvdbModel.getNotificationEngineBaseURL();
   }

   public String getNotificationEngineActionEndPoint()
   {
      return dvdbModel.getNotificationEngineActionEndPoint();
   }

   public String getNotificationEngineRegistrationEndPoint()
   {
      return dvdbModel.getNotificationEngineRegistrationEndPoint();
   }


   public int getAuthExpiryTime()
   {
      return dvdbModel.getAuthExpiryTime();
   }
   
   public String getMailSendTo()
   {
      return dvdbModel.getMailSendTo();
   }
   
   public String getMailSubject()
   {
      return dvdbModel.getMailSubject();
   }   

   public String getMailBody()
   {
      return dvdbModel.getMailBody();
   }


   @Override
   public String toString()
   {
      return "DVSettings [getLogfilepath()=" + getLogfilepath()
               + ", isEncryptedLogger()=" + isEncryptedLogger()
               + ", getPmsVendor()=" + getPmsVendor() + ", getDatabaseIp()="
               + getDatabaseIp() + ", getDatabaseUserName()="
               + getDatabaseUserName() + ", getDatabasePassword()="
               + getDatabasePassword() + ", getDatabasePort()="
               + getDatabasePort() + ", getMovieDatabasePort()="
               + getMovieDatabasePort() + ", getDatabaseName()="
               + getDatabaseName() + ", getPmsIp()=" + getPmsIp()
               + ", getPmsPort()=" + getPmsPort() + ", getConnectionDelay()="
               + getConnectionDelay() + ", getHotelId()=" + getHotelId()
               + ", getSendDataToControllerPort()="
               + getSendDataToControllerPort() + ", getControllerDeviceType()="
               + getControllerDeviceType() + ", getIpadDeviceType()="
               + getIpadDeviceType() + ", getDiscardDeviceTypes()="
               + getDiscardDeviceTypes() + ", getKeyMappingData()="
               + getKeyMappingData() + ", getDefaultFeildData()="
               + getDefaultFeildData() + ", getValueMappingData()="
               + getValueMappingData() + ", getDvcDataPort()="
               + getDvcDataPort() + ", getXplayerDataPort()="
               + getXplayerDataPort() + ", getGuestNameFormat()="
               + getGuestNameFormat() + ", getGuestFullNameFormat()="
               + getGuestFullNameFormat() + ", getCurrencySymbol()="
               + getCurrencySymbol() + ", getTokenValidationUrl()="
               + getTokenValidationUrl() + ", getOneAuthClientId()="
               + getOneAuthClientId() + ", getOneAuthClientSecret()="
               + getOneAuthClientSecret() + ", getOneAuthToken()="
               + getOneAuthToken() + ", isOneAuthEnabled()="
               + isOneAuthEnabled() + ", getServiceIdData()="
               + getServiceIdData() + ", isCommunicationEncryption()="
               + isCommunicationEncryption() + ", getSecretKeyUrl()="
               + getSecretKeyUrl() + ", getDefaultWindowName()="
               + getDefaultWindowName() + ", getFolioIdNameMapping()="
               + getFolioIdNameMapping() + ", getMovieDatabaseIp()="
               + getMovieDatabaseIp() + ", getMovieDatabaseUserName()="
               + getMovieDatabaseUserName() + ", getMovieDatabasePassword()="
               + getMovieDatabasePassword() + ", getMovieDatabaseName()="
               + getMovieDatabaseName() + ", getSeekTimeForResume()="
               + getSeekTimeForResume() + ", getButlerUrl()=" + getButlerUrl()
               + ", getPrinterMailerUrl()=" + getPrinterMailerUrl()
               + ", getAlertMailId()=" + getAlertMailId()
               + ", getDigivaletServiceUrl()=" + getDigivaletServiceUrl()
               + ", getMoviePlan()=" + getMoviePlan() + ", getPmsUrl()="
               + getPmsUrl() + ", getMessageAlertUrl()=" + getMessageAlertUrl()
               + ", getMonitorLogSeconds()=" + getMonitorLogSeconds()
               + ", getPmsClientSecret()=" + getPmsClientSecret()
               + ", getExpressCheckoutFailureMessage()="
               + getExpressCheckoutFailureMessage() + ", getPmsClientToken()="
               + getPmsClientToken() + ", getPmsAccessToken()="
               + getPmsAccessToken() + ", getPmsWebSocketUrl()="
               + getPmsWebSocketUrl() + ", getExpressCheckoutSuccessMessage()="
               + getExpressCheckoutSuccessMessage() + ", getUserCode()="
               + getUserCode() + ", getOauthServerUrl()=" + getOauthServerUrl()
               + ", getOauthServerClientId()=" + getOauthServerClientId()
               + ", getOauthServerClientSecret()="
               + getOauthServerClientSecret() + ", getOauthServerScope()="
               + getOauthServerScope() + ", getOauthServerGrantType()="
               + getOauthServerGrantType() + ", getOauthServerTokenFetchTime()="
               + getOauthServerTokenFetchTime() + ", isOauthEnabled()="
               + isOauthEnabled() + ", isMovieId()=" + isMovieId()
               + ", getDashboardCheckinEventUrl()="
               + getDashboardCheckinEventUrl() + ", getButlerAcceestokenType()="
               + getButlerAcceestokenType() + ", getLsTimeout()="
               + getLsTimeout() + ", getLaTimeout()=" + getLaTimeout()
               + ", getSocketTimeout()=" + getSocketTimeout()
               + ", getOfflineBillDateFormat()=" + getOfflineBillDateFormat()
               + ", getCheckInRequestURL()=" + getCheckInRequestURL()
               + ", isNotificationEnabled()=" + isNotificationEnabled()
               + ", getNotificationEngineBaseURL()="
               + getNotificationEngineBaseURL()
               + ", getNotificationEngineActionEndPoint()="
               + getNotificationEngineActionEndPoint()
               + ", getNotificationEngineRegistrationEndPoint()="
               + getNotificationEngineRegistrationEndPoint() + "]";
   }

}
