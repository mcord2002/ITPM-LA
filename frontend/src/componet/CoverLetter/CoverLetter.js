import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import './CoverLetter.css';

const API = 'http://localhost:8080/api';

function CoverLetter() {
  const DESCRIPTION_MIN = 30;
  const DESCRIPTION_MAX = 8000;

  const [searchParams] = useSearchParams();
  const jobIdParam = searchParams.get('jobId');
  const userId = localStorage.getItem('userId'); import React, { useEffect, useState } from 'react';
  import { Link, useSearchParams } from 'react-router-dom';
  import axios from 'axios';
  import './CoverLetter.css';

  // Base API URL
  const API = 'http://localhost:8080/api';

  function CoverLetter() {

    // Validation limits for job description
    const DESCRIPTION_MIN = 30;
    const DESCRIPTION_MAX = 8000;

    // Get query parameters (for pre-selecting job)
    const [searchParams] = useSearchParams();
    const jobIdParam = searchParams.get('jobId');

    // Get logged-in user ID from local storage
    const userId = localStorage.getItem('userId');

    // State variables
    const [jobs, setJobs] = useState([]); // Available jobs
    const [loading, setLoading] = useState(true); // Loading state
    const [selectedJobId, setSelectedJobId] = useState(jobIdParam || ''); // Selected job
    const [jobDescription, setJobDescription] = useState(''); // Manual job description input
    const [generated, setGenerated] = useState(null); // Generated cover letter
    const [generating, setGenerating] = useState(false); // Generating state
    const [errors, setErrors] = useState({ selection: '', jobDescription: '', submit: '' }); // Validation errors

    // -------- VALIDATION FUNCTION --------
    const validate = (values) => {

      // Initialize error object
      const nextErrors = { selection: '', jobDescription: '', submit: '' };

      const hasJobId = Boolean(values.selectedJobId); // Check if job is selected
      const trimmedDescription = values.jobDescription.trim(); // Remove spaces

      // Validation: Either job selection OR description must be provided
      if (!hasJobId && !trimmedDescription) {
        nextErrors.selection = 'Select a job or paste a job description.';
      }

      // Validation: Minimum length check
      if (trimmedDescription && trimmedDescription.length < DESCRIPTION_MIN) {
        nextErrors.jobDescription = `Job description must be at least ${DESCRIPTION_MIN} characters.`;

        // Validation: Maximum length check
      } else if (trimmedDescription.length > DESCRIPTION_MAX) {
        nextErrors.jobDescription = `Job description must be under ${DESCRIPTION_MAX} characters.`;
      }

      return nextErrors;
    };

    // Handle description input change
    const handleDescriptionChange = (value) => {

      // Update state
      setJobDescription(value);

      // Re-validate input
      const nextErrors = validate({ selectedJobId, jobDescription: value });

      // Update error state
      setErrors((prev) => ({
        ...prev,
        selection: nextErrors.selection,
        jobDescription: nextErrors.jobDescription,
        submit: '',
      }));
    };

    // Handle job selection change
    const handleJobChange = (value) => {

      // Update selected job
      setSelectedJobId(value);

      // Re-validate selection
      const nextErrors = validate({ selectedJobId: value, jobDescription });

      setErrors((prev) => ({
        ...prev,
        selection: nextErrors.selection,
        jobDescription: nextErrors.jobDescription,
        submit: '',
      }));
    };

    // Load jobs from backend
    const loadJobs = async () => {
      try {
        const res = await axios.get(`${API}/jobs`, { params: { userId } });

        // Validation: ensure response is an array
        setJobs(Array.isArray(res.data) ? res.data : []);

      } catch (e) {

        // On error, fallback to empty list
        setJobs([]);

      } finally {
        setLoading(false);
      }
    };

    // Load jobs when component mounts
    useEffect(() => {

      // Validation: if user not logged in, stop loading
      if (!userId) { setLoading(false); return; }

      loadJobs();
    }, [userId]);

    // Update selected job if URL parameter changes
    useEffect(() => {
      if (jobIdParam) setSelectedJobId(jobIdParam);
    }, [jobIdParam]);

    // Handle generate button click
    const handleGenerate = async (e) => {
      e.preventDefault(); // Prevent form reload

      // Validation: ensure user is logged in
      if (!userId) return;

      // Validate inputs before sending request
      const nextErrors = validate({ selectedJobId, jobDescription });

      // If validation fails, stop submission
      if (nextErrors.selection || nextErrors.jobDescription) {
        setErrors(nextErrors);
        return;
      }

      setGenerating(true); // Start loading
      setGenerated(null); // Reset previous result
      setErrors((prev) => ({ ...prev, submit: '' }));

      try {
        // Send request to backend
        const res = await axios.post(`${API}/cover-letter/generate`, {

          // Convert IDs to numbers (type validation)
          userId: Number(userId),

          // If job selected, send ID, else null
          jobId: selectedJobId ? Number(selectedJobId) : null,

          // Trim description before sending
          jobDescription: jobDescription.trim() || null,
        });

        // Save generated result
        setGenerated(res.data);

      } catch (e) {

        // Handle submission error
        setErrors((prev) => ({
          ...prev,
          submit: 'Failed to generate cover letter. Please try again.'
        }));

      } finally {
        setGenerating(false); // Stop loading
      }
    };

    // Validation: block page if user not logged in
    if (!userId) {
      return (
        <div className="cover-letter-page">
          <div className="container">
            <p className="muted">Please log in.</p>
          </div>
        </div>
      );
    }

    return (
      <div className="cover-letter-page">
        <div className="container">

          {/* Page header */}
          <header className="page-header">
            <h1>AI-Powered Cover Letter Generator</h1>
            <p>Instantly generate personalized, professional cover letters tailored to specific job descriptions.</p>
          </header>

          {/* Form */}
          <form onSubmit={handleGenerate} className="card cover-form">

            <h3>Generate AI-Powered Cover Letter</h3>

            {/* Step 1: Select job */}
            <label>Step 1: Select a job from portal</label>

            <select
              value={selectedJobId}
              onChange={(e) => handleJobChange(e.target.value)}
              aria-invalid={Boolean(errors.selection)} // Accessibility validation
              aria-describedby={errors.selection ? 'cover-selection-error' : undefined}
            >
              <option value="">— Or paste job description below —</option>

              {/* Populate job options */}
              {jobs.map((j) => (
                <option key={j.id} value={j.id}>
                  {j.title} - {j.company || 'N/A'}
                </option>
              ))}
            </select>

            {/* Show selection error */}
            {errors.selection && <p id="cover-selection-error" className="cover-error">{errors.selection}</p>}

            {/* Step 2: Job description */}
            <label>Step 2: Or paste job description</label>

            <textarea
              placeholder="Paste job description..."
              value={jobDescription}
              onChange={(e) => handleDescriptionChange(e.target.value)}
              rows={6}
              maxLength={DESCRIPTION_MAX} // Prevent overflow
              aria-invalid={Boolean(errors.jobDescription)}
              aria-describedby={errors.jobDescription ? 'cover-description-error' : undefined}
            />

            {/* Show description error */}
            {errors.jobDescription && <p id="cover-description-error" className="cover-error">{errors.jobDescription}</p>}

            {/* Submit error */}
            {errors.submit && <p className="cover-error">{errors.submit}</p>}

            {/* Help text */}
            <p className="help-text">Tip: More details = better cover letter</p>

            {/* Submit button */}
            <button
              type="submit"
              className="btn btn-primary"

              // Disable if generating OR no input provided (validation)
              disabled={generating || (!selectedJobId && !jobDescription.trim())}
            >
              {generating ? 'Generating…' : 'Generate Tailored Cover Letter'}
            </button>
          </form>

          {/* Display generated result */}
          {generated && (
            <div className="card cover-result">

              <h3>Your AI-Generated Cover Letter</h3>

              <div className="cover-text" style={{ whiteSpace: 'pre-wrap' }}>
                {generated.generatedText}
              </div>

              <div className="cover-actions">

                {/* Copy to clipboard */}
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => {
                    navigator.clipboard.writeText(generated.generatedText);
                    alert('Cover letter copied!');
                  }}
                >
                  Copy to Clipboard
                </button>

                {/* Reset */}
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() => setGenerated(null)}
                >
                  Generate Another
                </button>
              </div>
            </div>
          )}

          {/* Navigation links */}
          <div className="page-links">
            <Link to="/jobs" className="btn btn-primary">Jobs</Link>
            <Link to="/resume" className="btn btn-ghost">Resume Match</Link>
            <Link to="/dashboard" className="btn btn-ghost">Dashboard</Link>
          </div>
        </div>
      </div>
    );
  }

  // Export component
  export default CoverLetter;
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedJobId, setSelectedJobId] = useState(jobIdParam || '');
  const [jobDescription, setJobDescription] = useState('');
  const [generated, setGenerated] = useState(null);
  const [generating, setGenerating] = useState(false);
  const [errors, setErrors] = useState({ selection: '', jobDescription: '', submit: '' });

  const validate = (values) => {
    const nextErrors = { selection: '', jobDescription: '', submit: '' };
    const hasJobId = Boolean(values.selectedJobId);
    const trimmedDescription = values.jobDescription.trim();

    if (!hasJobId && !trimmedDescription) {
      nextErrors.selection = 'Select a job or paste a job description.';
    }

    if (trimmedDescription && trimmedDescription.length < DESCRIPTION_MIN) {
      nextErrors.jobDescription = `Job description must be at least ${DESCRIPTION_MIN} characters.`;
    } else if (trimmedDescription.length > DESCRIPTION_MAX) {
      nextErrors.jobDescription = `Job description must be under ${DESCRIPTION_MAX} characters.`;
    }

    return nextErrors;
  };

  const handleDescriptionChange = (value) => {
    setJobDescription(value);
    const nextErrors = validate({ selectedJobId, jobDescription: value });
    setErrors((prev) => ({
      ...prev,
      selection: nextErrors.selection,
      jobDescription: nextErrors.jobDescription,
      submit: '',
    }));
  };

  const handleJobChange = (value) => {
    setSelectedJobId(value);
    const nextErrors = validate({ selectedJobId: value, jobDescription });
    setErrors((prev) => ({
      ...prev,
      selection: nextErrors.selection,
      jobDescription: nextErrors.jobDescription,
      submit: '',
    }));
  };

  const loadJobs = async () => {
    try {
      const res = await axios.get(`${API}/jobs`, { params: { userId } });
      setJobs(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      setJobs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!userId) { setLoading(false); return; }
    loadJobs();
  }, [userId]);

  useEffect(() => {
    if (jobIdParam) setSelectedJobId(jobIdParam);
  }, [jobIdParam]);

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!userId) return;

    const nextErrors = validate({ selectedJobId, jobDescription });
    if (nextErrors.selection || nextErrors.jobDescription) {
      setErrors(nextErrors);
      return;
    }

    setGenerating(true);
    setGenerated(null);
    setErrors((prev) => ({ ...prev, submit: '' }));
    try {
      const res = await axios.post(`${API}/cover-letter/generate`, {
        userId: Number(userId),
        jobId: selectedJobId ? Number(selectedJobId) : null,
        jobDescription: jobDescription.trim() || null,
      });
      setGenerated(res.data);
    } catch (e) {
      setErrors((prev) => ({ ...prev, submit: 'Failed to generate cover letter. Please try again.' }));
    } finally {
      setGenerating(false);
    }
  };

  if (!userId) {
    return <div className="cover-letter-page"><div className="container"><p className="muted">Please log in.</p></div></div>;
  }

  return (
    <div className="cover-letter-page">
      <div className="container">
        <header className="page-header">
          <h1>AI-Powered Cover Letter Generator</h1>
          <p>Instantly generate personalized, professional cover letters tailored to specific job descriptions. Save time and increase your application success rate.</p>
        </header>

        <form onSubmit={handleGenerate} className="card cover-form">
          <h3>Generate AI-Powered Cover Letter</h3>
          <label>Step 1: Select a job from portal</label>
          <select
            value={selectedJobId}
            onChange={(e) => handleJobChange(e.target.value)}
            aria-invalid={Boolean(errors.selection)}
            aria-describedby={errors.selection ? 'cover-selection-error' : undefined}
          >
            <option value="">— Or paste job description below —</option>
            {jobs.map((j) => (
              <option key={j.id} value={j.id}>{j.title} - {j.company || 'N/A'}</option>
            ))}
          </select>
          {errors.selection && <p id="cover-selection-error" className="cover-error">{errors.selection}</p>}
          <label>Step 2: Or paste job description</label>
          <textarea
            placeholder="Or paste the full job description here. Include job title, company, responsibilities, and required skills."
            value={jobDescription}
            onChange={(e) => handleDescriptionChange(e.target.value)}
            rows={6}
            maxLength={DESCRIPTION_MAX}
            aria-invalid={Boolean(errors.jobDescription)}
            aria-describedby={errors.jobDescription ? 'cover-description-error' : undefined}
          />
          {errors.jobDescription && <p id="cover-description-error" className="cover-error">{errors.jobDescription}</p>}
          {errors.submit && <p className="cover-error">{errors.submit}</p>}
          <p className="help-text">Tip: The more detailed the job description, the better your cover letter will be.</p>
          <button type="submit" className="btn btn-primary" disabled={generating || (!selectedJobId && !jobDescription.trim())}>
            {generating ? 'Generating…' : 'Generate Tailored Cover Letter'}
          </button>
        </form>

        {generated && (
          <div className="card cover-result">
            <h3>Your AI-Generated Cover Letter</h3>
            <p className="cover-intro">Professional, personalized, and ready to use. Feel free to customize further.</p>
            <div className="cover-text" style={{ whiteSpace: 'pre-wrap', lineHeight: '1.6' }}>{generated.generatedText}</div>
            <div className="cover-actions">
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => {
                  navigator.clipboard.writeText(generated.generatedText);
                  alert('Cover letter copied to clipboard!');
                }}
              >
                Copy to Clipboard
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setGenerated(null)}
              >
                Generate Another
              </button>
            </div>
          </div>
        )}

        <div className="page-links">
          <Link to="/jobs" className="btn btn-primary">Jobs</Link>
          <Link to="/resume" className="btn btn-ghost">Resume Match</Link>
          <Link to="/dashboard" className="btn btn-ghost">Dashboard</Link>
        </div>
      </div>
    </div>
  );
}

export default CoverLetter;
