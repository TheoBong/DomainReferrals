package com.bongbong.referrals.listeners;

import com.bongbong.referrals.ReferralsPlugin;
import com.bongbong.referrals.utils.ThreadUtil;
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
        ThreadUtil.runTask(true, plugin, () -> {
            String hostName = event.getHostname();
            String[] splitHost = hostName.split("\\.");
            String subdomain = splitHost[0];

            plugin.getStorage().referCheck(event.getPlayer().getUniqueId(), subdomain, result -> {
                if (result == null) return;
                String resultString = (String) result;

                event.getPlayer().sendMessage(plugin.getConfig().getString("JOINREWARDS_GROUPS." + resultString + ".MESSAGE"));

                for(String command : plugin.getConfig().getStringList("JOIN_REWARDS_GROUPS." + resultString + ".COMMANDS")) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                }
            });
        });
    }
}
