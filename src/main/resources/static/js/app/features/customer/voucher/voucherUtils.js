import { toCurrency } from "../../../lib/helpers.js";

export function normalizeVoucherCode(value = "") {
  return String(value).trim().toUpperCase();
}

export function renderVoucherOptions(selectEl, vouchers = [], emptyText) {
  if (!selectEl) return;

  selectEl.innerHTML = '<option value="">Chọn mã giảm giá</option>';

  if (!Array.isArray(vouchers) || vouchers.length === 0) {
    selectEl.innerHTML = `<option value="">${emptyText}</option>`;
    selectEl.disabled = true;
    return;
  }

  selectEl.disabled = false;
  vouchers.forEach((voucher) => {
    const option = document.createElement("option");
    option.value = normalizeVoucherCode(voucher.code || "");
    option.textContent = `${voucher.code} - Giảm ${voucher.discount}`;
    selectEl.appendChild(option);
  });
}

export function getAppliedVoucherMessage(code, discount) {
  return `Đã áp dụng mã ${code} (-${toCurrency(discount)})`;
}
