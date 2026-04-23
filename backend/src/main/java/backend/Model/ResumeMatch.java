package backend.Model;

// Import JPA annotations for ORM mapping
import jakarta.persistence.*;

// Import date-time class for timestamp
import java.time.LocalDateTime;

// Marks this class as a JPA entity (maps to a database table)
@Entity

// Specifies the table name in the database
@Table(name = "resume_matches")
public class ResumeMatch {

    // Primary key of the table
    @Id

    // Auto-generate ID using database identity strategy
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Column storing user ID (cannot be null)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Column storing resume ID (optional)
    @Column(name = "resume_id")
    private Long resumeId;

    // Column storing job ID (cannot be null)
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    // Column storing match score in percentage
    @Column(name = "score_percent")
    private Integer scorePercent;

    // Column storing improvement suggestions (TEXT for large content)
    @Column(name = "points_to_improve", columnDefinition = "TEXT")
    private String pointsToImprove;

    // Column storing creation timestamp (default = current time)
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Default constructor (required by JPA)
    public ResumeMatch() {
    }

    // Getter for ID
    public Long getId() {
        return id;
    }

    // Setter for ID
    public void setId(Long id) {
        this.id = id;
    }

    // Getter for user ID
    public Long getUserId() {
        return userId;
    }

    // Setter for user ID
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Getter for resume ID
    public Long getResumeId() {
        return resumeId;
    }

    // Setter for resume ID
    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    // Getter for job ID
    public Long getJobId() {
        return jobId;
    }

    // Setter for job ID
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    // Getter for score percentage
    public Integer getScorePercent() {
        return scorePercent;
    }

    // Setter for score percentage
    public void setScorePercent(Integer scorePercent) {
        this.scorePercent = scorePercent;
    }

    // Getter for improvement points
    public String getPointsToImprove() {
        return pointsToImprove;
    }

    // Setter for improvement points
    public void setPointsToImprove(String pointsToImprove) {
        this.pointsToImprove = pointsToImprove;
    }

    // Getter for creation timestamp
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setter for creation timestamp
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}