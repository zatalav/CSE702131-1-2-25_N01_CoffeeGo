const form = document.getElementById("registerForm");
const alertBox = document.getElementById("registerAlert");
const stepOtp = document.getElementById("stepOtp");
const stepProfile = document.getElementById("stepProfile");
const sendOtpBtn = document.getElementById("sendOtpBtn");
const verifyOtpBtn = document.getElementById("verifyOtpBtn");
const completeBtn = document.getElementById("completeBtn");
const birthDateInput = form?.querySelector("input[name='ngaySinh']");

const normalizeVnDateInput = (value) => {
  const raw = (value || "").trim();
  if (!raw) return "";
  if (/^\d{8}$/.test(raw)) {
    return `${raw.slice(0, 2)}/${raw.slice(2, 4)}/${raw.slice(4)}`;
  }
  return raw;
};

birthDateInput?.addEventListener("blur", () => {
  birthDateInput.value = normalizeVnDateInput(birthDateInput.value);
});

const showAlert = (message, isSuccess) => {
  if (!alertBox) return;
  alertBox.textContent = message;
  alertBox.classList.toggle("success", Boolean(isSuccess));
  alertBox.style.display = "block";
};

const hideAlert = () => {
  if (!alertBox) return;
  alertBox.style.display = "none";
};

const postJson = async (url, data) => {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(payload.message || "Có lỗi xảy ra");
  }
  return payload;
};

if (sendOtpBtn) {
  sendOtpBtn.addEventListener("click", async () => {
    hideAlert();
    const gmail = form.querySelector("input[name='gmail']")?.value || "";
    const password = form.querySelector("input[name='password']")?.value || "";
    const confirm =
      form.querySelector("input[name='confirmPassword']")?.value || "";

    if (!gmail.trim()) {
      showAlert("Email không được để trống.", false);
      return;
    }
    if (password.length < 6) {
      showAlert("Mật khẩu tối thiểu 6 ký tự.", false);
      return;
    }
    if (password !== confirm) {
      showAlert("Mật khẩu xác nhận không khớp.", false);
      return;
    }

    try {
      sendOtpBtn.disabled = true;
      const payload = await postJson("/register/request-otp", {
        gmail,
        password,
        confirmPassword: confirm,
      });
      showAlert(payload.message || "Đã gửi OTP.", true);
    } catch (err) {
      showAlert(err.message, false);
    } finally {
      sendOtpBtn.disabled = false;
    }
  });
}

if (verifyOtpBtn) {
  verifyOtpBtn.addEventListener("click", async () => {
    hideAlert();
    const otp = form.querySelector("input[name='otp']")?.value || "";
    if (!otp.trim()) {
      showAlert("Vui lòng nhập OTP.", false);
      return;
    }
    try {
      verifyOtpBtn.disabled = true;
      const payload = await postJson("/register/verify-otp", { otp });
      showAlert(payload.message || "OTP hợp lệ.", true);
      if (stepOtp && stepProfile) {
        stepOtp.style.display = "none";
        stepProfile.style.display = "flex";
      }
    } catch (err) {
      showAlert(err.message, false);
    } finally {
      verifyOtpBtn.disabled = false;
    }
  });
}

if (completeBtn) {
  completeBtn.addEventListener("click", async () => {
    hideAlert();
    const otp = form.querySelector("input[name='otp']")?.value || "";
    const tenKh = form.querySelector("input[name='tenKh']")?.value || "";
    const sdt = form.querySelector("input[name='sdt']")?.value || "";
    const gioiTinh = form.querySelector("select[name='gioiTinh']")?.value || "";
    const ngaySinh = normalizeVnDateInput(
      form.querySelector("input[name='ngaySinh']")?.value || "",
    );
    const diaChi = form.querySelector("textarea[name='diaChi']")?.value || "";

    if (!tenKh.trim()) {
      showAlert("Họ tên không được để trống.", false);
      return;
    }
    if (!sdt.trim()) {
      showAlert("Số điện thoại không được để trống.", false);
      return;
    }

    try {
      completeBtn.disabled = true;
      const payload = await postJson("/register/complete", {
        otp,
        tenKh,
        sdt,
        gioiTinh,
        ngaySinh,
        diaChi,
      });
      const nextUrl =
        typeof payload.redirect === "string" && payload.redirect.trim()
          ? payload.redirect.trim()
          : "/customer/menu?registered=1";
      window.location.href = nextUrl;
      return;
    } catch (err) {
      showAlert(err.message, false);
    } finally {
      completeBtn.disabled = false;
    }
  });
}
