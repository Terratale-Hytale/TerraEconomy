package terratale.commands;

import terratale.models.Bank;
import terratale.models.BankAccount;
import terratale.models.BankAccountOwner;
import terratale.models.BankInvitation;
import terratale.models.BankTransaction;
import terratale.models.Transaction;
import terratale.models.User;
import terratale.plugin.TerratalePlugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.entity.entities.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BankCommand extends AbstractCommandCollection {

    public BankCommand() {
        super("bank", "Manage your banks");

        addSubCommand(new BankCreateSubCommand());
        addSubCommand(new BankSetSubCommand());
        addSubCommand(new BankBalanceSubCommand());
        addSubCommand(new BankListSubCommand());
        addSubCommand(new AccountsListSubCommand());
        addSubCommand(new BankDepositSubCommand());
        addSubCommand(new BankWithdrawSubCommand());
        addSubCommand(new BankDeleteSubCommand());
        addSubCommand(new BankInviteSubCommand());
    }
}

// /bank create <name>
class BankCreateSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> nameArg;

    public BankCreateSubCommand() {
        super("create", "Create a new bank");
        nameArg = withRequiredArg("name", "Bank name", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        List<Bank> userBanks = User.findOrCreate(context.sender().getUuid(), context.sender().getDisplayName()).getBanks();

        if (userBanks.size() >= 1) {
            context.sender().sendMessage(Message.raw("No puedes crear más de 1 banco."));
            return CompletableFuture.completedFuture(null);
        }

        String bankName = nameArg.get(context);

        UUID playerUUID = context.sender().getUuid();
        String playerName = context.sender().getDisplayName();

        User user = User.findOrCreate(playerUUID, playerName);
        Double bankCost = TerratalePlugin.get().config().bankCreationCost;

        if (user.getMoney() < bankCost) {
            context.sender().sendMessage(Message.raw("No tienes suficientes monedas para crear un banco. Costo: " + bankCost + " monedas."));
            return CompletableFuture.completedFuture(null);
        }

        Bank bank = new Bank(bankName, playerUUID);
        bank.save();

        String gouvernmentAccount = TerratalePlugin.get().config().gouvernmentNumberAccount;
        BankAccount govAccount = BankAccount.findByAccountNumber(gouvernmentAccount);
        if (govAccount != null) {
            govAccount.setBalance(govAccount.getBalance() + bankCost);
            govAccount.save();

            BankTransaction govTransaction = new BankTransaction(govAccount.getBankId(), "deposit", bankCost, playerUUID.toString());
            govTransaction.save();
        }

        context.sender().sendMessage(Message.raw("Banco creado exitosamente!"));
        context.sender().sendMessage(Message.raw("Nombre: " + bankName));
        context.sender().sendMessage(Message.raw("ID: #" + bank.getId()));

        return CompletableFuture.completedFuture(null);
    }
}

