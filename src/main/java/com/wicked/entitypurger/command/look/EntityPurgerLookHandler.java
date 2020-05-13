package com.wicked.entitypurger.command.look;

import com.wicked.entitypurger.EntityHelper;
import com.wicked.entitypurger.EntityPurger;
import com.wicked.entitypurger.EntityPurgerTaskManager;
import com.wicked.entitypurger.configuration.ConfigManager;
import com.wicked.entitypurger.entity.EntityNameplate;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class EntityPurgerLookHandler {
    private static final float DISTANCE_TO_SPAWN_NAMEPLATE = 10f;

    private final EntityPurger entityPurger;
    private final EntityPurgerTaskManager taskManager;
    private final ConfigManager configManager;
    private final Minecraft minecraftInstance;
    private final Logger logger;

    private final Map<Integer, EntityNameplate> nameplates;

    public EntityPurgerLookHandler(ConfigManager configManager,
                                   EntityPurgerTaskManager taskManager,
                                   EntityPurger entityPurger,
                                   Logger logger){
        this.taskManager = taskManager;
        this.configManager = configManager;
        this.entityPurger = entityPurger;
        this.logger = logger;
        this.nameplates = new HashMap<>();
        this.minecraftInstance = Minecraft.getMinecraft();
    }

    private void run(EntityPlayer player) {
        Minecraft mcInstance = Minecraft.getMinecraft();

        Vec3d startpoint = player.getPositionEyes(1f);
        Vec3d forwardRaw = player.getForward();
        Vec3d forward = forwardRaw.scale(DISTANCE_TO_SPAWN_NAMEPLATE);
        Vec3d endpoint = startpoint.add(forward);

        RayTraceResult rayTraceResult = mcInstance.world.rayTraceBlocks(startpoint, endpoint, false, true, false);
        if(rayTraceResult != null){
            if(rayTraceResult.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                BlockPos pos = rayTraceResult.getBlockPos();

                int boundingBoxSize = 3;
                AxisAlignedBB axisAlignedBB = new AxisAlignedBB(pos.getX() - boundingBoxSize,
                        pos.getY() - boundingBoxSize,
                        pos.getZ() - boundingBoxSize,
                        pos.getX() + boundingBoxSize,
                        pos.getY() + boundingBoxSize,
                        pos.getZ() + boundingBoxSize);
                List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, axisAlignedBB);
                for (Entity entity : entities) {
                    if(!EntityHelper.isNameplate(entity) && !EntityHelper.isPlayer(entity)){
                        int entityId = entity.getEntityId();
                        if(!nameplates.containsKey(entityId)){
                            BlockPos entityPos = entity.getPosition();
                            EntityNameplate toSpawn = new EntityNameplate(
                                    player.world,
                                    EntityHelper.getName(entity),
                                    entity,
                                    entityPos.getX(),
                                    entityPos.getY(),
                                    entityPos.getZ());
                            player.world.spawnEntity(toSpawn);

                            nameplates.put(entityId, toSpawn);
                        }
                    }
                }
            }
        }

        List<Integer> toRemove = new ArrayList<>();
        for(Integer entityId : nameplates.keySet()){
            EntityNameplate nameplate = nameplates.get(entityId);
            if(nameplate.getDistance(player) > DISTANCE_TO_SPAWN_NAMEPLATE){
                nameplate.setDead();
                toRemove.add(entityId);
            };
        }

        for(Integer removeId : toRemove){
            nameplates.remove(removeId);
        }
    }

    public void purgeAllNameplates(){
        taskManager.registerTask(() -> {
            List<Integer> toRemove = new ArrayList<>();
            for(Integer entityId : nameplates.keySet()){
                EntityNameplate nameplate = nameplates.get(entityId);
                nameplate.setDead();
                toRemove.add(entityId);
            }

            for(Integer removeId : toRemove){
                nameplates.remove(removeId);
            }
        });
    }

    @SubscribeEvent
    public void runnerTimer(TickEvent.ClientTickEvent clientTickEvent){
        if(configManager.isLookMode() && entityPurger.getSide().isClient()){
            run(minecraftInstance.player);
        }
    }
}
