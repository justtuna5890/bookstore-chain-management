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

            <jsp:param name="active" value="dashboard"/>

        </jsp:include>



        <!-- Main Display Frame -->

        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">

            <!-- Header bar with Role-Specific Presentation -->

            <header class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-white/5">

                <div>

                    <div class="flex flex-wrap items-center gap-2 mb-1.5">

                        <c:choose>

                            <c:when test="${isAdmin}">

                                <span class="px-3 py-1 rounded-full bg-rose-600/20 text-rose-300 text-[11px] font-semibold border border-rose-500/40 flex items-center gap-1.5 shadow-sm">

                                    <i class="fa-solid fa-shield-halved text-[10px] text-rose-400"></i>

                                    <span>Quản trị hệ thống</span>

                                </span>

                                <span class="px-2.5 py-0.5 rounded-full bg-white/10 text-slate-300 text-[10px] font-medium border border-white/10">

                                    Phạm vi: Toàn chuỗi

                                </span>

                            </c:when>

                            <c:when test="${isManager}">

                                <span class="px-3 py-1 rounded-full bg-purple-600/20 text-purple-300 text-[11px] font-semibold border border-purple-500/40 flex items-center gap-1.5 shadow-sm">

                                    <i class="fa-solid fa-briefcase text-[10px] text-purple-400"></i>

                                    <span>Quản lý vận hành</span>

                                </span>

                                <span class="px-2.5 py-0.5 rounded-full bg-purple-500/10 text-purple-200 text-[10px] font-medium border border-purple-500/20">

                                    ${not empty currentStoreName ? 'Phụ trách: '.concat(currentStoreName) : 'Giám sát điều phối chuỗi'}

                                </span>

                            </c:when>

                            <c:when test="${isStaff}">

                                <span class="px-3 py-1 rounded-full bg-sky-600/20 text-sky-300 text-[11px] font-semibold border border-sky-500/40 flex items-center gap-1.5 shadow-sm">

                                    <i class="fa-solid fa-user-check text-[10px] text-sky-400"></i>

                                    <span>Nhân viên chi nhánh</span>

                                </span>

                                <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30 flex items-center gap-1">

                                    <i class="fa-solid fa-store text-[9px]"></i>

                                    <span>${not empty currentStoreName ? currentStoreName : 'Chưa phân bổ chi nhánh'}</span>

                                </span>

                            </c:when>

                        </c:choose>

                    </div>



                    <c:choose>

                        <c:when test="${isAdmin}">

                            <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">

                                Tổng Quan Điều Hành Toàn Chuỗi

                            </h1>

                            <p class="text-xs text-slate-400 mt-1">Giám sát tổng thể doanh số, kho sách, nhân sự và mạng lưới chi nhánh trên toàn hệ thống.</p>

                        </c:when>

                        <c:when test="${isManager}">

                            <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">

                                Trung Tâm Giám Sát Vận Hành & Điều Phối

                            </h1>

                            <p class="text-xs text-slate-400 mt-1">Điều phối luân chuyển sách giữa các chi nhánh, phê duyệt đơn thu mua và kiểm soát tồn kho.</p>

                        </c:when>

                        <c:when test="${isStaff}">

                            <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">

                                Bàn Làm Việc Vận Hành Quầy & Kho

                            </h1>

                            <p class="text-xs text-slate-400 mt-1">Theo dõi tồn kho tại quầy, tiếp nhận thẩm định thu mua sách cũ và lập phiếu điều chuyển sách.</p>

                        </c:when>

                    </c:choose>

                </div>



                <div class="flex items-center gap-3">

                    <!-- Export Excel dropdown - Role Specific -->

                    <c:choose>

                        <c:when test="${isAdmin || isManager}">

                            <div class="relative group">

                                <button class="px-4 py-2 rounded-xl bg-white/10 hover:bg-white/15 text-white text-xs font-semibold border border-white/15 transition flex items-center gap-2">

                                    <i class="fa-solid fa-file-excel text-emerald-400"></i>

                                    <span>Xuất Báo Cáo Excel</span>

                                    <i class="fa-solid fa-chevron-down text-[10px] opacity-70"></i>

                                </button>

                                <div class="absolute right-0 mt-2 w-56 rounded-2xl glass-panel p-2 shadow-glass-elevated border border-white/10 hidden group-hover:block z-30">

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

                                    <div class="my-1 border-t border-white/10"></div>

                                    <a class="flex items-center gap-2 px-3 py-2 rounded-xl text-xs text-teal-300 hover:bg-white/10 hover:text-white transition" href="/admin/reports">

                                        <i class="fa-solid fa-chart-pie text-teal-400"></i>

                                        <span>Báo cáo theo kỳ (Từ ngày - Đến ngày)</span>

                                    </a>

                                </div>

                            </div>

                        </c:when>

                        <c:when test="${isStaff}">

                            <a href="/admin/export/inventory/excel" class="px-4 py-2 rounded-xl bg-white/10 hover:bg-white/15 text-white text-xs font-semibold border border-white/15 transition flex items-center gap-2">

                                <i class="fa-solid fa-file-excel text-emerald-400"></i>

                                <span>Xuất Tồn Kho Chi Nhánh</span>

                            </a>

                        </c:when>

                    </c:choose>



                    <a href="/" class="px-4 py-2 rounded-xl bg-gradient-to-r from-brand-gold to-yellow-500 hover:from-yellow-400 hover:to-amber-500 text-slate-950 text-xs font-bold shadow-glow-gold transition flex items-center gap-2">

                        <i class="fa-solid fa-globe"></i>

                        <span>Xem Sàn Bạn Đọc</span>

                    </a>

                </div>

            </header>







            <!-- Cảnh báo nếu nhân viên chưa được gán chi nhánh -->



            <c:if test="${unassignedStoreWarning}">



                <div class="mt-4 p-4 rounded-2xl bg-amber-500/15 border border-amber-500/40 text-amber-200 text-xs flex items-center gap-3 shadow-lg animate-pulse">



                    <i class="fa-solid fa-triangle-exclamation text-amber-400 text-xl shrink-0"></i>



                    <div>



                        <strong class="font-bold block text-sm mb-0.5">Tài khoản chưa được phân bổ chi nhánh công tác!</strong>



                        <span class="text-slate-300 leading-relaxed">Bạn đang truy cập ở chế độ an toàn giới hạn. Để thực hiện các nghiệp vụ kho, kiểm đếm và giao dịch tại quầy, vui lòng liên hệ Quản trị viên để được gán chi nhánh phụ trách.</span>



                    </div>



                </div>



            </c:if>



            
            <!-- ================= CỤM CẢNH BÁO VẬN HÀNH THỜI GIAN THỰC ================= -->
            <div class="mt-6 glass-panel rounded-2xl p-5 border border-white/10 shadow-glass-elevated">
                <div class="flex flex-col sm:flex-row sm:items-center justify-between pb-4 mb-4 border-b border-white/10 gap-3">
                    <div class="flex items-center gap-3">
                        <div class="relative flex items-center justify-center w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-400 shrink-0">
                            <i class="fa-solid fa-bell-concierge text-base"></i>
                            <c:if test="${not empty operationalAlerts}">
                                <span class="absolute -top-1 -right-1 flex h-3 w-3">
                                    <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-rose-400 opacity-75"></span>
                                    <span class="relative inline-flex rounded-full h-3 w-3 bg-rose-500"></span>
                                </span>
                            </c:if>
                        </div>
                        <div>
                            <div class="flex items-center gap-2">
                                <h2 class="text-sm font-bold text-white uppercase tracking-wider">
                                    Trung Tâm Cảnh Báo Vận Hành & Tắc Nghẽn
                                </h2>
                                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold ${not empty operationalAlerts ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30' : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'}">
                                    ${not empty operationalAlerts ? operationalAlerts.size().concat(' sự vụ cần xử lý') : 'Hoạt động ổn định'}
                                </span>
                            </div>
                            <p class="text-xs text-slate-400 mt-0.5">Giám sát tự động đơn thu mua trễ hẹn, phiếu chuyển kho quá giờ và cảnh báo mức tồn kho.</p>
                        </div>
                    </div>
                    <div class="text-xs text-slate-400 flex items-center gap-2">
                        <span class="inline-block w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                        <span>Quét liên tục theo dữ liệu kho thực</span>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${not empty operationalAlerts}">
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-3.5">
                            <c:forEach var="alert" items="${operationalAlerts}">
                                <div class="p-4 rounded-xl border transition flex flex-col justify-between gap-3 ${alert.level == 'DANGER' ? 'bg-rose-500/10 border-rose-500/30 hover:border-rose-500/50' : alert.level == 'WARNING' ? 'bg-amber-500/10 border-amber-500/30 hover:border-amber-500/50' : 'bg-sky-500/10 border-sky-500/30 hover:border-sky-500/50'}">
                                    <div class="flex items-start gap-3">
                                        <div class="w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${alert.level == 'DANGER' ? 'bg-rose-500/20 text-rose-400' : alert.level == 'WARNING' ? 'bg-amber-500/20 text-amber-400' : 'bg-sky-500/20 text-sky-400'}">
                                            <i class="fa-solid ${alert.level == 'DANGER' ? 'fa-triangle-exclamation' : alert.level == 'WARNING' ? 'fa-circle-exclamation' : 'fa-circle-info'} text-sm"></i>
                                        </div>
                                        <div class="min-w-0 flex-1">
                                            <div class="flex items-center gap-2 mb-1">
                                                <span class="text-xs font-bold text-white truncate">${alert.title}</span>
                                                <span class="px-2 py-0.5 rounded text-[9px] font-bold uppercase tracking-wider ${alert.level == 'DANGER' ? 'bg-rose-500/30 text-rose-200' : alert.level == 'WARNING' ? 'bg-amber-500/30 text-amber-200' : 'bg-sky-500/30 text-sky-200'}">
                                                    ${alert.level == 'DANGER' ? 'Khẩn cấp' : alert.level == 'WARNING' ? 'Chú ý' : 'Thông tin'}
                                                </span>
                                            </div>
                                            <p class="text-xs text-slate-300 leading-relaxed">${alert.message}</p>
                                        </div>
                                    </div>
                                    <div class="flex justify-end pt-2 border-t border-white/5">
                                        <a href="${alert.actionUrl}" class="px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition ${alert.level == 'DANGER' ? 'bg-rose-500/20 text-rose-300 hover:bg-rose-500/30' : alert.level == 'WARNING' ? 'bg-amber-500/20 text-amber-300 hover:bg-amber-500/30' : 'bg-sky-500/20 text-sky-300 hover:bg-sky-500/30'}">
                                            <span>${alert.actionLabel}</span>
                                            <i class="fa-solid fa-arrow-right text-[10px]"></i>
                                        </a>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="py-4 px-5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-xs text-emerald-300 flex items-center gap-3">
                            <i class="fa-solid fa-circle-check text-base text-emerald-400 shrink-0"></i>
                            <div>
                                <span class="font-bold text-white block">Toàn bộ quy trình vận hành đang trong trạng thái kiểm soát tốt!</span>
                                <span class="text-slate-400">Không phát hiện hồ sơ thu mua nào trễ hạn quá 48h, không có phiếu chuyển kho bị đình trệ quá 72h và kho sách duy trì mức tồn hợp lý.</span>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

