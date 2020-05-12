package com.wicked.entitypurger.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EntitySettings {
    public boolean useThreshold;
    public String entityId;
    public Integer threshold;
    public Integer lifetime;

    public EntitySettings(@JsonProperty(value = "threshold", required = false, defaultValue = "100") Integer threshold,
                          @JsonProperty(value = "useThreshold", required = true) boolean useThreshold,
                          @JsonProperty(value = "lifetime", required = false, defaultValue = "120") Integer lifetime,
                          @JsonProperty(value = "entityId", required = true) String entityId) {
        if(useThreshold){
            this.useThreshold = true;
            this.threshold = threshold;
        }else{
            this.useThreshold = false;
            this.threshold = null;
        }
        this.entityId = entityId;
        this.lifetime = lifetime;
    }

    public String getEntityId() {
        return entityId;
    }

    public boolean isUseThreshold() {
        return useThreshold;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public Integer getLifetime() {
        return lifetime;
    }
}
