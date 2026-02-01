package terratale.commands;

import terratale.Helpers.PorcentualHelper;
import terratale.models.Bank;
import terratale.models.User;
import terratale.pages.BanksPage;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

import java.math.BigDecimal;
import java.util.List;

public class CheckPorcentualCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> porcentualArg;

    public CheckPorcentualCommand() {
        super("checkporcentual", "Check porcentual points of all users");
        porcentualArg = withRequiredArg("porcentual", "Porcentual Points", ArgTypes.STRING);
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

        BigDecimal porcentualPoints;
        try {
            porcentualPoints = new BigDecimal(porcentualArg.get(context));
        } catch (NumberFormatException e) {
            context.sender().sendMessage(Message.raw("Invalid porcentual points format."));
            return;
        }

        Double amount = PorcentualHelper.calculatePorcentual(porcentualPoints);
        Double totalMoney = PorcentualHelper.getAllMoneyBank();

        context.sender().sendMessage(Message.raw("=== Porcentual Calculation ==="));
        context.sender().sendMessage(Message.raw("Points: " + porcentualPoints));
        context.sender().sendMessage(Message.raw("Amount: " + amount));
        context.sender().sendMessage(Message.raw("Total Money in Banks: " + String.format("%.2f", totalMoney)));
    }
}
