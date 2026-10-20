<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html class="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Bảng Điều Khiển Quản Trị — OldBook</title>
    <!-- Tailwind CSS v3 with Plugins -->
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script>
        tailwind.config = {
            darkMode: 'class',
            theme: {
                extend: {
                    colors: {
                        brand: {
                            gold: '#D4AF37',
                            goldLight: '#F3E5AB',
                            goldHover: '#c19e2e',
                            darkBg: '#0b0c10',
                            glassBg: 'rgba(18, 20, 26, 0.72)',
                            glassBorder: 'rgba(255, 255, 255, 0.08)',
                            glassHighlight: 'rgba(255, 255, 255, 0.12)',
                            surface: '#15181E',
                            forest: '#1E382B',
                            terracotta: '#A6553B'
                        }
                    },
                    fontFamily: {
                        serif: ['Playfair Display', 'Merriweather', 'Georgia', 'serif'],
                        sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'],
                    },
                    boxShadow: {
                        'glow-gold': '0 0 25px -5px rgba(212, 175, 55, 0.35)',
                        'glass-elevated': '0 20px 50px rgba(0, 0, 0, 0.65), 0 0 1px 1px rgba(255, 255, 255, 0.08)',
                    }
                }
            }
        }
    </script>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com" rel="preconnect"/>
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
    <link href="https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,300;0,400;0,700;1,400&family=Playfair+Display:ital,wght@0,500;0,600;0,700;1,600&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <!-- FontAwesome 6 -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"/>
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>

    <style>
        body {
            background-color: #0b0c10;
            font-family: 'Plus Jakarta Sans', sans-serif;
            color: #e2e8f0;
            overflow-x: hidden;
        }
        .bg-reading-room {
            background: radial-gradient(circle at 15% 20%, rgba(212, 175, 55, 0.08) 0%, transparent 45%),
                        radial-gradient(circle at 85% 65%, rgba(166, 85, 59, 0.07) 0%, transparent 50%),
                        radial-gradient(circle at 50% 100%, rgba(30, 56, 43, 0.25) 0%, transparent 60%),
                        linear-gradient(180deg, #090a0d 0%, #11141a 50%, #0d0f14 100%);
            background-attachment: fixed;
        }
        .ambient-glow {
            position: absolute;
            width: 650px;
            height: 650px;
            background: radial-gradient(circle, rgba(212, 175, 55, 0.12) 0%, rgba(193, 158, 46, 0.03) 45%, transparent 70%);
            filter: blur(80px);
            pointer-events: none;
            z-index: 0;
        }
        .glass-panel {
            background: rgba(18, 21, 28, 0.65);
            backdrop-filter: blur(28px) saturate(160%);
            -webkit-backdrop-filter: blur(28px) saturate(160%);
            border: 1px solid rgba(255, 255, 255, 0.09);
        }
        .glass-panel-subtle {
            background: rgba(25, 30, 40, 0.5);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.06);
        }
    </style>
