package it.corerelics.commands;

import it.corerelics.CoreRelicsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RelicTabCompleter implements TabCompleter {

    private final CoreRelicsPlugin plugin;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "list", "give", "remove", "info", "history", "fame", "spawn", "reload"
    );

    public RelicTabCompleter(CoreRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            completions = SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String partial = args[1].toLowerCase();

            switch (sub) {
                case "give":
                case "remove":
                    // Suggest player names
                    completions = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(partial))
                            .collect(Collectors.toList());
                    break;
                case "info":
                case "history":
                case "spawn":
                    // Suggest relic IDs
                    completions = plugin.getRelicRegistry().getRelicIds().stream()
                            .filter(id -> id.toLowerCase().startsWith(partial))
                            .collect(Collectors.toList());
                    break;
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            String partial = args[2].toLowerCase();

            if (sub.equals("give") || sub.equals("remove")) {
                completions = plugin.getRelicRegistry().getRelicIds().stream()
                        .filter(id -> id.toLowerCase().startsWith(partial))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}
