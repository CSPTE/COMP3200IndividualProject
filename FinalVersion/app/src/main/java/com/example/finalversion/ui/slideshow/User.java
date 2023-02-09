package com.example.finalversion.ui.slideshow;

import java.util.Comparator;

public class User implements Comparable<User> {
    private String username;
    private int weeklyScore;
    private int monthlyScore;
    private int alltimeScore;

    public User(String username, Long weeklyScore, Long monthlyScore, Long alltimeScore) {
        this.username = username;
        this.weeklyScore = Math.toIntExact(weeklyScore);
        this.monthlyScore = Math.toIntExact(monthlyScore);
        this.alltimeScore = Math.toIntExact(alltimeScore);
    }

    public String getUsername() {
        return username;
    }

    public int getWeeklyScore() {
        return weeklyScore;
    }

    public int getMonthlyScore() {
        return monthlyScore;
    }

    public int getAlltimeScore() {
        return alltimeScore;
    }

    @Override
    public int compareTo(User other) {
        return Integer.compare(alltimeScore, other.alltimeScore);
    }
}

class WeeklyScoreComparator implements Comparator<User> {
    @Override
    public int compare(User a, User b) {
        return Integer.compare(b.getWeeklyScore(), a.getWeeklyScore());
    }
}

class MonthlyScoreComparator implements Comparator<User> {
    @Override
    public int compare(User a, User b) {
        return Integer.compare(b.getMonthlyScore(), a.getMonthlyScore());
    }
}

class AlltimeScoreComparator implements Comparator<User> {
    @Override
    public int compare(User a, User b) {
        return Integer.compare(b.getAlltimeScore(), a.getAlltimeScore());
    }
}
