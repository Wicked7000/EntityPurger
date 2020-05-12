package com.wicked.entitypurger.command;

import com.wicked.entitypurger.EntityPurger;
import com.wicked.entitypurger.configuration.ConfigLoadResult;
import com.wicked.entitypurger.configuration.ConfigManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityPurgeReload extends CommandBase {
    private final EntityPurger entityPurger;

    public EntityPurgeReload(EntityPurger entityPurger){
        super();
        this.entityPurger = entityPurger;
    }

    @Override
    @Nonnull
    public String getName() {
        return "reload";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.wicked.entitypurge.reload";
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
        ConfigLoadResult loadResult = entityPurger.getConfigManager().reload();

        if(loadResult.isSuccessful()){
            TextComponentString successfulReload = new TextComponentString("Refreshed config!");
            successfulReload.setStyle(new Style().setColor(TextFormatting.GREEN));
            sender.sendMessage(successfulReload);
        }else{
            Style errorStyle = new Style().setColor(TextFormatting.RED);
            TextComponentString failedToRefresh = new TextComponentString("Failed to refresh config!");
            TextComponentString failReason = new TextComponentString(String.format("Reason: %s", loadResult.getErrorMessage()));
            failedToRefresh.setStyle(errorStyle);
            failReason.setStyle(errorStyle);

            sender.sendMessage(failedToRefresh);
            sender.sendMessage(failReason);
        }

    }
}
