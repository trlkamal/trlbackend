package com.app.trlapp.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CustomConfig {

    @Bean
    public String encryptedSecretKey() {
        return System.getenv("ENCRYPTED_SECRET_KEY");
    }

    @Bean
    public String masterKey() {
        return System.getenv("MASTER_KEY");
    }

    @Bean
    public String ivParameter() {
        return System.getenv("IV_PARAMETER");
    }

    @Bean
    public String apiDocsPath() {
        return System.getenv("API_PATH");
    }

    @Bean
    public String customApiDocsPath() {
        return System.getenv("API_PATH");
    }
}
