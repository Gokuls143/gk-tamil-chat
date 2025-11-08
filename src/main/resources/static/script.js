document.addEventListener('DOMContentLoaded', () => {
    // Mobile optimization: Add mobile-specific classes and behaviors
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    const isTouch = 'ontouchstart' in window || navigator.maxTouchPoints > 0;
    
    if (isMobile || isTouch) {
        document.body.classList.add('touch-device');
        
        // Prevent zoom on input focus for iOS
        const inputs = document.querySelectorAll('input[type="text"], input[type="email"], input[type="password"], input[type="number"]');
        inputs.forEach(input => {
            if (parseFloat(window.getComputedStyle(input).fontSize) < 16) {
                input.style.fontSize = '16px';
            }
        });
        
        // Improve scroll behavior on mobile
        document.addEventListener('touchstart', function() {}, {passive: true});
        document.addEventListener('touchmove', function() {}, {passive: true});
    }

    const form = document.getElementById('loginForm');
    const registerBtn = document.getElementById('registerBtn');

    const passwordInput = document.getElementById('password');
    const toggleBtn = document.getElementById('togglePassword');

    function showPassword() {
        if (passwordInput) {
            passwordInput.type = 'text';
            passwordInput.focus();
        }
    }
    function hidePassword() {
        if (passwordInput) {
            passwordInput.type = 'password';
        }
    }

    if (toggleBtn && passwordInput) {
        // Mouse events: show while pressed
        toggleBtn.addEventListener('mousedown', (e) => {
            e.preventDefault();
            showPassword();
        });
        toggleBtn.addEventListener('mouseup', (e) => {
            e.preventDefault();
            hidePassword();
        });
        toggleBtn.addEventListener('mouseleave', (e) => {
            hidePassword();
        });

        // Touch events for mobile: show while touch active
        toggleBtn.addEventListener('touchstart', (e) => {
            e.preventDefault();
            showPassword();
        }, { passive: false });
        toggleBtn.addEventListener('touchend', (e) => {
            e.preventDefault();
            hidePassword();
        });
        toggleBtn.addEventListener('touchcancel', (e) => {
            hidePassword();
        });
    }

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value;
            const password = passwordInput ? passwordInput.value : '';

            try {
                const resp = await fetch('/api/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });

                if (resp.ok) {
                    window.location.href = '/landing.html';
                } else {
                    const text = await resp.text();
                    alert('Login failed: ' + (text || resp.status));
                }
            } catch (err) {
                console.error('Login request failed', err);
                alert('Network error');
            }
        });
    }

    if (registerBtn) {
        registerBtn.addEventListener('click', () => {
            window.location.href = "register.html";
        });
    }

    // ----- Register form handling (new) -----
    const registerForm = document.getElementById('registerForm');
    const registerStatus = document.getElementById('registerStatus');
    const registerIcon = document.getElementById('registerStatusIcon');

    function setRegisterStatus(type, message) {
        if (!registerStatus) return;
        registerStatus.textContent = message || '';
        if (!registerIcon) return;
        registerIcon.innerHTML = type === 'success'
            ? '<svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path fill="#0a0" d="M9 16.2 4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4z"/></svg>'
            : '<svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path fill="#d00" d="M12 10.586 16.95 5.636 18.364 7.05 13.414 12 18.364 16.95 16.95 18.364 12 13.414 7.05 18.364 5.636 16.95 10.586 12 5.636 7.05 7.05 5.636z"/></svg>';
        // add a CSS class for styling
        registerIcon.classList.toggle('ok', type === 'success');
        registerIcon.classList.toggle('err', type !== 'success');
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = (document.getElementById('reg-username').value || '').trim().toLowerCase();
            const password = document.getElementById('reg-password').value || '';

            setRegisterStatus('', 'Submitting...');
            try {
                const resp = await fetch('/api/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });

                if (resp.status === 201) {
                    setRegisterStatus('success', 'User registered successfully');
                } else if (resp.status === 409) {
                    setRegisterStatus('error', 'User already registered');
                } else {
                    const text = await resp.text();
                    setRegisterStatus('error', text || `Error ${resp.status}`);
                }
            } catch (err) {
                console.error('Register request failed', err);
                setRegisterStatus('error', 'Network error');
            }

            // optional: clear message after a short time
            setTimeout(() => {
                if (registerStatus) registerStatus.textContent = '';
                if (registerIcon) registerIcon.innerHTML = '';
            }, 6000);
        });
    }

    // Forgot-username UI handlers
    const forgotLink = document.getElementById('forgotUsernameLink');
    const forgotModal = document.getElementById('forgotUsernameModal');
    const forgotCancel = document.getElementById('forgotCancel');
    const forgotSend = document.getElementById('forgotSend');
    const forgotEmail = document.getElementById('forgotEmail');
    const forgotError = document.getElementById('forgotError');

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    if (forgotLink && forgotModal) {
        forgotLink.addEventListener('click', (e) => {
            e.preventDefault();
            forgotModal.style.display = 'flex';
            if (forgotEmail) { forgotEmail.value = ''; forgotEmail.focus(); }
            if (forgotError) forgotError.textContent = '';
        });
    }
    if (forgotCancel && forgotModal) {
        forgotCancel.addEventListener('click', () => { forgotModal.style.display = 'none'; });
    }

    if (forgotSend && forgotEmail) {
        forgotSend.addEventListener('click', async () => {
            const email = (forgotEmail.value || '').trim();
            if (!isValidEmail(email)) {
                if (forgotError) forgotError.textContent = 'Enter a valid email.';
                return;
            }
            if (forgotError) forgotError.textContent = '';
            forgotSend.disabled = true;
            try {
                const resp = await fetch('/api/forgot-username', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email })
                });

                if (resp.status === 200) {
                    // server should send email; show success message then close
                    forgotSend.disabled = false;
                    if (forgotError) { forgotError.style.color = '#0a0'; forgotError.textContent = 'If the email exists, we sent instructions.'; }
                    setTimeout(() => { forgotModal.style.display = 'none'; if (forgotError) { forgotError.style.color = '#d00'; forgotError.textContent = ''; } }, 1800);
                } else {
                    const text = await resp.text();
                    if (forgotError) { forgotError.style.color = '#d00'; forgotError.textContent = text || 'Unable to process request'; }
                    forgotSend.disabled = false;
                }
            } catch (err) {
                console.error(err);
                if (forgotError) { forgotError.style.color = '#d00'; forgotError.textContent = 'Network error'; }
                forgotSend.disabled = false;
            }
        });
    }

    // close modal on outside click
    if (forgotModal) {
        forgotModal.addEventListener('click', (ev) => {
            if (ev.target === forgotModal) forgotModal.style.display = 'none';
        });
    }
});
