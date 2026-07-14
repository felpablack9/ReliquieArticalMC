package it.reliclore.data;

import it.reliclore.RelicLorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ChronicleManager {

    private final RelicLorePlugin plugin;
    private final List<ChronicleEntry> entries;
    private File dataFile;
    private FileConfiguration dataConfig;

    public ChronicleManager(RelicLorePlugin plugin) {
        this.plugin = plugin;
        this.entries = new ArrayList<>();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "chronicle.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("entries");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) continue;

            entries.add(new ChronicleEntry(
                    entry.getString("type", ""),
                    entry.getString("message", ""),
                    entry.getLong("timestamp", 0),
                    entry.getString("player", null),
                    entry.getString("relic", null)
            ));
        }
    }

    public void save() {
        if (dataConfig == null) return;
        dataConfig.set("entries", null);

        int max = plugin.getConfig().getInt("settings.max-chronicle-entries", 1000);
        List<ChronicleEntry> toSave = entries;
        if (entries.size() > max) {
            toSave = entries.subList(entries.size() - max, entries.size());
        }

        for (int i = 0; i < toSave.size(); i++) {
            ChronicleEntry e = toSave.get(i);
            String path = "entries." + i;
            dataConfig.set(path + ".type", e.type);
            dataConfig.set(path + ".message", e.message);
            dataConfig.set(path + ".timestamp", e.timestamp);
            if (e.playerName != null) dataConfig.set(path + ".player", e.playerName);
            if (e.relicId != null) dataConfig.set(path + ".relic", e.relicId);
        }

        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public void recordEvent(String type, String message, String playerName, String relicId) {
        if (!plugin.getConfig().getBoolean("settings.auto-record", true)) return;
        if (!plugin.getConfig().getBoolean("tracked-events." + type, true)) return;

        entries.add(new ChronicleEntry(type, message, System.currentTimeMillis(), playerName, relicId));

        int max = plugin.getConfig().getInt("settings.max-chronicle-entries", 1000);
        while (entries.size() > max) {
            entries.remove(0);
        }

        save();
    }

    public void recordEvent(String type, Map<String, String> placeholders) {
        String template = plugin.getConfig().getString("templates." + type, "");
        if (template.isEmpty()) return;

        String message = template;
        for (Map.Entry<String, String> ph : placeholders.entrySet()) {
            message = message.replace("{" + ph.getKey() + "}", ph.getValue());
        }

        String playerName = placeholders.get("player");
        String relicId = placeholders.get("relic");

        recordEvent(type, message, playerName, relicId);
    }

    public List<ChronicleEntry> getRecentEntries(int count) {
        int start = Math.max(0, entries.size() - count);
        return new ArrayList<>(entries.subList(start, entries.size()));
    }

    public List<ChronicleEntry> getEntriesByPlayer(String playerName) {
        return entries.stream()
                .filter(e -> playerName.equalsIgnoreCase(e.playerName))
                .collect(Collectors.toList());
    }

    public List<ChronicleEntry> getEntriesByRelic(String relicId) {
        return entries.stream()
                .filter(e -> relicId.equalsIgnoreCase(e.relicId))
                .collect(Collectors.toList());
    }

    public ItemStack createLoreBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        String title = plugin.getConfig().getString("messages.book-title", "&0Cronaca del Mondo");
        String author = plugin.getConfig().getString("messages.book-author", "&8Il Cronista");

        meta.setTitle(ChatColor.translateAlternateColorCodes('&', title));
        meta.setAuthor(ChatColor.translateAlternateColorCodes('&', author));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        StringBuilder page = new StringBuilder();
        page.append(ChatColor.DARK_PURPLE).append("✦ Cronaca ✦\n\n");

        int lineCount = 0;
        List<ChronicleEntry> recent = getRecentEntries(50);

        for (ChronicleEntry entry : recent) {
            String line = ChatColor.DARK_GRAY + "[" + sdf.format(new Date(entry.timestamp)) + "] "
                    + ChatColor.BLACK + ChatColor.stripColor(
                    ChatColor.translateAlternateColorCodes('&', entry.message)) + "\n";

            if (lineCount >= 10) {
                meta.addPage(page.toString());
                page = new StringBuilder();
                lineCount = 0;
            }

            page.append(line);
            lineCount++;
        }

        if (page.length() > 0) {
            meta.addPage(page.toString());
        }

        if (meta.getPageCount() == 0) {
            meta.addPage(ChatColor.GRAY + "La cronaca è vuota.\nGli eventi verranno\nregistrati qui.");
        }

        book.setItemMeta(meta);
        return book;
    }

    public int getEntryCount() { return entries.size(); }

    public static class ChronicleEntry {
        public final String type;
        public final String message;
        public final long timestamp;
        public final String playerName;
        public final String relicId;

        public ChronicleEntry(String type, String message, long timestamp, String playerName, String relicId) {
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
            this.playerName = playerName;
            this.relicId = relicId;
        }

        public String getFormattedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(new Date(timestamp));
        }
    }
}
