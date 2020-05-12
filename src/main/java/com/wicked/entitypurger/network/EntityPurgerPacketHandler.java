package com.wicked.entitypurger.network;

import com.wicked.entitypurger.EntityPurger;
import com.wicked.entitypurger.network.LookMode;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

public class EntityPurgerPacketHandler {
    private final EntityPurger entityPurger;
    private final Logger logger;
    private final SimpleNetworkWrapper networkInstance;

    private int id;

    public EntityPurgerPacketHandler(EntityPurger entityPurger, Logger logger){
        this.entityPurger = entityPurger;
        this.logger = logger;
        this.id = 0;

        networkInstance = NetworkRegistry.INSTANCE.newSimpleChannel("entitypurger");
        registerMessages();
    }

    private void registerMessages(){
        LookMode lookMode = new LookMode(entityPurger);
        networkInstance.registerMessage(lookMode, LookMode.LookModeRequest.class, id++, Side.CLIENT);
    }

    public void sendLookModeMessage(EntityPlayerMP playerMP){
        networkInstance.sendTo(new LookMode.LookModeRequest(), playerMP);
    }
}
