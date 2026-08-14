/* Test Execution & Timer Engine */

let testState = {
    attemptId: null,
    questions: [],
    currentIndex: 0,
    answers: {}, // questionId -> selectedOption
    remainingSeconds: 0,
    timerInterval: null
};

async function initTestPage() {
    const urlParams = new URLSearchParams(window.location.search);
    let code = urlParams.get('code');

    if (!code) {
        showToast('Invalid or missing test share link code', 'error');
        return;
    }

    // Sanitize share token if full URL was passed as code parameter
    if (code.includes('/')) {
        code = code.substring(code.lastIndexOf('/') + 1);
    }
    code = code.trim();

    try {
        const testInfo = await apiFetch(`/api/test/${code}`);
        if (!testInfo) return;

        document.getElementById('test-title').innerText = testInfo.title;
        document.getElementById('test-subject').innerText = testInfo.subjectName;
        document.getElementById('test-faculty').innerText = `Faculty: ${testInfo.facultyName}`;
        document.getElementById('test-duration').innerText = `${testInfo.durationMinutes} Minutes`;
        document.getElementById('test-count').innerText = `${testInfo.questionCount} Questions`;
        document.getElementById('test-marks').innerText = `${testInfo.totalMarks} Total Marks`;
        document.getElementById('test-description').innerText = testInfo.description || 'Answer all questions within the allocated time limit.';

        // Display configured duration on header timer initially
        const timerEl = document.getElementById('timer-display');
        if (timerEl) {
            timerEl.innerText = `${String(testInfo.durationMinutes).padStart(2, '0')}:00`;
        }

        document.getElementById('start-test-btn').onclick = () => startTestSession(testInfo.id);

    } catch (e) {
        document.getElementById('test-prestart-card').innerHTML = `<h3 style="color:var(--danger)">Unable to load test</h3><p>${e.message}</p>`;
    }
}

async function startTestSession(assessmentId) {
    const user = getCurrentUser();
    if (!user) {
        showToast('Please log in as a student to take this assessment', 'warning');
        setTimeout(() => window.location.href = `index.html?redirect_code=${new URLSearchParams(window.location.search).get('code')}`, 1500);
        return;
    }

    try {
        showLoading('#start-test-btn');
        const res = await apiFetch('/api/attempts/start', {
            method: 'POST',
            body: JSON.stringify({ assessmentId })
        });

        if (res) {
            testState.attemptId = res.attemptId;
            testState.questions = res.questions;
            testState.remainingSeconds = res.durationSeconds;
            testState.currentIndex = 0;
            testState.answers = {};

            document.getElementById('test-prestart-card').style.display = 'none';
            document.getElementById('test-active-view').style.display = 'block';

            setupAntiCheat();
            startTimer();
            renderQuestionNavGrid();
            renderCurrentQuestion();
        }
    } catch (e) {
        hideLoading('#start-test-btn');
    }
}

function startTimer() {
    updateTimerDisplay();
    testState.timerInterval = setInterval(() => {
        testState.remainingSeconds--;
        updateTimerDisplay();

        if (testState.remainingSeconds === 300) {
            showToast('⚠️ 5 Minutes Remaining!', 'warning');
        } else if (testState.remainingSeconds === 60) {
            showToast('🚨 1 Minute Remaining! Test will auto-submit shortly.', 'error');
        }

        if (testState.remainingSeconds <= 0) {
            clearInterval(testState.timerInterval);
            showToast('Time expired! Auto-submitting assessment...', 'warning');
            submitTestAnswers(true);
        }
    }, 1000);
}

function updateTimerDisplay() {
    const minutes = Math.floor(testState.remainingSeconds / 60);
    const seconds = testState.remainingSeconds % 60;
    const formatted = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    const timerEl = document.getElementById('timer-display');
    if (timerEl) timerEl.innerText = formatted;
}

