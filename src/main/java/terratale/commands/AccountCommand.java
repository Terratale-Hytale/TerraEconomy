package terratale.commands;

import terratale.models.AccountInvitation;
import terratale.models.Bank;
import terratale.models.BankAccount;
import terratale.models.BankAccountOwner;
import terratale.models.BankInvitation;
import terratale.models.Transaction;
import terratale.models.BankTransaction;
import terratale.models.User;
import terratale.pages.AccountsPage;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AccountCommand extends AbstractCommandCollection {

    public AccountCommand() {
        super("account", "Manage your bank accounts");

        addSubCommand(new AccountCreateSubCommand());
        addSubCommand(new AccountListSubCommand());
        addSubCommand(new AccountWithdrawSubCommand());
        addSubCommand(new AccountDepositSubCommand());
        addSubCommand(new AccountTransferSubCommand());
        addSubCommand(new AccountInviteSubCommand());
        addSubCommand(new AccountInviteAcceptSubCommand());
        addSubCommand(new AccountInvitesSubCommand());
        addSubCommand(new AccountRemoveSubCommand());
    }
}

// Subcomando: /account create <bank_name>
class AccountCreateSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> bankNameArg;

    public AccountCreateSubCommand() {
        super("create", "Create a new account in a bank");
        bankNameArg = withRequiredArg("bank_name", "Bank name", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        String bankName = bankNameArg.get(context);
        UUID playerUUID = context.sender().getUuid();
        Player player = (Player) context.sender();

        // Buscar el banco por nombre
        Bank bank = Bank.findByName(bankName);
        if (bank == null) {
            if (player != null) {
                player.sendMessage(Message.raw("Banco no encontrado"));
            }
            return CompletableFuture.completedFuture(null);
        }

        BankInvitation invitation = BankInvitation.findByBankAndUser(bank.getId(), playerUUID);

        if (bank.getVisibility().equals("private") && invitation == null) {
            if (player != null) {
                player.sendMessage(Message.raw("No tienes permiso para crear una cuenta en este banco."));
            }
            return CompletableFuture.completedFuture(null);
        } 

        // Crear la cuenta
        BankAccount account = new BankAccount(bank.getId());
        account.save();

        invitation.delete();

        BankAccountOwner ownerLink = new BankAccountOwner(account.getId(), playerUUID);
        ownerLink.save();

        if (player != null) {
            player.sendMessage(Message.raw("Cuenta creada exitosamente!"));
            player.sendMessage(Message.raw("Banco: " + bank.getName()));
            player.sendMessage(Message.raw("ID de cuenta: #" + account.getId()));
            player.sendMessage(Message.raw("Balance: 0.00 monedas"));
        }

        return CompletableFuture.completedFuture(null);
    }
}

// Subcomando: /account list
class AccountListSubCommand extends AbstractPlayerCommand {

    public AccountListSubCommand() {
        super("list", "List all your accounts");
    }

    @Override
    @Nonnull
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
        }

        UUID playerUUID = context.sender().getUuid();
        Player player = (Player) context.sender();
        String playerName = player.getDisplayName();

        User user = User.findOrCreate(playerUUID, playerName);
        List<BankAccount> accounts = user.getBankAccounts();

        if (player != null) {
            if (accounts.isEmpty()) {
                player.sendMessage(Message.raw("No tienes cuentas bancarias."));
                player.sendMessage(Message.raw("Usa /account create <banco> para crear una."));
            } else {
                player.sendMessage(Message.raw("=== Tus Cuentas Bancarias ==="));
                for (BankAccount account : accounts) {
                    Bank bank = Bank.find(account.getBankId());
                    String bankName = bank != null ? bank.getName() : "Desconocido";
                    String accountNumber = account.getAccountNumber();
                    
                    player.sendMessage(Message.raw("Cuenta: " + accountNumber + 
                        " - " + bankName + 
                        " (Balance: " + String.format("%.2f", account.getBalance()) + " monedas)"));
                }
            }
        }

        AccountsPage page = new AccountsPage(
            playerRef,
            CustomPageLifetime.CanDismiss,
            accounts
        );

        player.getPageManager().openCustomPage(ref, store, page);
    }
}

