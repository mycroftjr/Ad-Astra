package earth.terrarium.ad_astra.common.networking.packet.client;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.ad_astra.AdAstra;
import earth.terrarium.ad_astra.common.util.ModKeyBindings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record JetSuitFlightEnabledPacket(boolean enabled) implements Packet<JetSuitFlightEnabledPacket> {

    public static final ResourceLocation ID = new ResourceLocation(AdAstra.MOD_ID, "jet_suit_flight_enabled_packet");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<JetSuitFlightEnabledPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<JetSuitFlightEnabledPacket> {
        @Override
        public void encode(JetSuitFlightEnabledPacket packet, FriendlyByteBuf buf) {
            buf.writeBoolean(packet.enabled);
        }

        @Override
        public JetSuitFlightEnabledPacket decode(FriendlyByteBuf buf) {
            return new JetSuitFlightEnabledPacket(buf.readBoolean());
        }

        @Override
        public PacketContext handle(JetSuitFlightEnabledPacket packet) {
            return (player, level) -> {
                ModKeyBindings.setSuitFlightEnabled(player, packet.enabled());
            };
        }
    }
}
