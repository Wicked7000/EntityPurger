package com.wicked.entitypurger.command.look;

import com.wicked.entitypurger.EntityPurger;
import com.wicked.entitypurger.network.EntityPurgerPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.awt.*;

public class EntityPurgeLook extends CommandBase {
    private final EntityPurger entityPurger;

    public EntityPurgeLook(EntityPurger entityPurger){
        super();
        this.entityPurger = entityPurger;
    }

    @Override
    @Nonnull
    public String getName() {
        return "look";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.wicked.entitypurge.look";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender)
    {
        if(server.isDedicatedServer()){
            return sender.canUseCommand(2, "");
        }else{
            return true;
        }
    }

    private static void onLookModeToggle(Minecraft mcInstance, EntityPurger entityPurger){
        boolean newLookModeValue = !entityPurger.getConfigManager().isLookMode();
        entityPurger.getConfigManager().setLookMode(newLookModeValue);

        if(!newLookModeValue){
            entityPurger.getLookHandler().purgeAllNameplates();
        }

        String lookModeStatus = newLookModeValue ? "enabled" : "disabled";
        TextFormatting color = newLookModeValue ? TextFormatting.GREEN : TextFormatting.RED;
        TextComponentString lookMode = new TextComponentString(String.format("Look mode is now %s", lookModeStatus));
        lookMode.setStyle(new Style().setColor(color));

        mcInstance.ingameGUI.addChatMessage(ChatType.SYSTEM, lookMode);
    }

    @Override
    public void execute(@Nonnull MinecraftServer server,@Nonnull ICommandSender sender,@Nonnull String[] args) throws CommandException {
        if(!server.isDedicatedServer()){
            onLookModeToggle(Minecraft.getMinecraft(), entityPurger);
        }else{
            TextComponentString warning = new TextComponentString("This command will only execute if EntityPurger installed locally!");
            warning.setStyle(new Style().setColor(TextFormatting.GOLD));
            sender.sendMessage(warning);
            EntityPurgerPacketHandler packetHandler = entityPurger.getEntityPurgerPacketHandler();
            Entity senderEntity = sender.getCommandSenderEntity();

            if(sender instanceof EntityPlayerMP){
                packetHandler.sendLookModeMessage((EntityPlayerMP)senderEntity);
            }
        }
    }

    public static void executeFromServer(Minecraft mcInstance, EntityPurger entityPurger){
        onLookModeToggle(mcInstance, entityPurger);
    }
}
