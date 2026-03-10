import { toCurrency } from "./app/lib/helpers.js";

// Simple report JS: wire filters and render lightweight charts using Canvas
(function () {
  function sampleData() {
    // returns sample daily revenue for last 14 days and product/category breakdown
    var days = [];
    var now = new Date();
    for (var i = 13; i >= 0; i--) {
      var d = new Date(now.getFullYear(), now.getMonth(), now.getDate() - i);
      days.push({
        date: d,
        revenue: Math.floor(50000 + Math.random() * 200000),
      });
    }
    var products = [
      { name: "Cà Phê Sữa", sold: 95, revenue: 2850000 },
      { name: "Latte", sold: 62, revenue: 3600000 },
      { name: "Trà Tắc", sold: 55, revenue: 1100000 },
    ];
    var categories = [
      { name: "Cà Phê", pct: 50.2 },
      { name: "Trà", pct: 27.1 },
      { name: "Sinh Tố", pct: 22.7 },
    ];
    return { days: days, products: products, categories: categories };
  }

  function drawLineChart(canvas, data) {
    if (!canvas) return;
    var ctx = canvas.getContext("2d");
    var w = (canvas.width = canvas.clientWidth);
    var h = (canvas.height = canvas.clientHeight);
    ctx.clearRect(0, 0, w, h);
    // draw simple grid
    ctx.strokeStyle = "#eef2f7";
    ctx.lineWidth = 1;
    for (var i = 0; i <= 6; i++) {
      var y = (h / 6) * i;
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(w, y);
      ctx.stroke();
    }
    // draw line
    var max = Math.max.apply(
      null,
      data.map(function (d) {
        return d.revenue;
      }),
    );
    var px = w / Math.max(data.length - 1, 1);
    ctx.strokeStyle = "#6f4e37";
    ctx.lineWidth = 2;
    ctx.beginPath();
    data.forEach(function (d, i) {
      var x = i * px;
      var y = h - (d.revenue / max) * (h * 0.85) - 10;
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.stroke();
  }

  function drawPie(canvas, categories) {
    if (!canvas) return;
    var ctx = canvas.getContext("2d");
    var w = (canvas.width = canvas.clientWidth);
    var h = (canvas.height = canvas.clientHeight);
    ctx.clearRect(0, 0, w, h);
    var total = categories.reduce(function (s, c) {
      return s + c.pct;
    }, 0);
    var cx = w / 2,
      cy = h / 2,
      r = Math.min(w, h) * 0.35;
    var start = -Math.PI / 2;
    var colors = ["#3fc47e", "#6b9dfc", "#ffc46b"];
    categories.forEach(function (c, i) {
      var angle = (c.pct / total) * Math.PI * 2;
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.fillStyle = colors[i % colors.length];
      ctx.arc(cx, cy, r, start, start + angle);
      ctx.closePath();
      ctx.fill();
      start += angle;
    });
  }

  function render() {
    var data = sampleData();
    // metrics
    var totalRevenue = data.products.reduce(function (s, p) {
      return s + p.revenue;
    }, 0);
    document.getElementById("totalRevenue").textContent =
      toCurrency(totalRevenue);
    document.getElementById("totalOrders").textContent = data.products.reduce(
      function (s, p) {
        return s + p.sold;
      },
      0,
    );
    document.getElementById("topProduct").textContent = data.products[0].name;
    document.getElementById("avgRevenue").textContent = toCurrency(
      Math.round(
        totalRevenue /
          Math.max(
            1,
            data.products.reduce(function (s, p) {
              return s + p.sold;
            }, 0),
          ),
      ),
    );

    // product list
    var list = document.getElementById("productList");
    list.innerHTML = "";
    data.products.forEach(function (p) {
      var li = document.createElement("li");
      li.style.padding = "6px 0";
      li.textContent = p.name + " — " + p.sold;
      list.appendChild(li);
    });

    // top products table
    var tbody = document.getElementById("topProductsBody");
    tbody.innerHTML = "";
    data.products.forEach(function (p, idx) {
      var tr = document.createElement("tr");
      tr.innerHTML =
        "<td>" +
        (idx + 1) +
        ". " +
        p.name +
        '</td><td style="text-align:right; padding-left:12px;">' +
        p.sold +
        '</td><td style="text-align:right; padding-left:12px;">' +
        toCurrency(p.revenue) +
        "</td>";
      tbody.appendChild(tr);
    });

    // charts
    drawLineChart(document.getElementById("revenueChart"), data.days);
    drawPie(document.getElementById("categoryChart"), data.categories);
  }

  document.addEventListener("DOMContentLoaded", function () {
    window.AdminUiCommon?.initSidebarAndProfile?.();
    render();
    var btn = document.getElementById("filterBtn");
    if (btn)
      btn.addEventListener("click", function () {
        render();
      });
    // responsive redraw
    window.addEventListener("resize", function () {
      render();
    });
  });
})();
