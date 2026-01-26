package terratale.plugin;

import terratale.Helpers.ConfigManager;
import terratale.Helpers.PluginConfig;
import terratale.commands.AccountCommand;
import terratale.commands.BankCommand;
import terratale.commands.BanksCommand;
import terratale.commands.MoneyCommand;
import terratale.commands.TerrataleCommand;
import terratale.models.Model;
import terratale.plugin.integrations.cassaforte.TerrataleEconomyService;
import terratale.plugin.integrations.cassaforte.TerrataleCassaforteEconomy;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import java.util.logging.Level;

public class TerratalePlugin extends JavaPlugin {

    private static TerratalePlugin instance;

    private final JavaPluginInit init; // <--
    private TerrataleEconomyService economyService;
    private TerrataleCassaforteEconomy cassaforteEconomy;
    private ConfigManager configManager;

    public TerratalePlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
        this.init = init; // <--
    }

    public static TerratalePlugin get() {
        return instance;
    }

    @Override
    protected void setup() {
        instance = this;

        PluginFolders.setup(this);
        configManager = new ConfigManager(getDataDirectory().toFile());
        configManager.load();
        Model.initialize(getDataDirectory().toFile(), getLogger());

        getCommandRegistry().registerCommand(new MoneyCommand());
        getCommandRegistry().registerCommand(new BankCommand());
        getCommandRegistry().registerCommand(new BanksCommand());
        getCommandRegistry().registerCommand(new AccountCommand());
        getCommandRegistry().registerCommand(new TerrataleCommand());

        getLogger().at(Level.INFO).log("Plugin setup complete!");
    }

    public PluginConfig config() {
        return configManager.getConfig();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    protected void shutdown() {
        Model.close();
        getLogger().at(Level.INFO).log("Plugin shutting down!");
    }

    public TerrataleEconomyService getEconomyService() {
        return economyService;
    }
}
