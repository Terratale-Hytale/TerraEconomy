package terratale.commands;

import terratale.models.BankAccount;
import terratale.models.BankAccountOwner;
import terratale.models.Invoice;
import terratale.Helpers.InvoiceHelper;
import terratale.responses.InvoicePaymentResponse;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.entity.entities.Player;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InvoiceCommand extends AbstractCommandCollection {

    public InvoiceCommand() {
        super("invoice", "Manage invoices");

        addSubCommand(new InvoiceCreateSubCommand());
        addSubCommand(new InvoicePaySubCommand());
        addSubCommand(new InvoiceRejectSubCommand());
    }
}

// /invoice create <receptor_account> <payer_account> <amount> <days_until_due> <description>
class InvoiceCreateSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> receptorAccountArg;
    private final RequiredArg<String> payerAccountArg;
    private final RequiredArg<Double> amountArg;
    private final RequiredArg<Integer> daysArg;
    private final RequiredArg<String> descriptionArg;

    public InvoiceCreateSubCommand() {
        super("create", "Create a new invoice");
        receptorAccountArg = withRequiredArg("receptor_account", "Receptor account number", ArgTypes.STRING);
        payerAccountArg = withRequiredArg("payer_account", "Payer account number", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to charge", ArgTypes.DOUBLE);
        daysArg = withRequiredArg("days_until_due", "Days until due date", ArgTypes.INTEGER);
        descriptionArg = withRequiredArg("description", "Invoice description", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        String receptorAccount = receptorAccountArg.get(context);
        String payerAccount = payerAccountArg.get(context);
        double amount = amountArg.get(context);
        int daysUntilDue = daysArg.get(context);
        String description = descriptionArg.get(context);
        UUID playerUUID = context.sender().getUuid();

        // Validar que el monto sea positivo
        if (amount <= 0) {
            context.sender().sendMessage(Message.raw("El monto debe ser mayor a 0."));
            return CompletableFuture.completedFuture(null);
        }

        // Validar que los días sean positivos
        if (daysUntilDue <= 0 || daysUntilDue >= 30) {
            context.sender().sendMessage(Message.raw("Los días hasta el vencimiento deben ser mayor a 0 y menor o igual a 30."));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que las cuentas existan
        BankAccount receptorAcc = BankAccount.findByAccountNumber(receptorAccount);
        BankAccount payerAcc = BankAccount.findByAccountNumber(payerAccount);

        if (receptorAcc == null) {
            context.sender().sendMessage(Message.raw("La cuenta receptora no existe."));
            return CompletableFuture.completedFuture(null);
        }

        if (payerAcc == null) {
            context.sender().sendMessage(Message.raw("La cuenta pagadora no existe."));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que el jugador sea dueño de la cuenta receptora
        List<Integer> playerAccounts = BankAccountOwner.getAccountsByOwner(playerUUID);
        if (!playerAccounts.contains(receptorAcc.getId())) {
            context.sender().sendMessage(Message.raw("No eres dueño de la cuenta receptora."));
            return CompletableFuture.completedFuture(null);
        }

        if (receptorAccount.equals(payerAccount)) {
            context.sender().sendMessage(Message.raw("La cuenta receptora y la cuenta pagadora no pueden ser la misma."));
            return CompletableFuture.completedFuture(null);
        }

        // Calcular fecha de vencimiento
        long currentTimeMillis = System.currentTimeMillis();
        long dueTimeMillis = currentTimeMillis + (daysUntilDue * 24L * 60L * 60L * 1000L);
        Date dueDate = new Date(dueTimeMillis);

        // Crear la factura
        Invoice invoice = new Invoice(receptorAccount, payerAccount, amount, dueDate, description);
        invoice.save();

        context.sender().sendMessage(Message.raw("Factura creada exitosamente!"));
        context.sender().sendMessage(Message.raw("ID: #" + invoice.getId()));
        context.sender().sendMessage(Message.raw("Cuenta Receptora: " + receptorAccount));
        context.sender().sendMessage(Message.raw("Cuenta Pagadora: " + payerAccount));
        context.sender().sendMessage(Message.raw("Monto: $" + String.format("%.2f", amount)));
        context.sender().sendMessage(Message.raw("Vencimiento: " + dueDate.toString()));
        context.sender().sendMessage(Message.raw("Descripción: " + description));

        return CompletableFuture.completedFuture(null);
    }
}

// /invoice pay <invoice_id>
class InvoicePaySubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> invoiceIdArg;

    public InvoicePaySubCommand() {
        super("pay", "Pay an invoice");
        invoiceIdArg = withRequiredArg("invoice_id", "Invoice ID", ArgTypes.INTEGER);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        int invoiceId = invoiceIdArg.get(context);
        UUID playerUUID = context.sender().getUuid();
         InvoicePaymentResponse response;

        try {
            response = InvoiceHelper.payInvoice(invoiceId, playerUUID);
        } catch (RuntimeException e) {
            context.sender().sendMessage(Message.raw("Error al pagar la factura: " + e.getMessage()));
            return CompletableFuture.completedFuture(null);
        }

        context.sender().sendMessage(Message.raw("Factura pagada exitosamente!"));
        context.sender().sendMessage(Message.raw("ID: #" + invoiceId));
        context.sender().sendMessage(Message.raw("Monto factura: $" + String.format("%.2f", response.getInvoice().getAmount())));
        context.sender().sendMessage(Message.raw("Comisión bancaria: $" + String.format("%.2f", response.getBankTransferFee())));
        context.sender().sendMessage(Message.raw("Total deducido: $" + String.format("%.2f", response.getTotalDeducted())));
        context.sender().sendMessage(Message.raw("Nuevo saldo: $" + String.format("%.2f", response.getNewBalance())));

        return CompletableFuture.completedFuture(null);
    }
}

// /invoice reject <invoice_id>
class InvoiceRejectSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> invoiceIdArg;

    public InvoiceRejectSubCommand() {
        super("reject", "Reject an invoice");
        invoiceIdArg = withRequiredArg("invoice_id", "Invoice ID", ArgTypes.INTEGER);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        int invoiceId = invoiceIdArg.get(context);
        UUID playerUUID = context.sender().getUuid();

        try {
            InvoiceHelper.rejectInvoice(invoiceId, playerUUID);
        } catch (RuntimeException e) {
            context.sender().sendMessage(Message.raw("Error al rechazar la factura: " + e.getMessage()));
            return CompletableFuture.completedFuture(null);
        }
        

        context.sender().sendMessage(Message.raw("Factura rechazada exitosamente."));
        context.sender().sendMessage(Message.raw("ID: #" + invoiceId));

        return CompletableFuture.completedFuture(null);
    }
}
