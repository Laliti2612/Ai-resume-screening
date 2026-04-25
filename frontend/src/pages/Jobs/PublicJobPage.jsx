import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import './PublicJobPage.css';

const PublicJobPage = () => {
  const { publicId }              = useParams();
  const [job, setJob]             = useState(null);
  const [loading, setLoading]     = useState(true);
  const [notFound, setNotFound]   = useState(false);
  const [file, setFile]           = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadMsg, setUploadMsg] = useState('');
  const [applied, setApplied]     = useState(false);

  useEffect(() => {
    if (!publicId) {
      setNotFound(true);
      setLoading(false);
      return;
    }

    axios.get(`http://localhost:8080/api/jobs/public/${publicId}`)
      .then(res => {
        if (res.data && res.data.success && res.data.data) {
          setJob(res.data.data);
        } else {
          setNotFound(true);
        }
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [publicId]);

  const handleApply = async () => {
    if (!file) {
      setUploadMsg('⚠️ Please select your resume PDF first.');
      return;
    }
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      setUploadMsg('⚠️ Only PDF files are accepted.');
      return;
    }

    setUploading(true);
    setUploadMsg('');

    const formData = new FormData();
    formData.append('file', file);
    formData.append('jobId', job.id);

    try {
      const res = await axios.post(
        'http://localhost:8080/api/resumes/upload/public',
        formData,
        { headers: { 'Content-Type': 'multipart/form-data' } }
      );

      if (res.data && res.data.success) {
        setApplied(true);
      } else {
        setUploadMsg('✗ ' + (res.data?.message || 'Upload failed. Try again.'));
      }
    } catch (err) {
      setUploadMsg('✗ ' + (err.response?.data?.message || 'Failed to submit. Please try again.'));
    } finally {
      setUploading(false);
    }
  };

  // ── Loading ──────────────────────────────────────────────
  if (loading) return (
    <div className="public-page">
      <div className="public-header">
        <div className="public-brand">
          <span className="brand-icon">🤖</span>
          <span className="brand-name">AI Recruit</span>
        </div>
      </div>
      <div className="public-loading">
        <div className="spinner"></div>
        <p>Loading job details...</p>
      </div>
    </div>
  );

  // ── Not Found ────────────────────────────────────────────
  if (notFound) return (
    <div className="public-page">
      <div className="public-header">
        <div className="public-brand">
          <span className="brand-icon">🤖</span>
          <span className="brand-name">AI Recruit</span>
        </div>
      </div>
      <div className="public-container">
        <div className="public-not-found">
          <div style={{ fontSize: 64 }}>😕</div>
          <h2>Job Not Found</h2>
          <p>This job posting may have been removed or the link is invalid.</p>
        </div>
      </div>
    </div>
  );

  // ── Applied Success ──────────────────────────────────────
  if (applied) return (
    <div className="public-page">
      <div className="public-header">
        <div className="public-brand">
          <span className="brand-icon">🤖</span>
          <span className="brand-name">AI Recruit</span>
        </div>
      </div>
      <div className="public-container">
        <div className="public-success">
          <div style={{ fontSize: 64 }}>🎉</div>
          <h2>Application Submitted!</h2>
          <p>Your resume has been submitted for <strong>{job?.title}</strong>.</p>
          <p style={{ color: '#64748b', fontSize: 14 }}>
            Our HR team will review your application using AI screening
            and get back to you soon.
          </p>
        </div>
      </div>
    </div>
  );

  const skills = job.requiredSkills
    ? job.requiredSkills.split(',').map(s => s.trim()).filter(Boolean)
    : [];

  return (
    <div className="public-page">

      {/* ── Header ────────────────────────────────────────── */}
      <div className="public-header">
        <div className="public-brand">
          <span className="brand-icon">🤖</span>
          <span className="brand-name">AI Recruit</span>
        </div>
      </div>

      <div className="public-container">
        <div className="public-job-card">

          {/* Status badge */}
          <div className="public-status-row">
            <span className={`public-status ${
              job.status === 'ACTIVE' ? 'active' : 'closed'}`}>
              {job.status === 'ACTIVE'
                ? '🟢 Actively Hiring'
                : '🔴 Position Closed'}
            </span>
          </div>

          {/* Job Title */}
          <h1 className="public-job-title">{job.title}</h1>

          {/* Meta */}
          <div className="public-meta">
            {job.location && (
              <div className="meta-item">
                <span className="meta-icon">📍</span>
                <span>{job.location}</span>
              </div>
            )}
            <div className="meta-item">
              <span className="meta-icon">💼</span>
              <span>{job.experienceMin}–{job.experienceMax} years experience</span>
            </div>
            <div className="meta-item">
              <span className="meta-icon">📅</span>
              <span>Posted {job.createdAt
                ? new Date(job.createdAt).toLocaleDateString('en-IN', {
                    day: 'numeric', month: 'long', year: 'numeric'
                  })
                : 'Recently'}</span>
            </div>
          </div>

          <div className="public-divider"></div>

          {/* Required Skills */}
          {skills.length > 0 && (
            <div className="public-section">
              <h3>🛠 Required Skills</h3>
              <div className="skills-tags">
                {skills.map((skill, i) => (
                  <span key={i} className="skill-tag">{skill}</span>
                ))}
              </div>
            </div>
          )}

          {/* Description */}
          {job.description && (
            <div className="public-section">
              <h3>📋 Job Description</h3>
              <p className="public-description">{job.description}</p>
            </div>
          )}

          <div className="public-divider"></div>

          {/* ── Apply Section ────────────────────────────── */}
          {job.status === 'ACTIVE' ? (
            <div className="public-apply">
              <h3>📤 Apply for this Position</h3>
              <p style={{ color: '#64748b', fontSize: 14, marginBottom: 16 }}>
                Upload your resume in PDF format.
                Our AI will screen it automatically.
              </p>

              {/* File Upload Area */}
              <div
                className="file-upload-area"
                onClick={() => document.getElementById('resumeFile').click()}>
                <input
                  id="resumeFile"
                  type="file"
                  accept=".pdf"
                  style={{ display: 'none' }}
                  onChange={e => {
                    setFile(e.target.files[0]);
                    setUploadMsg('');
                  }}
                />
                {file ? (
                  <div className="file-selected">
                    <span style={{ fontSize: 36 }}>📄</span>
                    <div>
                      <div style={{ fontWeight: 700, color: '#1e293b' }}>
                        {file.name}
                      </div>
                      <div style={{ fontSize: 12, color: '#64748b' }}>
                        {(file.size / 1024).toFixed(1)} KB — Click to change
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="file-placeholder">
                    <span style={{ fontSize: 48 }}>📁</span>
                    <div style={{ fontWeight: 600, color: '#3b82f6', fontSize: 15 }}>
                      Click to upload your resume
                    </div>
                    <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 4 }}>
                      PDF only • Max 10MB
                    </div>
                  </div>
                )}
              </div>

              {/* Error / Warning message */}
              {uploadMsg && (
                <div className={`upload-msg ${
                  uploadMsg.startsWith('✗') ? 'error' : 'warn'}`}>
                  {uploadMsg}
                </div>
              )}

              {/* Submit Button */}
              <button
                className="apply-btn"
                onClick={handleApply}
                disabled={uploading || !file}>
                {uploading
                  ? '⏳ Submitting your application...'
                  : '🚀 Submit Application'}
              </button>
            </div>
          ) : (
            <div className="public-closed">
              🔴 This job posting is currently closed. No longer accepting applications.
            </div>
          )}
        </div>

        <p className="public-footer">
          Powered by <strong>AI Recruit</strong> — AI-Based Resume Screening System
        </p>
      </div>
    </div>
  );
};

export default PublicJobPage;
