/* Biểu đồ doanh thu bán hàng online dùng chung cho dashboard và trang báo cáo. Cần Chart.js đã được nạp. */
window.SalesCharts = (function () {
    var text = '#94a3b8';
    var grid = 'rgba(255, 255, 255, 0.06)';
    var palette = ['rgba(16, 185, 129, 0.85)', 'rgba(212, 175, 55, 0.85)', 'rgba(56, 189, 248, 0.85)', 'rgba(166, 85, 59, 0.85)'];
    var vnd = new Intl.NumberFormat('vi-VN');

    function short(value) {
        if (value >= 1e6) { return (value / 1e6).toFixed(value % 1e6 === 0 ? 0 : 1) + ' tr'; }
        if (value >= 1e3) { return Math.round(value / 1e3) + 'k'; }
        return value;
    }

    function canvas(id) {
        return id ? document.getElementById(id) : null;
    }

    /**
     * options: daily [{date, revenue, orders}], payment {nhãn: tiền}, stores {tên chi nhánh: tiền},
     * dailyCanvas / paymentCanvas / storeCanvas là id thẻ canvas (bỏ trống thì không vẽ biểu đồ đó).
     */
    function render(options) {
        var dailyEl = canvas(options.dailyCanvas);
        if (dailyEl) {
            new Chart(dailyEl, {
                data: {
                    labels: options.daily.map(function (d) { return d.date.substring(8, 10) + '/' + d.date.substring(5, 7); }),
                    datasets: [
                        { type: 'bar', label: 'Doanh thu (₫)', data: options.daily.map(function (d) { return d.revenue; }), backgroundColor: palette[0], borderRadius: 4, yAxisID: 'y' },
                        { type: 'line', label: 'Số đơn', data: options.daily.map(function (d) { return d.orders; }), borderColor: '#D4AF37', backgroundColor: '#D4AF37', pointRadius: 2, tension: 0.3, yAxisID: 'y1' }
                    ]
                },
                options: {
                    responsive: true, maintainAspectRatio: false, interaction: { mode: 'index', intersect: false },
                    plugins: {
                        legend: { labels: { color: text, boxWidth: 12 } },
                        tooltip: { callbacks: { label: function (c) { return c.dataset.label + ': ' + (c.datasetIndex === 0 ? vnd.format(c.parsed.y) + ' ₫' : c.parsed.y); } } }
                    },
                    scales: {
                        x: { ticks: { color: text, maxTicksLimit: 10 }, grid: { color: grid } },
                        y: { beginAtZero: true, ticks: { color: text, callback: short }, grid: { color: grid } },
                        y1: { beginAtZero: true, position: 'right', ticks: { color: '#D4AF37', precision: 0 }, grid: { drawOnChartArea: false } }
                    }
                }
            });
        }

        var paymentEl = canvas(options.paymentCanvas);
        if (paymentEl) {
            var paymentLabels = Object.keys(options.payment);
            new Chart(paymentEl, {
                type: 'doughnut',
                data: {
                    labels: paymentLabels.length ? paymentLabels : ['Chưa có dữ liệu'],
                    datasets: [{ data: paymentLabels.length ? Object.values(options.payment) : [1], backgroundColor: paymentLabels.length ? palette : ['rgba(255,255,255,0.08)'], borderWidth: 0 }]
                },
                options: {
                    responsive: true, maintainAspectRatio: false, cutout: '62%',
                    plugins: {
                        legend: { position: 'bottom', labels: { color: text, boxWidth: 12 } },
                        tooltip: { callbacks: { label: function (c) { return paymentLabels.length ? c.label + ': ' + vnd.format(c.parsed) + ' ₫' : c.label; } } }
                    }
                }
            });
        }

        var storeEl = canvas(options.storeCanvas);
        if (storeEl) {
            var storeNames = Object.keys(options.stores);
            // Bỏ tiền tố chung "OldBook " để tên chi nhánh không bị cắt ở trục dọc
            new Chart(storeEl, {
                type: 'bar',
                data: {
                    labels: storeNames.length ? storeNames.map(function (name) { return name.replace(/^OldBook\s*/, ''); }) : ['Chưa có dữ liệu'],
                    datasets: [{ label: 'Doanh thu (₫)', data: storeNames.length ? Object.values(options.stores) : [0], backgroundColor: palette[2], borderRadius: 4 }]
                },
                options: {
                    indexAxis: 'y', responsive: true, maintainAspectRatio: false,
                    plugins: { legend: { display: false }, tooltip: { callbacks: { label: function (c) { return vnd.format(c.parsed.x) + ' ₫'; } } } },
                    scales: {
                        x: { beginAtZero: true, ticks: { color: text, callback: short }, grid: { color: grid } },
                        y: { ticks: { color: text }, grid: { display: false } }
                    }
                }
            });
        }
    }

    return { render: render };
})();
