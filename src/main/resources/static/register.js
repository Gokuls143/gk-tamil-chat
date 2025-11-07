document.addEventListener('DOMContentLoaded', () => {
  const registerForm = document.getElementById('registerForm');
  const registerStatus = document.getElementById('registerStatus');
  const registerIcon = document.getElementById('registerStatusIcon');
  const fieldError = document.getElementById('fieldError');

  const pwd = document.getElementById('reg-password');
  const pwdConfirm = document.getElementById('reg-password-confirm');
  const togglePwd = document.getElementById('toggleRegPassword');
  const togglePwdConfirm = document.getElementById('toggleRegPasswordConfirm');
  const emailEl = document.getElementById('reg-email');
  const usernameEl = document.getElementById('reg-username');

  function setRegisterStatus(type, message) {
    if (registerStatus) registerStatus.textContent = message || '';
    if (!registerIcon) return;
    if (type === 'success') {
      registerIcon.innerHTML = '<svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path fill="#0a0" d="M9 16.2 4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4z"/></svg>';
      registerIcon.classList.add('status-ok'); registerIcon.classList.remove('status-err');
    } else if (type === 'error') {
      registerIcon.innerHTML = '<svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path fill="#d00" d="M12 10.586 16.95 5.636 18.364 7.05 13.414 12 18.364 16.95 16.95 18.364 12 13.414 7.05 18.364 5.636 16.95 10.586 12 5.636 7.05 7.05 5.636z"/></svg>';
      registerIcon.classList.add('status-err'); registerIcon.classList.remove('status-ok');
    } else {
      registerIcon.innerHTML = '';
      registerIcon.classList.remove('status-ok','status-err');
    }
  }

  function showWhilePressed(inputEl, btnEl) {
    if (!inputEl || !btnEl) return;
    const show = () => { inputEl.type = 'text'; inputEl.focus(); };
    const hide = () => { inputEl.type = 'password'; };

    btnEl.addEventListener('mousedown', (e) => { e.preventDefault(); show(); });
    btnEl.addEventListener('mouseup', (e) => { e.preventDefault(); hide(); });
    btnEl.addEventListener('mouseleave', () => { hide(); });

    // Touch events
    btnEl.addEventListener('touchstart', (e) => { e.preventDefault(); show(); }, { passive: false });
    btnEl.addEventListener('touchend', (e) => { e.preventDefault(); hide(); });
    btnEl.addEventListener('touchcancel', () => { hide(); });
  }

  showWhilePressed(pwd, togglePwd);
  showWhilePressed(pwdConfirm, togglePwdConfirm);

  if (!registerForm) return;

  function isValidEmail(email) {
    // simple, permissive regex
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    setRegisterStatus('', ''); // clear
    if (fieldError) fieldError.textContent = '';

    const username = (usernameEl && usernameEl.value) ? usernameEl.value.trim().toLowerCase() : '';
    const email = (emailEl && emailEl.value) ? emailEl.value.trim() : '';
    const password = (pwd && pwd.value) ? pwd.value : '';
    const passwordConfirm = (pwdConfirm && pwdConfirm.value) ? pwdConfirm.value : '';

    if (!username || !email || !password || !passwordConfirm) {
      if (fieldError) fieldError.textContent = 'All fields are required';
      return;
    }

    if (!isValidEmail(email)) {
      if (fieldError) fieldError.textContent = 'Please enter a valid email address';
      if (emailEl) emailEl.focus();
      return;
    }

    if (password !== passwordConfirm) {
      setRegisterStatus('error', 'Passwords do not match');
      if (pwd) pwd.value = '';
      if (pwdConfirm) pwdConfirm.value = '';
      if (pwd) pwd.focus();
      return;
    }

    setRegisterStatus('', 'Submitting...');
    try {
      const resp = await fetch('/api/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, email, password })
      });

      if (resp.status === 201) {
        setRegisterStatus('success', 'User registered successfully');
        registerForm.reset();
        setTimeout(() => { window.location.href = 'login.html'; }, 1400);
      } else if (resp.status === 409) {
        if (pwd) pwd.value = '';
        if (pwdConfirm) pwdConfirm.value = '';
        setRegisterStatus('error', 'User already registered');
        if (usernameEl) usernameEl.focus();
      } else {
        const text = await resp.text();
        setRegisterStatus('error', text || `Error ${resp.status}`);
        if (pwd) pwd.value = '';
        if (pwdConfirm) pwdConfirm.value = '';
      }
    } catch (err) {
      console.error(err);
      setRegisterStatus('error', 'Network error');
      if (pwd) pwd.value = '';
      if (pwdConfirm) pwdConfirm.value = '';
    }

    setTimeout(() => { setRegisterStatus('', ''); if (fieldError) fieldError.textContent = ''; }, 6000);
  });
});
