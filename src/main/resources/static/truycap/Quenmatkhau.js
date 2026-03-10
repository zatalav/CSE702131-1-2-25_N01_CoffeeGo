const requestOtpForm = document.getElementById("requestOtpForm");
const messageEl = document.getElementById("message");

function setMessage(text, type) {
  messageEl.textContent = text;
  messageEl.className = "msg " + (type || "");
}

requestOtpForm?.addEventListener("submit", async (event) => {
  event.preventDefault();

  const gmail = document.getElementById("gmail").value.trim();
  if (!gmail) {
    setMessage("Please enter gmail", "error");
    return;
  }

  try {
    const response = await fetch("/forgot-password/request-otp", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ gmail }),
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      setMessage(data.message || "Cannot send OTP", "error");
      return;
    }

    setMessage(data.message || "OTP sent", "success");
    window.setTimeout(() => {
      window.location.href = "/forgot-password/otp";
    }, 600);
  } catch (error) {
    setMessage("Network error", "error");
  }
});
