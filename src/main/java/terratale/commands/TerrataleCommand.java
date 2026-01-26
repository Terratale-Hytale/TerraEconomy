package terratale.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import terratale.plugin.TerratalePlugin;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class TerrataleCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> action;

    public TerrataleCommand() {
        super("terratale", "Reload plugin configuration");
        action = withRequiredArg("action", "Action to perform", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        if (!action.get(context).equalsIgnoreCase("reload")) {
            context.sender().sendMessage(Message.raw("Acción desconocida. Usa: /terratale reload"));
            return CompletableFuture.completedFuture(null);
        }
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