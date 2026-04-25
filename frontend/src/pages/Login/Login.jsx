import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import "./Login.css";

function Login() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState("signin");
  const [loading, setLoading]     = useState(false);
  const [error, setError]         = useState("");
  const [success, setSuccess]     = useState("");

  const [forgotStep, setForgotStep]   = useState('forgot');
  const [forgotEmail, setForgotEmail] = useState('');
  const [resetForm, setResetForm]     = useState({
    newPassword: '', confirmPassword: ''
  });
  const [showNewPwd, setShowNewPwd] = useState(false);
  const [showConPwd, setShowConPwd] = useState(false);
  const [showPwd, setShowPwd]       = useState(false);

  const [signInForm, setSignInForm] = useState({
    email: "", password: ""
  });
  const [signUpForm, setSignUpForm] = useState({
    firstName: "", lastName: "",
    email: "", password: "", confirmPassword: ""
  });

  const handleSignIn = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const res = await axios.post(
        "http://localhost:8080/api/auth/login",
        { username: signInForm.email,
          password: signInForm.password }
      );
      if (res.data.success) {
        localStorage.setItem('jwt_token', res.data.token);
        localStorage.setItem('user_email', signInForm.email);
        localStorage.setItem('user_name',
          res.data.user?.name || 'HR Manager');
        localStorage.setItem('user_role',
          res.data.user?.role || 'HR');
        navigate("/dashboard");
      } else {
        setError("Invalid email or password.");
      }
    } catch {
      setError(
        "Login failed. Make sure backend is running.");
    }
    setLoading(false);
  };

  const handleSignUp = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    if (signUpForm.password !== signUpForm.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    if (signUpForm.password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }
    setLoading(true);
    try {
      const res = await axios.post(
        "http://localhost:8080/api/auth/register",
        {
          name: signUpForm.firstName + " " +
                signUpForm.lastName,
          email:    signUpForm.email,
          password: signUpForm.password
        }
      );
      if (res.data.success) {
        setSuccess(
          "Account created! Please sign in.");
        setActiveTab("signin");
        setSignInForm({
          email: signUpForm.email, password: ""
        });
        setSignUpForm({
          firstName: "", lastName: "",
          email: "", password: "", confirmPassword: ""
        });
      } else {
        setError(res.data.message ||
          "Registration failed.");
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Registration failed.");
    }
    setLoading(false);
  };

  const handleForgotSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    if (!forgotEmail.trim()) {
      setError("Please enter your email.");
      return;
    }
    setLoading(true);
    try {
      const res = await axios.post(
        "http://localhost:8080/api/auth/forgot-password",
        { email: forgotEmail.trim() }
      );
      if (res.data.success) {
        setForgotStep('reset');
        setSuccess("Email verified! Set new password.");
        setError("");
      } else {
        setError(res.data.message ||
          "No account found.");
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "No account found with this email.");
    }
    setLoading(false);
  };

  const handleResetSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    if (resetForm.newPassword.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }
    if (resetForm.newPassword !== resetForm.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    setLoading(true);
    try {
      const res = await axios.post(
        "http://localhost:8080/api/auth/reset-password",
        {
          email:       forgotEmail.trim(),
          newPassword: resetForm.newPassword
        }
      );
      if (res.data.success) {
        setSuccess("Password reset! Please sign in.");
        setActiveTab("signin");
        setForgotStep('forgot');
        setForgotEmail('');
        setResetForm({ newPassword: '', confirmPassword: '' });
        setSignInForm({
          email: forgotEmail.trim(), password: ''
        });
      } else {
        setError(res.data.message || "Reset failed.");
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Reset failed.");
    }
    setLoading(false);
  };

  const switchTab = (tab) => {
    setActiveTab(tab);
    setForgotStep('forgot');
    setForgotEmail('');
    setResetForm({ newPassword: '', confirmPassword: '' });
    setError('');
    setSuccess('');
  };

  const goBack = () => {
    setActiveTab('signin');
    setForgotStep('forgot');
    setForgotEmail('');
    setResetForm({ newPassword: '', confirmPassword: '' });
    setError('');
    setSuccess('');
  };

  const getPwdStrength = (pwd) => {
    if (!pwd) return null;
    if (pwd.length >= 10)
      return { w: '100%', c: '#16a34a', l: '✅ Strong' };
    if (pwd.length >= 8)
      return { w: '66%',  c: '#f59e0b', l: '⚠️ Medium' };
    if (pwd.length >= 6)
      return { w: '33%',  c: '#ef4444', l: '❌ Weak' };
    return   { w: '10%',  c: '#ef4444', l: '❌ Too short' };
  };

  const strength = getPwdStrength(resetForm.newPassword);

  const features = [
    { icon: '📄', bg: 'rgba(59,130,246,0.18)',
      title: 'Instant Resume Parsing',
      desc: 'PDF and DOC files processed in under 3 seconds' },
    { icon: '🧠', bg: 'rgba(6,182,212,0.18)',
      title: 'AI Skill Extraction',
      desc: '95%+ accuracy using Groq LLaMA AI models' },
    { icon: '📊', bg: 'rgba(34,197,94,0.18)',
      title: 'Smart Scoring System',
      desc: 'Weighted scoring — skills, experience & education' },
    { icon: '⚡', bg: 'rgba(168,85,247,0.18)',
      title: 'Instant Shortlisting',
      desc: 'Auto shortlist top candidates for any job' },
  ];

  return (
    <div className="login-page">

      {/* ── Left Panel ── */}
      <div className="login-left">
        <div className="brand">
          <div className="brand-icon">🤖</div>
          <span className="brand-name">AI Recruit</span>
        </div>

        <div className="left-content">
          <h1 className="left-title">
            Smart Hiring<br />
            Powered by <span>AI</span>
          </h1>
          <p className="left-subtitle">
            Automate resume screening, reduce hiring bias,
            and find the best candidates faster with our
            AI-powered platform.
          </p>

          <div className="features-list">
            {features.map((f, i) => (
              <div key={i} className="feature-item">
                <div className="feature-icon"
                     style={{ background: f.bg }}>
                  {f.icon}
                </div>
                <div className="feature-text">
                  <h4>{f.title}</h4>
                  <p>{f.desc}</p>
                </div>
              </div>
            ))}
          </div>

          <div className="stats-row">
            {[
              { num: '10K+', label: 'Resumes Processed' },
              { num: '95%',  label: 'AI Accuracy' },
              { num: '3s',   label: 'Avg Parse Time' },
            ].map((s, i) => (
              <div key={i} className="stat-item">
                <div className="stat-num">{s.num}</div>
                <div className="stat-label">{s.label}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* ── Right Panel ── */}
      <div className="login-right">
        <div className="login-box">

          <div className="brand" style={{
            marginBottom: 20,
            justifyContent: 'center'
          }}>
            <div className="brand-icon">🤖</div>
            <span className="brand-name"
                  style={{ color: '#1e293b' }}>
              AI Recruit
            </span>
          </div>

          {/* ── Forgot Password ── */}
          {activeTab === 'forgot' ? (
            <>
              <button className="back-btn" onClick={goBack}>
                ← Back to Sign In
              </button>
              <h2 className="login-box-title"
                  style={{ marginTop: 10 }}>
                {forgotStep === 'forgot'
                  ? '🔐 Forgot Password'
                  : '🔑 Set New Password'}
              </h2>
              <p className="login-box-sub">
                {forgotStep === 'forgot'
                  ? 'Enter your registered email to continue'
                  : `New password for ${forgotEmail}`}
              </p>

              {error &&
                <div className="login-error">{error}</div>}
              {success &&
                <div className="login-success">{success}</div>}

              {forgotStep === 'forgot' && (
                <form onSubmit={handleForgotSubmit}
                      className="auth-form">
                  <div className="form-group">
                    <label>Registered Email</label>
                    <input
                      type="email"
                      placeholder="you@example.com"
                      value={forgotEmail}
                      onChange={e =>
                        setForgotEmail(e.target.value)}
                      required
                    />
                  </div>
                  <button type="submit"
                          className="submit-btn"
                          disabled={loading}>
                    {loading
                      ? 'Verifying...'
                      : 'Verify Email →'}
                  </button>
                </form>
              )}

              {forgotStep === 'reset' && (
                <form onSubmit={handleResetSubmit}
                      className="auth-form">
                  <div className="form-group">
                    <label>New Password</label>
                    <div className="pwd-wrapper">
                      <input
                        type={showNewPwd
                          ? 'text' : 'password'}
                        placeholder="Min 6 characters"
                        value={resetForm.newPassword}
                        onChange={e => setResetForm({
                          ...resetForm,
                          newPassword: e.target.value
                        })}
                        required
                      />
                      <button type="button"
                              className="pwd-toggle"
                              onClick={() =>
                                setShowNewPwd(p => !p)}>
                        {showNewPwd ? '🙈' : '👁️'}
                      </button>
                    </div>
                  </div>

                  {strength && (
                    <div className="pwd-strength">
                      <div className="pwd-strength-bar">
                        <div className="pwd-strength-fill"
                          style={{
                            width:      strength.w,
                            background: strength.c
                          }}
                        />
                      </div>
                      <span className="pwd-strength-label">
                        {strength.l}
                      </span>
                    </div>
                  )}

                  <div className="form-group">
                    <label>Confirm Password</label>
                    <div className="pwd-wrapper">
                      <input
                        type={showConPwd
                          ? 'text' : 'password'}
                        placeholder="Repeat new password"
                        value={resetForm.confirmPassword}
                        onChange={e => setResetForm({
                          ...resetForm,
                          confirmPassword: e.target.value
                        })}
                        required
                      />
                      <button type="button"
                              className="pwd-toggle"
                              onClick={() =>
                                setShowConPwd(p => !p)}>
                        {showConPwd ? '🙈' : '👁️'}
                      </button>
                    </div>
                  </div>

                  {resetForm.confirmPassword && (
                    <div style={{
                      fontSize: 12, fontWeight: 600,
                      color: resetForm.newPassword ===
                             resetForm.confirmPassword
                             ? '#16a34a' : '#ef4444'
                    }}>
                      {resetForm.newPassword ===
                       resetForm.confirmPassword
                        ? '✅ Passwords match'
                        : '❌ Passwords do not match'}
                    </div>
                  )}

                  <button type="submit"
                          className="submit-btn"
                          disabled={loading}>
                    {loading
                      ? 'Resetting...'
                      : 'Reset Password →'}
                  </button>
                </form>
              )}
            </>

          ) : (
            <>
              <h2 className="login-box-title">
                {activeTab === 'signin'
                  ? 'Welcome back 👋'
                  : 'Create account 🚀'}
              </h2>
              <p className="login-box-sub">
                {activeTab === 'signin'
                  ? 'Sign in to your HR dashboard'
                  : 'Start screening resumes with AI'}
              </p>

              <div className="auth-tabs">
                <button
                  className={`auth-tab ${
                    activeTab === 'signin' ? 'active' : ''
                  }`}
                  onClick={() => switchTab('signin')}>
                  Sign In
                </button>
                <button
                  className={`auth-tab ${
                    activeTab === 'signup' ? 'active' : ''
                  }`}
                  onClick={() => switchTab('signup')}>
                  Sign Up
                </button>
              </div>

              {error &&
                <div className="login-error">{error}</div>}
              {success &&
                <div className="login-success">{success}</div>}

              {activeTab === 'signin' && (
                <form onSubmit={handleSignIn}
                      className="auth-form">
                  <div className="form-group">
                    <label>Email Address</label>
                    <input
                      type="email"
                      placeholder="you@example.com"
                      value={signInForm.email}
                      onChange={e => setSignInForm({
                        ...signInForm,
                        email: e.target.value
                      })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Password</label>
                    <div className="pwd-wrapper">
                      <input
                        type={showPwd ? 'text' : 'password'}
                        placeholder="Enter your password"
                        value={signInForm.password}
                        onChange={e => setSignInForm({
                          ...signInForm,
                          password: e.target.value
                        })}
                        required
                      />
                      <button type="button"
                              className="pwd-toggle"
                              onClick={() =>
                                setShowPwd(p => !p)}>
                        {showPwd ? '🙈' : '👁️'}
                      </button>
                    </div>
                  </div>
                  <div className="forgot-row">
                    <span className="forgot-link"
                      onClick={() => {
                        setActiveTab('forgot');
                        setError('');
                        setSuccess('');
                      }}>
                      Forgot password?
                    </span>
                  </div>
                  <button type="submit"
                          className="submit-btn"
                          disabled={loading}>
                    {loading ? 'Signing in...' : 'Sign In →'}
                  </button>
                </form>
              )}

              {activeTab === 'signup' && (
                <form onSubmit={handleSignUp}
                      className="auth-form">
                  <div className="form-row">
                    <div className="form-group">
                      <label>First Name</label>
                      <input
                        type="text"
                        placeholder="John"
                        value={signUpForm.firstName}
                        onChange={e => setSignUpForm({
                          ...signUpForm,
                          firstName: e.target.value
                        })}
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label>Last Name</label>
                      <input
                        type="text"
                        placeholder="Doe"
                        value={signUpForm.lastName}
                        onChange={e => setSignUpForm({
                          ...signUpForm,
                          lastName: e.target.value
                        })}
                        required
                      />
                    </div>
                  </div>
                  <div className="form-group">
                    <label>Email Address</label>
                    <input
                      type="email"
                      placeholder="you@example.com"
                      value={signUpForm.email}
                      onChange={e => setSignUpForm({
                        ...signUpForm,
                        email: e.target.value
                      })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Password</label>
                    <input
                      type="password"
                      placeholder="Min 6 characters"
                      value={signUpForm.password}
                      onChange={e => setSignUpForm({
                        ...signUpForm,
                        password: e.target.value
                      })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Confirm Password</label>
                    <input
                      type="password"
                      placeholder="Repeat password"
                      value={signUpForm.confirmPassword}
                      onChange={e => setSignUpForm({
                        ...signUpForm,
                        confirmPassword: e.target.value
                      })}
                      required
                    />
                  </div>
                  <button type="submit"
                          className="submit-btn"
                          disabled={loading}>
                    {loading
                      ? 'Creating account...'
                      : 'Create Account →'}
                  </button>
                </form>
              )}
            </>
          )}

          <div className="login-footer">
            AI-Based Resume Screening System v1.0
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;