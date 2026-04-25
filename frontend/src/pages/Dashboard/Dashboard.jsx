import React, { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  PieChart, Pie, Cell, Legend, ResponsiveContainer } from 'recharts';
import axios from 'axios';
import './Dashboard.css';

const COLORS = ['#22c55e', '#3b82f6', '#ef4444', '#f97316'];

const Dashboard = () => {
  const [stats, setStats] = useState({
    total: 0, shortlisted: 0, rejected: 0,
    newCount: 0, excellent: 0, good: 0, average: 0, poor: 0
  });

  useEffect(() => {
    axios.get('http://localhost:8080/api/dashboard/stats', {
      headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
    })
    .then(res => setStats(res.data.data))
    .catch(err => console.log(err));
  }, []);

  const scoreData = [
    { name: 'Excellent', value: stats.excellent },
    { name: 'Good', value: stats.good },
    { name: 'Average', value: stats.average },
    { name: 'Poor', value: stats.poor },
  ];

  const statusData = [
    { name: 'Shortlisted', value: stats.shortlisted || 1 },
    { name: 'New', value: stats.newCount || 1 },
    { name: 'Rejected', value: stats.rejected || 1 },
  ];

  const kpiCards = [
    { label: 'Total Candidates', value: stats.total, color: '#3b82f6', bg: '#eff6ff' },
    { label: 'Shortlisted', value: stats.shortlisted, color: '#22c55e', bg: '#f0fdf4' },
    { label: 'Rejected', value: stats.rejected, color: '#ef4444', bg: '#fef2f2' },
    { label: 'New', value: stats.newCount, color: '#f97316', bg: '#fff7ed' },
  ];

  return (
    <div className="dashboard">
      <div className="page-header">
        <h2>Dashboard</h2>
        <p>Welcome back! Here is your hiring overview.</p>
      </div>

      <div className="kpi-row">
        {kpiCards.map(card => (
          <div key={card.label} className="kpi-card"
               style={{ background: card.bg, borderLeft: `4px solid ${card.color}` }}>
            <div className="kpi-value" style={{ color: card.color }}>{card.value}</div>
            <div className="kpi-label">{card.label}</div>
          </div>
        ))}
      </div>

      <div className="charts-row">
        <div className="chart-card">
          <h3>Score Distribution</h3>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={scoreData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="name" tick={{ fontSize: 13 }} />
              <YAxis tick={{ fontSize: 13 }} />
              <Tooltip />
              <Bar dataKey="value" fill="#3b82f6" radius={[4, 4, 0, 0]} name="Candidates" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h3>Candidate Status</h3>
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie data={statusData} cx="50%" cy="50%"
                innerRadius={70} outerRadius={110}
                dataKey="value" paddingAngle={3}>
                {statusData.map((entry, index) => (
                  <Cell key={index} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;