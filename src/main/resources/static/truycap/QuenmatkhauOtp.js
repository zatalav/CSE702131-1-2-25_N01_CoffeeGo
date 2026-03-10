const verifyOtpForm = document.getElementById("verifyOtpForm");
const otpMessageEl = document.getElementById("message");

function setOtpMessage(text, type) {
  otpMessageEl.textContent = text;
  otpMessageEl.className = "msg " + (type || "");
}

verifyOtpForm?.addEventListener("submit", async (event) => {
  event.preventDefault();
  const otp = document.getElementById("otp").value.trim();

  if (!otp) {
    setOtpMessage("Please enter OTP", "error");
    return;
  }

  try {
    const response = await fetch("/forgot-password/verify-otp", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ otp }),
    });

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      setOtpMessage(data.message || "OTP is invalid", "error");
      return;
    }

    setOtpMessage(data.message || "OTP verified", "success");
    window.setTimeout(() => {
      window.location.href = "/forgot-password/reset";
    }, 600);
  } catch (error) {
    setOtpMessage("Network error", "error");
  }
});
