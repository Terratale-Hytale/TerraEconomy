package terratale.commands;

import terratale.models.Bank;
import terratale.models.User;
import terratale.pages.BanksPage;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BanksCommand extends AbstractPlayerCommand {

    public BanksCommand() {
        super("banks", "List all created banks");
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

        List<Bank> banks = Bank.findAll();
        Player player = (Player) context.sender();

        if (banks.isEmpty()) {
            context.sender().sendMessage(Message.raw("No hay bancos creados."));
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

        BanksPage banksPage = new BanksPage(
            playerRef, 
            CustomPageLifetime.CanDismiss, 
            banks);

        player.getPageManager().openCustomPage(ref, store, banksPage);
    }
}
