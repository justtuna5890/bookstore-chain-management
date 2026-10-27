<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>

<html class="dark" data-bs-theme="dark" lang="vi">

<head>

    <meta charset="utf-8"/>

    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>

    <title>Báo Cáo Thống Kê Chuyên Sâu — OldBook</title>

        <!-- Bootstrap 5.3.3 Framework (Dark Theme Native) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
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



    <style>
        /* Universal Responsive Safeguards */
        html, body {
            overflow-x: hidden !important;
            width: 100% !important;
            max-width: 100vw !important;
        }
        img, svg, video {
            max-width: 100%;
            height: auto;
        }
        * {
            box-sizing: border-box;
            -webkit-tap-highlight-color: transparent;
        }


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

        .glass-input {

            background: rgba(255, 255, 255, 0.05);

            backdrop-filter: blur(12px);

            border: 1px solid rgba(255, 255, 255, 0.08);

            color: #e2e8f0;

        }

        .glass-input:focus {

            background: rgba(255, 255, 255, 0.08);

            border-color: rgba(212, 175, 55, 0.45);

            outline: none;

        }

        input[type="date"],
        input[type="date"].glass-input {
            color-scheme: dark !important;
            background-color: #12151c !important;
            color: #f1f5f9 !important;
            border: 1px solid rgba(255, 255, 255, 0.12) !important;
        }
        input[type="date"]::-webkit-calendar-picker-indicator {

            filter: invert(0.8);

            cursor: pointer;

        }

    
        /* Dark Luxury Styling for Select Dropdowns & Options */
        select, .glass-input {
            color-scheme: dark !important;
        }
        select option {
            background-color: #12151c !important;
            color: #f1f5f9 !important;
            padding: 8px 12px !important;
        }
        select option:hover,
        select option:focus,
        select option:checked {
            background-color: #1e2430 !important;
            color: #D4AF37 !important;
            font-weight: 600 !important;
        }
    </style>

</head>

