package com.cylorun.obs;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class OBSController {
    private static OBSController instance;
    public static final File OBS_IN = TourneyMasterOptions.getTrackerDir().resolve("obsstate").toFile();
    public static final URL OBS_SCRIPT_DOWNLOAD_URL;

    static {
        try {
            OBS_SCRIPT_DOWNLOAD_URL = new URL("https://raw.githubusercontent.com/cylorun/tourney-master/refs/heads/main/obsscript/TMC.lua");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private OBSController() {
        this.writeOBSState("");
    }

    public static void downloadObsScript(Path downloadDir) {
        try {
            if (TourneyMaster.VERSION.equals("DEV")) { // use the local script when in a development environment
                Files.copy(Path.of("obsscript", "TMC.lua"), downloadDir.resolve("TMC.lua"), StandardCopyOption.REPLACE_EXISTING);

                TourneyMaster.log(Level.INFO, "Moved OBS Script");
                return;
            }

            Files.copy(OBS_SCRIPT_DOWNLOAD_URL.openStream(), downloadDir.resolve("TMC.lua"), StandardCopyOption.REPLACE_EXISTING);
            TourneyMaster.log(Level.INFO, "Downloaded OBS Script");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized OBSController getInstance() {
        if (instance == null) {
            instance = new OBSController();
        }

        return instance;
    }

    private synchronized void writeOBSState(String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OBS_IN))) {
            writer.write(data);
            writer.flush();
            TourneyMaster.log(Level.INFO, "Writing OBS State: " + data);
        } catch (IOException e) {
            TourneyMaster.log(Level.SEVERE, "Error writing to OBS state file: " + e.getMessage());
        }
    }

    private synchronized void sendOBS(String request) {
        this.writeOBSState(request);
    }

    public void openScene(String name) {
        this.sendOBS("SetActiveScene:" + name);
    }

    public void setBrowserSourceURL(String sourceName, String url) {
        this.sendOBS("SetBrowserSourceURL:" + sourceName + ";" + url);
    }

    public void genPlayerSources(String sceneName, int count)  {
        this.sendOBS("GenPlayerSources:" + sceneName + ";" + count);
    }

    public void editPlayerSource(int num, String newttv, String label) {
        this.editPlayerSource("Main", num, newttv, label);
    }

    public void editPlayerSource(String sceneName, int num, String newttv, String label) {
        this.sendOBS(String.format("EditPlayerSource:%s;%s;%s", sceneName, num, newttv));
    }

    public void swapPlayerSources(int src1, int src2) {
        this.swapPlayerSources("Main", src1, src2);
    }

    public void swapPlayerSources(String sceneName, int src1, int src2) {
        this.sendOBS(String.format("SwapPlayerSources:%s;%s;%s", sceneName, src1, src2));
    }
}
