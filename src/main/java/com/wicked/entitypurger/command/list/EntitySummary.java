package com.wicked.entitypurger.command.list;

import net.minecraft.entity.Entity;

import java.util.List;

public class EntitySummary {
    private final String entityType;
    private final List<Entity> entities;

    public EntitySummary(String entityType, List<Entity> entities){
        this.entities = entities;
        this.entityType = entityType;
    }

    public Entity getEntity(){
        return entities.get(0);
    }

    public int getCount() {
        return entities.size();
    }

    public String getEntityType() {
        return entityType;
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
