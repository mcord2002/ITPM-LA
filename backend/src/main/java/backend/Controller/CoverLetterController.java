package backend.Controller;

// Import model (entity representing cover letter data)
import backend.Model.CoverLetter;

// Import repository (used for direct database operations)
import backend.Repository.CoverLetterRepository;

// Import service (contains business logic for generating cover letters)
import backend.Service.CoverLetterService;

// Spring annotations and classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Marks this class as a REST controller (handles HTTP requests)
@RestController

// Base URL for all endpoints in this controller
@RequestMapping("/api/cover-letter")

// Allows requests from frontend running on these ports (CORS configuration)
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class CoverLetterController {

    // Injects CoverLetterService (used for business logic like generating cover
    // letters)
    @Autowired
    private CoverLetterService coverLetterService;

    // Injects CoverLetterRepository (used for database queries)
    @Autowired
    private CoverLetterRepository coverLetterRepository;

    // POST: Generate a new cover letter based on request data
    @PostMapping("/generate")
    public CoverLetter generate(@RequestBody GenerateRequest req) {

        // Calls service method to generate and save cover letter
        return coverLetterService.generateAndSave(
                req.userId, // user ID
                req.jobId, // job ID
                req.jobDescription // job description text
        );
    }

    // GET: Retrieve cover letter history for a specific user
    // Example: /api/cover-letter/history?userId=1
    @GetMapping("/history")
    public List<CoverLetter> history(@RequestParam Long userId) {

        // Fetches all cover letters for the user ordered by latest created date
        return coverLetterRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // GET: Retrieve a single cover letter by ID
    @GetMapping("/{id}")
    public ResponseEntity<CoverLetter> getById(@PathVariable Long id) {

        return coverLetterRepository.findById(id)
                // If found, return 200 OK with data
                .map(ResponseEntity::ok)
                // If not found, return 404 Not Found
                .orElse(ResponseEntity.notFound().build());
    }

    // Inner static class used to receive request body for generating cover letter
    public static class GenerateRequest {

        // ID of the user requesting the cover letter
        public Long userId;

        // ID of the job the cover letter is for
        public Long jobId;

        // Job description text used to generate the cover letter
        public String jobDescription;
    }
}