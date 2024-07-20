package earth.terrarium.ad_astra.common.item.armor;

import dev.architectury.injectables.annotations.PlatformOnly;
import earth.terrarium.ad_astra.AdAstra;
import earth.terrarium.ad_astra.client.screen.PlayerOverlayScreen;
import earth.terrarium.ad_astra.common.config.SpaceSuitConfig;
import earth.terrarium.ad_astra.common.registry.ModItems;
import earth.terrarium.ad_astra.common.util.ModKeyBindings;
import earth.terrarium.ad_astra.common.util.ModUtils;
import earth.terrarium.botarium.api.energy.EnergyHooks;
import earth.terrarium.botarium.api.energy.EnergyItem;
import earth.terrarium.botarium.api.energy.ItemEnergyContainer;
import earth.terrarium.botarium.api.item.ItemStackHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JetSuit extends NetheriteSpaceSuit implements EnergyItem {

    private boolean isFallFlying;
    private boolean emitParticles;

    public JetSuit(ArmorMaterial material, EquipmentSlot slot, Properties properties) {
        super(material, slot, properties);
    }

    public void spawnParticles(Level level, LivingEntity entity, HumanoidModel<LivingEntity> model) {
        if (!SpaceSuitConfig.spawnJetSuitParticles || !emitParticles) return;

        spawnParticles(level, entity, model.rightArm.xRot + 0.05, entity.isFallFlying() ? 0.0 : 0.8, -0.45);
        spawnParticles(level, entity, model.leftArm.xRot + 0.05, entity.isFallFlying() ? 0.0 : 0.8, 0.45);

        spawnParticles(level, entity, model.rightLeg.xRot + 0.05, entity.isFallFlying() ? 0.1 : 0.0, -0.1);
        spawnParticles(level, entity, model.leftLeg.xRot + 0.05, entity.isFallFlying() ? 0.1 : 0.0, 0.1);
    }

    // Spawns particles at the limbs of the player
    private void spawnParticles(Level level, LivingEntity entity, double pitch, double yOffset, double zOffset) {
        double yaw = entity.yBodyRot;
        double xRotator = Math.cos(yaw * Math.PI / 180.0) * zOffset;
        double zRotator = Math.sin(yaw * Math.PI / 180.0) * zOffset;
        double xRotator1 = Math.cos((yaw - 90) * Math.PI / 180.0) * pitch;
        double zRotator1 = Math.sin((yaw - 90) * Math.PI / 180.0) * pitch;

        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, true, entity.getX() + xRotator + xRotator1, entity.getY() + yOffset, entity.getZ() + zRotator1 + zRotator, 0.0, 0.0, 0.0);
    }

    public static boolean hasFullSet(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (!(stack.getItem() instanceof JetSuit)) {
                return false;
            }
        }
        return true;
    }

    public static void updateBatteryOverlay(ItemStack suit) {
        var energy = EnergyHooks.getItemEnergyManager(suit);
        PlayerOverlayScreen.batteryRatio = energy.getStoredEnergy() / (double) energy.getCapacity();
    }

    @Override
    public long getTankSize() {
        return SpaceSuitConfig.jetSuitTankSize;
    }

    // Display energy
    @Override
    public void appendHoverText(ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        if (stack.is(ModItems.JET_SUIT.get())) {
            long energy = EnergyHooks.getItemEnergyManager(stack).getStoredEnergy();
            tooltip.add(Component.translatable("gauge_text.ad_astra.storage", energy, SpaceSuitConfig.jetSuitMaxEnergy).setStyle(Style.EMPTY.withColor(energy > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)));
        }
    }

    public void fly(Player player, ItemStack stack) {
        emitParticles = false;
        if (!SpaceSuitConfig.enableJetSuitFlight || !ModKeyBindings.suitFlightEnabled(player)) {
            return;
        }

        // Don't fly the Jet Suit in creative
        ItemStackHolder stackHolder = new ItemStackHolder(stack);
        if (player.getAbilities().flying) {
            return;
        }

        // Don't fly if the Jet Suit has no energy
        if (EnergyHooks.getItemEnergyManager(stack).getStoredEnergy() <= 0) {
            return;
        }

        if (ModKeyBindings.sprintKeyDown(player)) {
            this.fallFly(player, stackHolder);
        } else {
            this.flyUpward(player, stackHolder);
        }

        if (isFallFlying) {
            if (!player.isFallFlying()) {
                player.startFallFlying();
            }
        } else {
            if (player.isFallFlying()) {
                player.stopFallFlying();
            }
        }
        emitParticles = true;
        if (stackHolder.isDirty()) player.setItemSlot(EquipmentSlot.CHEST, stackHolder.getStack());
        ModUtils.sendUpdatePacket((ServerPlayer) player);
    }

    public void flyUpward(Player player, ItemStackHolder stack) {
        if (EnergyHooks.isEnergyItem(stack.getStack())) {
            player.fallDistance /= 2;

            var energy = EnergyHooks.getItemEnergyManager(stack.getStack());
            long tickEnergy = SpaceSuitConfig.jetSuitEnergyPerTick;
            if (!player.isCreative()) {
                energy.extract(stack, tickEnergy, false);
            }
            isFallFlying = false;

            double speed = SpaceSuitConfig.jetSuitUpwardsSpeed;
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, speed, 0.0));
            if (player.getDeltaMovement().y() > speed) {
                player.setDeltaMovement(player.getDeltaMovement().x(), speed, player.getDeltaMovement().z());
            }
        }
    }

    public void fallFly(Player player, ItemStackHolder stack) {
        if (player.isOnGround()) {
            player.fallDistance /= 2;
        }
        var energy = EnergyHooks.getItemEnergyManager(stack.getStack());
        long tickEnergy = SpaceSuitConfig.jetSuitEnergyPerTick;
        if (!player.isCreative()) {
            energy.extract(stack, tickEnergy, false);
        }
        isFallFlying = true;

        double speed = SpaceSuitConfig.jetSuitSpeed - (ModUtils.getEntityGravity(player) * 0.25);
        Vec3 rotationVector = player.getLookAngle().scale(speed);
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.add(rotationVector.x() * 0.1 + (rotationVector.x() * 1.5 - velocity.x()) * 0.5, rotationVector.y() * 0.1 + (rotationVector.y() * 1.5 - velocity.y()) * 0.5, rotationVector.z() * 0.1 + (rotationVector.z() * 1.5 - velocity.z()) * 0.5));
    }

    @Override
    public ItemEnergyContainer getEnergyStorage(ItemStack itemStack) {
        if (!itemStack.is(ModItems.JET_SUIT.get())) return new ItemEnergyContainer(itemStack, 0);
        return new ItemEnergyContainer(itemStack, SpaceSuitConfig.jetSuitMaxEnergy) {
            @Override
            public long maxInsert() {
                return 512;
            }

            @Override
            public long maxExtract() {
                return 256;
            }
        };
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (!slot.equals(EquipmentSlot.CHEST)) {
            return new ResourceLocation(AdAstra.MOD_ID, "textures/entity/armour/jet_suit/jet_suit_5.png").toString();
        }
        if (stack.getItem() instanceof JetSuit) {
            var energy = EnergyHooks.getItemEnergyManager(stack);
            return new ResourceLocation(AdAstra.MOD_ID, "textures/entity/armour/jet_suit/jet_suit_" + (energy.getStoredEnergy() <= 0 ? 0 : ((int) Math.min((energy.getStoredEnergy() * 5 / Math.max(1, energy.getCapacity())) + 1, 5))) + ".png").toString();
        }
        return new ResourceLocation(AdAstra.MOD_ID, "textures/entity/armour/jet_suit/jet_suit_5.png").toString();
    }

    public void setFallFlying(boolean fallFlying) {
        isFallFlying = fallFlying;
    }

    public boolean setEmitParticles(boolean emitParticles) {
        return this.emitParticles = emitParticles;
    }

    @PlatformOnly("forge")
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!entity.level.isClientSide) {
            ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof JetSuit) {
                int nextFlightTick = flightTicks + 1;
                if (nextFlightTick % 10 == 0) {
                    if (nextFlightTick % 20 == 0) {
                        stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.CHEST));
                    }
                    entity.gameEvent(GameEvent.ELYTRA_GLIDE);
                }
            }
        }
        return true;
    }

    @PlatformOnly("forge")
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return this.isFallFlying;
    }
}