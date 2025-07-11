package com.cylorun;

import com.cylorun.model.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TourneyMasterOptions {

    public int rows = 3;
    public int cols = 3;
    public int max_lb_entries = 12;
    public boolean enable_commentators = false;
    public boolean enable_paceman_lb = false;
    public String paceman_eventid = "";
    public String paceman_eventvanity = "";
    public List<Player> players = new ArrayList<>();
    public String lastView = "host"; // commentator | host
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = getTrackerDir().resolve("config.json");
    private static TourneyMasterOptions instance;

    public synchronized static TourneyMasterOptions getInstance() {
        if (instance == null) {
            ensureConfigDir();
            if (Files.exists(CONFIG_PATH)) {
                try {
                    instance = GSON.fromJson(new String((Files.readAllBytes(CONFIG_PATH))), TourneyMasterOptions.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                instance = new TourneyMasterOptions();
            }

        }
        return instance;
    }

    public static void ensureConfigDir() {
        if (!getTrackerDir().toFile().exists()) {
            getTrackerDir().toFile().mkdirs();
        }
    }

    public static Path getTrackerDir() {
        return Paths.get(System.getProperty("user.home"), ".tourneymaster");
    }

    public static void save() {
        FileWriter writer;
        try {
            writer = new FileWriter(CONFIG_PATH.toFile());
            GSON.toJson(instance, writer);
            writer.close();
        } catch (IOException e) {
            TourneyMaster.log(Level.SEVERE, "Failed to write to config file: " + e.getMessage());
        }
    }
}
