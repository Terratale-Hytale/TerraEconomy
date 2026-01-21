package terratale.Helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final File configFile;
    private final ObjectMapper mapper;
    private PluginConfig config;

    public ConfigManager(File pluginFolder) {
        this.configFile = new File(pluginFolder, "config.json");
        this.mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void load() {
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                config = new PluginConfig();
                save();
            } else {
                config = mapper.readValue(configFile, PluginConfig.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar config.json", e);
        }
    }

    public void save() {
        try {
            mapper.writeValue(configFile, config);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar config.json", e);
        }
    }

    public PluginConfig getConfig() {
        return config;
    }
}
