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
import java.util.Map;

public class EntityPurgeForce extends CommandBase {
    private final EntityPurger entityPurger;

    public EntityPurgeForce(EntityPurger entityPurger){
        super();
        this.entityPurger = entityPurger;
    }

    @Override
    @Nonnull
    public String getName() {
        return "force";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.wicked.entitypurge.force";
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
        Map<String, Integer> purgedEntities = entityPurger.getEntityPurgeRunner().run();
        if(purgedEntities.size() > 0){
            TextComponentString enabled = new TextComponentString("Force purge result:\n");
            enabled.setStyle(new Style().setUnderlined(true).setColor(TextFormatting.GREEN));
            sender.sendMessage(enabled);

            for(String entityType : purgedEntities.keySet()){
                Integer numberPurged = purgedEntities.get(entityType);
                TextComponentString purgedEntity = new TextComponentString(String.format("Purged %d of %s", numberPurged, entityType));
                purgedEntity.setStyle(new Style().setColor(TextFormatting.DARK_GREEN));
                sender.sendMessage(purgedEntity);
            }
        }else{
            TextComponentString noPurge = new TextComponentString("No entities were purged!");
            noPurge.setStyle(new Style().setUnderlined(true).setColor(TextFormatting.RED));
            sender.sendMessage(noPurge);
        }
    }
}
