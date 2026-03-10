/* ================= HELPER ================= */
function closeModal(btnId, modal) {
  const btn = document.getElementById(btnId);
  if (btn && modal) btn.onclick = () => (modal.style.display = "none");
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.innerText = value ?? "";
}

function setValue(id, value) {
  const el = document.getElementById(id);
  if (el) el.value = value ?? "";
}

function formatTrangThai(value) {
  if (value === "HOẠT ĐỘNG") return "Hoạt động";
  if (value === "VÔ HIỆU") return "Vô hiệu";
  return value ?? "";
}

async function uploadImageToCloudinary(file) {
  const cloudName = (window.CLOUDINARY_CLOUD_NAME || "").trim();
  const uploadPreset = (
    window.CLOUDINARY_UPLOAD_PRESET || "coffee_upload"
  ).trim();

  if (!cloudName) {
    throw new Error("Thiếu cloud name Cloudinary");
  }

  const endpoint = `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`;
  const formData = new FormData();
  formData.append("file", file);
  formData.append("upload_preset", uploadPreset);

  const response = await fetch(endpoint, {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(`Upload Cloudinary thất bại: ${errorBody}`);
  }

  const result = await response.json();
  return result.secure_url || "";
}

function bindDirectCloudinaryUpload({
  formId,
  fileInputId,
  hiddenUrlId,
  statusId,
}) {
  const form = document.getElementById(formId);
  const fileInput = document.getElementById(fileInputId);
  const hiddenUrl = document.getElementById(hiddenUrlId);
  const statusEl = document.getElementById(statusId);
  if (!form || !fileInput || !hiddenUrl) return;

  let submitting = false;
  form.addEventListener("submit", async (e) => {
    if (submitting) return;

    const file = fileInput.files?.[0];
    if (!file) return;

    e.preventDefault();
    try {
      if (statusEl) {
        statusEl.style.color = "#0f766e";
        statusEl.textContent = "Đang upload ảnh lên Cloudinary...";
      }

      const imageUrl = await uploadImageToCloudinary(file);
      if (!imageUrl) {
        throw new Error("Không nhận được URL ảnh từ Cloudinary");
      }

      hiddenUrl.value = imageUrl;
      fileInput.value = "";

      if (statusEl) {
        statusEl.style.color = "#15803d";
        statusEl.textContent = "Upload ảnh thành công";
      }

      submitting = true;
      form.submit();
    } catch (error) {
      console.error(error);
      if (statusEl) {
        statusEl.style.color = "#b91c1c";
        statusEl.textContent = `Upload ảnh thất bại: ${error?.message || "Unknown error"}`;
      }
      alert(
        `Upload ảnh lên Cloudinary thất bại. ${error?.message || "Vui lòng thử lại."}`,
      );
    }
  });
}

function createVariantRow(size = "", price = "") {
  const normalizedSize = (size || "").toString().trim().toUpperCase();
  const options = [
    `<option value="" ${!normalizedSize ? "selected" : ""} disabled>-- chọn size --</option>`,
    ...["M", "L", "XL"].map(
      (value) =>
        `<option value="${value}" ${
          normalizedSize === value ? "selected" : ""
        }>${value}</option>`,
    ),
  ]
    .map((value) => value)
    .join("");

  const tr = document.createElement("tr");
  tr.innerHTML = `
    <td>
      <select name="size[]" required>
        ${options}
      </select>
    </td>

    <td>
      <input type="number" name="price[]" min="0" step="10" placeholder="Nhập giá" value="${price}" required>
    </td>

    <td class="center">
      <button type="button" class="btn-delete-row">×</button>
    </td>
  `;
  return tr;
}

/* ===== CREATE RECIPE ROW (FIX KEY) ===== */
function createRecipeRow(nlId = "", soLuong = "", donVi = "kg") {
  const tr = document.createElement("tr");
  // support different key names from server/template (id/nguyenlieuId and ten/tenNguyenLieu)
  const optionsHtml = Array.isArray(window.DS_NGUYEN_LIEU)
    ? window.DS_NGUYEN_LIEU.map((nl) => {
        const nid =
          nl.id ?? nl.nguyenlieuId ?? nl.nguyenLieuId ?? nl.nguyenlieu_Id;
        const nten =
          nl.ten ?? nl.tenNguyenLieu ?? nl.ten_nguyen_lieu ?? nl.name;
        const selected = nid == nlId ? "selected" : "";
        return `<option value="${nid}" ${selected}>${nten ?? "(không tên)"}</option>`;
      }).join("")
    : "";

  const dv = (donVi || "").toString().toLowerCase();
  const unitOptions = [
    { v: "kg", t: "kg" },
    { v: "lít", t: "lít" },
    { v: "túi", t: "túi" },
    { v: "hộp", t: "hộp" },
  ]
    .map((u) => {
      const is = dv === u.v || (u.v === "lít" && dv === "lit");
      return `<option value="${u.v}" ${is ? "selected" : ""}>${u.t}</option>`;
    })
    .join("");

  tr.innerHTML = `
    <td>
      <select name="nguyenLieuId[]" required>
        <option value="" disabled ${!nlId ? "selected" : ""}>- chọn -</option>
        ${optionsHtml}
      </select>
    </td>

    <td>
      <input type="number" name="soLuong[]" min="0" step="0.001"
        value="${soLuong}" required>
    </td>

    <td>
      <select name="donVi[]">
        ${unitOptions}
      </select>
    </td>

    <td>
      <button type="button" class="btn-delete-row">×</button>
    </td>
  `;
  return tr;
}

let ingredientLoaderPromise = null;
async function ensureIngredientOptionsLoaded() {
  if (
    Array.isArray(window.DS_NGUYEN_LIEU) &&
    window.DS_NGUYEN_LIEU.length > 0
  ) {
    return;
  }
  if (!ingredientLoaderPromise) {
    ingredientLoaderPromise = fetch("/admin/sanpham/ingredients-lite")
      .then((res) => (res.ok ? res.json() : []))
      .then((items) => {
        window.DS_NGUYEN_LIEU = Array.isArray(items) ? items : [];
      })
      .catch(() => {
        window.DS_NGUYEN_LIEU = [];
      })
      .finally(() => {
        ingredientLoaderPromise = null;
      });
  }
  await ingredientLoaderPromise;
}

const addRecipeBody = document.getElementById("addRecipeBody");
if (addRecipeBody) {
  addRecipeBody.innerHTML = "";
}

const addVariantBody = document.getElementById("addVariantBody");
if (addVariantBody) {
  addVariantBody.innerHTML = "";
}

/* ================= MAIN ================= */
document.addEventListener("DOMContentLoaded", () => {
  console.log("Quanlysanpham.js loaded");

  const alerts = document.querySelectorAll(".alert");
  if (alerts.length) {
    setTimeout(() => {
      alerts.forEach((alert) => {
        alert.style.display = "none";
      });
    }, 5000);
  }

  window.AdminUiCommon?.initSidebarAndProfile?.();

  /* ===== ADD ===== */
  const addBtn = document.getElementById("addBtn");
  const addModal = document.getElementById("addModal");

  if (addBtn && addModal)
    addBtn.onclick = async () => {
      await ensureIngredientOptionsLoaded();
      if (addRecipeBody && !addRecipeBody.querySelector("tr")) {
        addRecipeBody.appendChild(createRecipeRow());
      }
      addModal.style.display = "flex";
    };
  closeModal("closeModal", addModal);
  closeModal("cancelBtnFooter", addModal);

  /* ===== FILTER (SEARCH + STATUS) ===== */
  const searchInput = document.getElementById("categorySearch");
  const statusFilter = document.getElementById("statusFilter");
  const tableBody = document.querySelector(".table-list tbody");

  const applyFilters = () => {
    if (!tableBody) return;
    const term = (searchInput?.value || "").trim().toLowerCase();
    const statusVal = (statusFilter?.value || "").trim().toUpperCase();

    Array.from(tableBody.querySelectorAll("tr")).forEach((row) => {
      const idText = row.querySelector("td:nth-child(2)")?.textContent || "";
      const nameText = row.querySelector("td:nth-child(3)")?.textContent || "";
      const haystack = `${idText} ${nameText}`.toLowerCase();

      const rowStatusRaw = row.dataset.status || "";
      const rowStatus = rowStatusRaw.toString().trim().toUpperCase();

      const matchText = term === "" || haystack.includes(term);
      const matchStatus = statusVal === "" || rowStatus === statusVal;

      row.style.display = matchText && matchStatus ? "" : "none";
    });
  };

  if (searchInput) searchInput.addEventListener("input", applyFilters);
  if (statusFilter) statusFilter.addEventListener("change", applyFilters);

  /* ===== VIEW ===== */
  const viewModal = document.getElementById("viewModal");

  document.querySelectorAll(".open-view").forEach((btn) => {
    btn.onclick = () => {
      setText("viewId", btn.dataset.id);
      setText("viewTen", btn.dataset.ten);
      setText("viewGia", btn.dataset.gia);
      setText("viewVariants", btn.dataset.variants || "-");
      setText("viewTrangThai", formatTrangThai(btn.dataset.trangthai));
      setText("viewMoTa", btn.dataset.mota);
      viewModal.style.display = "flex";
    };
  });

  closeModal("closeViewModal", viewModal);
  closeModal("closeViewBtn", viewModal);

  /* ===== EDIT ===== */
  const editModal = document.getElementById("editModal");
  const editRecipeBody = document.getElementById("editRecipeBody");
  const editVariantBody = document.getElementById("editVariantBody");

  document.querySelectorAll(".open-edit").forEach((btn) => {
    btn.onclick = async () => {
      const id = btn.dataset.id;

      try {
        await ensureIngredientOptionsLoaded();
        const res = await fetch(`/admin/sanpham/edit?id=${id}`);
        const sp = await res.json();

        setValue("editId", sp.id);
        setValue("editTen", sp.ten);
        setValue("editMoTa", sp.moTa);
        setValue("editImageUrl", sp.hinhAnh || "");
        // Only allow activation statuses for products: HOẠT ĐỘNG / VÔ HIỆU
        const editTrangEl = document.getElementById("editTrangThai");
        if (editTrangEl) {
          const raw = (sp.trangThai || "").toString().trim();
          const dbVal = raw.toUpperCase();
          if (dbVal === "HOẠT ĐỘNG" || dbVal === "VÔ HIỆU") {
            editTrangEl.value = dbVal;
          } else {
            // default to HOẠT ĐỘNG when DB value is not one of allowed
            editTrangEl.value = "HOẠT ĐỘNG";
          }
        }
        setValue("editDanhMuc", sp.danhmucId); // ⭐ FIX

        editRecipeBody.innerHTML = "";

        if (Array.isArray(sp.congThucs) && sp.congThucs.length > 0) {
          sp.congThucs.forEach((ct) => {
            editRecipeBody.appendChild(
              createRecipeRow(ct.nguyenLieuId ?? ct.id, ct.soLuong, ct.donVi),
            );
          });
        } else {
          // ensure there's at least one empty row like the add form
          editRecipeBody.appendChild(createRecipeRow());
        }

        if (editVariantBody) {
          editVariantBody.innerHTML = "";
          if (Array.isArray(sp.variants) && sp.variants.length > 0) {
            sp.variants.forEach((variant) => {
              editVariantBody.appendChild(
                createVariantRow(variant.size ?? "", variant.price ?? ""),
              );
            });
          } else {
            editVariantBody.appendChild(createVariantRow("M", sp.gia ?? ""));
          }
        }

        editModal.style.display = "flex";
      } catch (err) {
        console.error("Lỗi load sản phẩm:", err);
      }
    };
  });

  closeModal("closeEditModal", editModal);
  closeModal("cancelEditBtn", editModal);

  /* ===== DELETE ===== */
  const deleteModal = document.getElementById("deleteModal");

  document.querySelectorAll(".open-delete").forEach((btn) => {
    btn.onclick = () => {
      setValue("deleteId", btn.dataset.id);
      setText("deleteTen", btn.dataset.ten);
      deleteModal.style.display = "flex";
    };
  });

  closeModal("closeDeleteModal", deleteModal);
  closeModal("cancelDeleteBtn", deleteModal);

  /* ===== ADD / DELETE ROW ===== */
  document.addEventListener("click", (e) => {
    const addBtn = e.target.closest?.(".btn-add-row") || null;
    if (addBtn) {
      const tbody =
        addBtn.closest(".form-group")?.querySelector("tbody") ||
        addBtn.closest("table")?.querySelector("tbody") ||
        addBtn.closest(".modal")?.querySelector("tbody");
      if (tbody) {
        if (
          addBtn.id === "addVariantRowAdd" ||
          addBtn.id === "addVariantRowEdit"
        ) {
          tbody.appendChild(createVariantRow());
        } else {
          tbody.appendChild(createRecipeRow());
        }
      }
      return;
    }

    const delBtn = e.target.closest?.(".btn-delete-row") || null;
    if (delBtn) {
      delBtn.closest("tr")?.remove();
      return;
    }
  });

  /* ===== CLICK OUTSIDE ===== */
  document.querySelectorAll(".modal-overlay").forEach((overlay) => {
    overlay.onclick = (e) => {
      if (e.target === overlay) overlay.style.display = "none";
    };
  });

  bindDirectCloudinaryUpload({
    formId: "addProductForm",
    fileInputId: "addFileInput",
    hiddenUrlId: "addImageUrl",
    statusId: "addUploadStatus",
  });

  bindDirectCloudinaryUpload({
    formId: "editProductForm",
    fileInputId: "editFileInput",
    hiddenUrlId: "editImageUrl",
    statusId: "editUploadStatus",
  });
});
