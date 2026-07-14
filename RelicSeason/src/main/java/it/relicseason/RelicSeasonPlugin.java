package it.relicseason;

import it.corerelics.CoreRelicsPlugin;
import it.relicseason.commands.SeasonCommand;
import it.relicseason.data.HallOfFame;
import it.relicseason.data.ScoreManager;
import it.relicseason.listeners.SeasonListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RelicSeasonPlugin extends JavaPlugin {

    private static RelicSeasonPlugin instance;
    private CoreRelicsPlugin coreRelics;
    private ScoreManager scoreManager;
    private HallOfFame hallOfFame;
    private long seasonStartTime;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        coreRelics = (CoreRelicsPlugin) getServer().getPluginManager().getPlugin("CoreRelics");
        if (coreRelics == null) {
            getLogger().severe("CoreRelics non trovato!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        scoreManager = new ScoreManager(this);
        scoreManager.load();

        hallOfFame = new HallOfFame(this);
        hallOfFame.load();

        seasonStartTime = getConfig().getLong("internal.season-start", System.currentTimeMillis());
        if (!getConfig().isSet("internal.season-start")) {
            getConfig().set("internal.season-start", seasonStartTime);
            saveConfig();
        }

        getCommand("season").setExecutor(new SeasonCommand(this));
        getServer().getPluginManager().registerEvents(new SeasonListener(this), this);

        // Daily points for relic owners
        getServer().getScheduler().runTaskTimer(this, this::awardDailyPoints, 20 * 60 * 60, 20 * 60 * 60 * 24);
        // Check warnings
        getServer().getScheduler().runTaskTimer(this, this::checkSeasonWarnings, 20 * 60, 20 * 60 * 30);

        getLogger().info("RelicSeason attivato! Stagione: " + getConfig().getString("settings.current-season-name"));
    }

    @Override
    public void onDisable() {
        if (scoreManager != null) scoreManager.save();
        if (hallOfFame != null) hallOfFame.save();
        getLogger().info("RelicSeason disattivato!");
    }

    private void awardDailyPoints() {
        int pointsPerDay = getConfig().getInt("scoring.points-per-day-owned", 5);
        for (var relic : coreRelics.getRelicRegistry().getAllRelics()) {
            if (relic.getCurrentOwner() != null) {
                scoreManager.addPoints(relic.getCurrentOwner(), pointsPerDay, "possesso giornaliero reliquia");
            }
        }
        scoreManager.save();
    }

    private void checkSeasonWarnings() {
        int remainingDays = getRemainingDays();

        List<Integer> warningDays = getConfig().getIntegerList("settings.warning-days-before-end");
        for (int day : warningDays) {
            if (remainingDays == day) {
                String msg = getConfig().getString("messages.season-ending-days",
                        "&c⚠ La stagione finisce tra &f{days} &cgiorni!");
                msg = msg.replace("{days}", String.valueOf(day));
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                        getConfig().getString("messages.prefix", "&6[Season] &r") + msg));
                break;
            }
        }

        if (remainingDays <= 0) {
            endSeason();
        }
    }

    public void endSeason() {
        String prefix = getConfig().getString("messages.prefix", "&6[Season] &r");
        String endMsg = getConfig().getString("messages.season-ended", "&6&l✦ FINE STAGIONE! ✦");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + endMsg));

        // Award final points for relics owned
        int pointsPerRelic = getConfig().getInt("scoring.points-per-relic-owned", 100);
        for (var relic : coreRelics.getRelicRegistry().getAllRelics()) {
            if (relic.getCurrentOwner() != null) {
                scoreManager.addPoints(relic.getCurrentOwner(), pointsPerRelic, "reliquia alla fine stagione");
            }
        }

        // Announce winner
        if (getConfig().getBoolean("end-season.announce-winner", true)) {
            var topEntry = scoreManager.getTopPlayer();
            if (topEntry != null) {
                String winnerMsg = getConfig().getString("messages.winner-announce",
                        "&6&l\uD83D\uDC51 Il vincitore della {season} è: &e&l{winner} &6&lcon &c{points} &6&lpunti!");
                winnerMsg = winnerMsg.replace("{season}", getConfig().getString("settings.current-season-name", "Stagione"))
                        .replace("{winner}", topEntry.getKey())
                        .replace("{points}", String.valueOf(topEntry.getValue()));
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + winnerMsg));
            }
        }

        // Save to Hall of Fame
        if (getConfig().getBoolean("end-season.save-hall-of-fame", true)) {
            var topEntry = scoreManager.getTopPlayer();
            if (topEntry != null) {
                hallOfFame.addEntry(
                        getConfig().getInt("settings.current-season-number", 1),
                        getConfig().getString("settings.current-season-name", "Stagione 1"),
                        topEntry.getKey(),
                        topEntry.getValue());
                hallOfFame.save();
            }
        }

        // Execute end commands
        List<String> endCommands = getConfig().getStringList("end-season.end-commands");
        for (String cmd : endCommands) {
            if (!cmd.startsWith("#")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }

        scoreManager.save();
    }

    public int getRemainingDays() {
        int durationDays = getConfig().getInt("settings.season-duration-days", 90);
        long elapsed = System.currentTimeMillis() - seasonStartTime;
        long elapsedDays = elapsed / (1000L * 60 * 60 * 24);
        return (int) Math.max(0, durationDays - elapsedDays);
    }

    public int getCurrentDay() {
        long elapsed = System.currentTimeMillis() - seasonStartTime;
        return (int) (elapsed / (1000L * 60 * 60 * 24)) + 1;
    }

    public static RelicSeasonPlugin getInstance() { return instance; }
    public CoreRelicsPlugin getCoreRelics() { return coreRelics; }
    public ScoreManager getScoreManager() { return scoreManager; }
    public HallOfFame getHallOfFame() { return hallOfFame; }
}
