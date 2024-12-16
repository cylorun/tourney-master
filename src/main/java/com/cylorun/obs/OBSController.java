package com.cylorun.obs;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.logging.Level;

public class OBSController {
    private Stack<Consumer<String>> waitingRequests;
    private static OBSController instance;

    private OBSController() {
        this.waitingRequests = new Stack<>();
        OBSOutputFile.getInstance().onOutputChange(this::onOutputChange);
    }


    public static synchronized OBSController getInstance() {
        if (instance == null) {
            instance = new OBSController();
        }
        return instance;
    }

    private void writeOBSState(String data) {
        File obsstate = TourneyMasterOptions.getTrackerDir().resolve("obsstate").toFile();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(obsstate));

            writer.write(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onOutputChange(String c) {
        Consumer<String> lastConsumer = this.waitingRequests.pop();
        if (lastConsumer == null) {
            TourneyMaster.log(Level.WARNING, "Unexpected output change: " + c);
            return;
        }

        lastConsumer.accept(c);
    }

    private void sendAndGetOBS(String req, Consumer<String> res) {
        this.writeOBSState(req);

        this.waitingRequests.push(res);

    }

    private void sendOBS(String req) {
        this.writeOBSState(req);
    }

    public void openScene(String name) {
        this.sendOBS("SetActiveScene:" + name);
    }

    public void setBrowserSourceURL(String sourceName, String url) {
        this.sendOBS("SetBrowserSourceURL:" + sourceName + ";" + url);
    }

    public void getAllSceneNames(Consumer<List<String>> consumer) {
        this.sendAndGetOBS("GetAllScenes", (out) -> {
            if (out != null && !out.isEmpty()) {
                List<String> list = Arrays.stream(out.split(";")).toList();
                consumer.accept(list);
            } else {
                consumer.accept(List.of());
            }
        });
    }
}
