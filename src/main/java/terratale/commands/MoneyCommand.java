package terratale.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import java.util.List;
import java.util.ArrayList;

import terratale.models.User;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MoneyCommand extends AbstractCommandCollection {

    public MoneyCommand() {
        super("money", "Check and manage money");

        addSubCommand(new MoneyBalanceSubCommand());
        addSubCommand(new MoneySetSubCommand());
        addSubCommand(new MoneyWithdrawSubCommand());
        addSubCommand(new MoneyDepositSubCommand());
    }
}

// /money balance - mostrar balance propio
class MoneyBalanceSubCommand extends AbstractAsyncCommand {

    public MoneyBalanceSubCommand() {
        super("balance", "Check your money balance");
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
        String playerName = player.getDisplayName();

        User user = User.findOrCreate(playerUUID, playerName);

        double balance = user.getMoney();
        player.sendMessage(Message.raw("Tu balance en el bolsillo es: " + String.format("%.2f", balance) + " monedas"));

        return CompletableFuture.completedFuture(null);
    }
}

// /money set <username> <amount>
class MoneySetSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> usernameArg;
    private final RequiredArg<Double> amountArg;

    public MoneySetSubCommand() {
        super("set", "Set a player's money balance");
        usernameArg = withRequiredArg("username", "Player username", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to set", ArgTypes.DOUBLE);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        String targetUsername = usernameArg.get(context);
        double amount = amountArg.get(context);

        if (amount < 0) {
            context.sender().sendMessage(Message.raw("La cantidad no puede ser negativa"));
            return CompletableFuture.completedFuture(null);
        }

        User targetUser = User.findByUsername(targetUsername);

        if (targetUser == null) {
            context.sender().sendMessage(Message.raw("Jugador no encontrado"));
            return CompletableFuture.completedFuture(null);
        }

        targetUser.setMoney(amount);
        targetUser.saveMoney();

        context.sender().sendMessage(Message.raw("Balance de " + targetUsername + " establecido a " + String.format("%.2f", amount) + " monedas"));

        return CompletableFuture.completedFuture(null);
    }
}

// /money withdraw <amount>
class MoneyWithdrawSubCommand extends AbstractAsyncCommand {

    private final RequiredArg<Integer> amountArg;

    // Ajusta esto al ID REAL de tu item. Ejemplos comunes:
    // "Terratale_Coin"  (como tú lo llamas)
    // "terratale:coin"  (si usas namespace)
    private static final String COIN_ITEM_ID = "Terratale_Coin";

    public MoneyWithdrawSubCommand() {
        super("withdraw", "Withdraw money as Terratale_Coin items");
        amountArg = withRequiredArg("amount", "Amount to withdraw", ArgTypes.INTEGER);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        Player player = (Player) context.sender();
        UUID playerUUID = context.sender().getUuid();
        String playerName = player.getDisplayName();
        int amount = amountArg.get(context);

        if (amount <= 0) {
            player.sendMessage(Message.raw("La cantidad debe ser un número entero positivo."));
            return CompletableFuture.completedFuture(null);
        }

        User user = User.findOrCreate(playerUUID, playerName);
        double balance = user.getMoney();

        if (balance < amount) {
            player.sendMessage(Message.raw("No tienes suficiente dinero. Tu balance es: " + String.format("%.2f", balance) + " monedas"));
            return CompletableFuture.completedFuture(null);
        }

        // Restar el dinero
        user.setMoney(balance - amount);
        user.saveMoney();

        // Dar items al inventario (API actual: ItemStack por ID + cantidad)
        Inventory inventory = player.getInventory();

        try {
            ItemStack coinStack = new ItemStack(COIN_ITEM_ID, amount);
            ItemContainer invContainer = inventory.getStorage();
            invContainer.addItemStack(coinStack);

            player.sendMessage(Message.raw("Has retirado " + amount + " monedas como items " + COIN_ITEM_ID + "."));
        } catch (Exception e) {
            user.setMoney(balance);
            user.saveMoney();

            player.sendMessage(Message.raw("Error: no se pudo crear el item '" + COIN_ITEM_ID + "'. Se ha revertido el retiro."));
        }

        return CompletableFuture.completedFuture(null);
    }
}

class MoneyDepositSubCommand extends AbstractAsyncCommand {

    private static final String COIN_ITEM_ID = "Terratale_Coin";

    public MoneyDepositSubCommand() {
        super("deposit", "Deposit all Terratale_Coin items from inventory to money");
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        if (!(context.sender() instanceof Player)) {
            context.sender().sendMessage(Message.raw("Este comando solo puede usarse en juego."));
            return CompletableFuture.completedFuture(null);
        }

        Player player = (Player) context.sender();
        UUID playerUUID = context.sender().getUuid();
        String playerName = player.getDisplayName();

        User user = User.findOrCreate(playerUUID, playerName);

        Inventory inventory = player.getInventory();

        ItemContainer container = inventory.getCombinedEverything();

        class SlotQty {
            final short slot;
            final int qty;
            SlotQty(short slot, int qty) { this.slot = slot; this.qty = qty; }
        }

        List<SlotQty> toRemove = new ArrayList<>();
        final int[] totalDeposited = {0};

        container.forEach((slot, stack) -> {
            if (stack == null || stack.isEmpty()) return;

            if (COIN_ITEM_ID.equals(stack.getItemId())) { 
                int qty = stack.getQuantity();
                if (qty > 0) {
                    totalDeposited[0] += qty;
                    toRemove.add(new SlotQty(slot, qty));
                }
            }
        });

        if (totalDeposited[0] == 0) {
            player.sendMessage(Message.raw("No tienes items " + COIN_ITEM_ID + " en tu inventario."));
            return CompletableFuture.completedFuture(null);
        }

        int removed = 0;
        for (SlotQty s : toRemove) {
            container.removeItemStackFromSlot(s.slot, s.qty);
            removed += s.qty;
        }

        if (removed <= 0) {
            player.sendMessage(Message.raw("No se pudieron retirar las monedas del inventario."));
            return CompletableFuture.completedFuture(null);
        }

        double newBalance = user.getMoney() + removed;
        user.setMoney(newBalance);
        user.saveMoney();

        player.sendMessage(Message.raw(
            "Has depositado " + removed + " monedas. Nuevo balance: " + String.format("%.2f", user.getMoney()) + " monedas"
        ));

        return CompletableFuture.completedFuture(null);
    }
}