function renderCurrentQuestion() {
    const q = testState.questions[testState.currentIndex];
    if (!q) return;

    document.getElementById('question-progress-label').innerText = `Question ${testState.currentIndex + 1} of ${testState.questions.length}`;
    document.getElementById('question-text').innerText = q.questionText;
    document.getElementById('question-marks-badge').innerText = `${q.marks} Mark${q.marks > 1 ? 's' : ''}`;

    const optionsContainer = document.getElementById('options-container');
    optionsContainer.innerHTML = '';

    const options = [
        { key: 'A', text: q.optionA },
        { key: 'B', text: q.optionB },
        { key: 'C', text: q.optionC },
        { key: 'D', text: q.optionD }
    ];

    const currentSelected = testState.answers[q.id];

    options.forEach(opt => {
        const optionCard = document.createElement('div');
        optionCard.className = `option-card ${currentSelected === opt.key ? 'selected' : ''}`;
        optionCard.style.cssText = `
            padding: 1rem 1.25rem;
            margin-bottom: 0.75rem;
            border: 1px solid ${currentSelected === opt.key ? 'var(--primary)' : 'var(--border-color)'};
            background: ${currentSelected === opt.key ? 'rgba(79, 70, 229, 0.15)' : 'var(--bg-card)'};
            border-radius: var(--radius-md);
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 1rem;
            transition: all 0.2s;
        `;

        optionCard.onclick = () => selectOption(q.id, opt.key);

        optionCard.innerHTML = `
            <div style="width:28px; height:28px; border-radius:50%; background:${currentSelected === opt.key ? 'var(--primary)' : 'var(--bg-card-hover)'}; display:flex; align-items:center; justify-content:center; font-weight:bold; font-size:0.85rem;">
                ${opt.key}
            </div>
            <div style="flex:1; font-size:0.95rem;">${opt.text}</div>
        `;
        optionsContainer.appendChild(optionCard);
    });

    document.getElementById('prev-btn').disabled = testState.currentIndex === 0;
    document.getElementById('next-btn').innerText = testState.currentIndex === testState.questions.length - 1 ? 'Finish & Submit' : 'Next Question →';
}

function selectOption(questionId, optionKey) {
    testState.answers[questionId] = optionKey;
    renderCurrentQuestion();
    renderQuestionNavGrid();
}

function navigateQuestion(delta) {
    const nextIndex = testState.currentIndex + delta;
    if (nextIndex >= 0 && nextIndex < testState.questions.length) {
        testState.currentIndex = nextIndex;
        renderCurrentQuestion();
    } else if (nextIndex >= testState.questions.length) {
        if (confirm("Are you sure you want to submit your assessment?")) {
            submitTestAnswers(false);
        }
    }
}

function jumpToQuestion(index) {
    testState.currentIndex = index;
    renderCurrentQuestion();
}

function renderQuestionNavGrid() {
    const grid = document.getElementById('question-grid-nav');
    if (!grid) return;
    grid.innerHTML = '';

    testState.questions.forEach((q, idx) => {
        const isAnswered = testState.answers[q.id] != null;
        const isCurrent = idx === testState.currentIndex;

        const btn = document.createElement('button');
        btn.innerText = idx + 1;
        btn.onclick = () => jumpToQuestion(idx);
        btn.style.cssText = `
            width: 36px;
            height: 36px;
            border-radius: var(--radius-sm);
            border: 1px solid ${isCurrent ? 'var(--primary)' : 'var(--border-color)'};
            background: ${isCurrent ? 'var(--primary)' : (isAnswered ? 'var(--success)' : 'var(--bg-card-hover)')};
            color: white;
            font-weight: bold;
            cursor: pointer;
        `;
        grid.appendChild(btn);
    });
}

async function submitTestAnswers(isAutoSubmit = false) {
    if (testState.timerInterval) clearInterval(testState.timerInterval);

    const answersPayload = testState.questions.map(q => ({
        questionId: q.id,
        selectedAnswer: testState.answers[q.id] || null
    }));

    try {
        const res = await apiFetch(`/api/attempts/${testState.attemptId}/submit`, {
            method: 'POST',
            body: JSON.stringify({ answers: answersPayload })
        });

        if (res) {
            showToast('Test submitted successfully!', 'success');
            setTimeout(() => {
                window.location.href = `result.html?attemptId=${testState.attemptId}`;
            }, 1000);
        }
    } catch (e) {
        console.error(e);
    }
}

function setupAntiCheat() {
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            showToast('⚠️ Anti-Cheat Warning: Tab switch detected! Event has been recorded.', 'warning');
        }
    });
}
