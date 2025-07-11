package com.cylorun.model;

public class Player {
    public String twitch;
    public String label;

    public Player(String twitch, String label){
        this.twitch = twitch == null ? "" : twitch.trim();
        this.label = label == null ? "" : label.trim();
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