</head>
<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-6 px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">
    <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>

    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col xl:flex-row items-start gap-5">
        <!-- Navigation Dock -->
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="dashboard"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Header bar -->
            <header class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-white/5">
                <div>
                    <div class="flex items-center gap-2 mb-1">
                        <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30">
                            TRUNG TÂM QUẢN TRỊ
                        </span>
                        <c:if test="${isStaff}">
                            <span class="px-2.5 py-0.5 rounded-full bg-blue-500/15 text-blue-300 text-[10px] font-semibold border border-blue-500/30">
                                CHI NHÁNH: ${currentStoreName}
                            </span>
                        </c:if>
                    </div>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        Tổng Quan Vận Hành Chuỗi
                    </h1>
                </div>

                <div class="flex items-center gap-3">
                    <!-- Export Excel dropdown -->
                    <div class="relative group">
                        <button class="px-4 py-2 rounded-xl bg-white/10 hover:bg-white/15 text-white text-xs font-semibold border border-white/15 transition flex items-center gap-2">
                            <i class="fa-solid fa-file-excel text-emerald-400"></i>
                            <span>Xuất Báo Cáo Excel</span>
                            <i class="fa-solid fa-chevron-down text-[10px] opacity-70"></i>
                        </button>
                        <div class="absolute right-0 mt-2 w-56 rounded-2xl glass-panel p-2 shadow-glass-elevated border border-white/10 hidden group-hover:block z-30">
                            <c:choose>
                                <c:when test="${isStaff}">
                                    <a class="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-slate-200 hover:bg-white/10 hover:text-brand-gold transition" href="/admin/export/inventory/excel">
                                        <i class="fa-solid fa-boxes-stacked text-emerald-400"></i>
                                        <span>Tồn kho chi nhánh</span>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a class="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-slate-200 hover:bg-white/10 hover:text-brand-gold transition" href="/admin/export/inventory/excel">
                                        <i class="fa-solid fa-boxes-stacked text-emerald-400"></i>
                                        <span>Tồn kho toàn chuỗi</span>
                                    </a>
                                    <a class="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-slate-200 hover:bg-white/10 hover:text-brand-gold transition" href="/admin/export/transfers/excel">
                                        <i class="fa-solid fa-truck-ramp-box text-blue-400"></i>
                                        <span>Lịch sử chuyển kho</span>
                                    </a>
                                    <a class="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-slate-200 hover:bg-white/10 hover:text-brand-gold transition" href="/admin/export/buybacks/excel">
                                        <i class="fa-solid fa-hand-holding-dollar text-amber-400"></i>
                                        <span>Danh sách đơn thu mua</span>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <a href="/" class="px-4 py-2 rounded-xl bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 text-xs font-bold shadow-glow-gold transition flex items-center gap-2">
                        <i class="fa-solid fa-globe"></i>
                        <span>Xem Website Bạn Đọc</span>
                    </a>
                </div>
            </header>

            <!-- 4 Metric Cards -->
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-xs text-slate-400 font-medium">Chi Nhánh Hoạt Động</span>
                        <div class="w-8 h-8 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center">
                            <i class="fa-solid fa-store"></i>
                        </div>
                    </div>
                    <div class="text-2xl font-bold font-serif text-white">${totalStores}</div>
                    <span class="text-[11px] text-slate-500 mt-1 block">Điểm tiếp nhận & kho hàng</span>
                </div>

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-xs text-slate-400 font-medium">Đầu Sách Đang Quản Lý</span>
                        <div class="w-8 h-8 rounded-lg bg-amber-500/10 text-brand-gold flex items-center justify-center">
                            <i class="fa-solid fa-book"></i>
                        </div>
                    </div>
                    <div class="text-2xl font-bold font-serif text-white">${totalBooks}</div>
                    <span class="text-[11px] text-slate-500 mt-1 block">Tác phẩm trong danh mục</span>
                </div>

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-xs text-slate-400 font-medium">Bản Sách Khả Dụng (Kho)</span>
                        <div class="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
                            <i class="fa-solid fa-boxes-stacked"></i>
                        </div>
                    </div>
                    <div class="text-2xl font-bold font-serif text-emerald-400">${availableCopies}</div>
                    <span class="text-[11px] text-slate-500 mt-1 block">Bản sao sẵn sàng bán</span>
                </div>

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-xs text-slate-400 font-medium">Giá Trị Kho Ước Tính</span>
                        <div class="w-8 h-8 rounded-lg bg-yellow-500/10 text-brand-goldLight flex items-center justify-center">
                            <i class="fa-solid fa-vault"></i>
                        </div>
                    </div>
                    <div class="text-xl font-bold font-serif text-brand-gold">
                        <fmt:formatNumber value="${totalInventoryValue != null ? totalInventoryValue : 0}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                    </div>
                    <span class="text-[11px] text-slate-500 mt-1 block">Định giá kho hiện thời</span>
                </div>
            </div>

            <!-- 4 Visual Charts Grid -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
                <!-- Chart 1: Sách theo chi nhánh -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">
                    <h3 class="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">
                        <i class="fa-solid fa-chart-column text-brand-gold"></i>
                        <span>Phân Bổ Tồn Kho Theo Chi Nhánh</span>
                    </h3>
                    <div class="h-64 flex items-center justify-center">
                        <canvas id="storeInventoryChart"></canvas>
                    </div>
                </div>

                <!-- Chart 2: Giá trị tồn kho -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">
                    <h3 class="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">
                        <i class="fa-solid fa-sack-dollar text-emerald-400"></i>
                        <span>Giá Trị Tồn Kho Ước Tính (VNĐ)</span>
                    </h3>
                    <div class="h-64 flex items-center justify-center">
                        <canvas id="storeValueChart"></canvas>
                    </div>
                </div>

                <!-- Chart 3: Tình trạng sách -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">
                    <h3 class="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">
                        <i class="fa-solid fa-chart-pie text-brand-goldLight"></i>
                        <span>Tỉ Lệ Tình Trạng Thẩm Định Sách Cũ</span>
                    </h3>
                    <div class="h-64 flex items-center justify-center">
                        <canvas id="conditionChart"></canvas>
                    </div>
                </div>

                <!-- Chart 4: Trạng thái thu mua -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">
                    <h3 class="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">
                        <i class="fa-solid fa-hand-holding-dollar text-rose-400"></i>
                        <span>Tỉ Lệ Trạng Thái Yêu Cầu Thu Mua</span>
                    </h3>
                    <div class="h-64 flex items-center justify-center">
                        <canvas id="buybackChart"></canvas>
                    </div>
                </div>
            </div>

            <!-- Quick Action Shortcuts -->
            <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 mt-6">
                <h3 class="text-xs uppercase tracking-wider font-semibold text-slate-400 mb-3">
                    Phím Tắt Tác Vụ Nghiệp Vụ Nhanh
                </h3>
                <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
                    <a href="/admin/users" class="p-3 rounded-xl glass-panel border border-white/5 hover:border-brand-gold/40 text-xs font-medium text-slate-300 hover:text-brand-gold transition flex items-center gap-2.5">
                        <i class="fa-solid fa-user-plus text-brand-gold"></i>
                        <span>Quản lý Nhân sự</span>
                    </a>
                    <a href="/admin/transfers/new" class="p-3 rounded-xl glass-panel border border-white/5 hover:border-brand-gold/40 text-xs font-medium text-slate-300 hover:text-brand-gold transition flex items-center gap-2.5">
                        <i class="fa-solid fa-plus-circle text-blue-400"></i>
                        <span>Tạo Phiếu Chuyển Kho</span>
                    </a>
                    <a href="/admin/buybacks" class="p-3 rounded-xl glass-panel border border-white/5 hover:border-brand-gold/40 text-xs font-medium text-slate-300 hover:text-brand-gold transition flex items-center gap-2.5">
                        <i class="fa-solid fa-clipboard-check text-amber-400"></i>
                        <span>Thẩm Định Thu Mua</span>
                    </a>
                    <a href="/admin/book-copies" class="p-3 rounded-xl glass-panel border border-white/5 hover:border-brand-gold/40 text-xs font-medium text-slate-300 hover:text-brand-gold transition flex items-center gap-2.5">
                        <i class="fa-solid fa-barcode text-emerald-400"></i>
                        <span>Tra Cứu Bản Sách</span>
                    </a>
                </div>
            </div>
        </main>
    </div>

    <!-- Chart Configuration Script -->
    <script>
        const inventoryByStore = ${inventoryByStoreJson};
        const inventoryValue = ${inventoryValueJson};
        const conditionBreakdown = ${conditionBreakdownJson};
        const buybackStatus = ${buybackStatusJson};

        // Theme palette
        const goldColor = 'rgba(212, 175, 55, 0.85)';
        const emeraldColor = 'rgba(16, 185, 129, 0.85)';
        const terracottaColor = 'rgba(166, 85, 59, 0.85)';
        const forestColor = 'rgba(30, 56, 43, 0.85)';
        const textColor = '#94a3b8';
        const gridColor = 'rgba(255, 255, 255, 0.06)';

        // 1. Chart: Sách theo chi nhánh
        const storeLabels = Object.keys(inventoryByStore);
        const storeData = Object.values(inventoryByStore);
        new Chart(document.getElementById('storeInventoryChart'), {
            type: 'bar',
            data: {
                labels: storeLabels.length ? storeLabels : ['Chưa có dữ liệu'],
                datasets: [{
                    label: 'Số cuốn tồn kho',
                    data: storeData.length ? storeData : [0],
                    backgroundColor: goldColor,
                    borderColor: '#D4AF37',
                    borderWidth: 1,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { ticks: { color: textColor }, grid: { color: gridColor } },
                    y: { beginAtZero: true, ticks: { color: textColor, precision: 0 }, grid: { color: gridColor } }
                }
            }
        });

        // 2. Chart: Giá trị tồn kho
        const valLabels = Object.keys(inventoryValue);
        const valData = Object.values(inventoryValue);
        new Chart(document.getElementById('storeValueChart'), {
            type: 'bar',
            data: {
                labels: valLabels.length ? valLabels : ['Chưa có dữ liệu'],
                datasets: [{
                    label: 'Giá trị (VNĐ)',
                    data: valData.length ? valData : [0],
                    backgroundColor: emeraldColor,
                    borderColor: '#10b981',
                    borderWidth: 1,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { ticks: { color: textColor }, grid: { color: gridColor } },
                    y: { beginAtZero: true, ticks: { color: textColor }, grid: { color: gridColor } }
                }
            }
        });

        // 3. Chart: Tình trạng sách
        const condLabels = Object.keys(conditionBreakdown);
        const condData = Object.values(conditionBreakdown);
        new Chart(document.getElementById('conditionChart'), {
            type: 'doughnut',
            data: {
                labels: condLabels.length ? condLabels : ['Chưa có dữ liệu'],
                datasets: [{
                    data: condData.length ? condData : [1],
                    backgroundColor: ['#D4AF37', '#10B981', '#3B82F6', '#EF4444'],
                    borderColor: '#0b0c10',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { color: textColor, font: { size: 11 } } }
                }
            }
        });

        // 4. Chart: Trạng thái thu mua
        const bbLabels = Object.keys(buybackStatus);
        const bbData = Object.values(buybackStatus);
        new Chart(document.getElementById('buybackChart'), {
            type: 'pie',
            data: {
                labels: bbLabels.length ? bbLabels : ['Chưa có dữ liệu'],
                datasets: [{
                    data: bbData.length ? bbData : [1],
                    backgroundColor: ['#F59E0B', '#3B82F6', '#10B981', '#EF4444'],
                    borderColor: '#0b0c10',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { color: textColor, font: { size: 11 } } }
                }
            }
        });
    </script>
</body>
</html>