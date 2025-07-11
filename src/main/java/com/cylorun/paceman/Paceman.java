package com.cylorun.paceman;

import com.cylorun.TourneyMaster;
import com.cylorun.model.Pace;
import com.cylorun.model.PacemanEvent;
import com.google.gson.*;

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

    public static List<PacemanEvent> getAllPacemanEvents() {
        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("https://paceman.gg/api/get-events"))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println(res);
            PacemanEvent[] events = new Gson().fromJson(res.body(), PacemanEvent[].class);

            return List.of(events);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            TourneyMaster.log(Level.SEVERE, "Failed to fetch paceman events");

            return List.of();
        } catch (JsonParseException e) {
            TourneyMaster.log(Level.SEVERE, "Failed to parse JSON payload from paceman.gg/api/get-events");

            return List.of();
        }
    }

    public static Optional<PacemanEvent> getEventById(String eventId) {
        Optional<PacemanEvent> event = Paceman.getAllPacemanEvents().stream()
                .filter(evt -> evt._id.equalsIgnoreCase(eventId))
                .findFirst();

        if (event.isEmpty()) {
            TourneyMaster.log(Level.SEVERE, "Event with this id does not exist: " + eventId);
            return Optional.empty();
        }

        return Optional.of(event.get());
    }

    public static Optional<PacemanEvent> getEventByVanity(String vanity) {
        Optional<PacemanEvent> event = Paceman.getAllPacemanEvents().stream()
                .filter(evt -> evt.vanity.equalsIgnoreCase(vanity))
                .findFirst();

        if (event.isEmpty()) {
            TourneyMaster.log(Level.SEVERE, "Event with this vanity does not exist: " + vanity);
            return Optional.empty();
        }

        return event;
    }

    public static List<String> getPlayersForEvent(String eventId) {
        Optional<PacemanEvent> event = getEventById(eventId);
        return event.map((d) -> d.whitelist).orElse(List.of());
    }

    /**
     * @param runObj Paceman live run data, from https://paceman.gg/api/ars/liveruns
     * @return a Pace object, containing data for the last event in a run
     */
    public static Pace getLatestSplitPace(JsonObject runObj) {
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
        if (players == null || players.isEmpty()) {
            return null;
        }

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
                    .filter(run -> players.contains(run.getAsJsonObject("user").get("uuid").getAsString()))
                    .toList();

        } catch (IOException | InterruptedException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }
}
