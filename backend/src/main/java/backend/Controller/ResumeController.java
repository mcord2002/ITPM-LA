package backend.Controller;

// Import configuration for upload directory
import backend.Config.UploadConfig;

// Import Resume entity (represents resume data in DB)
import backend.Model.Resume;

// Repository for database operations
import backend.Repository.ResumeRepository;

// Service used to extract text from PDF files
import backend.Service.PdfTextService;

// Library for reading DOCX files
import org.apache.poi.xwpf.usermodel.XWPFDocument;

// Spring annotations and classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Java utility classes
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

// Marks this class as REST controller
@RestController

// Base URL for this controller
@RequestMapping("/api/resumes")

// Allow frontend requests from these origins
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class ResumeController {

    // Inject repository for Resume operations
    @Autowired
    private ResumeRepository resumeRepository;

    // Inject upload configuration (file storage path)
    @Autowired
    private UploadConfig uploadConfig;

    // Inject PDF text extraction service
    @Autowired
    private PdfTextService pdfTextService;

    // GET: Fetch all resumes of a user
    @GetMapping
    public List<Resume> getByUser(@RequestParam Long userId) {
        // Returns resumes ordered by upload time (latest first)
        return resumeRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    // GET: Fetch latest resume of a user
    @GetMapping("/latest")
    public ResponseEntity<Resume> getLatest(@RequestParam Long userId) {
        return resumeRepository.findFirstByUserIdOrderByUploadedAtDesc(userId)
                // Return 200 OK if found
                .map(ResponseEntity::ok)
                // Return 404 if not found
                .orElse(ResponseEntity.notFound().build());
    }

    // GET: Fetch resume by ID
    @GetMapping("/{id}")
    public ResponseEntity<Resume> getById(@PathVariable Long id) {
        return resumeRepository.findById(id)
                // Return 200 OK if found
                .map(ResponseEntity::ok)
                // Return 404 if not found
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: Create resume directly from request body
    @PostMapping
    public Resume create(@RequestBody Resume resume) {
        // Save resume into database
        return resumeRepository.save(resume);
    }

    // POST: Upload resume file
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            // User ID parameter
            @RequestParam Long userId,

            // Uploaded file
            @RequestParam("file") MultipartFile file) throws IOException {

        // Check if file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty.");
        }

        // Get original file name or default name
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume";

        // Convert filename to lowercase for extension checking
        String lowerName = originalName.toLowerCase();

        // Read file content as bytes
        byte[] bytes = file.getBytes();

        String extractedText;

        // If file is PDF, extract text using PDF service
        if (lowerName.endsWith(".pdf")) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
                extractedText = pdfTextService.extractText(inputStream);
            }

            // If file is DOCX, extract text using Apache POI
        } else if (lowerName.endsWith(".docx")) {
            extractedText = extractDocxText(new ByteArrayInputStream(bytes));

            // If file is TXT, convert bytes to string
        } else if (lowerName.endsWith(".txt")) {
            extractedText = new String(bytes, StandardCharsets.UTF_8);

            // Unsupported file type
        } else {
            return ResponseEntity.badRequest().body("Unsupported file type. Please upload PDF, DOCX, or TXT.");
        }

        // Validate extracted text length
        if (extractedText == null || extractedText.trim().length() < 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Could not extract enough text from the uploaded CV.");
        }

        // Default file extension
        String ext = ".txt";

        // Extract extension from original filename
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0 && dot < originalName.length() - 1) {
            ext = originalName.substring(dot);
        }

        // Generate unique file path
        Path dest = uploadConfig.getUploadPath().resolve(UUID.randomUUID() + ext);

        // Save file to disk
        Files.write(dest, bytes);

        // Create Resume object
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setContent(extractedText.trim());
        resume.setFilePath(dest.toAbsolutePath().toString());

        // Save to database and return response
        return ResponseEntity.ok(resumeRepository.save(resume));
    }

    // Helper method to extract text from DOCX files
    private String extractDocxText(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Open DOCX document
        try (XWPFDocument document = new XWPFDocument(inputStream)) {

            // Extract text from paragraphs
            document.getParagraphs().forEach(p -> {
                if (p != null && p.getText() != null) {
                    sb.append(p.getText()).append("\n");
                }
            });

            // Extract text from tables
            document.getTables().forEach(table -> table.getRows().forEach(row -> row.getTableCells().forEach(cell -> {
                if (cell != null && cell.getText() != null) {
                    sb.append(cell.getText()).append(" ");
                }
            })));
        }

        // Return extracted text
        return sb.toString();
    }

    // DELETE: Remove resume by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        // Check if resume exists
        if (!resumeRepository.existsById(id))
            return ResponseEntity.notFound().build();

        // Delete resume
        resumeRepository.deleteById(id);

        // Return 204 No Content
        return ResponseEntity.noContent().build();
    }
}