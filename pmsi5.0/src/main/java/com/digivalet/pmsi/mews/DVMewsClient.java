package com.digivalet.pmsi.mews;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.bind.JAXBException;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.json.JSONArray;
import org.json.JSONObject;
import com.digivalet.core.DVLogger;
import com.digivalet.core.DVPmsMain;
import com.digivalet.pmsi.exceptions.DVFileException;
import com.digivalet.pmsi.mews.models.MewsCustomerData;
import com.digivalet.pmsi.mews.models.MewsMasterReservationData;
import com.digivalet.pmsi.settings.DVSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * 
 * @author lavin
 * 
 *         MEWS Integration API calls for PMS operations.
 * 
 * @operations: Get Bill, Get Guest Details, Get Reservations, Remote Checkout,
 *              Get Room-Spaces, Add Bill
 * 
 * @description: On Init: Get Spaces is called & mapping of Key and Room Nos is
 *               stored in a Map. Then PMS Sync thread is initialised so that
 *               all the existing reservations can -- -- be fetched and
 *               synchronized with digivalet system. Another thread is
 *               continuously executed for fetching the guest details: as Mews
 *               dont send any event for Guest-Info-Update
 */

public class DVMewsClient extends Thread
{
   private DVLogger dvLogger = DVLogger.getInstance();

   private final String GET_CUSTOMER = "/customers/getAll";
   private final String GET_BILL = "/bills/getAllByCustomers";
   private final String GET_SPACES = "/spaces/getAll";
   private final String GET_RESERVATION = "/reservations/getAllByCustomers";
   private final String REMOTE_CHECKOUT = "/reservations/process";
   private final String GET_RESERVATION_BY_ID = "/reservations/getAllByIds";
   private final String GET_ALL_RESERVATIONS = "/reservations/getAll";
   private final String ADD_ORDER_BILL = "/orders/add";
   // private static DVMewsClient dvMewsClient = null;

   private DVSettings dvSettings;
   private String url;
   private String clientToken;
   private String accessToken;
   private DVPmsMews dvPmsMews;
   private final long INIT_SLEEP = 1000L;

   public DVMewsClient(DVSettings dvSettings, DVPmsMews dvPmsMews)
   {
      this.dvSettings = dvSettings;
      this.dvPmsMews = dvPmsMews;
      init();
      
   }

   private void init()
   {
      this.url = dvSettings.getPmsUrl();
      clientToken = dvSettings.getPmsClientToken();
      accessToken = dvSettings.getPmsAccessToken();


   }

   @Override
   public void run()
   {
      try
      {
         Thread.sleep(INIT_SLEEP);

         getSpaceData();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while fetching space mapping\n", e);
      }
   }


   /*
    * static public void main(String... tE) { DVMewsClient client = new
    * DVMewsClient(); // System.out.println(client //
    * .getGuestDetailsById("39e792ff-2690-4b72-9d54-1386f78bdaac"));
    * 
    * // client.getSpaceData();
    * 
    * 
    * System.out.println(client
    * .getReservationDetails("39e792ff-2690-4b72-9d54-1386f78bdaac",
    * "2018-12-10 11:53:04", "2018-12-11 11:00:00",
    * "6aee5468-8d93-4279-a12b-54c0458475a7") .toString());
    * 
    * 
    * 
    * System.out.println(client.getReservationDetailsById(
    * "957bb318-60a5-461e-8a8c-659e3549bb72"));
    * 
    * 
    * System.out
    * .println(client.getBill("b8d360f1-a35c-4595-92f3-8b4be69896bc"));
    * 
    * // client.getSpaceData(); }
    */


