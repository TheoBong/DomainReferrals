package com.bongbong.referrals.commands.impl;

import com.bongbong.referrals.ReferralsPlugin;
import com.bongbong.referrals.commands.BaseCommand;
import com.bongbong.referrals.database.Result;
import com.bongbong.referrals.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReferCommand extends BaseCommand {
    private final ReferralsPlugin plugin;

    public ReferCommand(ReferralsPlugin plugin, String name, String... aliases) {
        super(name);
        this.plugin = plugin;
        this.setAliases(aliases);
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return;
        }

        Player player;
        switch (args[0]) {
            case "claim":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return;
                }
                player = (Player) sender;

                if (args[1] == null) {
                    player.sendMessage("You must input a subdomain to claim.");
                    return;
                }

                String[] separated = args[1].split("\\.");
                String subdomain = separated[0];

                plugin.getStorage().createDomain(player.getUniqueId(), subdomain, result -> {
                    if (result instanceof Boolean) {
                        boolean success = (boolean) result;
                        if (success) {
                            player.sendMessage("You have claimed the subdomain: " + subdomain + ".kitpvp.cc!");
                            player.sendMessage("You will be rewarded whenever a player joins using that.");
                        } else {
                            player.sendMessage("Someone has already claimed this subdomain.");
                        }
                    }
                });

                break;
            case "leaderboard":
                sender.sendMessage("Leaderboard is work in progress");
                break;
            case "rewards":
                sender.sendMessage("Rewards is work in progress");
                break;
            case "check":
                Player target;

                if (args.length < 2) {
                    if (sender instanceof Player) {
                        target = (Player) sender;
                    } else {
                        sender.sendMessage("You must specify a player.");
                        break;
                    }
                } else {
                    target = Bukkit.getPlayer(args[1]);
                }

                //TODO: offline player lookup via db
                if (target == null) {
                    break;
                }

                plugin.getStorage().getReferredList(target.getUniqueId(), result -> {
                    if (result == null) {
                        sender.sendMessage("Target has no referrals");
                        return;
                    }

                    if (result instanceof String) {
                        String referredString = (String) result;
                        String[] test = referredString.split(",");
                        List<String> referredList = new ArrayList<>(Arrays.asList(test));

                        referredList.forEach(sender::sendMessage);
                        sender.sendMessage("Player has referred " + referredList.size() + "players");
                    }
                });
                break;
            case "reset":
                sender.sendMessage("Reset is work in progress");
                break;
            case "reload":
                sender.sendMessage("Reload is work in progress");
                break;
            default:
                sendHelp(sender);
                break;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Colors.convertLegacyColors("&7&m-------------------------------------"));
        sender.sendMessage(Colors.convertLegacyColors("&a&lDOMAIN REFERRALS &7- &fhttps://github.com/TheoBong/Referrals/"));
        sender.sendMessage(Colors.convertLegacyColors(""));
        sender.sendMessage(Colors.convertLegacyColors("&f/refer claim <subdomain> &7- claim a subdomain as your own"));
        sender.sendMessage(Colors.convertLegacyColors("&f/refer leaderboard &7- check the leaderboard for referrals"));
        sender.sendMessage(Colors.convertLegacyColors("&f/refer rewards &7- claim your rewards for referring people"));
        sender.sendMessage(Colors.convertLegacyColors("&f/refer check [player] &7- check how many referrals someone has"));

        if (sender.hasPermission("refer.admin")) {
            sender.sendMessage(Colors.convertLegacyColors("&f/refer reset <uuid> &7- reset a players domain & referrals"));
            sender.sendMessage(Colors.convertLegacyColors("&f/refer reload &7- reloads the config file for domain referrals"));
        }

        sender.sendMessage(Colors.convertLegacyColors("&7&m-------------------------------------"));
    }
}
