package com.wicked.entitypurger;

import com.wicked.entitypurger.command.list.EntitySummary;
import com.wicked.entitypurger.entity.EntityNameplate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;

public class EntityHelper {
    public static String getName(Entity entity){
        return entity.getClass().getCanonicalName();
    }

    public static boolean isNameplate(Entity entity){
        return entity instanceof EntityNameplate;
    }

    public static boolean isPlayer(Entity entity){
        return entity instanceof EntityPlayer;
    }

    public static boolean isEntityPlayer(Entity entity){
        return entity instanceof EntityPlayer;
    }

    public static boolean isEntityVillager(Entity entity){
        return entity instanceof EntityVillager;
    }

    public static boolean isEntityTamed(Entity entity){
        if(entity instanceof EntityTameable){
            EntityTameable tameable = (EntityTameable)entity;
            return tameable.isTamed();
        }
        return false;
    }

    public static List<EntitySummary> buildListOfCurrentEntities(World world){
        Map<String, List<Entity>> entitiesMap = new HashMap<>();
        List<Entity> entities = world instanceof WorldServer ? world.loadedEntityList : world.getLoadedEntityList();

        for(Entity entity : entities){

            String key = getName(entity);
            List<Entity> currentEntitiesOfType = entitiesMap.getOrDefault(key, new ArrayList<>());
            currentEntitiesOfType.add(entity);
            entitiesMap.put(key, currentEntitiesOfType);
        }

        List<EntitySummary> entitySummaries = new ArrayList<>();
        entitiesMap.forEach((key, value) -> {
            entitySummaries.add(new EntitySummary(key, value));
        });

        entitySummaries.sort(Comparator.comparingInt(EntitySummary::getCount).reversed());

        return entitySummaries;
    }
}
