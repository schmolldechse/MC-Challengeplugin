package io.github.schmolldechse.setting;

import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public abstract class Setting implements Listener {

    public abstract ItemStack getItemStack();
    public abstract Component getDisplayName();
    public abstract List<Component> getDescription();

    /**
     * Saves challenge data which can be applied after a restart again
     * @return Map<String, Object>
     */
    public Map<String, Object> save() { return Map.of(); }

    /**
     * Appends saved challenge data
     */
    public void append(Map<String, Object> data) { }

    private final String identifierName;

    protected boolean active = false;

    protected final Plugin plugin;

    public Setting(
            String identifierName
    ) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.identifierName = identifierName;
    }

    protected void fail() {
        if (this.plugin.timerHandler.isPaused()) return;

        this.plugin.timerHandler.pause();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getGameMode() != GameMode.SURVIVAL) return;

            player.setHealth(0.0D);
            player.setGameMode(GameMode.SPECTATOR);
        });

        Duration duration = Duration.ofSeconds(this.plugin.timerHandler.time);
        String timeFormatted = this.plugin.timerHandler.format(duration);

        Bukkit.broadcast(Component.text("Die Challenge ist fehlgeschlagen", NamedTextColor.RED));
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(Component.text("Zeit: ", NamedTextColor.RED).append(Component.text(timeFormatted).decoration(TextDecoration.ITALIC, true)));
    }

    public void toggle() {
        if (this.plugin.settingHandler == null) throw new IllegalStateException("ChallengeHandler is not initialized");

        this.active = !this.active;

        if (this.active) this.onActivate();
        else this.onDeactivate();

        String displayNameSerialized = PlainTextComponentSerializer.plainText().serialize(this.getDisplayName());
        Component descriptionComponent = this.getDisplayName();

        for (Component line : this.getDescription()) {
            descriptionComponent = descriptionComponent.append(line).append(Component.newline());
        }

        Bukkit.broadcast(Component
                .text(displayNameSerialized + (this.active ? " aktiviert" : " deaktiviert"), (this.active ? NamedTextColor.GREEN : NamedTextColor.RED))
                .hoverEvent(HoverEvent.showText(descriptionComponent))
        );
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void onActivate() {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void onDeactivate() {
        HandlerList.unregisterAll(this);
    }

    public boolean isActive() { return active; }

    public String getIdentifierName() { return identifierName; }
}