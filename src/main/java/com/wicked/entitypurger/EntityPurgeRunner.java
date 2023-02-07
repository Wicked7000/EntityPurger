package com.wicked.entitypurger;

import com.wicked.entitypurger.command.list.EntitySummary;
import com.wicked.entitypurger.configuration.ConfigManager;
import com.wicked.entitypurger.configuration.EntitySettings;
import com.wicked.entitypurger.integration.FTBUtilitiesIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
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

    private boolean canKillEntity(Entity entity){
        boolean entityTamed = EntityHelper.isEntityTamed(entity);
        boolean namedEntity = EntityHelper.isNamedEntity(entity);
        boolean purgeTamedEntity = (configManager.canPurgeTamedEntities() || !entityTamed);
        boolean purgeNamedEntity = (configManager.canPurgeNamedEntities() || !namedEntity);
        return purgeNamedEntity && purgeTamedEntity && !EntityHelper.isEntityPlayer(entity);
    }

    private int killViaThreshold(int dimensionId, int amountOfEntity, EntitySettings entitySettings, List<Entity> entitiesOfType){
        int amountKilled = 0;
        int amountToKillToReachThreshold = amountOfEntity - entitySettings.getThreshold();

        int entityIdx = 0;
        while(amountKilled < amountToKillToReachThreshold && entityIdx < entitiesOfType.size() && amountOfEntity > 0){
            Entity entity = entitiesOfType.get(entityIdx);
            if(isChunkBlacklisted(dimensionId, entity, entitySettings)){
                entityIdx++;
                continue;
            }
            if (canKillEntity(entity)) {
                killEntity(entity);
                amountKilled++;
            }

            amountOfEntity--;
            entityIdx++;
        }

        return amountKilled;
    }

    private int killViaTicksExisted(int dimensionId, EntitySettings entitySettings, List<Entity> entitiesOfType){
        int amountKilled = 0;

        for(Entity entity : entitiesOfType){
            if(isChunkBlacklisted(dimensionId, entity, entitySettings)){
                continue;
            }
            if (canKillEntity(entity) && entity.ticksExisted / TICKS_IN_SECOND > entitySettings.getLifetime()) {
                killEntity(entity);
                amountKilled++;
            }
        }
        return amountKilled;
    }

    private boolean isChunkBlacklisted(int dimensionId, Entity entity, EntitySettings entitySettings){
        ChunkPos entityChunk = new ChunkPos(entity.getPosition());

        FTBUtilitiesIntegration ftbUtilitiesIntegration = entityPurger.getFtbUtilitiesIntegration();
        if(Objects.nonNull(ftbUtilitiesIntegration) && configManager.isFtbUtilsIntegrationEnabled() && configManager.areClaimedChunksBlacklisted() && !entitySettings.overrideClaimedChunk()){
            if(ftbUtilitiesIntegration.isClaimedChunk(entityChunk, dimensionId)){
                return true;
            };
        }

        int chunkDistanceNearPlayer = configManager.getBlacklistedChunksNearPlayer();
        if(!entitySettings.isOverridePlayerChunk()){
            if(entityPurger.getSide().isServer()){
                MinecraftServer server = entityPurger.minecraftServer();
                List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
                for(EntityPlayerMP player : players){
                    ChunkPos playerChunk = new ChunkPos(player.getPosition());
                    int chunkDistance = EntityHelper.getChunkDistance(entityChunk, playerChunk);
                    if(chunkDistance <= chunkDistanceNearPlayer){
                        return true;
                    }
                }
            } else {
                EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
                if(player != null){
                    ChunkPos playerChunk = new ChunkPos(player.getPosition());
                    int chunkDistance = EntityHelper.getChunkDistance(entityChunk, playerChunk);
                    return chunkDistance <= chunkDistanceNearPlayer;
                }
            }
        }


        return false;
    }

    private Integer killEntityOfType(int dimensionId, String entityKey, List<Entity> entitiesOfType){
        int amountOfEntity = entitiesOfType.size();
        EntitySettings entitySettings = configManager.getSettingsForEntity(entityKey);

        int amountKilled = 0;
        if(Objects.nonNull(entitySettings.getLifetime())){
            amountKilled = killViaTicksExisted(dimensionId, entitySettings, entitiesOfType);
            if(configManager.isLoggingEnabled() && amountKilled > 0){
                logger.info(String.format("Removed %d entities that 'expired' their lifetime %s", amountKilled, entityKey));
            }
        }
        if(Objects.nonNull(entitySettings.getThreshold()) && (amountOfEntity - amountKilled) > entitySettings.getThreshold()){
            amountKilled += killViaThreshold(dimensionId, amountOfEntity, entitySettings, entitiesOfType);
            if(configManager.isLoggingEnabled() && amountKilled > 0){
                logger.info(String.format("Removed %d entities that went over the threshold %s", amountKilled, entityKey));
            }

        }

        return amountKilled;
    }

    private boolean testMatch(List<String> list, String entityType){
        if(entityType == null || entityType.isEmpty()){
            return false;
        }

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
        List<String> blocklist = configManager.getBlocklist();
        List<String> allowlist = configManager.getAllowList();

        for(int dimensionId : DimensionManager.getIDs()){
            World world = DimensionManager.getWorld(dimensionId);

            List<EntitySummary> entitySummaries = EntityHelper.buildListOfCurrentEntities(world);
            for(EntitySummary entitySummary : entitySummaries){
                String entityType = entitySummary.getEntityType();
                if(blocklist.size() > 0 && testMatch(blocklist, entityType)){
                    continue;
                }

                Integer purgedAmount = 0;
                if(allowlist.size() > 0 && testMatch(allowlist, entityType)){
                    purgedAmount = killEntityOfType(dimensionId, entityType, entitySummary.getEntities());
                }else if(allowlist.size() == 0){
                    purgedAmount = killEntityOfType(dimensionId, entityType, entitySummary.getEntities());
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
