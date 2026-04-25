import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Layout from "./components/Layout/Layout";
import Upload from "./components/Upload/Upload";
import Login from "./pages/Login/Login";
import Dashboard from "./pages/Dashboard/Dashboard";
import Candidates from "./pages/Candidates/Candidates";
import Jobs from "./pages/Jobs/Jobs";
import PublicJobPage from "./pages/Jobs/PublicJobPage";

function App() {
  return (
    <Router>
      <Routes>

        {/* ── Public Routes (no login needed) ── */}
        <Route path="/" element={<Login />} />
        <Route path="/jobs/public/:publicId" element={<PublicJobPage />} />

        {/* ── Protected Routes (login needed) ── */}
        <Route path="/dashboard" element={
          <Layout><Dashboard /></Layout>
        } />

        <Route path="/upload" element={
          <Layout><Upload /></Layout>
        } />

        <Route path="/candidates" element={
          <Layout><Candidates /></Layout>
        } />

        <Route path="/jobs" element={
          <Layout><Jobs /></Layout>
        } />

      </Routes>
    </Router>
  );
}

export default App;