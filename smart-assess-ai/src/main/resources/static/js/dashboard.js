/* Dashboard Logic for Faculty & Student Views */

async function loadFacultyDashboard() {
    try {
        const assessments = await apiFetch('/api/assessments');
        if (!assessments) return;

        const totalAssessments = assessments.length;
        const activeAssessments = assessments.filter(a => a.status === 'PUBLISHED').length;

        document.getElementById('total-assessments-val').innerText = totalAssessments;
        document.getElementById('active-assessments-val').innerText = activeAssessments;

        const tableBody = document.getElementById('recent-assessments-table');
        if (tableBody) {
            tableBody.innerHTML = '';
            if (assessments.length === 0) {
                tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--text-muted);">No assessments created yet. Click "Create Assessment" to start.</td></tr>`;
                return;
            }

            assessments.forEach(a => {
                const tr = document.createElement('tr');
                const fullShareUrl = a.shareToken ? `${window.location.origin}/test.html?code=${a.shareToken}` : null;

                tr.innerHTML = `
                    <td><strong>${a.title}</strong></td>
                    <td>${a.subject ? a.subject.name : '-'}</td>
                    <td>${a.durationMinutes} mins</td>
                    <td><span class="badge badge-${a.status.toLowerCase()}">${a.status}</span></td>
                    <td>
                        ${a.shareToken ? `
                            <div style="display:flex; gap:0.4rem; align-items:center; flex-wrap:wrap;">
                                <code style="color:var(--secondary); font-weight:bold;">${a.shareToken}</code>
                                <button class="btn btn-secondary" onclick="copyLink('${a.shareToken}')" style="padding:0.25rem 0.5rem; font-size:0.75rem;">📋 Copy Link</button>
                                <button class="btn btn-secondary" onclick="window.open('${fullShareUrl}', '_blank')" style="padding:0.25rem 0.5rem; font-size:0.75rem;">↗ Open</button>
                            </div>
                        ` : '-'}
                    </td>
                    <td>
                        ${a.status === 'DRAFT' ? `<button class="btn btn-primary" onclick="publishTest(${a.id})" style="padding:0.35rem 0.65rem; font-size:0.8rem;">Publish</button>` : ''}
                        <a href="analytics.html?id=${a.id}" class="btn btn-secondary" style="padding:0.35rem 0.65rem; font-size:0.8rem;">Analytics</a>
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        }
    } catch (e) {
        console.error(e);
    }
}

async function publishTest(id) {
    if (!confirm("Are you sure you want to publish this assessment? It will generate a shareable test link for students.")) return;
    try {
        const res = await apiFetch(`/api/assessments/${id}/publish`, { method: 'POST' });
        if (res && res.shareToken) {
            showToast("Assessment published successfully!", "success");
            loadFacultyDashboard();
        }
    } catch (e) {
        console.error(e);
    }
}

function copyLink(token) {
    let cleanToken = token || '';
    if (cleanToken.includes('/')) {
        cleanToken = cleanToken.substring(cleanToken.lastIndexOf('/') + 1);
    }
    if (cleanToken.includes('code=')) {
        cleanToken = cleanToken.split('code=')[1];
    }
    cleanToken = cleanToken.trim();

    const fullUrl = `${window.location.origin}/test.html?code=${cleanToken}`;
    navigator.clipboard.writeText(fullUrl);
    showToast("Test link copied successfully.", "success");
}

async function loadStudentDashboard() {
    try {
        const attempts = await apiFetch('/api/attempts/my-attempts');
        if (!attempts) return;

        const totalAttempts = attempts.length;
        const avgScore = totalAttempts > 0 
            ? Math.round(attempts.reduce((acc, a) => acc + (a.percentage || 0), 0) / totalAttempts) 
            : 0;

        document.getElementById('student-attempts-val').innerText = totalAttempts;
        document.getElementById('student-avg-val').innerText = `${avgScore}%`;

        const tableBody = document.getElementById('student-history-table');
        if (tableBody) {
            tableBody.innerHTML = '';
            if (attempts.length === 0) {
                tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--text-muted);">No test attempts yet. Use a test link from your faculty to start an assessment.</td></tr>`;
                return;
            }

            attempts.forEach(att => {
                const tr = document.createElement('tr');
                const dateStr = att.startedAt ? new Date(att.startedAt).toLocaleString() : '-';
                tr.innerHTML = `
                    <td><strong>${att.assessment ? att.assessment.title : 'Assessment'}</strong></td>
                    <td>${dateStr}</td>
                    <td>${att.score} / ${att.totalMarks}</td>
                    <td><strong>${att.percentage}%</strong></td>
                    <td>
                        <a href="result.html?attemptId=${att.id}" class="btn btn-secondary" style="padding:0.35rem 0.65rem; font-size:0.8rem;">View Result</a>
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        }
    } catch (e) {
        console.error(e);
    }
}