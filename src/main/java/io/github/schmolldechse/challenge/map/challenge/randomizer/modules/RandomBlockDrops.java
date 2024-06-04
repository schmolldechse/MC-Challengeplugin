package io.github.schmolldechse.challenge.map.challenge.randomizer.modules;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.challenge.module.Module;
import io.github.schmolldechse.challenge.map.challenge.randomizer.RandomizerChallenge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomBlockDrops extends Module<RandomizerChallenge> implements Listener {

    private final Random random = new Random();

    private Map<Material, Material> blocksRandomizerMap = new HashMap<>();
    private final List<Material> filteredItemBlockList;

    public RandomBlockDrops(RandomizerChallenge challenge) {
        super(challenge, "randomizer_blockdrops");

        this.filteredItemBlockList = Stream.of(Material.values())
                .filter(Material::isItem)
                .filter(material -> !this.challenge.excludedMaterials.contains(material))
                .filter(Material::isBlock)
                .filter(material -> material != Material.AIR)
                .toList();

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.GRASS_BLOCK)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .pdc(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Drops aus Blöcken", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.text("Beinhaltet " + this.blocksRandomizerMap.size() + " Drops", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("[Klick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                        .append(Component.text(" zum (De-) Aktivieren", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                Component.empty(),
                this.active
                        ? Component.text("Aktiviert", NamedTextColor.GREEN)
                        : Component.text("Deaktiviert", NamedTextColor.RED)
        );
    }

    @Override
    public void toggle() {
        super.toggle();

        if (!this.active) return;

        List<Material> copy = new ArrayList<>(this.filteredItemBlockList);

        this.blocksRandomizerMap = this.filteredItemBlockList.stream()
                .collect(Collectors.toMap(
                        material -> material,
                        material -> {
                            Material randomMaterial = copy.get(this.random.nextInt(copy.size()));
                            copy.remove(randomMaterial);
                            return randomMaterial;
                        }
                ));

        this.plugin.getLogger().info("Shuffled " + this.getIdentifierName() + " with " + this.blocksRandomizerMap.size() + " blocks");
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();

        List<List<String>> blocksSerialized = this.blocksRandomizerMap.entrySet().stream()
                .map(entry -> Arrays.asList(entry.getKey().name(), entry.getValue().name()))
                .toList();
        data.put("map", blocksSerialized);
        data.put("active", this.active);

        return data;
    }

    @Override
    public void append(Map<String, Object> data) {
        if (data.containsKey("active")) this.active = (boolean) data.get("active");

        if (data.containsKey("map")) {
            List<List<String>> blocksSerialized = (List<List<String>>) data.get("map");
            this.blocksRandomizerMap = blocksSerialized.stream()
                    .collect(Collectors.toMap(
                            entry -> Material.valueOf(entry.get(0)),
                            entry -> Material.valueOf(entry.get(1))
                    ));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(BlockBreakEvent event) {
        if (!this.challenge.isActive()) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.active) return;
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        Material randomMaterial = this.blocksRandomizerMap.get(event.getBlock().getType());
        if (randomMaterial == null) return;

        event.setDropItems(false);

        event.getBlock().getDrops().forEach(drop -> {
            if (drop.getAmount() < 1) return;
            ItemStack newItem = new ItemStack(randomMaterial, drop.getAmount());
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), newItem);
        });
    }
}