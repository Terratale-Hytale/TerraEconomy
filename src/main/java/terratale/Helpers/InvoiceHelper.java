package terratale.Helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;

import terratale.models.Bank;
import terratale.models.BankAccount;
import terratale.models.BankAccountOwner;
import terratale.models.BankTransaction;
import terratale.models.Invoice;
import terratale.models.Transaction;
import terratale.plugin.TerratalePlugin;
import terratale.responses.InvoicePaymentResponse;

public class InvoiceHelper {
    public static InvoicePaymentResponse payInvoice(int invoiceId, UUID playerUUID) {
        // Obtener las cuentas
        Invoice invoice = Invoice.find(invoiceId);
        if (invoice == null) {
            throw new RuntimeException("Factura no encontrada.");
        }

        if (!InvoiceStatus.PENDING.equals(invoice.getStatus())) {
            throw new RuntimeException("Esta factura ya fue procesada (estado: " + invoice.getStatus() + ").");
        }

        BankAccount payerAcc = BankAccount.findByAccountNumber(invoice.getPayerAccountNumber());
        BankAccount receptorAcc = BankAccount.findByAccountNumber(invoice.getReceptorAccountNumber());
        BankAccount govAccount = BankAccount.findByAccountNumber(TerratalePlugin.get().config().gouvernmentNumberAccount);

        if (payerAcc == null || receptorAcc == null) {
            throw new RuntimeException("Cuenta pagadora o receptora no encontrada.");
        }

        // Verificar que el jugador sea dueño de la cuenta pagadora
        List<Integer> playerAccounts = BankAccountOwner.getAccountsByOwner(playerUUID);
        if (!playerAccounts.contains(payerAcc.getId())) {
            throw new RuntimeException("El jugador no es dueño de la cuenta pagadora.");
        }

        // Obtener banco del pagador
        Bank payerBank = payerAcc.getBank();
        if (payerBank == null) {
            throw new RuntimeException("Banco del pagador no encontrado.");
        }

        // Calcular comisiones e impuestos
        int governmentFeePercent = TerratalePlugin.get().config().taxPercentage;
        double governmentFeeAmount = (invoice.getAmount() * governmentFeePercent) / 100.0;

        Double bankTransferFeePercent = payerAcc.getTransactionsFee() != null ? 
            payerAcc.getTransactionsFee() : payerBank.getTransactionsFee();
        double bankTransferFeeAmount = (invoice.getAmount() * bankTransferFeePercent) / 100.0;

        // Total a deducir de la cuenta del pagador
        double totalDeducted = invoice.getAmount() + bankTransferFeeAmount;

        // Verificar que haya suficiente saldo en la cuenta del pagador
        if (payerAcc.getBalance() < totalDeducted) {
            throw new RuntimeException("Saldo insuficiente en la cuenta pagadora.");
        }

        // Realizar la transferencia
        payerAcc.setBalance(payerAcc.getBalance() - totalDeducted);
        receptorAcc.setBalance(receptorAcc.getBalance() + (invoice.getAmount() - governmentFeeAmount));
        
        if (govAccount != null) {
            govAccount.setBalance(govAccount.getBalance() + governmentFeeAmount);
            govAccount.save();
        }
        
        // Agregar comisión al banco
        payerBank.setBalance(payerBank.getBalance() + bankTransferFeeAmount);
        
        payerAcc.save();
        receptorAcc.save();
        payerBank.save();

        // Registrar transacciones
        Transaction payerTransaction = new Transaction(
            payerAcc.getId(),
            TransactionTypes.INVOICE_WITHDRAWAL,
            totalDeducted,
            playerUUID.toString()
        );
        payerTransaction.save();

        Transaction receptorTransaction = new Transaction(
            receptorAcc.getId(),
            TransactionTypes.INVOICE_DEPOSIT,
            invoice.getAmount() - governmentFeeAmount,
            playerUUID.toString()
        );
        receptorTransaction.save();

        if (govAccount != null) {
            Transaction govTransaction = new Transaction(
                govAccount.getId(),
                TransactionTypes.GOVERNMENT_FEE,
                governmentFeeAmount,
                playerUUID.toString()
            );
            govTransaction.save();
        }

        BankTransaction bankTransaction = new BankTransaction(
            payerBank.getId(),
            TransactionTypes.TRANSFER_FEE,
            bankTransferFeeAmount,
            playerUUID.toString()
        );
        bankTransaction.save();

        // Marcar la factura como pagada
        invoice.markAsPaid(playerUUID.toString());

        InvoicePaymentResponse response = new InvoicePaymentResponse(
            invoice,
            totalDeducted,
            bankTransferFeeAmount,
            governmentFeeAmount,
            payerAcc.getBalance()
        );
        
        return response;
    }

    public static Boolean rejectInvoice(int invoiceId, UUID playerUUID) {
        // Obtener la factura
        Invoice invoice = Invoice.find(invoiceId);

        if (invoice == null) {
            throw new RuntimeException("Factura no encontrada.");
        }

        // Verificar que la factura esté pendiente
        if (!InvoiceStatus.PENDING.equals(invoice.getStatus())) {
            throw new RuntimeException("Esta factura ya fue procesada.");
        }

        // Obtener la cuenta pagadora
        BankAccount payerAcc = BankAccount.findByAccountNumber(invoice.getPayerAccountNumber());
        
        if (payerAcc == null) {
            throw new RuntimeException("Cuenta pagadora no encontrada.");
        }

        // Verificar que el jugador sea dueño de la cuenta pagadora
        List<Integer> playerAccounts = BankAccountOwner.getAccountsByOwner(playerUUID);
        if (!playerAccounts.contains(payerAcc.getId())) {
            throw new RuntimeException("El jugador no es dueño de la cuenta pagadora.");
        }

        // Marcar la factura como cancelada
        invoice.markAsCancelled(playerUUID.toString());

        return true;
    }
}
