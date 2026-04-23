package backend.Repository;

// Import CoverLetter entity
import backend.Model.CoverLetter;

// Import JpaRepository for CRUD operations
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository interface for CoverLetter entity
// Extends JpaRepository to provide built-in database operations
public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {

    // Custom query method:
    // Fetch all cover letters for a specific user
    // Ordered by createdAt in descending order (latest first)
    List<CoverLetter> findByUserIdOrderByCreatedAtDesc(Long userId);
}