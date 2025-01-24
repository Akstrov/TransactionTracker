package ma.ismagi.cp2.transactiontracker.model;
import java.io.Serializable;
import java.util.Date;

public class Goal implements Serializable {

    private String id;
    private String goalType; // "savings" or "spending"
    private double targetAmount;
    private double currentProgress;
    private String description;
    private String deadline; // optional, store as a String (e.g., "YYYY-MM-DD")
    private String createdBy; // reference to user who created the goal
    private boolean isCompleted; // "true" or "false"
    private String createdAt;

    // Constructor
    public Goal() {
    }
    public Goal(String goalType, double targetAmount, double currentProgress, String description, String deadline, String createdBy) {
        this.goalType = goalType;
        this.targetAmount = targetAmount;
        this.currentProgress = currentProgress;
        this.description = description;
        this.deadline = deadline;
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(double currentProgress) {
        this.currentProgress = currentProgress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // Method to calculate progress percentage
    public double getProgressPercentage() {
        return (currentProgress / targetAmount) * 100;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", goalType='" + goalType + '\'' +
                ", targetAmount=" + targetAmount +
                ", currentProgress=" + currentProgress +
                ", description='" + description + '\'' +
                ", deadline='" + deadline + '\'' +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}
