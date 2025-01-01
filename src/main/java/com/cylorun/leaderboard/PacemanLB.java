package com.cylorun.leaderboard;

import com.cylorun.TourneyMaster;
import com.cylorun.TourneyMasterOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PacemanLB  implements Runnable {

    public static final Path OUT_FILE = TourneyMasterOptions.getTrackerDir().resolve("lb.txt");
    private static final PacemanLB INSTANCE = new PacemanLB(TourneyMasterOptions.getInstance().paceman_eventid);
    private URI compmetionsURL;
    private PacemanLB(String eventId) {
        try {
            this.compmetionsURL = new URI( String.format("https://paceman.gg/api/get-event-completions?eventId=%s", eventId));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        TourneyMaster.log(Level.INFO, "Paceman leaderboard initiated for event: " + eventId);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this, 0L, 10L, TimeUnit.SECONDS);
    }

    public void setEventId(String id) {
        try {
            this.compmetionsURL = new URI( String.format("https://paceman.gg/api/get-event-completions?eventId=%s", id));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static PacemanLB getInstance() {
        return INSTANCE;
    }

    private static void writeToOut(String s) throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(OUT_FILE.toFile()));
        w.write(s);
        w.close();
    }

    @Override
    public void run() {
        if (!TourneyMasterOptions.getInstance().enable_paceman_lb) return;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(this.compmetionsURL)
                .GET()
                .build();

        StringBuilder lb = new StringBuilder();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body().equals("400")) {  // love paceman api error handling :)
                TourneyMaster.log(Level.SEVERE, "Failed to fetch completions with status code: " + response.statusCode());
                return;
            }

            JsonArray completions = JsonParser.parseString(response.body()).getAsJsonArray();
            for (int i = 0; i < completions.size() && i <= TourneyMasterOptions.getInstance().max_lb_entries; i++) {
                JsonElement e = completions.get(i);

                JsonObject completion = e.getAsJsonObject();
                String nick = completion.get("nickname").getAsString();
                String time = TourneyMaster.msToString(completion.get("time").getAsLong());
                lb.append(String.format("%s. %s %s\n", i + 1, nick, time));
            }

            writeToOut(lb.toString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
