package com.wicked.entitypurger.configuration;

import com.fasterxml.jackson.databind.JsonNode;

public class ConfigLoadResult{
    private final String errorMessage;
    private final boolean successful;
    private final JsonNode configuration;

    public ConfigLoadResult(boolean successful, JsonNode config, String errorMessage){
        this.successful = successful;
        this.configuration = config;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public JsonNode getConfiguration() {
        return configuration;
    }
}