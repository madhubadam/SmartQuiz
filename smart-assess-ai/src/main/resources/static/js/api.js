/* API Fetch Wrapper with JWT Header */

async function apiFetch(endpoint, options = {}) {
    const token = getAuthToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(endpoint, config);

        if (response.status === 401) {
            showToast('Session expired. Please log in again.', 'warning');
            logout();
            return null;
        }

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            const errorMsg = data.message || `Server returned error ${response.status}`;
            throw new Error(errorMsg);
        }

        return data;
    } catch (error) {
        showToast(error.message, 'error');
        throw error;
    }
}
