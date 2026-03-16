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
  const cartFeedback = document.getElementById("cartFeedback");
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
  let voucherRefreshTimer = null;

  const showFeedback = (message = "") => {
    if (!cartFeedback) return;
    const text = String(message || "").trim();
    cartFeedback.textContent = text;
    cartFeedback.style.display = text ? "block" : "none";
  };

  const reportActionError = (actionName, fallbackMessage, error) => {
    const status = error?.status ? ` (HTTP ${error.status})` : "";
    const message =
      String(error?.message || "").trim() ||
      String(fallbackMessage || "Đã xảy ra lỗi.");
    showFeedback(`${fallbackMessage}${status}`);
    console.error(`[checkout] ${actionName} failed`, {
      action: actionName,
      status: error?.status || null,
      message,
      error,
    });
  };

  const scheduleVoucherRefresh = () => {
    if (!appliedVoucher?.code) return;
    if (voucherRefreshTimer) clearTimeout(voucherRefreshTimer);
    voucherRefreshTimer = setTimeout(() => {
      applyVoucher(appliedVoucher.code, false).catch(console.error);
      voucherRefreshTimer = null;
    }, 220);
  };

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
      showFeedback("");
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
    showFeedback("");

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
      reportActionError(
        "load-cart",
        "Không tải được giỏ hàng. Vui lòng thử lại.",
        error,
      );
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
      if (showAlert) {
        reportActionError(
          "apply-voucher",
          "Không áp dụng được mã giảm giá.",
          error,
        );
      }
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
        reportActionError(
          "update-quantity",
          "Không thể cập nhật số lượng sản phẩm.",
          error,
        );
        return;
      }

      const normalizedSize = String(size || "M")
        .trim()
        .toUpperCase();
      const itemIndex = cartItemsData.findIndex(
        (item) =>
          item?.productId === productId &&
          String(item?.size || "M")
            .trim()
            .toUpperCase() === normalizedSize,
      );

      if (itemIndex >= 0) {
        if (nextQty <= 0) {
          cartItemsData.splice(itemIndex, 1);
        } else {
          cartItemsData[itemIndex].qty = nextQty;
          cartItemsData[itemIndex].quantity = nextQty;
        }
        renderCart();
        scheduleVoucherRefresh();
      } else {
        await loadCart();
      }
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
        console.warn("[checkout] remove endpoint failed, fallback to qty=0", {
          productId,
          size,
          customerId,
          status: error?.status || null,
          message: error?.message || null,
        });
        try {
          await updateCustomerCartQty({
            customerId,
            productId,
            size,
            qty: 0,
          });
        } catch (fallbackError) {
          reportActionError(
            "remove-item",
            "Không thể xóa sản phẩm khỏi giỏ hàng.",
            fallbackError,
          );
          return;
        }
      }

      const normalizedSize = String(size || "M")
        .trim()
        .toUpperCase();
      const nextItems = cartItemsData.filter(
        (item) =>
          !(
            item?.productId === productId &&
            String(item?.size || "M")
              .trim()
              .toUpperCase() === normalizedSize
          ),
      );

      if (nextItems.length !== cartItemsData.length) {
        cartItemsData = nextItems;
        renderCart();
        scheduleVoucherRefresh();
      } else {
        await loadCart();
      }
    }
  });

  submitOrder?.addEventListener("click", async () => {
    if (!customerId) {
      showFeedback("Vui lòng đăng ký hoặc đăng nhập.");
      return;
    }

    if (!Array.isArray(cartItemsData) || cartItemsData.length === 0) {
      showFeedback("Giỏ hàng đang trống.");
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
