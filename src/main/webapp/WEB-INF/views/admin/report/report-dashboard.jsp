<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>

<html class="dark" lang="vi">

<head>

    <meta charset="utf-8"/>

    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>

    <title>Báo Cáo Thống Kê Chuyên Sâu — OldBook</title>

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



    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col xl:flex-row items-start gap-5">

        <!-- Navigation Dock -->

        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">

            <jsp:param name="active" value="reports"/>

        </jsp:include>



        <!-- Main Display Frame -->

        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">

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

                        <span>Xuất File Excel Theo Kỳ</span>

                    </a>

                    <a href="/admin/dashboard" class="px-3.5 py-2 rounded-xl glass-panel-subtle hover:bg-white/10 text-slate-300 text-xs transition flex items-center gap-1.5">

                        <i class="fa-solid fa-gauge-high"></i>

                        <span>Về Dashboard</span>

                    </a>

                </div>

            </header>



            <!-- Filter Controls Bar -->

            <div class="glass-panel-subtle rounded-2xl p-4 border border-white/5 mt-6">

                <form action="/admin/reports" method="get" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 items-end">

                    <input type="hidden" name="tab" value="${currentTab}"/>



                    <div>

                        <label class="block text-xs font-medium text-slate-300 mb-1.5">

                            <i class="fa-solid fa-calendar-day text-brand-gold mr-1"></i>Từ Ngày

                        </label>

                        <input type="date" name="startDate" value="${startDate}" required

                               class="glass-input w-full px-3.5 py-2 text-xs rounded-xl bg-[#12151c] text-slate-100 border border-white/10"/>

                    </div>



                    <div>

                        <label class="block text-xs font-medium text-slate-300 mb-1.5">

                            <i class="fa-solid fa-calendar-check text-brand-gold mr-1"></i>Đến Ngày

                        </label>

                        <input type="date" name="endDate" value="${endDate}" required

                               class="glass-input w-full px-3.5 py-2 text-xs rounded-xl bg-[#12151c] text-slate-100 border border-white/10"/>

                    </div>



                    <div>

                        <label class="block text-xs font-medium text-slate-300 mb-1.5">

                            <i class="fa-solid fa-store text-brand-gold mr-1"></i>Chi Nhánh

                        </label>

                        <select name="storeId" class="glass-input w-full px-3.5 py-2 text-xs rounded-xl bg-[#15181E]">

                            <option value="">— Tất cả chi nhánh toàn chuỗi —</option>

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

            <div class="flex items-center gap-2 border-b border-white/10 mt-8 mb-4">

                <a href="/admin/reports?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}&tab=overview" 

                   class="px-4 py-2.5 text-xs font-semibold transition border-b-2 flex items-center gap-2 ${currentTab == 'overview' ? 'border-brand-gold text-brand-gold' : 'border-transparent text-slate-400 hover:text-white'}">

                    <i class="fa-solid fa-table-cells-large"></i>

                    <span>Tổng Hợp Số Liệu</span>

                </a>

                <a href="/admin/reports?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}&tab=buybacks" 

                   class="px-4 py-2.5 text-xs font-semibold transition border-b-2 flex items-center gap-2 ${currentTab == 'buybacks' ? 'border-brand-gold text-brand-gold' : 'border-transparent text-slate-400 hover:text-white'}">

                    <i class="fa-solid fa-hand-holding-dollar"></i>

                    <span>Chi Tiết Thu Mua (${summary.buybackRequests.size()})</span>

                </a>

                <a href="/admin/reports?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}&tab=transfers" 

                   class="px-4 py-2.5 text-xs font-semibold transition border-b-2 flex items-center gap-2 ${currentTab == 'transfers' ? 'border-brand-gold text-brand-gold' : 'border-transparent text-slate-400 hover:text-white'}">

                    <i class="fa-solid fa-truck-ramp-box"></i>

                    <span>Chi Tiết Chuyển Kho (${summary.stockTransfers.size()})</span>

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

                                <a href="/admin/reports/export/excel?startDate=${startDate}&endDate=${endDate}&storeId=${selectedStoreId != null ? selectedStoreId : ''}" 

                                   class="w-full py-2.5 px-4 rounded-xl bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 text-xs font-semibold transition flex items-center justify-center gap-2">

                                    <i class="fa-solid fa-download text-brand-gold"></i>

                                    <span>Tải toàn bộ bộ số liệu về máy tính (.xlsx)</span>

                                </a>

                            </div>

                        </div>

                    </div>

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

            </c:choose>

        </main>

    </div>

</body>

</html>

