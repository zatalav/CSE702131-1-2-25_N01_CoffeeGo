document.addEventListener("DOMContentLoaded", () => {
  const modal = document.getElementById("cancelModal");
  const closeBtn = document.getElementById("closeCancelModal");
  const cancelBtn = document.getElementById("cancelCloseBtn");
  const confirmBtn = document.getElementById("confirmCancelBtn");
  const reasonSelect = document.getElementById("cancelReason");
  const cancelNote = document.getElementById("cancelNote");
  const cancelOrderId = document.getElementById("cancelOrderId");

  const openModal = (orderId) => {
    cancelOrderId.value = orderId;
    modal.style.display = "flex";
  };

  const closeModal = () => {
    modal.style.display = "none";
  };

  document.querySelectorAll(".open-cancel").forEach((btn) => {
    btn.addEventListener("click", () => {
      openModal(btn.dataset.id);
    });
  });

  closeBtn?.addEventListener("click", closeModal);
  cancelBtn?.addEventListener("click", closeModal);

  modal?.addEventListener("click", (event) => {
    if (event.target === modal) {
      closeModal();
    }
  });

  confirmBtn?.addEventListener("click", async () => {
    const orderId = cancelOrderId.value;
    const reason = reasonSelect.value;
    const note = cancelNote.value;

    if (!orderId || !reason) {
      return;
    }

    const payload = {
      orderId,
      reason: note ? `${reason} - ${note}` : reason,
    };

    const response = await fetch("/nhanvien/donhang/cancel/pdf", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      return;
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    const timestamp = new Date()
      .toISOString()
      .replace(/[:.]/g, "-")
      .slice(0, 19);

    link.href = url;
    link.download = `huy-don-${orderId}-${timestamp}.pdf`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);

    window.location.reload();
  });

  const menuBtn = document.getElementById("menuBtn");
  const menuDropdown = document.getElementById("menuDropdown");

  if (menuBtn && menuDropdown) {
    menuBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      const isOpen = menuDropdown.style.display === "block";
      menuDropdown.style.display = isOpen ? "none" : "block";
      menuBtn.setAttribute("aria-expanded", (!isOpen).toString());
    });

    document.addEventListener("click", () => {
      menuDropdown.style.display = "none";
      menuBtn.setAttribute("aria-expanded", "false");
    });

    menuDropdown.addEventListener("click", (e) => {
      e.stopPropagation();
    });
  }
});
