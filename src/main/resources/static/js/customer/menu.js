import {
  addCustomerCartItem,
  getCustomerCartCount,
} from "../app/services/customerCartService.js";
import { toCurrency } from "../app/lib/helpers.js";
import { showToast } from "../app/core/ui/toast.js";
import {
  normalizeProductImage,
  parseVariantsFromCard,
  resolveCustomerId,
} from "../app/features/customer/menu/menuUtils.js";

window.__menuProfileReady = true;

document.addEventListener("DOMContentLoaded", () => {
  const searchInput = document.getElementById("productSearch");
  const productList = document.getElementById("productList");
  const pills = Array.from(document.querySelectorAll(".pill"));
  const cartCount = document.getElementById("cartCount");
  const cartLink = document.getElementById("cartLink");
  const profileBtn = document.getElementById("profileBtn");
  const profileMenu = document.getElementById("profileMenu");

  const customerId = resolveCustomerId();

  const modal = document.getElementById("productModal");
  const modalClose = document.getElementById("modalClose");
  const modalBack = document.getElementById("modalBack");
  const modalAdd = document.getElementById("modalAdd");
  const modalImage = document.getElementById("modalImage");
  const modalName = document.getElementById("modalName");
  const modalPrice = document.getElementById("modalPrice");
  const modalDesc = document.getElementById("modalDesc");
  const modalQty = document.getElementById("modalQty");
  const modalTotal = document.getElementById("modalTotal");
  const sizeOptions = document.getElementById("sizeOptions");

  let currentVariants = [];
  let currentProductId = "";
  let cartCountValue = Number(cartCount?.textContent || 0);
  const placeholderImage =
    "/img/z7463588836100_9c92ae464febb1ec2b21ec6ac60c6fce.jpg";
  const setCartCount = (nextValue) => {
    const safe = Math.max(0, Number(nextValue) || 0);
    cartCountValue = safe;
    if (cartCount) {
      cartCount.textContent = safe;
    }
  };

  const bumpCartCountOptimistic = (delta) => {
    setCartCount(cartCountValue + Math.max(0, Number(delta) || 0));
  };

  const rollbackCartCountOptimistic = (delta) => {
    setCartCount(cartCountValue - Math.max(0, Number(delta) || 0));
  };

  const getActiveVariant = () => {
    const activeSize =
      sizeOptions?.querySelector(".size-btn.active")?.dataset.size || "M";
    const exact = currentVariants.find(
      (variant) => variant.size.toUpperCase() === activeSize.toUpperCase(),
    );
    return exact || currentVariants[0] || { size: "M", price: 0 };
  };

  const renderSizeButtons = (variants) => {
    if (!sizeOptions) return;
    sizeOptions.innerHTML = variants
      .map(
        (variant, index) =>
          `<button type="button" class="size-btn ${index === 0 ? "active" : ""}" data-size="${variant.size}">${variant.size}</button>`,
      )
      .join("");
  };

  const updateTotal = () => {
    const qty = Math.max(1, Number(modalQty?.value || 1));
    const activeVariant = getActiveVariant();
    const total = activeVariant.price * qty;
    if (modalPrice) modalPrice.textContent = toCurrency(activeVariant.price);
    if (modalTotal) modalTotal.textContent = toCurrency(total);
  };

  const updateCartCount = async () => {
    if (!customerId) return;
    try {
      const data = await getCustomerCartCount(customerId);
      setCartCount(data.count ?? 0);
    } catch (error) {
      console.error(error);
    }
  };

  if (cartLink && customerId) {
    cartLink.href = `/customer/checkout?kh=${customerId}`;
  }

  const applyFilters = () => {
    if (!productList) return;
    const term = (searchInput?.value || "").trim().toLowerCase();
    const activePill = pills.find((pill) => pill.classList.contains("active"));
    const category = (activePill?.dataset.value || "").trim().toLowerCase();

    Array.from(productList.querySelectorAll(".product-card")).forEach(
      (card) => {
        const name = (card.dataset.name || "").toLowerCase();
        const cat = (card.dataset.category || "").toLowerCase();
        const matchText = !term || name.includes(term);
        const matchCat = !category || cat === category;
        card.style.display = matchText && matchCat ? "" : "none";
      },
    );
  };

  searchInput?.addEventListener("input", applyFilters);

  pills.forEach((pill) => {
    pill.addEventListener("click", () => {
      pills.forEach((p) => p.classList.remove("active"));
      pill.classList.add("active");
      applyFilters();
    });
  });

  const addToCartQuick = async (productId, size = "M", qty = 1) => {
    if (!customerId) {
      alert("Vui lòng đăng ký hoặc đăng nhập để sử dụng giỏ hàng.");
      return;
    }
    const payload = { customerId, productId, qty, size };
    const addedQty = Math.max(1, Number(qty) || 1);
    bumpCartCountOptimistic(addedQty);
    try {
      const result = await addCustomerCartItem(payload);
      if (result && typeof result.count === "number") {
        setCartCount(result.count);
      }
      const successMsg = result?.message || "Thêm sản phẩm thành công";
      showToast(successMsg, "success");
    } catch (error) {
      rollbackCartCountOptimistic(addedQty);
      console.error(error);
      showToast(error?.message || "Thêm sản phẩm thất bại", "error");
    }
  };

  productList?.addEventListener("click", (event) => {
    const addBtn = event.target.closest(".add-btn");
    if (addBtn) {
      const card = addBtn.closest(".product-card");
      const productId = card?.dataset.id || "";
      const quickVariants = card ? parseVariantsFromCard(card) : [];
      const defaultSize = quickVariants[0]?.size || "M";
      if (productId) {
        addToCartQuick(productId, defaultSize).catch(console.error);
      }
      return;
    }

    const card = event.target.closest(".product-card");
    if (!card || !modal) return;

    const name = card.dataset.name || "";
    const variants = parseVariantsFromCard(card);
    const image = normalizeProductImage(card.dataset.image || "");
    const desc = card.dataset.desc || "";
    currentProductId = card.dataset.id || "";
    currentVariants = variants;

    if (modalName) modalName.textContent = name;
    if (modalDesc) modalDesc.textContent = desc || "Mô tả sản phẩm.";

    if (modalImage) {
      modalImage.src = image || placeholderImage;
    }

    if (modalQty) modalQty.value = 1;
    renderSizeButtons(currentVariants);

    updateTotal();
    modal.classList.add("show");
    modal.setAttribute("aria-hidden", "false");
  });

  const closeModal = () => {
    if (!modal) return;
    modal.classList.remove("show");
    modal.setAttribute("aria-hidden", "true");
  };

  modalClose?.addEventListener("click", closeModal);
  modalBack?.addEventListener("click", closeModal);
  modal?.addEventListener("click", (event) => {
    if (event.target === modal) closeModal();
  });

  sizeOptions?.addEventListener("click", (event) => {
    const btn = event.target.closest(".size-btn");
    if (!btn) return;
    sizeOptions.querySelectorAll(".size-btn").forEach((b) => {
      b.classList.remove("active");
    });
    btn.classList.add("active");
    updateTotal();
  });

  modalQty?.addEventListener("input", updateTotal);

  document.addEventListener("click", (event) => {
    const actionBtn = event.target.closest(".qty-btn");
    if (!actionBtn) return;
    const action = actionBtn.dataset.action;
    const current = Number(modalQty?.value || 1);
    if (action === "increase") {
      modalQty.value = current + 1;
    } else if (action === "decrease") {
      modalQty.value = Math.max(1, current - 1);
    }
    updateTotal();
  });

  modalAdd?.addEventListener("click", async () => {
    if (!customerId) {
      alert("Vui lòng đăng ký hoặc đăng nhập để sử dụng giỏ hàng.");
      closeModal();
      return;
    }

    const activeSize = getActiveVariant().size;
    const qty = Math.max(1, Number(modalQty?.value || 1));
    const payload = {
      customerId,
      productId: currentProductId,
      qty,
      size: activeSize || "M",
    };
    const addedQty = Math.max(1, Number(qty) || 1);
    bumpCartCountOptimistic(addedQty);

    try {
      const result = await addCustomerCartItem(payload);
      if (result && typeof result.count === "number") {
        setCartCount(result.count);
      }
      const successMsg = result?.message || "Thêm sản phẩm thành công";
      showToast(successMsg, "success");
    } catch (error) {
      rollbackCartCountOptimistic(addedQty);
      console.error(error);
      showToast(error?.message || "Thêm sản phẩm thất bại", "error");
    } finally {
      closeModal();
    }
  });

  updateCartCount();

  profileBtn?.addEventListener("click", (event) => {
    event.stopPropagation();
    if (!profileMenu) return;
    const isVisible =
      profileMenu.classList.contains("show") && !profileMenu.hidden;
    const nextVisible = !isVisible;
    profileMenu.classList.toggle("show", nextVisible);
    profileMenu.hidden = !nextVisible;
    profileMenu.style.display = nextVisible ? "block" : "none";
    profileBtn.setAttribute("aria-expanded", nextVisible.toString());
  });

  document.addEventListener("click", (event) => {
    const isProfile = event.target.closest(".profile-wrapper");
    if (!isProfile && profileMenu) {
      profileMenu.classList.remove("show");
      profileMenu.hidden = true;
      profileMenu.style.display = "none";
      profileBtn?.setAttribute("aria-expanded", "false");
    }
  });
});
