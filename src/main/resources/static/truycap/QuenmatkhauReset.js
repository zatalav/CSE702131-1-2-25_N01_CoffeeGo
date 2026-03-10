const resetPasswordForm = document.getElementById("resetPasswordForm");
const resetMessageEl = document.getElementById("message");

function setResetMessage(text, type) {
  resetMessageEl.textContent = text;
  resetMessageEl.className = "msg " + (type || "");
}

resetPasswordForm?.addEventListener("submit", async (event) => {
  event.preventDefault();

  const newPassword = document.getElementById("newPassword").value.trim();
  const confirmPassword = document
    .getElementById("confirmPassword")
    .value.trim();

  if (!newPassword || !confirmPassword) {
    setResetMessage("Please enter both password fields", "error");
    return;
  }
  if (newPassword !== confirmPassword) {
    setResetMessage("Password confirmation does not match", "error");
    return;
  }

  try {
    const response = await fetch("/forgot-password/reset", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ newPassword, confirmPassword }),
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      setResetMessage(data.message || "Cannot reset password", "error");
      return;
    }

    setResetMessage(data.message || "Password reset successful", "success");
    window.setTimeout(() => {
      window.location.href = data.redirect || "/login?reset=1";
    }, 700);
  } catch (error) {
    setResetMessage("Network error", "error");
  }
});
