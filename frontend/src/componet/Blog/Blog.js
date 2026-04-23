import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import './Blog.css';

// Base API URL
const API = 'http://localhost:8080/api';

function Blog() {

  // Validation constants (min/max limits for input fields)
  const TITLE_MIN = 5;
  const TITLE_MAX = 120;
  const CONTENT_MIN = 30;
  const CONTENT_MAX = 5000;

  // Get user info from local storage
  const userId = localStorage.getItem('userId');
  const role = localStorage.getItem('userRole');

  // Check if user is alumni (only alumni can create/delete posts)
  const isAlumni = role === 'ALUMNI';

  // State variables
  const [posts, setPosts] = useState([]);              // List of blog posts
  const [loading, setLoading] = useState(true);        // Loading state
  const [showForm, setShowForm] = useState(false);     // Toggle form visibility
  const [form, setForm] = useState({ title: '', content: '' }); // Form data
  const [formErrors, setFormErrors] = useState({ title: '', content: '', submit: '' }); // Validation errors
  const [viewId, setViewId] = useState(null);          // Selected post ID
  const [viewPost, setViewPost] = useState(null);      // Selected post data

  // Validation function for form inputs
  const validateForm = (values) => {

    // Initialize error object
    const next = { title: '', content: '', submit: '' };

    // Trim input values (removes spaces)
    const title = values.title.trim();
    const content = values.content.trim();

    // -------- TITLE VALIDATION --------
    // Check if title is empty
    if (!title) {
      next.title = 'Title is required.';

      // Check minimum length
    } else if (title.length < TITLE_MIN) {
      next.title = `Title must be at least ${TITLE_MIN} characters.`;

      // Check maximum length
    } else if (title.length > TITLE_MAX) {
      next.title = `Title must be under ${TITLE_MAX} characters.`;
    }

    // -------- CONTENT VALIDATION --------
    // Check if content is empty
    if (!content) {
      next.content = 'Content is required.';

      // Check minimum length
    } else if (content.length < CONTENT_MIN) {
      next.content = `Content must be at least ${CONTENT_MIN} characters.`;

      // Check maximum length
    } else if (content.length > CONTENT_MAX) {
      next.content = `Content must be under ${CONTENT_MAX} characters.`;
    }

    // Return validation errors
    return next;
  };

  // Check if there are any validation errors
  const hasErrors = (errors) => Boolean(errors.title || errors.content);

  // Handle input changes
  const handleChange = (field, value) => {

    // Update form state
    const nextForm = { ...form, [field]: value };
    setForm(nextForm);

    // Validate updated form
    const nextErrors = validateForm(nextForm);

    // Update error state
    setFormErrors((prev) => ({
      ...prev,
      title: nextErrors.title,
      content: nextErrors.content,
      submit: ''
    }));
  };

  // Load all blog posts from backend
  const loadPosts = async () => {
    try {
      const res = await axios.get(`${API}/blog`);

      // Ensure response is an array (basic validation)
      setPosts(Array.isArray(res.data) ? res.data : []);

    } catch (e) {

      // If error occurs, set empty list
      setPosts([]);

    } finally {

      // Stop loading state
      setLoading(false);
    }
  };

  // Load posts when component mounts
  useEffect(() => {
    loadPosts();
  }, []);

  // Load single post when viewId changes
  useEffect(() => {

    // If no post selected, reset
    if (!viewId) { setViewPost(null); return; }

    // Fetch post by ID
    axios.get(`${API}/blog/${viewId}`)
      .then((res) => setViewPost(res.data))
      .catch(() => setViewPost(null)); // Handle error

  }, [viewId]);

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevent page reload

    // Validate form before submitting
    const errors = validateForm(form);

    // If errors exist, stop submission
    if (hasErrors(errors)) {
      setFormErrors(errors);
      return;
    }

    try {
      // Send POST request to backend
      await axios.post(`${API}/blog`, {

        // Trim values before sending (validation/sanitization)
        title: form.title.trim(),
        content: form.content.trim(),
        authorId: Number(userId), // Convert to number
      });

      // Reset form after success
      setForm({ title: '', content: '' });
      setFormErrors({ title: '', content: '', submit: '' });
      setShowForm(false);

      // Reload posts
      loadPosts();

    } catch (e) {

      // Show submission error
      setFormErrors((prev) => ({
        ...prev,
        submit: 'Failed to publish blog post. Please try again.'
      }));
    }
  };

  // Delete blog post
  const deletePost = async (id) => {

    // Confirm before deleting (user validation)
    if (!window.confirm('Delete this post?')) return;

    try {
      await axios.delete(`${API}/blog/${id}`);

      // Reset view and reload posts
      setViewId(null);
      loadPosts();

    } catch (e) {
      alert('Failed to delete'); // Error handling
    }
  };

  // Format date for display
  const formatDate = (d) => (d ? new Date(d).toLocaleString() : '');

  // If user not logged in, block access (validation)
  if (!userId) {
    return (
      <div className="blog-page">
        <div className="container">
          <p className="muted">Please log in.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="blog-page">
      <div className="container">

        {/* Page header */}
        <header className="page-header">
          <h1>Alumni Blog</h1>
          <p>A knowledge-sharing space where alumni can publish experiences and students can learn from real career journeys.</p>
        </header>

        {/* Only alumni can create posts (role-based validation) */}
        {isAlumni && (
          <>
            <button type="button" className="btn btn-primary" onClick={() => setShowForm(true)}>
              + Write New Blog
            </button>

            {/* Blog form */}
            {showForm && (
              <form onSubmit={handleSubmit} className="card blog-form">

                <h3>Write Blog Post</h3>

                {/* Title input with validation */}
                <input
                  placeholder="Title"
                  value={form.title}
                  onChange={(e) => handleChange('title', e.target.value)}
                  maxLength={TITLE_MAX} // Prevent exceeding max length
                  aria-invalid={Boolean(formErrors.title)}
                  aria-describedby={formErrors.title ? 'blog-title-error' : undefined}
                  required
                />

                {/* Display title error */}
                {formErrors.title && <p id="blog-title-error" className="form-error">{formErrors.title}</p>}

                {/* Content input with validation */}
                <textarea
                  placeholder="Content..."
                  value={form.content}
                  onChange={(e) => handleChange('content', e.target.value)}
                  rows={8}
                  maxLength={CONTENT_MAX} // Prevent exceeding max length
                  aria-invalid={Boolean(formErrors.content)}
                  aria-describedby={formErrors.content ? 'blog-content-error' : undefined}
                  required
                />

                {/* Display content error */}
                {formErrors.content && <p id="blog-content-error" className="form-error">{formErrors.content}</p>}

                {/* Display submission error */}
                {formErrors.submit && <p className="form-error">{formErrors.submit}</p>}

                {/* Form buttons */}
                <div className="form-actions">
                  <button type="submit" className="btn btn-primary">Publish</button>

                  <button
                    type="button"
                    className="btn btn-ghost"
                    onClick={() => {
                      setShowForm(false);
                      setFormErrors({ title: '', content: '', submit: '' });
                    }}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            )}
          </>
        )}

        {/* Loading state */}
        {loading ? (
          <p className="muted">Loading…</p>

          // Single post view
        ) : viewPost ? (
          <article className="card blog-full">

            <button type="button" className="back-btn" onClick={() => setViewId(null)}>
              ← Back to list
            </button>

            <h2>{viewPost.title}</h2>
            <p className="blog-meta">{formatDate(viewPost.createdAt)}</p>
            <div className="blog-body">{viewPost.content}</div>

            {/* Delete allowed only for alumni */}
            {isAlumni && (
              <button type="button" className="btn-sm btn-danger" onClick={() => deletePost(viewPost.id)}>
                Delete
              </button>
            )}
          </article>

          // Blog list view
        ) : (
          <div className="blog-list">

            {/* If no posts */}
            {posts.length === 0 ? (
              <p className="muted">No posts yet.</p>

            ) : (

              // Display posts
              posts.map((p) => (
                <article key={p.id} className="card blog-card" onClick={() => setViewId(p.id)}>

                  <h3>{p.title}</h3>
                  <p className="blog-meta">{formatDate(p.createdAt)}</p>

                  {/* Preview first 120 characters */}
                  <p className="blog-preview">{p.content?.slice(0, 120)}…</p>

                  {/* Delete button (alumni only) */}
                  {isAlumni && (
                    <div className="blog-card-actions">
                      <button
                        type="button"
                        className="btn-sm btn-danger"
                        onClick={(e) => {
                          e.stopPropagation(); // Prevent opening post
                          deletePost(p.id);
                        }}
                      >
                        Delete Blog
                      </button>
                    </div>
                  )}
                </article>
              ))
            )}
          </div>
        )}

        {/* Navigation links */}
        <div className="page-links">
          <Link to="/jobs" className="btn btn-primary">Jobs</Link>
          <Link to="/dashboard" className="btn btn-ghost">Dashboard</Link>
        </div>
      </div>
    </div>
  );
}

// Export component
export default Blog;