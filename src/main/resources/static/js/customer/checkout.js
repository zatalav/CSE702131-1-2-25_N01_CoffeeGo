import {
  applyCustomerVoucher,
  getCustomerCartItems,
  getCustomerVouchers,
  removeCustomerCartItem,
  updateCustomerCartQty,
} from "../app/services/customerCartService.js";
import { toCurrency } from "../app/lib/helpers.js";
import {
  calcSubtotal,
  calcTotalQty,
} from "../app/features/customer/cart/cartMath.js";
import { renderCheckoutCartHtml } from "../app/features/customer/cart/cartTemplates.js";
import {
  getAppliedVoucherMessage,
  normalizeVoucherCode,
  renderVoucherOptions,
} from "../app/features/customer/voucher/voucherUtils.js";

document.addEventListener("DOMContentLoaded", () => {
  const customerId = document.body?.dataset.customerId || "";
  const orderItems = document.getElementById("orderItems");
  const emptyCart = document.getElementById("emptyCart");
  const cartCountText = document.getElementById("cartCountText");
  const subtotalAmount = document.getElementById("subtotalAmount");
  const discountAmount = document.getElementById("discountAmount");
  const finalTotalAmount = document.getElementById("finalTotalAmount");
  const voucherSelect = document.getElementById("voucherSelect");
  const voucherMessage = document.getElementById("voucherMessage");
  const applyVoucherBtn = document.getElementById("applyVoucherBtn");
  const submitOrder = document.getElementById("submitOrder");

  let cartItemsData = [];
  let appliedVoucher = null;

  const renderSummary = () => {
    const subtotal = calcSubtotal(cartItemsData);
    const discount = Number(appliedVoucher?.discount || 0);
    const finalTotal = Math.max(0, subtotal - discount);

    subtotalAmount.textContent = toCurrency(subtotal);
    discountAmount.textContent = toCurrency(discount);
    finalTotalAmount.textContent = toCurrency(finalTotal);
  };

  const renderCart = () => {
    if (!Array.isArray(cartItemsData) || cartItemsData.length === 0) {
      orderItems.innerHTML = "";
      if (emptyCart) emptyCart.style.display = "block";
      if (cartCountText) cartCountText.textContent = "0 sản phẩm";
      appliedVoucher = null;
      if (voucherMessage) voucherMessage.textContent = "";
      renderSummary();
      return;
    }

    if (emptyCart) emptyCart.style.display = "none";
    const totalQty = calcTotalQty(cartItemsData);
    if (cartCountText) {
      cartCountText.textContent = `${totalQty} sản phẩm`;
    }

    orderItems.innerHTML = renderCheckoutCartHtml(cartItemsData);

    renderSummary();
  };

  const loadVouchers = async () => {
    if (!voucherSelect) return;
    try {
      const vouchers = await getCustomerVouchers();
      renderVoucherOptions(
        voucherSelect,
        vouchers,
        "Bạn chưa có mã giảm giá khả dụng",
      );
    } catch (error) {
      voucherSelect.innerHTML = `<option value="">Không tải được mã giảm giá</option>`;
      voucherSelect.disabled = true;
      console.error(error);
    }
  };

  const loadCart = async () => {
    if (!customerId) return;
    try {
      cartItemsData = await getCustomerCartItems(customerId);
    } catch (error) {
      console.error(error);
      return;
    }

    renderCart();

    if (appliedVoucher?.code) {
      await applyVoucher(appliedVoucher.code, false);
    }
  };

  const applyVoucher = async (codeInput = "", showAlert = true) => {
    if (!customerId) return false;

    const code = normalizeVoucherCode(codeInput || voucherSelect?.value || "");
    if (!code) {
      appliedVoucher = null;
      if (voucherMessage) voucherMessage.textContent = "";
      renderSummary();
      return false;
    }

    try {
      const data = await applyCustomerVoucher({ customerId, code });
      appliedVoucher = {
        code: data.code,
        discount: Number(data.discount || 0),
      };
      if (voucherSelect) voucherSelect.value = appliedVoucher.code;
      if (voucherMessage) {
        voucherMessage.textContent = getAppliedVoucherMessage(
          appliedVoucher.code,
          appliedVoucher.discount,
        );
      }
      renderSummary();
      return true;
    } catch (error) {
      const text = error?.message || "Không áp dụng được mã giảm giá.";
      appliedVoucher = null;
      renderSummary();
      if (voucherMessage)
        voucherMessage.textContent = text || "Không áp dụng được mã giảm giá.";
      if (showAlert) alert(text || "Không áp dụng được mã giảm giá.");
      return false;
    }
  };

  applyVoucherBtn?.addEventListener("click", async () => {
    await applyVoucher(voucherSelect?.value || "", true);
  });

  orderItems?.addEventListener("click", async (event) => {
    if (!customerId) return;

    const qtyBtn = event.target.closest(".qty-adjust");
    if (qtyBtn) {
      const productId = qtyBtn.getAttribute("data-product-id") || "";
      const size = qtyBtn.getAttribute("data-size") || "M";
      const currentQty = Number(qtyBtn.getAttribute("data-qty") || "1");
      const action = qtyBtn.getAttribute("data-action");
      const nextQty = action === "decrease" ? currentQty - 1 : currentQty + 1;
      if (!productId) return;

      try {
        await updateCustomerCartQty({
          customerId,
          productId,
          size,
          qty: nextQty,
        });
      } catch (error) {
        const text = error?.message || "Không thể cập nhật số lượng.";
        alert(text || "Không thể cập nhật số lượng.");
        return;
      }

      await loadCart();
      return;
    }

    const removeBtn = event.target.closest(".remove-cart-item");
    if (removeBtn) {
      const productId = removeBtn.getAttribute("data-product-id") || "";
      const size = removeBtn.getAttribute("data-size") || "M";
      if (!productId) return;

      try {
        await removeCustomerCartItem({ customerId, productId, size });
      } catch (error) {
        const text = error?.message || "Không thể xóa sản phẩm khỏi giỏ.";
        alert(text || "Không thể xóa sản phẩm khỏi giỏ.");
        return;
      }

      await loadCart();
    }
  });

  submitOrder?.addEventListener("click", async () => {
    if (!customerId) {
      alert("Vui lòng đăng ký hoặc đăng nhập.");
      return;
    }

    if (!Array.isArray(cartItemsData) || cartItemsData.length === 0) {
      alert("Giỏ hàng đang trống.");
      return;
    }

    const voucherQuery = appliedVoucher?.code
      ? `&voucher=${encodeURIComponent(appliedVoucher.code)}`
      : "";
    window.location.href = `/customer/purchase?kh=${encodeURIComponent(customerId)}${voucherQuery}`;
  });

  loadVouchers().catch(console.error);
  loadCart().catch(console.error);
});
