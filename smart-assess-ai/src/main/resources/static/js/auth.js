/* Authentication Management */

const AUTH_KEY = 'smart_assess_token';
const USER_KEY = 'smart_assess_user';

function setAuthToken(token, user) {
    localStorage.setItem(AUTH_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
}

function getAuthToken() {
    return localStorage.getItem(AUTH_KEY);
}

function getCurrentUser() {
    const data = localStorage.getItem(USER_KEY);
    return data ? JSON.parse(data) : null;
}

function logout() {
    localStorage.removeItem(AUTH_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.href = 'index.html';
}

function checkAuth(requiredRole = null) {
    const token = getAuthToken();
    const user = getCurrentUser();

    if (!token || !user) {
        window.location.href = 'index.html';
        return false;
    }

    if (requiredRole && user.role !== requiredRole) {
        if (user.role === 'FACULTY') {
            window.location.href = 'faculty-dashboard.html';
        } else {
            window.location.href = 'student-dashboard.html';
        }
        return false;
    }

    // Populate user details on page if element exists
    document.addEventListener('DOMContentLoaded', () => {
        const userNameEl = document.getElementById('user-name-display');
        const userRoleEl = document.getElementById('user-role-display');
        if (userNameEl) userNameEl.innerText = user.name;
        if (userRoleEl) userRoleEl.innerText = user.role;
    });

    return true;
}