// /bank set <bank_id> <fee_type> <value>
class BankSetSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> bankIdArg;
    private final RequiredArg<String> feeTypeArg;
    private final RequiredArg<String> valueArg;

    public BankSetSubCommand() {
        super("set", "Set bank configs");

        bankIdArg = withRequiredArg("bank_id", "Bank ID", ArgTypes.INTEGER);
        feeTypeArg = withRequiredArg("fee_type", "Fee type (withdrawal_fee/deposit_fee/transfer_fee/visibility)", ArgTypes.STRING);
        valueArg = withRequiredArg("value", "Fee value (0-100)", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        int bankId = bankIdArg.get(context);
        String feeType = feeTypeArg.get(context);
        String value = valueArg.get(context);
        double valueDouble = 0.0;

        if (!feeType.equals("visibility")) {
            try {
                valueDouble = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                context.sender().sendMessage(Message.raw("Valor de comisión inválido. Debe ser un número."));
                return CompletableFuture.completedFuture(null);
            }

            if (valueDouble < 0 || valueDouble > 100) {
                context.sender().sendMessage(Message.raw("El valor de la comisión debe estar entre 0 y 100."));
                return CompletableFuture.completedFuture(null);
            }
        }

        Bank bank = Bank.find(bankId);
        if (bank == null) {
            context.sender().sendMessage(Message.raw("Banco no encontrado"));
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = context.sender().getUuid();

        if (!bank.getOwnerUuid().equals(playerUUID)) {
            context.sender().sendMessage(Message.raw("No eres el dueño de este banco"));
            return CompletableFuture.completedFuture(null);
        }

        switch (feeType.toLowerCase()) {
            case "withdrawal_fee" -> {
                bank.setWithdrawFee(valueDouble);
                context.sender().sendMessage(Message.raw("Comisión de retiro actualizada a " + value + "%"));
            }
            case "deposit_fee" -> {
                bank.setDepositFee(valueDouble);
                context.sender().sendMessage(Message.raw("Comisión de depósito actualizada a " + value + "%"));
            }
            case "transfer_fee" -> {
                bank.setTransactionsFee(valueDouble);
                context.sender().sendMessage(Message.raw("Comisión de transferencia actualizada a " + value + "%"));
            }
            case "visibility" -> {
                String visibilityValue = value.toLowerCase();
                if (!visibilityValue.equals("public") && !visibilityValue.equals("private")) {
                    context.sender().sendMessage(Message.raw("Visibilidad inválida. Usa: public o private"));
                    return CompletableFuture.completedFuture(null);
                }
                bank.setVisibility(visibilityValue);
                context.sender().sendMessage(Message.raw("Visibilidad del banco actualizada a " + visibilityValue));
            }
            default -> {
                context.sender().sendMessage(Message.raw("Tipo de configuración inválido. Usa: withdrawal_fee, deposit_fee, transfer_fee o visibility"));
                return CompletableFuture.completedFuture(null);
            }
        }

        bank.save();
        return CompletableFuture.completedFuture(null);
    }
}

// /bank balance <bank_id>
class BankBalanceSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> bankIdArg;

    public BankBalanceSubCommand() {
        super("balance", "Check bank balance");
        bankIdArg = withRequiredArg("bank_id", "Bank ID", ArgTypes.INTEGER);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        int bankId = bankIdArg.get(context);

        Bank bank = Bank.find(bankId);
        if (bank == null) {
            context.sender().sendMessage(Message.raw("Banco no encontrado"));
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = context.sender().getUuid();

        if (!bank.getOwnerUuid().equals(playerUUID)) {
            context.sender().sendMessage(Message.raw("No eres el dueño de este banco"));
            return CompletableFuture.completedFuture(null);
        }

        context.sender().sendMessage(Message.raw("=== " + bank.getName() + " ==="));
        context.sender().sendMessage(Message.raw("Balance: " + String.format("%.2f", bank.getBalance()) + " monedas"));
        context.sender().sendMessage(Message.raw("Comisiones:"));
        context.sender().sendMessage(Message.raw("  - Retiro: " + bank.getWithdrawFee() + "%"));
        context.sender().sendMessage(Message.raw("  - Depósito: " + bank.getDepositFee() + "%"));
        context.sender().sendMessage(Message.raw("  - Transferencia: " + bank.getTransactionsFee() + "%"));
        return CompletableFuture.completedFuture(null);
    }
}

// /bank list
class BankListSubCommand extends AbstractAsyncCommand {

    public BankListSubCommand() {
        super("list", "List your banks");
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = context.sender().getUuid();
        String playerName = context.sender().getDisplayName();

        User user = User.findOrCreate(playerUUID, playerName);
        List<Bank> banks = user.getBanks();

        if (banks.isEmpty()) {
            context.sender().sendMessage(Message.raw("No tienes bancos creados."));
            context.sender().sendMessage(Message.raw("Usa /bank create <nombre> para crear uno."));
            return CompletableFuture.completedFuture(null);
        }

        context.sender().sendMessage(Message.raw("=== Tus Bancos ==="));

        for (Bank bank : banks) {
            context.sender().sendMessage(Message.raw(
                "#" + bank.getId() +
                " - " + bank.getName() +
                " (Balance: " + String.format("%.2f", bank.getBalance()) + " monedas)"
            ));
        }

        return CompletableFuture.completedFuture(null);
    }
}

class AccountsListSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> bankIdArg;

    public AccountsListSubCommand() {
        super("accounts", "List bank accounts");
        bankIdArg = withRequiredArg("bank_id", "Bank ID", ArgTypes.INTEGER);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

            UUID playerUUID = context.sender().getUuid();
            String playerName = context.sender().getDisplayName();
    
            User user = User.findOrCreate(playerUUID, playerName);
            Bank bank = Bank.find(bankIdArg.get(context));

            if (bank == null) {
                context.sender().sendMessage(Message.raw("Banco no encontrado"));
                return CompletableFuture.completedFuture(null);
            }

            if (!bank.getOwnerUuid().equals(playerUUID)) {
                context.sender().sendMessage(Message.raw("No eres el dueño de este banco"));
                return CompletableFuture.completedFuture(null);
            }

            List<BankAccount> accounts = BankAccount.findByBank(bank.getId());
    
            if (accounts.isEmpty()) {
                context.sender().sendMessage(Message.raw("No tienes cuentas bancarias."));
                return CompletableFuture.completedFuture(null);
            }
    
            context.sender().sendMessage(Message.raw("=== Tus Cuentas Bancarias ==="));
    
            for (BankAccount acc : accounts) {
                String accountNumber = acc.getAccountNumber();
                context.sender().sendMessage(Message.raw(
                    "Cuenta:" + accountNumber +
                    " - Banco ID: " + acc.getBankId() +
                    " (Balance: " + String.format("%.2f", acc.getBalance()) + " monedas)"
                ));
            }
        return CompletableFuture.completedFuture(null);
    }
}

