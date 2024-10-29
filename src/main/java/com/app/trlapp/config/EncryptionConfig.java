package com.app.trlapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    @Value("${encrypted.secretKey}")
    private String encryptedSecretKey;
    @Value("${MASTER_KEY}")  
    private String masterkey;

    @Value("${iv.parameter}")
    private String ivParameter;
    
    @Value("${custom.api-docs.path}")
    private String apiDocsPath;

    public String getEncryptedSecretKey() {
        return encryptedSecretKey;
    }
    
    public String getIvParameter() {
        return ivParameter;
    }
    public String getMasterkey() {
        return masterkey;
    }
   
    public String getApiDocsPath() {
        return apiDocsPath;
    } 
    
}
