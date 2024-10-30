package com.app.trlapp.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    private String encryptedSecretKey;
    private String masterKey;
    private String ivParameter;
    private String apiDocsPath;

    // Constructor to initialize values from environment variables
    public EncryptionConfig() {
        this.encryptedSecretKey = System.getenv("ENCRYPTED_SECRET_KEY");
        this.masterKey = System.getenv("MASTER_KEY");
        this.ivParameter = System.getenv("IV_PARAMETER");
        this.apiDocsPath = System.getenv("API_PATH");
    }

    public String getEncryptedSecretKey() {
        return encryptedSecretKey;
    }
    
    public String getIvParameter() {
        return ivParameter;
    }
    
    public String getMasterKey() {
        return masterKey;
    }
   
    public String getApiDocsPath() {
        return apiDocsPath;
    } 
}