<!-- 4 Metric Cards - Role Aware -->

            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">

                <!-- Card 1 -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">

                    <div class="flex items-center justify-between mb-3">

                        <c:choose>

                            <c:when test="${isStaff}">

                                <span class="text-xs text-slate-400 font-medium">Chi Nhánh Phụ Trách</span>

                            </c:when>

                            <c:otherwise>

                                <span class="text-xs text-slate-400 font-medium">Chi Nhánh Hoạt Động</span>

                            </c:otherwise>

                        </c:choose>

                        <div class="w-8 h-8 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center">

                            <i class="fa-solid fa-store"></i>

                        </div>

                    </div>

                    <c:choose>

                        <c:when test="${isStaff}">

                            <div class="text-lg font-bold font-serif text-white truncate" title="${currentStoreName}">${not empty currentStoreName ? currentStoreName : 'Chưa phân bổ'}</div>

                            <span class="text-[11px] text-slate-500 mt-1 block">Điểm làm việc quầy & kho</span>

                        </c:when>

                        <c:otherwise>

                            <div class="text-2xl font-bold font-serif text-white">${totalStores}</div>

                            <span class="text-[11px] text-slate-500 mt-1 block">Điểm tiếp nhận & kho hàng</span>

                        </c:otherwise>

                    </c:choose>

                </div>



                <!-- Card 2 -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">

                    <div class="flex items-center justify-between mb-3">

                        <span class="text-xs text-slate-400 font-medium">Đầu Sách Trong Hệ Thống</span>

                        <div class="w-8 h-8 rounded-lg bg-amber-500/10 text-brand-gold flex items-center justify-center">

                            <i class="fa-solid fa-book"></i>

                        </div>

                    </div>

                    <div class="text-2xl font-bold font-serif text-white">${totalBooks}</div>

                    <span class="text-[11px] text-slate-500 mt-1 block">Tác phẩm trong danh mục</span>

                </div>



                <!-- Card 3 -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">

                    <div class="flex items-center justify-between mb-3">

                        <c:choose>

                            <c:when test="${isStaff}">

                                <span class="text-xs text-slate-400 font-medium">Sách Khả Dụng (Chi Nhánh)</span>

                            </c:when>

                            <c:otherwise>

                                <span class="text-xs text-slate-400 font-medium">Bản Sách Toàn Chuỗi (Kho)</span>

                            </c:otherwise>

                        </c:choose>

                        <div class="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">

                            <i class="fa-solid fa-boxes-stacked"></i>

                        </div>

                    </div>

                    <div class="text-2xl font-bold font-serif text-emerald-400">${availableCopies}</div>

                    <span class="text-[11px] text-slate-500 mt-1 block">Bản sao sẵn sàng bán</span>

                </div>



                <!-- Card 4 -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-brand-gold/30 transition">

                    <div class="flex items-center justify-between mb-3">

                        <c:choose>

                            <c:when test="${isStaff}">

                                <span class="text-xs text-slate-400 font-medium">Giá Trị Kho Chi Nhánh</span>

                            </c:when>

                            <c:otherwise>

                                <span class="text-xs text-slate-400 font-medium">Giá Trị Kho Ước Tính</span>

                            </c:otherwise>

                        </c:choose>

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



            
            <!-- 4 Operational Metrics Row -->
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-4">
                <!-- Op Metric 1: Thu mua chờ duyệt -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-amber-500/30 transition">
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-xs text-slate-400 font-medium">Thu Mua Chờ Thẩm Định</span>
                        <div class="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center">
                            <i class="fa-solid fa-hand-holding-dollar"></i>
                        </div>
                    </div>
                    <div class="flex items-baseline justify-between">
                        <div class="text-2xl font-bold font-serif text-amber-300">${extendedMetrics.pendingBuybacks}</div>
                        <span class="px-2 py-0.5 rounded text-[10px] font-semibold ${extendedMetrics.pendingBuybacks > 0 ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30' : 'bg-white/5 text-slate-400'}">
                            ${extendedMetrics.pendingBuybacks > 0 ? 'Cần định giá' : 'Đã sạch hàng chờ'}
                        </span>
                    </div>
                    <div class="mt-2 flex items-center justify-between text-[11px]">
                        <span class="text-slate-500">Hạn phản hồi trong 48h</span>
                        <a href="/admin/buybacks" class="text-brand-gold hover:underline">Xử lý ngay &rarr;</a>
                    </div>
                </div>

                <!-- Op Metric 2: Phiếu chuyển kho đang vận chuyển -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-blue-500/30 transition">
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-xs text-slate-400 font-medium">Chuyển Kho Đang Lưu Thông</span>
                        <div class="w-8 h-8 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center">
                            <i class="fa-solid fa-truck-fast"></i>
                        </div>
                    </div>
                    <div class="flex items-baseline justify-between">
                        <div class="text-2xl font-bold font-serif text-sky-400">${extendedMetrics.activeTransfers}</div>
                        <span class="px-2 py-0.5 rounded text-[10px] font-semibold ${extendedMetrics.activeTransfers > 0 ? 'bg-sky-500/20 text-sky-300 border border-sky-500/30' : 'bg-white/5 text-slate-400'}">
                            ${extendedMetrics.activeTransfers > 0 ? 'Đang chuyển' : 'Đã nhập kho đủ'}
                        </span>
                    </div>
                    <div class="mt-2 flex items-center justify-between text-[11px]">
                        <span class="text-slate-500">Luân chuyển giữa các chi nhánh</span>
                        <a href="/admin/transfers" class="text-brand-gold hover:underline">Xem lộ trình &rarr;</a>
                    </div>
                </div>

                <!-- Op Metric 3: Cảnh báo sách sắp hết -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-rose-500/30 transition">
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-xs text-slate-400 font-medium">Đầu Sách Sắp Hết Kho</span>
                        <div class="w-8 h-8 rounded-lg bg-rose-500/10 text-rose-400 flex items-center justify-center">
                            <i class="fa-solid fa-triangle-exclamation"></i>
                        </div>
                    </div>
                    <div class="flex items-baseline justify-between">
                        <div class="text-2xl font-bold font-serif text-rose-400">${extendedMetrics.lowStockCount}</div>
                        <span class="px-2 py-0.5 rounded text-[10px] font-semibold ${extendedMetrics.lowStockCount > 0 ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30' : 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'}">
                            ${extendedMetrics.lowStockCount > 0 ? 'Tồn <= 2 cuốn' : 'Đủ tồn kho'}
                        </span>
                    </div>
                    <div class="mt-2 flex items-center justify-between text-[11px]">
                        <span class="text-slate-500">Ưu tiên bổ sung hoặc thu mua</span>
                        <a href="/admin/book-copies" class="text-brand-gold hover:underline">Chi tiết kho &rarr;</a>
                    </div>
                </div>

                <!-- Op Metric 4: Tỷ lệ hoàn tất thu mua -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 hover:border-emerald-500/30 transition">
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-xs text-slate-400 font-medium">Tỷ Lệ Thu Mua Thành Công</span>
                        <div class="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
                            <i class="fa-solid fa-percent"></i>
                        </div>
                    </div>
                    <div class="flex items-baseline justify-between">
                        <div class="text-2xl font-bold font-serif text-emerald-400">${extendedMetrics.buybackApprovalRate}%</div>
                        <span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                            Thành công / Hoàn tất
                        </span>
                    </div>
                    <div class="mt-2 flex items-center justify-between text-[11px]">
                        <span class="text-slate-500">${extendedMetrics.completedBuybacks} hoàn tất / ${extendedMetrics.rejectedBuybacks} từ chối</span>
                        <a href="/admin/buybacks" class="text-brand-gold hover:underline">Báo cáo &rarr;</a>
                    </div>
                </div>
            </div>

