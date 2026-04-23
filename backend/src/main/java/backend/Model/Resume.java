package backend.Model;

// Import JPA annotations for ORM mapping
import jakarta.persistence.*;

// Import date-time class for timestamp
import java.time.LocalDateTime;

// Marks this class as a JPA entity (maps to a database table)
@Entity

// Specifies the table name in the database
@Table(name = "resumes")
public class Resume {

    // Primary key of the table
    @Id

    // Auto-generate ID using database identity strategy
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Column storing user ID (cannot be null)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Extracted or pasted text content */
    // Stores resume content (large text allowed)
    @Column(columnDefinition = "TEXT")
    private String content;

    // Stores file path of uploaded resume (if file is saved)
    @Column(name = "file_path")
    private String filePath;

    // Timestamp when resume was uploaded (default = current time)
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // Default constructor (required by JPA)
    public Resume() {
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

    // Getter for resume content
    public String getContent() {
        return content;
    }

    // Setter for resume content
    public void setContent(String content) {
        this.content = content;
    }

    // Getter for file path
    public String getFilePath() {
        return filePath;
    }

    // Setter for file path
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Getter for upload timestamp
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    // Setter for upload timestamp
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}