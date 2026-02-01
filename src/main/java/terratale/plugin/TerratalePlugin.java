package terratale.plugin;

import terratale.Helpers.ConfigManager;
import terratale.Helpers.PluginConfig;
import terratale.commands.AccountCommand;
import terratale.commands.BankCommand;
import terratale.commands.BanksCommand;
import terratale.commands.CheckPorcentualCommand;
import terratale.commands.InvoiceCommand;
import terratale.commands.InvoicesCommand;
import terratale.commands.MoneyCommand;
import terratale.commands.SchedulePaymentCommand;
import terratale.commands.TerrataleCommand;
import terratale.models.Model;
import terratale.plugin.integrations.vaultUnlocked.TerrataleVaultEconomy;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import net.cfh.vault.VaultUnlockedServicesManager;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TerratalePlugin extends JavaPlugin {

    private static TerratalePlugin instance;

    private final JavaPluginInit init; // <--
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

        if (HytaleServer.get().getPluginManager().hasPlugin(
                PluginIdentifier.fromString("TheNewEconomy:VaultUnlocked"),
                SemverRange.WILDCARD
        )) {
            getLogger().at(Level.INFO).log("VaultUnlocked is installed, enabling VaultUnlocked support.");

            VaultUnlockedServicesManager.get().economy(new TerrataleVaultEconomy());
        } else {
            getLogger().at(Level.INFO).log("VaultUnlocked is not installed, disabling VaultUnlocked support.");
        }

        PluginFolders.setup(this);
        configManager = new ConfigManager(getDataDirectory().toFile());
        configManager.load();
        Model.initialize(getDataDirectory().toFile(), getLogger());

        getCommandRegistry().registerCommand(new MoneyCommand());
        getCommandRegistry().registerCommand(new BankCommand());
        getCommandRegistry().registerCommand(new BanksCommand());
        getCommandRegistry().registerCommand(new AccountCommand());
        getCommandRegistry().registerCommand(new InvoiceCommand());
        getCommandRegistry().registerCommand(new InvoicesCommand());
        getCommandRegistry().registerCommand(new SchedulePaymentCommand());
        getCommandRegistry().registerCommand(new TerrataleCommand());
        getCommandRegistry().registerCommand(new CheckPorcentualCommand());

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
}
