export function getItemQty(item) {
  return Number(item?.qty ?? item?.quantity ?? 1);
}

export function getItemUnitPrice(item) {
  return Number(item?.unitPrice ?? item?.price ?? 0);
}

export function calcSubtotal(items = []) {
  return items.reduce(
    (sum, item) => sum + getItemUnitPrice(item) * getItemQty(item),
    0,
  );
}

export function calcTotalQty(items = []) {
  return items.reduce((sum, item) => sum + getItemQty(item), 0);
}