<!-- 4 Visual Charts Grid -->

            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">

                <!-- Chart 1: Sách theo chi nhánh -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">

                    <h3 class="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">

                        <i class="fa-solid fa-chart-column text-brand-gold"></i>

                        <c:choose>

                            <c:when test="${isStaff}">

                                <span>Tồn Kho Tại Chi Nhánh (${currentStoreName})</span>

                            </c:when>

                            <c:otherwise>

                                <span>Phân Bổ Tồn Kho Theo Chi Nhánh</span>

                            </c:otherwise>

                        </c:choose>

                    </h3>

                    <div class="h-64 flex items-center justify-center">

                        <canvas id="storeInventoryChart"></canvas>

                    </div>

                </div>



                <!-- Chart 2: Giá trị tồn kho -->

                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5">

                    <h3 class="text-sm font-semibold text-slate-200 mb-4 flex items-center gap-2">

                        <i class="fa-solid fa-sack-dollar text-emerald-400"></i>

                        <c:choose>

                            <c:when test="${isStaff}">

                                <span>Định Giá Tồn Kho Chi Nhánh (VNĐ)</span>

                            </c:when>

                            <c:otherwise>

                                <span>Giá Trị Tồn Kho Ước Tính (VNĐ)</span>

                            </c:otherwise>

                        </c:choose>

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




                        
            <!-- Ma trận luồng luân chuyển kho liên chi nhánh -->
            <c:if test="${not empty transferFlow}">
                <div class="mt-6 glass-panel-subtle rounded-2xl p-5 border border-white/5">
                    <div class="flex items-center justify-between pb-3 mb-4 border-b border-white/5">
                        <div class="flex items-center gap-2">
                            <div class="w-8 h-8 rounded-xl bg-purple-500/15 border border-purple-500/30 flex items-center justify-center text-purple-400">
                                <i class="fa-solid fa-arrows-split-up-and-left text-xs"></i>
                            </div>
                            <div>
                                <h3 class="text-xs uppercase tracking-wider font-semibold text-purple-300">
                                    Ma Trận Luồng Điều Chuyển Đã Hoàn Tất
                                </h3>
                                <p class="text-[10px] text-slate-400">Tần suất và lộ trình luân chuyển sách giữa các cặp chi nhánh</p>
                            </div>
                        </div>
                        <a href="/admin/transfers" class="text-[11px] text-brand-gold hover:underline font-medium">Quản lý phiếu chuyển &rarr;</a>
                    </div>
                    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                        <c:forEach var="entry" items="${transferFlow}">
                            <div class="p-3 rounded-xl bg-white/[0.02] border border-white/5 flex items-center justify-between">
                                <div class="text-xs text-slate-300 font-medium truncate pr-2" title="${entry.key}">${entry.key}</div>
                                <span class="px-2.5 py-1 rounded-lg bg-purple-500/20 text-purple-300 font-bold text-xs shrink-0">${entry.value} chuyến</span>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

