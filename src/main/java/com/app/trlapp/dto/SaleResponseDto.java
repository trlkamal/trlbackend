package com.app.trlapp.dto;

import com.app.trlapp.model.Sale;

import java.util.ArrayList;
import java.util.List;

public class SaleResponseDto {
    private Sale sale;
    private List<String> messages = new ArrayList<>();
    private boolean hasError;
    
    
	public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public boolean hasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
}
