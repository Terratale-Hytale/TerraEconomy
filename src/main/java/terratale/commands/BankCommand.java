package terratale.commands;

import terratale.models.Bank;
import terratale.models.BankAccount;
import terratale.models.User;

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

        Bank bank = new Bank(bankName, playerUUID);
        bank.save();

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
    private final RequiredArg<Double> valueArg;

    public BankSetSubCommand() {
        super("set", "Set bank fees");

        bankIdArg = withRequiredArg("bank_id", "Bank ID", ArgTypes.INTEGER);
        feeTypeArg = withRequiredArg("fee_type", "Fee type (withdrawal_fee/deposit_fee/transfer_fee)", ArgTypes.STRING);
        valueArg = withRequiredArg("value", "Fee value (0-100)", ArgTypes.DOUBLE);
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
        double value = valueArg.get(context);

        if (value < 0 || value > 100) {
            context.sender().sendMessage(Message.raw("El valor debe estar entre 0 y 100"));
            return CompletableFuture.completedFuture(null);
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
                bank.setWithdrawFee(value);
                context.sender().sendMessage(Message.raw("Comisión de retiro actualizada a " + value + "%"));
            }
            case "deposit_fee" -> {
                bank.setDepositFee(value);
                context.sender().sendMessage(Message.raw("Comisión de depósito actualizada a " + value + "%"));
            }
            case "transfer_fee" -> {
                bank.setTransactionsFee(value);
                context.sender().sendMessage(Message.raw("Comisión de transferencia actualizada a " + value + "%"));
            }
            default -> {
                context.sender().sendMessage(Message.raw("Tipo de comisión inválido. Usa: withdrawal_fee, deposit_fee o transfer_fee"));
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
