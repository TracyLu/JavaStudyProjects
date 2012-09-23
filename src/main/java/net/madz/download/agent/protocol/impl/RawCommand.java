package net.madz.download.agent.protocol.impl;

import java.util.LinkedList;
import java.util.List;

public class RawCommand {

    private String name;
    private List<String> options = new LinkedList<String>();
    private List<String> args = new LinkedList<String>();

    public void setName(String name) {
        this.name = name;
    }

    public void addOption(String option) {
        options.add(option);
    }

    public List<String> getOptions() {
        return options;
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }

    public void addArg(String arg) {
        this.args.add(arg);
    }
}
