package com.wicked.entitypurger.command;

import com.wicked.entitypurger.EntityPurger;
import com.wicked.entitypurger.command.list.EntityPurgeList;
import com.wicked.entitypurger.command.look.EntityPurgeLook;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;
import javax.annotation.Nonnull;

public class EntityPurgerCommand extends CommandTreeBase {

    public EntityPurgerCommand(EntityPurger entityPurger){
        super.addSubcommand(new EntityPurgeList(entityPurger));
        super.addSubcommand(new EntityPurgeReload(entityPurger));
        super.addSubcommand(new EntityPurgeDisable(entityPurger));
        super.addSubcommand(new EntityPurgeEnable(entityPurger));
        super.addSubcommand(new EntityPurgeStatus(entityPurger));
        super.addSubcommand(new EntityPurgeForce(entityPurger));
        super.addSubcommand(new EntityPurgeLook(entityPurger));
    }

    @Override
    @Nonnull
    public String getName() {
        return "entitypurge";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.wicked.entitypurge";
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
}
