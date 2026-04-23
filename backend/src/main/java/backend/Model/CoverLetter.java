package backend.Model;

// Import JPA annotations for ORM mapping
import jakarta.persistence.*;

// Import date-time class for timestamp
import java.time.LocalDateTime;

// Marks this class as a JPA entity (mapped to database table)
@Entity

// Specifies the table name in the database
@Table(name = "cover_letters")
public class CoverLetter {

    // Primary key of the table
    @Id

    // Auto-generates ID using database identity strategy
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Column storing user ID (cannot be null)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Column storing job ID (optional)
    @Column(name = "job_id")
    private Long jobId;

    // Column storing job description (TEXT type for large content)
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    // Column storing generated cover letter text (TEXT type)
    @Column(name = "generated_text", columnDefinition = "TEXT")
    private String generatedText;

    // Column storing creation timestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Default constructor (required by JPA)
    public CoverLetter() {
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

    // Getter for job ID
    public Long getJobId() {
        return jobId;
    }

    // Setter for job ID
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    // Getter for job description
    public String getJobDescription() {
        return jobDescription;
    }

    // Setter for job description
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    // Getter for generated cover letter text
    public String getGeneratedText() {
        return generatedText;
    }

    // Setter for generated cover letter text
    public void setGeneratedText(String generatedText) {
        this.generatedText = generatedText;
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