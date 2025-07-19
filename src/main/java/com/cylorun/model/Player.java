package com.cylorun.model;

public class Player {
    private final String twitch;
    private final String label;

    public Player(String twitch, String label){
        this.twitch = twitch;
        this.label = label;
    }

    public String getTwitch() {
        return this.twitch == null ? "" : this.twitch.trim();
    }

    public String getLabel() {
        return this.label == null || this.label.isBlank() ? this.twitch : this.label.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Player p) {
            return p.label.equalsIgnoreCase(this.label) &&
                    p.twitch.equalsIgnoreCase(this.twitch);
        }

        return false;
    }

    @Override
    public String toString() {
        return "Player{" +
                "twitch='" + twitch + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
