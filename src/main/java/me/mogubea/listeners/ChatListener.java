package me.mogubea.listeners;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.mogubea.chat.Emote;
import me.mogubea.chat.EmoteManager;
import me.mogubea.chat.MoguChatRenderer;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import me.mogubea.statistics.PlayerStatistics;
import me.mogubea.statistics.SimpleStatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;

public class ChatListener extends EventListener {

    private final ChatRenderer chatRenderer;
    private final EmoteManager emoteManager;

    protected ChatListener(Main plugin) {
        super(plugin);

        chatRenderer = new MoguChatRenderer();
        emoteManager = new EmoteManager();
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        e.renderer(chatRenderer);

        final PlayerStatistics stats = PlayerProfile.from(e.getPlayer()).getStats();
        final String content = ((TextComponent)e.message()).content();
        final Set<Player> pinged = new HashSet<>();

        // @Name and Emotes
        if (content.contains("@") || content.contains(":")) {
            final String[] spaceSplit = ((TextComponent)e.message()).content().split(" ");
            Component newMessage = Component.text("");
            for (String word : spaceSplit) {
                if (word.startsWith(":") && word.endsWith(":") && word.length() > 2) { // Emote Check
                    Emote emote = emoteManager.getEmote(word);
                    if (emote != null) {
                        newMessage = newMessage.append(Component.text(emote.getUnicode(), NamedTextColor.WHITE)
                                .hoverEvent(HoverEvent.showText(Component.text(word.toLowerCase(), NamedTextColor.WHITE))));

                        stats.addToStat(SimpleStatType.CHAT_EMOTE, emote.getIdentifier(), 1);
                        stats.addToStat(SimpleStatType.CHAT_EMOTE, "TOTAL", 1);
                        continue;
                    }
                } else if (word.startsWith("@")) { // Handle looking for pings within the message.
                    Player ping = getPlugin().searchForPlayer(word.substring(1));
                    if (ping != null) {
                        PlayerProfile prof = PlayerProfile.from(ping);
                        if (pinged.add(ping))
                            ping.playSound(ping.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.3F, 0.7F);
                        newMessage = newMessage.append(Component.text("@").color(prof.getNameColour()).append(prof.getColouredName()).append(Component.text(" ")));
                        continue;
                    }
                }

                newMessage = newMessage.append(Component.text(word + " "));
            }

            e.message(newMessage);
        }
    }

}
