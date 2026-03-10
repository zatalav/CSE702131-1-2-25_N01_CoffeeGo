const searchInput = document.getElementById("kitchenSearch");
const tableBody = document.getElementById("kitchenBody");
const viewModal = document.getElementById("viewModal");
const closeViewModal = document.getElementById("closeViewModal");
const closeViewBtn = document.getElementById("closeViewBtn");

const setText = (id, value) => {
  const el = document.getElementById(id);
  if (el) el.textContent = value ?? "";
};

if (searchInput && tableBody) {
  searchInput.addEventListener("input", () => {
    const keyword = searchInput.value.trim().toLowerCase();
    const rows = Array.from(tableBody.querySelectorAll("tr"));

    rows.forEach((row) => {
      const text = row.textContent.toLowerCase();
      row.style.display = text.includes(keyword) ? "" : "none";
    });
  });
}

if (viewModal && tableBody) {
  tableBody.querySelectorAll(".open-view").forEach((btn) => {
    btn.addEventListener("click", () => {
      setText("viewId", btn.dataset.id);
      setText("viewTen", btn.dataset.ten);
      setText("viewSlTon", btn.dataset.slton);
      setText("viewDonVi", btn.dataset.donvi);
      setText("viewNhaCungCap", btn.dataset.nhacungcap);
      setText("viewTrangThai", btn.dataset.trangthai);
      viewModal.style.display = "flex";
    });
  });
}

if (closeViewModal) {
  closeViewModal.addEventListener("click", () => {
    viewModal.style.display = "none";
  });
}

if (closeViewBtn) {
  closeViewBtn.addEventListener("click", () => {
    viewModal.style.display = "none";
  });
}

document.querySelectorAll(".modal-overlay").forEach((overlay) => {
  overlay.addEventListener("click", (e) => {
    if (e.target === overlay) {
      overlay.style.display = "none";
    }
  });
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
