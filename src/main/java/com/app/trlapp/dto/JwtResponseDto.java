package com.app.trlapp.dto;

import lombok.Data;

@Data
public class JwtResponseDto {
	private String accessToken;
    private String refreshToken;
    
    public JwtResponseDto()
    {
    	
    }

    public JwtResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    public String getAccessToken()
    {
    	return accessToken;
    }
    public void setAccessToken(String acessToken)
    {
    this.accessToken = 	accessToken;
    }
}
