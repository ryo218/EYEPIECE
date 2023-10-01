package EYEPIECE;

import java.util.ArrayList;

public class Requirement {
    private String name;
    private ArrayList<String> RTM;//actual traces
    private ArrayList<Rank> rankList;//predicted ranking

    public Requirement(String name) {
        this.name = name;
        RTM = new ArrayList<>();
        rankList = new ArrayList<>();
    }

    public void addRTM(String className) {
        RTM.add(className);
    }

    public void setRTM(ArrayList<String> RTM) {
        this.RTM = RTM;
    }

    public void setRankList(ArrayList<Rank> rankList) {
        this.rankList = rankList;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getRTM() {
        return RTM;
    }

    public ArrayList<Rank> getRankList() {
        return rankList;
    }
}
