document.addEventListener("DOMContentLoaded", function () {
  window.AdminUiCommon?.initSidebarAndProfile?.();
  window.QuanlyPrefetch?.init?.();

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
  const setImage = (id, value) => {
    const el = document.getElementById(id);
    if (!el) return;
    const normalized =
      value && !/^https?:\/\//i.test(value) && !value.startsWith("/")
        ? `/img_nv/${value}`
        : value;
    if (value) {
      el.src = normalized;
      el.style.display = "block";
    } else {
      el.removeAttribute("src");
      el.style.display = "none";
    }
  };
  const setValue = (id, value) => {
    const el = document.getElementById(id);
    if (el) el.value = value ?? "";
  };

  const normalizeVnDateInput = (value) => {
    const raw = (value || "").trim();
    if (!raw) return "";
    if (/^\d{8}$/.test(raw)) {
      return `${raw.slice(0, 2)}/${raw.slice(2, 4)}/${raw.slice(4)}`;
    }
    return raw;
  };

  const bindVnDateAutoFormat = (selector) => {
    document.querySelectorAll(selector).forEach((input) => {
      input.addEventListener("blur", () => {
        input.value = normalizeVnDateInput(input.value);
      });
    });
  };

  bindVnDateAutoFormat("#ngaySinh, #editNgaySinh");

  const cloudName = (window.CLOUDINARY_CLOUD_NAME || "").trim();
  const uploadPreset = (window.CLOUDINARY_UPLOAD_PRESET || "").trim();

  const uploadImageToCloudinary = async (file, folder = "nhanvien") => {
    if (!cloudName || !uploadPreset) {
      throw new Error("Thiếu cấu hình Cloudinary");
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", uploadPreset);
    formData.append("folder", folder);

    const response = await fetch(
      `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`,
      {
        method: "POST",
        body: formData,
      },
    );

    const data = await response.json();
    if (!response.ok || !data.secure_url) {
      throw new Error(data.error?.message || "Upload ảnh thất bại");
    }
    return data.secure_url;
  };

  /* =====================================================
     ADD MODAL
  ===================================================== */
  const addBtn = document.getElementById("addBtn");
  const addModal = document.getElementById("addStaffModal");
  const closeAdd = document.getElementById("closeModal");
  const cancelAdd = document.getElementById("cancelBtn");

  if (addBtn && addModal) {
    addBtn.onclick = () => (addModal.style.display = "flex");
    if (closeAdd) closeAdd.onclick = () => (addModal.style.display = "none");
    if (cancelAdd) cancelAdd.onclick = () => (addModal.style.display = "none");
  }

  /* =====================================================
     VIEW MODAL
  ===================================================== */
  const viewModal = document.getElementById("viewModal");
  const viewId = document.getElementById("viewId");
  const viewTen = document.getElementById("viewTen");
  const viewSdt = document.getElementById("viewSdt");
  const viewEmail = document.getElementById("viewEmail");
  const viewDiaChi = document.getElementById("viewDiaChi");
  const closeViewModal = document.getElementById("closeViewModal");
  const closeViewBtn = document.getElementById("closeViewBtn");

  if (viewModal) {
    document.querySelectorAll(".open-view").forEach((btn) => {
      btn.onclick = () => {
        setText("viewId", btn.dataset.id);
        setText("viewTen", btn.dataset.ten);
        setText("viewSdt", btn.dataset.sdt);
        setText("viewEmail", btn.dataset.email);
        setText("viewDiaChi", btn.dataset.diachi);
        setText("viewChucVu", btn.dataset.chucvu);
        setText("viewGioiTinh", btn.dataset.gioitinh);
        setText("viewNgaySinh", btn.dataset.ngaysinh);
        setText("viewCccd", btn.dataset.cccd);
        setText("viewCoso", btn.dataset.coso);
        setImage("viewImgNv", btn.dataset.img);
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

  /* =====================================================
     EDIT MODAL
  ===================================================== */
  const editModal = document.getElementById("editModal");
  const editId = document.getElementById("editId");
  const editTen = document.getElementById("editTen");
  const editSdt = document.getElementById("editSdt");
  const editEmail = document.getElementById("editEmail");
  const editDiaChi = document.getElementById("editDiaChi");
  const closeEdit = document.getElementById("closeEditModal");
  const cancelEdit = document.getElementById("cancelEditBtn");

  if (editModal) {
    document.querySelectorAll(".open-edit").forEach((btn) => {
      btn.onclick = () => {
        setValue("editId", btn.dataset.id);
        setValue("editTen", btn.dataset.ten);
        setValue("editSdt", btn.dataset.sdt);
        setValue("editEmail", btn.dataset.email);
        setValue("editDiaChi", btn.dataset.diachi);
        setValue("editChucVu", btn.dataset.chucvu);
        setValue("editGioiTinh", btn.dataset.gioitinh);
        setValue("editNgaySinh", btn.dataset.ngaysinh);
        setValue("editCccd", btn.dataset.cccd);
        setValue("editPassword", btn.dataset.password);
        setValue("editImgNv", btn.dataset.img);
        setImage("editImgPreview", btn.dataset.img);
        setValue("editCosoId", btn.dataset.cosoid);
        setValue("editDonVi", btn.dataset.donvi);
        setValue("editGiaNhap", btn.dataset.gianhap);
        setValue("editNhaCungCapId", btn.dataset.nhacungcapid);
        setValue("editDanhMucId", btn.dataset.danhmucid);
        setValue("editTrangThai", btn.dataset.trangthai);
        setValue("editSlTon", btn.dataset.slton);
        toggleCosoField(btn.dataset.chucvu, editCoso, editCosoGroup);
        editModal.style.display = "flex";
      };
    });

    if (closeEdit) closeEdit.onclick = () => (editModal.style.display = "none");
    if (cancelEdit)
      cancelEdit.onclick = () => (editModal.style.display = "none");
  }

  /* =====================================================
     DELETE MODAL
  ===================================================== */
  const deleteModal = document.getElementById("deleteModal");
  const deleteId = document.getElementById("deleteId");
  const deleteTen = document.getElementById("deleteTen");
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

  /* =====================================================
     CLICK NGOÀI MODAL ĐỂ ĐÓNG
  ===================================================== */
  document.querySelectorAll(".modal-overlay").forEach((overlay) => {
    overlay.addEventListener("click", (e) => {
      if (e.target === overlay) {
        overlay.style.display = "none";
      }
    });
  });

  /* =====================================================
     ESC ĐÓNG TẤT CẢ MODAL
  ===================================================== */
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      document.querySelectorAll(".modal-overlay").forEach((modal) => {
        modal.style.display = "none";
      });
    }
  });

  /* =====================================================
     SEARCH FILTER (CASE-INSENSITIVE)
  ===================================================== */
  const searchInput = document.getElementById("categorySearch");
  const searchBtn = document.getElementById("categorySearchBtn");
  const tableBody = document.querySelector(".table-list tbody");

  const applySearchFilter = () => {
    if (!tableBody || !searchInput) return;
    const term = (searchInput.value || "").trim().toLowerCase();

    Array.from(tableBody.querySelectorAll("tr")).forEach((row) => {
      const rowText = Array.from(row.querySelectorAll("td"))
        .map((td) => td.textContent || "")
        .join(" ")
        .toLowerCase();

      row.style.display = term === "" || rowText.includes(term) ? "" : "none";
    });
  };

  if (searchInput) searchInput.addEventListener("input", applySearchFilter);
  if (searchBtn)
    searchBtn.addEventListener("click", (e) => {
      e.preventDefault();
      applySearchFilter();
      searchInput?.focus();
    });

  /* =====================================================
     WAREHOUSE FILTER (SEARCH + TYPE)
  ===================================================== */
  const warehouseSearchInput = document.getElementById("warehouseSearch");
  const warehouseSearchBtn = document.getElementById("warehouseSearchBtn");
  const warehouseTableBody = document.getElementById("warehouseTableBody");
  const viewMode = document.getElementById("viewMode");

  const normalizeText = (value) =>
    (value || "")
      .toString()
      .trim()
      .toLowerCase()
      .normalize("NFD")
      .replace(/\p{Diacritic}/gu, "");

  const applyWarehouseFilters = () => {
    if (!warehouseTableBody) return;
    const term = normalizeText(warehouseSearchInput?.value || "");
    const mode = viewMode?.value || "";
    const modeText =
      mode === "grid" ? "nhap kho" : mode === "list" ? "xuat kho" : "";

    Array.from(warehouseTableBody.querySelectorAll("tr")).forEach((row) => {
      const cells = row.querySelectorAll("td");
      if (!cells.length) return;

      const rowText = normalizeText(
        Array.from(cells)
          .map((td) => td.textContent || "")
          .join(" "),
      );
      const loaiPhieu = normalizeText(cells[1]?.textContent || "");

      const matchesSearch = term === "" || rowText.includes(term);
      const matchesMode = modeText === "" || loaiPhieu.includes(modeText);

      row.style.display = matchesSearch && matchesMode ? "" : "none";
    });
  };

  if (warehouseSearchInput)
    warehouseSearchInput.addEventListener("input", applyWarehouseFilters);
  if (warehouseSearchBtn)
    warehouseSearchBtn.addEventListener("click", (e) => {
      e.preventDefault();
      applyWarehouseFilters();
      warehouseSearchInput?.focus();
    });
  if (viewMode) viewMode.addEventListener("change", applyWarehouseFilters);

  /* =====================================================
     CƠ SỞ THEO CHỨC VỤ (chỉ bắt buộc NV phục vụ)
  ===================================================== */
  const toggleCosoField = (chucVuValue, cosoSelect, cosoGroup) => {
    if (!cosoSelect) return;
    const role = (chucVuValue || "").toLowerCase();
    const isPhucVu = role.includes("phục vụ") || role.includes("phuc vu");

    if (isPhucVu) {
      cosoSelect.disabled = false;
      cosoSelect.required = true;
      if (cosoGroup) cosoGroup.style.opacity = "1";
    } else {
      cosoSelect.required = false;
      cosoSelect.value = "";
      cosoSelect.disabled = true;
      if (cosoGroup) cosoGroup.style.opacity = "0.7";
    }
  };

  const addChucVu = document.getElementById("addChucVu");
  const addCoso = document.getElementById("addCosoId");
  const addCosoGroup = document.getElementById("addCosoGroup");
  if (addChucVu) {
    toggleCosoField(addChucVu.value, addCoso, addCosoGroup);
    addChucVu.addEventListener("change", (e) => {
      toggleCosoField(e.target.value, addCoso, addCosoGroup);
    });
  }

  const editChucVu = document.getElementById("editChucVu");
  const editCoso = document.getElementById("editCosoId");
  const editCosoGroup = document.getElementById("editCosoGroup");
  if (editChucVu) {
    toggleCosoField(editChucVu.value, editCoso, editCosoGroup);
    editChucVu.addEventListener("change", (e) => {
      toggleCosoField(e.target.value, editCoso, editCosoGroup);
    });
  }

  const bindPreview = (fileInputId, previewId) => {
    const fileInput = document.getElementById(fileInputId);
    const preview = document.getElementById(previewId);
    if (!fileInput || !preview) return;

    fileInput.addEventListener("change", (event) => {
      const file = event.target.files?.[0];
      if (!file) return;
      preview.src = URL.createObjectURL(file);
      preview.style.display = "block";
    });
  };

  bindPreview("addImgNvFile", "addImgPreview");
  bindPreview("editImgNvFile", "editImgPreview");

  const bindDirectUploadSubmit = (
    formId,
    fileInputId,
    urlInputId,
    statusId,
  ) => {
    const form = document.getElementById(formId);
    const fileInput = document.getElementById(fileInputId);
    const urlInput = document.getElementById(urlInputId);
    const statusEl = document.getElementById(statusId);
    if (!form || !fileInput || !urlInput) return;

    let pendingUploadPromise = null;

    fileInput.addEventListener("change", () => {
      const file = fileInput.files?.[0];
      pendingUploadPromise = null;
      if (!file) {
        if (statusEl) statusEl.textContent = "";
        return;
      }

      // Reset hidden URL so it is always synced with the newest selected image.
      urlInput.value = "";
      if (statusEl) {
        statusEl.style.color = "#0f766e";
        statusEl.textContent = "Đang tải ảnh lên Cloudinary...";
      }
      pendingUploadPromise = uploadImageToCloudinary(file, "nhanvien")
        .then((imageUrl) => {
          urlInput.value = imageUrl || "";
          if (statusEl) {
            statusEl.style.color = "#15803d";
            statusEl.textContent = "Đã tải ảnh xong";
          }
          return imageUrl;
        })
        .catch((error) => {
          pendingUploadPromise = null;
          if (statusEl) {
            statusEl.style.color = "#b91c1c";
            statusEl.textContent = "Tải ảnh thất bại";
          }
          throw error;
        });
    });

    form.addEventListener("submit", async (event) => {
      const file = fileInput.files?.[0];
      if (!file) return;

      event.preventDefault();
      try {
        if (!urlInput.value) {
          if (pendingUploadPromise) {
            const imageUrl = await pendingUploadPromise;
            urlInput.value = imageUrl || "";
          } else {
            if (statusEl) {
              statusEl.style.color = "#0f766e";
              statusEl.textContent = "Đang tải ảnh lên Cloudinary...";
            }
            const imageUrl = await uploadImageToCloudinary(file, "nhanvien");
            urlInput.value = imageUrl;
            if (statusEl) {
              statusEl.style.color = "#15803d";
              statusEl.textContent = "Đã tải ảnh xong";
            }
          }
        }
        form.submit();
      } catch (error) {
        if (statusEl) {
          statusEl.style.color = "#b91c1c";
          statusEl.textContent = "Tải ảnh thất bại";
        }
        alert(error?.message || "Không thể tải ảnh lên Cloudinary");
      }
    });
  };

  bindDirectUploadSubmit(
    "addStaffForm",
    "addImgNvFile",
    "addImgNv",
    "addImgUploadStatus",
  );
  bindDirectUploadSubmit(
    "editStaffForm",
    "editImgNvFile",
    "editImgNv",
    "editImgUploadStatus",
  );
});
