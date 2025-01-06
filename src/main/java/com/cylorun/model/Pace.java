package com.cylorun.model;

import com.cylorun.paceman.Paceman;

public class Pace {
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
