package com.wicked.entitypurger.command;

import com.wicked.entitypurger.EntityPurger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

public class EntityPurgeStatus extends CommandBase {
    private final EntityPurger entityPurger;

    public EntityPurgeStatus(EntityPurger entityPurger){
        super();
        this.entityPurger = entityPurger;
    }

    @Override
    @Nonnull
    public String getName() {
        return "status";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.wicked.entitypurge.status";
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

    @Override
    public void execute(@Nonnull MinecraftServer server,@Nonnull ICommandSender sender,@Nonnull String[] args) throws CommandException {
        String status = entityPurger.getConfigManager().isEnabled() ? "running" : "disabled";
        TextComponentString statusComponent = new TextComponentString(String.format("Entity Purger is currently %s!", status));
        TextFormatting color = entityPurger.getConfigManager().isEnabled() ? TextFormatting.GREEN : TextFormatting.RED;
        statusComponent.setStyle(new Style().setColor(color));
        sender.sendMessage(statusComponent);
    }
}
