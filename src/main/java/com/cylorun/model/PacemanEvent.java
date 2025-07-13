package com.cylorun.model;

import java.util.List;

public class PacemanEvent {
    public final String _id;
    public final String name;
    public final long[] starts;
    public final int[] ends;
    public final List<String> whitelist;
    public final String vanity;
    public final List<CompletionPoints> points;

    public PacemanEvent(String _id, String name, long[] starts, int[] ends, List<String> whitelist, String vanity, List<CompletionPoints> points) {
        this._id = _id;
        this.name = name;
        this.starts = starts;
        this.ends = ends;
        this.whitelist = whitelist;
        this.vanity = vanity;
        this.points = points;
    }

    public static class CompletionPoints {
        public final long barrier;
        public final int points;

        public CompletionPoints(long barrier, int points) {
            this.barrier = barrier;
            this.points = points;
        }
    }
}
