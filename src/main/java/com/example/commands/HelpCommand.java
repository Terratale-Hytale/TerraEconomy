package com.example.commands;

import com.example.content.MarkdownLoader;
import com.example.content.MarkdownParser;
import com.example.content.MarkdownSection;
import com.example.pages.HelpPage;
import com.example.plugin.TerratalePlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class HelpCommand extends AbstractPlayerCommand {

    public HelpCommand() {
        super("normas", "Say hello to the server");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());

        try {
            Path mdPath = TerratalePlugin.get()
                    .getDataDirectory()
                    .getParent()
                    .resolve("Terratale/views/rules/sections/normas.md");

            List<String> lines = null;
            try {
                lines = MarkdownLoader.loadLines(mdPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            List<MarkdownSection> sections = MarkdownParser.parse(lines);

            HelpPage helpPage = new HelpPage(
                    playerRef,
                    sections
            );

            player.getPageManager().openCustomPage(ref, store, helpPage);
        }
        catch (Exception e) {
            player.sendMessage(Message.raw("§cError loading rules."));
            e.printStackTrace();
        }
    }
}
