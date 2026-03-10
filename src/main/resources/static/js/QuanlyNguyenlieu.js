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
        setText("viewDonVi", btn.dataset.donvi);
        setText("viewSlTon", btn.dataset.slton);
        setText("viewGiaNhap", btn.dataset.gianhap);
        setText("viewNhaCungCap", btn.dataset.nhacungcap);
        setText("viewTrangThai", btn.dataset.trangthai);
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
        setValue("editDonVi", btn.dataset.donvi);
        setValue("editSlTon", btn.dataset.slton);
        setValue("editGiaNhap", btn.dataset.gianhap);
        setValue("editNhaCungCapId", btn.dataset.nhacungcapid);
        setValue("editDanhMucId", btn.dataset.danhmucid);
        setValue("editTrangThai", btn.dataset.trangthai);
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
        const deleteForm = document.getElementById("deleteForm");
        if (deleteForm && btn.dataset.deleteUrl) {
          deleteForm.action = btn.dataset.deleteUrl;
        }
        deleteModal.style.display = "flex";
      };
    });

    if (closeDelete)
      closeDelete.onclick = () => (deleteModal.style.display = "none");
    if (cancelDelete)
      cancelDelete.onclick = () => (deleteModal.style.display = "none");
  }

  /* ===== FILTER (SEARCH + STATUS) ===== */
  const searchInput = document.getElementById("ingredientSearch");
  const statusFilter = document.getElementById("ingredientStatusFilter");
  const tableBody = document.querySelector(".table-list tbody");

  const normalizeStatus = (value) => (value || "").trim().toLowerCase();

  const applyFilters = () => {
    if (!tableBody) return;
    const term = (searchInput?.value || "").trim().toLowerCase();
    const statusVal = normalizeStatus(statusFilter?.value || "");

    Array.from(tableBody.querySelectorAll("tr")).forEach((row) => {
      const idText = row.querySelector("td:nth-child(1)")?.textContent || "";
      const nameText = row.querySelector("td:nth-child(2)")?.textContent || "";
      const supplierText =
        row.querySelector("td:nth-child(6)")?.textContent || "";
      const haystack = `${idText} ${nameText} ${supplierText}`.toLowerCase();

      const rowStatus = normalizeStatus(row.dataset.status || "");
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