// Subcomando: /account withdraw <account_id> <amount>
class AccountWithdrawSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> accountIdArg;
    private final RequiredArg<String> amountArg;

    public AccountWithdrawSubCommand() {
        super("withdraw", "Withdraw money from an account");
        accountIdArg = withRequiredArg("account_number", "Account Number", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to withdraw", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        String accountIdStr = accountIdArg.get(context);
        String amountStr = amountArg.get(context);
        
        UUID playerUUID = context.sender().getUuid();
        Player player = (Player) context.sender();

        // Parsear ID de cuenta
        String accountNumber = accountIdArg.get(context);

        // Parsear cantidad
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                if (player != null) {
                    player.sendMessage(Message.raw("La cantidad debe ser mayor a 0"));
                }
                return CompletableFuture.completedFuture(null);
            }
        } catch (NumberFormatException e) {
            if (player != null) {
                player.sendMessage(Message.raw("Cantidad inválida"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Buscar cuenta
        BankAccount account = BankAccount.findByAccountNumber(accountNumber);
        if (account == null) {
            if (player != null) {
                player.sendMessage(Message.raw("Cuenta no encontrada"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Buscar banco
        Bank bank = Bank.find(account.getBankId());
        if (bank == null) {
            if (player != null) {
                player.sendMessage(Message.raw("Banco no encontrado"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que el jugador sea el dueño de la cuenta
        if (!BankAccountOwner.getOwnersByAccount(account.getId()).contains(playerUUID)) {
            if (player != null) {
                player.sendMessage(Message.raw("No tienes acceso a esta cuenta"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que hay suficiente balance
        if (account.getBalance() < amount) {
            if (player != null) {
                player.sendMessage(Message.raw("Balance insuficiente"));
                player.sendMessage(Message.raw("Balance actual: " + String.format("%.2f", account.getBalance()) + " monedas"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Calcular comisión (usar comisión de cuenta o del banco)
        double withdrawFee = account.getWithdrawFee() != null ? 
            account.getWithdrawFee() : bank.getWithdrawFee();
        double feeAmount = amount * (withdrawFee / 100.0);
        double totalDeducted = amount + feeAmount;

        // Verificar que puede pagar la comisión
        if (account.getBalance() < totalDeducted) {
            if (player != null) {
                player.sendMessage(Message.raw("Balance insuficiente (incluyendo comisión de " + String.format("%.2f", feeAmount) + " monedas)"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Realizar retiro
        account.setBalance(account.getBalance() - totalDeducted);
        account.save();

        User user = User.find(playerUUID);
        if (user != null) {
            user.setMoney(user.getMoney() + amount);
            user.save();
        }

        Transaction transaction = new Transaction(
            account.getId(),
            "withdraw",
            totalDeducted,
            playerUUID.toString()
        );
        transaction.save();

        Double newBankBalance = bank.getBalance() + feeAmount;
        bank.setBalance(newBankBalance);
        bank.save();

        BankTransaction bankTransaction = new BankTransaction(
            bank.getId(),
            "withdraw",
            totalDeducted,
            playerUUID.toString()
        );
        bankTransaction.save();

        if (player != null) {
            player.sendMessage(Message.raw("Retiro exitoso!"));
            player.sendMessage(Message.raw("Cantidad retirada: " + String.format("%.2f", amount) + " monedas"));
            player.sendMessage(Message.raw("Comisión (" + withdrawFee + "%): " + String.format("%.2f", feeAmount) + " monedas"));
            player.sendMessage(Message.raw("Nuevo balance: " + String.format("%.2f", account.getBalance()) + " monedas"));
        }

        return CompletableFuture.completedFuture(null);
    }
}

// Subcomando: /account deposit <account_id> <amount>
class AccountDepositSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> accountIdArg;
    private final RequiredArg<String> amountArg;

    public AccountDepositSubCommand() {
        super("deposit", "Deposit money into an account");
        accountIdArg = withRequiredArg("account_number", "Account Number", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to deposit", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        String accountNumber = accountIdArg.get(context);
        String amountStr = amountArg.get(context);
        
        UUID playerUUID = context.sender().getUuid();
        Player player = (Player) context.sender();

        // Parsear cantidad
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                if (player != null) {
                    player.sendMessage(Message.raw("La cantidad debe ser mayor a 0"));
                }
                return CompletableFuture.completedFuture(null);
            }
        } catch (NumberFormatException e) {
            if (player != null) {
                player.sendMessage(Message.raw("Cantidad inválida"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Buscar cuenta
        BankAccount account = BankAccount.findByAccountNumber(accountNumber);
        if (account == null) {
            if (player != null) {
                player.sendMessage(Message.raw("Cuenta no encontrada"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Buscar banco
        Bank bank = Bank.find(account.getBankId());
        if (bank == null) {
            if (player != null) {
                player.sendMessage(Message.raw("Banco no encontrado"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que el jugador sea el dueño de la cuenta
        if (!BankAccountOwner.getOwnersByAccount(account.getId()).contains(playerUUID)) {
            if (player != null) {
                player.sendMessage(Message.raw("No tienes acceso a esta cuenta"));
            }
            return CompletableFuture.completedFuture(null);
        }

        // Calcular comisión (usar comisión de cuenta o del banco)
        double depositFee = account.getDepositFee() != null ? 
            account.getDepositFee() : bank.getDepositFee();
        double feeAmount = amount * (depositFee / 100.0);
        double netDeposit = amount;

        User user = User.find(playerUUID);
        if (user == null || user.getMoney() < amount + feeAmount) {
            if (player != null) {
                player.sendMessage(Message.raw("No tienes suficiente dinero para depositar esa cantidad."));
                if (user != null) {
                    player.sendMessage(Message.raw("Dinero disponible: " + String.format("%.2f", user.getMoney()) + " monedas"));
                }
            }
            return CompletableFuture.completedFuture(null);
        }

        // Deduct money from user
        user.setMoney(user.getMoney() - (amount + feeAmount));
        user.save();
        user.saveMoney();

        // Realizar depósito
        account.setBalance(account.getBalance() + netDeposit);
        account.save();

        Transaction transaction = new Transaction(
            account.getId(),
            "deposit",
            netDeposit + feeAmount,
            playerUUID.toString()
        );
        transaction.save();

        Double newBankBalance = bank.getBalance() + feeAmount;
        bank.setBalance(newBankBalance);
        bank.save();

        BankTransaction bankTransaction = new BankTransaction(
            bank.getId(),
            "deposit",
            netDeposit + feeAmount,
            playerUUID.toString()
        );
        bankTransaction.save();

        if (player != null) {
            player.sendMessage(Message.raw("Depósito exitoso!"));
            player.sendMessage(Message.raw("Cantidad depositada: " + String.format("%.2f", amount) + " monedas"));
            player.sendMessage(Message.raw("Comisión (" + depositFee + "%): " + String.format("%.2f", feeAmount) + " monedas"));
            player.sendMessage(Message.raw("Monto neto: " + String.format("%.2f", netDeposit) + " monedas"));
            player.sendMessage(Message.raw("Nuevo balance: " + String.format("%.2f", account.getBalance()) + " monedas"));
        }

        return CompletableFuture.completedFuture(null);
    }
}

// Subcomando: /account transfer <from_account> <to_account> <amount>
class AccountTransferSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> fromAccountArg;
    private final RequiredArg<String> toAccountArg;
    private final RequiredArg<String> amountArg;

    public AccountTransferSubCommand() {
        super("transfer", "Transfer money between accounts");
        fromAccountArg = withRequiredArg("from_account", "Source Account Number", ArgTypes.STRING);
        toAccountArg = withRequiredArg("to_account", "Destination Account Number", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to transfer", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        String fromAccountNumber = fromAccountArg.get(context);
        String toAccountNumber = toAccountArg.get(context);
        String amountStr = amountArg.get(context);
        
        UUID playerUUID = context.sender().getUuid();
        Player player = (Player) context.sender();

        // Parsear cantidad
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                player.sendMessage(Message.raw("La cantidad debe ser mayor a 0"));
                return CompletableFuture.completedFuture(null);
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Message.raw("Cantidad inválida"));
            return CompletableFuture.completedFuture(null);
        }

        // Buscar cuenta origen
        BankAccount fromAccount = BankAccount.findByAccountNumber(fromAccountNumber);
        if (fromAccount == null) {
            player.sendMessage(Message.raw("Cuenta origen no encontrada"));
            return CompletableFuture.completedFuture(null);
        }

        // Buscar cuenta destino
        BankAccount toAccount = BankAccount.findByAccountNumber(toAccountNumber);
        if (toAccount == null) {
            player.sendMessage(Message.raw("Cuenta destino no encontrada"));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que no sea la misma cuenta
        if (fromAccount.getId().equals(toAccount.getId())) {
            player.sendMessage(Message.raw("No puedes transferir a la misma cuenta"));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que el jugador tenga acceso a la cuenta origen
        if (!BankAccountOwner.getOwnersByAccount(fromAccount.getId()).contains(playerUUID)) {
            player.sendMessage(Message.raw("No tienes acceso a la cuenta origen"));
            return CompletableFuture.completedFuture(null);
        }

        // Buscar bancos
        Bank fromBank = Bank.find(fromAccount.getBankId());
        Bank toBank = Bank.find(toAccount.getBankId());
        
        if (fromBank == null || toBank == null) {
            player.sendMessage(Message.raw("Error al encontrar los bancos"));
            return CompletableFuture.completedFuture(null);
        }

        // Calcular comisión (usar comisión de cuenta origen o del banco origen)
        double transferFee = fromAccount.getTransactionsFee() != null ? 
            fromAccount.getTransactionsFee() : fromBank.getTransactionsFee();
        double feeAmount = amount * (transferFee / 100.0);
        double totalDeducted = amount + feeAmount;

        // Verificar que hay suficiente balance
        if (fromAccount.getBalance() < totalDeducted) {
            player.sendMessage(Message.raw("Balance insuficiente"));
            player.sendMessage(Message.raw("Balance actual: " + String.format("%.2f", fromAccount.getBalance()) + " monedas"));
            player.sendMessage(Message.raw("Cantidad + comisión: " + String.format("%.2f", totalDeducted) + " monedas"));
            return CompletableFuture.completedFuture(null);
        }

        // Realizar transferencia
        fromAccount.setBalance(fromAccount.getBalance() - totalDeducted);
        fromAccount.save();

        toAccount.setBalance(toAccount.getBalance() + amount);
        toAccount.save();

        // Registrar transacciones
        Transaction withdrawTransaction = new Transaction(
            fromAccount.getId(),
            "transfer_out",
            totalDeducted,
            playerUUID.toString()
        );
        withdrawTransaction.save();

        Transaction depositTransaction = new Transaction(
            toAccount.getId(),
            "transfer_in",
            amount,
            playerUUID.toString()
        );
        depositTransaction.save();

        // Agregar comisión al balance del banco origen
        fromBank.setBalance(fromBank.getBalance() + feeAmount);
        fromBank.save();

        BankTransaction bankTransaction = new BankTransaction(
            fromBank.getId(),
            "transfer_fee",
            feeAmount,
            playerUUID.toString()
        );
        bankTransaction.save();

        player.sendMessage(Message.raw("Transferencia exitosa!"));
        player.sendMessage(Message.raw("De: " + fromAccountNumber + " (" + fromBank.getName() + ")"));
        player.sendMessage(Message.raw("A: " + toAccountNumber + " (" + toBank.getName() + ")"));
        player.sendMessage(Message.raw("Cantidad transferida: " + String.format("%.2f", amount) + " monedas"));
        player.sendMessage(Message.raw("Comisión (" + transferFee + "%): " + String.format("%.2f", feeAmount) + " monedas"));
        player.sendMessage(Message.raw("Nuevo balance cuenta origen: " + String.format("%.2f", fromAccount.getBalance()) + " monedas"));

        return CompletableFuture.completedFuture(null);
    }
}

// Subcomando: /account invite <account_id> <username>
class AccountInviteSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> accountIdArg;
    private final RequiredArg<String> usernameArg;

    public AccountInviteSubCommand() {
        super("invite", "Invite a user to an account");
        accountIdArg = withRequiredArg("account_number", "Account Number", ArgTypes.STRING);
        usernameArg = withRequiredArg("username", "Username to invite", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        String accountNumber = accountIdArg.get(context);
        String targetUsername = usernameArg.get(context);
        
        UUID playerUUID = context.sender().getUuid();
        Player player = (Player) context.sender();


        // Buscar cuenta
        BankAccount account = BankAccount.findByAccountNumber(accountNumber);
        if (account == null) {
            player.sendMessage(Message.raw("Cuenta no encontrada"));
            return CompletableFuture.completedFuture(null);
        }

        // Buscar banco
        Bank bank = Bank.find(account.getBankId());
        if (bank == null) {
            player.sendMessage(Message.raw("Banco no encontrado"));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que el jugador sea el dueño del banco
        if (!bank.getOwnerUuid().equals(playerUUID)) {
            player.sendMessage(Message.raw("No tienes permiso para invitar usuarios a esta cuenta"));
            return CompletableFuture.completedFuture(null);
        }

        // Buscar usuario a invitar
        User targetUser = User.findByUsername(targetUsername);
        if (targetUser == null) {
            player.sendMessage(Message.raw("Usuario no encontrado"));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que no esté invitándose a sí mismo
        if (targetUser.getUuid().equals(playerUUID)) {
            player.sendMessage(Message.raw("No puedes invitarte a ti mismo"));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar si ya es dueño de la cuenta
        List<Integer> userAccounts = BankAccountOwner.getAccountsByOwner(targetUser.getUuid());
        if (userAccounts.contains(account.getId())) {
            player.sendMessage(Message.raw(targetUsername + " ya es dueño de esta cuenta"));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar si ya tiene una invitación pendiente
        AccountInvitation existingInvite = AccountInvitation.findPending(account.getId(), targetUser.getUuid());
        if (existingInvite != null) {
            player.sendMessage(Message.raw(targetUsername + " ya tiene una invitación pendiente para esta cuenta"));
            return CompletableFuture.completedFuture(null);
        }

        // Crear invitación
        AccountInvitation invitation = new AccountInvitation(account.getId(), targetUser.getUuid(), playerUUID);
        invitation.save();

        player.sendMessage(Message.raw("Invitación enviada a " + targetUsername + " para la cuenta #" + account.getId()));

        return CompletableFuture.completedFuture(null);
    }
}

// Subcomando: /account invite accept <account_id>
class AccountInviteAcceptSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> accountIdArg;

    public AccountInviteAcceptSubCommand() {
        super("accept", "Accept an account invitation");
        accountIdArg = withRequiredArg("account_number", "Account number", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        String accountIdStr = accountIdArg.get(context);
        
        UUID playerUUID = context.sender().getUuid();
        Player player = (Player) context.sender();

        // Parsear ID de cuenta
        String accountNumber = accountIdStr;
        BankAccount account = BankAccount.findByAccountNumber(accountNumber);

        if (account == null) {
            player.sendMessage(Message.raw("Cuenta no encontrada"));
            return CompletableFuture.completedFuture(null);
        }

        // Buscar invitación pendiente
        AccountInvitation invitation = AccountInvitation.findPending(account.getId(), playerUUID);
        if (invitation == null) {
            player.sendMessage(Message.raw("No tienes una invitación pendiente para esta cuenta"));
            return CompletableFuture.completedFuture(null);
        }

        // Buscar banco
        Bank bank = Bank.find(account.getBankId());
        if (bank == null) {
            player.sendMessage(Message.raw("§cBanco no encontrado"));
            return CompletableFuture.completedFuture(null);
        }

        // Aceptar invitación (crea el vínculo y elimina la invitación)
        invitation.accept();

        player.sendMessage(Message.raw("Invitación aceptada!"));
        player.sendMessage(Message.raw("Ahora tienes acceso a la cuenta #" + account.getAccountNumber() + " del banco " + bank.getName()));

        return CompletableFuture.completedFuture(null);
    }
}

// Subcomando: /account invites
class AccountInvitesSubCommand extends AbstractAsyncCommand {

    public AccountInvitesSubCommand() {
        super("invites", "List your pending account invitations");
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

        // Buscar invitaciones pendientes
        List<AccountInvitation> invitations = AccountInvitation.findByInvitedUser(playerUUID);

        if (invitations.isEmpty()) {
            player.sendMessage(Message.raw("No tienes invitaciones pendientes."));
            return CompletableFuture.completedFuture(null);
        }

        player.sendMessage(Message.raw("=== Invitaciones Pendientes ==="));
        for (AccountInvitation invitation : invitations) {
            BankAccount account = BankAccount.find(invitation.getAccountId());
            if (account == null) continue;
            
            Bank bank = Bank.find(account.getBankId());
            if (bank == null) continue;
            
            User inviter = User.find(invitation.getInviterUuid());
            String inviterName = inviter != null ? inviter.getUsername() : "Desconocido";
            
            player.sendMessage(Message.raw("Cuenta #" + invitation.getAccountId() + 
                " - Banco: " + bank.getName() + 
                " - Invitado por: " + inviterName));
            player.sendMessage(Message.raw("  Usa /account accept " + invitation.getAccountId() + " para aceptar"));
        }

        return CompletableFuture.completedFuture(null);
    }
}

// Subcomando: /account remove <account_number>
class AccountRemoveSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> accountNumberArg;

    public AccountRemoveSubCommand() {
        super("remove", "Remove an account (must have 0 balance)");
        accountNumberArg = withRequiredArg("account_number", "Account Number", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        String accountNumber = accountNumberArg.get(context);
        
        UUID playerUUID = context.sender().getUuid();
        Player player = (Player) context.sender();

        // Buscar cuenta
        BankAccount account = BankAccount.findByAccountNumber(accountNumber);
        if (account == null) {
            player.sendMessage(Message.raw("Cuenta no encontrada"));
            return CompletableFuture.completedFuture(null);
        }

        // Buscar banco
        Bank bank = Bank.find(account.getBankId());
        if (bank == null) {
            player.sendMessage(Message.raw("Banco no encontrado"));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que el jugador sea el dueño del banco
        if (!bank.getOwnerUuid().equals(playerUUID)) {
            player.sendMessage(Message.raw("Solo el dueño del banco puede eliminar cuentas"));
            return CompletableFuture.completedFuture(null);
        }

        // Verificar que la cuenta tenga balance 0
        if (account.getBalance() > 0) {
            player.sendMessage(Message.raw("No puedes eliminar una cuenta con fondos"));
            player.sendMessage(Message.raw("Balance actual: " + String.format("%.2f", account.getBalance()) + " monedas"));
            player.sendMessage(Message.raw("Retira todos los fondos antes de eliminar la cuenta"));
            return CompletableFuture.completedFuture(null);
        }

        // Eliminar vínculos de propietarios
        BankAccountOwner.deleteByAccount(account.getId());

        // Eliminar invitaciones pendientes
        List<AccountInvitation> invitations = AccountInvitation.findByAccount(account.getId());
        for (AccountInvitation invitation : invitations) {
            invitation.delete();
        }

        // Eliminar la cuenta
        account.delete();

        player.sendMessage(Message.raw("Cuenta " + accountNumber + " eliminada exitosamente"));
        player.sendMessage(Message.raw("Banco: " + bank.getName()));

        return CompletableFuture.completedFuture(null);
    }
}
