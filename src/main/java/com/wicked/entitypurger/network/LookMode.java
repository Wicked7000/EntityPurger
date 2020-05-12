package com.wicked.entitypurger.network;

import com.wicked.entitypurger.EntityPurger;
import com.wicked.entitypurger.command.look.EntityPurgeLook;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class LookMode implements IMessageHandler<LookMode.LookModeRequest, LookMode.LookModeRequest> {
    private final EntityPurger entityPurger;

    public LookMode(EntityPurger entityPurger){
        this.entityPurger = entityPurger;
    }

    @Override
    public LookModeRequest onMessage(LookModeRequest message, MessageContext ctx) {
        if(ctx.side.isClient()){
            Minecraft mcInstance = Minecraft.getMinecraft();
            EntityPurgeLook.executeFromServer(mcInstance, entityPurger);
        }
        return null;
    }

    public static class LookModeRequest implements IMessage{
        @Override
        public void fromBytes(ByteBuf buf) { }

        @Override
        public void toBytes(ByteBuf buf) { }
    }
}
