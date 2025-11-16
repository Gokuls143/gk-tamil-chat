// -------------------------
// LOGIN FUNCTIONALITY
// -------------------------
document.getElementById("loginBtn").addEventListener("click", async () => {
    const email = document.getElementById("home-email").value.trim();
    const password = document.getElementById("home-password").value.trim();
    const feedback = document.getElementById("login-feedback");

    if (!email || !password) {
        feedback.innerHTML = "<span style='color:red'>Enter email & password!</span>";
        return;
    }

    try {
        const response = await fetch("/api/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            feedback.innerHTML = "<span style='color:red'>Invalid credentials!</span>";
            return;
        }

        const data = await response.json();

        if (data.success === true) {
            feedback.innerHTML = "<span style='color:green'>Login successful!</span>";
            setTimeout(() => {
                window.location.href = "landing.html"; 
            }, 700);
        } else {
            feedback.innerHTML = "<span style='color:red'>Invalid credentials!</span>";
        }

    } catch (error) {
        feedback.innerHTML = "<span style='color:red'>Server error!</span>";
    }
});
