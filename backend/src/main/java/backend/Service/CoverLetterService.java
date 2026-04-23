package backend.Service;

// Import model classes
import backend.Model.CoverLetter;
import backend.Model.JobPost;

// Import repositories for database access
import backend.Repository.CoverLetterRepository;
import backend.Repository.JobPostRepository;

// Spring annotations
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Java utility classes
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI-powered cover letter generator: extracts job details, identifies key
 * skills,
 * and generates personalized, professional cover letters.
 */

// Marks this class as a service (business logic layer)
@Service
public class CoverLetterService {

    // Inject CoverLetterRepository for saving generated cover letters
    @Autowired
    private CoverLetterRepository coverLetterRepository;

    // Inject JobPostRepository to fetch job details
    @Autowired
    private JobPostRepository jobPostRepository;

    // Main method: generates and saves a cover letter
    public CoverLetter generateAndSave(Long userId, Long jobId, String jobDescriptionRaw) {

        // Use provided description or empty string if null
        String jobDesc = jobDescriptionRaw != null ? jobDescriptionRaw : "";

        // Initialize job title and company
        String jobTitle = "";
        String company = "";

        // If jobId is provided, fetch job details from database
        if (jobId != null) {
            var opt = jobPostRepository.findById(jobId);

            if (opt.isPresent()) {
                var j = opt.get();

                // Extract title and company safely
                jobTitle = j.getTitle() != null ? j.getTitle() : "";
                company = j.getCompany() != null ? j.getCompany() : "";

                // Combine title and description
                jobDesc = (jobTitle + "\n" + (j.getDescription() != null ? j.getDescription() : "")).trim();
            }
        }

        // If job title is still empty, extract from description
        if (jobTitle.isBlank()) {
            jobTitle = extractJobTitle(jobDesc);
        }

        // Extract key skills from job description
        List<String> keySkills = extractKeySkills(jobDesc);

        // Extract responsibilities from job description
        List<String> responsibilities = extractResponsibilities(jobDesc);

        // Detect job seniority level
        String seniority = detectSeniority(jobDesc);

        // Generate final personalized cover letter
        String generated = generatePersonalizedCoverLetter(
                jobTitle, company, keySkills, responsibilities, seniority);

        // Create CoverLetter object
        CoverLetter cl = new CoverLetter();
        cl.setUserId(userId);
        cl.setJobId(jobId);
        cl.setJobDescription(jobDesc);
        cl.setGeneratedText(generated);

        // Save to database and return
        return coverLetterRepository.save(cl);
    }

    // Extract job title from description text
    private String extractJobTitle(String jobDesc) {

        // Return default if empty
        if (jobDesc == null || jobDesc.isBlank()) {
            return "this position";
        }

        // Split description into lines
        String[] lines = jobDesc.split("\\n");

        // Find a valid title line
        for (String line : lines) {
            String clean = line.trim();

            // Basic filtering rules for title
            if (clean.length() > 5 && clean.length() < 100 &&
                    !clean.contains("@") && !clean.startsWith("•")) {
                return clean;
            }
        }

        // Default fallback
        return "this position";
    }

    // Extract common technical skills from job description
    private List<String> extractKeySkills(String jobDesc) {

        List<String> skills = new ArrayList<>();

        // Return empty list if description is null
        if (jobDesc == null)
            return skills;

        // Convert to lowercase for matching
        String lower = jobDesc.toLowerCase();

        // Predefined list of common skills
        String[] commonSkills = {
                "python", "java", "javascript", "sql", "react", "node",
                "docker", "kubernetes", "aws", "azure", "machine learning",
                "data analysis", "spring", "rest api", "microservices",
                "agile", "scrum", "git", "linux", "ci/cd"
        };

        // Check if skills exist in description
        for (String skill : commonSkills) {
            if (lower.contains(skill.toLowerCase()) && skills.size() < 6) {
                skills.add(capitalizeWords(skill));
            }
        }

        return skills;
    }

    // Extract responsibilities (bullet points) from job description
    private List<String> extractResponsibilities(String jobDesc) {

        List<String> items = new ArrayList<>();

        // Return empty list if null
        if (jobDesc == null)
            return items;

        // Split description into lines
        String[] lines = jobDesc.split("\\n");

        for (String line : lines) {
            String clean = line.trim();

            // Check for bullet point formats
            if ((clean.startsWith("•") || clean.startsWith("-") || clean.startsWith("*"))
                    && items.size() < 3) {

                // Remove bullet symbols
                String resp = clean.replaceAll("^[•\\-*\\s]+", "").trim();

                // Add valid responsibility
                if (!resp.isBlank() && resp.length() < 120) {
                    items.add(resp);
                }
            }
        }

        return items;
    }

    // Detect job seniority level from description
    private String detectSeniority(String jobDesc) {

        if (jobDesc == null)
            return "entry";

        String lower = jobDesc.toLowerCase();

        if (lower.contains("senior") || lower.contains("lead") || lower.contains("architect")) {
            return "senior";
        } else if (lower.contains("mid") || lower.contains("experienced")) {
            return "mid";
        } else if (lower.contains("junior") || lower.contains("intern") || lower.contains("entry")) {
            return "entry";
        }

        return "entry";
    }

    // Generate the final cover letter text
    private String generatePersonalizedCoverLetter(
            String jobTitle,
            String company,
            List<String> skills,
            List<String> responsibilities,
            String seniority) {

        StringBuilder letter = new StringBuilder();

        // Greeting
        letter.append("Dear Hiring Manager,\n\n");

        // Opening paragraph
        if (!company.isBlank()) {
            letter.append("I am writing to express my strong interest in the ")
                    .append(jobTitle)
                    .append(" position at ")
                    .append(company)
                    .append(". ");
        } else {
            letter.append("I am writing to express my interest in the ")
                    .append(jobTitle)
                    .append(" role. ");
        }

        letter.append(
                "I am confident that my background, technical expertise, and passion for technology make me an excellent fit for your team.\n\n");

        // Skills section
        if (!skills.isEmpty()) {
            letter.append("With hands-on experience in ")
                    .append(String.join(", ", skills.subList(0, Math.min(3, skills.size()))));

            if (skills.size() > 3) {
                letter.append(", and more");
            }

            letter.append(", I am well-positioned to contribute immediately to your team. ");
        }

        // Responsibilities section
        if (!responsibilities.isEmpty()) {
            letter.append("I am particularly excited about the opportunity to ")
                    .append(responsibilities.get(0).toLowerCase())
                    .append(", as this aligns perfectly with my professional goals. ");
        }

        letter.append(
                "I am eager to bring my problem-solving abilities, attention to detail, and collaborative mindset to your organization.\n\n");

        // Closing paragraph
        letter.append(
                "I would welcome the opportunity to discuss how my background and skills can contribute to your team's success. ");
        letter.append(
                "Thank you for considering my application. I look forward to the possibility of speaking with you soon.\n\n");

        // Signature
        letter.append("Sincerely,\n").append("[Your Name]");

        return letter.toString();
    }

    // Utility method: capitalize each word
    private String capitalizeWords(String input) {

        if (input == null || input.isBlank())
            return input;

        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (sb.length() > 0)
                sb.append(" ");

            sb.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1));
        }

        return sb.toString();
    }
}