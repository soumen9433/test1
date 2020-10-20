package com.digivalet.pmsi.operaserver;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import com.digivalet.core.DVLogger;

public class DVEncryptDecrypt
{
   private DVLogger dvLogger = DVLogger.getInstance();
   private final String UTF = "UTF-8";

   public String encrypt(String key, String value)
   {
      try
      {
         SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(UTF), "AES");

         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
         cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

         byte[] encrypted = cipher.doFinal(value.getBytes());
         dvLogger.info("encrypted string: ",
                  Base64.encodeBase64String(encrypted));

         return Base64.encodeBase64String(encrypted);
      }
      catch (Exception ex)
      {
         dvLogger.error("Error while encrypting Cipher Text", ex);
      }

      return null;
   }

   public String decrypt(String key, String encrypted)
   {
      try
      {
         SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(UTF), "AES");

         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
         cipher.init(Cipher.DECRYPT_MODE, skeySpec);

         byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

         return new String(original);
      }
      catch (Exception ex)
      {
         dvLogger.error("Error while dencrypting Cipher Text", ex);
      }

      return null;
   }

}
