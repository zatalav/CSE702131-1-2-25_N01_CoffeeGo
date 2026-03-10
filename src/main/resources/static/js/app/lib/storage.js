export const storage = {
  get(key, fallback = null) {
    const raw = localStorage.getItem(key);
    if (!raw) return fallback;

    try {
      return JSON.parse(raw);
    } catch {
      return raw;
    }
  },

  set(key, value) {
    const payload = typeof value === "string" ? value : JSON.stringify(value);
    localStorage.setItem(key, payload);
  },

  remove(key) {
    localStorage.removeItem(key);
  },
};
