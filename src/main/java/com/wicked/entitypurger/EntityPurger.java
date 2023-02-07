package com.wicked.entitypurger;

import com.wicked.entitypurger.command.EntityPurgerCommand;
import com.wicked.entitypurger.command.look.EntityPurgerLookHandler;
import com.wicked.entitypurger.configuration.ConfigManager;
import com.wicked.entitypurger.entity.EntityNameplate;
import com.wicked.entitypurger.integration.FTBUtilitiesIntegration;
import com.wicked.entitypurger.network.EntityPurgerPacketHandler;
import com.wicked.entitypurger.render.RenderNameplate;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = EntityPurger.MODID,
        name = EntityPurger.NAME,
        version = EntityPurger.VERSION,
        canBeDeactivated = true,
        acceptableRemoteVersions = "*",
        dependencies = "after:ftbutilities;"
)
public class EntityPurger
{
    public static final String MODID = "entitypurger";
    public static final String NAME = "Entity Purger";
    public static final String VERSION = "1.2.4";

    private Side side;
    private MinecraftServer minecraftServer;

    private EntityPurgerLookHandler lookHandler;
    private EntityPurgerTaskManager taskManager;
    private EntityPurgeRunner purgeRunner;
    private EntityPurgerPacketHandler packetHandler;
    private ConfigManager configManager;

    //Integrations
    private FTBUtilitiesIntegration ftbUtilitiesIntegration;

    private void loadIntegrations(){
        if(Loader.isModLoaded(FTBUtilitiesIntegration.MOD_ID) && configManager.isFtbUtilsIntegrationEnabled()){
            ftbUtilitiesIntegration = new FTBUtilitiesIntegration();
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        side = event.getSide();
        Logger LOGGER = event.getModLog();

        configManager = new ConfigManager(event, VERSION, LOGGER);
        packetHandler = new EntityPurgerPacketHandler(this, LOGGER);

        if(side.isClient()){
            taskManager = new EntityPurgerTaskManager(LOGGER);
            lookHandler = new EntityPurgerLookHandler(configManager, taskManager, this, LOGGER);
            RenderingRegistry.registerEntityRenderingHandler(EntityNameplate.class, RenderNameplate::new);
            MinecraftForge.EVENT_BUS.register(lookHandler);
            MinecraftForge.EVENT_BUS.register(configManager);
        }

        purgeRunner = new EntityPurgeRunner(configManager,this, LOGGER);
        MinecraftForge.EVENT_BUS.register(purgeRunner);
        MinecraftForge.EVENT_BUS.register(this);

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        loadIntegrations();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        minecraftServer = event.getServer();
        EntityPurgerCommand entityPurgerCommand = new EntityPurgerCommand(this);
        event.registerServerCommand(entityPurgerCommand);

        configManager.resetState();
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent clientDisconnectionFromServerEvent){
        configManager.resetState();
        if(side.isClient()){
            taskManager.destroyAllTasks();
        }
    }

    public ConfigManager getConfigManager(){
        return configManager;
    }

    public EntityPurgeRunner getPurgeRunner(){
        return purgeRunner;
    }

    public EntityPurgerPacketHandler getPacketHandler() {
        return packetHandler;
    }

    public EntityPurgerLookHandler getLookHandler() { return lookHandler; }

    public Side getSide() {
        return side;
    }

    public MinecraftServer minecraftServer() { return minecraftServer; }

    public FTBUtilitiesIntegration getFtbUtilitiesIntegration() {
        return ftbUtilitiesIntegration;
    }
}