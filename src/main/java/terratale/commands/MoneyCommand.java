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

    private final RequiredArg<Double> amountArg;

    // Ajusta esto al ID REAL de tu item. Ejemplos comunes:
    // "Terratale_Coin"  (como tú lo llamas)
    // "terratale:coin"  (si usas namespace)
    private static final String COIN_ITEM_ID = "Terratale_Coin";
    private static final String CENT_ITEM_ID = "Terratale_Cent";

    public MoneyWithdrawSubCommand() {
        super("withdraw", "Withdraw money as Terratale_Coin items");
        amountArg = withRequiredArg("amount", "Amount to withdraw", ArgTypes.DOUBLE);
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
        Double totalAmount = amountArg.get(context);
        int amount = (int) Math.floor(totalAmount);
        Double cents = totalAmount % 1.0;
        player.sendMessage(Message.raw("Intentando retirar: " + totalAmount + " monedas (" + amount + " " + COIN_ITEM_ID + " y " + String.format("%.0f", cents * 100) + " " + CENT_ITEM_ID + ")"));

        if (totalAmount <= 0) {
            player.sendMessage(Message.raw("La cantidad debe ser un número positivo."));
            return CompletableFuture.completedFuture(null);
        }

        User user = User.findOrCreate(playerUUID, playerName);
        double balance = user.getMoney();

        if (balance < totalAmount) {
            player.sendMessage(Message.raw("No tienes suficiente dinero. Tu balance es: " + String.format("%.2f", balance) + " monedas"));
            return CompletableFuture.completedFuture(null);
        }

        // Restar el dinero
        user.setMoney(balance - totalAmount);
        user.saveMoney();

        // Dar items al inventario (API actual: ItemStack por ID + cantidad)
        Inventory inventory = player.getInventory();

        try {
            ItemStack coinStack = new ItemStack(COIN_ITEM_ID, amount);
            ItemContainer invContainer = inventory.getStorage();

            if (cents > 0) {
                ItemStack centStack = new ItemStack(CENT_ITEM_ID, (int) Math.round(cents * 100));
                invContainer.addItemStack(centStack);
            }
            invContainer.addItemStack(coinStack);

            player.sendMessage(Message.raw("Has retirado " + totalAmount + "."));
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
    private static final String CENT_ITEM_ID = "Terratale_Cent";

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
        final double[] totalDeposited = {0.0};

        container.forEach((slot, stack) -> {
            if (stack == null || stack.isEmpty()) return;

            int qty = stack.getQuantity();
            if (qty > 0) {
                if (COIN_ITEM_ID.equals(stack.getItemId())) { 
                    totalDeposited[0] += qty;
                    toRemove.add(new SlotQty(slot, qty));
                } else if (CENT_ITEM_ID.equals(stack.getItemId())) {
                    totalDeposited[0] += qty * 0.01;
                    toRemove.add(new SlotQty(slot, qty));
                }
            }
        });

        if (totalDeposited[0] == 0.0) {
            player.sendMessage(Message.raw("No tienes items " + COIN_ITEM_ID + " o " + CENT_ITEM_ID + " en tu inventario."));
            return CompletableFuture.completedFuture(null);
        }

        int itemsRemoved = 0;
        for (SlotQty s : toRemove) {
            container.removeItemStackFromSlot(s.slot, s.qty);
            itemsRemoved += s.qty;
        }

        if (itemsRemoved <= 0) {
            player.sendMessage(Message.raw("No se pudieron retirar los items del inventario."));
            return CompletableFuture.completedFuture(null);
        }

        double newBalance = user.getMoney() + totalDeposited[0];
        user.setMoney(newBalance);
        user.saveMoney();

        player.sendMessage(Message.raw(
            "Has depositado " + String.format("%.2f", totalDeposited[0]) + " monedas. Nuevo balance: " + String.format("%.2f", user.getMoney()) + " monedas"
        ));

        return CompletableFuture.completedFuture(null);
    }
}