<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-6 px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">

    <div class="ambient-glow top-12 left-1/4"></div>

    <div class="ambient-glow bottom-20 right-10"></div>



    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col lg:flex-row items-start gap-5">

        <!-- Navigation Dock -->

        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">

            <jsp:param name="active" value="reports"/>

        </jsp:include>



        <!-- Main Display Frame -->

        <main class="flex-1 w-full glass-panel rounded-2xl sm:rounded-[32px] p-3.5 sm:p-5 lg:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">

            <!-- Header bar -->

            <header class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-white/5">

                <div>

                    <div class="flex items-center gap-2 mb-1.5">

                        <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30 flex items-center gap-1.5">

                            <i class="fa-solid fa-chart-line text-[9px]"></i>

                            <span>BÁO CÁO CHUYÊN SÂU</span>

                        </span>

                        <span class="px-2.5 py-0.5 rounded-full bg-white/10 text-slate-300 text-[10px] font-medium border border-white/10">

                            ${summary.storeName}

                        </span>

                    </div>

                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">

                        Báo Cáo Vận Hành & Doanh Số Kho

                    </h1>

                    <p class="text-xs text-slate-400 mt-1">

                        Phân tích luân chuyển kho, thu mua sách cũ và định giá tài sản theo khoảng thời gian thực tế.

                    </p>

                </div>



                <div class="flex items-center gap-3">

                                        <a href="/admin/reports/export/excel?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}" 
                       class="px-4 py-2 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 text-white font-bold text-xs shadow-lg transition flex items-center gap-2">
                        <i class="fa-solid fa-file-excel"></i>
                        <span>Xuất File Excel</span>
                    </a>

                    <a href="/admin/reports/export/pdf?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}" 
                       target="_blank"
                       class="px-4 py-2 rounded-xl bg-gradient-to-r from-rose-600 to-amber-600 hover:from-rose-500 hover:to-amber-500 text-white font-bold text-xs shadow-lg transition flex items-center gap-2">
                        <i class="fa-solid fa-file-pdf"></i>
                        <span>Xuất Báo Cáo PDF (A4)</span>
                    </a>

                    <a href="/admin/dashboard" class="px-3.5 py-2 rounded-xl glass-panel-subtle hover:bg-white/10 text-slate-300 text-xs transition flex items-center gap-1.5">

                        <i class="fa-solid fa-gauge-high"></i>

                        <span>Về Dashboard</span>

                    </a>

                </div>

            </header>



                        <!-- Filter Controls Bar -->
            <div class="glass-panel-subtle rounded-2xl p-4 border border-white/5 mt-6">
                <!-- Preset Quick Buttons Bar -->
                <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-3 pb-3 border-b border-white/5">
                    <div class="flex items-center gap-2 text-xs text-slate-400">
                        <i class="fa-solid fa-bolt text-brand-gold"></i>
                        <span class="font-medium text-slate-300">Chọn nhanh mốc thời gian:</span>
                    </div>
                    <div class="flex flex-wrap items-center gap-1.5">
                        <button type="button" onclick="setQuickFilter('today')" class="px-2.5 py-1 rounded-lg text-xs font-medium bg-white/5 hover:bg-brand-gold/15 hover:text-brand-gold text-slate-300 transition border border-white/5">Hôm nay</button>
                        <button type="button" onclick="setQuickFilter('7days')" class="px-2.5 py-1 rounded-lg text-xs font-medium bg-white/5 hover:bg-brand-gold/15 hover:text-brand-gold text-slate-300 transition border border-white/5">7 ngày qua</button>
                        <button type="button" onclick="setQuickFilter('30days')" class="px-2.5 py-1 rounded-lg text-xs font-medium bg-white/5 hover:bg-brand-gold/15 hover:text-brand-gold text-slate-300 transition border border-white/5">30 ngày qua</button>
                        <button type="button" onclick="setQuickFilter('thisMonth')" class="px-2.5 py-1 rounded-lg text-xs font-medium bg-white/5 hover:bg-brand-gold/15 hover:text-brand-gold text-slate-300 transition border border-white/5">Tháng này</button>
                        <button type="button" onclick="setQuickFilter('thisQuarter')" class="px-2.5 py-1 rounded-lg text-xs font-medium bg-white/5 hover:bg-brand-gold/15 hover:text-brand-gold text-slate-300 transition border border-white/5">Quý này</button>
                        <button type="button" onclick="setQuickFilter('thisYear')" class="px-2.5 py-1 rounded-lg text-xs font-medium bg-white/5 hover:bg-brand-gold/15 hover:text-brand-gold text-slate-300 transition border border-white/5">Năm nay</button>
                        <button type="button" onclick="resetQuickFilter()" class="px-2.5 py-1 rounded-lg text-xs font-medium bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 transition border border-rose-500/20 flex items-center gap-1">
                            <i class="fa-solid fa-arrow-rotate-left text-[10px]"></i>
                            <span>Đặt lại</span>
                        </button>
                    </div>
                </div>

                <form id="reportFilterForm" action="/admin/reports" method="get" onsubmit="return validateDateRange()" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
                    <input type="hidden" name="tab" value="${currentTab}"/>

                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5">
                            <i class="fa-solid fa-calendar-day text-brand-gold mr-1"></i>Từ Ngày
                        </label>
                        <input type="date" id="inputStartDate" name="startDate" value="${startDate}" required
                               class="glass-input w-full px-3.5 py-2 text-xs rounded-xl bg-[#12151c] text-slate-100 border border-white/10"/>
                    </div>

                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5">
                            <i class="fa-solid fa-calendar-check text-brand-gold mr-1"></i>Đến Ngày
                        </label>
                        <input type="date" id="inputEndDate" name="endDate" value="${endDate}" required
                               class="glass-input w-full px-3.5 py-2 text-xs rounded-xl bg-[#12151c] text-slate-100 border border-white/10"/>
                    </div>

                    <div>
                        <label class="block text-xs font-medium text-slate-300 mb-1.5">
                            <i class="fa-solid fa-store text-brand-gold mr-1"></i>Chi Nhánh
                        </label>
                        <select name="storeId" class="glass-input w-full px-3.5 py-2 text-xs rounded-xl bg-[#15181E]">
                            <c:if test="${!isScopedToStore}">
                                <option value="">— Tất cả chi nhánh toàn chuỗi —</option>
                            </c:if>
                            <c:forEach var="st" items="${stores}">
                                <option value="${st.id}" ${selectedStoreId == st.id ? 'selected' : ''}>
                                    ${st.name}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div>
                        <button type="submit" class="w-full py-2 px-4 rounded-xl bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 text-slate-950 font-bold text-xs shadow-glow-gold transition flex items-center justify-center gap-2">
                            <i class="fa-solid fa-filter"></i>
                            <span>Áp Dụng Bộ Lọc</span>
                        </button>
                    </div>
                </form>
            </div>

