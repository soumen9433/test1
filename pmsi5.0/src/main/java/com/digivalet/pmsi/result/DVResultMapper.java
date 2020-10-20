package com.digivalet.pmsi.result;

import com.digivalet.core.DVLogger;

public class DVResultMapper
{
   private static DVLogger dvLogger = DVLogger.getInstance();
   public int getApiCode(int dvCode)
   {

      int apiCode = 0;

      switch (dvCode)
      {

         case 115:
            apiCode = 200;
            break;

         case 116:
            apiCode = 400;
            break;

         case 200:
            apiCode = 200;
            break;

         case 201:
            apiCode = 400;
            break;

         case 202:
            apiCode = 200;
            break;

         case 203:
            apiCode = 404;
            break;

         case 204:
            apiCode = 200;
            break;

         case 205:
            apiCode = 404;
            break;
         case 206:
            apiCode = 200;
            break;
         case 207:
            apiCode = 200;
            break;
         case 208:
            apiCode = 200;
            break;
         case 209:
            apiCode = 400;
            break;
         case 210:
            apiCode = 404;
            break;
         case 211:
            apiCode = 400;
            break;
         case 212:
            apiCode = 400;
            break;
         case 213:
            apiCode = 404;
            break;
         case 214:
            apiCode = 400;
            break;
         case 215:
            apiCode = 404;
            break;
         case 216:
            apiCode = 400;
            break;
         case 217:
            apiCode = 404;
            break;
         case 218:
            apiCode = 400;
            break;
         case 219:
            apiCode = 404;
            break;
         case 220:
            apiCode = 400;
            break;
         default:
            apiCode = 405;
            break;
      }

      dvLogger.info("API code=" + apiCode);

      return apiCode;

   }


}
