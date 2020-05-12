package com.wicked.entitypurger.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;

public class EntityNameplate extends Entity {
    private final Entity nameplateFor;

    public EntityNameplate(World worldIn, String name, Entity nameplateFor, double x, double y, double z) {
        super(worldIn);
        this.setSize(3, 1);
        this.setPosition(x, y, z);
        this.nameplateFor = nameplateFor;

        centerOnNameplateEntity();
    }

    private void centerOnNameplateEntity(){
        AxisAlignedBB boundingBox = nameplateFor.getEntityBoundingBox();
        Vec3d boundingBoxCenter = boundingBox.getCenter();
        double y = nameplateFor.getEntityBoundingBox().maxY + 1f;
        this.setPosition(boundingBoxCenter.x, y, boundingBoxCenter.z);
    }

    public void onUpdate()
    {
        super.onUpdate();

        if(Objects.isNull(nameplateFor) || nameplateFor.isDead){
            this.setDead();
        }else{
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            centerOnNameplateEntity();
        }
    }

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {}

    public Entity getNameplateFor() {
        return nameplateFor;
    }
}
