package com.deceptionsmp.missions.model;

public enum MissionDifficulty {
    EASY(12, 10),
    MEDIUM(24, 25),
    HARD(48, 50),
    CLASSIFIED(72, 100);

    private final int timeLimitHours;
    private final int xpReward;

    MissionDifficulty(int timeLimitHours, int xpReward) {
        this.timeLimitHours = timeLimitHours;
        this.xpReward = xpReward;
    }

    public int getTimeLimitHours() { return timeLimitHours; }
    public int getXpReward() { return xpReward; }
}
