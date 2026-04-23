package backend.Repository;

// Import Resume entity
import backend.Model.Resume;

// Import JpaRepository for CRUD operations
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repository interface for Resume entity
// Extends JpaRepository to provide built-in database operations
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // Custom query method:
    // Fetch all resumes for a specific user
    // Ordered by uploadedAt in descending order (latest first)
    List<Resume> findByUserIdOrderByUploadedAtDesc(Long userId);

    // Custom query method:
    // Fetch the latest (most recent) resume for a specific user
    // Returns Optional because result may or may not exist
    Optional<Resume> findFirstByUserIdOrderByUploadedAtDesc(Long userId);
}