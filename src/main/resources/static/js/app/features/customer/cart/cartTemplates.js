import { toCurrency } from "../../../lib/helpers.js";
import { getItemQty, getItemUnitPrice } from "./cartMath.js";

export function renderCheckoutCartHtml(items = []) {
  return items
    .map((item) => {
      const qty = getItemQty(item);
      const unitPrice = getItemUnitPrice(item);
      const lineTotal = unitPrice * qty;

      return `
        <div class="order-item">
          <div class="item-main">
            <strong>${item.name}</strong>
            <small>Size: ${item.size || "M"}</small>
          </div>
          <div class="item-actions">
            <button type="button" class="qty-adjust" data-action="decrease" data-qty="${qty}" data-product-id="${item.productId}" data-size="${item.size || "M"}">-</button>
            <span class="qty-value">${qty}</span>
            <button type="button" class="qty-adjust" data-action="increase" data-qty="${qty}" data-product-id="${item.productId}" data-size="${item.size || "M"}">+</button>
            <span class="line-total">${toCurrency(lineTotal)}</span>
            <button type="button" class="remove-cart-item" data-product-id="${item.productId}" data-size="${item.size || "M"}">Xóa</button>
          </div>
        </div>
      `;
    })
    .join("");
}

export function renderPurchaseCartHtml(items = []) {
  return items
    .map((item) => {
      const qty = getItemQty(item);
      const unitPrice = getItemUnitPrice(item);
      return `
        <div class="order-item">
          <div class="item-name">
            <strong>${item.name}</strong>
            <small>Size: ${item.size || "M"}</small>
          </div>
          <div>${toCurrency(unitPrice)}</div>
          <div class="qty-wrap">
            <button class="qty-btn" data-action="decrease" data-product-id="${item.productId}" data-size="${item.size || "M"}" data-qty="${qty}" type="button">-</button>
            <span>${qty}</span>
            <button class="qty-btn" data-action="increase" data-product-id="${item.productId}" data-size="${item.size || "M"}" data-qty="${qty}" type="button">+</button>
          </div>
          <strong>${toCurrency(unitPrice * qty)}</strong>
        </div>
      `;
    })
    .join("");
}
