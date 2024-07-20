package earth.terrarium.ad_astra.common.constants;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ConstantComponents {
    public static final Component TOGGLE_SUIT_FLIGHT_KEY = Component.translatable("key.ad_astra.toggle_suit_flight");

    public static final Component AD_ASTRA_CATEGORY = Component.translatable("key.categories.ad_astra");

    public static final Component SUIT_FLIGHT_ENABLED = Component.translatable("message.ad_astra.suit_flight_enabled").withStyle(ChatFormatting.GOLD);
    public static final Component SUIT_FLIGHT_DISABLED = Component.translatable("message.ad_astra.suit_flight_disabled").withStyle(ChatFormatting.GOLD);
}
