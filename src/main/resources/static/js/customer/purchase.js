import {
  applyCustomerVoucher,
  checkoutCustomer,
  getCustomerCartItems,
  getCustomerShippingFee,
  getCustomerVouchers,
  updateCustomerCartQty,
} from "../app/services/customerCartService.js";
import { toCurrency } from "../app/lib/helpers.js";
import {
  calcSubtotal,
  calcTotalQty,
} from "../app/features/customer/cart/cartMath.js";
import { renderPurchaseCartHtml } from "../app/features/customer/cart/cartTemplates.js";
import {
  getAppliedVoucherMessage,
  normalizeVoucherCode,
  renderVoucherOptions,
} from "../app/features/customer/voucher/voucherUtils.js";

document.addEventListener("DOMContentLoaded", () => {
  const customerId = document.body?.dataset.customerId || "";
  const defaultVoucherCode = (document.body?.dataset.voucherCode || "")
    .trim()
    .toUpperCase();
  const customerAddress = document.body?.dataset.customerAddress || "";

  const orderItems = document.getElementById("orderItems");
  const emptyCart = document.getElementById("emptyCart");
  const itemCountText = document.getElementById("itemCountText");
  const subtotalAmount = document.getElementById("subtotalAmount");
  const discountAmount = document.getElementById("discountAmount");
  const shippingAmount = document.getElementById("shippingAmount");
  const shippingInfo = document.getElementById("shippingInfo");
  const finalTotalAmount = document.getElementById("finalTotalAmount");
  const footerTotal = document.getElementById("footerTotal");
  const voucherSelect = document.getElementById("voucherSelect");
  const voucherMessage = document.getElementById("voucherMessage");
  const applyVoucherBtn = document.getElementById("applyVoucherBtn");
  const placeOrderBtn = document.getElementById("placeOrderBtn");
  const customerNoteInput = document.getElementById("customerNote");

  const pageParams = new URLSearchParams(window.location.search);
  const payError = (pageParams.get("payError") || "").trim();
  if (payError === "card_auth_failed") {
    alert("Sai thông tin thẻ hoặc sai mật khẩu.");
  }

  let cartItemsData = [];
  let appliedVoucher = null;
  let shippingFee = 0;
  let shippingDistanceKm = 0;
  let shippingNearestBranch = "";
  let shippingReady = false;

  const renderSummary = () => {
    const subtotal = calcSubtotal(cartItemsData);
    const discount = Number(appliedVoucher?.discount || 0);
    const finalTotal = Math.max(0, subtotal - discount) + shippingFee;

    subtotalAmount.textContent = toCurrency(subtotal);
    discountAmount.textContent = toCurrency(discount);
    if (shippingAmount) {
      shippingAmount.textContent = toCurrency(shippingFee);
    }
    if (shippingInfo) {
      if (shippingNearestBranch || shippingDistanceKm > 0) {
        const distanceText =
          shippingDistanceKm > 0
            ? `${shippingDistanceKm.toFixed(2)} km`
            : "không rõ";
        const branchText = shippingNearestBranch || "cơ sở gần nhất";
        shippingInfo.textContent = `Tính từ ${branchText} (${distanceText})`;
      } else if (shippingReady) {
        shippingInfo.textContent = "";
      }
    }
    finalTotalAmount.textContent = toCurrency(finalTotal);
    footerTotal.textContent = toCurrency(finalTotal);
  };

  const loadShippingFee = async () => {
    if (!customerAddress) {
      shippingFee = 0;
      shippingReady = false;
      if (shippingInfo) {
        shippingInfo.textContent = "Thiếu địa chỉ khách hàng để tính phí ship.";
      }
      renderSummary();
      return;
    }

    try {
      const data = await getCustomerShippingFee(customerId);
      shippingFee = Number(data.shippingFee || 0);
      shippingDistanceKm = Number(data.distanceKm || 0);
      shippingNearestBranch = String(data.nearestBranch || "");
      shippingReady = shippingFee > 0;
    } catch (error) {
      const message = error?.message || "Không thể tính phí ship.";
      shippingFee = 0;
      shippingDistanceKm = 0;
      shippingNearestBranch = "";
      shippingReady = false;
      if (shippingInfo) {
        shippingInfo.textContent = message;
      }
    }
    renderSummary();
  };

  const renderCart = () => {
    if (!Array.isArray(cartItemsData) || cartItemsData.length === 0) {
      orderItems.innerHTML = "";
      emptyCart.style.display = "block";
      itemCountText.textContent = "0 sản phẩm";
      appliedVoucher = null;
      voucherMessage.textContent = "";
      renderSummary();
      return;
    }

    emptyCart.style.display = "none";
    const totalQty = calcTotalQty(cartItemsData);
    itemCountText.textContent = `${totalQty} sản phẩm`;

    orderItems.innerHTML = renderPurchaseCartHtml(cartItemsData);

    renderSummary();
  };

  const loadVouchers = async () => {
    try {
      const vouchers = await getCustomerVouchers();
      renderVoucherOptions(voucherSelect, vouchers, "Không có mã khả dụng");
    } catch (error) {
      voucherSelect.innerHTML = `<option value="">Không tải được mã giảm giá</option>`;
      voucherSelect.disabled = true;
      console.error(error);
    }
  };

  const applyVoucher = async (codeInput = "", showAlert = true) => {
    const code = normalizeVoucherCode(codeInput || voucherSelect?.value || "");
    if (!code) {
      appliedVoucher = null;
      voucherMessage.textContent = "";
      renderSummary();
      return false;
    }

    try {
      const data = await applyCustomerVoucher({ customerId, code });
      appliedVoucher = {
        code: data.code,
        discount: Number(data.discount || 0),
      };
      voucherSelect.value = appliedVoucher.code;
      voucherMessage.textContent = getAppliedVoucherMessage(
        appliedVoucher.code,
        appliedVoucher.discount,
      );
      renderSummary();
      return true;
    } catch (error) {
      const text = error?.message || "Không áp dụng được mã giảm giá.";
      appliedVoucher = null;
      voucherMessage.textContent = text || "Không áp dụng được mã giảm giá.";
      renderSummary();
      if (showAlert) alert(text || "Không áp dụng được mã giảm giá.");
      return false;
    }
  };

  const loadCart = async () => {
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

  orderItems?.addEventListener("click", async (event) => {
    const qtyBtn = event.target.closest(".qty-btn");
    if (!qtyBtn) return;

    const productId = qtyBtn.getAttribute("data-product-id") || "";
    const size = qtyBtn.getAttribute("data-size") || "M";
    const currentQty = Number(qtyBtn.getAttribute("data-qty") || "1");
    const action = qtyBtn.getAttribute("data-action");
    const nextQty = action === "decrease" ? currentQty - 1 : currentQty + 1;

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
  });

  applyVoucherBtn?.addEventListener("click", async () => {
    await applyVoucher(voucherSelect?.value || "", true);
  });

  placeOrderBtn?.addEventListener("click", async () => {
    if (!customerId) {
      alert("Vui lòng đăng nhập lại.");
      return;
    }

    if (!Array.isArray(cartItemsData) || cartItemsData.length === 0) {
      alert("Giỏ hàng đang trống.");
      return;
    }

    if (!shippingReady) {
      alert("Chưa tính được phí ship. Vui lòng kiểm tra địa chỉ giao hàng.");
      return;
    }

    const paymentMethod =
      document.querySelector('input[name="paymentMethod"]:checked')?.value ||
      "COD";

    placeOrderBtn.disabled = true;
    try {
      const payload = await checkoutCustomer({
        customerId,
        voucherCode: appliedVoucher?.code || null,
        paymentMethod,
        address: customerAddress,
        customerNote: String(customerNoteInput?.value || "").trim(),
      });
      const isOnlinePayment =
        paymentMethod === "BANK" || paymentMethod === "CARD";
      if (isOnlinePayment) {
        const paymentUrl = String(payload?.paymentUrl || "").trim();
        if (!paymentUrl) {
          alert("Không tạo được liên kết thanh toán VNPAY.");
          return;
        }
        window.location.href = paymentUrl;
        return;
      }

      const orderId = String(payload?.orderId || "").trim();
      if (!orderId) {
        alert("Đặt hàng thành công nhưng thiếu mã đơn hàng.");
        return;
      }

      window.location.href = `/customer/order-success?kh=${encodeURIComponent(customerId)}&orderId=${encodeURIComponent(orderId)}&payment=COD`;
    } catch (error) {
      alert(error?.message || "Không thể đặt hàng.");
    } finally {
      placeOrderBtn.disabled = false;
    }
  });

  if (!customerId) {
    alert("Thiếu thông tin khách hàng.");
    window.location.href = "/customer/menu";
    return;
  }

  loadVouchers()
    .then(async () => {
      if (defaultVoucherCode) {
        voucherSelect.value = defaultVoucherCode;
        await applyVoucher(defaultVoucherCode, false);
      }
    })
    .catch(console.error);

  loadShippingFee().catch(console.error);
  loadCart().catch(console.error);
});
