package backend.Repository;

// Import ResumeMatch entity
import backend.Model.ResumeMatch;

// Import JpaRepository for CRUD operations
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository interface for ResumeMatch entity
// Extends JpaRepository to provide built-in database operations
public interface ResumeMatchRepository extends JpaRepository<ResumeMatch, Long> {

    // Custom query method:
    // Fetch all resume matches for a specific user
    // Ordered by createdAt in descending order (latest first)
    List<ResumeMatch> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Custom query method:
    // Fetch resume matches for a specific user and a specific job
    // Useful for checking previous match results between a user and a job
    List<ResumeMatch> findByUserIdAndJobId(Long userId, Long jobId);
}