package me.mogubea.listeners;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener extends EventListener {

    protected ConnectionListener(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onServerListPing(PaperServerListPingEvent e) {
        e.motd(Component.text("ᴍᴏɢᴜʙᴀɴɢʏ"));
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        try {
            PlayerProfile profile = getPlugin().getProfileManager().getPlayerProfile(e.getUniqueId(), e.getName());
            profile.setName(e.getName());
            e.getPlayerProfile().setName(profile.getDisplayName());
        } catch (Exception ex) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("There was a problem loading your Player Profile.", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
        PlayerProfile profile = PlayerProfile.from(e.getPlayer());
        profile.updateDisplayedNames(false);
        profile.getGuildData().refreshPlayer();
        profile.getManager().getTeamManager().initScoreboard(e.getPlayer());
        if (e.getPlayer().hasPlayedBefore())
            e.joinMessage(profile.getColouredName().append(Component.text(" joined the game.", NamedTextColor.YELLOW)));
        else
            e.joinMessage(profile.getColouredName().append(Component.text(" joined the game for the first time! (#"+profile.getId()+")", NamedTextColor.YELLOW)));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        e.getPlayer().closeInventory(InventoryCloseEvent.Reason.DISCONNECT);

        PlayerProfile profile = PlayerProfile.from(e.getPlayer());
        e.quitMessage(profile.getColouredName().append(Component.text(" left the game.", NamedTextColor.YELLOW)));
    }

}
