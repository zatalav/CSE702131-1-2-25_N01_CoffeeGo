// Login Form Handler - allow real form submit for Spring Security
const loginForm = document.getElementById("loginForm");
if (loginForm) {
  loginForm.addEventListener("submit", function (e) {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    // Basic validation: if fields empty, prevent submit and alert
    if (!username || !password) {
      e.preventDefault();
      alert("Please fill in all fields");
      return;
    }
    // otherwise allow the form to submit to /login (Spring Security)
  });
}

// Close button handler (removed from DOM) - guard in case present
const closeBtn = document.querySelector(".close-btn");
if (closeBtn) {
  closeBtn.addEventListener("click", function () {
    if (confirm("Close login page?")) {
      window.location.href = "about:blank";
    }
  });
}

// Social login handlers (buttons removed) - guard in case present
const googleBtn = document.querySelector(".google-btn");
if (googleBtn) {
  googleBtn.addEventListener("click", function () {
    console.log("Google login clicked");
    alert("Google login would be initiated here");
  });
}

const facebookBtn = document.querySelector(".facebook-btn");
if (facebookBtn) {
  facebookBtn.addEventListener("click", function () {
    console.log("Facebook login clicked");
    alert("Facebook login would be initiated here");
  });
}

// Forgot password handler
const forgot = document.querySelector(".forgot-password");
if (forgot) {
  forgot.addEventListener("click", function (e) {
    if (forgot.getAttribute("href")) {
      return;
    }
    e.preventDefault();
    window.location.href = "/forgot-password";
  });
}

// Add smooth transitions for inputs
const inputs = document.querySelectorAll(".form-input");
inputs.forEach((input) => {
  input.addEventListener("focus", function () {
    this.parentElement.style.transform = "scale(1.02)";
    this.parentElement.style.transition = "transform 0.2s ease";
  });

  input.addEventListener("blur", function () {
    this.parentElement.style.transform = "scale(1)";
  });
});

// Toggle password visibility
const toggleBtn = document.querySelector(".toggle-password");
if (toggleBtn) {
  const passwordInput = document.getElementById("password");
  toggleBtn.addEventListener("click", function () {
    if (!passwordInput) return;
    const isPassword = passwordInput.type === "password";
    passwordInput.type = isPassword ? "text" : "password";
    // toggle visual state on parent wrapper for CSS
    const wrapper = passwordInput.parentElement;
    if (isPassword) {
      wrapper.classList.add("show-password");
      toggleBtn.setAttribute("aria-label", "Hide password");
      toggleBtn.setAttribute("title", "Hide password");
    } else {
      wrapper.classList.remove("show-password");
      toggleBtn.setAttribute("aria-label", "Show password");
      toggleBtn.setAttribute("title", "Show password");
    }
  });
}
