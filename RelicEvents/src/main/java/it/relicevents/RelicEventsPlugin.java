package it.relicevents;

import it.corerelics.CoreRelicsPlugin;
import it.relicevents.events.EventManager;
import it.relicevents.events.EventScheduler;
import it.relicevents.listeners.EventListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class RelicEventsPlugin extends JavaPlugin {

    private static RelicEventsPlugin instance;
    private CoreRelicsPlugin coreRelics;
    private EventManager eventManager;
    private EventScheduler eventScheduler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        coreRelics = (CoreRelicsPlugin) getServer().getPluginManager().getPlugin("CoreRelics");
        if (coreRelics == null) {
            getLogger().severe("CoreRelics non trovato! RelicEvents richiede CoreRelics.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        eventManager = new EventManager(this);
        eventScheduler = new EventScheduler(this);

        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        eventScheduler.start();

        getLogger().info("RelicEvents attivato! Eventi registrati: " + eventManager.getEventTypeCount());
    }

    @Override
    public void onDisable() {
        if (eventScheduler != null) {
            eventScheduler.stop();
        }
        if (eventManager != null) {
            eventManager.cleanupAll();
        }
        getLogger().info("RelicEvents disattivato!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("relicevent")) return false;

        if (!sender.hasPermission("relicevents.admin")) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Non hai il permesso.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                handleStart(sender, args);
                break;
            case "stop":
                handleStop(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "force":
                handleForce(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(org.bukkit.ChatColor.GOLD + "=== RelicEvents - Comandi ===");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "/relicevent start" + org.bukkit.ChatColor.GRAY + " - Avvia scheduler eventi");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "/relicevent stop" + org.bukkit.ChatColor.GRAY + " - Ferma scheduler eventi");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "/relicevent status" + org.bukkit.ChatColor.GRAY + " - Stato evento corrente");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "/relicevent force <tipo>" + org.bukkit.ChatColor.GRAY + " - Forza un evento (dungeon/ghost_ship/meteor/boss)");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "/relicevent reload" + org.bukkit.ChatColor.GRAY + " - Ricarica configurazione");
    }

    private void handleStart(CommandSender sender, String[] args) {
        eventScheduler.start();
        sender.sendMessage(org.bukkit.ChatColor.GREEN + "Scheduler eventi avviato!");
    }

    private void handleStop(CommandSender sender) {
        eventScheduler.stop();
        eventManager.cleanupAll();
        sender.sendMessage(org.bukkit.ChatColor.GREEN + "Scheduler eventi fermato e eventi puliti.");
    }

    private void handleStatus(CommandSender sender) {
        sender.sendMessage(org.bukkit.ChatColor.GOLD + "=== Stato RelicEvents ===");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "Scheduler: " +
                (eventScheduler.isRunning() ? org.bukkit.ChatColor.GREEN + "Attivo" : org.bukkit.ChatColor.RED + "Fermo"));
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "Evento attivo: " +
                (eventManager.hasActiveEvent() ?
                        org.bukkit.ChatColor.GREEN + eventManager.getActiveEventName() :
                        org.bukkit.ChatColor.GRAY + "Nessuno"));
    }

    private void handleForce(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Uso: /relicevent force <dungeon|ghost_ship|meteor|boss>");
            return;
        }

        if (eventManager.hasActiveEvent()) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "C'è già un evento attivo! Usa /relicevent stop prima.");
            return;
        }

        boolean success = eventManager.forceEvent(args[1].toLowerCase());
        if (success) {
            sender.sendMessage(org.bukkit.ChatColor.GREEN + "Evento forzato: " + args[1]);
        } else {
            sender.sendMessage(org.bukkit.ChatColor.RED + "Tipo evento non valido: " + args[1]);
            sender.sendMessage(org.bukkit.ChatColor.GRAY + "Tipi validi: dungeon, ghost_ship, meteor, boss");
        }
    }

    private void handleReload(CommandSender sender) {
        reloadConfig();
        sender.sendMessage(org.bukkit.ChatColor.GREEN + "Configurazione ricaricata!");
    }

    public static RelicEventsPlugin getInstance() {
        return instance;
    }

    public CoreRelicsPlugin getCoreRelics() {
        return coreRelics;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public EventScheduler getEventScheduler() {
        return eventScheduler;
    }
}
