package com.bongbong.referrals.listeners;

import com.bongbong.referrals.ReferralsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.logging.Level;

public class JoinListener implements Listener {
    private final ReferralsPlugin plugin;

    public JoinListener(ReferralsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        String hostName = event.getHostname();
        String[] splitHost = hostName.split("\\.");
        String subdomain = splitHost[0];

        plugin.getStorage().referCheck(event.getPlayer().getUniqueId(), subdomain);
    }
}
