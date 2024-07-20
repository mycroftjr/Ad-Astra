package earth.terrarium.ad_astra.client.registry;

import earth.terrarium.ad_astra.AdAstra;
import earth.terrarium.ad_astra.client.AdAstraClient;
import earth.terrarium.ad_astra.common.config.SpaceSuitConfig;
import earth.terrarium.ad_astra.common.constants.ConstantComponents;
import earth.terrarium.ad_astra.common.entity.vehicle.Rocket;
import earth.terrarium.ad_astra.common.item.armor.JetSuit;
import earth.terrarium.ad_astra.common.networking.NetworkHandling;
import earth.terrarium.ad_astra.common.networking.packet.client.JetSuitFlightEnabledPacket;
import earth.terrarium.ad_astra.common.networking.packet.client.KeybindPacket;
import earth.terrarium.ad_astra.common.networking.packet.client.LaunchRocketPacket;
import earth.terrarium.ad_astra.common.util.ModKeyBindings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ClientModKeybindings {

    public static boolean clickingJump;
    public static boolean clickingSprint;
    public static boolean clickingForward;
    public static boolean clickingBack;
    public static boolean clickingLeft;
    public static boolean clickingRight;

    private static boolean sentJumpPacket;
    private static boolean sentSprintPacket;
    private static boolean sentForwardPacket;
    private static boolean sentBackPacket;
    private static boolean sentLeftPacket;
    private static boolean sentRightPacket;

    private static boolean jetSuitFlightSent;

    public static void onStartTick(Minecraft minecraft) {

        clickingJump = minecraft.options.keyJump.isDown();
        clickingSprint = minecraft.options.keySprint.isDown();
        clickingForward = minecraft.options.keyUp.isDown();
        clickingBack = minecraft.options.keyDown.isDown();
        clickingLeft = minecraft.options.keyLeft.isDown();
        clickingRight = minecraft.options.keyRight.isDown();

        if (minecraft.level != null) {

            if (clickingJump && sentJumpPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.JUMP, true));
                sentJumpPacket = false;
            }

            if (clickingSprint && sentSprintPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.SPRINT, true));
                sentSprintPacket = false;
            }

            if (clickingForward && sentForwardPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.FORWARD, true));
                sentForwardPacket = false;
            }

            if (clickingBack && sentBackPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.BACK, true));
                sentBackPacket = false;
            }

            if (clickingLeft && sentLeftPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.LEFT, true));
                sentLeftPacket = false;
            }

            if (clickingRight && sentRightPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.RIGHT, true));
                sentRightPacket = false;
            }

            if (!clickingJump && !sentJumpPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.JUMP, false));
                sentJumpPacket = true;
            }

            if (!clickingSprint && !sentSprintPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.SPRINT, false));
                sentSprintPacket = true;
            }

            if (!clickingForward && !sentForwardPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.FORWARD, false));
                sentForwardPacket = true;
            }

            if (!clickingBack && !sentBackPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.BACK, false));
                sentBackPacket = true;
            }

            if (!clickingLeft && !sentLeftPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.LEFT, false));
                sentLeftPacket = true;
            }

            if (!clickingRight && !sentRightPacket) {
                NetworkHandling.CHANNEL.sendToServer(new KeybindPacket(KeybindPacket.Keybind.RIGHT, false));
                sentRightPacket = true;
            }

            LocalPlayer player = minecraft.player;
            if (player == null) return;

            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest.getItem() instanceof JetSuit jetSuit) {
                if (AdAstraClient.KEY_TOGGLE_SUIT_FLIGHT.consumeClick()) {
                    SpaceSuitConfig.enableJetSuitFlight = !SpaceSuitConfig.enableJetSuitFlight;
                    Minecraft.getInstance().tell(() -> AdAstra.CONFIGURATOR.saveConfig(SpaceSuitConfig.class));
                    player.displayClientMessage(SpaceSuitConfig.enableJetSuitFlight ? ConstantComponents.SUIT_FLIGHT_ENABLED : ConstantComponents.SUIT_FLIGHT_DISABLED, true);
                    ModKeyBindings.setSuitFlightEnabled(player, SpaceSuitConfig.enableJetSuitFlight);
                    NetworkHandling.CHANNEL.sendToServer(new JetSuitFlightEnabledPacket(SpaceSuitConfig.enableJetSuitFlight));
                } else if (!jetSuitFlightSent) {
                    ModKeyBindings.setSuitFlightEnabled(player, SpaceSuitConfig.enableJetSuitFlight);
                    NetworkHandling.CHANNEL.sendToServer(new JetSuitFlightEnabledPacket(SpaceSuitConfig.enableJetSuitFlight));
                    jetSuitFlightSent = true;
                }
            }
        }
    }

    public static void launchRocket() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            if (minecraft.player.getVehicle() instanceof Rocket rocket && !rocket.isFlying()) {
                NetworkHandling.CHANNEL.sendToServer(new LaunchRocketPacket());
            }
        }
    }

    public static void init() {
        jetSuitFlightSent = false;
    }
}
