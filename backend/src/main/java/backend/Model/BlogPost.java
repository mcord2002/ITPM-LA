package backend.Model;

// Import JPA annotations
import jakarta.persistence.*;

// Import date-time class
import java.time.LocalDateTime;

// Marks this class as a JPA entity (maps to a database table)
@Entity

// Specifies the table name in the database
@Table(name = "blog_posts")
public class BlogPost {

    // Primary key of the table
    @Id

    // Auto-generate ID using database identity strategy
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Column for storing author ID (cannot be null)
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    // Column for blog title (cannot be null)
    @Column(nullable = false)
    private String title;

    // Column for blog content (TEXT type, cannot be null)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Column for storing creation date and time
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Default constructor (required by JPA)
    public BlogPost() {
    }

    // Getter for ID
    public Long getId() {
        return id;
    }

    // Setter for ID
    public void setId(Long id) {
        this.id = id;
    }

    // Getter for author ID
    public Long getAuthorId() {
        return authorId;
    }

    // Setter for author ID
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    // Getter for title
    public String getTitle() {
        return title;
    }

    // Setter for title
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter for content
    public String getContent() {
        return content;
    }

    // Setter for content
    public void setContent(String content) {
        this.content = content;
    }

    // Getter for created date
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setter for created date
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}