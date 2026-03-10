window.AdminUiCommon = window.AdminUiCommon || {
  initSidebarSubmenu: function () {
    document.querySelectorAll(".menu-toggle").forEach((toggle) => {
      toggle.addEventListener("click", (e) => {
        e.preventDefault();

        const parent = toggle.closest(".has-submenu");
        if (!parent) return;
        const submenu = parent.querySelector(".submenu");
        const isOpen = parent.classList.contains("open");

        document.querySelectorAll(".has-submenu").forEach((item) => {
          item.classList.remove("open");
          const sub = item.querySelector(".submenu");
          if (sub) sub.style.display = "none";
        });

        if (!isOpen) {
          parent.classList.add("open");
          if (submenu) submenu.style.display = "block";
        }
      });
    });
  },

  initProfileMenu: function () {
    const profileBtn = document.getElementById("profileBtn");
    const profileMenu = document.getElementById("profileMenu");
    if (!profileBtn || !profileMenu) return;

    profileBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      const isOpen = profileMenu.style.display === "block";
      profileMenu.style.display = isOpen ? "none" : "block";
      profileBtn.setAttribute("aria-expanded", (!isOpen).toString());
    });

    document.addEventListener("click", () => {
      profileMenu.style.display = "none";
      profileBtn.setAttribute("aria-expanded", "false");
    });

    profileMenu.addEventListener("click", (e) => e.stopPropagation());
  },

  initSidebarAndProfile: function () {
    this.initSidebarSubmenu();
    this.initProfileMenu();
  },
};
