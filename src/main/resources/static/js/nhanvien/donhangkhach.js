document.addEventListener("DOMContentLoaded", () => {
  const menuBtn = document.getElementById("menuBtn");
  const menuDropdown = document.getElementById("menuDropdown");

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
});
