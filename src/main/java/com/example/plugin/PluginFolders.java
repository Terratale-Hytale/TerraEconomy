package com.example.plugin;

import java.nio.file.Path;
import java.util.logging.Level;

public final class PluginFolders {

    private PluginFolders() {
        // Evita instanciación
    }

    public static Path setup(TerratalePlugin plugin) {
        try {
            Path modsDir = plugin.getDataDirectory().getParent();
            Path baseDir = modsDir.resolve("Terratale");

            createDir(plugin, baseDir);
            createDir(plugin, baseDir.resolve("views"));
            createDir(plugin,
                    baseDir
                        .resolve("views")
                        .resolve("rules"));
            createDir(plugin,
                    baseDir
                        .resolve("views")
                        .resolve("rules")
                        .resolve("sections"));

            return baseDir;

        } catch (Exception e) {
            plugin.getLogger()
                    .at(Level.SEVERE)
                    .withCause(e)
                    .log("Error while setting up plugin folders");
            return null;
        }
    }

    private static void createDir(TerratalePlugin plugin, Path dir) {
        if (dir.toFile().exists()) {
            return;
        }

        if (dir.toFile().mkdirs()) {
            plugin.getLogger()
                    .at(Level.INFO)
                    .log("Created directory: " + dir);
        } else {
            plugin.getLogger()
                    .at(Level.WARNING)
                    .log("Failed to create directory: " + dir);
        }
    }
}
