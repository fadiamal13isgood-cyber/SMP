package com.deceptionsmp.missions.model;

public class Mission {

    private final String id;
    private final MissionDifficulty difficulty;
    private final String type;
    private final String targetMaterial;
    private final int requiredAmount;
    private final long deadlineTimestamp;
    private int progress;

    public Mission(String id, MissionDifficulty difficulty, String type, String targetMaterial, int requiredAmount, long deadlineTimestamp) {
        this.id = id;
        this.difficulty = difficulty;
        this.type = type;
        this.targetMaterial = targetMaterial;
        this.requiredAmount = requiredAmount;
        this.deadlineTimestamp = deadlineTimestamp;
        this.progress = 0;
    }

    public String getId() {
        return id;
    }

    public MissionDifficulty getDifficulty() {
        return difficulty;
    }

    public String getType() {
        return type;
    }

    public String getTargetMaterial() {
        return targetMaterial;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public long getDeadlineTimestamp() {
        return deadlineTimestamp;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getDescription() {
        String targetName = targetMaterial != null ? targetMaterial.replace("_", " ") : "Items";
        return "Gather " + requiredAmount + "x " + targetName;
    }
}
