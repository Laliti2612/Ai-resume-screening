import React, { useState, useRef, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import './Layout.css';

function Layout({ children }) {
  const location = useLocation();
  const navigate  = useNavigate();

  const [showProfile, setShowProfile]    = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [profilePhoto, setProfilePhoto]  = useState(
    localStorage.getItem('profile_photo') || null
  );
  const [editForm, setEditForm] = useState({
    name:  localStorage.getItem('user_name')  || 'HR Manager',
    email: localStorage.getItem('user_email') || '',
  });

  const userName  = localStorage.getItem('user_name')
                 || 'HR Manager';
  const userEmail = localStorage.getItem('user_email') || '';
  const userRole  = localStorage.getItem('user_role')
                 || 'Administrator';

  const dropdownRef = useRef();
  const photoRef    = useRef();

  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current &&
          !dropdownRef.current.contains(e.target)) {
        setShowProfile(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () =>
      document.removeEventListener('mousedown', handler);
  }, []);

  const getInitials = (name) => {
    if (!name) return 'HR';
    const parts = name.trim().split(' ');
    if (parts.length >= 2)
      return (parts[0][0] + parts[1][0]).toUpperCase();
    return name.substring(0, 2).toUpperCase();
  };

  const handlePhotoChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
      alert('Photo must be under 2MB');
      return;
    }
    const reader = new FileReader();
    reader.onload = (ev) => {
      const base64 = ev.target.result;
      setProfilePhoto(base64);
      localStorage.setItem('profile_photo', base64);
    };
    reader.readAsDataURL(file);
  };

  const handleSaveProfile = () => {
    if (!editForm.name.trim()) {
      alert('Name is required');
      return;
    }
    localStorage.setItem('user_name', editForm.name.trim());
    localStorage.setItem('user_email', editForm.email.trim());
    setShowEditModal(false);
    setShowProfile(false);
    window.location.reload();
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const triggerPhotoUpload = () => {
    if (photoRef.current) photoRef.current.click();
  };

  const links = [
    { path: '/dashboard',  label: 'Dashboard',    icon: '📊' },
    { path: '/upload',     label: 'Upload Resume', icon: '📤' },
    { path: '/candidates', label: 'Candidates',    icon: '👥' },
    { path: '/jobs',       label: 'Jobs',          icon: '💼' },
  ];

  const currentPage = links.find(
    l => l.path === location.pathname)?.label
    || 'AI Resume Screening';

  const Avatar = ({ size = 36 }) => (
    profilePhoto ? (
      <img
        src={profilePhoto}
        alt="profile"
        style={{
          width: size, height: size,
          borderRadius: '50%',
          objectFit: 'cover'
        }}
      />
    ) : (
      <span style={{
        fontSize: size * 0.36,
        fontWeight: 700
      }}>
        {getInitials(userName)}
      </span>
    )
  );

  return (
    <div className="layout">

      {/* ── Hidden file input ── */}
      <input
        ref={photoRef}
        type="file"
        accept="image/*"
        style={{ display: 'none' }}
        onChange={handlePhotoChange}
      />

      {/* ── Sidebar ── */}
      <div className="sidebar">
        <div className="sidebar-logo">
          <span className="logo-icon">🤖</span>
          <span>AI Recruit</span>
        </div>

        <nav>
          {links.map(link => (
            <Link
              key={link.path}
              to={link.path}
              className={
                location.pathname === link.path
                  ? 'nav-link active'
                  : 'nav-link'
              }>
              <span className="nav-icon">{link.icon}</span>
              <span>{link.label}</span>
            </Link>
          ))}
        </nav>

        {/* ── Profile Footer ── */}
        <div className="sidebar-footer" ref={dropdownRef}>

          {/* Dropdown */}
          {showProfile && (
            <div className="profile-dropdown">
              <div className="profile-dropdown-header">
                <div
                  className="profile-dropdown-avatar"
                  onClick={triggerPhotoUpload}
                  title="Click to change photo">
                  <Avatar size={44} />
                  <div className="photo-overlay">📷</div>
                </div>
                <div className="profile-dropdown-info">
                  <div className="profile-dropdown-name">
                    {userName}
                  </div>
                  <div className="profile-dropdown-email">
                    {userEmail}
                  </div>
                  <div className="profile-dropdown-role">
                    {userRole}
                  </div>
                </div>
              </div>

              <div className="profile-dropdown-divider" />

              <button
                className="profile-menu-item"
                onClick={() => {
                  setEditForm({
                    name: localStorage.getItem('user_name')
                          || '',
                    email: localStorage.getItem('user_email')
                           || '',
                  });
                  setShowEditModal(true);
                  setShowProfile(false);
                }}>
                ✏️ &nbsp;Edit Profile
              </button>

              <button
                className="profile-menu-item"
                onClick={triggerPhotoUpload}>
                📷 &nbsp;Change Photo
              </button>

              <div className="profile-dropdown-divider" />

              <button
                className="profile-menu-item logout"
                onClick={handleLogout}>
                🚪 &nbsp;Sign Out
              </button>
            </div>
          )}

          {/* User Row */}
          <div
            className="user-info"
            onClick={() => setShowProfile(p => !p)}>
            <div className="user-avatar">
              <Avatar size={36} />
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div className="user-name">
                {userName.length > 14
                  ? userName.substring(0, 14) + '…'
                  : userName}
              </div>
              <div className="user-role">{userRole}</div>
            </div>
            <span style={{
              color: 'rgba(255,255,255,0.35)',
              fontSize: 9,
              transform: showProfile
                ? 'rotate(180deg)' : 'rotate(0deg)',
              transition: 'transform 0.2s',
              display: 'inline-block'
            }}>▲</span>
          </div>
        </div>
      </div>

      {/* ── Main Content ── */}
      <div className="main-content">

        {/* ── Top Bar — NO profile here ── */}
        <div className="top-bar">
          <div className="top-bar-title">
            {currentPage}
          </div>
          <div className="top-bar-right">
            <span className="online-dot" />
            <span style={{
              fontSize: 12, color: '#64748b'
            }}>
              Online
            </span>
          </div>
        </div>

        <div className="page-content">
          {children}
        </div>
      </div>

      {/* ── Edit Profile Modal ── */}
      {showEditModal && (
        <div
          className="modal-overlay"
          onClick={() => setShowEditModal(false)}>
          <div
            className="edit-modal"
            onClick={e => e.stopPropagation()}>

            <div className="edit-modal-header">
              <h3>Edit Profile</h3>
              <button
                className="modal-close-btn"
                onClick={() => setShowEditModal(false)}>
                ✕
              </button>
            </div>

            <div className="edit-modal-photo">
              <div
                className="edit-photo-circle"
                onClick={triggerPhotoUpload}
                title="Click to change photo">
                {profilePhoto ? (
                  <img
                    src={profilePhoto}
                    alt="profile"
                    style={{
                      width: '100%', height: '100%',
                      borderRadius: '50%',
                      objectFit: 'cover'
                    }}
                  />
                ) : (
                  <span style={{
                    fontSize: 28, fontWeight: 700,
                    color: 'white'
                  }}>
                    {getInitials(editForm.name)}
                  </span>
                )}
                <div className="edit-photo-overlay">
                  📷 Change
                </div>
              </div>
              <p style={{
                fontSize: 12,
                color: '#64748b',
                marginTop: 8
              }}>
                Click to upload photo (max 2MB)
              </p>
            </div>

            <div className="edit-modal-body">
              <div className="edit-field">
                <label>Full Name</label>
                <input
                  type="text"
                  value={editForm.name}
                  onChange={e => setEditForm({
                    ...editForm, name: e.target.value
                  })}
                  placeholder="Enter your name"
                />
              </div>
              <div className="edit-field">
                <label>Email Address</label>
                <input
                  type="email"
                  value={editForm.email}
                  onChange={e => setEditForm({
                    ...editForm, email: e.target.value
                  })}
                  placeholder="Enter your email"
                />
              </div>
            </div>

            <div className="edit-modal-footer">
              <button
                className="cancel-btn"
                onClick={() => setShowEditModal(false)}>
                Cancel
              </button>
              <button
                className="save-btn"
                onClick={handleSaveProfile}>
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Layout;