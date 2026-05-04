import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './Jobs.css';

const Jobs = () => {
  const [jobs, setJobs]             = useState([]);
  const [showForm, setShowForm]     = useState(false);
  const [form, setForm]             = useState({
    title: '', description: '', location: '',
    experienceMin: 0, experienceMax: 5, requiredSkills: ''
  });
  const [message, setMessage]       = useState('');
  const [copiedId, setCopiedId]     = useState(null);
  const [shareModal, setShareModal] = useState(null);

  const token = localStorage.getItem('jwt_token');

  const fetchJobs = () => {
    axios.get('http://localhost:8080/api/jobs', {
      headers: { Authorization: `Bearer ${token}` }
    })
    .then(res => {
      const data = Array.isArray(res.data) ? res.data :
                   Array.isArray(res.data.data) ? res.data.data : [];
      setJobs(data);
    })
    .catch(err => console.log('Jobs error:', err));
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { fetchJobs(); }, []);

  const handleSave = async () => {
    try {
      await axios.post(
        'http://localhost:8080/api/jobs',
        {
          title:          form.title,
          description:    form.description,
          location:       form.location,
          experienceMin:  parseInt(form.experienceMin),
          experienceMax:  parseInt(form.experienceMax),
          requiredSkills: form.requiredSkills,
          status: 'ACTIVE'
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      setMessage('✓ Job created successfully!');
      setShowForm(false);
      setForm({ title: '', description: '', location: '',
                experienceMin: 0, experienceMax: 5, requiredSkills: '' });
      fetchJobs();
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      setMessage('✗ Failed to create job: ' + (err.response?.data || err.message));
    }
  };

  const deleteJob = async (id) => {
    if (!window.confirm('Are you sure you want to delete this job?')) return;
    try {
      await axios.delete(
        `http://localhost:8080/api/jobs/${id}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setJobs(prev => prev.filter(j => j.id !== id));
      setMessage('✓ Job deleted successfully!');
      setTimeout(() => setMessage(''), 3000);
    } catch {
      setMessage('✗ Failed to delete job.');
    }
  };

  const getPublicUrl = (job) => {
    return `${window.location.origin}/jobs/public/${job.publicId}`;
  };

  const copyLink = (job, e) => {
    e.stopPropagation();
    navigator.clipboard.writeText(getPublicUrl(job)).then(() => {
      setCopiedId(job.id);
      setTimeout(() => setCopiedId(null), 2000);
    });
  };

  const openShareModal = (job, e) => {
    e.stopPropagation();
    setShareModal(job);
  };

  const statusColor = (status) => {
    const map = {
      ACTIVE: { bg: '#f0fdf4', color: '#16a34a' },
      CLOSED: { bg: '#fef2f2', color: '#dc2626' },
      DRAFT:  { bg: '#fff7ed', color: '#ea580c' },
    };
    return map[status] || { bg: '#f8fafc', color: '#64748b' };
  };

  return (
    <div className="jobs-page">
      <div className="page-header">
        <div>
          <h2>Job Postings</h2>
          <p>Create and manage job openings</p>
        </div>
        <button className="add-btn" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ Add New Job'}
        </button>
      </div>

      {message && (
        <div className={`message ${message.startsWith('✓') ? 'success' : 'error'}`}>
          {message}
        </div>
      )}

      {showForm && (
        <div className="job-form">
          <h3>Create New Job</h3>
          <div className="form-grid">
            <div className="form-field">
              <label>Job Title</label>
              <input placeholder="e.g. Senior Java Developer"
                value={form.title}
                onChange={e => setForm({ ...form, title: e.target.value })} />
            </div>
            <div className="form-field">
              <label>Location</label>
              <input placeholder="e.g. Hyderabad"
                value={form.location}
                onChange={e => setForm({ ...form, location: e.target.value })} />
            </div>
            <div className="form-field">
              <label>Min Experience (yrs)</label>
              <input type="number" min="0" max="20"
                value={form.experienceMin}
                onChange={e => setForm({ ...form, experienceMin: e.target.value })} />
            </div>
            <div className="form-field">
              <label>Max Experience (yrs)</label>
              <input type="number" min="0" max="20"
                value={form.experienceMax}
                onChange={e => setForm({ ...form, experienceMax: e.target.value })} />
            </div>
            <div className="form-field full-width">
              <label>Required Skills (comma separated)</label>
              <input placeholder="e.g. Java, Spring Boot, MySQL"
                value={form.requiredSkills}
                onChange={e => setForm({ ...form, requiredSkills: e.target.value })} />
            </div>
            <div className="form-field full-width">
              <label>Description</label>
              <textarea placeholder="Job description..."
                value={form.description}
                onChange={e => setForm({ ...form, description: e.target.value })} />
            </div>
          </div>
          <button className="save-btn" onClick={handleSave}>Save Job</button>
        </div>
      )}

      <div className="table-card">
        <table className="jobs-table">
          <thead>
            <tr>
              <th>Job Title</th>
              <th>Location</th>
              <th>Experience</th>
              <th>Skills</th>
              <th>Status</th>
              <th>Created</th>
              <th>Share</th>
              <th>Delete</th>
            </tr>
          </thead>
          <tbody>
            {jobs.length === 0 ? (
              <tr>
                <td colSpan="8" className="empty">
                  No jobs found — click Add New Job to create one
                </td>
              </tr>
            ) : (
              jobs.map(j => {
                const st = statusColor(j.status);
                return (
                  <tr key={j.id}>
                    <td className="job-title">{j.title}</td>
                    <td>{j.location}</td>
                    <td>{j.experienceMin}–{j.experienceMax} yrs</td>
                    <td className="skills-cell">{j.requiredSkills}</td>
                    <td>
                      <span className="badge" style={{ background: st.bg, color: st.color }}>
                        {j.status}
                      </span>
                    </td>
                    <td>{j.createdAt ? new Date(j.createdAt).toLocaleDateString() : '-'}</td>
                    <td onClick={e => e.stopPropagation()}>
                      <button className="share-btn" onClick={e => openShareModal(j, e)} title="Share job link">
                        🔗 Share
                      </button>
                    </td>
                    <td>
                      <button className="delete-btn" onClick={() => deleteJob(j.id)} title="Delete job">🗑</button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {shareModal && (
        <div className="modal-overlay" onClick={() => setShareModal(null)}>
          <div className="share-modal" onClick={e => e.stopPropagation()}>
            <div className="share-modal-header">
              <div>
                <h3>🔗 Share Job Opening</h3>
                <p style={{ color: '#64748b', fontSize: 13, margin: 0 }}>{shareModal.title}</p>
              </div>
              <button className="modal-close" onClick={() => setShareModal(null)}>✕</button>
            </div>
            <div className="share-modal-body">
              <div className="share-job-info">
                <div className="share-info-row"><span>📍</span><span>{shareModal.location || 'Not specified'}</span></div>
                <div className="share-info-row"><span>💼</span><span>{shareModal.experienceMin}–{shareModal.experienceMax} years experience</span></div>
                <div className="share-info-row"><span>🛠</span><span>{shareModal.requiredSkills}</span></div>
              </div>
              <div className="share-link-label">🔗 Public Link</div>
              <div className="share-link-box">
                <input type="text" readOnly value={getPublicUrl(shareModal)} className="share-link-input" />
                <button
                  className={`copy-btn ${copiedId === shareModal.id ? 'copied' : ''}`}
                  onClick={e => copyLink(shareModal, e)}>
                  {copiedId === shareModal.id ? '✓ Copied!' : '📋 Copy'}
                </button>
              </div>
              <p style={{ fontSize: 12, color: '#94a3b8', margin: '10px 0 16px', textAlign: 'center' }}>
                Anyone with this link can view and apply — no login needed
              </p>
              <div className="share-options">
                <button className="share-option-btn whatsapp"
                  onClick={() => {
                    const url  = getPublicUrl(shareModal);
                    const text = `🚀 *Job Opening: ${shareModal.title}*\n📍 ${shareModal.location}\n💼 ${shareModal.experienceMin}-${shareModal.experienceMax} yrs exp\n🛠 ${shareModal.requiredSkills}\n\n👉 Apply here: ${url}`;
                    window.open(`https://wa.me/?text=${encodeURIComponent(text)}`, '_blank');
                  }}>
                  💬 WhatsApp
                </button>
                <button className="share-option-btn linkedin"
                  onClick={() => {
                    const url = getPublicUrl(shareModal);
                    window.open(`https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}`, '_blank');
                  }}>
                  💼 LinkedIn
                </button>
                <button className="share-option-btn email"
                  onClick={() => {
                    const url     = getPublicUrl(shareModal);
                    const subject = `Job Opening: ${shareModal.title}`;
                    const body    = `Hi,\n\nWe have a job opening:\n\nRole: ${shareModal.title}\nLocation: ${shareModal.location}\nExperience: ${shareModal.experienceMin}-${shareModal.experienceMax} years\nSkills: ${shareModal.requiredSkills}\n\nApply here: ${url}`;
                    window.open(`mailto:?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`);
                  }}>
                  📧 Email
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Jobs;