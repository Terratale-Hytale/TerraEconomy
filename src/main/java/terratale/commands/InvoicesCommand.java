package terratale.commands;

import terratale.models.BankAccount;
import terratale.models.BankAccountOwner;
import terratale.models.Invoice;
import terratale.pages.InvoicesPage;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvoicesCommand extends AbstractPlayerCommand {

    private final OptionalArg<String> accountNumberArg;

    public InvoicesCommand() {
        super("invoices", "List all your invoices");
        accountNumberArg = withOptionalArg("account_number", "Account Number", ArgTypes.STRING);
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
        Player player = (Player) context.sender();
        UUID playerUUID = playerRef.getUuid();

        // Obtener todas las cuentas del jugador
        List<Integer> playerAccountIds = BankAccountOwner.getAccountsByOwner(playerUUID);

        if (playerAccountIds.isEmpty()) {
            context.sender().sendMessage(Message.raw("No tienes cuentas bancarias."));
        }

        // Obtener todas las facturas relacionadas con las cuentas del jugador
        List<Invoice> allInvoices = new ArrayList<>();

        if (accountNumberArg.get(context) != null) {
            String accNumber = accountNumberArg.get(context);
            BankAccount account = BankAccount.findByAccountNumber(accNumber);
            if (account == null || !playerAccountIds.contains(account.getId())) {
                context.sender().sendMessage(Message.raw("No tienes una cuenta con ese número."));
            }
            // Facturas recibidas y enviadas
            allInvoices.addAll(Invoice.findByPayerAccount(accNumber));
            allInvoices.addAll(Invoice.findByReceptorAccount(accNumber));
        } else {
            for (Integer accountId : playerAccountIds) {
                BankAccount account = BankAccount.find(accountId);
                if (account != null) {
                    String accountNumber = account.getAccountNumber();
                    allInvoices.addAll(Invoice.findByPayerAccount(accountNumber));
                    allInvoices.addAll(Invoice.findByReceptorAccount(accountNumber));
                }
            }
        }

        // Abrir la interfaz gráfica con todas las facturas
        InvoicesPage page = new InvoicesPage(playerRef, CustomPageLifetime.CanDismiss, allInvoices);
        player.getPageManager().openCustomPage(ref, store, page);
    }
}
