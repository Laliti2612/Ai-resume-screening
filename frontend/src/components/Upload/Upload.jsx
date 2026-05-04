import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import './upload.css';

const Upload = () => {
  const [jobs, setJobs] = useState([]);
  const [jobId, setJobId] = useState('');
  const [files, setFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState({});
  const [results, setResults] = useState([]);
  const [dragging, setDragging] = useState(false);
  const inputRef = useRef();

  const token = localStorage.getItem('jwt_token') || '';
  const userEmail = localStorage.getItem('user_email') || '';

  const getAuthHeader = () => ({
    Authorization: `Bearer ${token}`
  });

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    if (!token) return;
    axios.get('http://localhost:8080/api/jobs', {
      headers: getAuthHeader()
    })
    .then(res => {
      const allJobs = Array.isArray(res.data) ? res.data :
                      Array.isArray(res.data.data) ? res.data.data : [];
      setJobs(allJobs);
      if (allJobs.length > 0) setJobId(String(allJobs[0].id));
    })
    .catch(err => {
      console.error('Failed to load jobs:', err);
      setJobs([]);
    });
  }, []);

  const handleFiles = (newFiles) => {
    const valid = Array.from(newFiles).filter(f =>
      ['.pdf', '.doc', '.docx'].some(ext =>
        f.name.toLowerCase().endsWith(ext))
    );
    setFiles(prev => [...prev, ...valid]);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    handleFiles(e.dataTransfer.files);
  };

  const removeFile = (index) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
  };

  const uploadAll = async () => {
    if (!jobId) {
      alert('Please select a job first');
      return;
    }
    if (files.length === 0) {
      alert('Please select at least one resume file');
      return;
    }

    setUploading(true);
    setResults([]);
    const allResults = [];

    for (const file of files) {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('jobId', jobId);

      try {
        console.log(`Uploading: ${file.name} | jobId: ${jobId} | user: ${userEmail}`);

        const res = await axios.post(
          'http://localhost:8080/api/resumes/upload',
          formData,
          {
            headers: {
              Authorization: `Bearer ${token}`
            },
            onUploadProgress: (e) => {
              const pct = Math.round((e.loaded * 100) / e.total);
              setProgress(prev => ({ ...prev, [file.name]: pct }));
            }
          }
        );

        console.log('Upload response:', res.data);

        const data  = res.data?.data  || {};
        const score = data?.score     || {};

        allResults.push({
          name:          file.name,
          success:       true,
          candidateId:   data.candidateId   || null,
          candidateName: data.candidateName || 'Unknown',
          totalScore:    score.totalScore      ?? 0,
          skillScore:    score.skillScore      ?? 0,
          expScore:      score.experienceScore ?? 0,
          eduScore:      score.educationScore  ?? 0,
          kwScore:       score.keywordScore    ?? 0,
          status:        data.recommendation  || score.recommendation || 'NEW',
          aiSummary:     data.aiSummary || score.aiSummary || '',
          skills:        score.extractedSkills || [],
        });

      } catch (e) {
        console.error('Upload error:', e.response?.data || e.message);
        allResults.push({
          name:    file.name,
          success: false,
          error:   e.response?.data?.message || e.message || 'Upload failed'
        });
      }
    }

    setResults(allResults);
    setFiles([]);
    setProgress({});
    setUploading(false);
  };

  const getStatusStyle = (status) => {
    const map = {
      SHORTLISTED: { bg: '#f0fdf4', color: '#16a34a',
                     border: '#86efac', label: '✓ Shortlisted' },
      NEW:         { bg: '#eff6ff', color: '#2563eb',
                     border: '#93c5fd', label: '~ Under Review' },
      REJECTED:    { bg: '#fef2f2', color: '#dc2626',
                     border: '#fca5a5', label: '✗ Rejected' },
    };
    return map[status] || map['NEW'];
  };

  const getScoreColor = (score) => {
    if (score >= 85) return '#16a34a';
    if (score >= 70) return '#2563eb';
    if (score >= 45) return '#ea580c';
    return '#dc2626';
  };

  const selectedJob = jobs.find(j => String(j.id) === String(jobId));

  return (
    <div className="upload-page">
      <div className="upload-header">
        <h2>Upload Resumes</h2>
        <p>AI-powered screening — get instant score and recommendation</p>
        {userEmail && (
          <p style={{ fontSize: 12, color: '#64748b', marginTop: 4 }}>
            Uploading as: <strong>{userEmail}</strong>
          </p>
        )}
      </div>

      {/* Job Selection */}
      <div className="job-select-card">
        <label className="job-select-label">Select Job Posting</label>
        {jobs.length === 0 ? (
          <div className="no-jobs">
            No active jobs found. Please create a job first from the Jobs page.
          </div>
        ) : (
          <select
            className="job-select"
            value={jobId}
            onChange={e => setJobId(e.target.value)}>
            {jobs.map(j => (
              <option key={j.id} value={j.id}>
                {j.title} — {j.location || 'N/A'}
                {j.experienceMin === 0
                  ? ' (Fresher / 0 yrs)'
                  : ` (${j.experienceMin}–${j.experienceMax} yrs)`}
              </option>
            ))}
          </select>
        )}
        {selectedJob && (
          <div className="job-skills-preview">
            <strong>Required Skills: </strong>
            <span className="skills-text">
              {selectedJob.requiredSkills || 'Not specified'}
            </span>
            <span style={{ marginLeft: 16, color: '#64748b', fontSize: 13 }}>
              Experience: {selectedJob.experienceMin}–{selectedJob.experienceMax} yrs
            </span>
          </div>
        )}
      </div>

      {/* Drop Zone */}
      <div
        className={`drop-zone ${dragging ? 'dragging' : ''}`}
        onClick={() => inputRef.current.click()}
        onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
        onDragLeave={() => setDragging(false)}
        onDrop={handleDrop}>
        <div className="drop-icon">📄</div>
        <p>Drag and drop resumes here</p>
        <span>or click to browse — PDF, DOC, DOCX up to 10MB</span>
        <input
          ref={inputRef}
          type="file"
          multiple
          accept=".pdf,.doc,.docx"
          style={{ display: 'none' }}
          onChange={e => handleFiles(e.target.files)}
        />
      </div>

      {/* File List */}
      {files.length > 0 && (
        <div className="file-list">
          <div className="file-list-header">
            {files.length} file(s) ready to upload
          </div>
          {files.map((f, i) => (
            <div key={i} className="file-item">
              <div className="file-info">
                <span className="file-icon">📎</span>
                <div>
                  <div className="file-name">{f.name}</div>
                  <div className="file-size">
                    {(f.size / 1024).toFixed(1)} KB
                  </div>
                </div>
              </div>
              {progress[f.name] !== undefined ? (
                <div className="file-progress">
                  <div className="file-progress-bar">
                    <div style={{ width: `${progress[f.name]}%` }} />
                  </div>
                  <span>{progress[f.name]}%</span>
                </div>
              ) : (
                <button className="remove-btn"
                        onClick={() => removeFile(i)}>✕</button>
              )}
            </div>
          ))}
          <button
            className="upload-btn"
            onClick={uploadAll}
            disabled={uploading || !jobId || jobs.length === 0}>
            {uploading
              ? '🤖 AI is processing...'
              : `Upload & Screen ${files.length} Resume(s)`}
          </button>
        </div>
      )}

      {/* Results */}
      {results.length > 0 && (
        <div className="results-section">
          <h3>Screening Results</h3>
          {results.map((r, i) => {
            if (!r.success) {
              return (
                <div key={i} className="result-card failed">
                  <div className="result-header">
                    <span className="result-filename">📎 {r.name}</span>
                    <span className="result-error">✗ {r.error}</span>
                  </div>
                </div>
              );
            }

            const st = getStatusStyle(r.status);
            return (
              <div key={i} className="result-card success"
                   style={{ borderColor: st.border }}>
                <div className="result-header" style={{ background: st.bg }}>
                  <div className="result-name-section">
                    <div className="result-candidate">
                      👤 {r.candidateName}
                    </div>
                    <div className="result-filename">{r.name}</div>
                  </div>
                  <div className="result-right">
                    <div
                      className="result-score-circle"
                      style={{
                        borderColor: getScoreColor(r.totalScore),
                        color:       getScoreColor(r.totalScore)
                      }}>
                      <div className="rsc-num">{r.totalScore}</div>
                      <div className="rsc-label">Score</div>
                    </div>
                    <div
                      className="result-status-badge"
                      style={{ background: st.color, color: 'white' }}>
                      {st.label}
                    </div>
                  </div>
                </div>

                <div className="result-body">
                  <div className="score-bars">
                    {[
                      { label: 'Skill Match', value: r.skillScore,
                        color: '#3b82f6', weight: '45%' },
                      { label: 'Experience',  value: r.expScore,
                        color: '#22c55e',  weight: '25%' },
                      { label: 'Education',   value: r.eduScore,
                        color: '#a855f7',  weight: '15%' },
                      { label: 'Keywords',    value: r.kwScore,
                        color: '#f97316',  weight: '15%' },
                    ].map(item => (
                      <div key={item.label} className="score-bar-row">
                        <div className="score-bar-label">
                          <span>{item.label}</span>
                          <span className="score-bar-weight">
                            {item.weight}
                          </span>
                          <span style={{
                            color: item.color, fontWeight: 700
                          }}>
                            {item.value}
                          </span>
                        </div>
                        <div className="score-bar-track">
                          <div
                            className="score-bar-fill"
                            style={{
                              width:      `${item.value}%`,
                              background: item.color
                            }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>

                  {r.skills && r.skills.length > 0 && (
                    <div className="result-skills">
                      <div className="skills-title">Extracted Skills</div>
                      <div className="skills-tags">
                        {r.skills.slice(0, 15).map((s, idx) => (
                          <span key={idx} className="skill-tag">{s}</span>
                        ))}
                        {r.skills.length > 15 && (
                          <span className="skill-tag more">
                            +{r.skills.length - 15} more
                          </span>
                        )}
                      </div>
                    </div>
                  )}

                  {r.aiSummary && (
                    <div className="result-summary">
                      <span className="summary-icon">🤖</span>
                      <p>{r.aiSummary}</p>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Upload;