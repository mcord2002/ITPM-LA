package backend.Controller;

// Import model (entity class representing blog post table)
import backend.Model.BlogPost;

// Import repository (used for database operations)
import backend.Repository.BlogPostRepository;

// Spring annotations and classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Marks this class as a REST controller (handles HTTP requests)
@RestController

// Base URL for all endpoints in this controller
@RequestMapping("/api/blog")

// Allows requests from frontend running on these ports (CORS configuration)
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class BlogPostController {

    // Injects BlogPostRepository to interact with database
    @Autowired
    private BlogPostRepository blogPostRepository;

    // GET: Fetch all blog posts ordered by latest created date
    @GetMapping
    public List<BlogPost> list() {
        return blogPostRepository.findAllByOrderByCreatedAtDesc();
    }

    // GET: Fetch blog posts by a specific author using query parameter
    // Example: /api/blog/my?authorId=1
    @GetMapping("/my")
    public List<BlogPost> myPosts(@RequestParam Long authorId) {
        return blogPostRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    // GET: Fetch a single blog post by ID
    @GetMapping("/{id}")
    public ResponseEntity<BlogPost> getById(@PathVariable Long id) {
        return blogPostRepository.findById(id)
                // If found, return 200 OK with data
                .map(ResponseEntity::ok)
                // If not found, return 404 Not Found
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: Create a new blog post
    @PostMapping
    public BlogPost create(@RequestBody BlogPost post) {
        // Saves the incoming blog post object to database
        return blogPostRepository.save(post);
    }

    // PUT: Update an existing blog post by ID
    @PutMapping("/{id}")
    public ResponseEntity<BlogPost> update(@PathVariable Long id, @RequestBody BlogPost body) {
        return blogPostRepository.findById(id)
                .map(b -> {
                    // Update title only if provided (not null)
                    //Null check during UPDATE
                    if (body.getTitle() != null)
                        b.setTitle(body.getTitle());

                    // Update content only if provided (not null)
                    if (body.getContent() != null)
                        b.setContent(body.getContent());

                    // Save updated entity and return 200 OK
                    return ResponseEntity.ok(blogPostRepository.save(b));
                })
                // If blog post not found, return 404 Not Found  Resource existence check (DELETE)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: Remove a blog post by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        // Check if the blog post exists
        if (!blogPostRepository.existsById(id))
            // Return 404 if not found
            return ResponseEntity.notFound().build();

        // Delete the blog post
        blogPostRepository.deleteById(id);

        // Return 204 No Content after successful deletion
        return ResponseEntity.noContent().build();
    }
}