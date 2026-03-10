const menuBtn = document.getElementById("menuBtn");
const menuDropdown = document.getElementById("menuDropdown");

if (menuBtn && menuDropdown) {
  menuBtn.addEventListener("click", (e) => {
    e.stopPropagation();
    const isOpen = menuDropdown.style.display === "block";
    menuDropdown.style.display = isOpen ? "none" : "block";
    menuBtn.setAttribute("aria-expanded", (!isOpen).toString());
  });

  document.addEventListener("click", () => {
    menuDropdown.style.display = "none";
    menuBtn.setAttribute("aria-expanded", "false");
  });

  menuDropdown.addEventListener("click", (e) => {
    e.stopPropagation();
  });
}