<!-- 4 Metric Cards for Selected Period -->

            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">

                <!-- Card 1: Thu mua -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">

                    <div class="flex items-center justify-between mb-3">

                        <span class="text-xs text-slate-400 font-medium">Chi Phí Thu Mua (Kỳ Này)</span>

                        <div class="w-8 h-8 rounded-lg bg-yellow-500/10 text-brand-gold flex items-center justify-center">

                            <i class="fa-solid fa-hand-holding-dollar"></i>

                        </div>

                    </div>

                    <div class="text-xl font-bold font-serif text-brand-gold">

                        <fmt:formatNumber value="${summary.totalBuybackAmount}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>

                    </div>

                    <span class="text-[11px] text-slate-500 mt-1 block">Chi trả mua sách cũ từ độc giả</span>

                </div>



                <!-- Card 2: Đơn thu mua -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">

                    <div class="flex items-center justify-between mb-3">

                        <span class="text-xs text-slate-400 font-medium">Đơn Thu Mua Tiếp Nhận</span>

                        <div class="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center">

                            <i class="fa-solid fa-clipboard-list"></i>

                        </div>

                    </div>

                    <div class="text-2xl font-bold font-serif text-white">${summary.totalBuybacks}</div>

                    <div class="flex items-center gap-2 mt-1.5 text-[10px]">

                        <span class="text-emerald-400 font-medium"><i class="fa-solid fa-check"></i> ${summary.completedBuybacks} xong</span>

                        <span class="text-amber-300 font-medium"><i class="fa-solid fa-clock"></i> ${summary.pendingBuybacks} chờ</span>

                    </div>

                </div>



                <!-- Card 3: Phiếu chuyển kho -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">

                    <div class="flex items-center justify-between mb-3">

                        <span class="text-xs text-slate-400 font-medium">Phiếu Điều Chuyển Kho</span>

                        <div class="w-8 h-8 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center">

                            <i class="fa-solid fa-truck-ramp-box"></i>

                        </div>

                    </div>

                    <div class="text-2xl font-bold font-serif text-white">${summary.totalTransfers}</div>

                    <div class="flex items-center gap-2 mt-1.5 text-[10px]">

                        <span class="text-emerald-400 font-medium"><i class="fa-solid fa-circle-check"></i> ${summary.completedTransfers} đã nhận</span>

                        <span class="text-blue-300 font-medium"><i class="fa-solid fa-truck-fast"></i> ${summary.inTransitTransfers} đang chuyển</span>

                    </div>

                </div>



                <!-- Card 4: Sách luân chuyển -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">

                    <div class="flex items-center justify-between mb-3">

                        <span class="text-xs text-slate-400 font-medium">Sách Luân Chuyển Giữa Trạm</span>

                        <div class="w-8 h-8 rounded-lg bg-purple-500/10 text-purple-400 flex items-center justify-center">

                            <i class="fa-solid fa-boxes-stacked"></i>

                        </div>

                    </div>

                    <div class="text-2xl font-bold font-serif text-purple-300">${summary.totalCopiesTransferred}</div>

                    <span class="text-[11px] text-slate-500 mt-1 block">Bản sao đã được điều phối</span>

                </div>

            </div>



                        <!-- Detail Tabs Navigation -->
            <div class="flex items-center gap-2 border-b border-white/10 mt-8 mb-4 overflow-x-auto pb-1">
                <a href="/admin/reports?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}&tab=overview" 
                   class="px-4 py-2.5 text-xs font-semibold transition border-b-2 whitespace-nowrap flex items-center gap-2 ${currentTab == 'overview' ? 'border-brand-gold text-brand-gold' : 'border-transparent text-slate-400 hover:text-white'}">
                    <i class="fa-solid fa-table-cells-large"></i>
                    <span>Tổng Hợp Số Liệu</span>
                </a>
                <a href="/admin/reports?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}&tab=sales"
                   class="px-4 py-2.5 text-xs font-semibold transition border-b-2 whitespace-nowrap flex items-center gap-2 ${currentTab == 'sales' ? 'border-brand-gold text-brand-gold' : 'border-transparent text-slate-400 hover:text-white'}">
                    <i class="fa-solid fa-cart-shopping"></i>
                    <span>Doanh Thu Bán Hàng<c:if test="${summary.sales != null}"> (${summary.sales.orderCount})</c:if></span>
                </a>
                <a href="/admin/reports?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}&tab=buybacks" 
                   class="px-4 py-2.5 text-xs font-semibold transition border-b-2 whitespace-nowrap flex items-center gap-2 ${currentTab == 'buybacks' ? 'border-brand-gold text-brand-gold' : 'border-transparent text-slate-400 hover:text-white'}">
                    <i class="fa-solid fa-hand-holding-dollar"></i>
                    <span>Chi Tiết Thu Mua (${summary.buybackRequests.size()})</span>
                </a>
                <a href="/admin/reports?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}&tab=transfers" 
                   class="px-4 py-2.5 text-xs font-semibold transition border-b-2 whitespace-nowrap flex items-center gap-2 ${currentTab == 'transfers' ? 'border-brand-gold text-brand-gold' : 'border-transparent text-slate-400 hover:text-white'}">
                    <i class="fa-solid fa-truck-ramp-box"></i>
                    <span>Chi Tiết Chuyển Kho (${summary.stockTransfers.size()})</span>
                </a>
                <a href="/admin/reports?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}&tab=inventory" 
                   class="px-4 py-2.5 text-xs font-semibold transition border-b-2 whitespace-nowrap flex items-center gap-2 ${currentTab == 'inventory' ? 'border-brand-gold text-brand-gold' : 'border-transparent text-slate-400 hover:text-white'}">
                    <i class="fa-solid fa-boxes-stacked"></i>
                    <span>Kho Sách & Tồn Kho</span>
                </a>
                <a href="/admin/reports?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}&tab=customers" 
                   class="px-4 py-2.5 text-xs font-semibold transition border-b-2 whitespace-nowrap flex items-center gap-2 ${currentTab == 'customers' ? 'border-brand-gold text-brand-gold' : 'border-transparent text-slate-400 hover:text-white'}">
                    <i class="fa-solid fa-users"></i>
                    <span>Khách Hàng & Bạn Đọc (${summary.customerActivities.size()})</span>
                </a>
            </div>

            <!-- Tab Content -->

            <c:choose>

                <c:when test="${currentTab == 'overview'}">

                    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

                        <!-- Left: Tóm tắt thu mua & chuyển kho -->

                        <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 space-y-4">

                            <h3 class="text-sm font-bold text-white flex items-center gap-2">

                                <i class="fa-solid fa-clipboard-check text-brand-gold"></i>

                                <span>Tóm Tắt Chỉ Số Vận Hành Kỳ Này</span>

                            </h3>

                            <div class="divide-y divide-white/5 text-xs">

                                <div class="py-2.5 flex justify-between">

                                    <span class="text-slate-400">Khoảng thời gian báo cáo:</span>

                                    <span class="text-white font-medium">${startDate} đến ${endDate}</span>

                                </div>

                                <div class="py-2.5 flex justify-between">

                                    <span class="text-slate-400">Chi nhánh áp dụng:</span>

                                    <span class="text-brand-gold font-medium">${summary.storeName}</span>

                                </div>

                                <div class="py-2.5 flex justify-between">

                                    <span class="text-slate-400">Tỷ lệ duyệt đơn thu mua thành công:</span>

                                    <span class="text-emerald-400 font-bold">

                                        <c:choose>

                                            <c:when test="${summary.totalBuybacks > 0}">

                                                <fmt:formatNumber value="${(summary.completedBuybacks * 100.0) / summary.totalBuybacks}" maxFractionDigits="1"/>%

                                            </c:when>

                                            <c:otherwise>0%</c:otherwise>

                                        </c:choose>

                                    </span>

                                </div>

                                <div class="py-2.5 flex justify-between">

                                    <span class="text-slate-400">Chi phí thu mua trung bình mỗi đơn:</span>

                                    <span class="text-amber-300 font-bold">

                                        <c:choose>

                                            <c:when test="${summary.completedBuybacks > 0}">

                                                <fmt:formatNumber value="${summary.totalBuybackAmount.doubleValue() / summary.completedBuybacks}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>

                                            </c:when>

                                            <c:otherwise>0 ₫</c:otherwise>

                                        </c:choose>

                                    </span>

                                </div>

                                <div class="py-2.5 flex justify-between">

                                    <span class="text-slate-400">Số bản sách điều chuyển trung bình mỗi phiếu:</span>

                                    <span class="text-purple-300 font-bold">

                                        <c:choose>

                                            <c:when test="${summary.totalTransfers > 0}">

                                                <fmt:formatNumber value="${(summary.totalCopiesTransferred * 1.0) / summary.totalTransfers}" maxFractionDigits="1"/> cuốn

                                            </c:when>

                                            <c:otherwise>0 cuốn</c:otherwise>

                                        </c:choose>

                                    </span>

                                </div>

                            </div>

                        </div>



                        <!-- Right: Hiện trạng tồn kho trạm -->

                        <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 space-y-4">

                            <h3 class="text-sm font-bold text-white flex items-center gap-2">

                                <i class="fa-solid fa-vault text-emerald-400"></i>

                                <span>Hiện Trạng Kho & Tài Sản Sách Hiện Thời</span>

                            </h3>

                            <div class="divide-y divide-white/5 text-xs">

                                <div class="py-2.5 flex justify-between">

                                    <span class="text-slate-400">Tổng số bản sao sách đang lưu kho:</span>

                                    <span class="text-white font-bold">${summary.totalBookCopies} cuốn</span>

                                </div>

                                <div class="py-2.5 flex justify-between">

                                    <span class="text-slate-400">Tổng giá trị định giá kho hiện tại:</span>

                                    <span class="text-brand-gold font-bold font-serif text-sm">

                                        <fmt:formatNumber value="${summary.totalInventoryValue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>

                                    </span>

                                </div>

                                <div class="py-2.5 flex justify-between">

                                    <span class="text-slate-400">Định dạng xuất báo cáo hỗ trợ:</span>

                                    <span class="text-emerald-400 font-medium">Microsoft Excel (.xlsx), PDF</span>

                                </div>

                            </div>

                            <div class="pt-2">

                                                                <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                                    <a href="/admin/reports/export/excel?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}" 
                                       class="py-2.5 px-3 rounded-xl bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 text-xs font-semibold transition flex items-center justify-center gap-2">
                                        <i class="fa-solid fa-file-excel text-emerald-400"></i>
                                        <span>Tải file Excel (.xlsx)</span>
                                    </a>
                                    <a href="/admin/reports/export/pdf?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}" 
                                       target="_blank"
                                       class="py-2.5 px-3 rounded-xl bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 text-xs font-semibold transition flex items-center justify-center gap-2">
                                        <i class="fa-solid fa-file-pdf text-rose-400"></i>
                                        <span>In Báo Cáo PDF (A4)</span>
                                    </a>
                                </div>

                            </div>

                        </div>

                    </div>

                </c:when>

                <c:when test="${currentTab == 'sales'}">

                    <jsp:include page="/WEB-INF/views/admin/report/sales-tab.jsp"/>

                </c:when>

                <c:when test="${currentTab == 'buybacks'}">

                    <div class="overflow-x-auto rounded-xl border border-white/5">

                        <table class="w-full text-left text-xs">

                            <thead class="bg-white/5 text-slate-300 font-semibold uppercase tracking-wider text-[11px]">

                                <tr>

                                    <th class="py-3 px-4">Mã Đơn</th>

                                    <th class="py-3 px-4">Chi Nhánh</th>

                                    <th class="py-3 px-4">Khách Hàng</th>

                                    <th class="py-3 px-4">Số Cuốn</th>

                                    <th class="py-3 px-4">Tổng Tiền</th>

                                    <th class="py-3 px-4">Trạng Thái</th>

                                    <th class="py-3 px-4">Ngày Gửi</th>

                                    <th class="py-3 px-4 text-center">Thao Tác</th>

                                </tr>

                            </thead>

                            <tbody class="divide-y divide-white/5">

                                <c:forEach var="br" items="${summary.buybackRequests}">

                                    <tr class="hover:bg-white/[0.02] transition">

                                        <td class="py-3 px-4 font-bold font-mono text-brand-gold">#BB-${br.id}</td>

                                        <td class="py-3 px-4 text-slate-300">${br.store.name}</td>

                                        <td class="py-3 px-4">

                                            <div class="text-white font-medium">${br.customer.fullName}</div>

                                            <span class="text-[10px] text-slate-500">${br.customer.phone}</span>

                                        </td>

                                        <td class="py-3 px-4 text-slate-300">${br.items.size()} cuốn</td>

                                        <td class="py-3 px-4 font-bold font-serif text-brand-gold">

                                            <c:set var="totalPrice" value="0"/>

                                            <c:forEach var="item" items="${br.items}">

                                                <c:if test="${item.offeredPrice != null}">

                                                    <c:set var="totalPrice" value="${totalPrice + item.offeredPrice}"/>

                                                </c:if>

                                            </c:forEach>

                                            <fmt:formatNumber value="${totalPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>

                                        </td>

                                        <td class="py-3 px-4">

                                            <c:choose>

                                                <c:when test="${br.status == 'PENDING'}">

                                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-500/15 text-amber-300 border border-amber-500/30">Chờ duyệt</span>

                                                </c:when>

                                                <c:when test="${br.status == 'APPROVED'}">

                                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-sky-500/15 text-sky-300 border border-sky-500/30">Đã định giá</span>

                                                </c:when>

                                                <c:when test="${br.status == 'COMPLETED'}">

                                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/15 text-emerald-300 border border-emerald-500/30">Hoàn tất</span>

                                                </c:when>

                                                <c:when test="${br.status == 'REJECTED' || br.status == 'CANCELLED'}">

                                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-rose-500/15 text-rose-300 border border-rose-500/30">Đã từ chối</span>

                                                </c:when>

                                            </c:choose>

                                        </td>

                                        <td class="py-3 px-4 text-slate-400">${br.formattedCreatedAt}</td>

                                        <td class="py-3 px-4 text-center">

                                            <a href="/admin/buybacks/${br.id}" class="px-2.5 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-brand-gold text-xs transition" title="Xem chi tiết">

                                                <i class="fa-solid fa-eye"></i>

                                            </a>

                                        </td>

                                    </tr>

                                </c:forEach>

                                <c:if test="${empty summary.buybackRequests}">

                                    <tr>

                                        <td colspan="8" class="text-center py-8 text-slate-400">

                                            Không có đơn thu mua nào trong khoảng thời gian đã chọn.

                                        </td>

                                    </tr>

                                </c:if>

                            </tbody>

                        </table>

                    </div>

                </c:when>

                <c:when test="${currentTab == 'transfers'}">

                    <div class="overflow-x-auto rounded-xl border border-white/5">

                        <table class="w-full text-left text-xs">

                            <thead class="bg-white/5 text-slate-300 font-semibold uppercase tracking-wider text-[11px]">

                                <tr>

                                    <th class="py-3 px-4">Mã Phiếu</th>

                                    <th class="py-3 px-4">Chi Nhánh Gửi</th>

                                    <th class="py-3 px-4">Chi Nhánh Nhận</th>

                                    <th class="py-3 px-4">Người Tạo</th>

                                    <th class="py-3 px-4">Số Cuốn</th>

                                    <th class="py-3 px-4">Trạng Thái</th>

                                    <th class="py-3 px-4">Ngày Lập</th>

                                    <th class="py-3 px-4 text-center">Thao Tác</th>

                                </tr>

                            </thead>

                            <tbody class="divide-y divide-white/5">

                                <c:forEach var="st" items="${summary.stockTransfers}">

                                    <tr class="hover:bg-white/[0.02] transition">

                                        <td class="py-3 px-4 font-bold font-mono text-brand-gold">#TR-${st.id}</td>

                                        <td class="py-3 px-4 text-slate-300">${st.senderStore.name}</td>

                                        <td class="py-3 px-4 text-slate-300">${st.receiverStore.name}</td>

                                        <td class="py-3 px-4 text-slate-400">${st.creator.fullName}</td>

                                        <td class="py-3 px-4 text-purple-300 font-bold">${st.items.size()} cuốn</td>

                                        <td class="py-3 px-4">

                                            <c:choose>

                                                <c:when test="${st.status == 'PENDING'}">

                                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-500/15 text-amber-300 border border-amber-500/30">Chờ duyệt</span>

                                                </c:when>

                                                <c:when test="${st.status == 'IN_TRANSIT'}">

                                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-blue-500/15 text-blue-300 border border-blue-500/30">Đang chuyển</span>

                                                </c:when>

                                                <c:when test="${st.status == 'COMPLETED'}">

                                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/15 text-emerald-300 border border-emerald-500/30">Hoàn tất</span>

                                                </c:when>

                                                <c:when test="${st.status == 'CANCELLED'}">

                                                    <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-rose-500/15 text-rose-300 border border-rose-500/30">Đã hủy</span>

                                                </c:when>

                                            </c:choose>

                                        </td>

                                        <td class="py-3 px-4 text-slate-400">${st.formattedCreatedAt}</td>

                                        <td class="py-3 px-4 text-center">

                                            <a href="/admin/transfers/${st.id}" class="px-2.5 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-brand-gold text-xs transition" title="Xem chi tiết">

                                                <i class="fa-solid fa-eye"></i>

                                            </a>

                                        </td>

                                    </tr>

                                </c:forEach>

                                <c:if test="${empty summary.stockTransfers}">

                                    <tr>

                                        <td colspan="8" class="text-center py-8 text-slate-400">

                                            Không có phiếu điều chuyển nào trong khoảng thời gian đã chọn.

                                        </td>

                                    </tr>

                                </c:if>

                            </tbody>

                        </table>

                    </div>

                </c:when>

            
                <c:when test="${currentTab == 'inventory'}">
                    <div class="space-y-6">
                        <!-- 4 Mini inventory cards -->
                        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                            <div class="p-4 rounded-xl glass-panel-subtle border border-white/5">
                                <span class="text-xs text-slate-400 block mb-1">Tổng Số Bản Sách</span>
                                <div class="text-xl font-bold font-serif text-white">${summary.totalBookCopies} cuốn</div>
                                <span class="text-[10px] text-slate-500 mt-1 block">Toàn bộ bản sao trong kho</span>
                            </div>
                            <div class="p-4 rounded-xl glass-panel-subtle border border-white/5">
                                <span class="text-xs text-slate-400 block mb-1">Định Giá Kho Hiện Tại</span>
                                <div class="text-xl font-bold font-serif text-brand-gold">
                                    <fmt:formatNumber value="${summary.totalInventoryValue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                </div>
                                <span class="text-[10px] text-slate-500 mt-1 block">Giá trị niêm yết bán lẻ</span>
                            </div>
                            <div class="p-4 rounded-xl glass-panel-subtle border border-white/5">
                                <span class="text-xs text-slate-400 block mb-1">Sách Lưu Kho Lâu (>30 Ngày)</span>
                                <div class="text-xl font-bold font-serif text-amber-300">${summary.staleCopiesCount} cuốn</div>
                                <span class="text-[10px] text-slate-500 mt-1 block">Chưa phát sinh luân chuyển</span>
                            </div>
                            <div class="p-4 rounded-xl glass-panel-subtle border border-white/5">
                                <span class="text-xs text-slate-400 block mb-1">Đầu Sách Sắp Hết Kho</span>
                                <div class="text-xl font-bold font-serif text-rose-400">${summary.lowStockBooks.size()} đầu sách</div>
                                <span class="text-[10px] text-slate-500 mt-1 block">Còn tồn &le; 2 bản khả dụng</span>
                            </div>
                        </div>

                        <!-- 2 Breakdown Grids: Condition & Status -->
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                            <!-- Phân bổ theo Tình trạng -->
                            <div class="p-5 rounded-2xl glass-panel-subtle border border-white/5">
                                <h4 class="text-xs font-bold text-white uppercase tracking-wider mb-3 flex items-center gap-2">
                                    <i class="fa-solid fa-certificate text-brand-gold"></i>
                                    <span>Phân Bổ Tình Trạng Sách Thẩm Định</span>
                                </h4>
                                <div class="space-y-2">
                                    <c:forEach var="entry" items="${summary.copiesByCondition}">
                                        <div class="p-2.5 rounded-xl bg-white/[0.02] border border-white/5 flex items-center justify-between text-xs">
                                            <span class="text-slate-300 font-medium">
                                                <c:choose>
                                                    <c:when test="${entry.key == 'NEW'}"><span class="text-emerald-400 font-bold">Mới 100% (NEW)</span></c:when>
                                                    <c:when test="${entry.key == 'LIKE_NEW'}"><span class="text-teal-300 font-bold">Như mới (LIKE NEW)</span></c:when>
                                                    <c:when test="${entry.key == 'GOOD'}"><span class="text-sky-300 font-bold">Tốt (GOOD)</span></c:when>
                                                    <c:when test="${entry.key == 'FAIR'}"><span class="text-amber-300 font-bold">Chấp nhận được (FAIR)</span></c:when>
                                                    <c:when test="${entry.key == 'POOR'}"><span class="text-rose-400 font-bold">Cũ / Kém (POOR)</span></c:when>
                                                    <c:otherwise>${entry.key}</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <span class="font-bold text-white">${entry.value} cuốn</span>
                                        </div>
                                    </c:forEach>
                                    <c:if test="${empty summary.copiesByCondition}">
                                        <div class="text-xs text-slate-500 py-4 text-center italic">Chưa có dữ liệu phân loại tình trạng.</div>
                                    </c:if>
                                </div>
                            </div>

                            <!-- Phân bổ theo Trạng thái -->
                            <div class="p-5 rounded-2xl glass-panel-subtle border border-white/5">
                                <h4 class="text-xs font-bold text-white uppercase tracking-wider mb-3 flex items-center gap-2">
                                    <i class="fa-solid fa-tags text-sky-400"></i>
                                    <span>Phân Bổ Trạng Thái Bản Sao Sách</span>
                                </h4>
                                <div class="space-y-2">
                                    <c:forEach var="entry" items="${summary.copiesByStatus}">
                                        <div class="p-2.5 rounded-xl bg-white/[0.02] border border-white/5 flex items-center justify-between text-xs">
                                            <span class="text-slate-300 font-medium">
                                                <c:choose>
                                                    <c:when test="${entry.key == 'AVAILABLE'}"><span class="text-emerald-400 font-bold"><i class="fa-solid fa-circle-check text-[10px] mr-1"></i>Sẵn sàng bán (AVAILABLE)</span></c:when>
                                                    <c:when test="${entry.key == 'HOLDING'}"><span class="text-amber-300 font-bold"><i class="fa-solid fa-hand text-[10px] mr-1"></i>Đang giữ hàng (HOLDING)</span></c:when>
                                                    <c:when test="${entry.key == 'RESERVED'}"><span class="text-sky-300 font-bold"><i class="fa-solid fa-cart-shopping text-[10px] mr-1"></i>Khách đã đặt (RESERVED)</span></c:when>
                                                    <c:when test="${entry.key == 'IN_TRANSIT'}"><span class="text-sky-400 font-bold"><i class="fa-solid fa-truck text-[10px] mr-1"></i>Đang chuyển kho (IN TRANSIT)</span></c:when>
                                                    <c:when test="${entry.key == 'SOLD'}"><span class="text-slate-400 font-bold"><i class="fa-solid fa-bag-shopping text-[10px] mr-1"></i>Đã bán (SOLD)</span></c:when>
                                                    <c:otherwise>${entry.key}</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <span class="font-bold text-white">${entry.value} cuốn</span>
                                        </div>
                                    </c:forEach>
                                    <c:if test="${empty summary.copiesByStatus}">
                                        <div class="text-xs text-slate-500 py-4 text-center italic">Chưa có dữ liệu trạng thái kho.</div>
                                    </c:if>
                                </div>
                            </div>
                        </div>

                        <!-- Bảng cảnh báo đầu sách sắp hết kho -->
                        <div class="p-5 rounded-2xl glass-panel-subtle border border-white/5">
                            <div class="flex items-center justify-between pb-3 mb-3 border-b border-white/5">
                                <div>
                                    <h4 class="text-xs font-bold text-rose-300 uppercase tracking-wider flex items-center gap-2">
                                        <i class="fa-solid fa-triangle-exclamation text-rose-400"></i>
                                        <span>Danh Sách Đầu Sách Cảnh Báo Tồn Kho Thấp (&le; 2 Bản Khả Dụng)</span>
                                    </h4>
                                    <p class="text-[10px] text-slate-400 mt-0.5">Cần ưu tiên thẩm định thu mua bổ sung hoặc đề xuất điều chuyển từ chi nhánh khác</p>
                                </div>
                                <a href="/admin/book-copies" class="text-xs text-brand-gold hover:underline">Quản lý kho bản sách &rarr;</a>
                            </div>

                            <div class="overflow-x-auto">
                                <table class="w-full text-left text-xs">
                                    <thead class="bg-white/5 text-slate-300 uppercase tracking-wider text-[10px]">
                                        <tr>
                                            <th class="py-2.5 px-3">#</th>
                                            <th class="py-2.5 px-3">Tựa Đầu Sách</th>
                                            <th class="py-2.5 px-3">Tồn Khả Dụng</th>
                                            <th class="py-2.5 px-3">Mức Độ Cảnh Báo</th>
                                            <th class="py-2.5 px-3 text-center">Thao Tác</th>
                                        </tr>
                                    </thead>
                                    <tbody class="divide-y divide-white/5">
                                        <c:forEach var="item" items="${summary.lowStockBooks}" varStatus="status">
                                            <tr class="hover:bg-white/[0.02] transition">
                                                <td class="py-2.5 px-3 text-slate-400 font-mono">${status.index + 1}</td>
                                                <td class="py-2.5 px-3 font-semibold text-white">${item[0]}</td>
                                                <td class="py-2.5 px-3 font-bold font-serif ${item[1] <= 1 ? 'text-rose-400' : 'text-amber-300'}">${item[1]} cuốn</td>
                                                <td class="py-2.5 px-3">
                                                    <c:choose>
                                                        <c:when test="${item[1] <= 1}">
                                                            <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/20 text-rose-300 border border-rose-500/30">Cực kỳ khẩn cấp</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30">Cần gom hàng</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="py-2.5 px-3 text-center">
                                                    <a href="/admin/books" class="px-2 py-1 rounded bg-white/5 hover:bg-white/10 text-brand-gold text-[11px] transition">
                                                        <i class="fa-solid fa-arrow-up-right-from-square"></i>
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty summary.lowStockBooks}">
                                            <tr>
                                                <td colspan="5" class="py-6 text-center text-slate-500 italic text-xs">
                                                    Hiện không có đầu sách nào dưới ngưỡng tồn kho an toàn.
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </c:when>

                <c:when test="${currentTab == 'customers'}">
                    <div class="space-y-6">
                        <!-- Customer KPI Cards -->
                        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            <div class="p-4 rounded-xl glass-panel-subtle border border-white/5">
                                <span class="text-xs text-slate-400 block mb-1">Tổng Số Độc Giả Đăng Ký</span>
                                <div class="text-2xl font-bold font-serif text-white">${summary.totalCustomers}</div>
                                <span class="text-[10px] text-slate-500 mt-1 block">Tài khoản khách hàng toàn hệ thống</span>
                            </div>
                            <div class="p-4 rounded-xl glass-panel-subtle border border-white/5">
                                <span class="text-xs text-slate-400 block mb-1">Độc Giả Mới Trong Kỳ</span>
                                <div class="text-2xl font-bold font-serif text-emerald-400">${summary.newCustomersInPeriod}</div>
                                <span class="text-[10px] text-slate-500 mt-1 block">Gia nhập từ ${startDate} đến ${endDate}</span>
                            </div>
                            <div class="p-4 rounded-xl glass-panel-subtle border border-white/5">
                                <span class="text-xs text-slate-400 block mb-1">Độc Giả Tham Gia Thu Mua</span>
                                <div class="text-2xl font-bold font-serif text-brand-gold">${summary.customerActivities.size()}</div>
                                <span class="text-[10px] text-slate-500 mt-1 block">Khách hàng thanh lý sách cũ trong kỳ</span>
                            </div>
                        </div>

                        <!-- Table: Độc giả tích cực thanh lý sách cũ -->
                        <div class="overflow-x-auto rounded-xl border border-white/5">
                            <table class="w-full text-left text-xs">
                                <thead class="bg-white/5 text-slate-300 font-semibold uppercase tracking-wider text-[11px]">
                                    <tr>
                                        <th class="py-3 px-4">#</th>
                                        <th class="py-3 px-4">Họ và Tên Độc Giả</th>
                                        <th class="py-3 px-4">Email</th>
                                        <th class="py-3 px-4">Số Điện Thoại</th>
                                        <th class="py-3 px-4 text-center">Tổng Đơn Gửi</th>
                                        <th class="py-3 px-4 text-center">Đơn Hoàn Tất</th>
                                        <th class="py-3 px-4">Tổng Tiền Đã Thanh Toán</th>
                                    </tr>
                                </thead>
                                <tbody class="divide-y divide-white/5">
                                    <c:forEach var="c" items="${summary.customerActivities}" varStatus="status">
                                        <tr class="hover:bg-white/[0.02] transition">
                                            <td class="py-3 px-4 font-mono text-slate-400">${status.index + 1}</td>
                                            <td class="py-3 px-4 font-bold text-white">${c.fullName}</td>
                                            <td class="py-3 px-4 text-slate-300 font-mono text-[11px]">${c.email}</td>
                                            <td class="py-3 px-4 text-slate-300">${c.phone}</td>
                                            <td class="py-3 px-4 text-center font-bold text-amber-300">${c.totalRequests}</td>
                                            <td class="py-3 px-4 text-center font-bold text-emerald-400">${c.completedRequests}</td>
                                            <td class="py-3 px-4 font-serif font-bold text-brand-gold">
                                                <fmt:formatNumber value="${c.totalPayout}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty summary.customerActivities}">
                                        <tr>
                                            <td colspan="7" class="text-center py-8 text-slate-400 italic">
                                                Chưa có độc giả nào phát sinh yêu cầu bán sách cũ trong khoảng thời gian này.
                                            </td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:when>

            </c:choose>

        </main>

    </div>


    <!-- Quick Filter & Date Validation Scripts -->
    <script>
        function formatDate(d) {
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        }

        function setQuickFilter(type) {
            const now = new Date();
            let start = new Date();
            let end = new Date();

            switch (type) {
                case 'today':
                    start = new Date();
                    break;
                case '7days':
                    start.setDate(now.getDate() - 7);
                    break;
                case '30days':
                    start.setDate(now.getDate() - 30);
                    break;
                case 'thisMonth':
                    start = new Date(now.getFullYear(), now.getMonth(), 1);
                    break;
                case 'thisQuarter':
                    const currentQuarterMonth = Math.floor(now.getMonth() / 3) * 3;
                    start = new Date(now.getFullYear(), currentQuarterMonth, 1);
                    break;
                case 'thisYear':
                    start = new Date(now.getFullYear(), 0, 1);
                    break;
            }

            document.getElementById('inputStartDate').value = formatDate(start);
            document.getElementById('inputEndDate').value = formatDate(end);
            document.getElementById('reportFilterForm').submit();
        }

        function resetQuickFilter() {
            const now = new Date();
            const start = new Date();
            start.setDate(now.getDate() - 30);
            document.getElementById('inputStartDate').value = formatDate(start);
            document.getElementById('inputEndDate').value = formatDate(now);
            document.getElementById('reportFilterForm').submit();
        }

        function validateDateRange() {
            const startVal = document.getElementById('inputStartDate').value;
            const endVal = document.getElementById('inputEndDate').value;
            if (startVal && endVal && startVal > endVal) {
                alert('⚠️ Ngày bắt đầu không thể lớn hơn ngày kết thúc! Vui lòng chọn lại.');
                return false;
            }
            return true;
        }
    </script>

</body>

</html>

