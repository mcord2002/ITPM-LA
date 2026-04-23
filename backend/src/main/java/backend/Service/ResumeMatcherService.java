package backend.Service;

// Import model classes
import backend.Model.JobPost;
import backend.Model.Resume;
import backend.Model.ResumeMatch;

// Import repositories for database operations
import backend.Repository.JobPostRepository;
import backend.Repository.ResumeMatchRepository;
import backend.Repository.ResumeRepository;

// Spring annotations
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Java utility classes
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock AI: computes match score from keyword overlap. Can be replaced with real
 * AI.
 */

// Marks this class as a service (business logic layer)
@Service
public class ResumeMatcherService {

    // Predefined list of core skills used for matching
    private static final List<String> CORE_SKILLS = List.of(
            "python", "java", "sql", "machine learning", "ml", "data analysis", "statistics",
            "docker", "kubernetes", "aws", "azure", "gcp", "linux", "ci/cd",
            "react", "node", "javascript", "spring", "git", "rest", "api",
            "excel", "power bi", "tableau");

    // Inject Resume repository
    @Autowired
    private ResumeRepository resumeRepository;

    // Inject JobPost repository
    @Autowired
    private JobPostRepository jobPostRepository;

    // Inject ResumeMatch repository
    @Autowired
    private ResumeMatchRepository matchRepository;

    // Tokenize text into cleaned set of words
    private static Set<String> tokenize(String text) {

        // Return empty set if text is null or blank
        if (text == null || text.isBlank())
            return Set.of();

        return Arrays.stream(text.toLowerCase().split("\\s+"))
                // Remove non-alphanumeric characters
                .map(s -> s.replaceAll("[^a-z0-9]", ""))
                // Keep words longer than 1 character
                .filter(s -> s.length() > 1)
                // Collect into a set
                .collect(Collectors.toSet());
    }

    // Main method: compute match score and save result
    public ResumeMatch computeAndSave(Long userId, Long resumeId, Long jobId) {

        // Fetch resume and job from database
        Resume resume = resumeRepository.findById(resumeId).orElse(null);
        JobPost job = jobPostRepository.findById(jobId).orElse(null);

        // Return null if either is missing
        if (resume == null || job == null)
            return null;

        // Get resume text content
        String resumeText = resume.getContent() != null ? resume.getContent() : "";

        // Combine job title and description
        String jobText = (job.getTitle() != null ? job.getTitle() + " " : "") +
                (job.getDescription() != null ? job.getDescription() : "");

        // Convert to lowercase for comparison
        String resumeLower = resumeText.toLowerCase();
        String jobLower = jobText.toLowerCase();

        // Extract skill sets from resume and job
        Set<String> resumeSkillSet = extractMentionedSkills(resumeLower);
        Set<String> jobSkillSet = extractMentionedSkills(jobLower);

        // Tokenize both texts into word sets
        Set<String> resumeWords = tokenize(resumeText);
        Set<String> jobWords = tokenize(jobText);

        // Default fallback if jobWords is empty
        if (jobWords.isEmpty())
            jobWords = Set.of("skill", "experience");

        // Count overlapping words
        long overlap = resumeWords.stream().filter(jobWords::contains).count();

        // Calculate keyword score
        double keywordScore = jobWords.isEmpty() ? 0.0 : (double) overlap / Math.max(1, jobWords.size());

        double skillScore;

        // If no job skills, fallback to keyword score
        if (jobSkillSet.isEmpty()) {
            skillScore = keywordScore;
        } else {
            // Calculate matched skills ratio
            long matchedSkills = jobSkillSet.stream()
                    .filter(resumeSkillSet::contains)
                    .count();

            skillScore = (double) matchedSkills / jobSkillSet.size();
        }

        // Check if resume contains education keywords
        boolean hasEducation = containsAny(resumeLower,
                List.of("bsc", "bachelor", "degree", "university", "diploma", "education"));

        // Check if resume contains experience keywords
        boolean hasExperience = containsAny(resumeLower,
                List.of("experience", "intern", "project", "worked", "employment"));

        // Final score calculation (weighted)
        int score = (int) Math.round(
                Math.min(1.0, skillScore) * 70
                        + Math.min(1.0, keywordScore) * 20
                        + (hasEducation ? 5 : 0)
                        + (hasExperience ? 5 : 0));

        // Ensure score is between 0 and 100
        score = Math.max(0, Math.min(100, score));

        // Identify missing skills or keywords
        List<String> missing = (jobSkillSet.isEmpty() ? jobWords : jobSkillSet).stream()
                .filter(w -> !resumeLower.contains(w.toLowerCase()))
                .limit(8)
                .collect(Collectors.toList());

        String pointsToImprove;

        // If no missing items, give positive feedback
        if (missing.isEmpty()) {
            pointsToImprove = "Great alignment. Consider quantifying achievements and adding measurable project impact for an even stronger profile.";
        } else {
            // Suggest top missing skills
            String top = missing.stream().limit(3).collect(Collectors.joining(", "));
            pointsToImprove = "Add " + top + " experience for better match.";
        }

        // Create ResumeMatch object
        ResumeMatch match = new ResumeMatch();
        match.setUserId(userId);
        match.setResumeId(resumeId);
        match.setJobId(jobId);
        match.setScorePercent(score);
        match.setPointsToImprove(pointsToImprove);

        // Save and return result
        return matchRepository.save(match);
    }

    // Extract mentioned skills from text using predefined skill list
    private Set<String> extractMentionedSkills(String text) {
        return CORE_SKILLS.stream()
                // Check if skill exists in text
                .filter(text::contains)
                // Collect into ordered set
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Check if text contains any of the given terms
    private boolean containsAny(String text, List<String> terms) {
        return terms.stream().anyMatch(text::contains);
    }
}