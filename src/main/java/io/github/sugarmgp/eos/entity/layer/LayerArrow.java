package io.github.sugarmgp.eos.entity.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.sugarmgp.eos.entity.model.ModelFriend;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerArrow<T extends LivingEntity, M extends ModelFriend<T>> extends LayerStuckInBody<T, M> {
    private final EntityRendererManager rendererManager;
    private ArrowEntity arrowEntity;

    public LayerArrow(LivingRenderer rendererIn) {
        super(rendererIn);
        this.rendererManager = rendererIn.getRenderManager();
    }

    protected int func_225631_a_(LivingEntity entity) {
        return entity.getArrowCountInEntity();
    }

    protected void func_225632_a_(MatrixStack p_225632_1_, IRenderTypeBuffer p_225632_2_, int p_225632_3_, Entity p_225632_4_, float p_225632_5_, float p_225632_6_, float p_225632_7_, float p_225632_8_) {
        float f = MathHelper.sqrt(p_225632_5_ * p_225632_5_ + p_225632_7_ * p_225632_7_);
        this.arrowEntity = new ArrowEntity(p_225632_4_.world, p_225632_4_.getPosX(), p_225632_4_.getPosY(), p_225632_4_.getPosZ());
        this.arrowEntity.rotationYaw = (float) (Math.atan2(p_225632_5_, p_225632_7_) * (double) (180F / (float) Math.PI));
        this.arrowEntity.rotationPitch = (float) (Math.atan2(p_225632_6_, f) * (double) (180F / (float) Math.PI));
        this.arrowEntity.prevRotationYaw = this.arrowEntity.rotationYaw;
        this.arrowEntity.prevRotationPitch = this.arrowEntity.rotationPitch;
        this.rendererManager.renderEntityStatic(this.arrowEntity, 0.0D, 0.0D, 0.0D, 0.0F, p_225632_8_, p_225632_1_, p_225632_2_, p_225632_3_);
    }
}
