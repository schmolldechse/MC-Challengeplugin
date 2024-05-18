package io.github.schmolldechse.challenge.map;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.timer.TimerHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AllDieChallenge extends Challenge {

    private final Plugin plugin;

    @Inject
    public AllDieChallenge(TimerHandler timerHandler, ChallengeHandler challengeHandler) {
        super(
                "c_alldie",
                timerHandler,
                challengeHandler
        );

        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.SKELETON_SKULL)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .pdc(persistentDataContainer -> {
                    NamespacedKey key = new NamespacedKey(this.plugin, "identifier");
                    persistentDataContainer.set(key, PersistentDataType.STRING, this.getIdentifierName());
                })
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Einer stirbt, Alle sterben", NamedTextColor.RED);
    }

    @Override
    public List<Component> getDescription() {
        return Arrays.asList(
                Component.empty(),
                Component.text("Ziel: ", NamedTextColor.WHITE).append(Component.text("Enderdrachen besiegen", NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, true),
                Component.text("Stirbt jemand, gilt die Challenge als fehlgeschlagen", NamedTextColor.WHITE)
        );
    }

    @Override
    public Map<String, Object> save() { return Map.of(); }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(PlayerDeathEvent event) {
        if (!this.active) return;
        event.deathMessage(null);

        if (this.timerHandler.isPaused()) return;

        Bukkit.broadcast(Component.text(event.getPlayer().getName(), NamedTextColor.GREEN).append(Component.text(" ist gestorben!", NamedTextColor.RED)));
        this.fail();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(EntityDeathEvent event) {
        if (!this.active) return;
        if (this.timerHandler.isPaused()) return;

        if (!(event.getEntity() instanceof EnderDragon)) return;

        this.success();
    }
}
