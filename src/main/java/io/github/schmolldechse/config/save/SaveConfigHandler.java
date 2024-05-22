package io.github.schmolldechse.config.save;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SaveConfigHandler {

    private final Plugin plugin;

    private final File path;

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Inject
    public SaveConfigHandler() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.path = new File(this.plugin.getDataFolder(), "save.cache");
    }

    public void readAppend() {
        if (!this.path.exists()) return;

        try {
            byte[] bytes = Files.readAllBytes(Paths.get(this.path.toURI()));
            String json = new String(bytes, StandardCharsets.UTF_8);

            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> data = this.GSON.fromJson(json, type);

            Map<String, Map<String, Object>> challengeData = (Map<String, Map<String, Object>>) data.get("challenges");
            challengeData.forEach((key, value) -> {
                Challenge challenge = this.plugin.challengeHandler.getChallenge(key);
                if (challenge == null) return;

                challenge.onActivate();
                challenge.append(value);
            });

            Map<String, Object> timerData = (Map<String, Object>) data.get("timer");
            this.plugin.timerHandler.time = ((Number) timerData.get("time")).intValue();
            this.plugin.timerHandler.reverse = (boolean) timerData.get("reverse");

            this.path.delete();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read data", exception);
        }
    }

    public void save() {
        Map<String, Object> data = new HashMap<>();

        /**
         * Saving challenge data
         */
        Map<String, Map<String, Object>> challengeData = new HashMap<>();
        this.plugin.challengeHandler.registeredChallenges.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .forEach(entry -> challengeData.put(entry.getKey(), entry.getValue().save()));
        data.put("challenges", challengeData);

        /**
         * Saving timer data
         */
        data.put("timer", Map.of(
                "time", this.plugin.timerHandler.time,
                "reverse", this.plugin.timerHandler.reverse
        ));

        String json = this.GSON.toJson(data);

        try {
            Files.write(Paths.get(this.path.toURI()), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to save data", exception);
        }
    }
}