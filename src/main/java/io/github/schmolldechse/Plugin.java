package io.github.schmolldechse;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.commands.ResetCommand;
import io.github.schmolldechse.commands.SetupCommand;
import io.github.schmolldechse.commands.TimerCommand;
import io.github.schmolldechse.timer.TimerHandler;
import io.github.schmolldechse.world.WorldHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin {

    @Inject
    private TimerHandler timerHandler;

    @Inject
    private ChallengeHandler challengeHandler;

    @Inject
    private WorldHandler worldHandler;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

        Injector injector = Guice.createInjector(new PluginModule());
        injector.injectMembers(this);

        new TimerCommand(this.timerHandler).registerCommand();
        new SetupCommand(this.timerHandler, this.challengeHandler).registerCommand();
        new ResetCommand(this.worldHandler).registerCommand();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();

        if (this.timerHandler != null) this.timerHandler.shutdown();
    }
}