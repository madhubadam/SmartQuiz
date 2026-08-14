/* UI Utilities: Toasts, Modals, Loading States */

function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerText = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function showLoading(elementOrSelector) {
    const el = typeof elementOrSelector === 'string' ? document.querySelector(elementOrSelector) : elementOrSelector;
    if (el) {
        el.dataset.oldContent = el.innerHTML;
        el.innerHTML = `<span class="spinner" style="display:inline-block; width:16px; height:16px; border:2px solid rgba(255,255,255,0.3); border-top-color:white; border-radius:50%; animation:spin 0.8s linear infinite;"></span> Processing...`;
        el.disabled = true;
    }
}

function hideLoading(elementOrSelector) {
    const el = typeof elementOrSelector === 'string' ? document.querySelector(elementOrSelector) : elementOrSelector;
    if (el && el.dataset.oldContent) {
        el.innerHTML = el.dataset.oldContent;
        el.disabled = false;
    }
}

// Keyframe animation for spinner
const style = document.createElement('style');
style.innerHTML = `@keyframes spin { to { transform: rotate(360deg); } }`;
document.head.appendChild(style);
