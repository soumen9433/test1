package com.digivalet.pmsi.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DVDBModelSettings {
	@SerializedName("pmsAdapter")
	@Expose
	private String pmsAdapter;

	@SerializedName("pmsIp")
	@Expose
	private String pmsIp;

	@SerializedName("pmsPort")
	@Expose
	private int pmsPort = 7007;

	@SerializedName("connectionDelay")
	@Expose
	private int connectionDelay = 10000;

	@SerializedName("hotelId")
	@Expose
	private String hotelId = "1";

	@SerializedName("sendDataToControllerPort")
	@Expose
	private int sendDataToControllerPort = 9229;

	@SerializedName("controllerDeviceType")
	@Expose
	private int controllerDeviceType = 2;

	@SerializedName("ipadDeviceType")
	@Expose
	private int ipadDeviceType = 5;

	@SerializedName("discardDeviceTypes")
	@Expose
	private String discardDeviceTypes;

	@SerializedName("keyMappingData")
	@Expose
	private String keyMappingData;

	@SerializedName("defaultFeildData")
	@Expose
	private String defaultFeildData;

	@SerializedName("valueMappingData")
	@Expose
	private String valueMappingData;

	@SerializedName("dvcDataPort")
	@Expose
	private int dvcDataPort = 9229;

	@SerializedName("xplayerDataPort")
	@Expose
	private int xplayerDataPort = 9229;

	@SerializedName("guestNameFormat")
	@Expose
	private String guestNameFormat;

	@SerializedName("guestFullNameFormat")
	@Expose
	private String guestFullNameFormat;

	@SerializedName("currencySymbol")
	@Expose
	private String currencySymbol;

	@SerializedName("serviceIdData")
	@Expose
	private String serviceIdData;

	@SerializedName("communicationEncryption")
	@Expose
	private boolean communicationEncryption = false;

	@SerializedName("secretKeyUrl")
	@Expose
	private String secretKeyUrl;

	@SerializedName("defaultWindowName")
	@Expose
	private String defaultWindowName;

	@SerializedName("folioIdNameMapping")
	@Expose
	private String folioIdNameMapping;

	@SerializedName("seekTimeForResume")
	@Expose
	private float seekTimeForResume = 10.0f;

	@SerializedName("butlerUrl")
	@Expose
	private String butlerUrl;

	@SerializedName("alertMailId")
	@Expose
	private String alertMailId;

	@SerializedName("digivaletServiceUrl")
	@Expose
	private String digivaletServiceUrl;
	@SerializedName("hotelCode")
	@Expose
	private String hotelCode;

	@SerializedName("userCode")
	@Expose
	private String userCode = "PmsiUser";

	@SerializedName("langCode")
	@Expose
	private String langCode = "en";

	@SerializedName("tokenValidationUrl")
	@Expose
	private String tokenValidationUrl;

	@SerializedName("oneAuthClientId")
	@Expose
	private String oneAuthClientId = "dvoa_50227b333421613d0ca2a1cbe2b02f44";

	@SerializedName("oneAuthClientSecret")
	@Expose
	private String oneAuthClientSecret = "a92d47d00d19040e5dcfa961a085b18277728b3e1aebe7f1414b30e7934ad9d9";

	@SerializedName("oneAuthToken")
	@Expose
	private String oneAuthToken = "f745e083c4e2bd469f81013008ac072f87830ec76a86d297a777a018c57f842b";

	@SerializedName("oneAuthEnabled")
	@Expose
	private Boolean oneAuthEnabled = true;

	@SerializedName("printerMailerUrl")
	@Expose
	private String printerMailerUrl = "NA";

	@SerializedName("moviePlan")
	@Expose
	private String moviePlan = "";
	@SerializedName("messageAlertUrl")
	@Expose
	private String messageAlertUrl = "";
	@SerializedName("monitorLogSeconds")
	@Expose
	private Long monitorLogSeconds = 3000L;

	@SerializedName("expressCheckoutSuccessMessage")
	@Expose
	private String expressCheckoutSuccessMessage = "Your request for Checkout has been submitted";

	@SerializedName("expressCheckoutFailureMessage")
	@Expose
	private String expressCheckoutFailureMessage = "Sorry we are unable process your request";

	@SerializedName("pmsUrl")
	@Expose
	private String pmsUrl;

	@SerializedName("pmsClientSecret")
	@Expose
	private String pmsClientSecret;

	@SerializedName("pmsClientToken")
	@Expose
	private String pmsClientToken;

	@SerializedName("pmsWebSocketUrl")
	@Expose
	private String pmsWebSocketUrl;

	@SerializedName("oauthServerUrl")
	@Expose
	private String oauthServerUrl = "";

	@SerializedName("oauthServerClientId")
	@Expose
	private String oauthServerClientId = "";

	@SerializedName("oauthServerClientSecret")
	@Expose
	private String oauthServerClientSecret = "";

	@SerializedName("oauthServerScope")
	@Expose
	private String oauthServerScope = "";

	@SerializedName("oauthServerGrantType")
	@Expose
	private String oauthServerGrantType = "";

	@SerializedName("oauthServerTokenFetchTime")
	@Expose
	private String oauthServerTokenFetchTime = "";

	@SerializedName("oauthEnabled")
	@Expose
	private boolean oauthEnabled = true;

	@SerializedName("movieId")
	@Expose
	private boolean movieId = false;

	@SerializedName("dashboardCheckinEventUrl")
	@Expose
	private String dashboardCheckinEventUrl = "na";

	@SerializedName("butlerAcceestokenType")
	@Expose
	private String butlerAcceestokenType = "access_token";

	@SerializedName("lsTimeout")
	@Expose
	private int lsTimeout = 10 * 1000;

	@SerializedName("laTimeout")
	@Expose
	private int laTimeout = 10 * 1000;

	@SerializedName("socketTimeout")
	@Expose
	private int socketTimeout = 10 * 60 * 1000;
	
   	@SerializedName("offlineBillDateFormat")
   	@Expose
   	private String offlineBillDateFormat = "dd/MM/yyyy";

	@SerializedName("serviceId")
   	@Expose
   	private String serviceId;

	@SerializedName("checkInRequestURL")
	@Expose
	private String checkInRequestURL;
	
   	@SerializedName("isNotificationEnabled")
   	@Expose
   	private boolean isNotificationEnabled = false;
   
   	@SerializedName("notificationEngineBaseURL")
   	@Expose
   	private String notificationEngineBaseURL;
   
   	@SerializedName("notificationEngineActionEndPoint")
   	@Expose
   	private String notificationEngineActionEndPoint;

   	@SerializedName("notificationEngineRegistrationEndPoint")
   	@Expose
   	private String notificationEngineRegistrationEndPoint;

   	@SerializedName("pmsAccessToken")
   	@Expose
   	private String pmsAccessToken;
   	
   	@SerializedName("authExpiryTime")
    @Expose
    private Integer authExpiryTime = 480000;
   	
    @SerializedName("mailSendTo")
    @Expose
    private String mailSendTo = "";
    
    @SerializedName("mailSubject")
    @Expose
    private String mailSubject = "Check-Out request from room no : %(roomnum)";
    
    @SerializedName("mailBody")
    @Expose
    private String mailBody = "";

	public String getPmsAdapter() {
		return pmsAdapter;
	}

	public void setPmsAdapter(String pmsAdapter) {
		this.pmsAdapter = pmsAdapter;
	}

	public String getPmsIp() {
		return pmsIp;
	}

	public void setPmsIp(String pmsIp) {
		this.pmsIp = pmsIp;
	}

	public int getPmsPort() {
		return pmsPort;
	}

	public void setPmsPort(int pmsPort) {
		this.pmsPort = pmsPort;
	}

	public int getConnectionDelay() {
		return connectionDelay;
	}

	public void setConnectionDelay(int connectionDelay) {
		this.connectionDelay = connectionDelay;
	}

	public String getHotelId() {
		return hotelId;
	}

	public void setHotelId(String hotelId) {
		this.hotelId = hotelId;
	}

	public int getSendDataToControllerPort() {
		return sendDataToControllerPort;
	}

	public void setSendDataToControllerPort(int sendDataToControllerPort) {
		this.sendDataToControllerPort = sendDataToControllerPort;
	}

	public int getControllerDeviceType() {
		return controllerDeviceType;
	}

	public void setControllerDeviceType(int controllerDeviceType) {
		this.controllerDeviceType = controllerDeviceType;
	}

	public int getIpadDeviceType() {
		return ipadDeviceType;
	}

	public void setIpadDeviceType(int ipadDeviceType) {
		this.ipadDeviceType = ipadDeviceType;
	}

	public String getDiscardDeviceTypes() {
		return discardDeviceTypes;
	}

	public void setDiscardDeviceTypes(String discardDeviceTypes) {
		this.discardDeviceTypes = discardDeviceTypes;
	}

	public String getKeyMappingData() {
		return keyMappingData;
	}

	public void setKeyMappingData(String keyMappingData) {
		this.keyMappingData = keyMappingData;
	}

	public String getDefaultFeildData() {
		return defaultFeildData;
	}

	public void setDefaultFeildData(String defaultFeildData) {
		this.defaultFeildData = defaultFeildData;
	}

	public String getValueMappingData() {
		return valueMappingData;
	}

	public void setValueMappingData(String valueMappingData) {
		this.valueMappingData = valueMappingData;
	}

	public int getDvcDataPort() {
		return dvcDataPort;
	}

	public void setDvcDataPort(int dvcDataPort) {
		this.dvcDataPort = dvcDataPort;
	}

	public int getXplayerDataPort() {
		return xplayerDataPort;
	}

	public void setXplayerDataPort(int xplayerDataPort) {
		this.xplayerDataPort = xplayerDataPort;
	}

	public String getGuestNameFormat() {
		return guestNameFormat;
	}

	public void setGuestNameFormat(String guestNameFormat) {
		this.guestNameFormat = guestNameFormat;
	}

	public String getGuestFullNameFormat() {
		return guestFullNameFormat;
	}

	public void setGuestFullNameFormat(String guestFullNameFormat) {
		this.guestFullNameFormat = guestFullNameFormat;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public String getServiceIdData() {
		return serviceIdData;
	}

	public void setServiceIdData(String serviceIdData) {
		this.serviceIdData = serviceIdData;
	}

	public boolean isCommunicationEncryption() {
		return communicationEncryption;
	}

	public void setCommunicationEncryption(boolean communicationEncryption) {
		this.communicationEncryption = communicationEncryption;
	}

	public String getSecretKeyUrl() {
		return secretKeyUrl;
	}

	public void setSecretKeyUrl(String secretKeyUrl) {
		this.secretKeyUrl = secretKeyUrl;
	}

	public String getDefaultWindowName() {
		return defaultWindowName;
	}

	public void setDefaultWindowName(String defaultWindowName) {
		this.defaultWindowName = defaultWindowName;
	}

	public String getFolioIdNameMapping() {
		return folioIdNameMapping;
	}

	public void setFolioIdNameMapping(String folioIdNameMapping) {
		this.folioIdNameMapping = folioIdNameMapping;
	}

	public float getSeekTimeForResume() {
		return seekTimeForResume;
	}

	public void setSeekTimeForResume(float seekTimeForResume) {
		this.seekTimeForResume = seekTimeForResume;
	}

	public String getButlerUrl() {
		return butlerUrl;
	}

	public void setButlerUrl(String butlerUrl) {
		this.butlerUrl = butlerUrl;
	}

	public String getAlertMailId() {
		return alertMailId;
	}

	public void setAlertMailId(String alertMailId) {
		this.alertMailId = alertMailId;
	}

	public String getDigivaletServiceUrl() {
		return digivaletServiceUrl;
	}

	public void setDigivaletServiceUrl(String digivaletServiceUrl) {
		this.digivaletServiceUrl = digivaletServiceUrl;
	}

	public String getHotelCode() {
		return hotelCode;
	}

	public void setHotelCode(String hotelCode) {
		this.hotelCode = hotelCode;
	}

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	public String getTokenValidationUrl() {
		return tokenValidationUrl;
	}

	public void setTokenValidationUrl(String tokenValidationUrl) {
		this.tokenValidationUrl = tokenValidationUrl;
	}

	public String getOneAuthClientId() {
		return oneAuthClientId;
	}

	public void setOneAuthClientId(String oneAuthClientId) {
		this.oneAuthClientId = oneAuthClientId;
	}

	public String getOneAuthClientSecret() {
		return oneAuthClientSecret;
	}

	public void setOneAuthClientSecret(String oneAuthClientSecret) {
		this.oneAuthClientSecret = oneAuthClientSecret;
	}

	public String getOneAuthToken() {
		return oneAuthToken;
	}

	public void setOneAuthToken(String oneAuthToken) {
		this.oneAuthToken = oneAuthToken;
	}

	public Boolean getOneAuthEnabled() {
		return oneAuthEnabled;
	}

	public void setOneAuthEnabled(Boolean oneAuthEnabled) {
		this.oneAuthEnabled = oneAuthEnabled;
	}

	public String getPrinterMailerUrl() {
		return printerMailerUrl;
	}

	public void setPrinterMailerUrl(String printerMailerUrl) {
		this.printerMailerUrl = printerMailerUrl;
	}

	public String getMoviePlan() {
		return moviePlan;
	}

	public void setMoviePlan(String moviePlan) {
		this.moviePlan = moviePlan;
	}

	public String getMessageAlertUrl() {
		return messageAlertUrl;
	}

	public void setMessageAlertUrl(String messageAlertUrl) {
		this.messageAlertUrl = messageAlertUrl;
	}

	public Long getMonitorLogSeconds() {
		return monitorLogSeconds;
	}

	public void setMonitorLogSeconds(Long monitorLogSeconds) {
		this.monitorLogSeconds = monitorLogSeconds;
	}

	public String getExpressCheckoutSuccessMessage() {
		return expressCheckoutSuccessMessage;
	}

	public void setExpressCheckoutSuccessMessage(String expressCheckoutSuccessMessage) {
		this.expressCheckoutSuccessMessage = expressCheckoutSuccessMessage;
	}

	public String getExpressCheckoutFailureMessage() {
		return expressCheckoutFailureMessage;
	}

	public void setExpressCheckoutFailureMessage(String expressCheckoutFailureMessage) {
		this.expressCheckoutFailureMessage = expressCheckoutFailureMessage;
	}

	public String getPmsUrl() {
		return pmsUrl;
	}

	public void setPmsUrl(String pmsUrl) {
		this.pmsUrl = pmsUrl;
	}

	public String getPmsClientSecret() {
		return pmsClientSecret;
	}

	public void setPmsClientSecret(String pmsClientSecret) {
		this.pmsClientSecret = pmsClientSecret;
	}

	public String getPmsClientToken() {
		return pmsClientToken;
	}

	public void setPmsClientToken(String pmsClientToken) {
		this.pmsClientToken = pmsClientToken;
	}

	public String getPmsWebSocketUrl() {
		return pmsWebSocketUrl;
	}

	public void setPmsWebSocketUrl(String pmsWebSocketUrl) {
		this.pmsWebSocketUrl = pmsWebSocketUrl;
	}

	public String getOauthServerUrl() {
		return oauthServerUrl;
	}

	public void setOauthServerUrl(String oauthServerUrl) {
		this.oauthServerUrl = oauthServerUrl;
	}

	public String getOauthServerClientId() {
		return oauthServerClientId;
	}

	public void setOauthServerClientId(String oauthServerClientId) {
		this.oauthServerClientId = oauthServerClientId;
	}

	public String getOauthServerClientSecret() {
		return oauthServerClientSecret;
	}

	public void setOauthServerClientSecret(String oauthServerClientSecret) {
		this.oauthServerClientSecret = oauthServerClientSecret;
	}

	public String getOauthServerScope() {
		return oauthServerScope;
	}

	public void setOauthServerScope(String oauthServerScope) {
		this.oauthServerScope = oauthServerScope;
	}

	public String getOauthServerGrantType() {
		return oauthServerGrantType;
	}

	public void setOauthServerGrantType(String oauthServerGrantType) {
		this.oauthServerGrantType = oauthServerGrantType;
	}

	public String getOauthServerTokenFetchTime() {
		return oauthServerTokenFetchTime;
	}

	public void setOauthServerTokenFetchTime(String oauthServerTokenFetchTime) {
		this.oauthServerTokenFetchTime = oauthServerTokenFetchTime;
	}

	public boolean isOauthEnabled() {
		return oauthEnabled;
	}

	public void setOauthEnabled(boolean oauthEnabled) {
		this.oauthEnabled = oauthEnabled;
	}

	public boolean isMovieId() {
		return movieId;
	}

	public void setMovieId(boolean movieId) {
		this.movieId = movieId;
	}

	public String getButlerAcceestokenType() {
		return butlerAcceestokenType;
	}

	public void setButlerAcceestokenType(String butlerAcceestokenType) {
		this.butlerAcceestokenType = butlerAcceestokenType;
	}

	public String getDashboardCheckinEventUrl() {
		return dashboardCheckinEventUrl;
	}

	public void setDashboardCheckinEventUrl(String dashboardCheckinEventUrl) {
		this.dashboardCheckinEventUrl = dashboardCheckinEventUrl;
	}

	public int getLsTimeout() {
		return lsTimeout;
	}

	public void setLsTimeout(int lsTimeout) {
		this.lsTimeout = lsTimeout;
	}

	public int getLaTimeout() {
		return laTimeout;
	}

	public void setLaTimeout(int laTimeout) {
		this.laTimeout = laTimeout;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

 public String getOfflineBillDateFormat()
   {
      return offlineBillDateFormat;
   }

   public void setOfflineBillDateFormat(String offlineBillDateFormat)
   {
      this.offlineBillDateFormat = offlineBillDateFormat;
   }

   public String getServiceId()
   {
      return serviceId;
   }

   public void setServiceId(String serviceId)
   {
      this.serviceId = serviceId;
   }
	
	public String getCheckInRequestURL() {
		return checkInRequestURL;
	}
	
	public void setCheckInRequestURL(String checkInRequestURL) {
		this.checkInRequestURL=checkInRequestURL;
	}

   public boolean isNotificationEnabled()
   {
      return isNotificationEnabled;
   }

   public void setNotificationEnabled(boolean isNotificationEnabled)
   {
      this.isNotificationEnabled = isNotificationEnabled;
   }
	
   public String getNotificationEngineBaseURL()
   {
      return notificationEngineBaseURL;
   }

   public void setNotificationEngineBaseURL(String notificationEngineBaseURL)
   {
      this.notificationEngineBaseURL = notificationEngineBaseURL;
   }

   public String getNotificationEngineActionEndPoint()
   {
      return notificationEngineActionEndPoint;
   }

   public void setNotificationEngineActionEndPoint(
            String notificationEngineActionEndPoint)
   {
      this.notificationEngineActionEndPoint = notificationEngineActionEndPoint;
   }

   public String getNotificationEngineRegistrationEndPoint()
   {
      return notificationEngineRegistrationEndPoint;
   }

   public void setNotificationEngineRegistrationEndPoint(
            String notificationEngineRegistrationEndPoint)
   {
      this.notificationEngineRegistrationEndPoint =
               notificationEngineRegistrationEndPoint;
   }

   public String getPmsAccessToken()
   {
      return pmsAccessToken;
   }

   public void setPmsAccessToken(String pmsAccessToken)
   {
      this.pmsAccessToken = pmsAccessToken;
   }
   
   public Integer getAuthExpiryTime()
   {
      return this.authExpiryTime;
   }

   public void setAuthExpiryTime(Integer authExpiryTime)
   {
      this.authExpiryTime = authExpiryTime;
   }
   
   public String getMailSendTo()
   {
      return mailSendTo;
   }
   
   public void setMailSendTo(String mailSendTo)
   {
      this.mailSendTo = mailSendTo;
   }

   public String getMailSubject()
   {
      return mailSubject;
   }
   
   public void setMailSubject(String mailSubject)
   {
      this.mailSubject = mailSubject;
   }

   public String getMailBody()
   {
      return mailBody;
   }
   
   public void setMailBody(String mailBody)
   {
      this.mailBody = mailBody;
   }
   
	@Override
   public String toString()
   {
      return "DVDBModelSettings [pmsAdapter=" + pmsAdapter + ", pmsIp=" + pmsIp
               + ", pmsPort=" + pmsPort + ", connectionDelay=" + connectionDelay
               + ", hotelId=" + hotelId + ", sendDataToControllerPort="
               + sendDataToControllerPort + ", controllerDeviceType="
               + controllerDeviceType + ", ipadDeviceType=" + ipadDeviceType
               + ", discardDeviceTypes=" + discardDeviceTypes
               + ", keyMappingData=" + keyMappingData + ", defaultFeildData="
               + defaultFeildData + ", valueMappingData=" + valueMappingData
               + ", dvcDataPort=" + dvcDataPort + ", xplayerDataPort="
               + xplayerDataPort + ", guestNameFormat=" + guestNameFormat
               + ", guestFullNameFormat=" + guestFullNameFormat
               + ", currencySymbol=" + currencySymbol + ", serviceIdData="
               + serviceIdData + ", communicationEncryption="
               + communicationEncryption + ", secretKeyUrl=" + secretKeyUrl
               + ", defaultWindowName=" + defaultWindowName
               + ", folioIdNameMapping=" + folioIdNameMapping
               + ", seekTimeForResume=" + seekTimeForResume + ", butlerUrl="
               + butlerUrl + ", alertMailId=" + alertMailId
               + ", digivaletServiceUrl=" + digivaletServiceUrl + ", hotelCode="
               + hotelCode + ", userCode=" + userCode + ", langCode=" + langCode
               + ", tokenValidationUrl=" + tokenValidationUrl
               + ", oneAuthClientId=" + oneAuthClientId
               + ", oneAuthClientSecret=" + oneAuthClientSecret
               + ", oneAuthToken=" + oneAuthToken + ", oneAuthEnabled="
               + oneAuthEnabled + ", printerMailerUrl=" + printerMailerUrl
               + ", moviePlan=" + moviePlan + ", messageAlertUrl="
               + messageAlertUrl + ", monitorLogSeconds=" + monitorLogSeconds
               + ", expressCheckoutSuccessMessage="
               + expressCheckoutSuccessMessage
               + ", expressCheckoutFailureMessage="
               + expressCheckoutFailureMessage + ", pmsUrl=" + pmsUrl
               + ", pmsClientSecret=" + pmsClientSecret + ", pmsClientToken="
               + pmsClientToken + ", pmsWebSocketUrl=" + pmsWebSocketUrl
               + ", oauthServerUrl=" + oauthServerUrl + ", oauthServerClientId="
               + oauthServerClientId + ", oauthServerClientSecret="
               + oauthServerClientSecret + ", oauthServerScope="
               + oauthServerScope + ", oauthServerGrantType="
               + oauthServerGrantType + ", oauthServerTokenFetchTime="
               + oauthServerTokenFetchTime + ", oauthEnabled=" + oauthEnabled
               + ", movieId=" + movieId + ", dashboardCheckinEventUrl="
               + dashboardCheckinEventUrl + ", butlerAcceestokenType="
               + butlerAcceestokenType + ", lsTimeout=" + lsTimeout
               + ", laTimeout=" + laTimeout + ", socketTimeout=" + socketTimeout
               + ", offlineBillDateFormat=" + offlineBillDateFormat
               + ", serviceId=" + serviceId + ", checkInRequestURL="
               + checkInRequestURL + ", isNotificationEnabled="
               + isNotificationEnabled + ", notificationEngineBaseURL="
               + notificationEngineBaseURL
               + ", notificationEngineActionEndPoint="
               + notificationEngineActionEndPoint
               + ", notificationEngineRegistrationEndPoint="
               + notificationEngineRegistrationEndPoint + ", pmsAccessToken="
               + pmsAccessToken + "]";
   }

}
