const detailBody = document.getElementById("detailBody");
const addRowBtn = document.getElementById("addRowBtn");

let masterDataPromise = null;

async function ensureMasterDataLoaded() {
  if (
    Array.isArray(window.DS_NGUYEN_LIEU) &&
    window.DS_NGUYEN_LIEU.length > 0
  ) {
    return;
  }
  if (!masterDataPromise) {
    const masterDataUrl = window.KHO_MASTER_DATA_URL || "/kho/master-data";
    masterDataPromise = fetch(masterDataUrl)
      .then((res) => {
        const contentType = res.headers.get("content-type") || "";
        if (!res.ok || !contentType.includes("application/json")) {
          throw new Error("master-data response is not JSON");
        }
        return res.json();
      })
      .then((data) => {
        window.DS_NGUYEN_LIEU = data.dsNguyenLieuJs || [];
        window.DS_NHA_CUNG_CAP = data.dsNhaCungCapJs || [];
        window.MAP_NGUYENLIEU_NCC = data.mapNguyenLieuNcc || {};
        window.MAP_TENNGUYENLIEU_NCC = data.mapTenNguyenLieuNcc || {};
        window.MAP_NCC_TENNGUYENLIEU = data.mapNccTenNguyenLieu || {};
      })
      .catch(() => {
        window.DS_NGUYEN_LIEU = [];
        window.DS_NHA_CUNG_CAP = [];
        window.MAP_NGUYENLIEU_NCC = {};
        window.MAP_TENNGUYENLIEU_NCC = {};
        window.MAP_NCC_TENNGUYENLIEU = {};
      })
      .finally(() => {
        masterDataPromise = null;
      });
  }
  await masterDataPromise;
}

const getUsedSuppliersByName = () => {
  const used = {};
  document.querySelectorAll(".detail-row").forEach((row) => {
    const ingSelect = row.querySelector('select[name="nguyenLieuId[]"]');
    const supSelect = row.querySelector('select[name="nhacungcapId[]"]');
    const name =
      ingSelect?.selectedOptions[0]?.getAttribute("data-name") ||
      ingSelect?.selectedOptions[0]?.textContent?.trim();
    const sup = supSelect?.value;
    if (!name || !sup) return;
    const key = name.toLowerCase();
    if (!used[key]) used[key] = new Set();
    used[key].add(sup);
  });
  return used;
};

const buildOptions = (selectedName = "") => {
  if (!Array.isArray(window.DS_NGUYEN_LIEU)) return "";
  const usedSuppliers = getUsedSuppliersByName();
  const seen = new Set();

  return window.DS_NGUYEN_LIEU.filter((nl) => {
    const name = (nl.ten || "").trim();
    if (!name) return false;
    const key = name.toLowerCase();
    if (seen.has(key)) return false;

    const allowed = window.MAP_TENNGUYENLIEU_NCC?.[key] || [];
    const used = usedSuppliers[key] ? Array.from(usedSuppliers[key]) : [];
    const remaining = allowed.filter((id) => !used.includes(id));
    const isCurrent = selectedName && selectedName.toLowerCase() === key;
    // If there is no CungCap mapping for this ingredient yet, keep it selectable.
    if (allowed.length > 0 && remaining.length === 0 && !isCurrent)
      return false;

    seen.add(key);
    return true;
  })
    .map((nl) => {
      const donVi = nl.donVi ?? "";
      const giaNhap = nl.giaNhap ?? "";
      const ten = nl.ten ?? "";
      const selected =
        selectedName && ten.toLowerCase() === selectedName.toLowerCase()
          ? "selected"
          : "";
      return `<option value="${nl.id}" data-name="${ten}" data-donvi="${donVi}" data-gianhap="${giaNhap}" ${selected}>${ten}</option>`;
    })
    .join("");
};

const createRow = () => {
  const row = document.createElement("div");
  row.className = "detail-row";
  row.innerHTML = `
    <select name="nguyenLieuId[]" required>
      <option value="" disabled selected>- chọn -</option>
      ${buildOptions()}
    </select>
		<select name="nhacungcapId[]" required>
			<option value="" disabled selected>- chọn -</option>
		</select>
		<input type="text" name="donVi[]" placeholder="Đơn vị" readonly />
		<input type="number" name="soLuong[]" min="1" value="1" />
		<input type="number" name="giaNhap[]" min="0" step="100" value="0" />
    <input type="text" name="hanSuDung[]" placeholder="dd/MM/yyyy" pattern="[0-9]{2}/[0-9]{2}/[0-9]{4}" title="Nhập ngày theo định dạng dd/MM/yyyy" />
    <button type="button" class="icon-action danger">×</button>
	`;

  const select = row.querySelector('select[name="nguyenLieuId[]"]');
  const supplierSelect = row.querySelector('select[name="nhacungcapId[]"]');
  const donViInput = row.querySelector('input[name="donVi[]"]');
  const giaNhapInput = row.querySelector('input[name="giaNhap[]"]');
  const deleteBtn = row.querySelector(".icon-action.danger");

  select.addEventListener("change", (e) => {
    const option = e.target.selectedOptions[0];
    if (!option) return;
    const dv = option.getAttribute("data-donvi") || "";
    const gia = option.getAttribute("data-gianhap") || "0";
    donViInput.value = dv;
    giaNhapInput.value = gia;
    updateSupplierOptionsForRow(row);
    refreshAllSupplierOptions();
    refreshAllIngredientOptions();
  });

  supplierSelect.addEventListener("change", () => {
    refreshAllSupplierOptions();
  });

  deleteBtn.addEventListener("click", () => {
    row.remove();
    refreshAllSupplierOptions();
    refreshAllIngredientOptions();
  });

  updateSupplierOptionsForRow(row);
  return row;
};

