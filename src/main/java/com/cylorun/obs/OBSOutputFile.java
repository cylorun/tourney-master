package com.cylorun.obs;

import com.cylorun.TourneyMaster;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

public class OBSOutputFile extends Thread {
    private String lastOutput = "";
    private long lastLastModified = 0L;
    private final List<Consumer<String>> onChangeList;

    private static OBSOutputFile instance;

    private OBSOutputFile() {
        this.onChangeList = new ArrayList<>();
        this.start();
    }

    public static OBSOutputFile getInstance() {
        if (instance == null) {
             instance = new OBSOutputFile();
        }
        return instance;
    }

    private synchronized void notifyConsumers() {
        System.out.println("Change detected: " + this.lastOutput);
        for (Consumer<String> consumer : this.onChangeList) {
            consumer.accept(this.lastOutput);
        }
    }

    private synchronized String readOutput() {
        try (BufferedReader reader = new BufferedReader(new FileReader(OBSController.OBS_OUT))) {
            String output = reader.readLine();
            return (output != null) ? output.trim() : null;
        } catch (FileNotFoundException e) {
            TourneyMaster.log(Level.WARNING, "OBS output file not found!");
            return null;
        } catch (IOException e) {
            TourneyMaster.log(Level.SEVERE, "Error reading OBS output file: " + e.getMessage());
            return null;
        }
    }

    public void onOutputChange(Consumer<String> consumer) {
        this.onChangeList.add(consumer);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (OBSController.OBS_OUT.lastModified() != this.lastLastModified) {
                    String currentOutput = this.readOutput() ;
                    this.lastLastModified = OBSController.OBS_OUT.lastModified();

                    if (currentOutput == null) continue;
                    this.lastOutput = currentOutput;
                    this.notifyConsumers();
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
