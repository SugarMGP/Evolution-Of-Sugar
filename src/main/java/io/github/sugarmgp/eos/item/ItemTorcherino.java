package io.github.sugarmgp.eos.item;

import io.github.sugarmgp.eos.EOS;
import io.github.sugarmgp.eos.handler.ConfigHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.List;

public class ItemTorcherino extends Item {
    public ItemTorcherino() {
        super(new Properties().group(EOS.ITEMGROUP));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        playerIn.swingArm(handIn);
        if (!worldIn.isRemote) {
            int count = playerIn.getHeldItemMainhand().getCount();
            playerIn.setHeldItem(handIn, ItemStack.EMPTY);
            playerIn.sendStatusMessage(new TranslationTextComponent("message.torcherino.use"), false);
            playerIn.sendStatusMessage(new TranslationTextComponent("message.torcherino.use2"), false);
            if (!ConfigHandler.torcherinoExploding.get()) {
                playerIn.attackEntityFrom(DamageSource.GENERIC, 40 * count);
            } else {
                //手上的火把越多，爆炸威力越强
                playerIn.attackEntityFrom(DamageSource.causeExplosionDamage(worldIn.createExplosion(playerIn, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), 2 * count + 6, Explosion.Mode.BREAK)), 40 * count);
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("tooltip.eos.torcherino"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
