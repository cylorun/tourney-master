package com.cylorun.obs;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class OBSController {
    private final Deque<Consumer<String>> waitingRequests;
    private static OBSController instance;

    public static final File OBS_IN = TourneyMasterOptions.getTrackerDir().resolve("obsstate").toFile();
    public static final File OBS_OUT = TourneyMasterOptions.getTrackerDir().resolve("obsstate.out").toFile();
    public static final URL OBS_SCRIPT_DOWNLOAD_URL;

    static {
        try {
            OBS_SCRIPT_DOWNLOAD_URL = new URL("https://raw.githubusercontent.com/cylorun/tourney-master/refs/heads/main/obsscript/TMC.lua");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private OBSController() {
        this.waitingRequests = new ArrayDeque<>();
        this.writeOBSState("");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//        OBSOutputFile.getInstance().onOutputChange(this::onOutputChange);
    }

    public static void downloadObsScript(Path downloadDir) {
        try {
            if (TourneyMaster.VERSION.equals("DEV")) {
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
        } catch (IOException e) {
            TourneyMaster.log(Level.SEVERE, "Error writing to OBS state file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private synchronized void onOutputChange(String output) {
        if (this.waitingRequests.isEmpty()) {
            TourneyMaster.log(Level.WARNING, "Unexpected OBS output: " + output);
            return;
        }

        Consumer<String> consumer = this.waitingRequests.poll();
        if (consumer != null) {
            consumer.accept(output);
        }
    }

    private synchronized void sendAndGetOBS(String request, Consumer<String> responseHandler) {
        this.waitingRequests.offer(responseHandler);
        this.writeOBSState(request);
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

    public void editPlayerSource(int num, String newttv) {
        this.editPlayerSource("Main", num, newttv);
    }

    public void editPlayerSource(String sceneName, int num, String newttv) {
        this.sendOBS(String.format("EditPlayerSource:%s;%s;%s", sceneName, num, newttv));
    }
    public void swapSources( int src1, int src2) {
        this.swapSources("Main", src1, src2);
    }
    public void swapSources(String sceneName, int src1, int src2) {
        this.sendOBS(String.format("SwapPlayerSources:%s;%s;%s", sceneName, src1, src2));
    }

    public void getAllSceneNames(Consumer<List<String>> consumer) {
       consumer.accept(List.of("Main", "Everyone", "Intermission")); // will implement proper responses at some point
//        this.sendAndGetOBS("GetAllScenes", (output) -> {
//            if (output != null && !output.isEmpty()) {
//                List<String> scenes = Arrays.asList(output.split(";"));
//                consumer.accept(scenes);
//            } else {
//                consumer.accept(Collections.emptyList());
//            }
//        })
    }
}
