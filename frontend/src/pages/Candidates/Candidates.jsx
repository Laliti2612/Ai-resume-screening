import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './Candidates.css';

function Candidates() {
  const [candidates, setCandidates]     = useState([]);
  const [loading, setLoading]           = useState(true);
  const [search, setSearch]             = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selected, setSelected]         = useState(null);
  const [deleting, setDeleting]         = useState(null);
  const [message, setMessage]           = useState('');
  const [deleteModal, setDeleteModal]   = useState(null);

  const token = localStorage.getItem('jwt_token') || '';

  const getAuthHeader = () => ({
    headers: { Authorization: `Bearer ${token}` }
  });

  const fetchCandidates = () => {
    setLoading(true);
    axios.get('http://localhost:8080/api/candidates', getAuthHeader())
      .then(res => {
        const data =
          Array.isArray(res.data)      ? res.data :
          Array.isArray(res.data.data) ? res.data.data : [];
        setCandidates(data);
      })
      .catch(err => console.error('Fetch error:', err))
      .finally(() => setLoading(false));
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { fetchCandidates(); }, []);

  const handleStatusChange = async (id, newStatus) => {
    try {
      await axios.patch(
        `http://localhost:8080/api/candidates/${id}/status`,
        { status: newStatus },
        getAuthHeader()
      );
      setCandidates(prev => prev.map(c =>
        c.id === id ? { ...c, status: newStatus } : c));
      if (selected?.id === id)
        setSelected(prev => ({ ...prev, status: newStatus }));
    } catch (err) {
      console.error('Status update error:', err);
    }
  };

  const confirmDelete = (candidate, e) => {
    e.stopPropagation();
    setDeleteModal(candidate);
  };

  const handleDelete = async () => {
    if (!deleteModal) return;
    const id = deleteModal.id;
    setDeleting(id);
    setDeleteModal(null);
    try {
      await axios.delete(
        `http://localhost:8080/api/candidates/${id}`,
        getAuthHeader()
      );
      setCandidates(prev => prev.filter(c => c.id !== id));
      if (selected?.id === id) setSelected(null);
      setMessage(`✓ ${deleteModal.fullName || 'Candidate'} deleted successfully`);
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      console.error('Delete error:', err);
      setMessage('✗ Failed to delete candidate');
      setTimeout(() => setMessage(''), 3000);
    } finally {
      setDeleting(null);
    }
  };

  const getStatusStyle = (status) => {
    const map = {
      AUTO_SHORTLISTED:    { bg: '#f0fdf4', color: '#15803d', label: '⚡ Auto Shortlisted' },
      SHORTLISTED:         { bg: '#dcfce7', color: '#16a34a', label: '✓ Shortlisted' },
      UNDER_CONSIDERATION: { bg: '#eff6ff', color: '#2563eb', label: '~ Under Consideration' },
      REJECTED:            { bg: '#fff7ed', color: '#ea580c', label: '↓ Rejected' },
      AUTO_REJECTED:       { bg: '#fef2f2', color: '#dc2626', label: '✗ Auto Rejected' },
      HIRED:               { bg: '#fdf4ff', color: '#9333ea', label: '★ Hired' },
      NEW:                 { bg: '#eff6ff', color: '#2563eb', label: '~ Under Review' },
    };
    return map[status] || map['UNDER_CONSIDERATION'];
  };

  const getScoreColor = (score) => {
    if (score >= 85) return '#15803d';
    if (score >= 70) return '#16a34a';
    if (score >= 55) return '#2563eb';
    if (score >= 40) return '#ea580c';
    return '#dc2626';
  };

  const getScoreBand = (score) => {
    if (score >= 85) return 'Excellent';
    if (score >= 70) return 'Good';
    if (score >= 55) return 'Average';
    if (score >= 40) return 'Below Avg';
    return 'Poor';
  };

  const filtered = candidates.filter(c => {
    const matchSearch =
      (c.fullName || '').toLowerCase().includes(search.toLowerCase()) ||
      (c.email    || '').toLowerCase().includes(search.toLowerCase());
    const matchStatus =
      statusFilter === 'ALL' || c.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const stats = [
    { label: 'Total', count: candidates.length, color: '#3b82f6', filter: 'ALL' },
    { label: 'Auto Shortlisted', count: candidates.filter(c => c.status === 'AUTO_SHORTLISTED').length, color: '#15803d', filter: 'AUTO_SHORTLISTED' },
    { label: 'Shortlisted', count: candidates.filter(c => c.status === 'SHORTLISTED').length, color: '#16a34a', filter: 'SHORTLISTED' },
    { label: 'Consideration', count: candidates.filter(c => c.status === 'UNDER_CONSIDERATION' || c.status === 'NEW').length, color: '#2563eb', filter: 'UNDER_CONSIDERATION' },
    { label: 'Rejected', count: candidates.filter(c => c.status === 'REJECTED').length, color: '#ea580c', filter: 'REJECTED' },
    { label: 'Auto Rejected', count: candidates.filter(c => c.status === 'AUTO_REJECTED').length, color: '#dc2626', filter: 'AUTO_REJECTED' },
    { label: 'Hired', count: candidates.filter(c => c.status === 'HIRED').length, color: '#9333ea', filter: 'HIRED' },
  ];

  return (
    <div className="candidates-page">

      {message && (
        <div className={`message ${message.startsWith('✓') ? 'success' : 'error'}`}>
          {message}
        </div>
      )}

      <div className="toolbar">
        <input
          className="search-input"
          placeholder="🔍  Search by name or email..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <select
          className="filter-select"
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}>
          <option value="ALL">All Status</option>
          <option value="AUTO_SHORTLISTED">⚡ Auto Shortlisted</option>
          <option value="SHORTLISTED">✓ Shortlisted</option>
          <option value="UNDER_CONSIDERATION">~ Under Consideration</option>
          <option value="REJECTED">↓ Rejected</option>
          <option value="AUTO_REJECTED">✗ Auto Rejected</option>
          <option value="HIRED">★ Hired</option>
        </select>
      </div>

      <div className="stats-summary">
        {stats.map((s, i) => (
          <div
            key={i}
            className={`stat-card ${statusFilter === s.filter ? 'active' : ''}`}
            style={{ borderTop: `3px solid ${s.color}`, cursor: 'pointer' }}
            onClick={() => setStatusFilter(s.filter)}>
            <div className="stat-count" style={{ color: s.color }}>{s.count}</div>
            <div className="stat-label">{s.label}</div>
          </div>
        ))}
      </div>

      <div className="table-card">
        {loading ? (
          <div className="empty">🔄 Loading candidates...</div>
        ) : (
          <table className="candidates-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Candidate</th>
                <th>Email</th>
                <th>Experience</th>
                <th>Score</th>
                <th>Band</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan="8" className="empty">No candidates found</td>
                </tr>
              ) : (
                filtered.map((c, idx) => {
                  const st = getStatusStyle(c.status);
                  const sc = c.totalScore || 0;
                  return (
                    <tr key={c.id} className="clickable-row" onClick={() => setSelected(c)}>
                      <td style={{ color: '#94a3b8', fontSize: 13, fontWeight: 600 }}>{idx + 1}</td>
                      <td>
                        <div className="candidate-name-cell">
                          <div className="candidate-initial" style={{ background: getScoreColor(sc) }}>
                            {(c.fullName || 'U')[0].toUpperCase()}
                          </div>
                          <div className="name-cell">{c.fullName || 'Unknown'}</div>
                        </div>
                      </td>
                      <td className="email-cell">{c.email || '-'}</td>
                      <td>
                        <span style={{ fontSize: 13, color: '#475569', fontWeight: 500 }}>
                          {c.totalExperience != null ? c.totalExperience === 0 ? '🎓 Fresher' : `${c.totalExperience} yrs` : '-'}
                        </span>
                      </td>
                      <td>
                        <div className="score-badge" style={{ color: getScoreColor(sc), background: getScoreColor(sc) + '18' }}>
                          {sc}
                        </div>
                      </td>
                      <td>
                        <span style={{ fontSize: 11, fontWeight: 700, color: getScoreColor(sc), background: getScoreColor(sc) + '15', padding: '2px 8px', borderRadius: 8, whiteSpace: 'nowrap' }}>
                          {getScoreBand(sc)}
                        </span>
                      </td>
                      <td onClick={e => e.stopPropagation()}>
                        <select
                          className="status-select"
                          value={c.status || 'UNDER_CONSIDERATION'}
                          style={{ background: st.bg, color: st.color, borderColor: st.color + '50' }}
                          onChange={e => handleStatusChange(c.id, e.target.value)}>
                          <option value="AUTO_SHORTLISTED">⚡ Auto Shortlisted</option>
                          <option value="SHORTLISTED">✓ Shortlisted</option>
                          <option value="UNDER_CONSIDERATION">~ Under Consideration</option>
                          <option value="REJECTED">↓ Rejected</option>
                          <option value="AUTO_REJECTED">✗ Auto Rejected</option>
                          <option value="HIRED">★ Hired</option>
                        </select>
                      </td>
                      <td onClick={e => e.stopPropagation()}>
                        <button
                          className={`delete-btn ${deleting === c.id ? 'deleting' : ''}`}
                          onClick={e => confirmDelete(c, e)}
                          disabled={deleting === c.id}
                          title="Delete candidate">
                          🗑
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        )}
      </div>

      {deleteModal && (
        <div className="modal-overlay" onClick={() => setDeleteModal(null)}>
          <div className="delete-confirm-modal" onClick={e => e.stopPropagation()}>
            <div className="delete-modal-icon">🗑️</div>
            <h3 className="delete-modal-title">Delete Candidate</h3>
            <p className="delete-modal-message">
              Are you sure you want to delete
              <strong> {deleteModal.fullName || 'this candidate'} </strong>?
            </p>
            <p className="delete-modal-warning">
              ⚠️ This action cannot be undone.
            </p>
            <div className="delete-modal-actions">
              <button className="delete-cancel-btn" onClick={() => setDeleteModal(null)}>Cancel</button>
              <button className="delete-confirm-btn" onClick={handleDelete}>🗑 Yes, Delete</button>
            </div>
          </div>
        </div>
      )}

      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3>{selected.fullName || 'Unknown'}</h3>
                <p>{selected.email || ''}</p>
              </div>
              <button className="modal-close" onClick={() => setSelected(null)}>✕</button>
            </div>
            <div className="modal-body">
              <div className="score-overview">
                <div className="total-score-circle" style={{ borderColor: getScoreColor(selected.totalScore || 0), color: getScoreColor(selected.totalScore || 0) }}>
                  <div className="score-num">{selected.totalScore || 0}</div>
                  <div className="score-text">Score</div>
                </div>
                <div>
                  <div style={{ fontSize: 18, fontWeight: 800, color: getScoreColor(selected.totalScore || 0), marginBottom: 4 }}>
                    {getScoreBand(selected.totalScore || 0)}
                  </div>
                  <div style={{ fontSize: 13, color: '#64748b', marginBottom: 8 }}>AI Screening Result</div>
                  <span className="score-label-badge" style={{ background: getStatusStyle(selected.status).bg, color: getStatusStyle(selected.status).color }}>
                    {getStatusStyle(selected.status).label}
                  </span>
                </div>
              </div>
              <div className="info-grid">
                <div className="info-item">
                  <span className="info-label">Full Name</span>
                  <span className="info-value">{selected.fullName || '-'}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">Email</span>
                  <span className="info-value">{selected.email || '-'}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">Phone</span>
                  <span className="info-value">{selected.phone || '-'}</span>
                </div>
                <div className="info-item">
                  <span className="info-label">Experience</span>
                  <span className="info-value">
                    {selected.totalExperience === 0 ? '🎓 Fresher' : `${selected.totalExperience} years`}
                  </span>
                </div>
                <div className="info-item">
                  <span className="info-label">Score</span>
                  <span className="info-value" style={{ color: getScoreColor(selected.totalScore || 0), fontSize: 18, fontWeight: 800 }}>
                    {selected.totalScore || 0}
                    <span style={{ fontSize: 13, color: '#94a3b8', fontWeight: 500 }}> / 100</span>
                  </span>
                </div>
                <div className="info-item">
                  <span className="info-label">Band</span>
                  <span className="info-value" style={{ color: getScoreColor(selected.totalScore || 0), fontWeight: 700 }}>
                    {getScoreBand(selected.totalScore || 0)}
                  </span>
                </div>
              </div>
              <div className="modal-status">
                <span style={{ fontSize: 13, fontWeight: 600, color: '#64748b', whiteSpace: 'nowrap' }}>Update Status:</span>
                <select
                  className="status-select-modal"
                  value={selected.status || 'UNDER_CONSIDERATION'}
                  onChange={e => handleStatusChange(selected.id, e.target.value)}>
                  <option value="AUTO_SHORTLISTED">⚡ Auto Shortlisted</option>
                  <option value="SHORTLISTED">✓ Shortlisted</option>
                  <option value="UNDER_CONSIDERATION">~ Under Consideration</option>
                  <option value="REJECTED">↓ Rejected</option>
                  <option value="AUTO_REJECTED">✗ Auto Rejected</option>
                  <option value="HIRED">★ Hired</option>
                </select>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Candidates;