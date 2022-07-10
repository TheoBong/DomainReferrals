package com.bongbong.referrals.commands.impl;

import com.bongbong.referrals.ReferralsPlugin;
import com.bongbong.referrals.commands.BaseCommand;
import com.bongbong.referrals.utils.Colors;
import com.bongbong.referrals.utils.ThreadUtil;
import com.bongbong.referrals.utils.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ReferCommand extends BaseCommand {
    private final ReferralsPlugin plugin;

    public ReferCommand(ReferralsPlugin plugin, String name, String... aliases) {
        super(name);
        this.plugin = plugin;
        this.setAliases(aliases);
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        ThreadUtil.runTask(true, plugin, () -> {
            if (args.length < 1) {
                sendHelp(sender);
                return;
            }

            Player player;
            Player target;
            UUID targetUUID;
            switch (args[0]) {
                case "create":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Only players can use this command.");
                        return;
                    }
                    player = (Player) sender;

                    if (args.length < 2) {
                        sendHelp(sender);
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
                case "rewards":
                    sender.sendMessage("Rewards is work in progress");
                    break;
                case "check":
                    if (args.length < 2) {
                        if (sender instanceof Player) {
                            target = (Player) sender;
                        } else {
                            sendHelp(sender);
                            break;
                        }
                    } else {
                        target = Bukkit.getPlayer(args[1]);
                    }

                    if (target == null) {
                        WebPlayer webPlayer = new WebPlayer(args[1]);
                        targetUUID = webPlayer.getUuid();
                    } else {
                        targetUUID = target.getUniqueId();
                    }

                    plugin.getStorage().getReferredList(targetUUID, result -> {
                        if (result == null) {
                            sender.sendMessage("Target has no referrals");
                            return;
                        }

                        String referredString = (String) result;
                        String[] test = referredString.split(",");
                        List<String> referredList = new ArrayList<>(Arrays.asList(test));

                        referredList.forEach(sender::sendMessage);
                        sender.sendMessage("Player has referred " + referredList.size() + "players");
                    });
                    break;
                case "checkdomain":
                    if (args.length < 2) {
                        sendHelp(sender);
                        break;
                    }

                    plugin.getStorage().checkDomain(args[1], result -> {
                        if (result == null) {
                            sender.sendMessage("Domain does not belong to anyone");
                            return;
                        }

                        UUID uuid = UUID.fromString((String) result);

                        WebPlayer webPlayer = new WebPlayer(uuid);
                        sender.sendMessage("This domain belongs to: " + webPlayer.getName());

                        plugin.getStorage().getReferredList(uuid, result1 -> {
                            if (result1 == null) {
                                sender.sendMessage("Target has no referrals");
                                return;
                            }

                            String referredString = (String) result1;
                            String[] test = referredString.split(",");
                            List<String> referredList = new ArrayList<>(Arrays.asList(test));

                            referredList.forEach(sender::sendMessage);
                            sender.sendMessage("Player has referred " + referredList.size() + "players");
                        });
                    });
                    break;
                case "reset":
                    if (!sender.hasPermission("refer.admin")) {
                        sender.sendMessage("No permission!");
                        return;
                    }

                    if (args.length < 2) {
                        sendHelp(sender);
                        break;
                    }

                    target = Bukkit.getPlayer(args[1]);

                    if (target == null) {
                        WebPlayer webPlayer = new WebPlayer(args[1]);
                        targetUUID = webPlayer.getUuid();
                    } else {
                        targetUUID = target.getUniqueId();
                    }

                    plugin.getStorage().resetReferrals(targetUUID, result -> {
                        boolean success = (boolean) result;
                        if (success) {
                            sender.sendMessage("Success!");
                        } else {
                            sender.sendMessage("Player has never joined before.");
                        }
                    });
                    break;
                case "reload":
                    plugin.reloadConfig();
                    sender.sendMessage("Success!");
                    break;
                case "setgroup":
                    if (!sender.hasPermission("refer.admin")) {
                        sender.sendMessage("No permission!");
                        return;
                    }

                    if (args.length < 3) {
                        sendHelp(sender);
                        break;
                    }

                    target = Bukkit.getPlayer(args[1]);
                    String group = args[2];

                    if (target == null) {
                        WebPlayer webPlayer = new WebPlayer(args[1]);
                        targetUUID = webPlayer.getUuid();
                    } else {
                        targetUUID = target.getUniqueId();
                    }

                    plugin.getStorage().setGroup(targetUUID, group, result -> {
                        boolean success = (boolean) result;
                        if (success) {
                            sender.sendMessage("Success!");
                        } else {
                            sender.sendMessage("Player either does not have a domain or has never joined.");
                        }
                    });
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
        });
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Colors.convertLegacyColors("&7&m-------------------------------------"));
        sender.sendMessage(Colors.convertLegacyColors("&a&lDOMAIN REFERRALS &7- &fhttps://github.com/TheoBong/Referrals/"));
        sender.sendMessage(Colors.convertLegacyColors(""));
        sender.sendMessage(Colors.convertLegacyColors("&f/refer create <subdomain> &7- claim a subdomain as your own"));
        sender.sendMessage(Colors.convertLegacyColors("&f/refer rewards &7- claim your rewards for referring people"));
        sender.sendMessage(Colors.convertLegacyColors("&f/refer check [player] &7- check how many referrals someone has"));
        sender.sendMessage(Colors.convertLegacyColors("&f/refer checkdomain <subdomain> &7- check information about a subdomain"));

        if (sender.hasPermission("refer.admin")) {
            sender.sendMessage(Colors.convertLegacyColors("&f/refer reset <player> &7- reset a players domain & referrals"));
            sender.sendMessage(Colors.convertLegacyColors("&f/refer reload &7- reloads the config file for domain referrals"));
            sender.sendMessage(Colors.convertLegacyColors("&f/refer setgroup [subdomain] [group] &7- set a domain's reward group"));
        }

        sender.sendMessage(Colors.convertLegacyColors("&7&m-------------------------------------"));
    }
}
