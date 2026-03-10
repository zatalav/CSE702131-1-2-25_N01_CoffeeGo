document.addEventListener("DOMContentLoaded", function () {
  const alerts = document.querySelectorAll(".alert");
  if (alerts.length) {
    setTimeout(() => {
      alerts.forEach((alert) => {
        alert.style.display = "none";
      });
    }, 5000);
  }

  const setText = (id, value) => {
    const el = document.getElementById(id);
    if (el) el.innerText = value ?? "";
  };
  const setValue = (id, value) => {
    const el = document.getElementById(id);
    if (el) el.value = value ?? "";
  };

  window.AdminUiCommon?.initSidebarAndProfile?.();

  /* ===== ADD MODAL ===== */
  const addBtn = document.getElementById("addBtn");
  const addModal = document.getElementById("addStaffModal");
  const closeAdd = document.getElementById("closeModal");
  const cancelAdd = document.getElementById("cancelBtn");

  if (addBtn && addModal) {
    addBtn.onclick = () => (addModal.style.display = "flex");
    if (closeAdd) closeAdd.onclick = () => (addModal.style.display = "none");
    if (cancelAdd) cancelAdd.onclick = () => (addModal.style.display = "none");
  }

  /* ===== VIEW MODAL ===== */
  const viewModal = document.getElementById("viewModal");
  const closeViewModal = document.getElementById("closeViewModal");
  const closeViewBtn = document.getElementById("closeViewBtn");

  if (viewModal) {
    document.querySelectorAll(".open-view").forEach((btn) => {
      btn.onclick = () => {
        setText("viewId", btn.dataset.id);
        setText("viewTen", btn.dataset.ten);
        setText("viewGiamGia", btn.dataset.giamgia);
        setText("viewMaxGiam", btn.dataset.maxGiam);
        setText("viewGiaTri", btn.dataset.giaTri);
        setText("viewNgayHet", btn.dataset.ngayhet);
        setText(
          "viewTrangThai",
          btn.dataset.trangthai === "HOAT_DONG" ? "Hoạt động" : "Hết hạn",
        );
        setText("viewMoTa", btn.dataset.mota);
        viewModal.style.display = "flex";
      };
    });

    if (closeViewModal)
      closeViewModal.onclick = () => (viewModal.style.display = "none");
    if (closeViewBtn)
      closeViewBtn.onclick = () => (viewModal.style.display = "none");
  }

  /* ===== EDIT MODAL ===== */
  const editModal = document.getElementById("editModal");
  const closeEdit = document.getElementById("closeEditModal");
  const cancelEdit = document.getElementById("cancelEditBtn");

  if (editModal) {
    document.querySelectorAll(".open-edit").forEach((btn) => {
      btn.onclick = () => {
        setValue("editId", btn.dataset.id);
        setValue("editTen", btn.dataset.ten);
        setValue("editGiamGia", btn.dataset.giamgia);
        setValue("editMaxGiam", btn.dataset.maxGiam);
        setValue("editGiaTri", btn.dataset.giaTri);
        setValue("editNgayHet", btn.dataset.ngayhet);
        setValue("editTrangThai", btn.dataset.trangthai);
        setValue("editMoTa", btn.dataset.mota);
        editModal.style.display = "flex";
      };
    });

    if (closeEdit) closeEdit.onclick = () => (editModal.style.display = "none");
    if (cancelEdit)
      cancelEdit.onclick = () => (editModal.style.display = "none");
  }

  /* ===== DELETE MODAL ===== */
  const deleteModal = document.getElementById("deleteModal");
  const closeDelete = document.getElementById("closeDeleteModal");
  const cancelDelete = document.getElementById("cancelDeleteBtn");

  if (deleteModal) {
    document.querySelectorAll(".open-delete").forEach((btn) => {
      btn.onclick = () => {
        setValue("deleteId", btn.dataset.id);
        setText("deleteTen", btn.dataset.ten);
        deleteModal.style.display = "flex";
      };
    });

    if (closeDelete)
      closeDelete.onclick = () => (deleteModal.style.display = "none");
    if (cancelDelete)
      cancelDelete.onclick = () => (deleteModal.style.display = "none");
  }

  /* ===== FILTER (SEARCH + STATUS) ===== */
  const searchInput = document.getElementById("couponSearch");
  const statusFilter = document.getElementById("couponStatusFilter");
  const tableBody = document.querySelector(".table-list tbody");

  const applyFilters = () => {
    if (!tableBody) return;
    const term = (searchInput?.value || "").trim().toLowerCase();
    const statusVal = (statusFilter?.value || "").trim().toUpperCase();

    Array.from(tableBody.querySelectorAll("tr")).forEach((row) => {
      const idText = row.querySelector("td:nth-child(1)")?.textContent || "";
      const nameText = row.querySelector("td:nth-child(2)")?.textContent || "";
      const haystack = `${idText} ${nameText}`.toLowerCase();

      const rowStatus = (row.dataset.status || "")
        .toString()
        .trim()
        .toUpperCase();
      const matchText = term === "" || haystack.includes(term);
      const matchStatus = statusVal === "" || rowStatus === statusVal;

      row.style.display = matchText && matchStatus ? "" : "none";
    });
  };

  if (searchInput) searchInput.addEventListener("input", applyFilters);
  if (statusFilter) statusFilter.addEventListener("change", applyFilters);

  /* ===== CLICK OUTSIDE MODAL ===== */
  document.querySelectorAll(".modal-overlay").forEach((overlay) => {
    overlay.addEventListener("click", (e) => {
      if (e.target === overlay) overlay.style.display = "none";
    });
  });

  /* ===== ESC CLOSE ALL ===== */
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      document.querySelectorAll(".modal-overlay").forEach((modal) => {
        modal.style.display = "none";
      });
    }
  });
});
