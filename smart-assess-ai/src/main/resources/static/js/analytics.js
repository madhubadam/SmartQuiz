/* Faculty Analytics & Chart.js Visualizations */

let chartInstances = {};

async function loadAnalyticsPage() {
    const urlParams = new URLSearchParams(window.location.search);
    const assessmentId = urlParams.get('id');

    if (!assessmentId) {
        // Load list of assessments into dropdown selector
        const assessments = await apiFetch('/api/assessments');
        if (assessments && assessments.length > 0) {
            renderAssessmentSelector(assessments);
            loadAssessmentAnalytics(assessments[0].id);
        } else {
            document.getElementById('analytics-content').innerHTML = `<p style="text-align:center; padding:3rem; color:var(--text-muted);">No assessments available to view analytics.</p>`;
        }
        return;
    }

    const assessments = await apiFetch('/api/assessments');
    if (assessments) renderAssessmentSelector(assessments, assessmentId);
    loadAssessmentAnalytics(assessmentId);
}

function renderAssessmentSelector(assessments, selectedId = null) {
    const selectEl = document.getElementById('assessment-selector');
    if (!selectEl) return;
    selectEl.innerHTML = '';

    assessments.forEach(a => {
        const opt = document.createElement('option');
        opt.value = a.id;
        opt.innerText = `${a.title} (${a.status})`;
        if (selectedId && String(a.id) === String(selectedId)) opt.selected = true;
        selectEl.appendChild(opt);
    });

    selectEl.onchange = (e) => {
        const id = e.target.value;
        window.history.pushState({}, '', `analytics.html?id=${id}`);
        loadAssessmentAnalytics(id);
    };
}

async function loadAssessmentAnalytics(assessmentId) {
    try {
        const data = await apiFetch(`/api/analytics/assessment/${assessmentId}`);
        if (!data) return;

        // Render Overview Stats Cards
        document.getElementById('total-students-attempted').innerText = data.totalStudentsAttempted;
        document.getElementById('total-attempts').innerText = data.totalAttempts;
        document.getElementById('average-score').innerText = `${data.averageScore}`;
        document.getElementById('pass-percentage').innerText = `${data.passPercentage}%`;

        // Render AI Performance Insights Block
        const aiBox = document.getElementById('ai-insights-box');
        if (aiBox) {
            aiBox.innerHTML = `
                <div style="display:flex; align-items:center; gap:0.75rem; margin-bottom:0.75rem;">
                    <div style="width:32px; height:32px; border-radius:50%; background:linear-gradient(135deg, var(--secondary), var(--accent)); display:flex; align-items:center; justify-content:center; color:white; font-weight:bold;">✨</div>
                    <h3 style="color:var(--text-primary); font-size:1.1rem;">AI Class Performance Insights</h3>
                </div>
                <p style="color:var(--text-secondary); line-height:1.6; font-size:0.95rem; white-space:pre-line;">${data.aiInsights || 'Generating insights...'}</p>
            `;
        }

        // Render Student Performance Table
        const studentTable = document.getElementById('student-performance-table');
        if (studentTable) {
            studentTable.innerHTML = '';
            if (data.studentPerformances.length === 0) {
                studentTable.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No student attempts submitted yet.</td></tr>`;
            } else {
                data.studentPerformances.forEach(sp => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td><strong>${sp.studentName}</strong></td>
                        <td>${sp.email}</td>
                        <td>${sp.score}</td>
                        <td><strong>${sp.percentage}%</strong></td>
                        <td>${sp.attemptsCount}</td>
                        <td><span class="badge badge-${sp.status === 'PASSED' ? 'published' : 'closed'}">${sp.status}</span></td>
                    `;
                    studentTable.appendChild(tr);
                });
            }
        }

        // Render Charts using Chart.js
        renderCharts(data);

    } catch (e) {
        console.error(e);
    }
}

function renderCharts(data) {
    if (typeof Chart === 'undefined') return;

    // Destroy existing charts to prevent canvas re-use errors
    if (chartInstances.scores) chartInstances.scores.destroy();
    if (chartInstances.doughnut) chartInstances.doughnut.destroy();
    if (chartInstances.topics) chartInstances.topics.destroy();

    // 1. Student Scores Bar Chart
    const ctxScores = document.getElementById('chart-scores');
    if (ctxScores) {
        const studentNames = data.studentPerformances.map(s => s.studentName);
        const studentScores = data.studentPerformances.map(s => s.score);

        chartInstances.scores = new Chart(ctxScores, {
            type: 'bar',
            data: {
                labels: studentNames.length > 0 ? studentNames : ['Sample Student'],
                datasets: [{
                    label: 'Student Scores',
                    data: studentScores.length > 0 ? studentScores : [0],
                    backgroundColor: '#4f46e5',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: '#334155' }, ticks: { color: '#94a3b8' } },
                    x: { grid: { display: false }, ticks: { color: '#94a3b8' } }
                }
            }
        });
    }

    // 2. Correct vs Incorrect Doughnut Chart
    const ctxDoughnut = document.getElementById('chart-doughnut');
    if (ctxDoughnut) {
        let totalCorrect = 0;
        let totalWrong = 0;
        data.questionPerformances.forEach(q => {
            totalCorrect += q.correctPercentage;
            totalWrong += q.wrongPercentage;
        });

        chartInstances.doughnut = new Chart(ctxDoughnut, {
            type: 'doughnut',
            data: {
                labels: ['Correct Attempts', 'Incorrect Attempts'],
                datasets: [{
                    data: [totalCorrect || 75, totalWrong || 25],
                    backgroundColor: ['#10b981', '#ef4444'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { position: 'bottom', labels: { color: '#94a3b8' } } }
            }
        });
    }

    // 3. Topic Performance Horizontal Bar Chart
    const ctxTopics = document.getElementById('chart-topics');
    if (ctxTopics) {
        const topicNames = data.topicPerformances.map(t => t.topicName);
        const topicPcts = data.topicPerformances.map(t => t.averagePercentage);

        chartInstances.topics = new Chart(ctxTopics, {
            type: 'bar',
            data: {
                labels: topicNames.length > 0 ? topicNames : ['OLAP Operations', 'ETL Processes', 'Data Cubes'],
                datasets: [{
                    label: 'Topic Accuracy %',
                    data: topicPcts.length > 0 ? topicPcts : [86, 74, 62],
                    backgroundColor: '#06b6d4',
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                plugins: { legend: { display: false } },
                scales: {
                    x: { beginAtZero: true, max: 100, grid: { color: '#334155' }, ticks: { color: '#94a3b8' } },
                    y: { grid: { display: false }, ticks: { color: '#94a3b8' } }
                }
            }
        });
    }
}