document.addEventListener("DOMContentLoaded", async () => {
  await ensureMasterDataLoaded();

  if (detailBody && !detailBody.querySelector(".detail-row")) {
    detailBody.appendChild(createRow());
  }

  if (addRowBtn) {
    addRowBtn.addEventListener("click", () => {
      detailBody.appendChild(createRow());
    });
  }
});

function updateSupplierOptionsForRow(row) {
  const ingredientSelect = row.querySelector('select[name="nguyenLieuId[]"]');
  const supplierSelect = row.querySelector('select[name="nhacungcapId[]"]');
  if (!ingredientSelect || !supplierSelect) return;

  const name =
    ingredientSelect.selectedOptions[0]?.getAttribute("data-name") ||
    ingredientSelect.selectedOptions[0]?.textContent?.trim();
  const key = name ? name.toLowerCase() : null;
  const allowedByName = key ? window.MAP_TENNGUYENLIEU_NCC?.[key] : null;
  const allowedById = ingredientSelect.value
    ? window.MAP_NGUYENLIEU_NCC?.[ingredientSelect.value]
    : null;
  const allowedRaw =
    (Array.isArray(allowedById) && allowedById.length > 0
      ? allowedById
      : null) ||
    (Array.isArray(allowedByName) && allowedByName.length > 0
      ? allowedByName
      : null);
  const allowedIds = allowedRaw ? new Set(allowedRaw) : null;

  supplierSelect.disabled = false;

  const usedByOthers = new Set();
  document.querySelectorAll('select[name="nguyenLieuId[]"]').forEach((el) => {
    const rowEl = el.closest(".detail-row");
    if (!rowEl || rowEl === row) return;
    const rowName =
      el.selectedOptions[0]?.getAttribute("data-name") ||
      el.selectedOptions[0]?.textContent?.trim();
    if (rowName && rowName.toLowerCase() === key) {
      const supplierEl = rowEl.querySelector('select[name="nhacungcapId[]"]');
      if (supplierEl && supplierEl.value) {
        usedByOthers.add(supplierEl.value);
      }
    }
  });

  const currentValue = supplierSelect.value;
  supplierSelect.innerHTML = '<option value="" disabled>- chọn -</option>';

  const allSuppliers = Array.isArray(window.DS_NHA_CUNG_CAP)
    ? window.DS_NHA_CUNG_CAP
    : [];

  const filtered = allowedIds
    ? allSuppliers.filter((ncc) => allowedIds.has(ncc.id))
    : allSuppliers;

  filtered.forEach((ncc) => {
    if (usedByOthers.has(ncc.id)) return;
    const opt = document.createElement("option");
    opt.value = ncc.id;
    opt.textContent = ncc.ten;
    supplierSelect.appendChild(opt);
  });

  if (
    currentValue &&
    filtered.some((ncc) => ncc.id === currentValue) &&
    !usedByOthers.has(currentValue)
  ) {
    supplierSelect.value = currentValue;
  } else {
    supplierSelect.selectedIndex = 0;
  }
}

function updateIngredientOptionsForRow(row) {
  const ingredientSelect = row.querySelector('select[name="nguyenLieuId[]"]');
  if (!ingredientSelect) return;

  const currentName =
    ingredientSelect.selectedOptions[0]?.getAttribute("data-name") ||
    ingredientSelect.selectedOptions[0]?.textContent?.trim() ||
    "";

  ingredientSelect.innerHTML =
    '<option value="" disabled>- chọn -</option>' + buildOptions(currentName);

  if (currentName) {
    const options = Array.from(ingredientSelect.options);
    const match = options.find(
      (opt) =>
        (opt.getAttribute("data-name") || opt.textContent || "")
          .trim()
          .toLowerCase() === currentName.toLowerCase(),
    );
    if (match) {
      ingredientSelect.value = match.value;
    }
  }
}

function refreshAllSupplierOptions() {
  document.querySelectorAll(".detail-row").forEach((row) => {
    updateSupplierOptionsForRow(row);
  });
}

function refreshAllIngredientOptions() {
  document.querySelectorAll(".detail-row").forEach((row) => {
    updateIngredientOptionsForRow(row);
  });
}