// /bank deposit <bank_id> <amount>
class BankDepositSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> bankIdArg;
    private final RequiredArg<Double> amountArg;

    public BankDepositSubCommand() {
        super("deposit", "Deposit money into your bank");
        bankIdArg = withRequiredArg("bank_id", "Bank ID", ArgTypes.INTEGER);
        amountArg = withRequiredArg("amount", "Amount to deposit", ArgTypes.DOUBLE);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        int bankId = bankIdArg.get(context);
        double amount = amountArg.get(context);

        if (amount <= 0) {
            context.sender().sendMessage(Message.raw("La cantidad debe ser positiva."));
            return CompletableFuture.completedFuture(null);
        }

        Bank bank = Bank.find(bankId);
        if (bank == null) {
            context.sender().sendMessage(Message.raw("Banco no encontrado."));
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = context.sender().getUuid();

        if (!bank.getOwnerUuid().equals(playerUUID)) {
            context.sender().sendMessage(Message.raw("No eres el dueño de este banco."));
            return CompletableFuture.completedFuture(null);
        }

        User user = User.find(playerUUID);
        if (user == null) {
            context.sender().sendMessage(Message.raw("Usuario no encontrado."));
            return CompletableFuture.completedFuture(null);
        }

        if (user.getMoney() < amount) {
            context.sender().sendMessage(Message.raw("No tienes suficientes monedas."));
            return CompletableFuture.completedFuture(null);
        }

        double depositFee = bank.getDepositFee();
        double feeAmount = amount * (depositFee / 100.0);
        double actualDeposit = amount - feeAmount;

        // Update balances
        user.setMoney(user.getMoney() - amount);
        bank.setBalance(bank.getBalance() + actualDeposit);

        // Save changes
        user.saveMoney();
        bank.save();

        // Record transaction
        BankTransaction transaction = new BankTransaction(bankId, "deposit", actualDeposit, playerUUID.toString());
        transaction.save();

        context.sender().sendMessage(Message.raw("Depósito realizado exitosamente!"));
        context.sender().sendMessage(Message.raw("Cantidad depositada: " + String.format("%.2f", actualDeposit) + " monedas"));
        if (feeAmount > 0) {
            context.sender().sendMessage(Message.raw("Comisión aplicada: " + String.format("%.2f", feeAmount) + " monedas (" + depositFee + "%)"));
        }
        context.sender().sendMessage(Message.raw("Nuevo balance del banco: " + String.format("%.2f", bank.getBalance()) + " monedas"));

        return CompletableFuture.completedFuture(null);
    }
}

// /bank withdraw <bank_id> <amount>
class BankWithdrawSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> bankIdArg;
    private final RequiredArg<Double> amountArg;

    public BankWithdrawSubCommand() {
        super("withdraw", "Withdraw money from your bank");
        bankIdArg = withRequiredArg("bank_id", "Bank ID", ArgTypes.INTEGER);
        amountArg = withRequiredArg("amount", "Amount to withdraw", ArgTypes.DOUBLE);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        int bankId = bankIdArg.get(context);
        double amount = amountArg.get(context);

        if (amount <= 0) {
            context.sender().sendMessage(Message.raw("La cantidad debe ser positiva."));
            return CompletableFuture.completedFuture(null);
        }

        Bank bank = Bank.find(bankId);
        if (bank == null) {
            context.sender().sendMessage(Message.raw("Banco no encontrado."));
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = context.sender().getUuid();

        if (!bank.getOwnerUuid().equals(playerUUID)) {
            context.sender().sendMessage(Message.raw("No eres el dueño de este banco."));
            return CompletableFuture.completedFuture(null);
        }

        User user = User.find(playerUUID);
        if (user == null) {
            context.sender().sendMessage(Message.raw("Usuario no encontrado."));
            return CompletableFuture.completedFuture(null);
        }

        double withdrawFee = bank.getWithdrawFee();
        double feeAmount = amount * (withdrawFee / 100.0);
        double actualWithdraw = amount - feeAmount;

        if (bank.getBalance() < actualWithdraw) {
            context.sender().sendMessage(Message.raw("El banco no tiene suficientes fondos."));
            return CompletableFuture.completedFuture(null);
        }

        // Update balances
        bank.setBalance(bank.getBalance() - actualWithdraw);
        user.setMoney(user.getMoney() + actualWithdraw);

        // Save changes
        bank.save();
        user.saveMoney();

        // Record transaction
        BankTransaction transaction = new BankTransaction(bankId, "withdraw", actualWithdraw, playerUUID.toString());
        transaction.save();

        context.sender().sendMessage(Message.raw("Retiro realizado exitosamente!"));
        context.sender().sendMessage(Message.raw("Cantidad retirada: " + String.format("%.2f", actualWithdraw) + " monedas"));
        if (feeAmount > 0) {
            context.sender().sendMessage(Message.raw("Comisión aplicada: " + String.format("%.2f", feeAmount) + " monedas (" + withdrawFee + "%)"));
        }
        context.sender().sendMessage(Message.raw("Nuevo balance del banco: " + String.format("%.2f", bank.getBalance()) + " monedas"));

        return CompletableFuture.completedFuture(null);
    }
}

class BankInviteSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> bankIdArg;
    private final RequiredArg<String> targetPlayerArg;

    public BankInviteSubCommand() {
        super("invite", "Invite a player to your bank");
        bankIdArg = withRequiredArg("bank_id", "Bank ID", ArgTypes.INTEGER);
        targetPlayerArg = withRequiredArg("player_name", "Target Player Name", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        int bankId = bankIdArg.get(context);
        String targetPlayerName = targetPlayerArg.get(context);

        Bank bank = Bank.find(bankId);
        if (bank == null) {
            context.sender().sendMessage(Message.raw("Banco no encontrado."));
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = context.sender().getUuid();

        if (!bank.getOwnerUuid().equals(playerUUID)) {
            context.sender().sendMessage(Message.raw("No eres el dueño de este banco."));
            return CompletableFuture.completedFuture(null);
        }

        BankInvitation invitation = new BankInvitation(bankId, playerUUID);
        invitation.save();
        
        context.sender().sendMessage(Message.raw("Invitación enviada a " + targetPlayerName + " para unirse al banco #" + bankId));

        return CompletableFuture.completedFuture(null);
    }
}

// /bank delete <bank_id>
class BankDeleteSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> bankIdArg;

    public BankDeleteSubCommand() {
        super("delete", "Delete a bank and all its accounts");
        bankIdArg = withRequiredArg("bank_id", "Bank ID", ArgTypes.INTEGER);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {

        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        int bankId = bankIdArg.get(context);
        Bank bank = Bank.find(bankId);

        if (bank == null) {
            context.sender().sendMessage(Message.raw("Banco no encontrado."));
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = context.sender().getUuid();

        // Obtener cuenta del gobierno
        String gouvernmentAccount = TerratalePlugin.get().config().gouvernmentNumberAccount;
        BankAccount govAccount = BankAccount.findByAccountNumber(gouvernmentAccount);

        if (govAccount == null) {
            context.sender().sendMessage(Message.raw("Error: No se encontró la cuenta del gobierno."));
            return CompletableFuture.completedFuture(null);
        }

        // Obtener todas las cuentas del banco
        List<BankAccount> accounts = bank.getAccounts();
        double totalTransferred = 0.0;

        // Procesar cada cuenta
        for (BankAccount account : accounts) {
            // Transferir el balance de la cuenta al gobierno
            if (account.getBalance() > 0) {
                totalTransferred += account.getBalance();
                govAccount.setBalance(govAccount.getBalance() + account.getBalance());
            }

            // Eliminar los owners de la cuenta
            BankAccountOwner.deleteByAccount(account.getId());

            // Eliminar las transacciones de la cuenta
            List<Transaction> transactions = Transaction.findByAccount(account.getId());
            for (Transaction transaction : transactions) {
                transaction.delete();
            }

            // Eliminar la cuenta
            account.delete();
        }

        // Guardar la cuenta del gobierno con el nuevo balance
        if (totalTransferred > 0) {
            govAccount.save();
            
            // Registrar transacción en la cuenta del gobierno
            BankTransaction govTransaction = new BankTransaction(
                govAccount.getBankId(), 
                "bank_deletion", 
                totalTransferred, 
                playerUUID.toString()
            );
            govTransaction.save();
        }

        // Eliminar todas las transacciones del banco
        List<BankTransaction> bankTransactions = BankTransaction.findByBank(bankId);
        for (BankTransaction transaction : bankTransactions) {
            transaction.delete();
        }

        // Eliminar el banco
        bank.delete();

        context.sender().sendMessage(Message.raw("Banco eliminado exitosamente!"));
        context.sender().sendMessage(Message.raw("Cuentas eliminadas: " + accounts.size()));
        if (totalTransferred > 0) {
            context.sender().sendMessage(Message.raw("Fondos transferidos al gobierno: " + String.format("%.2f", totalTransferred) + " monedas"));
        }

        return CompletableFuture.completedFuture(null);
    }
}
