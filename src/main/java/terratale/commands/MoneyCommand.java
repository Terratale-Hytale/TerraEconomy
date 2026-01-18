package terratale.commands;

import terratale.models.User;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.entity.entities.Player;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MoneyCommand extends AbstractCommandCollection {

    public MoneyCommand() {
        super("money", "Check and manage money");
        
        addSubCommand(new MoneyBalanceSubCommand());
        addSubCommand(new MoneySetSubCommand());
    }
}

// /money balance - mostrar balance propio
class MoneyBalanceSubCommand extends AbstractAsyncCommand {

    public MoneyBalanceSubCommand() {
        super("balance", "Check your money balance");
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = context.sender().getUuid();
        Player player = (Player) context.sender();
        String playerName = player.getDisplayName();
        
        User user = User.findOrCreate(playerUUID, playerName);
        
        double balance = user.getMoney();
        player.sendMessage(Message.raw("Tu balance en el bolsillo es: " + String.format("%.2f", balance) + " monedas"));
        
        return CompletableFuture.completedFuture(null);
    }
}

// /money set <username> <amount>
class MoneySetSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> usernameArg;
    private final RequiredArg<Double> amountArg;

    public MoneySetSubCommand() {
        super("set", "Set a player's money balance");
        usernameArg = withRequiredArg("username", "Player username", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to set", ArgTypes.DOUBLE);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        String targetUsername = usernameArg.get(context);
        double amount = amountArg.get(context);
        
        if (amount < 0) {
            context.sender().sendMessage(Message.raw("La cantidad no puede ser negativa"));
            return CompletableFuture.completedFuture(null);
        }
        
        // Buscar el usuario por nombre
        User targetUser = User.findByUsername(targetUsername);
        
        if (targetUser == null) {
            context.sender().sendMessage(Message.raw("Jugador no encontrado"));
            return CompletableFuture.completedFuture(null);
        }
        
        // Establecer el nuevo balance
        targetUser.setMoney(amount);
        targetUser.saveMoney();
        
        context.sender().sendMessage(Message.raw("Balance de " + targetUsername + " establecido a " + String.format("%.2f", amount) + " monedas"));
        
        return CompletableFuture.completedFuture(null);
    }
}
