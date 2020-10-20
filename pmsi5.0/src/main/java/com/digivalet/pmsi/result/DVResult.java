package com.digivalet.pmsi.result;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import com.digivalet.core.DVLogger;
import com.digivalet.pmsi.settings.DVSettings;

/**
 * <B>Description:</B> This class holds the list of possible Info, Error & Warning codes with its
 * description. While processing for request, If any error is encountered then in DVResult
 * Constructor , Error code is passed else if it successfully executed then Info code is passed.
 * Passed code is returned to Queue Handler which then uses {@link DVResponseMapper} class to wrap
 * returned code to Code which will be passed to iPad.
 * 
 */
public class DVResult
{
   private static DVLogger dvLogger = DVLogger.getInstance();
   private int responseCode;
   private String description;
   private String status = "{\"error\":\"status_not_set\"}";
  // private static Map<String, String> responseDescription = new HashMap<String, String>();
   public static final int DVERROR_BEFORE_PROCESSING = 100;
   public static final int DVERROR_FILE_NOT_FOUND = 101;
   public static final int DVINFO_DATA_SENT_SUCCESSFULLY = 107;
   public static final int DVERROR_PARSING_REQUEST = 109;
   public static final int DVERROR_PREPARING_COMMAND = 110;
   public static final int DVERROR_COMMAND_LIST_IS_EMPTY = 111;
   public static final int DVERROR_COMMAND_NOT_INITIALIZED = 112;
   public static final int DVERROR_INVALID_OPERATION = 113;
   public static final int DVINFO_REQUEST_COMPLETED_SUCCESSFULLY = 114;
   public static final int DVERROR_LANGUAGE_CODE_NOT_FOUND = 217; 
   public static final int DVERROR_LANGUAGE_CODE_NULL = 218;
   public static final int DVERROR_DEVICE_TYPE_NOT_FOUND = 215; 
   public static final int DVERROR_DEVICE_TYPE_NULL = 216;
   public static final int DVERROR_GUESTID_NOT_FOUND = 205;
   public static final int DVINFO_GUESTID_FOUND = 206;
   public static final int DVERROR_ROOMID_NOT_FOUND = 207;
   public static final int DVINFO_ROOMID_FOUND = 208; 
   public static final int DVERROR_ROOMID_NULL = 209;
   public static final int DVERROR_HOTELCODE_NULL = 210;
   public static final int DVERROR_GUESTID_NULL = 211;
   public static final int DVERROR_HOTELCODE_NOT_FOUND = 201;
   public static final int DVINFO_HOTELCODE_FOUND = 202;
   public static final int DVERROR_PMS_DOWN=251;
   public static final int DVERROR_REMOTE_GO_FAILED=252;
   public static final int DVERROR_ADDING_PREFERENCE=252;
   public static final int DVERROR_INVALID_MOODID=253;
   public static final int DVERROR_INVALID_RESERVATION_NO=254;
   public static final int SUCCESS = 200;
   public DVResult()
   {
      
   }  

   public DVResult(DVResponseCode responseCode, String message)
   {
      this.responseCode = responseCode.code;
      this.description = String.format(DVResponseCode.getDescription(responseCode.code), message);      
   }
   
   public DVResult(int responseCode, String message,String... args)
   {
      this.responseCode = responseCode;
      if(DVResponseCode.isDescriptionAvailable(responseCode))
      {
         this.description = String.format(DVResponseCode.getDescription(responseCode), message); 
      }
      else
      {           
         this.description = message;
      }         
      if (args.length > 0)
      {
         status = args[0];
      } 
   }

   public String getStatus()
   {
      return status;
   }
   
   public enum DVResponseCode
   {
      
      DVINFO_STARTED_PROCESSING(100, "Starting to perform request. %s"), 
      DVERROR_FILE_NOT_FOUND(101,"Error : File not found. Filepath is %s & filename is %s"), 
      DVERROR_PRECONDITIONS_NOT_MATCHED(106, "ERROR : Not Submitting request."),
      DVINFO_DATA_SENT_SUCCESSFULLY(107, "INFO : Data has been written successfully to Server. %s"), 
      DVERROR_PREPARING_COMMAND(110,"Error : Error preparing commmands. %s");

      
      private int code;
      private String description;
      private static final Map<Integer, String> codeMap = new HashMap<>();

      static
      {
         for (DVResponseCode dvResponseCode : DVResponseCode.values())
         {
            codeMap.put(dvResponseCode.code, dvResponseCode.description);
         }
      }

      DVResponseCode(int code, String description)
      {
         this.code = code;
         this.description = description;
      }

      public static String getDescription(int code)
      {
         return codeMap.get(code);
      }

      public static boolean isDescriptionAvailable(int code)
      {
         return codeMap.containsKey(code);
      }

   }



   /**
    * <B>Description:</B> This function is used to load description for codes defined in this class.
    * It Uses DVSettings class instance to get the path of file to load description according to
    * codes.
    * 
    * @param dvSettings
    * @throws ParserConfigurationException
    * @throws SAXException
    * @throws IOException
    * @throws DVResultFileException
    * @throws XPathExpressionException
    */
   public static void init(DVSettings dvSettings)
   {
      dvLogger.info("Description for response/error codes are initialized successfully ");
   }

   public int getCode()
   {
      return this.responseCode;
   }

   /**
    * <B>Description</B> This method is used to get the Description of DVResult
    * 
    * @return Updated Response description in String format
    */
   public String getDescription()
   {
      return description;
   }

   @Override
   public String toString()
   {
      return " [responseCode=" + responseCode + ", description=" + description + "]";
   }

}

