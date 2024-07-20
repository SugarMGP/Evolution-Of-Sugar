package io.github.sugarmgp.eos.entity.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.sugarmgp.eos.entity.model.ModelFriend;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public abstract class LayerStuckInBody<T extends LivingEntity, M extends ModelFriend<T>> extends LayerRenderer<T, M> {
    public LayerStuckInBody(LivingRenderer livingRenderer) {
        super(livingRenderer);
    }

    protected abstract int func_225631_a_(LivingEntity entity);

    protected abstract void func_225632_a_(MatrixStack p_225632_1_, IRenderTypeBuffer p_225632_2_, int p_225632_3_, Entity p_225632_4_, float p_225632_5_, float p_225632_6_, float p_225632_7_, float p_225632_8_);

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T livingEntityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        int i = this.func_225631_a_(livingEntityIn);
        Random random = new Random(livingEntityIn.getEntityId());
        if (i > 0) {
            for (int j = 0; j < i; ++j) {
                matrixStackIn.push();
                ModelRenderer modelRenderer = this.getEntityModel().getRandomModelRenderer(random);
                ModelRenderer.ModelBox modelRenderer$modelBox = modelRenderer.getRandomCube(random);
                modelRenderer.translateRotate(matrixStackIn);
                float f = random.nextFloat();
                float f1 = random.nextFloat();
                float f2 = random.nextFloat();
                float f3 = MathHelper.lerp(f, modelRenderer$modelBox.posX1, modelRenderer$modelBox.posX2) / 16.0F;
                float f4 = MathHelper.lerp(f1, modelRenderer$modelBox.posY1, modelRenderer$modelBox.posY2) / 16.0F;
                float f5 = MathHelper.lerp(f2, modelRenderer$modelBox.posZ1, modelRenderer$modelBox.posZ2) / 16.0F;
                matrixStackIn.translate(f3, f4, f5);
                f = -1.0F * (f * 2.0F - 1.0F);
                f1 = -1.0F * (f1 * 2.0F - 1.0F);
                f2 = -1.0F * (f2 * 2.0F - 1.0F);
                this.func_225632_a_(matrixStackIn, bufferIn, packedLightIn, livingEntityIn, f, f1, f2, partialTicks);
                matrixStackIn.pop();
            }
        }
    }
}
