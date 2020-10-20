package com.digivalet.pmsi.exceptions;

@SuppressWarnings("serial")
public class DVFileException extends Exception
{
   private int exception_code ;
   private String exception_message;
   
   public enum DVExceptionCodes
   {
      FILE_NOT_FOUND_EXCEPTION(501, "File is not availble on specified path. %s"),
      UNMARSHAL_EXCPETION(502, "Error while unmarshling XML"),
      JAXB_PARSING_EXCEPTION(503, "Error while parsing XML"),
      FILE_READ_EXCEPTION(504, "File not found. %s "),
      JSON_PARSING_EXCEPTION(505, "Error while parsing JSON. %s "),
      BLANK_OR_NULL_FILEPATH(506, "Error while intializing because of null/blank file path . %s ");

      private final int id;
      private final String message;

      DVExceptionCodes(int id, String message) 
      {
        this.id = id;
        this.message = message;
      }

      public int getExceptionCode() 
      {
        return this.id;
      }

      public String getExceptionMessage() 
      {
        return this.message;
      }
   }
   
   
   public DVFileException(DVExceptionCodes dvExceptionCodes, String... args)
   {
      this.exception_code = dvExceptionCodes.getExceptionCode();
      this.exception_message = String.format(dvExceptionCodes.getExceptionMessage(), args[0]);
   }

   public int getException_code()
   {
      return exception_code;
   }

   public void setException_code(int exception_code)
   {
      this.exception_code = exception_code;
   }

   public String getException_message()
   {
      return exception_message;
   }

   public void setException_message(String exception_message)
   {
      this.exception_message = exception_message;
   }
   @Override
   public String getMessage()
   {
      return exception_message;
   }
}

