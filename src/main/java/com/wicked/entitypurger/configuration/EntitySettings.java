package com.wicked.entitypurger.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EntitySettings {
    private final String entityId;
    private final Integer threshold;
    private final Integer lifetime;
    private final boolean overrideClaimedChunk;
    private final boolean overridePlayerChunk;

    public EntitySettings(@JsonProperty(value = "threshold", required = false, defaultValue = "100") Integer threshold,
                          @JsonProperty(value = "lifetime", required = false) Integer lifetime,
                          @JsonProperty(value = "entityId", required = true) String entityId,
                          @JsonProperty(value = "overrideClaimedChunk", required = false, defaultValue = "false") boolean overrideClaimedChunk,
                          @JsonProperty(value = "overridePlayerChunk", required = false, defaultValue = "false") boolean overridePlayerChunk) {
        this.threshold = threshold;
        this.entityId = entityId;
        this.lifetime = lifetime;
        this.overrideClaimedChunk = overrideClaimedChunk;
        this.overridePlayerChunk = overridePlayerChunk;
    }

    public String getEntityId() {
        return entityId;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public Integer getLifetime() {
        return lifetime;
    }

    public boolean overrideClaimedChunk() { return overrideClaimedChunk; }

    public boolean isOverridePlayerChunk() {
        return overridePlayerChunk;
    }
}
