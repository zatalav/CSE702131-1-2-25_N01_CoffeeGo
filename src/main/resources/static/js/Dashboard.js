import {
  getDashboardStats,
  getDashboardOrders,
  getTopProducts,
} from "./app/services/dashboardService.js";
import { formatNumber, toCurrency } from "./app/lib/helpers.js";

const canvas = document.getElementById("revenueChart");
const ctx = canvas.getContext("2d");

canvas.width = 520;
canvas.height = 340;

const donutColors = [
  "#2F5B9A",
  "#F5B301",
  "#2978D9",
  "#F44336",
  "#4FC3C8",
  "#8B5CF6",
  "#10B981",
  "#F97316",
];

let donutSlices = [];
let hoveredSliceIndex = -1;
let donutData = [];

const donutGeometry = {
  centerX: 190,
  centerY: 170,
  outerRadius: 130,
  innerRadius: 72,
};

function getDonutTooltip() {
  let tooltip = document.getElementById("donutTooltip");
  if (!tooltip) {
    tooltip = document.createElement("div");
    tooltip.id = "donutTooltip";
    tooltip.className = "donut-tooltip";
    document.body.appendChild(tooltip);
  }
  return tooltip;
}

function hideDonutTooltip() {
  const tooltip = document.getElementById("donutTooltip");
  if (tooltip) {
    tooltip.style.display = "none";
  }
}

