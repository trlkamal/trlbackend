package com.app.trlapp.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AESUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING"; // AES with CBC and PKCS5Padding
    private static final String CHARSET = "UTF-8";
    private static final Logger logger = LoggerFactory.getLogger(AESUtil.class); // Logger for this class

    // Method to decrypt the encrypted user ID
    public static String decryptUserId(String encryptedUserId, String masterKey, String ivParameter) throws Exception {
        logger.info("Decrypting user ID");
        String decryptedUserId = decrypt(encryptedUserId, masterKey, ivParameter);
        logger.info("Decrypted user ID: {}", decryptedUserId);
        return decryptedUserId;
    }

    // Method to decrypt the encrypted username
    public static String decryptUserName(String encryptedUserName, String masterKey, String ivParameter) throws Exception {
        logger.info("Decrypting user name");
        String decryptedUserName = decrypt(encryptedUserName, masterKey, ivParameter);
        logger.info("Decrypted user name: {}", decryptedUserName);
        return decryptedUserName;
    }

    // Method to decrypt the encrypted password
    public static String decryptPassword(String encryptedPassword, String masterKey, String ivParameter) throws Exception {
       
        String decryptedPassword = decrypt(encryptedPassword, masterKey, ivParameter);
       
        return decryptedPassword;
    }

    // Common decryption logic to avoid code duplication
    private static String decrypt(String encryptedText, String masterKey, String ivParameter) throws Exception {
        // Decode the Base64 encoded values
        byte[] decodedKey = masterKey.getBytes(CHARSET);
        byte[] decodedIV = ivParameter.getBytes(CHARSET);

        // Set up AES cipher with CBC mode and PKCS5 padding
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(decodedIV);

        // Initialize the cipher for decryption
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

        // Decode the encrypted text from Base64
        byte[] decodedEncryptedText = Base64.getDecoder().decode(encryptedText);

        // Perform the decryption
        byte[] originalBytes = cipher.doFinal(decodedEncryptedText);

        // Convert the decrypted bytes to a string
        return new String(originalBytes, CHARSET);
    }
}
