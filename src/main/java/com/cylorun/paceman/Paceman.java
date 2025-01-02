package com.cylorun.paceman;

import com.cylorun.TourneyMaster;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class Paceman {

    private static final URI LIVERUNS_URI;

    static {
        try {
            LIVERUNS_URI = new URI("https://paceman.gg/api/ars/liveruns");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<JsonObject> getEventById(String eventId) {
        HttpClient client = HttpClient.newHttpClient();
        try {

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("https://paceman.gg/api/get-events"))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                TourneyMaster.log(Level.SEVERE, "Failed to fetch events from paceman.gg/api/ars/liveruns: code " + res.statusCode());
                return Optional.empty();
            }

            Optional<JsonElement> event = JsonParser.parseString(res.body()).getAsJsonArray()
                    .asList()
                    .stream()
                    .filter(evt -> evt.getAsJsonObject().get("_id").getAsString().equalsIgnoreCase(eventId))
                    .findFirst();

            if (event.isEmpty()) {
                TourneyMaster.log(Level.SEVERE, "Event with this id does not exist: " + eventId);
                return Optional.empty();
            }

            return Optional.of(event.get().getAsJsonObject());
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<String> getPlayersForEvent(String eventId) {
        Optional<JsonObject> event = getEventById(eventId);
        return event.map(jsonObject -> jsonObject.get("whitelist").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList()).orElseGet(List::of);
    }

    public static Pace getLastEventPace(JsonObject runObj) {
        JsonElement ttv = runObj.getAsJsonObject("user").get("liveAccount");
        if (ttv == null ||ttv.isJsonNull()) {
            return null;
        }

        JsonArray list = runObj.getAsJsonArray("eventList");
        JsonObject lastEvent = list.get(list.size() - 1).getAsJsonObject();

        return new Pace(ttv.getAsString(), lastEvent.get("eventId").getAsString(), lastEvent.get("igt").getAsLong());
    }

    public static String getSplitDesc(String split) {
        Map<String, String> paceDescriptions = new HashMap<>();
        paceDescriptions.put("rsg.enter_nether", "The Nether");
        paceDescriptions.put("rsg.enter_bastion", "Bastion");
        paceDescriptions.put("rsg.enter_fortress", "Fortress");
        paceDescriptions.put("rsg.first_portal", "First Portal");
        paceDescriptions.put("rsg.second_portal", "Second Portal");
        paceDescriptions.put("rsg.enter_stronghold", "Stronghold");
        paceDescriptions.put("rsg.enter_end", "The End");
        paceDescriptions.put("rsg.credits", "Finish!");
        return paceDescriptions.get(split);
    }


    public static List<JsonObject> getPaceForEvent(String eventId) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(LIVERUNS_URI)
                .GET()
                .build();

        List<String> players = getPlayersForEvent(eventId);
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                TourneyMaster.log(Level.SEVERE, "Failed to fetch runs from paceman.gg/api/ars/liveruns: code " + res.statusCode());
                return List.of();
            }

            return JsonParser.parseString(res.body()).getAsJsonArray()
                    .asList()
                    .stream()
                    .map(JsonElement::getAsJsonObject)
                    .filter(run -> players.contains(run.getAsJsonObject("user").get("uuid").getAsString().replace("-", "")))
                    .toList();

        } catch (IOException | InterruptedException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }


    public static class Pace {
        public String runner;
        public String split;
        public long lastTime;

        public Pace(String runner, String split, long lastTime) {
            this.runner = runner;
            this.split = Paceman.getSplitDesc(split);
            this.lastTime = lastTime;
        }

        @Override
        public String toString() {
            return "Pace{" +
                    "runner='" + runner + '\'' +
                    ", split='" + split + '\'' +
                    ", lastTime=" + lastTime +
                    '}';
        }
    }
}
