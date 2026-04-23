package backend.Controller;

// Import model classes (entities)
import backend.Model.JobApplication;
import backend.Model.JobPost;
import backend.Model.UserModel;

// Import repositories for database operations
import backend.Repository.JobApplicationRepository;
import backend.Repository.JobPostRepository;
import backend.Repository.UserRepository;

// Spring annotations and classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Java utility classes
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

// Marks this class as a REST controller
@RestController

// Base URL for all endpoints
@RequestMapping("/api/jobs")

// Allow frontend requests from these ports (CORS)
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class JobPostController {

    // Inject JobPost repository (for job-related DB operations)
    @Autowired
    private JobPostRepository jobPostRepository;

    // Inject JobApplication repository (for applications)
    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    // Inject User repository (for user-related data)
    @Autowired
    private UserRepository userRepository;

    /**
     * List jobs with visibility rule + optional filters.
     * Visibility: only 3rd/4th year students, ALUMNI, ADMIN.
     */
    @GetMapping
    public List<JobPost> list(
            // Optional user ID for filtering based on role
            @RequestParam(required = false) Long userId,

            // Optional filters
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String deadlineBefore) {
        // Fetch user if userId provided
        UserModel user = userId == null ? null : userRepository.findById(userId).orElse(null);

        // If user is not allowed to see jobs, return empty list User visibility
        // validation (list API)
        if (user == null || !canSeeJobs(user)) {
            return Collections.emptyList();
        }

        // Parse deadline filter
        LocalDate before = parseDate(deadlineBefore);

        // Normalize filter values Category / filter normalization (input validation)
        String categoryNorm = normalizeCategory(category);
        String fieldNorm = normalize(field);
        String skillsNorm = normalize(skills);

        // Fetch jobs and apply filters
        return jobPostRepository.findAllByOrderByCreatedAtDesc().stream()
                // Filter by category
                .filter(j -> categoryNorm == null || categoryNorm.equals(normalizeCategory(j.getCategory())))

                // Filter by field
                .filter(j -> fieldNorm.isBlank() || normalize(j.getField()).contains(fieldNorm))

                // Filter by required skills
                .filter(j -> skillsNorm.isBlank() || containsSkills(j.getSkillsRequired(), skillsNorm))

                // Filter by deadline
                .filter(j -> before == null || (j.getDeadline() != null && !j.getDeadline().isAfter(before)))

                // Check eligibility based on user
                .filter(j -> isEligibleForUser(j, user))

                // Collect results
                .collect(Collectors.toList());
    }

    // GET jobs posted by a specific user
    @GetMapping("/my")
    public List<JobPost> myPosts(@RequestParam Long postedBy) {
        return jobPostRepository.findByPostedByOrderByCreatedAtDesc(postedBy);
    }

    // GET list of job IDs that the user has applied to
    @GetMapping("/my-applications")
    public List<Long> myApplications(@RequestParam Long userId) {
        return jobApplicationRepository.findByApplicantIdOrderByCreatedAtDesc(userId)
                .stream()
                // Extract job IDs
                .map(JobApplication::getJobId)
                // Remove duplicates
                .distinct()
                // Collect to list
                .collect(Collectors.toList());
    }

    // GET job by ID
    @GetMapping("/{id}")
    public ResponseEntity<JobPost> getById(@PathVariable Long id) {
        return jobPostRepository.findById(id)
                // Return 200 OK if found
                .map(ResponseEntity::ok)
                // Return 404 if not found
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create a new job post
    @PostMapping
    public ResponseEntity<?> create(@RequestBody JobPost post) {

        // Fetch poster user
        UserModel poster = post.getPostedBy() == null ? null : userRepository.findById(post.getPostedBy()).orElse(null);

        // Validate poster existence
        if (poster == null) {
            return ResponseEntity.badRequest().body("Invalid postedBy user");
        }

        // Get role safely
        String role = safeRole(poster);

        // Only ALUMNI or ADMIN can create job posts
        if (!"ALUMNI".equals(role) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only alumni/admin can post jobs");
        }

        // Normalize post data before saving
        normalizePost(post);

        // Save job post
        return ResponseEntity.ok(jobPostRepository.save(post));
    }

    // POST apply for a job
    @PostMapping("/{id}/apply")
    public ResponseEntity<?> apply(@PathVariable Long id, @RequestBody ApplyRequest request) {

        // Find job
        JobPost job = jobPostRepository.findById(id).orElse(null);

        // Return 404 if job not found
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        // Validate request and userId
        if (request == null || request.userId == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }

        // Fetch applicant user
        UserModel applicant = userRepository.findById(request.userId).orElse(null);

        // Validate applicant
        if (applicant == null) {
            return ResponseEntity.badRequest().body("Invalid applicant user");
        }

        // Check visibility rules
        if (!canSeeJobs(applicant)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only eligible 3rd/4th year students can apply");
        }

        // Only students can apply
        if (!"STUDENT".equals(safeRole(applicant))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only students can apply");
        }

        // Check job eligibility
        if (!isEligibleForUser(job, applicant)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("This posting is not eligible for your academic year");
        }

        // Prevent duplicate applications
        if (jobApplicationRepository.existsByJobIdAndApplicantId(id, request.userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("You already applied for this posting");
        }

        // Create new application
        JobApplication application = new JobApplication();
        application.setJobId(id);
        application.setApplicantId(request.userId);
        application.setStatus("APPLIED");

        // Save application
        jobApplicationRepository.save(application);

        // Return success response
        return ResponseEntity.ok(application);
    }

    // PUT update job post
    @PutMapping("/{id}")
    public ResponseEntity<JobPost> update(@PathVariable Long id, @RequestBody JobPost body) {
        return jobPostRepository.findById(id)
                .map(j -> {
                    // Update fields only if provided
                    if (body.getTitle() != null)
                        j.setTitle(body.getTitle());
                    if (body.getCompany() != null)
                        j.setCompany(body.getCompany());
                    if (body.getDescription() != null)
                        j.setDescription(body.getDescription());
                    if (body.getCategory() != null)
                        j.setCategory(normalizeCategory(body.getCategory()));
                    if (body.getType() != null)
                        j.setType(body.getType().trim().toUpperCase(Locale.ROOT));
                    if (body.getField() != null)
                        j.setField(body.getField().trim());
                    if (body.getSkillsRequired() != null)
                        j.setSkillsRequired(body.getSkillsRequired().trim());
                    if (body.getDeadline() != null)
                        j.setDeadline(body.getDeadline());
                    if (body.getEligibleYears() != null)
                        j.setEligibleYears(body.getEligibleYears().trim());

                    // Save updated job
                    return ResponseEntity.ok(jobPostRepository.save(j));
                })
                // Return 404 if not found
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE job post
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        // Check existence
        if (!jobPostRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Delete job
        jobPostRepository.deleteById(id);

        // Return success response
        return ResponseEntity.noContent().build();
    }

    // Normalize job post data before saving
    private void normalizePost(JobPost post) {

        // Normalize category
        String category = normalizeCategory(post.getCategory());
        post.setCategory(category == null ? "INTERNSHIP" : category);

        // Set default type if missing
        if (post.getType() == null || post.getType().isBlank()) {
            post.setType("INTERNSHIP".equals(post.getCategory()) ? "INTERNSHIP" : "JOB");
        } else {
            post.setType(post.getType().trim().toUpperCase(Locale.ROOT));
        }

        // Set default eligible years
        if (post.getEligibleYears() == null || post.getEligibleYears().isBlank()) {
            post.setEligibleYears("3,4");
        }

        // Trim field
        if (post.getField() != null) {
            post.setField(post.getField().trim());
        }

        // Trim skills
        if (post.getSkillsRequired() != null) {
            post.setSkillsRequired(post.getSkillsRequired().trim());
        }
    }

    // Check if required skills match filter
    private boolean containsSkills(String skillsRequired, String filterSkills) {
        if (filterSkills.isBlank()) {
            return true;
        }

        String skillsLower = normalize(skillsRequired);
        String[] tokens = filterSkills.split(",");

        for (String token : tokens) {
            String t = token.trim();
            if (!t.isBlank() && !skillsLower.contains(t)) {
                return false;
            }
        }
        return true;
    }

    // Check if user can see jobs
    private boolean canSeeJobs(UserModel user) {
        if (user == null) {
            return false;
        }

        String role = safeRole(user);
        Integer year = user.getYear();

        return "ALUMNI".equals(role)
                || "ADMIN".equals(role)
                || ("STUDENT".equals(role) && year != null && Set.of(3, 4).contains(year));
    }

    // Check if user is eligible for a job
    private boolean isEligibleForUser(JobPost job, UserModel user) {
        if (job == null || user == null) {
            return false;
        }

        String role = safeRole(user);

        // Alumni/admin can access all
        if ("ALUMNI".equals(role) || "ADMIN".equals(role)) {
            return true;
        }

        Integer year = user.getYear();
        if (year == null) {
            return false;
        }

        String eligible = job.getEligibleYears();

        // Default eligibility
        if (eligible == null || eligible.isBlank()) {
            return Set.of(3, 4).contains(year);
        }

        // Check if user's year is in eligible list
        return Arrays.stream(eligible.split(","))
                .map(String::trim)
                .anyMatch(y -> y.equals(String.valueOf(year)));
    }

    // Safely get role (default STUDENT)
    private String safeRole(UserModel user) {
        return user.getRole() == null ? "STUDENT" : user.getRole().trim().toUpperCase(Locale.ROOT);
    }

    // Normalize text (trim + lowercase)
    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    // Normalize category to standard values
    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }

        String value = category.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        // Allow only specific categories
        if (Set.of("INTERNSHIP", "PART_TIME", "FULL_TIME").contains(value)) {
            return value;
        }
        return null;
    }

    // Parse date safely
    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // Request class for job application
    public static class ApplyRequest {

        // Applicant user ID
        public Long userId;
    }
}