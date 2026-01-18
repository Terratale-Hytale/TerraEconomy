package terratale.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class TerrataleCommand extends AbstractCommandCollection {

    public TerrataleCommand() {
        super("terratale", "Terratale plugin management");

        addSubCommand(new UpdateSubCommand());
    }
}

// Subcomando: /terratale update
class UpdateSubCommand extends AbstractAsyncCommand {

    private static final String REPO_URL = "https://github.com/Terratale-Hytale/TerraEconomy.git";
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/terratale-update";
    private static final String GITHUB_TOKEN = "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";
    private static final String GITHUB_USERNAME = "pablixtico";

    public UpdateSubCommand() {
        super("update", "Update plugin from GitHub");
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        context.sender().sendMessage(Message.raw("[TerraEconomy] Iniciando actualización..."));

        return CompletableFuture.runAsync(() -> {
            try {
                // Paso 1: Limpiar directorio temporal si existe
                File tempDir = new File(TEMP_DIR);
                if (tempDir.exists()) {
                    context.sender().sendMessage(Message.raw("[TerraEconomy] Limpiando directorio temporal..."));
                    deleteDirectory(tempDir);
                }

                // Paso 2: Clonar repositorio usando JGit (no requiere git instalado)
                context.sender().sendMessage(Message.raw("[TerraEconomy] Descargando última versión..."));
                
                try {
                    Git git = Git.cloneRepository()
                        .setURI(REPO_URL)
                        .setDirectory(tempDir)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_USERNAME, GITHUB_TOKEN))
                        .call();
                    
                    git.close();
                    context.sender().sendMessage(Message.raw("[TerraEconomy] Descarga completada"));
                    
                } catch (GitAPIException e) {
                    context.sender().sendMessage(Message.raw("[TerraEconomy] Error al descargar: " + e.getMessage()));
                    return;
                }

                // Paso 3: Copiar JAR a carpeta de mods
                context.sender().sendMessage(Message.raw("[TerraEconomy] Instalando nueva versión..."));
                
                File buildJar = findJarFile(new File(tempDir, "build/libs"));
                if (buildJar == null) {
                    context.sender().sendMessage(Message.raw("[TerraEconomy] No se encontró el archivo JAR compilado"));
                    return;
                }

                // Obtener directorio de mods (dos niveles arriba del plugin)
                File currentJar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                File modsDir = currentJar.getParentFile();
                
                // Eliminar JAR antiguo
                if (currentJar.exists()) {
                    currentJar.delete();
                }
                
                // Copiar nuevo JAR
                File newJar = new File(modsDir, buildJar.getName());
                Files.copy(buildJar.toPath(), newJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

                context.sender().sendMessage(Message.raw("[TerraEconomy] ¡Plugin actualizado exitosamente!"));
                context.sender().sendMessage(Message.raw("[TerraEconomy] Reinicia el servidor para aplicar los cambios"));

                // Paso 5: Limpiar directorio temporal
                deleteDirectory(tempDir);

            } catch (Exception e) {
                context.sender().sendMessage(Message.raw("[TerraEconomy] Error durante la actualización: " + e.getMessage()));
                e.printStackTrace();
            }
        }).thenRun(() -> {
            // Este código se ejecuta después de que termine la actualización
        });
    }

    private File findJarFile(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar") && !name.contains("sources") && !name.contains("javadoc"));
        return (files != null && files.length > 0) ? files[0] : null;
    }

    private void deleteDirectory(File dir) {
        if (!dir.exists()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    private String readProcessOutput(java.io.InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        } catch (Exception e) {
            return "Error al leer output: " + e.getMessage();
        }
    }
}
