package com.wicked.entitypurger;

import com.wicked.entitypurger.command.EntityPurgerCommand;
import com.wicked.entitypurger.command.look.EntityPurgerLookHandler;
import com.wicked.entitypurger.configuration.ConfigManager;
import com.wicked.entitypurger.entity.EntityNameplate;
import com.wicked.entitypurger.network.EntityPurgerPacketHandler;
import com.wicked.entitypurger.render.RenderNameplate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = EntityPurger.MODID, name = EntityPurger.NAME, version = EntityPurger.VERSION, canBeDeactivated = true, acceptableRemoteVersions = "*")
public class EntityPurger
{
    public static final String MODID = "entitypurger";
    public static final String NAME = "Entity Purger";
    public static final String VERSION = "1.2";

    private Logger LOGGER;
    private Side side;
    private MinecraftServer server;

    private EntityPurgerLookHandler lookHandler;
    private EntityPurgeRunner entityPurgeRunner;
    private EntityPurgerPacketHandler entityPurgerPacketHandler;
    private ConfigManager configManager;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();

        configManager = new ConfigManager(event, VERSION, LOGGER);
        entityPurgerPacketHandler = new EntityPurgerPacketHandler(this, LOGGER);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        side = event.getSide();

        if(side.isClient()){
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            renderManager.entityRenderMap.put(EntityNameplate.class, new RenderNameplate(renderManager));
            lookHandler = new EntityPurgerLookHandler(configManager, this, LOGGER);
            MinecraftForge.EVENT_BUS.register(lookHandler);
            MinecraftForge.EVENT_BUS.register(configManager);
        }

        entityPurgeRunner = new EntityPurgeRunner(configManager,this, LOGGER);
        MinecraftForge.EVENT_BUS.register(entityPurgeRunner);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        EntityPurgerCommand entityPurgerCommand = new EntityPurgerCommand(this);
        server = event.getServer();
        event.registerServerCommand(entityPurgerCommand);

        configManager.resetState();
    }

    public MinecraftServer getServer(){ return server; }

    public ConfigManager getConfigManager(){
        return configManager;
    }

    public EntityPurgeRunner getEntityPurgeRunner(){
        return entityPurgeRunner;
    }

    public EntityPurgerPacketHandler getEntityPurgerPacketHandler() {
        return entityPurgerPacketHandler;
    }

    public EntityPurgerLookHandler getLookHandler() { return lookHandler; }

    public Side getSide() {
        return side;
    }
}