<!-- 5. Active Operations: Recent Buybacks & Stock Transfers Directly on Dashboard -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-5 mt-6">
                <!-- Box 1: Recent Buyback Requests -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between pb-3 mb-3 border-b border-white/5">
                            <div class="flex items-center gap-2">
                                <div class="w-8 h-8 rounded-xl bg-amber-500/15 border border-amber-500/30 flex items-center justify-center text-amber-400">
                                    <i class="fa-solid fa-handshake text-xs"></i>
                                </div>
                                <div>
                                    <h3 class="text-xs uppercase tracking-wider font-semibold text-amber-300">
                                        Thu Mua Sách Cũ Cần Xử Lý
                                    </h3>
                                    <p class="text-[10px] text-slate-400">Hồ sơ tiếp nhận thẩm định và báo giá từ bạn đọc</p>
                                </div>
                            </div>
                            <a href="/admin/buybacks" class="text-[11px] text-brand-gold hover:text-brand-goldLight transition flex items-center gap-1 font-medium">
                                <span>Xem tất cả</span>
                                <i class="fa-solid fa-arrow-right text-[9px]"></i>
                            </a>
                        </div>

                        <div class="space-y-2.5">
                            <c:forEach var="bb" items="${recentBuybacks}">
                                <div class="p-3 rounded-xl bg-white/[0.02] hover:bg-white/[0.05] border border-white/5 transition flex items-center justify-between gap-3 text-xs">
                                    <div class="min-w-0">
                                        <div class="flex items-center gap-2 mb-0.5">
                                            <span class="font-mono font-bold text-amber-300">#BB-${bb.id}</span>
                                            <span class="font-semibold text-white truncate">${bb.customer.fullName}</span>
                                        </div>
                                        <div class="text-[10px] text-slate-400 flex items-center gap-2">
                                            <span><i class="fa-solid fa-store text-[9px] mr-1 text-slate-500"></i>${bb.store.name}</span>
                                            <span>•</span>
                                            <span>${bb.items.size()} cuốn</span>
                                        </div>
                                    </div>
                                    <div class="flex items-center gap-2.5 shrink-0">
                                        <c:choose>
                                            <c:when test="${bb.status == 'PENDING'}">
                                                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-500/15 border border-amber-500/30 text-amber-300">Chờ định giá</span>
                                            </c:when>
                                            <c:when test="${bb.status == 'APPROVED'}">
                                                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-sky-500/15 border border-sky-500/30 text-sky-300">Đã định giá</span>
                                            </c:when>
                                            <c:when test="${bb.status == 'COMPLETED'}">
                                                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/15 border border-emerald-500/30 text-emerald-300">Hoàn tất</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-rose-500/15 border border-rose-500/30 text-rose-300">Đã từ chối</span>
                                            </c:otherwise>
                                        </c:choose>
                                        <a href="/admin/buybacks/${bb.id}" class="px-2.5 py-1 rounded-lg bg-brand-gold/15 hover:bg-brand-gold/25 border border-brand-gold/30 text-brand-gold text-[11px] font-medium transition" title="Xem chi tiết & thẩm định">
                                            <i class="fa-solid fa-arrow-up-right-from-square"></i>
                                        </a>
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty recentBuybacks}">
                                <div class="py-6 text-center text-slate-500 text-xs italic">
                                    Không có yêu cầu thu mua nào gần đây.
                                </div>
                            </c:if>
                        </div>
                    </div>
                </div>

                <!-- Box 2: Recent Stock Transfers -->
                <div class="glass-panel-subtle rounded-2xl p-5 border border-white/5 flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between pb-3 mb-3 border-b border-white/5">
                            <div class="flex items-center gap-2">
                                <div class="w-8 h-8 rounded-xl bg-purple-500/15 border border-purple-500/30 flex items-center justify-center text-purple-400">
                                    <i class="fa-solid fa-truck-fast text-xs"></i>
                                </div>
                                <div>
                                    <h3 class="text-xs uppercase tracking-wider font-semibold text-purple-300">
                                        Phiếu Điều Chuyển Kho Gần Đây
                                    </h3>
                                    <p class="text-[10px] text-slate-400">Luân chuyển bản sao sách giữa các chi nhánh chuỗi</p>
                                </div>
                            </div>
                            <a href="/admin/transfers" class="text-[11px] text-brand-gold hover:text-brand-goldLight transition flex items-center gap-1 font-medium">
                                <span>Xem tất cả</span>
                                <i class="fa-solid fa-arrow-right text-[9px]"></i>
                            </a>
                        </div>

                        <div class="space-y-2.5">
                            <c:forEach var="tr" items="${recentTransfers}">
                                <div class="p-3 rounded-xl bg-white/[0.02] hover:bg-white/[0.05] border border-white/5 transition flex items-center justify-between gap-3 text-xs">
                                    <div class="min-w-0">
                                        <div class="flex items-center gap-2 mb-0.5">
                                            <span class="font-mono font-bold text-purple-300">#TR-${tr.id}</span>
                                            <span class="font-semibold text-white truncate">${tr.senderStore.name} → ${tr.receiverStore.name}</span>
                                        </div>
                                        <div class="text-[10px] text-slate-400 flex items-center gap-2">
                                            <span><i class="fa-solid fa-user-pen text-[9px] mr-1 text-slate-500"></i>${tr.creator.fullName}</span>
                                            <span>•</span>
                                            <span>${tr.items.size()} bản sách</span>
                                        </div>
                                    </div>
                                    <div class="flex items-center gap-2.5 shrink-0">
                                        <c:choose>
                                            <c:when test="${tr.status == 'PENDING'}">
                                                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-500/15 border border-amber-500/30 text-amber-300">Chờ chuyển</span>
                                            </c:when>
                                            <c:when test="${tr.status == 'IN_TRANSIT'}">
                                                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-blue-500/15 border border-blue-500/30 text-blue-300">Đang chuyển</span>
                                            </c:when>
                                            <c:when test="${tr.status == 'COMPLETED'}">
                                                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/15 border border-emerald-500/30 text-emerald-300">Đã nhập kho</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-500/15 border border-slate-500/30 text-slate-400">Đã hủy</span>
                                            </c:otherwise>
                                        </c:choose>
                                        <a href="/admin/transfers/${tr.id}" class="px-2.5 py-1 rounded-lg bg-brand-gold/15 hover:bg-brand-gold/25 border border-brand-gold/30 text-brand-gold text-[11px] font-medium transition" title="Xem chi tiết phiếu chuyển">
                                            <i class="fa-solid fa-arrow-up-right-from-square"></i>
                                        </a>
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty recentTransfers}">
                                <div class="py-6 text-center text-slate-500 text-xs italic">
                                    Không có phiếu điều chuyển nào gần đây.
                                </div>
                            </c:if>
                        </div>
                    </div>
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