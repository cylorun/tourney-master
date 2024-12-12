package com.cylorun.model;

public class Player {
    public String twitch;
    public String ign;

    public Player(String twitch, String ign){
        this.twitch = twitch.trim();
        this.ign = ign.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Player p) {
            return p.ign.equalsIgnoreCase(this.ign) &&
                    p.twitch.equalsIgnoreCase(this.twitch);
        }

        return false;
    }

    @Override
    public String toString() {
        return "Player{" +
                "twitch='" + twitch + '\'' +
                ", ign='" + ign + '\'' +
                '}';
    }
}
