package gg.vape.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gg.vape.Vape;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LocalConfigStore {
    private static final String CONFIG_FILE_NAME = "config.json";
    private final File configFile;

    public LocalConfigStore() {
        String baseDirectoryPath = System.getProperty("user.home");
        String clientDirectoryPath = baseDirectoryPath + File.separator + ".vapeclient";
        File clientDirectory = new File(clientDirectoryPath);
        if (!clientDirectory.exists()) {
            clientDirectory.mkdirs();
        }
        this.configFile = new File(clientDirectory, CONFIG_FILE_NAME);
    }

    public File getConfigFile() {
        return this.configFile;
    }

    public void save(JsonObject config) {
        try {
            File parentDirectory = this.configFile.getParentFile();
            if (parentDirectory != null && !parentDirectory.exists()) {
                parentDirectory.mkdirs();
            }
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(config);
            File tempFile = new File(parentDirectory, CONFIG_FILE_NAME + ".tmp");
            Files.write(tempFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
            Files.move(tempFile.toPath(), this.configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException iOException) {
            Vape.logThrowable(iOException);
        }
    }

    public JsonObject load() {
        try {
            if (!this.configFile.exists()) {
                return null;
            }
            String json = new String(Files.readAllBytes(this.configFile.toPath()), StandardCharsets.UTF_8);
            if (json.trim().isEmpty()) {
                return null;
            }
            return (JsonObject)new Gson().fromJson(json, JsonObject.class);
        }
        catch (Exception exception) {
            Vape.logThrowable(exception);
            return null;
        }
    }
}
