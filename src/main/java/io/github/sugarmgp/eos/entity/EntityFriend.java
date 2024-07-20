package io.github.sugarmgp.eos.entity;

import com.google.common.base.Predicate;
import io.github.sugarmgp.eos.handler.ItemHandler;
import io.github.sugarmgp.eos.util.EnumFriendMembers;
import io.github.sugarmgp.eos.util.EnumFriendRanks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class EntityFriend extends TameableEntity implements IAngerable {
    private static final DataParameter<Integer> RANK = EntityDataManager.createKey(EntityFriend.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> MEMBER = EntityDataManager.createKey(EntityFriend.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ANGER_TIME = EntityDataManager.createKey(EntityFriend.class, DataSerializers.VARINT);
    private static final RangedInteger RANGED_TICK = TickRangeConverter.convertRange(20, 39);
    private UUID angerTarget;

    public EntityFriend(EntityType<? extends TameableEntity> typeIn, World worldIn) {
        super(typeIn, worldIn);
        this.setTamed(false);
    }

    public static AttributeModifierMap.MutableAttribute createDefaultAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.7D)
                .createMutableAttribute(Attributes.MAX_HEALTH, 30.0D)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.625D, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 0.525D, 8, 2, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomWalkingGoal(this, 0.31D));
        this.goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 5));
        this.goalSelector.addGoal(6, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setCallsForHelp());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, LivingEntity.class, 7, true, false, new Predicate<LivingEntity>() {
            public boolean apply(@Nullable LivingEntity entity) {
                return entity instanceof MonsterEntity && !(entity instanceof CreeperEntity) && !entity.isInvisible(); //选择怪物进行攻击
            }
        }));
        this.targetSelector.addGoal(5, new ResetAngerGoal(this, true));
    }

    @Override
    public ActionResultType func_230254_b_(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        Item item = itemStack.getItem();
        if (item.equals(ItemHandler.itemFunnyApple.get())) {
            if (this.isTamed()) {
                if (Objects.equals(this.getOwner(), player)) {
                    if (this.getHealth() < this.getMaxHealth()) {
                        if (this.world.isRemote) {
                            this.playEffect(ParticleTypes.HEART, this.getPosX(), this.getPosY() + 0.35, this.getPosZ(), 4, 2.2);
                            return ActionResultType.SUCCESS;
                        }
                        if (!player.isCreative()) {
                            itemStack.shrink(1);
                        }
                        int heal = item.getFood().getHealing();
                        this.heal(heal);
                        return ActionResultType.SUCCESS;
                    }
                }
            } else if (!this.func_233678_J__()) {
                if (this.world.isRemote) {
                    this.playEffect(ParticleTypes.HAPPY_VILLAGER, this.getPosX(), this.getPosY() + 0.4, this.getPosZ(), 8, 2.4);
                    return ActionResultType.SUCCESS;
                }
                if (!player.isCreative()) {
                    itemStack.shrink(1);
                }
                if (!net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                    this.setTamedBy(player);
                    this.navigator.clearPath();
                    this.setAttackTarget(null);
                }
                return ActionResultType.SUCCESS;
            }
        }
        return super.func_230254_b_(player, hand);
    }

    @OnlyIn(Dist.CLIENT)
    protected void playEffect(BasicParticleType particleTypes, Double posX, Double posY, Double posZ, int times, double offset) {
        for (int i = 1; i <= times; ++i) {
            this.world.addParticle(particleTypes,
                    posX + this.rand.nextDouble() * this.getWidth() * offset - this.getWidth(),
                    posY + this.rand.nextDouble() * this.getHeight() * 0.8,
                    posZ + this.rand.nextDouble() * this.getWidth() * offset - this.getWidth(),
                    this.rand.nextGaussian() * 0.015,
                    this.rand.nextGaussian() * 0.015,
                    this.rand.nextGaussian() * 0.015
            );
        }
    }

    @Override
    public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner) {
        if ((target instanceof CreeperEntity) || (target instanceof GhastEntity) || (target instanceof EnderDragonEntity)) {
            return false;
        }
        if (target instanceof EntityFriend) {
            return false;
        }
        if (target instanceof PlayerEntity) {
            return false;
        }
        return !(target instanceof TameableEntity) || !((TameableEntity) target).isTamed();
    }

    @Override
    public void livingTick() {
        super.livingTick();

        this.updateArmSwingProgress();

        BasicParticleType particleType = this.getRank().getParticleType();
        if (this.world.isRemote && particleType != null && (this.getMotion().x != 0.0D || this.getMotion().z != 0.0D)) { //在移动时播放粒子效果
            this.playEffect(particleType, this.getPosX(), this.getPosY() - 0.5, this.getPosZ(), 1, 1.6);
        }

        int regenerationLevel = this.getRank().getRegenerationLevel();
        if (regenerationLevel >= 0 && !this.isPotionActive(Effects.REGENERATION)) { //给NPC添加生命恢复
            this.addPotionEffect(new EffectInstance(Effects.REGENERATION, 72000, regenerationLevel, false, false));
        }

        if (!this.world.isRemote) {
            this.func_241359_a_((ServerWorld) this.world, true);
        }
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (RANK.equals(key)) {
            this.applyRank();
        }
        if (MEMBER.equals(key)) {
            this.applyMember();
        }
        super.notifyDataManagerChange(key);
    }

    protected void setItem(Item handIn, Item feetIn) {
        ItemStack hand = new ItemStack(handIn);
        ItemStack feet = new ItemStack(feetIn);
        hand.getTag().putBoolean("Unbreakable", true);
        feet.getTag().putBoolean("Unbreakable", true);
        hand.addEnchantment(Enchantments.UNBREAKING, 100);
        feet.addEnchantment(Enchantments.UNBREAKING, 100);
        this.setItemStackToSlot(EquipmentSlotType.MAINHAND, hand);
        this.setItemStackToSlot(EquipmentSlotType.FEET, feet);
    }

    @Override
    protected void spawnDrops(DamageSource damageSourceIn) {
        //在掉落时替换成无附魔的物品
        this.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(this.getRank().getHand()));
        this.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(this.getRank().getFeet()));
        super.spawnDrops(damageSourceIn);
    }

    @Override
    protected int getExperiencePoints(PlayerEntity player) {
        return this.experienceValue + (int) (this.rand.nextInt(10) / 9.0 * 5);
    }

    public EnumFriendRanks getRank() {
        return EnumFriendRanks.getByKey(this.dataManager.get(RANK));
    }

    protected void applyRank() {
        EnumFriendRanks rank = this.getRank();
        this.experienceValue = rank.getExperienceValue();
        this.setItem(rank.getHand(), rank.getFeet());
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(rank.getMaxHealth());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(rank.getAttackDamage());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(rank.getMovementSpeed());
    }

    public EnumFriendMembers getMember() {
        return EnumFriendMembers.getByKey(this.dataManager.get(MEMBER));
    }

    protected void applyMember() {
        EnumFriendMembers member = this.getMember();
        this.setCustomName(new StringTextComponent(member.name()));
    }

    @Override
    public EntityFriend func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
        return null;
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(RANK, 0);
        this.dataManager.register(MEMBER, 0);
        this.dataManager.register(ANGER_TIME, 0);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.dataManager.set(RANK, compound.getInt("rank"));
        this.dataManager.set(MEMBER, compound.getInt("member"));
        if (!this.world.isRemote) {
            this.readAngerNBT((ServerWorld) this.world, compound);
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("rank", this.dataManager.get(RANK));
        compound.putInt("member", this.dataManager.get(MEMBER));
        this.writeAngerNBT(compound);
    }

    @Override
    public int getAngerTime() {
        return this.dataManager.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int time) {
        this.dataManager.set(ANGER_TIME, time);
    }

    @Override
    public void func_230258_H__() {
        this.setAngerTime(RANGED_TICK.getRandomWithinRange(this.rand));
    }

    @Override
    @Nullable
    public UUID getAngerTarget() {
        return this.angerTarget;
    }

    @Override
    public void setAngerTarget(@Nullable UUID target) {
        this.angerTarget = target;
    }

    @Override
    @Nullable
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        if (spawnDataIn == null) {
            spawnDataIn = new AgeableEntity.AgeableData(false);
        }
        this.dataManager.set(RANK, EnumFriendRanks.randomGetKey(this.rand));
        this.dataManager.set(MEMBER, EnumFriendMembers.randomGetKey(this.rand));
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
