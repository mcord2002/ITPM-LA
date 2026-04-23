package backend.Repository;

// Import JobPost entity
import backend.Model.JobPost;

// Import JpaRepository for CRUD operations
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository interface for JobPost entity
// Extends JpaRepository to provide built-in database operations
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    // Custom query method:
    // Fetch all job posts ordered by createdAt in descending order (latest first)
    List<JobPost> findAllByOrderByCreatedAtDesc();

    // Custom query method:
    // Fetch job posts created by a specific user (postedBy)
    // Ordered by createdAt in descending order
    List<JobPost> findByPostedByOrderByCreatedAtDesc(Long postedBy);

    // Custom query method:
    // Fetch job posts by type (e.g., JOB or INTERNSHIP)
    // Ordered by createdAt in descending order
    List<JobPost> findByTypeOrderByCreatedAtDesc(String type);
}