   public JSONObject getGuestDetailsById(String id)
   {
      JSONObject responseObject = new JSONObject();

      try
      {
         HttpPost post = new HttpPost(url + GET_CUSTOMER);

         List<String> customerIdList = new ArrayList<>();
         customerIdList.add(id);

         ObjectMapper mapper = new ObjectMapper();
         String body = mapper
                  .writeValueAsString(createGetCustomerRequest(customerIdList));
         StringEntity requestEntity = new StringEntity(body);

         post.setEntity(requestEntity);
         HttpClient client=null ;
         client=initClient(client);
         HttpResponse response = client.execute(post);
         int code = response.getStatusLine().getStatusCode();

         if (code == 200)
         {
            dvLogger.info("Response Code : "
                     + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Response: " + result.toString());

            responseObject = new JSONObject(result.toString());
         }
         else
         {
            dvLogger.info("Exception while fetching guest details\n");
         }
         post.releaseConnection();

         return responseObject;
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while fetching guest details\n", e);
      }

      return responseObject;
   }


   /*
    * public Map<String, String> getReservationDetails(String guestId, String
    * arrivalDate, String departure, String spaceId) { Map<String, String>
    * reservationDetails = new HashMap<>();
    * 
    * try { HttpPost post = new HttpPost(url + GET_RESERVATION);
    * 
    * ObjectMapper mapper = new ObjectMapper(); String body =
    * mapper.writeValueAsString(createGetCustomerRequest(guestId)); StringEntity
    * requestEntity = new StringEntity(body);
    * 
    * post.setEntity(requestEntity);
    * 
    * HttpResponse response = client.execute(post);
    * dvLogger.info("Response Code : " +
    * response.getStatusLine().getStatusCode());
    * System.out.println("Response Code : " +
    * response.getStatusLine().getStatusCode());
    * 
    * BufferedReader rd = new BufferedReader( new
    * InputStreamReader(response.getEntity().getContent()));
    * 
    * StringBuilder result = new StringBuilder(); String line = ""; while ((line
    * = rd.readLine()) != null) { result.append(line); }
    * 
    * dvLogger.info("Response: " + result.toString());
    * System.out.println("Response: " + result.toString());
    * 
    * JSONObject responseJson = new JSONObject(result.toString());
    * 
    * if (null != responseJson &&
    * responseJson.has(MewsKeyTags.RESERVATIONS.toString())) { JSONArray
    * reservations = responseJson
    * .getJSONArray(MewsKeyTags.RESERVATIONS.toString());
    * 
    * if (null != reservations && reservations.length() > 0) { int i = 0;
    * 
    * while (i < reservations.length()) { JSONObject detailObject =
    * reservations.getJSONObject(i);
    * 
    * if (detailObject.has(MewsKeyTags.ASSIGNEDSPACEID.toString()) && null !=
    * detailObject.getString( MewsKeyTags.ASSIGNEDSPACEID.toString()) &&
    * detailObject .getString(MewsKeyTags.ASSIGNEDSPACEID .toString())
    * .equalsIgnoreCase(spaceId) &&
    * detailObject.has(MewsKeyTags.STARTUTC.toString()) && null != detailObject
    * .getString(MewsKeyTags.STARTUTC.toString()) && detailObject
    * .getString(MewsKeyTags.STARTUTC.toString()) .equalsIgnoreCase(arrivalDate)
    * && detailObject.has(MewsKeyTags.ENDUTC.toString()) && null != detailObject
    * .getString(MewsKeyTags.ENDUTC.toString()) && detailObject
    * .getString(MewsKeyTags.ENDUTC.toString()) .equalsIgnoreCase(departure)) {
    * if (detailObject .getString(MewsKeyTags.CUSTOMERID.toString())
    * .equalsIgnoreCase(guestId)) {
    * reservationDetails.put(GuestData.guestType.toString(),
    * GuestData.PRIMARY.toString()); }
    * 
    * if (detailObject.has(MewsKeyTags.COMPANIONIDS.toString()) && null !=
    * detailObject .get(MewsKeyTags.COMPANIONIDS.toString()) &&
    * detailObject.getJSONArray( MewsKeyTags.COMPANIONIDS.toString()) .length()
    * > 0) { // CompanionIds for (int s = 0; s < detailObject .getJSONArray(
    * MewsKeyTags.COMPANIONIDS.toString()) .length(); s++) { if (detailObject
    * .getJSONArray(MewsKeyTags.COMPANIONIDS .toString()) .getString(i) !=
    * guestId) { reservationDetails.put( GuestData.SECONDARY.toString(),
    * detailObject.getJSONArray( MewsKeyTags.COMPANIONIDS .toString())
    * .getString(i)); break; } } }
    * 
    * break; }
    * 
    * i++; } } } } catch (Exception e) {
    * dvLogger.error("Exception in get reservation details\n", e);
    * 
    * System.err.println("Exception in get reservation details\n" + e); }
    * 
    * return reservationDetails; }
    */

   /*
    * public Map<String, String> getReservationId(String guestId, String
    * arrivalDate, String departure, String spaceId) { Map<String, String>
    * reservationDetails = new HashMap<>();
    * 
    * try { HttpPost post = new HttpPost(url + GET_RESERVATION);
    * 
    * ObjectMapper mapper = new ObjectMapper(); String body =
    * mapper.writeValueAsString(createGetCustomerRequest(guestId)); StringEntity
    * requestEntity = new StringEntity(body);
    * 
    * post.setEntity(requestEntity);
    * 
    * HttpResponse response = client.execute(post);
    * dvLogger.info("Response Code : " +
    * response.getStatusLine().getStatusCode());
    * 
    * BufferedReader rd = new BufferedReader( new
    * InputStreamReader(response.getEntity().getContent()));
    * 
    * StringBuilder result = new StringBuilder(); String line = ""; while ((line
    * = rd.readLine()) != null) { result.append(line); }
    * 
    * dvLogger.info("Response: " + result.toString());
    * 
    * JSONObject responseJson = new JSONObject(result.toString());
    * 
    * if (null != responseJson && responseJson.has("Reservations")) { JSONArray
    * reservations = responseJson.getJSONArray("Reservations");
    * 
    * if (null != reservations && reservations.length() > 0) { int i = 0;
    * 
    * while (i < reservations.length()) { JSONObject detailObject =
    * reservations.getJSONObject(i);
    * 
    * if (detailObject.has(MewsKeyTags.ASSIGNEDSPACEID.toString()) && null !=
    * detailObject.getString( MewsKeyTags.ASSIGNEDSPACEID.toString()) &&
    * detailObject .getString(MewsKeyTags.ASSIGNEDSPACEID .toString())
    * .equalsIgnoreCase(spaceId) &&
    * detailObject.has(MewsKeyTags.STARTUTC.toString()) && null != detailObject
    * .getString(MewsKeyTags.STARTUTC.toString()) && detailObject
    * .getString(MewsKeyTags.STARTUTC.toString()) .equalsIgnoreCase(arrivalDate)
    * && detailObject.has(MewsKeyTags.ENDUTC.toString()) && null != detailObject
    * .getString(MewsKeyTags.ENDUTC.toString()) && detailObject
    * .getString(MewsKeyTags.ENDUTC.toString()) .equalsIgnoreCase(departure)) {
    * if (detailObject.has(MewsKeyTags.ID.toString()) && null != detailObject
    * .getString(MewsKeyTags.ID.toString())) { reservationDetails.put(
    * GuestData.ReservationId.toString(), detailObject.getString(
    * MewsKeyTags.ID.toString())); }
    * 
    * break; }
    * 
    * i++; } } } } catch (Exception e) {
    * dvLogger.error("Exception in get reservation details\n", e); }
    * 
    * return reservationDetails; }
    */

   public void getSpaceData()
   {
      JSONObject responseObject = new JSONObject();

      try
      {
         HttpPost post = new HttpPost(url + GET_SPACES);

         ObjectMapper mapper = new ObjectMapper();
         String body = mapper.writeValueAsString(createGetSpacesRequest());
         StringEntity requestEntity = new StringEntity(body);

         post.setEntity(requestEntity);
         HttpClient client=null ;
         client=initClient(client);
         HttpResponse response = client.execute(post);

         int code = response.getStatusLine().getStatusCode();

         if (code == 200)
         {
            dvLogger.info("Response Code : "
                     + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Response: " + result.toString());

            responseObject = new JSONObject(result.toString());

            parseSpaceToMap(responseObject);
         }
         else
         {
            dvLogger.info(
                     "Exception while fetching space details\n error code : "
                              + code);
         }
         post.releaseConnection();

      }
      catch (Exception e)
      {
         dvLogger.error("Exception while fetching space details\n", e);
      }
   }

   private void parseSpaceToMap(JSONObject responseObject)
   {

      try
      {
         JSONArray spaceArray =
                  responseObject.getJSONArray(MewsKeyTags.SPACES.toString());

         for (int i = 0; i < spaceArray.length(); i++)
         {
            JSONObject jsonObject = spaceArray.getJSONObject(i);

            String spaceId = jsonObject.getString(MewsKeyTags.ID.toString());
            String room = jsonObject.getString(MewsKeyTags.NUMBER.toString());

            dvPmsMews.spaceKeyMapping.put(spaceId, room);
            dvPmsMews.spaceKeyReverseMapping.put(room, spaceId);
         }

         dvLogger.info("SpaceKeyMap: " + dvPmsMews.spaceKeyMapping);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while parsing get space response to Map\n",
                  e);
      }
   }


   private DVMewsGetGuestDataReqest createGetCustomerRequest(
            List<String> customerIds)
   {
      DVMewsGetGuestDataReqest getCustomerReq = new DVMewsGetGuestDataReqest();

      try
      {
         getCustomerReq.setAccessToken(accessToken);
         getCustomerReq.setClientToken(clientToken);
         getCustomerReq.setCustomerIds(customerIds);

      }
      catch (Exception e)
      {
         dvLogger.error("Exception while creating the get customer request\n",
                  e);
      }
      return getCustomerReq;
   }

   private DVMewsGetSpacesRequest createGetSpacesRequest()
   {
      DVMewsGetSpacesRequest getSpaceReq = new DVMewsGetSpacesRequest();

      try
      {
         getSpaceReq.setAccessToken(accessToken);
         getSpaceReq.setClientToken(clientToken);

         Extent extent = new Extent();

         extent.setInactive(false);
         extent.setSpaceCategories(false);
         extent.setSpaces(true);
         extent.setSpaceFeatures(false);

         getSpaceReq.setExtent(extent);
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while creating the get customer request\n",
                  e);
      }

      return getSpaceReq;
   }


