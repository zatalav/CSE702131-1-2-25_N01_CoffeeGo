const searchInput = document.getElementById("searchInput");
const tableBody = document.getElementById("tableBody");
const searchForm = searchInput?.closest("form");
const exportBtn = document.getElementById("exportBtn");
const exportAlert = document.getElementById("exportAlert");

if (searchInput && searchForm && tableBody) {
  let searchTimer;
  searchInput.addEventListener("input", () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
      searchForm.requestSubmit();
    }, 450);
  });
}

if (exportBtn) {
  exportBtn.addEventListener("click", () => {
    const selected = Array.from(
      document.querySelectorAll(".select-row:checked"),
    ).map((input) => input.value);

    if (!selected.length) {
      showExportAlert("Vui lòng chọn phiếu cần xuất file.", "error");
      return;
    }

    fetch("/kho/xuatkho/export", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(selected),
    })
      .then((res) => {
        if (!res.ok) throw new Error("export_failed");
        return res.blob();
      })
      .then((blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `phieu-xuat-kho-${Date.now()}.pdf`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        showExportAlert("Xuất file thành công!", "success");
      })
      .catch(() => {
        showExportAlert("Xuất file thất bại!", "error");
      });
  });
}

function showExportAlert(message, type) {
  if (!exportAlert) return;
  exportAlert.textContent = message;
  exportAlert.classList.remove("alert-success", "alert-error");
  exportAlert.classList.add(
    type === "success" ? "alert-success" : "alert-error",
  );
  exportAlert.style.display = "block";
  setTimeout(() => {
    exportAlert.style.display = "none";
  }, 5000);
}

const viewModal = document.getElementById("viewModal");
const deleteModal = document.getElementById("deleteModal");

const closeViewModal = document.getElementById("closeViewModal");
const closeViewBtn = document.getElementById("closeViewBtn");
const closeDeleteModal = document.getElementById("closeDeleteModal");
const cancelDeleteBtn = document.getElementById("cancelDeleteBtn");

const viewMaPhieu = document.getElementById("viewMaPhieu");
const viewNgayXuat = document.getElementById("viewNgayXuat");
const viewNhanVien = document.getElementById("viewNhanVien");
const viewGhiChu = document.getElementById("viewGhiChu");
const viewXuatDetailBody = document.getElementById("viewXuatDetailBody");

const deleteMaPhieu = document.getElementById("deleteMaPhieu");
const deleteMaPhieuText = document.getElementById("deleteMaPhieuText");

document.querySelectorAll(".open-view").forEach((btn) => {
  btn.addEventListener("click", () => {
    if (!viewModal) return;
    if (viewMaPhieu) viewMaPhieu.textContent = btn.dataset.id || "";
    if (viewNgayXuat) viewNgayXuat.textContent = btn.dataset.ngay || "";
    if (viewNhanVien) viewNhanVien.textContent = btn.dataset.nv || "";
    if (viewGhiChu) viewGhiChu.textContent = btn.dataset.ghichu || "";
    if (viewXuatDetailBody) viewXuatDetailBody.innerHTML = "";
    viewModal.style.display = "flex";

    const maPhieu = btn.dataset.id || "";
    if (maPhieu && viewXuatDetailBody) {
      fetch(`/kho/xuatkho/detail?maPhieu=${encodeURIComponent(maPhieu)}`)
        .then((res) => (res.ok ? res.json() : []))
        .then((items) => {
          viewXuatDetailBody.innerHTML = (items || [])
            .map(
              (item) => `
                <tr>
                  <td>${item.tenNguyenLieu || ""}</td>
                  <td>${item.soLuong ?? 0}</td>
                  <td>${item.donVi || ""}</td>
                  <td>${item.coSo || ""}</td>
                </tr>
              `,
            )
            .join("");
        })
        .catch(() => {
          viewXuatDetailBody.innerHTML = "";
        });
    }
  });
});

document.querySelectorAll(".open-delete").forEach((btn) => {
  btn.addEventListener("click", () => {
    if (!deleteModal) return;
    if (deleteMaPhieu) deleteMaPhieu.value = btn.dataset.id || "";
    if (deleteMaPhieuText) deleteMaPhieuText.textContent = btn.dataset.id || "";
    deleteModal.style.display = "flex";
  });
});

const closeModal = (modal) => {
  if (modal) modal.style.display = "none";
};

closeViewModal?.addEventListener("click", () => closeModal(viewModal));
closeViewBtn?.addEventListener("click", () => closeModal(viewModal));
closeDeleteModal?.addEventListener("click", () => closeModal(deleteModal));
cancelDeleteBtn?.addEventListener("click", () => closeModal(deleteModal));

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
