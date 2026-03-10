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

  const chartBox = document.getElementById("deliveryChartBox");
  const chartCanvas = document.getElementById("deliveryPieChart");
  const chartPeriod = document.getElementById("chartPeriod");
  const chartMonth = document.getElementById("chartMonth");
  const chartYear = document.getElementById("chartYear");
  const chartApplyBtn = document.getElementById("chartApplyBtn");
  const chartCaption = document.getElementById("chartCaption");
  let deliveryPieChart = null;

  function drawFallbackPie(values, labels) {
    if (!chartCanvas) return;
    const context = chartCanvas.getContext("2d");
    if (!context) return;

    const width = chartCanvas.width;
    const height = chartCanvas.height;
    context.clearRect(0, 0, width, height);

    const total = values.reduce((sum, value) => sum + Math.max(0, value), 0);
    if (total <= 0) {
      context.fillStyle = "#6b7280";
      context.font = "14px Arial";
      context.fillText("Không có dữ liệu để hiển thị", 16, 28);
      return;
    }

    const colors = ["#3b82f6", "#16a34a", "#ef4444", "#a855f7"];
    const centerX = 120;
    const centerY = 120;
    const radius = 85;
    let startAngle = -Math.PI / 2;

    values.forEach((value, index) => {
      const part = Math.max(0, value);
      if (part <= 0) {
        return;
      }
      const sliceAngle = (part / total) * Math.PI * 2;
      context.beginPath();
      context.moveTo(centerX, centerY);
      context.arc(
        centerX,
        centerY,
        radius,
        startAngle,
        startAngle + sliceAngle,
      );
      context.closePath();
      context.fillStyle = colors[index % colors.length];
      context.fill();
      startAngle += sliceAngle;
    });

    context.font = "12px Arial";
    context.fillStyle = "#2f2a2a";
    labels.forEach((label, index) => {
      const y = 32 + index * 22;
      context.fillStyle = colors[index % colors.length];
      context.fillRect(235, y - 10, 12, 12);
      context.fillStyle = "#2f2a2a";
      context.fillText(`${label}: ${values[index]}`, 254, y);
    });
  }

  function updateMonthVisibility() {
    if (!chartPeriod || !chartMonth) return;
    chartMonth.style.display =
      chartPeriod.value === "month" ? "inline-block" : "none";
  }

  async function loadDeliveryChart() {
    if (!chartBox || !chartCanvas || !chartPeriod || !chartYear) return;
    const chartUrl = chartBox.dataset.chartUrl;
    if (!chartUrl) return;

    const period = chartPeriod.value === "year" ? "year" : "month";
    const year = Number.parseInt(chartYear.value || "", 10);
    const month = Number.parseInt(chartMonth?.value || "", 10);

    const params = new URLSearchParams();
    params.set("period", period);
    if (!Number.isNaN(year)) {
      params.set("year", String(year));
    }
    if (period === "month" && !Number.isNaN(month)) {
      params.set("month", String(month));
    }

    let payload = null;
    let data = null;

    try {
      const response = await fetch(`${chartUrl}?${params.toString()}`, {
        headers: {
          Accept: "application/json",
        },
      });

      if (response.ok) {
        payload = await response.json();
        data = payload?.data || {};
      }
    } catch (error) {
      data = null;
    }

    if (!data) {
      data = {
        delivering: Number(chartBox.dataset.seedDelivering || 0),
        completed: Number(chartBox.dataset.seedCompleted || 0),
        cancelled: 0,
        other: 0,
      };
    }

    const labels = ["Đang giao hàng", "Đã hoàn thành", "Đã hủy", "Khác"];
    const values = [
      Number(data.delivering || 0),
      Number(data.completed || 0),
      Number(data.cancelled || 0),
      Number(data.other || 0),
    ];

    const context = chartCanvas.getContext("2d");
    if (!context) {
      return;
    }

    if (typeof Chart === "undefined") {
      drawFallbackPie(values, labels);
    } else {
      if (deliveryPieChart) {
        deliveryPieChart.destroy();
      }

      deliveryPieChart = new Chart(context, {
        type: "pie",
        data: {
          labels,
          datasets: [
            {
              data: values,
              backgroundColor: ["#3b82f6", "#16a34a", "#ef4444", "#a855f7"],
              borderWidth: 1,
            },
          ],
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              position: "bottom",
            },
          },
        },
      });
    }

    if (chartCaption) {
      const yearLabel = payload?.year
        ? `năm ${payload.year}`
        : `năm ${new Date().getFullYear()}`;
      const monthLabel = payload?.month ? `tháng ${payload.month}` : "cả năm";
      chartCaption.textContent =
        period === "month"
          ? `Thống kê ${monthLabel}, ${yearLabel}`
          : `Thống kê theo ${yearLabel}`;
    }
  }

  if (chartPeriod && chartYear) {
    const now = new Date();
    chartYear.value = String(now.getFullYear());
    if (chartMonth) {
      chartMonth.value = String(now.getMonth() + 1);
    }
    updateMonthVisibility();
    loadDeliveryChart();

    chartPeriod.addEventListener("change", () => {
      updateMonthVisibility();
      loadDeliveryChart();
    });

    if (chartApplyBtn) {
      chartApplyBtn.addEventListener("click", loadDeliveryChart);
    }
  }

  const routeBtn = document.getElementById("routeFromMeBtn");
  if (routeBtn) {
    routeBtn.addEventListener("click", () => {
      const destination = routeBtn.getAttribute("data-destination");
      if (!destination) {
        alert("Không có địa chỉ giao hàng để chỉ đường.");
        return;
      }

      if (!navigator.geolocation) {
        alert("Trình duyệt không hỗ trợ định vị vị trí hiện tại.");
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;
          const destinationEncoded = encodeURIComponent(destination);
          const directionUrl = `https://www.google.com/maps/dir/?api=1&origin=${lat},${lng}&destination=${destinationEncoded}&travelmode=driving`;
          window.open(directionUrl, "_blank");
        },
        () => {
          alert(
            "Không thể lấy vị trí hiện tại. Hãy bật quyền vị trí và thử lại.",
          );
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
        },
      );
    });
  }

  const cancelForms = document.querySelectorAll(".cancel-order-form");
  cancelForms.forEach((form) => {
    form.addEventListener("submit", (event) => {
      const reasonSelect = form.querySelector('select[name="reason"]');
      const otherReasonInput = form.querySelector('input[name="otherReason"]');

      // Detail page flow: choose a predefined reason, optionally provide custom text.
      if (reasonSelect) {
        const selected = (reasonSelect.value || "").trim();
        if (!selected) {
          event.preventDefault();
          window.alert("Vui lòng chọn lý do hủy đơn hàng.");
          return;
        }
        if (selected === "KHAC") {
          const other = (otherReasonInput?.value || "").trim();
          if (!other) {
            event.preventDefault();
            window.alert("Vui lòng nhập lý do hủy đơn hàng.");
            return;
          }
          reasonSelect.value = other;
        }
        return;
      }

      // Backward-compatible fallback for any legacy cancel form.
      const reason = window.prompt("Nhập lý do hủy đơn hàng:", "");
      if (reason === null) {
        event.preventDefault();
        return;
      }
      const trimmedReason = reason.trim();
      if (!trimmedReason) {
        event.preventDefault();
        window.alert("Vui lòng nhập lý do hủy đơn hàng.");
        return;
      }
      const reasonInput = form.querySelector('input[name="reason"]');
      if (reasonInput) {
        reasonInput.value = trimmedReason;
      }
    });
  });

  const cancelReasonSelect = document.getElementById("cancelReasonSelect");
  const cancelReasonOther = document.getElementById("cancelReasonOther");
  if (cancelReasonSelect && cancelReasonOther) {
    const syncOtherReasonVisibility = () => {
      const isOther = cancelReasonSelect.value === "KHAC";
      cancelReasonOther.style.display = isOther ? "block" : "none";
      cancelReasonOther.required = isOther;
      if (!isOther) {
        cancelReasonOther.value = "";
      }
    };
    cancelReasonSelect.addEventListener("change", syncOtherReasonVisibility);
    syncOtherReasonVisibility();
  }
});
