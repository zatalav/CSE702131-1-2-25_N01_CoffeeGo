window.QuanlyPrefetch = window.QuanlyPrefetch || {
  init: function () {
    const prefetchedUrls = new Set();

    const prefetchDocument = (href) => {
      if (!href) return;
      try {
        const url = new URL(href, window.location.origin);
        if (url.origin !== window.location.origin) return;
        if (
          url.pathname === window.location.pathname &&
          url.search === window.location.search
        ) {
          return;
        }
        if (url.pathname === "/logout") return;

        const key = url.pathname + url.search;
        if (prefetchedUrls.has(key)) return;

        const link = document.createElement("link");
        link.rel = "prefetch";
        link.as = "document";
        link.href = url.toString();
        document.head.appendChild(link);
        prefetchedUrls.add(key);
      } catch {
        // Ignore malformed URLs.
      }
    };

    const bindPrefetch = (selector) => {
      document.querySelectorAll(selector).forEach((anchor) => {
        const href = anchor.getAttribute("href");
        if (!href || href === "#") return;
        anchor.addEventListener("mouseenter", () => prefetchDocument(href), {
          passive: true,
        });
        anchor.addEventListener("focus", () => prefetchDocument(href), {
          passive: true,
        });
        anchor.addEventListener("touchstart", () => prefetchDocument(href), {
          passive: true,
        });
      });
    };

    bindPrefetch(".sidebar a[href]");
    bindPrefetch(".pagination a[href]");

    if ("requestIdleCallback" in window) {
      window.requestIdleCallback(() => {
        document
          .querySelectorAll(".sidebar .submenu a[href]")
          .forEach((anchor) => {
            prefetchDocument(anchor.getAttribute("href"));
          });
      });
    }
  },
};
