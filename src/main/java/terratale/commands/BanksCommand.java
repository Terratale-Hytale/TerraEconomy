package terratale.commands;

import terratale.models.Bank;
import terratale.models.User;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BanksCommand extends AbstractAsyncCommand {

    public BanksCommand() {
        super("banks", "List all created banks");
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        List<Bank> banks = Bank.findAll();

        if (banks.isEmpty()) {
            context.sender().sendMessage(Message.raw("No hay bancos creados."));
            return CompletableFuture.completedFuture(null);
        }

        context.sender().sendMessage(Message.raw("=== Bancos Disponibles ==="));
        context.sender().sendMessage(Message.raw("Total: " + banks.size()));
        context.sender().sendMessage(Message.raw(""));

        for (Bank bank : banks) {
            User owner = User.find(bank.getOwnerUuid());
            String ownerName = (owner != null) ? owner.getUsername() : "Desconocido";
            
            context.sender().sendMessage(Message.raw("ID: #" + bank.getId()));
            context.sender().sendMessage(Message.raw("  Nombre: " + bank.getName()));
            context.sender().sendMessage(Message.raw("  Propietario: " + ownerName));
            context.sender().sendMessage(Message.raw("  Balance: $" + String.format("%.2f", bank.getBalance())));
            context.sender().sendMessage(Message.raw("  Comisiones:"));
            context.sender().sendMessage(Message.raw("    - Retiro: " + bank.getWithdrawFee() + "%"));
            context.sender().sendMessage(Message.raw("    - Depósito: " + bank.getDepositFee() + "%"));
            context.sender().sendMessage(Message.raw("    - Transferencia: " + bank.getTransactionsFee() + "%"));
            context.sender().sendMessage(Message.raw(""));
        }

        return CompletableFuture.completedFuture(null);
    }
}
