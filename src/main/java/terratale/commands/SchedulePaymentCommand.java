package terratale.commands;

import terratale.models.BankAccount;
import terratale.models.BankAccountOwner;
import terratale.models.Invoice;
import terratale.models.SchedulePayment;
import terratale.models.User;
import terratale.models.ScheduleLog;
import terratale.plugin.TerratalePlugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.entity.entities.Player;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SchedulePaymentCommand extends AbstractCommandCollection {

    public SchedulePaymentCommand() {
        super("schedulepayment", "Manage scheduled payments");

        addSubCommand(new SchedulePaymentCreateSubCommand());
        addSubCommand(new SchedulePaymentListSubCommand());
        addSubCommand(new SchedulePaymentDeleteSubCommand());
        addSubCommand(new SchedulePaymentProcessSubCommand());
        addSubCommand(new SchedulePaymentLogsSubCommand());
    }
}

// /schedulepayment create <receptor_account> <payer_account> <amount> <day_of_month> <days_until_due> <description>
class SchedulePaymentCreateSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> receptorAccountArg;
    private final RequiredArg<String> payerAccountArg;
    private final RequiredArg<Double> amountArg;
    private final RequiredArg<Integer> dayOfMonthArg;
    private final RequiredArg<Integer> daysUntilDueArg;
    private final RequiredArg<String> descriptionArg;

    public SchedulePaymentCreateSubCommand() {
        super("create", "Create a new scheduled payment");
        receptorAccountArg = withRequiredArg("receptor_account", "Receptor account number", ArgTypes.STRING);
        payerAccountArg = withRequiredArg("payer_account", "Payer account number", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to charge", ArgTypes.DOUBLE);
        dayOfMonthArg = withRequiredArg("day_of_month", "Day of month to charge (1-28)", ArgTypes.INTEGER);
        daysUntilDueArg = withRequiredArg("days_until_due", "Days until due date", ArgTypes.INTEGER);
        descriptionArg = withRequiredArg("description", "Payment description", ArgTypes.STRING);
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
        int dayOfMonth = dayOfMonthArg.get(context);
        int daysUntilDue = daysUntilDueArg.get(context);
        String description = descriptionArg.get(context);
        UUID playerUUID = context.sender().getUuid();
        List<Integer> userAccountsIds = BankAccountOwner.getAccountsByOwner(playerUUID);

        description = description.replace("\"", "");
        description = description.replace("\'", "");

        // Validar que el monto sea positivo
        if (amount <= 0) {
            context.sender().sendMessage(Message.raw("El monto debe ser mayor a 0."));
            return CompletableFuture.completedFuture(null);
        }

        // Validar día del mes
        if (dayOfMonth < 1 || dayOfMonth > 28) {
            context.sender().sendMessage(Message.raw("El día del mes debe estar entre 1 y 28."));
            return CompletableFuture.completedFuture(null);
        }

        // Validar días hasta vencimiento
        if (daysUntilDue <= 0 || daysUntilDue > 30) {
            context.sender().sendMessage(Message.raw("Los días hasta el vencimiento deben estar entre 1 y 30."));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que las cuentas existan
        BankAccount receptorAcc = BankAccount.findByAccountNumber(receptorAccount);
        if (receptorAcc == null) {
            context.sender().sendMessage(Message.raw("La cuenta receptora no existe."));
            return CompletableFuture.completedFuture(null);
        }

        BankAccount payerAcc = BankAccount.findByAccountNumber(payerAccount);
        if (payerAcc == null) {
            context.sender().sendMessage(Message.raw("La cuenta pagadora no existe."));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar si la cuenta que recibe pertenece al jugador
        if (!userAccountsIds.contains(receptorAcc.getId())) {
            context.sender().sendMessage(Message.raw("No estas autorizado a utilizar la cuenta " + receptorAccount + "."));
            return CompletableFuture.completedFuture(null);
        }
        
        // Crear el pago programado
        SchedulePayment schedulePayment = new SchedulePayment(
            receptorAccount,
            payerAccount,
            description,
            daysUntilDue,
            amount,
            dayOfMonth
        );
        schedulePayment.save();

        context.sender().sendMessage(Message.raw("Pago programado creado exitosamente!"));
        context.sender().sendMessage(Message.raw("- Día de cobro: " + dayOfMonth + " de cada mes"));
        context.sender().sendMessage(Message.raw("- Días para pagar: " + daysUntilDue + " días"));
        context.sender().sendMessage(Message.raw("- Monto: $" + amount));

        return CompletableFuture.completedFuture(null);
    }
}

// /schedulepayment list [account]
class SchedulePaymentListSubCommand extends AbstractAsyncCommand {

    private final OptionalArg<String> accountArg;

    public SchedulePaymentListSubCommand() {
        super("list", "List scheduled payments");
        accountArg = withOptionalArg("account", "Account number to filter", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        List<SchedulePayment> schedulePayments;
        String account = accountArg.get(context);

        if (account != null && !account.isEmpty()) {
            // Buscar por cuenta
            BankAccount bankAccount = BankAccount.findByAccountNumber(account);
            if (bankAccount == null) {
                context.sender().sendMessage(Message.raw("La cuenta no existe."));
                return CompletableFuture.completedFuture(null);
            }

            // Obtener pagos donde la cuenta es pagadora o receptora
            List<SchedulePayment> asPayer = SchedulePayment.findByPayerAccount(account);
            List<SchedulePayment> asReceptor = SchedulePayment.findByReceptorAccount(account);
            
            context.sender().sendMessage(Message.raw("§e=== Pagos Programados de " + account + " ==="));
            
            if (!asPayer.isEmpty()) {
                context.sender().sendMessage(Message.raw("§6Pagos a realizar:"));
                for (SchedulePayment sp : asPayer) {
                    context.sender().sendMessage(Message.raw(
                        "§7- ID: " + sp.getId() + 
                        " | Día: " + sp.getDayOfMonth() + 
                        " | A: " + sp.getReceptorAccountNumber() +
                        " | $" + sp.getAmount() +
                        " | Estado: " + sp.getStatus()
                    ));
                }
            }
            
            if (!asReceptor.isEmpty()) {
                context.sender().sendMessage(Message.raw("§aPagos a recibir:"));
                for (SchedulePayment sp : asReceptor) {
                    context.sender().sendMessage(Message.raw(
                        "§7- ID: " + sp.getId() + 
                        " | Día: " + sp.getDayOfMonth() + 
                        " | De: " + sp.getPayerAccountNumber() +
                        " | $" + sp.getAmount() +
                        " | Estado: " + sp.getStatus()
                    ));
                }
            }
        } else {
            // Listar todos
            schedulePayments = SchedulePayment.all();
            context.sender().sendMessage(Message.raw("§e=== Todos los Pagos Programados ==="));
            
            if (schedulePayments.isEmpty()) {
                context.sender().sendMessage(Message.raw("§7No hay pagos programados."));
            } else {
                for (SchedulePayment sp : schedulePayments) {
                    context.sender().sendMessage(Message.raw(
                        "- ID: " + sp.getId() + 
                        " | Día: " + sp.getDayOfMonth() + 
                        " | De: " + sp.getPayerAccountNumber() +
                        " → " + sp.getReceptorAccountNumber() +
                        " | $" + sp.getAmount() +
                        " | " + sp.getStatus()
                    ));
                }
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}

// /schedulepayment delete <id>
class SchedulePaymentDeleteSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> idArg;

    public SchedulePaymentDeleteSubCommand() {
        super("delete", "Delete a scheduled payment");
        idArg = withRequiredArg("id", "Scheduled payment ID", ArgTypes.INTEGER);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        int id = idArg.get(context);

        SchedulePayment schedulePayment = SchedulePayment.find(id);
        if (schedulePayment == null) {
            context.sender().sendMessage(Message.raw("§cPago programado no encontrado."));
            return CompletableFuture.completedFuture(null);
        }

        schedulePayment.delete();
        context.sender().sendMessage(Message.raw("§aPago programado eliminado exitosamente."));

        return CompletableFuture.completedFuture(null);
    }
}

// /schedulepayment process
class SchedulePaymentProcessSubCommand extends AbstractAsyncCommand {

    public SchedulePaymentProcessSubCommand() {
        super("process", "Process today's scheduled payments and create invoices");
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        // Obtener día actual
        LocalDate today = LocalDate.now();
        int currentDayOfMonth = today.getDayOfMonth();

        // Buscar pagos programados para hoy
        List<SchedulePayment> todayPayments = SchedulePayment.findByDayOfMonth(currentDayOfMonth);

        if (todayPayments.isEmpty()) {
            context.sender().sendMessage(Message.raw("No hay pagos programados para el día " + currentDayOfMonth + "."));
            return CompletableFuture.completedFuture(null);
        }

        context.sender().sendMessage(Message.raw("=== Procesando Pagos del Día " + currentDayOfMonth + " ==="));
        
        int processed = 0;
        int failed = 0;

        for (SchedulePayment sp : todayPayments) {
            try {
                // Verificar que las cuentas aún existan
                BankAccount receptorAcc = BankAccount.findByAccountNumber(sp.getReceptorAccountNumber());
                BankAccount payerAcc = BankAccount.findByAccountNumber(sp.getPayerAccountNumber());

                if (receptorAcc == null || payerAcc == null) {
                    String errorMsg = "Una o ambas cuentas no existen";
                    context.sender().sendMessage(Message.raw(
                        "- ID " + sp.getId() + ": " + errorMsg
                    ));
                    
                    // Registrar log de error
                    ScheduleLog log = new ScheduleLog(sp.getId(), null, "failed", errorMsg);
                    log.save();
                    
                    failed++;
                    continue;
                }

                // Calcular fecha de vencimiento usando los días configurados
                LocalDate dueDate = today.plusDays(sp.getDueDays());

                // Crear factura
                Invoice invoice = new Invoice(
                    sp.getReceptorAccountNumber(),
                    sp.getPayerAccountNumber(),
                    sp.getAmount(),
                    Date.valueOf(dueDate),
                    sp.getDescription()
                );
                invoice.save();

                invoice.addEvent("generated_by", "gouvernement_system");
                invoice.save();

                // Registrar log de éxito
                String successMsg = "Factura #" + invoice.getId() + " creada exitosamente";
                ScheduleLog log = new ScheduleLog(sp.getId(), invoice.getId(), "success", successMsg);
                log.save();

                context.sender().sendMessage(Message.raw(
                    "- ID " + sp.getId() + ": " + successMsg + " - " +
                    sp.getPayerAccountNumber() + " → " + sp.getReceptorAccountNumber() + 
                    " | $" + sp.getAmount() + " | Vence: " + dueDate
                ));
                
                processed++;

            } catch (Exception e) {
                String errorMsg = "Error al procesar: " + e.getMessage();
                context.sender().sendMessage(Message.raw(
                    "- ID " + sp.getId() + ": " + errorMsg
                ));
                
                // Registrar log de error
                ScheduleLog log = new ScheduleLog(sp.getId(), null, "failed", errorMsg);
                log.save();
                
                failed++;
            }
        }

        context.sender().sendMessage(Message.raw("================================="));
        context.sender().sendMessage(Message.raw("Facturas creadas: " + processed));
        if (failed > 0) {
            context.sender().sendMessage(Message.raw("Fallidas: " + failed));
        }

        return CompletableFuture.completedFuture(null);
    }
}

// /schedulepayment logs [schedule_payment_id] [limit]
class SchedulePaymentLogsSubCommand extends AbstractAsyncCommand {

    private final OptionalArg<Integer> schedulePaymentIdArg;
    private final OptionalArg<Integer> limitArg;

    public SchedulePaymentLogsSubCommand() {
        super("logs", "View scheduled payment execution logs");
        schedulePaymentIdArg = withOptionalArg("schedule_payment_id", "Schedule payment ID to filter", ArgTypes.INTEGER);
        limitArg = withOptionalArg("limit", "Number of logs to show (default: 10)", ArgTypes.INTEGER);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        Integer schedulePaymentId = schedulePaymentIdArg.get(context);
        Integer limit = limitArg.get(context);
        
        if (limit == null) {
            limit = 10;
        }

        List<ScheduleLog> logs;
        
        if (schedulePaymentId != null) {
            // Buscar logs de un pago programado específico
            logs = ScheduleLog.findBySchedulePayment(schedulePaymentId);
            context.sender().sendMessage(Message.raw("=== Logs del Pago Programado #" + schedulePaymentId + " ==="));
        } else {
            // Obtener logs recientes
            logs = ScheduleLog.getRecent(limit);
            context.sender().sendMessage(Message.raw("=== Últimos " + limit + " Logs de Pagos Programados ==="));
        }

        if (logs.isEmpty()) {
            context.sender().sendMessage(Message.raw("No hay logs para mostrar."));
            return CompletableFuture.completedFuture(null);
        }

        for (ScheduleLog log : logs) {
            String statusColor = log.getStatus().equals("success") ? "§a" : "§c";
            String statusText = log.getStatus().equals("success") ? "✓" : "✗";
            
            String invoiceInfo = log.getInvoiceId() != null ? " | Factura #" + log.getInvoiceId() : "";
            
            context.sender().sendMessage(Message.raw(
                statusColor + statusText + " [" + log.getExecutedAt() + "] " +
                "Pago #" + log.getSchedulePaymentId() + invoiceInfo + " - " + log.getMessage()
            ));
        }

        return CompletableFuture.completedFuture(null);
    }
}
