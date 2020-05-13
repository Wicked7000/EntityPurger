package com.wicked.entitypurger;

import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class EntityPurgerTaskManager {

    private final Logger logger;
    private final Minecraft mcInstance;
    private final List<ListenableFuture<Object>> runningTasks;

    public EntityPurgerTaskManager(Logger logger){
        this.logger = logger;
        this.mcInstance = Minecraft.getMinecraft();
        this.runningTasks = new ArrayList<>();
    }

    public void registerTask(Runnable task){
        runningTasks.add(mcInstance.addScheduledTask(task));
    }

    public void destroyAllTasks(){
        logger.info("Cancelling all current tasks");
        for(ListenableFuture<Object> task : runningTasks){
            task.cancel(true);
        }
    }
}