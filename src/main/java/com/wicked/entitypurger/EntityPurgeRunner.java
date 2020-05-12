package com.wicked.entitypurger;

import com.wicked.entitypurger.command.list.EntitySummary;
import com.wicked.entitypurger.configuration.ConfigManager;
import com.wicked.entitypurger.configuration.EntitySettings;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityPurgeRunner {
    private static final int TICKS_IN_SECOND = 20;
    private final Logger logger;
    private final ConfigManager configManager;
    private final EntityPurger entityPurger;

    private int currentTimer = 0;

    public EntityPurgeRunner(ConfigManager configManager, EntityPurger entityPurger, Logger logger){
        this.configManager = configManager;
        this.entityPurger = entityPurger;
        this.logger = logger;
    }

    private void killEntity(Entity entity){
        entity.setDropItemsWhenDead(false);
        entity.setDead();
    }

    private Integer killEntityOfType(String entityKey, List<Entity> entitiesOfType){
        int amountOfEntity = entitiesOfType.size();
        EntitySettings entitySettings = configManager.getSettingsForEntity(entityKey);

        int amountKilled = 0;
        if(entitySettings.useThreshold && amountOfEntity > entitySettings.threshold){
            int amountToKillToReachThreshold = amountOfEntity - entitySettings.threshold;

            int entityIdx = 0;
            while(amountKilled < amountToKillToReachThreshold && entityIdx < entitiesOfType.size() && amountOfEntity > 0){
                Entity entity = entitiesOfType.get(entityIdx);
                boolean entityTamed = EntityHelper.isEntityTamed(entity);
                if ((configManager.canPurgeTamedEntities() || !entityTamed) && !EntityHelper.isEntityPlayer(entity)) {
                    killEntity(entity);
                    amountKilled += 1;
                }
                amountOfEntity -= 1;
                entityIdx += 1;
            }

            if(configManager.isLoggingEnabled()){
                logger.info(String.format("Removed %d entities that went over the threshold %s", amountKilled, entityKey));
            }
        }else if(!entitySettings.useThreshold){
            for(Entity entity : entitiesOfType){
                if (!EntityHelper.isEntityTamed(entity) && !EntityHelper.isEntityPlayer(entity)) {
                    if(entity.ticksExisted / TICKS_IN_SECOND > entitySettings.getLifetime()){
                        killEntity(entity);
                        amountKilled += 1;
                    }
                }
            }

            if(configManager.isLoggingEnabled()){
                logger.info(String.format("Removed %d entities that 'expired' their lifetime %s", amountKilled, entityKey));
            }
        }

        return amountKilled;
    }

    private boolean testMatch(List<String> list, String entityType){
        for(String str : list){
            Pattern pattern = Pattern.compile(str);
            Matcher matcher = pattern.matcher(entityType);
            if(matcher.matches()){
                return true;
            }
        }
        return false;
    }

    public Map<String, Integer> run(){
        Map<String, Integer> purgedEntities = new HashMap<>();

        currentTimer = 0;
        List<String> blacklist = configManager.getBlacklist();
        List<String> whitelist = configManager.getWhitelist();

        for(int worldId : DimensionManager.getIDs()){
            World world = DimensionManager.getWorld(worldId);

            List<EntitySummary> entitySummaries = EntityHelper.buildListOfCurrentEntities(world);
            for(EntitySummary entitySummary : entitySummaries){
                String entityType = entitySummary.getEntityType();
                if(blacklist.size() > 0 && testMatch(blacklist, entityType)){
                    continue;
                }

                Integer purgedAmount = 0;
                if(whitelist.size() > 0 && testMatch(whitelist, entityType)){
                    purgedAmount = killEntityOfType(entityType, entitySummary.getEntities());
                }else if(whitelist.size() == 0){
                    purgedAmount = killEntityOfType(entityType, entitySummary.getEntities());
                }

                if(purgedAmount > 0){
                    purgedEntities.put(entityType, purgedAmount);
                }
            }
        }

        return purgedEntities;
    }

    @SubscribeEvent
    public void runnerTimer(TickEvent.ServerTickEvent serverTickEvent){
        if(serverTickEvent.side.equals(Side.SERVER) && entityPurger.getConfigManager().isEnabled()){
            if(serverTickEvent.phase.equals(TickEvent.Phase.END)){
                currentTimer += 1;
            }

            if(currentTimer >= configManager.getCheckTimeSeconds() * TICKS_IN_SECOND){
                run();
            }
        }
    }
}
