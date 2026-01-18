package com.example.pages;

import com.example.content.MarkdownSection;
import com.example.content.block.*;
import com.example.content.inline.*;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

public class HelpPage extends InteractiveCustomUIPage<Void> {

    private final List<MarkdownSection> sections;

    public HelpPage(PlayerRef playerRef, List<MarkdownSection> sections) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, null);
        this.sections = sections;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store
    ) {
        // Página raíz
        cmd.append("Pages/Rules.ui");

        for (int i = 0; i < sections.size(); i++) {
            MarkdownSection section = sections.get(i);

            // ---------- SECCIÓN (H1) ----------
            cmd.append("#ContentList", "Pages/MarkdownSection.ui");
            String sectionSelector = "#ContentList[" + i + "]";

            cmd.set(sectionSelector + " #SecTitle.Text", section.title());

            int bodyIndex = 0;

            for (MarkdownBlock block : section.blocks()) {

                // ---------- SALTO DE LÍNEA ----------
                if (block instanceof LineBreakBlock) {
                    cmd.append(
                            sectionSelector + " #SecBody",
                            "Pages/blocks/LineBreak.ui"
                    );
                    bodyIndex++;
                    continue;
                }

                // ---------- H2 / H3 / H4 ----------
                if (block instanceof HeadingBlock heading) {

                    String uiPath = switch (heading.level()) {
                        case 2 -> "Pages/blocks/Heading2.ui";
                        case 3 -> "Pages/blocks/Heading3.ui";
                        case 4 -> "Pages/blocks/Heading4.ui";
                        default -> null;
                    };

                    if (uiPath != null) {
                        cmd.append(
                                sectionSelector + " #SecBody",
                                uiPath
                        );
                        cmd.set(
                                sectionSelector + " #SecBody[" + bodyIndex + "] #Text.Text",
                                heading.text()
                        );
                        bodyIndex++;
                    }

                    continue;
                }

                // ---------- SEPARADOR ----------
                if (block instanceof SeparatorBlock) {
                    cmd.append(
                            sectionSelector + " #SecBody",
                            "Pages/blocks/Separator.ui"
                    );
                    bodyIndex++;
                    continue;
                }

                // ---------- PÁRRAFO ----------
                if (block instanceof ParagraphBlock paragraph) {
                    cmd.append(
                            sectionSelector + " #SecBody",
                            "Pages/blocks/Paragraph.ui"
                    );

                    String paragraphSelector =
                            sectionSelector + " #SecBody[" + bodyIndex + "]";

                    int inlineIndex = 0;

                    for (InlineElement inline : paragraph.inlines()) {

                        // TEXTO NORMAL
                        if (inline instanceof TextInline text) {
                            cmd.append(
                                    paragraphSelector,
                                    "Pages/inline/Text.ui"
                            );
                            cmd.set(
                                    paragraphSelector + "[" + inlineIndex + "] #Text.Text",
                                    text.text()
                            );
                        }

                        // TEXTO EN NEGRITA
                        if (inline instanceof BoldInline bold) {
                            cmd.append(
                                    paragraphSelector,
                                    "Pages/inline/Bold.ui"
                            );
                            cmd.set(
                                    paragraphSelector + "[" + inlineIndex + "] #Text.Text",
                                    bold.text()
                            );
                        }

                        // ---------- CURSIVA ----------
                        if (inline instanceof ItalicInline italic) {
                            cmd.append(
                                    paragraphSelector,
                                    "Pages/inline/Italic.ui"
                            );
                            cmd.set(
                                    paragraphSelector + "[" + inlineIndex + "] #Text.Text",
                                    italic.text()
                            );
                        }

                        inlineIndex++;
                    }

                    bodyIndex++;
                }
            }
        }
    }
}
