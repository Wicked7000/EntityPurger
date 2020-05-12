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

public class EntityPurgeDisable extends CommandBase {
    private final EntityPurger entityPurger;

    public EntityPurgeDisable(EntityPurger entityPurger){
        super();
        this.entityPurger = entityPurger;
    }

    @Override
    @Nonnull
    public String getName() {
        return "disable";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.wicked.entitypurge.disable";
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
        entityPurger.getConfigManager().setEnabled(false);
        TextComponentString disabled = new TextComponentString("Entity Purger is disabled!");
        disabled.setStyle(new Style().setColor(TextFormatting.RED));
        sender.sendMessage(disabled);
    }
}
