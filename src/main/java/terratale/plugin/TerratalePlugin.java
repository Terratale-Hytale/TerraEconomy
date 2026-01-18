package terratale.plugin;

import terratale.commands.AccountCommand;
import terratale.commands.BankCommand;
import terratale.commands.MoneyCommand;
import terratale.commands.TerrataleCommand;
import terratale.models.Model;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import java.util.logging.Level;

public class TerratalePlugin extends JavaPlugin {

    private static TerratalePlugin instance;

    public TerratalePlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    public static TerratalePlugin get() {
        return instance;
    }

    @Override
    protected void setup() {
        instance = this;

        PluginFolders.setup(this);
        
        // Inicializar base de datos con el sistema de modelos
        Model.initialize(getDataDirectory().toFile(), getLogger());

        getCommandRegistry().registerCommand(new MoneyCommand());
        getCommandRegistry().registerCommand(new BankCommand());
        getCommandRegistry().registerCommand(new AccountCommand());
        getCommandRegistry().registerCommand(new TerrataleCommand());
        getLogger().at(Level.INFO).log("Plugin setup complete!");
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("Plugin started!");
    }

    @Override
    protected void shutdown() {
        Model.close();
        getLogger().at(Level.INFO).log("Plugin shutting down!");
    }

}
