package me.mogubea.chat;

import io.papermc.paper.chat.ChatRenderer;
import me.mogubea.profile.PlayerProfile;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoguChatRenderer implements ChatRenderer {

    @Override
    public @NotNull Component render(@NotNull Player player, @NotNull Component playerName, @NotNull Component message, @NotNull Audience audience) {
        PlayerProfile profile = PlayerProfile.from(player);

        return Component.empty()
                .append(profile.getColouredName()
                        .hoverEvent(HoverEvent.showText(Component.text(player.getUniqueId().toString())))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + profile.getDisplayName())))
                .append(Component.text(" » ", TextColor.color(0x59556A)))
                .append(message.color(TextColor.color(0xefefef)));
    }

}
