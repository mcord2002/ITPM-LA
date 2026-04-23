package backend.Repository;

// Import BlogPost entity
import backend.Model.BlogPost;

// Import JpaRepository for CRUD operations
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository interface for BlogPost entity
// Extends JpaRepository to provide built-in CRUD methods
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    // Custom query method:
    // Fetch all blog posts ordered by createdAt in descending order (latest first)
    List<BlogPost> findAllByOrderByCreatedAtDesc();

    // Custom query method:
    // Fetch blog posts by authorId ordered by createdAt in descending order
    List<BlogPost> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}