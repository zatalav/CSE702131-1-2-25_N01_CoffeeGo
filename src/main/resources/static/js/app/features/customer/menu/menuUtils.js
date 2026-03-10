export function resolveCustomerId() {
  const fromBody = document.body?.dataset.customerId || "";
  if (fromBody) return fromBody;
  const params = new URLSearchParams(window.location.search);
  return params.get("kh") || "";
}

export function parseVariantsFromCard(card) {
  const variantNodes = Array.from(card.querySelectorAll(".variant-item"));
  const parsed = variantNodes
    .map((node) => {
      const size = (node.dataset.size || "").trim().toUpperCase();
      const price = Number(node.dataset.price || 0);
      return { size, price };
    })
    .filter((item) => item.size && item.price > 0);

  if (parsed.length > 0) {
    return parsed;
  }

  const fallbackPrice = Number(card.dataset.price || 0);
  return [{ size: "M", price: fallbackPrice }];
}

export function normalizeProductImage(imagePath = "") {
  let image = String(imagePath || "");
  if (!image) return "";

  if (image.includes("\\")) {
    const fileName = image.split("\\").pop();
    image = fileName ? `/img/${fileName}` : "";
  }

  return image;
}
