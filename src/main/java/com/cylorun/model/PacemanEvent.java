package com.cylorun.model;

import java.util.List;

public class PacemanEvent {
    public final String _id;
    public final String name;
    public final long[] starts;
    public final int[] ends;
    public final List<String> whitelist;
    public final String vanity;

    public PacemanEvent(String _id, String name, long[] starts, int[] ends, List<String> whitelist, String vanity) {
        this._id = _id;
        this.name = name;
        this.starts = starts;
        this.ends = ends;
        this.whitelist = whitelist;
        this.vanity = vanity;
    }
}
