package terratale.plugin;

import terratale.commands.AccountCommand;
import terratale.commands.BankCommand;
import terratale.commands.BanksCommand;
import terratale.commands.MoneyCommand;
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
        Model.initialize(getDataDirectory().toFile(), getLogger());

        getCommandRegistry().registerCommand(new MoneyCommand());
        getCommandRegistry().registerCommand(new BankCommand());
        getCommandRegistry().registerCommand(new BanksCommand());
        getCommandRegistry().registerCommand(new AccountCommand());

        setupCassaforteIntegration();

        getLogger().at(Level.INFO).log("Plugin setup complete!");
    }

    private void setupCassaforteIntegration() {
        getLogger().at(Level.INFO).log("[Cassaforte] Iniciando integración con Cassaforte...");

        try {
            Class.forName("it.cassaforte.api.Cassaforte");
            getLogger().at(Level.INFO).log("[Cassaforte] Cassaforte detectado!");

            economyService = new TerrataleEconomyService(getLogger());

            // aquí ya tienes init disponible
            cassaforteEconomy = new TerrataleCassaforteEconomy(economyService);

            boolean registered = it.cassaforte.api.Cassaforte.registerEconomy(cassaforteEconomy);

            if (registered) {
                getLogger().at(Level.INFO).log("[Cassaforte] ✓ Economía registrada exitosamente!");
            } else {
                getLogger().at(Level.WARNING).log("[Cassaforte] ✗ No se pudo registrar: Otra economía ya está registrada");
            }
        } catch (ClassNotFoundException e) {
            getLogger().at(Level.INFO).log("[Cassaforte] Cassaforte no encontrado, integración deshabilitada");
        } catch (Throwable t) {
            getLogger().at(Level.WARNING).log("[Cassaforte] Error al configurar integración: " + t.getMessage());
            t.printStackTrace();
        }
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
