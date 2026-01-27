package terratale.commands;

import terratale.models.BankAccount;
import terratale.models.BankAccountOwner;
import terratale.models.BankTransaction;
import terratale.models.Invoice;
import terratale.models.Transaction;
import terratale.models.User;
import terratale.Helpers.TransactionTypes;
import terratale.plugin.TerratalePlugin;

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
        User playerUser = User.findOrCreate(playerUUID, context.sender().getDisplayName());

        // Buscar la factura
        Invoice invoice = Invoice.find(invoiceId);
        if (invoice == null) {
            context.sender().sendMessage(Message.raw("Factura no encontrada."));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que la factura esté pendiente
        if (!"pending".equals(invoice.getStatus())) {
            context.sender().sendMessage(Message.raw("Esta factura ya fue procesada (estado: " + invoice.getStatus() + ")."));
            return CompletableFuture.completedFuture(null);
        }

        // Obtener las cuentas
        BankAccount payerAcc = BankAccount.findByAccountNumber(invoice.getPayerAccountNumber());
        BankAccount receptorAcc = BankAccount.findByAccountNumber(invoice.getReceptorAccountNumber());
        BankAccount govAccount = BankAccount.findByAccountNumber(TerratalePlugin.get().config().gouvernmentNumberAccount);

        if (payerAcc == null || receptorAcc == null) {
            context.sender().sendMessage(Message.raw("Error: Una de las cuentas ya no existe."));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que el jugador sea dueño de la cuenta pagadora
        List<Integer> playerAccounts = BankAccountOwner.getAccountsByOwner(playerUUID);
        if (!playerAccounts.contains(payerAcc.getId())) {
            context.sender().sendMessage(Message.raw("No eres dueño de la cuenta pagadora."));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que haya suficiente saldo
        if (payerAcc.getBalance() < invoice.getAmount()) {
            context.sender().sendMessage(Message.raw("Saldo insuficiente en la cuenta pagadora."));
            context.sender().sendMessage(Message.raw("Saldo actual: $" + String.format("%.2f", payerAcc.getBalance())));
            context.sender().sendMessage(Message.raw("Monto requerido: $" + String.format("%.2f", invoice.getAmount())));
            return CompletableFuture.completedFuture(null);
        }

        // Esto se le quita al receptor de la factura como impuesto
        int gouvermentFeePercent = TerratalePlugin.get().config().taxPercentage;
        Double gouvermentFeeAmount = (invoice.getAmount() * gouvermentFeePercent) / 100;

        // Esto se le quita al pagador de la factura como comisión bancaria
        Double bankTransferFeePercent = payerAcc.getBank().getTransactionsFee();
        Double bankTransferFeeAmount = (invoice.getAmount() * bankTransferFeePercent) / 100;

        if (playerUser.getMoney() < invoice.getAmount() + bankTransferFeeAmount + gouvermentFeeAmount) {
            context.sender().sendMessage(Message.raw("Saldo insuficiente para cubrir las comisiones e impuestos."));
            context.sender().sendMessage(Message.raw("Saldo actual: $" + String.format("%.2f", playerUser.getMoney())));
            context.sender().sendMessage(Message.raw("Comisión bancaria: $" + String.format("%.2f", bankTransferFeeAmount)));
            return CompletableFuture.completedFuture(null);
        }
        
        // Realizar la transferencia
        payerAcc.setBalance(payerAcc.getBalance() - (invoice.getAmount() + bankTransferFeeAmount));
        receptorAcc.setBalance(receptorAcc.getBalance() + (invoice.getAmount() - gouvermentFeeAmount));
        govAccount.setBalance(govAccount.getBalance() + gouvermentFeeAmount);
        
        payerAcc.save();
        receptorAcc.save();
        govAccount.save();

        // Registrar transacciones
        Transaction payerTransaction = new Transaction(
            payerAcc.getId(),
            TransactionTypes.INVOICE_WITHDRAWAL,
            invoice.getAmount() + bankTransferFeeAmount,
            context.sender().getUuid().toString()
        );
        payerTransaction.save();

        Transaction receptorTransaction = new Transaction(
            receptorAcc.getId(),
            TransactionTypes.INVOICE_PAYMENT,
            invoice.getAmount() - gouvermentFeeAmount,
            context.sender().getUuid().toString()
        );
        receptorTransaction.save();

        Transaction govTransaction = new Transaction(
            govAccount.getId(),
            TransactionTypes.GOVERNMENT_FEE,
            gouvermentFeeAmount,
            context.sender().getUuid().toString()
        );
        govTransaction.save();

        BankTransaction bankTransaction = new BankTransaction(
            payerAcc.getBank().getId(),
            TransactionTypes.TRANSFER_FEE,
            bankTransferFeeAmount,
            context.sender().getUuid().toString()
        );
        bankTransaction.save();

        // Marcar la factura como pagada
        invoice.markAsPaid(playerUUID.toString());

        context.sender().sendMessage(Message.raw("Factura pagada exitosamente!"));
        context.sender().sendMessage(Message.raw("ID: #" + invoice.getId()));
        context.sender().sendMessage(Message.raw("Monto pagado: $" + String.format("%.2f", invoice.getAmount())));
        context.sender().sendMessage(Message.raw("Nuevo saldo: $" + String.format("%.2f", payerAcc.getBalance())));

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

        // Buscar la factura
        Invoice invoice = Invoice.find(invoiceId);
        if (invoice == null) {
            context.sender().sendMessage(Message.raw("Factura no encontrada."));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que la factura esté pendiente
        if (!"pending".equals(invoice.getStatus())) {
            context.sender().sendMessage(Message.raw("Esta factura ya fue procesada (estado: " + invoice.getStatus() + ")."));
            return CompletableFuture.completedFuture(null);
        }

        // Obtener la cuenta pagadora
        BankAccount payerAcc = BankAccount.findByAccountNumber(invoice.getPayerAccountNumber());
        
        if (payerAcc == null) {
            context.sender().sendMessage(Message.raw("Error: La cuenta pagadora ya no existe."));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que el jugador sea dueño de la cuenta pagadora
        List<Integer> playerAccounts = BankAccountOwner.getAccountsByOwner(playerUUID);
        if (!playerAccounts.contains(payerAcc.getId())) {
            context.sender().sendMessage(Message.raw("No eres dueño de la cuenta pagadora. Solo el pagador puede rechazar una factura."));
            return CompletableFuture.completedFuture(null);
        }

        // Marcar la factura como cancelada
        invoice.markAsCancelled(playerUUID.toString());

        context.sender().sendMessage(Message.raw("Factura rechazada exitosamente."));
        context.sender().sendMessage(Message.raw("ID: #" + invoice.getId()));
        context.sender().sendMessage(Message.raw("Descripción: " + invoice.getDescription()));

        return CompletableFuture.completedFuture(null);
    }
}
