package com.app.trlapp.util;

import java.security.Key;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class StrongKeyGenerator {

    // Generate a strong key suitable for HS512
    public Key generateStrongKey(int length) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512");
            keyGenerator.init(length); // Specify key size (e.g., 512 bits for HS512)
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate strong key", e);
        }
    }
}
