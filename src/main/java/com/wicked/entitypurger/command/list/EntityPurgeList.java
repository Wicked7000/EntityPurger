package com.wicked.entitypurger.command.list;

import com.wicked.entitypurger.EntityHelper;
import com.wicked.entitypurger.EntityPurger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
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

public class EntityPurgeList extends CommandBase {
    private final EntityPurger entityPurger;

    public EntityPurgeList(EntityPurger entityPurger){
        super();
        this.entityPurger = entityPurger;
    }

    @Override
    @Nonnull
    public String getName() {
        return "list";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.wicked.entitypurge.list";
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
        for(int worldId : DimensionManager.getIDs()){
            World world = DimensionManager.getWorld(worldId);
            DimensionType type = DimensionManager.getProviderType(worldId);
            List<EntitySummary> entities = EntityHelper.buildListOfCurrentEntities(world);

            TextComponentString worldText;
            if(Objects.nonNull(type)){
                worldText = new TextComponentString(String.format("World: %s", type.getName()));

            }else{
                worldText = new TextComponentString("World: ??");
            }
            worldText.setStyle(new Style().setColor(TextFormatting.BOLD).setUnderlined(true));
            sender.sendMessage(worldText);

            for(int summaryIdx = 0; summaryIdx < entities.size(); summaryIdx++){
                EntitySummary summary = entities.get(summaryIdx);
                Entity entityType = summary.getEntity();

                String newLine = summaryIdx == entities.size()-1 ? "\n" : "";

                if(!EntityHelper.isPlayer(entityType) && !EntityHelper.isNameplate(entityType)){
                    TextComponentString entityAmount = new TextComponentString(
                            String.format("%s (%d)%s",
                                    summary.getEntityType(), summary.getCount(), newLine));
                    entityAmount.setStyle(new Style().setColor(TextFormatting.GRAY));
                    sender.sendMessage(entityAmount);
                }
            }
        }
    }
}
