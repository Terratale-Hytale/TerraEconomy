package terratale.commands;

import terratale.models.BankAccount;
import terratale.models.BankAccountOwner;
import terratale.models.Invoice;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InvoicesCommand extends AbstractAsyncCommand {

    private final OptionalArg<String> accountNumberArg;

    public InvoicesCommand() {
        super("invoices", "List all your invoices");
        accountNumberArg = withOptionalArg("account_number", "Account Number", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = context.sender().getUuid();

        // Obtener todas las cuentas del jugador
        List<Integer> playerAccountIds = BankAccountOwner.getAccountsByOwner(playerUUID);

        if (playerAccountIds.isEmpty()) {
            context.sender().sendMessage(Message.raw("No tienes cuentas bancarias."));
            return CompletableFuture.completedFuture(null);
        }

        // Obtener todas las facturas relacionadas con las cuentas del jugador
        List<Invoice> receivedInvoices = new ArrayList<>();
        List<Invoice> sentInvoices = new ArrayList<>();

        if (accountNumberArg.get(context) != null) {
            String accNumber = accountNumberArg.get(context);
            BankAccount account = BankAccount.findByAccountNumber(accNumber);
            if (account == null || !playerAccountIds.contains(account.getId())) {
                context.sender().sendMessage(Message.raw("No tienes una cuenta con ese número."));
                return CompletableFuture.completedFuture(null);
            }
            // Facturas recibidas (donde el jugador es el pagador)
            receivedInvoices.addAll(Invoice.findByPayerAccount(accNumber));
            // Facturas enviadas (donde el jugador es el receptor)
            sentInvoices.addAll(Invoice.findByReceptorAccount(accNumber));
        } else {
            for (Integer accountId : playerAccountIds) {
                BankAccount account = BankAccount.find(accountId);
                if (account != null) {
                    String accountNumber = account.getAccountNumber();
                    
                    // Facturas recibidas (donde el jugador es el pagador)
                    receivedInvoices.addAll(Invoice.findByPayerAccount(accountNumber));
                    
                    // Facturas enviadas (donde el jugador es el receptor)
                    sentInvoices.addAll(Invoice.findByReceptorAccount(accountNumber));
                }
            }
        }

        // Mostrar facturas recibidas
        context.sender().sendMessage(Message.raw("=== Facturas Recibidas (Por Pagar) ==="));
        if (receivedInvoices.isEmpty()) {
            context.sender().sendMessage(Message.raw("No tienes facturas por pagar."));
        } else {
            context.sender().sendMessage(Message.raw("Total: " + receivedInvoices.size()));
            context.sender().sendMessage(Message.raw(""));
            
            for (Invoice invoice : receivedInvoices) {
                String overdueTag = invoice.isOverdue() ? " [VENCIDA]" : "";
                
                context.sender().sendMessage(Message.raw("ID: #" + invoice.getId() + " - Estado: " + invoice.getStatus().toUpperCase() + overdueTag));
                context.sender().sendMessage(Message.raw("  De: " + invoice.getReceptorAccountNumber()));
                context.sender().sendMessage(Message.raw("  A: " + invoice.getPayerAccountNumber()));
                context.sender().sendMessage(Message.raw("  Monto: $" + String.format("%.2f", invoice.getAmount())));
                context.sender().sendMessage(Message.raw("  Vencimiento: " + invoice.getDueDate().toString()));
                context.sender().sendMessage(Message.raw("  Descripción: " + invoice.getDescription()));
                context.sender().sendMessage(Message.raw(""));
            }
        }

        // Mostrar facturas enviadas
        context.sender().sendMessage(Message.raw("=== Facturas Enviadas (Por Cobrar) ==="));
        if (sentInvoices.isEmpty()) {
            context.sender().sendMessage(Message.raw("No has enviado facturas."));
        } else {
            context.sender().sendMessage(Message.raw("Total: " + sentInvoices.size()));
            context.sender().sendMessage(Message.raw(""));
            
            for (Invoice invoice : sentInvoices) {
                String overdueTag = invoice.isOverdue() ? " [VENCIDA]" : "";
                
                context.sender().sendMessage(Message.raw("ID: #" + invoice.getId() + " - Estado: " + invoice.getStatus().toUpperCase() + overdueTag));
                context.sender().sendMessage(Message.raw("  De: " + invoice.getReceptorAccountNumber()));
                context.sender().sendMessage(Message.raw("  A: " + invoice.getPayerAccountNumber()));
                context.sender().sendMessage(Message.raw("  Monto: $" + String.format("%.2f", invoice.getAmount())));
                context.sender().sendMessage(Message.raw("  Vencimiento: " + invoice.getDueDate().toString()));
                context.sender().sendMessage(Message.raw("  Descripción: " + invoice.getDescription()));
                context.sender().sendMessage(Message.raw(""));
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
