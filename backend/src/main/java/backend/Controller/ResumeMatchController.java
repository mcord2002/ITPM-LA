package backend.Controller;

// Import model representing resume match result
import backend.Model.ResumeMatch;

// Import repository for database operations
import backend.Repository.ResumeMatchRepository;

// Import service for matching logic
import backend.Service.ResumeMatcherService;

// Spring annotations and classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Marks this class as a REST controller
@RestController

// Base URL for all endpoints in this controller
@RequestMapping("/api/match")

// Allow frontend requests from these origins (CORS configuration)
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class ResumeMatchController {

    // Inject ResumeMatcherService (handles business logic for matching resumes)
    @Autowired
    private ResumeMatcherService matcherService;

    // Inject ResumeMatchRepository (handles DB operations)
    @Autowired
    private ResumeMatchRepository matchRepository;

    // POST: Compute resume-job match score
    @PostMapping
    public ResponseEntity<ResumeMatch> computeMatch(@RequestBody MatchRequest req) {

        // Call service to compute match and save result
        ResumeMatch m = matcherService.computeAndSave(
                req.userId, // user ID
                req.resumeId, // resume ID
                req.jobId // job ID
        );

        // If result is not null, return 200 OK with data
        // Otherwise return 400 Bad Request
        return m != null ? ResponseEntity.ok(m) : ResponseEntity.badRequest().build();
    }

    // GET: Fetch match history for a specific user
    // Example: /api/match/history?userId=1
    @GetMapping("/history")
    public List<ResumeMatch> history(@RequestParam Long userId) {

        // Retrieve matches ordered by latest created date
        return matchRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Inner static class used to receive request body for matching
    public static class MatchRequest {

        // ID of the user requesting the match
        public Long userId;

        // ID of the resume used for matching
        public Long resumeId;

        // ID of the job to match against
        public Long jobId;
    }
}