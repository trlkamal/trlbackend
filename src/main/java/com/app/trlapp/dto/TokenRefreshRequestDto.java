package com.app.trlapp.dto;

import lombok.Data;

@Data
public class TokenRefreshRequestDto {
	private String refreshToken;
	public TokenRefreshRequestDto(String  refreshToken)
	{
		this.refreshToken = refreshToken;
	}
	public String getRefreshToken()
	{
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken )
	{
		this.refreshToken = refreshToken;
	}
}
