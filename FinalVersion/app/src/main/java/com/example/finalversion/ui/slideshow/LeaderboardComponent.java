package com.example.finalversion.ui.slideshow;

import java.util.Comparator;

public class LeaderboardComponent{
    private String name;
    private int weeklyScore;
    private int monthlyScore;
    private int allTimeScore;
    private String weeklyRank;
    private String monthlyRank;
    private String allTimeRank;

    public LeaderboardComponent(String weeklyrank, String monthlyrank, String alltimerank,String name, int weeklyscore, int monthlyscore,int alltimescore) {
        weeklyRank = "#" + weeklyrank;
        monthlyRank = "#" + monthlyrank;
        allTimeRank = "#" + alltimerank;
        this.name = name;
        weeklyScore = weeklyscore;
        monthlyScore = monthlyscore;
        allTimeScore = alltimescore;
    }

    public String getName() {
        return name;
    }

    public String getWeeklyScore() {
        return String.valueOf(weeklyScore);
    }

    public String getMonthlyScore() {
        return String.valueOf(monthlyScore);
    }

    public String getAllTimeScore() {
        return String.valueOf(allTimeScore);
    }

    public String getWeeklyRank() {
        return weeklyRank;
    }

    public void setWeeklyRank(String i) {
        weeklyRank = i;
    }

    public String getMonthlyRank() {
        return monthlyRank;
    }

    public void setMonthlyRank(String i) {
        monthlyRank = i;
    }

    public String getAllTimeRank() { return allTimeRank; }

    public void setAllTimeRank(String i) {
        allTimeRank = i;
    }
}

class WeeklyScoreComparatorLB implements Comparator<LeaderboardComponent> {
    @Override
    public int compare(LeaderboardComponent a, LeaderboardComponent b) {
        return Integer.compare(Integer.parseInt(b.getWeeklyScore()), Integer.parseInt(a.getWeeklyScore()));
    }
}

class MonthlyScoreComparatorLB implements Comparator<LeaderboardComponent> {
    @Override
    public int compare(LeaderboardComponent a, LeaderboardComponent b) {
        return Integer.compare(Integer.parseInt(b.getMonthlyScore()), Integer.parseInt(a.getMonthlyScore()));
    }
}

class AlltimeScoreComparatorLB implements Comparator<LeaderboardComponent> {
    @Override
    public int compare(LeaderboardComponent a, LeaderboardComponent b) {
        return Integer.compare(Integer.parseInt(b.getAllTimeScore()), Integer.parseInt(a.getAllTimeScore()));
    }
}
