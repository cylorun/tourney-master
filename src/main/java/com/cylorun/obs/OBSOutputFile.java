package com.cylorun.obs;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;
import com.cylorun.model.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

public class OBSOutputFile extends Thread implements Runnable {

    private String lastOutput = "";
    private List<Consumer<String>> onChangeList;

    private static final File OUT_FILE = TourneyMasterOptions.getTrackerDir().resolve("obsstate.out").toFile();
    private static final OBSOutputFile INSTANCE = new OBSOutputFile();
    private OBSOutputFile() {
        this.onChangeList = new ArrayList<>();
        this.start();
    }

    public static OBSOutputFile getInstance() {
        return INSTANCE;
    }

    private void notifyConsumers() {
        for (Consumer<String> c : this.onChangeList) {
            c.accept(this.lastOutput);
        }
    }

    private String readOutput() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(OUT_FILE));
            String out =  reader.readLine();
            reader.close();

            return out;
        } catch (FileNotFoundException e) {
            TourneyMaster.log(Level.WARNING, "obs output file not found!");
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onOutputChange(Consumer<String> consumer) {
        this.onChangeList.add(consumer);
    }

    @Override
    public void run() {
        while (true) {
            String out = this.readOutput();
            if (out != null && !out.equals(this.lastOutput)) {
                this.lastOutput = out;
                this.notifyConsumers();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