function drawDonutChart(products) {
  const legend = document.getElementById("topProductsLegend");
  const chartData = Array.isArray(products) ? products : [];
  donutData = chartData.map((item) => ({
    name: item.name,
    sold: Number(item.sold) || 0,
  }));
  const totalSold = chartData.reduce(
    (sum, item) => sum + (Number(item.sold) || 0),
    0,
  );

  ctx.clearRect(0, 0, canvas.width, canvas.height);
  if (legend) {
    legend.innerHTML = "";
  }
  donutSlices = [];
  if (hoveredSliceIndex >= chartData.length) {
    hoveredSliceIndex = -1;
  }

  if (chartData.length === 0 || totalSold <= 0) {
    hoveredSliceIndex = -1;
    hideDonutTooltip();
    ctx.fillStyle = "#6b7280";
    ctx.font = "16px Arial";
    ctx.textAlign = "center";
    ctx.fillText("Chưa có dữ liệu", canvas.width / 2, canvas.height / 2);
    return;
  }

  const centerX = donutGeometry.centerX;
  const centerY = donutGeometry.centerY;
  const outerRadius = donutGeometry.outerRadius;
  const innerRadius = donutGeometry.innerRadius;

  let startAngle = -Math.PI / 2;
  chartData.forEach((item, index) => {
    const sold = Number(item.sold) || 0;
    const ratio = sold / totalSold;
    const sweep = ratio * Math.PI * 2;
    const isHovered = index === hoveredSliceIndex;
    const currentOuterRadius = isHovered ? outerRadius + 8 : outerRadius;
    const color = donutColors[index % donutColors.length];

    ctx.beginPath();
    ctx.moveTo(centerX, centerY);
    ctx.arc(
      centerX,
      centerY,
      currentOuterRadius,
      startAngle,
      startAngle + sweep,
    );
    ctx.closePath();
    ctx.fillStyle = color;
    ctx.fill();

    donutSlices.push({
      name: item.name,
      percent: Number((ratio * 100).toFixed(1)),
      color,
      sold,
      startAngle,
      endAngle: startAngle + sweep,
    });

    startAngle += sweep;
  });

  ctx.beginPath();
  ctx.arc(centerX, centerY, innerRadius, 0, Math.PI * 2);
  ctx.fillStyle = "#fff";
  ctx.fill();

  ctx.fillStyle = "#1f2937";
  ctx.font = "bold 34px Arial";
  ctx.textAlign = "center";
  ctx.fillText(formatNumber(totalSold), centerX, centerY - 6);
  ctx.font = "24px Arial";
  ctx.fillStyle = "#4b5563";
  ctx.fillText("Ly đã bán", centerX, centerY + 32);

  if (legend) {
    const title = document.createElement("h4");
    title.className = "legend-title";
    title.textContent = "Ghi chú sản phẩm";
    legend.appendChild(title);

    const table = document.createElement("table");
    table.className = "legend-table";

    table.innerHTML = `
      <thead>
        <tr>
          <th>SP</th>
          <th>Đã bán</th>
          <th>Tỷ lệ</th>
        </tr>
      </thead>
      <tbody></tbody>
    `;

    const tableBody = table.querySelector("tbody");
    chartData.forEach((item, index) => {
      const sold = Number(item.sold) || 0;
      const percent = ((sold / totalSold) * 100).toFixed(1);
      const productName = item.name || "Sản phẩm";
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>
          <span class="legend-label-wrap">
            <span class="legend-dot" style="background:${donutColors[index % donutColors.length]}"></span>
            <span class="legend-name">${productName}</span>
          </span>
        </td>
        <td class="legend-sold">${formatNumber(sold)}</td>
        <td class="legend-value">${percent}%</td>
      `;
      tableBody.appendChild(row);
    });

    legend.appendChild(table);
  }
}

function pickSliceAt(x, y) {
  const dx = x - donutGeometry.centerX;
  const dy = y - donutGeometry.centerY;
  const distance = Math.sqrt(dx * dx + dy * dy);

  if (
    distance < donutGeometry.innerRadius ||
    distance > donutGeometry.outerRadius + 10
  ) {
    return -1;
  }

  let angle = Math.atan2(dy, dx);
  if (angle < -Math.PI / 2) {
    angle += Math.PI * 2;
  }

  return donutSlices.findIndex(
    (slice) => angle >= slice.startAngle && angle < slice.endAngle,
  );
}

function setupDonutHover() {
  if (!canvas) {
    return;
  }

  canvas.addEventListener("mousemove", (event) => {
    if (!donutSlices.length) {
      hideDonutTooltip();
      return;
    }

    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    const x = (event.clientX - rect.left) * scaleX;
    const y = (event.clientY - rect.top) * scaleY;

    const sliceIndex = pickSliceAt(x, y);
    const tooltip = getDonutTooltip();

    if (sliceIndex === -1) {
      tooltip.style.display = "none";
      if (hoveredSliceIndex !== -1) {
        hoveredSliceIndex = -1;
        drawDonutChart(donutData);
      }
      return;
    }

    if (hoveredSliceIndex !== sliceIndex) {
      hoveredSliceIndex = sliceIndex;
      drawDonutChart(donutData);
    }

    const slice = donutSlices[sliceIndex];
    tooltip.innerHTML = `
      <div class="tooltip-title">${slice.name || "San pham"}</div>
      <div class="tooltip-row">
        <span class="tooltip-dot" style="background:${slice.color}"></span>
        <span>${formatNumber(slice.sold)} ly (${slice.percent}%)</span>
      </div>
    `;
    tooltip.style.display = "block";
    tooltip.style.left = `${event.clientX + 14}px`;
    tooltip.style.top = `${event.clientY + 14}px`;
  });

  canvas.addEventListener("mouseleave", () => {
    hideDonutTooltip();
    if (hoveredSliceIndex !== -1) {
      hoveredSliceIndex = -1;
      drawDonutChart(donutData);
    }
  });
}
// Load dashboard statistics
async function loadStats() {
  try {
    const data = await getDashboardStats();

    document.getElementById("todayRevenue").textContent = toCurrency(
      data.todayRevenue,
    );
    document.getElementById("revenuePercent").textContent =
      `▲ ${data.revenuePercent ?? 0}%`;
    document.getElementById("todayOrders").textContent = data.todayOrders;
    document.getElementById("ordersPercent").textContent =
      `▲ ${data.ordersPercent ?? 0}%`;
    document.getElementById("totalCustomers").textContent = data.totalCustomers;
    document.getElementById("onlineCustomers").textContent =
      `${data.onlineCustomers} online`;
    document.getElementById("lowStock").textContent = data.lowStock;
    document.getElementById("totalRevenue").textContent = toCurrency(
      data.totalRevenue,
    );
  } catch (error) {
    console.error("Error loading stats:", error);
  }
}

// Load orders
async function loadOrders() {
  try {
    const orders = await getDashboardOrders();

    const tbody = document.getElementById("ordersTableBody");
    tbody.innerHTML = "";

    orders.forEach((order) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${order.id}</td>
        <td>${order.customer}</td>
        <td class="${order.status}">${order.statusText}</td>
        <td>${toCurrency(order.total)}</td>
      `;
      tbody.appendChild(row);
    });

    if (orders.length === 0) {
      tbody.innerHTML =
        '<tr><td colspan="4" style="text-align:center">Chưa có dữ liệu đơn hàng</td></tr>';
    }
  } catch (error) {
    console.error("Error loading orders:", error);
  }
}

// Load top products
async function loadTopProducts() {
  try {
    const products = await getTopProducts();
    drawDonutChart(products);
  } catch (error) {
    console.error("Error loading products:", error);
    drawDonutChart([]);
  }
}

// Initialize dashboard on page load
document.addEventListener("DOMContentLoaded", () => {
  window.AdminUiCommon?.initSidebarAndProfile?.();
  drawDonutChart([]);
  setupDonutHover();
  loadStats();
  loadOrders();
  loadTopProducts();
});