   public JSONObject getBill(String guestId)
   {
      JSONObject dvGuestBillDetails = new JSONObject();

      try
      {
         HttpPost post = new HttpPost(url + GET_BILL);

         ObjectMapper mapper = new ObjectMapper();

         List<String> customerIdList = new ArrayList<>();
         customerIdList.add(guestId);

         String body = mapper
                  .writeValueAsString(createGetCustomerRequest(customerIdList));
         StringEntity requestEntity = new StringEntity(body);

         post.setEntity(requestEntity);
         HttpClient client=null ;
         client=initClient(client);
         HttpResponse response = client.execute(post);

         int code = response.getStatusLine().getStatusCode();

         if (code == 200)
         {
            dvLogger.info("Response Code : "
                     + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Response: " + result.toString());

            dvGuestBillDetails = new JSONObject(result.toString());
         }
         else
         {
            dvLogger.info(
                     "Exception while fetching the bill details\n error code : "
                              + code);
         }
         post.releaseConnection();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while fetching the bill details\n", e);
      }

      return dvGuestBillDetails;
   }

   public boolean remoteCheckout(String reservationId, String roomNumber,
            String guestId)
   {
      JSONObject responseObject = new JSONObject();
      boolean checkout = false;

      try
      {
         dvLogger.info("CALL  :: " + url + REMOTE_CHECKOUT);
         HttpPost post = new HttpPost(url + REMOTE_CHECKOUT);

         ObjectMapper mapper = new ObjectMapper();
         String body = mapper
                  .writeValueAsString(createRemoteCheckout(reservationId));
         dvLogger.info("REQUEST BODY :: REMOTE CHECKOUT :::" + body);
         StringEntity requestEntity = new StringEntity(body);

         post.setEntity(requestEntity);
         HttpClient client=null ;
         client=initClient(client);
       
         HttpResponse response = client.execute(post);
         int code = response.getStatusLine().getStatusCode();
         dvLogger.info("Response Code : "
                  + response.getStatusLine().getStatusCode());
         if (code == 200)
         {

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Response: " + result.toString());
            if (result.toString().equals("{}"))
            {
               checkout = true;
            }

            dvLogger.info("CHECKOUT :: " + checkout);
            responseObject = new JSONObject(result.toString());

         }
         else
         {
            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Response: " + result.toString());

            responseObject = new JSONObject(result.toString());
            dvLogger.info("CheckOut Failed  :: "
                     + responseObject.getString("Message") + "   " + roomNumber
                     + "    " + guestId);
            dvPmsMews.notifyCheckoutFailEvent(
                     responseObject.getString("Message"), roomNumber, guestId);
         }
         post.releaseConnection();
      }
      catch (Exception e)
      {
         dvLogger.error("Error while executing remote checkout\n", e);
      }

      return checkout;
   }

   private DVMewsRemoteCheckoutRequest createRemoteCheckout(
            String reservationId)
   {
      DVMewsRemoteCheckoutRequest dvMewsRemoteCheckoutRequest =
               new DVMewsRemoteCheckoutRequest();

      try
      {
         dvMewsRemoteCheckoutRequest.setAccessToken(accessToken);
         dvMewsRemoteCheckoutRequest.setClientToken(clientToken);

         dvMewsRemoteCheckoutRequest.setCloseBills(false);
         dvMewsRemoteCheckoutRequest.setReservationId(reservationId);
         dvMewsRemoteCheckoutRequest.setAllowOpenBalance(false);
         dvMewsRemoteCheckoutRequest.setNotes("");

      }
      catch (Exception e)
      {
         dvLogger.error(
                  "Exception while creating the remote checkout request\n", e);
      }

      return dvMewsRemoteCheckoutRequest;
   }

   /*
    * public Map<String, String> getReservationDetailsById(String reservationId)
    * { Map<String, String> reservationDetails = new HashMap<>();
    * 
    * try { HttpPost post = new HttpPost(url + GET_RESERVATION_BY_ID);
    * 
    * ObjectMapper mapper = new ObjectMapper(); String body =
    * mapper.writeValueAsString(
    * createGetReservationByIdRequest(reservationId));
    * 
    * StringEntity requestEntity = new StringEntity(body);
    * 
    * post.setEntity(requestEntity);
    * 
    * HttpResponse response = client.execute(post);
    * dvLogger.info("Response Code : " +
    * response.getStatusLine().getStatusCode());
    * System.out.println("Response Code : " +
    * response.getStatusLine().getStatusCode());
    * 
    * BufferedReader rd = new BufferedReader( new
    * InputStreamReader(response.getEntity().getContent()));
    * 
    * StringBuilder result = new StringBuilder(); String line = ""; while ((line
    * = rd.readLine()) != null) { result.append(line); }
    * 
    * dvLogger.info("Response: " + result.toString());
    * System.out.println("Response: " + result.toString());
    * 
    * JSONObject responseJson = new JSONObject(result.toString());
    * 
    * if (null != responseJson &&
    * responseJson.has(MewsKeyTags.RESERVATIONS.toString())) { JSONArray
    * reservations = responseJson
    * .getJSONArray(MewsKeyTags.RESERVATIONS.toString());
    * 
    * if (null != reservations && reservations.length() > 0) { int i = 0;
    * 
    * while (i < reservations.length()) { JSONObject detailObject =
    * reservations.getJSONObject(i);
    * 
    * if (detailObject.has(MewsKeyTags.ASSIGNEDSPACEID.toString()) && null !=
    * detailObject.getString( MewsKeyTags.ASSIGNEDSPACEID.toString()) &&
    * detailObject.has(MewsKeyTags.STARTUTC.toString()) && null != detailObject
    * .getString(MewsKeyTags.STARTUTC.toString()) &&
    * detailObject.has(MewsKeyTags.ENDUTC.toString()) && null != detailObject
    * .getString(MewsKeyTags.ENDUTC.toString())) {
    * 
    * if (null != detailObject .getString(MewsKeyTags.CUSTOMERID.toString()) &&
    * !"".equalsIgnoreCase(detailObject.getString(
    * MewsKeyTags.CUSTOMERID.toString()))) {
    * reservationDetails.put(GuestData.guestType.toString(),
    * GuestData.PRIMARY.toString()); reservationDetails.put(
    * MewsKeyTags.CUSTOMERID.toString(), detailObject.getString(
    * MewsKeyTags.CUSTOMERID.toString())); } else {
    * reservationDetails.put(GuestData.guestType.toString(),
    * GuestData.SECONDARY.toString()); }
    * 
    * if (null != detailObject.getJSONArray(
    * MewsKeyTags.COMPANIONIDS.toString())) { List<String> secondaryGuestList =
    * new ArrayList<>();
    * 
    * for (int j = 0; j < detailObject .getJSONArray(
    * MewsKeyTags.COMPANIONIDS.toString()) .length(); j++) { if (!detailObject
    * .getJSONArray(MewsKeyTags.COMPANIONIDS .toString())
    * .getString(0).equalsIgnoreCase("") && !detailObject .getJSONArray(
    * MewsKeyTags.COMPANIONIDS .toString()) .getString(0)
    * .equals(reservationDetails.get( MewsKeyTags.CUSTOMERID .toString()))) {
    * secondaryGuestList.add(detailObject.getJSONArray(
    * MewsKeyTags.COMPANIONIDS.toString()) .getString(0)); } }
    * 
    * if (!secondaryGuestList.isEmpty()) { reservationDetails.put(
    * MewsKeyTags.COMPANIONIDS.toString(), secondaryGuestList.get(0)); }
    * 
    * }
    * 
    * break; }
    * 
    * i++; } } } } catch (Exception e) {
    * dvLogger.error("Exception in get reservation details\n", e);
    * 
    * System.err.println("Exception in get reservation details\n" + e); }
    * 
    * return reservationDetails; }
    */


   private DVMewsGetReservationRequest createGetReservationByIdRequest(
            String id)
   {
      DVMewsGetReservationRequest getCustomerReq =
               new DVMewsGetReservationRequest();

      try
      {
         getCustomerReq.setAccessToken(accessToken);
         getCustomerReq.setClientToken(clientToken);

         List<String> reservationIds = new ArrayList<>();
         reservationIds.add(id);

         getCustomerReq.setReservationIds(reservationIds);

      }
      catch (Exception e)
      {
         dvLogger.error("Exception while creating the get customer request\n",
                  e);
      }

      return getCustomerReq;
   }

   public synchronized MewsMasterReservationData getReservationDetails(String reservationId)
   {
      dvLogger.info(" reservationId  :: " + reservationId);
      MewsMasterReservationData reservationDetails =
               new MewsMasterReservationData();
      try
      {
         dvLogger.info("URL  ::::    " + url + GET_RESERVATION_BY_ID);
         HttpPost post = new HttpPost(url + GET_RESERVATION_BY_ID);

         ObjectMapper mapper = new ObjectMapper();
         String body = mapper.writeValueAsString(
                  createGetReservationByIdRequest(reservationId));

         StringEntity requestEntity = new StringEntity(body);

         post.setEntity(requestEntity);
         HttpClient client=null ;
         client=initClient(client);
         HttpResponse response = client.execute(post);


         int code = response.getStatusLine().getStatusCode();
         dvLogger.info("RESPONSE CODE : : " + code);



         if (code == 200)
         {

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Response: " + result.toString());

            JSONObject responseJson = new JSONObject(result.toString());

            if (null != responseJson
                     && responseJson.has(MewsKeyTags.RESERVATIONS.toString())
                     && responseJson.has(MewsKeyTags.CUSTOMERS.toString()))
            {
               Gson gson = new Gson();

               reservationDetails = gson.fromJson(responseJson.toString(),
                        MewsMasterReservationData.class);
            }
         }
         else
         {
            dvLogger.info("Exception in get reservation details\n error code : "
                     + code);
         }
         post.releaseConnection();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         dvLogger.error("Exception in get reservation details\n", e);
      }

      return reservationDetails;
   }

   /**
    * 
    * @param reservationId
    * @return
    * 
    * @description: this method stub will call the getAll Reservations to MEWS
    *               and will synchronize the DigiValet Checkin-checkout status
    *               of the keys.
    * 
    */
   MewsMasterReservationData sync()
   {

      dvLogger.info("MEWS   :::  SYNC call");
      MewsMasterReservationData reservationDetails =
               new MewsMasterReservationData();
      try
      {
         HttpPost post = new HttpPost(url + GET_ALL_RESERVATIONS);

         ObjectMapper mapper = new ObjectMapper();
         String body =
                  mapper.writeValueAsString(createGetReservationSyncRequest());

         StringEntity requestEntity = new StringEntity(body);

         post.setEntity(requestEntity);
         HttpClient client=null ;
         client=initClient(client);
         HttpResponse response = client.execute(post);
         int code = response.getStatusLine().getStatusCode();

         dvLogger.info("Response Code SYNC : " + code);
         if (code == 200)
         {


            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            dvLogger.info("Response SYNC : " + result.toString());

            JSONObject responseJson = new JSONObject(result.toString());

            if (null != responseJson
                     && responseJson.has(MewsKeyTags.RESERVATIONS.toString())
                     && responseJson.has(MewsKeyTags.CUSTOMERS.toString()))
            {
               Gson gson = new Gson();

               reservationDetails = gson.fromJson(responseJson.toString(),
                        MewsMasterReservationData.class);
            }
         }
         else
         {
            dvLogger.info("Exception in get reservation details\n error code : "
                     + code);
         }
         post.releaseConnection();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception in get reservation details\n", e);
      }

      return reservationDetails;
   }

   private DVGetAllReservationRequest createGetReservationSyncRequest()
   {
      DVGetAllReservationRequest getReservationReq =
               new DVGetAllReservationRequest();

      try
      {
         getReservationReq.setAccessToken(accessToken);
         getReservationReq.setClientToken(clientToken);

         DVReservationExtent extent = new DVReservationExtent();
         extent.setCustomers(true);
         extent.setReservations(true);
         extent.setReservationGroups(false);

         getReservationReq.setExtent(extent);

         SimpleDateFormat format =
                  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
         format.setTimeZone(TimeZone.getTimeZone("UTC"));

         String startUtc = format.format(new Date());
         String endUtc = format.format(new Date());

         getReservationReq.setStartUtc(startUtc);
         getReservationReq.setEndUtc(endUtc);

      }
      catch (Exception e)
      {
         dvLogger.error("Exception while creating the get customer request\n",
                  e);
      }

      return getReservationReq;
   }


   /**
    * 
    * @param list of guestId;
    * @return List<MewsCustomerData>
    * 
    * @Description: Fetch the Customer's data by list of guest ids. This stub is
    *               used to get updated info of -- -- the existing customer's
    */

   public List<MewsCustomerData> getGuestDetailsById(List<String> guestIds)
   {
      List<MewsCustomerData> customerDataList = new ArrayList<>();

      try
      {
         HttpPost post = new HttpPost(url + GET_CUSTOMER);

         ObjectMapper mapper = new ObjectMapper();
         String body =
                  mapper.writeValueAsString(createGetCustomerRequest(guestIds));
         StringEntity requestEntity = new StringEntity(body);

         post.setEntity(requestEntity);
         HttpClient client=null ;
         client=initClient(client);
         HttpResponse response = client.execute(post);
         int code = response.getStatusLine().getStatusCode();

         if (code == 200)
         {
            dvLogger.info("Response Code : "
                     + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }

            rd.close();

            dvLogger.info("Response: " + result.toString());

            Gson gson = new Gson();
            MewsMasterReservationData data = gson.fromJson(result.toString(),
                     MewsMasterReservationData.class);

            customerDataList = data.getCustomers();
         }
         else
         {

            dvLogger.info(
                     "Exception while fetching guest details\n error code : "
                              + code);
         }
         post.releaseConnection();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while fetching guest details\n", e);
      }

      return customerDataList;
   }


   /**
    * To Add the bill for any order or purchase like: Movie, etc.
    * 
    * Reference:
    * https://mews-systems.gitbook.io/connector-api/operations/services#add-order
    */
   public void addNewBill()
   {
      // TODO: Impl the bill posting.
   }

   public JSONObject addNewBill(JSONObject jObj)
   {
      JSONObject responseObject = new JSONObject();
      try
      {
         HttpPost post = new HttpPost(url + ADD_ORDER_BILL);

         dvLogger.info("Body : " + jObj);
         StringEntity requestEntity = new StringEntity(jObj.toString());

         post.setEntity(requestEntity);
         HttpClient client=null ;
         client=initClient(client);
         HttpResponse response = client.execute(post);
         int code = response.getStatusLine().getStatusCode();

         if (code == 200)
         {
            dvLogger.info("Response Code : "
                     + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                     new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null)
            {
               result.append(line);
            }
            dvLogger.info("Response from Add bill: " + result.toString());

            responseObject = new JSONObject(result.toString());
         }
         else
         {
            dvLogger.info(
                     "Exception while fetching space details\n error code : "
                              + code);
         }
         post.releaseConnection();
      }
      catch (Exception e)
      {
         dvLogger.error("Exception while fetching space details\n", e);
      }
      return responseObject;
   }
   public HttpClient initClient(HttpClient client) 
   {
      RequestConfig.Builder requestBuilder = RequestConfig.custom();
      requestBuilder = requestBuilder.setConnectTimeout(25 * 1000);
      requestBuilder = requestBuilder.setConnectionRequestTimeout(25 * 1000);

      HttpClientContext context = HttpClientContext.create();
      CookieStore cookieStore = new BasicCookieStore();
      context.setCookieStore(cookieStore);
      client = HttpClientBuilder.create()
               .setDefaultCookieStore(cookieStore)
               .setRedirectStrategy(new LaxRedirectStrategy())
               .setDefaultRequestConfig(requestBuilder.build()).build();
      return client;
   }
   
   
}
