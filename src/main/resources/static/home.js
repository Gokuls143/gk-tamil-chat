document.addEventListener('DOMContentLoaded', () => {
  const openLogin = document.getElementById('openLogin');
  const openRegister = document.getElementById('openRegister');
  const openGuest = document.getElementById('openGuest');
  const loginModal = document.getElementById('loginModal');
  const registerModal = document.getElementById('registerModal');
  const guestModal = document.getElementById('guestModal');
  const loginModalContent = document.getElementById('loginModalContent');
  const registerModalContent = document.getElementById('registerModalContent');

  function show(modal) {
    modal.style.display = 'flex';
    modal.setAttribute('aria-hidden', 'false');
    const input = modal.querySelector('input');
    if (input) setTimeout(() => input.focus(), 60);
  }
  function hide(modal) {
    modal.style.display = 'none';
    modal.setAttribute('aria-hidden', 'true');
  }

  // load a form fragment from a static page and inject into target container
  async function injectForm(url, selector, container) {
    // if the container already has a visible form, skip
    const existing = container.querySelector(selector);
    if (existing && existing.offsetParent !== null) return existing;

    try {
      const res = await fetch(url, { cache: 'no-store' });
      if (!res.ok) throw new Error('Fetch failed');
      const text = await res.text();
      const parser = new DOMParser();
      const doc = parser.parseFromString(text, 'text/html');
      const form = doc.querySelector(selector);
      if (form) {
        // clear container and append cloned form (to avoid moving nodes from fetched doc)
        container.innerHTML = '';
        const clone = form.cloneNode(true);
        // ensure form is visible
        clone.style.display = '';
        container.appendChild(clone);
        return clone;
      }
    } catch (err) {
      // fetch failed -> leave fallback markup already present in container (hidden form)
      const fallback = container.querySelector(selector);
      if (fallback) {
        fallback.style.display = '';
        return fallback;
      }
      console.warn('injectForm failed for', url, err);
      return null;
    }
  }

  // show modal and ensure form markup is injected
  openLogin?.addEventListener('click', async () => {
    await injectForm('login.html', '#loginForm', loginModalContent);
    wireLoginModal(loginModalContent);
    show(loginModal);
  });

  openRegister?.addEventListener('click', async () => {
    await injectForm('register.html', '#registerForm', registerModalContent);
    wireRegisterModal(registerModalContent);
    show(registerModal);
  });

  openGuest?.addEventListener('click', () => show(guestModal));

  // close handlers
  document.querySelectorAll('.modal-backdrop').forEach((m) => {
    m.querySelectorAll('[data-close]').forEach(btn => btn.addEventListener('click', () => hide(m)));
    m.addEventListener('click', (e) => { if (e.target === m) hide(m); });
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') hide(m); });
  });

  // small helper: attach hold-to-show to any toggle buttons inside container
  function attachHoldToggles(container) {
    container.querySelectorAll('.icon-btn').forEach(btn => {
      // find adjacent input (password) - prefer previousElementSibling or query within container
      const input = container.querySelector('input[type="password"], input:not([type])');
      // better approach: attempt to find input sibling within the same .password-group
      let pwd = null;
      const pg = btn.closest('.password-group');
      if (pg) pwd = pg.querySelector('input[type="password"], input');
      if (!pwd) pwd = input;
      if (!pwd) return;
      const show = () => { pwd.type = 'text'; pwd.focus(); };
      const hidePwd = () => { pwd.type = 'password'; };
      btn.addEventListener('mousedown', (e) => { e.preventDefault(); show(); });
      btn.addEventListener('mouseup', (e) => { e.preventDefault(); hidePwd(); });
      btn.addEventListener('mouseleave', hidePwd);
      btn.addEventListener('touchstart', (e) => { e.preventDefault(); show(); }, { passive: false });
      btn.addEventListener('touchend', (e) => { e.preventDefault(); hidePwd(); });
      btn.addEventListener('touchcancel', hidePwd);
    });
  }

  // wire login modal behavior (submit, forgot link, toggles)
  function wireLoginModal(container) {
    attachHoldToggles(container);
    const form = container.querySelector('#loginForm');
    if (!form) return;

    // avoid double-binding
    if (form.dataset.wired === 'true') return;
    form.dataset.wired = 'true';

    const feedback = form.querySelector('#loginFeedback') || (() => {
      const fb = document.createElement('div'); fb.id = 'loginFeedback'; fb.className = 'feedback'; form.appendChild(fb); return fb;
    })();

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      feedback.textContent = '';
      const username = form.querySelector('[name="username"], #username')?.value || '';
      const password = form.querySelector('[name="password"], #password')?.value || '';
      try {
        const resp = await fetch('/api/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }) });
        if (resp.ok) {
          feedback.innerHTML = '<span class="ok">Login successful — redirecting…</span>';
          setTimeout(() => window.location.href = 'landing.html', 700);
        } else {
          const text = await resp.text();
          feedback.innerHTML = `<span class="err">${text || 'Login failed'}</span>`;
        }
      } catch (err) {
        feedback.innerHTML = `<span class="err">Network error</span>`;
      }
    });

    // forgot username link inside injected login form
    const forgotLink = form.querySelector('#forgotUsernameLink') || form.querySelector('[data-forgot]') || form.querySelector('a[href="#"][id*="forgot"]');
    if (forgotLink) {
      forgotLink.addEventListener('click', (ev) => {
        ev.preventDefault();
        const email = prompt('Enter the email used during registration:');
        if (email && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
          fetch('/api/forgot-username', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email }) })
            .then(() => alert('If that email exists, we sent instructions.'))
            .catch(() => alert('Network error'));
        } else if (email) {
          alert('Enter a valid email');
        }
      });
    }
  }

  // wire register modal behavior (submit, toggles, validation)
  function wireRegisterModal(container) {
    attachHoldToggles(container);
    const form = container.querySelector('#registerForm');
    if (!form) return;

    if (form.dataset.wired === 'true') return;
    form.dataset.wired = 'true';

    const feedback = form.querySelector('#registerFeedback') || (() => {
      const fb = document.createElement('div'); fb.id = 'registerFeedback'; fb.className = 'feedback'; form.appendChild(fb); return fb;
    })();
    const fieldError = form.querySelector('#fieldError') || form.querySelector('.field-error') || (() => {
      const fe = document.createElement('div'); fe.id = 'fieldError'; fe.className = 'field-error'; form.appendChild(fe); return fe;
    })();

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      feedback.textContent = '';
      fieldError.textContent = '';

      const username = (form.querySelector('[name="username"], #reg-username')?.value || '').trim().toLowerCase();
      const email = (form.querySelector('[name="email"], #reg-email')?.value || '').trim();
      const password = form.querySelector('[name="password"], #reg-password')?.value || '';
      const passwordConfirm = form.querySelector('[name="passwordConfirm"], #reg-password-confirm')?.value || '';

      if (!username || !email || !password || !passwordConfirm) {
        fieldError.textContent = 'All fields are required';
        return;
      }
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        fieldError.textContent = 'Please enter a valid email address';
        return;
      }
      if (password !== passwordConfirm) {
        fieldError.textContent = 'Passwords do not match';
        form.querySelector('#reg-password')?.focus();
        form.querySelector('#reg-password') && (form.querySelector('#reg-password').value = '');
        form.querySelector('#reg-password-confirm') && (form.querySelector('#reg-password-confirm').value = '');
        return;
      }

      feedback.textContent = 'Submitting...';
      try {
        const resp = await fetch('/api/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username, email, password })
        });
        if (resp.status === 201) {
          feedback.innerHTML = '<span class="ok">Registered — redirecting to login…</span>';
          setTimeout(() => {
            hide(registerModal);
            // show login modal (will inject login form if needed)
            document.getElementById('openLogin')?.click();
          }, 900);
        } else if (resp.status === 409) {
          feedback.innerHTML = '<span class="err">User already registered</span>';
          form.querySelector('#reg-password') && (form.querySelector('#reg-password').value = '');
          form.querySelector('#reg-password-confirm') && (form.querySelector('#reg-password-confirm').value = '');
          form.querySelector('#reg-username')?.focus();
        } else {
          const text = await resp.text();
          feedback.innerHTML = `<span class="err">${text || 'Registration failed'}</span>`;
        }
      } catch (err) {
        feedback.innerHTML = `<span class="err">Network error</span>`;
      }
    });
  }

  // guest handlers
  document.getElementById('guestContinue')?.addEventListener('click', () => { window.location.href = 'landing.html'; });
  document.getElementById('guestCancel')?.addEventListener('click', () => hide(guestModal));
});