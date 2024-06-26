package io.github.schmolldechse.challenge.map.player;

import com.google.inject.Inject;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import io.github.schmolldechse.misc.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class SplitDamageSetting extends Challenge implements Listener {

    private boolean isApplyingDamage = false;

    @Inject
    public SplitDamageSetting() {
        super("setting_splitdamage");
    }

    @Override
    public Identification challengeIdentification() {
        return Identification.PLAYER;
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.DIAMOND_SWORD)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Geteilter Schaden", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return Arrays.asList(
                Component.empty(),
                Component.text("Passt auf, diesesmal teilt ihr euch", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("euren Schaden! Nimmt jemand Schaden, dann", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("erleiden alle die gleiche Anzahl Schaden", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                this.activationComponent()
        );
    }

    @Override
    public void onActivate() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void onDeactivate() {
        EntityDamageEvent.getHandlerList().unregister(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void execute(EntityDamageEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;
        if (this.isApplyingDamage) return;

        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        this.isApplyingDamage = true;

        double damage = event.getFinalDamage();

        Bukkit.getOnlinePlayers().stream()
                .filter(online -> !online.getUniqueId().equals(player.getUniqueId()))
                .filter(online -> online.getGameMode() == GameMode.SURVIVAL)
                .forEach(online -> online.damage(damage));

        this.isApplyingDamage = false;
    }
}
