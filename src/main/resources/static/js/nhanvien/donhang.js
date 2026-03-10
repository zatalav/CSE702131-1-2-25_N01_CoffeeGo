import { toCurrency } from "../app/lib/helpers.js";

document.addEventListener("DOMContentLoaded", () => {
  const productList = document.getElementById("productList");
  const searchInput = document.getElementById("productSearch");
  const categoryFilter = document.getElementById("categoryFilter");
  const categoryPills = document.getElementById("categoryPills");
  const cartItemsEl = document.getElementById("cartItems");
  const emptyCartEl = document.getElementById("emptyCart");
  const subtotalEl = document.getElementById("subtotal");
  const discountEl = document.getElementById("discount");
  const totalEl = document.getElementById("total");
  const cartCountEl = document.getElementById("cartCount");
  const clearCartBtn = document.getElementById("clearCartBtn");
  const checkoutBtn = document.getElementById("checkoutBtn");
  const orderNoteEl = document.getElementById("orderNote");
  const menuBtn = document.getElementById("menuBtn");
  const menuDropdown = document.getElementById("menuDropdown");

  const showSuccess = (message) => {
    let notice = document.getElementById("orderSuccessNotice");
    if (!notice) {
      notice = document.createElement("div");
      notice.id = "orderSuccessNotice";
      notice.style.position = "fixed";
      notice.style.top = "20px";
      notice.style.right = "24px";
      notice.style.background = "#16a34a";
      notice.style.color = "#fff";
      notice.style.padding = "10px 16px";
      notice.style.borderRadius = "10px";
      notice.style.boxShadow = "0 10px 20px rgba(0,0,0,0.12)";
      notice.style.fontSize = "14px";
      notice.style.zIndex = "9999";
      document.body.appendChild(notice);
    }
    notice.textContent = message;
    notice.style.display = "block";

    window.clearTimeout(notice.dataset.timer);
    const timerId = window.setTimeout(() => {
      notice.style.display = "none";
    }, 5000);
    notice.dataset.timer = timerId;
  };

  const showError = (message) => {
    let notice = document.getElementById("orderErrorNotice");
    if (!notice) {
      notice = document.createElement("div");
      notice.id = "orderErrorNotice";
      notice.style.position = "fixed";
      notice.style.top = "20px";
      notice.style.right = "24px";
      notice.style.background = "#dc2626";
      notice.style.color = "#fff";
      notice.style.padding = "10px 16px";
      notice.style.borderRadius = "10px";
      notice.style.boxShadow = "0 10px 20px rgba(0,0,0,0.12)";
      notice.style.fontSize = "14px";
      notice.style.zIndex = "9999";
      document.body.appendChild(notice);
    }
    notice.textContent = message;
    notice.style.display = "block";

    window.clearTimeout(notice.dataset.timer);
    const timerId = window.setTimeout(() => {
      notice.style.display = "none";
    }, 5000);
    notice.dataset.timer = timerId;
  };

  if (menuBtn && menuDropdown) {
    menuBtn.addEventListener("click", (event) => {
      event.stopPropagation();
      const isOpen = menuDropdown.style.display === "block";
      menuDropdown.style.display = isOpen ? "none" : "block";
      menuBtn.setAttribute("aria-expanded", (!isOpen).toString());
    });

    document.addEventListener("click", () => {
      menuDropdown.style.display = "none";
      menuBtn.setAttribute("aria-expanded", "false");
    });

    menuDropdown.addEventListener("click", (event) => {
      event.stopPropagation();
    });
  }

  const cart = new Map();

  const formatCurrency = (value) => toCurrency(value);

  const getProducts = () =>
    Array.from(document.querySelectorAll(".product-card"));

  const applyFilters = () => {
    const keyword = searchInput.value.trim().toLowerCase();
    const category = categoryFilter.value.trim().toLowerCase();

    getProducts().forEach((card) => {
      const name = (card.dataset.name || "").toLowerCase();
      const cat = (card.dataset.category || "").toLowerCase();
      const matchKeyword = !keyword || name.includes(keyword);
      const matchCategory = !category || cat === category;

      card.style.display = matchKeyword && matchCategory ? "" : "none";
    });
  };

  const updateSummary = () => {
    let subtotal = 0;
    cart.forEach((item) => {
      subtotal += item.price * item.qty;
    });

    const discount = 0;
    const total = subtotal - discount;

    subtotalEl.textContent = formatCurrency(subtotal);
    discountEl.textContent = formatCurrency(discount);
    totalEl.textContent = formatCurrency(total);
    cartCountEl.textContent = cart.size;
  };

  const renderCart = () => {
    if (cart.size === 0) {
      cartItemsEl.innerHTML = "";
      emptyCartEl.style.display = "block";
      updateSummary();
      return;
    }

    emptyCartEl.style.display = "none";

    const html = Array.from(cart.values())
      .map(
        (item) => `
				<div class="cart-item" data-id="${item.id}">
					<div class="cart-item-header">
						<div class="cart-thumb">
							${
                item.image
                  ? `<img src="${item.image}" alt="${item.name}" />`
                  : `<div class="thumb-placeholder">No image</div>`
              }
						</div>
						<div class="cart-item-info">
							<strong>${item.name}</strong>
							<span>${formatCurrency(item.price)}</span>
						</div>
						<div class="qty-controls">
							<button class="qty-btn" data-action="decrease" type="button">-</button>
							<input class="qty-input" type="number" min="1" value="${item.qty}" />
							<button class="qty-btn" data-action="increase" type="button">+</button>
						</div>
					</div>
					<button class="custom-toggle" type="button" aria-expanded="${item.open}">
						Điều chỉnh yêu cầu khách ▾
					</button>
					<div class="customize-panel ${item.open ? "is-open" : ""}">
            <div class="custom-group">
              <p>Size</p>
              <div class="custom-buttons" data-field="size">
                ${["S", "M", "L", "XL"]
                  .map(
                    (value) =>
                      `<button type="button" class="${
                        value === item.size ? "active" : ""
                      }" data-value="${value}">${value}</button>`,
                  )
                  .join("")}
              </div>
            </div>
						<div class="custom-group">
							<p>Đường</p>
							<div class="custom-buttons" data-field="sugar">
								${["0%", "30%", "50%", "70%", "100%"]
                  .map(
                    (value) =>
                      `<button type="button" class="${
                        value === item.sugar ? "active" : ""
                      }" data-value="${value}">${value}</button>`,
                  )
                  .join("")}
							</div>
						</div>
						<div class="custom-group">
							<p>Đá</p>
							<div class="custom-buttons" data-field="ice">
								${["Không đá", "Ít", "Vừa", "Nhiều"]
                  .map(
                    (value) =>
                      `<button type="button" class="${
                        value === item.ice ? "active" : ""
                      }" data-value="${value}">${value}</button>`,
                  )
                  .join("")}
							</div>
						</div>
						<div class="custom-group">
							<p>Sữa</p>
							<div class="custom-buttons" data-field="milk">
								${["Ít sữa", "Thêm sữa"]
                  .map(
                    (value) =>
                      `<button type="button" class="${
                        value === item.milk ? "active" : ""
                      }" data-value="${value}">${value}</button>`,
                  )
                  .join("")}
							</div>
						</div>
						<textarea class="custom-note" rows="2" placeholder="Ghi chú...">${
              item.note
            }</textarea>
					</div>
					<div class="cart-item-actions">
						<button class="remove-btn" type="button">Xóa</button>
					</div>
				</div>
			`,
      )
      .join("");

    cartItemsEl.innerHTML = html;
    updateSummary();
  };

  const addToCart = (card) => {
    const id = card.dataset.id;
    const name = card.dataset.name || "";
    const price = Number(card.dataset.price) || 0;
    const image = card.dataset.image || card.querySelector("img")?.src || "";

    const existing = cart.get(id);
    if (existing) {
      existing.qty += 1;
    } else {
      cart.set(id, {
        id,
        name,
        price,
        image,
        qty: 1,
        size: "M",
        sugar: "50%",
        ice: "Ít",
        milk: "Ít sữa",
        note: "",
        open: false,
      });
    }

    renderCart();
  };

  searchInput?.addEventListener("input", applyFilters);
  categoryFilter?.addEventListener("change", applyFilters);

  categoryPills?.addEventListener("click", (event) => {
    const pill = event.target.closest(".pill");
    if (!pill) return;

    const value = pill.getAttribute("data-value") || "";
    categoryFilter.value = value;
    categoryPills
      .querySelectorAll(".pill")
      .forEach((el) => el.classList.remove("active"));
    pill.classList.add("active");
    applyFilters();
  });

  productList?.addEventListener("click", (event) => {
    const card = event.target.closest(".product-card");
    if (!card) return;

    addToCart(card);
  });

  cartItemsEl?.addEventListener("click", (event) => {
    const cartItem = event.target.closest(".cart-item");
    if (!cartItem) return;

    const id = cartItem.dataset.id;
    const item = cart.get(id);
    if (!item) return;

    if (event.target.closest(".qty-btn")) {
      const action = event.target.getAttribute("data-action");
      if (action === "increase") {
        item.qty += 1;
      } else if (action === "decrease") {
        item.qty = Math.max(1, item.qty - 1);
      }
      renderCart();
      return;
    }

    if (event.target.closest(".remove-btn")) {
      cart.delete(id);
      renderCart();
      return;
    }

    if (event.target.closest(".custom-toggle")) {
      item.open = !item.open;
      renderCart();
      return;
    }

    const customButton = event.target.closest(".custom-buttons button");
    if (customButton) {
      const group = customButton.closest(".custom-buttons");
      const field = group?.dataset.field;
      const value = customButton.getAttribute("data-value");
      if (field && value) {
        item[field] = value;
        renderCart();
      }
    }
  });

  cartItemsEl?.addEventListener("change", (event) => {
    const cartItem = event.target.closest(".cart-item");
    if (!cartItem) return;

    const id = cartItem.dataset.id;
    const item = cart.get(id);
    if (!item) return;

    if (event.target.classList.contains("qty-input")) {
      const value = Number(event.target.value);
      item.qty = Number.isNaN(value) || value < 1 ? 1 : value;
      renderCart();
      return;
    }

    return;
  });

  cartItemsEl?.addEventListener("input", (event) => {
    if (!event.target.classList.contains("custom-note")) return;

    const cartItem = event.target.closest(".cart-item");
    if (!cartItem) return;

    const id = cartItem.dataset.id;
    const item = cart.get(id);
    if (!item) return;

    item.note = event.target.value;
  });

  clearCartBtn?.addEventListener("click", () => {
    cart.clear();
    renderCart();
  });

  checkoutBtn?.addEventListener("click", async () => {
    if (cart.size === 0) {
      return;
    }

    const payload = {
      note: orderNoteEl?.value || "",
      items: Array.from(cart.values()).map((item) => ({
        id: item.id,
        name: item.name,
        price: item.price,
        qty: item.qty,
        size: item.size,
        sugar: item.sugar,
        ice: item.ice,
        milk: item.milk,
        note: item.note,
      })),
    };

    try {
      const response = await fetch("/nhanvien/donhang/pdf", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const text = await response.text();
        showError(text || "Hết nguyên liệu, không thể tạo đơn hàng.");
        return;
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      const timestamp = new Date()
        .toISOString()
        .replace(/[:.]/g, "-")
        .slice(0, 19);

      link.href = url;
      link.download = `don-hang-${timestamp}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      cart.clear();
      if (orderNoteEl) orderNoteEl.value = "";
      renderCart();
      showSuccess("Hoàn tất đơn hàng thành công!");
    } catch (error) {
      console.error(error);
    }
  });

  applyFilters();
  renderCart();
});
