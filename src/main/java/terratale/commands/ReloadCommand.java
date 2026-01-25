package terratale.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import terratale.plugin.TerratalePlugin;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ReloadCommand extends AbstractAsyncCommand {

    public ReloadCommand() {
        super("reload", "Reload plugin configuration");
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        try {
            // Recargar la configuración
            TerratalePlugin.get().getConfigManager().load();

            // Enviar mensaje de confirmación
            context.sender().sendMessage(Message.raw("Configuración recargada exitosamente!"));

        } catch (Exception e) {
            context.sender().sendMessage(Message.raw("Error al recargar la configuración: " + e.getMessage()));
        }

        return CompletableFuture.completedFuture(null);
    }
}