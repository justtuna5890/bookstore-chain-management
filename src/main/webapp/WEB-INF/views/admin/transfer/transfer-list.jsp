<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html class="dark" data-bs-theme="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Điều Chuyển Kho Liên Chi Nhánh — OldBook</title>
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
                            surface: '#15181E'
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
        .glass-input {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.08);
            color: #f1f5f9;
        }
        .glass-input:focus {
            background: rgba(255, 255, 255, 0.08);
            border-color: rgba(212, 175, 55, 0.45);
            outline: none;
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
        <!-- Unified Navigation Dock -->
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="transfers"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-2xl sm:rounded-[32px] p-3.5 sm:p-5 lg:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Top Nav & Breadcrumbs -->
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-white/5 mb-6">
                <div>
                    <div class="flex items-center gap-2 mb-1">
                        <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30">
                            ĐIỀU PHỐI MẠNG LƯỚI
                        </span>
                    </div>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        Phiếu Điều Chuyển Kho Liên Chi Nhánh
                    </h1>
                    <p class="text-xs text-slate-400 mt-0.5">Cân bằng cơ cấu tồn kho, xuất kho chuyển sách và xác nhận nhập hàng</p>
                </div>
                <div class="flex items-center gap-2.5">
                    <a href="/admin/export/excel/transfers" 
                       class="px-3.5 py-2.5 rounded-xl bg-blue-500/10 hover:bg-blue-500/20 text-blue-300 font-semibold text-xs border border-blue-500/30 transition flex items-center gap-2 shadow-sm">
                        <i class="fa-solid fa-file-excel text-blue-400"></i>
                        <c:choose>

                            <c:when test="${isStaffScoped}">

                                <span>Xuất Phiếu Chuyển Chi Nhánh</span>

                            </c:when>

                            <c:otherwise>

                                <span>Xuất Excel Phiếu Chuyển</span>

                            </c:otherwise>

                        </c:choose>
                    </a>
                    <a href="/admin/transfers/new" 
                       class="px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs shadow-glow-gold transition flex items-center gap-2">
                        <i class="fa-solid fa-plus"></i>
                        <span>Tạo Phiếu Mới</span>
                    </a>
                </div>
            </div>

            <!-- Filter Bar -->
            <div class="glass-panel rounded-2xl p-4 mb-6 border border-white/10 shadow-lg">
                <form action="/admin/transfers" method="get" class="grid grid-cols-1 sm:grid-cols-12 gap-3 items-center">
                    <div class="sm:col-span-5">
                        <c:choose>
                            <c:when test="${isStaffScoped}">
                                <div class="glass-input w-full px-3 py-2 text-xs rounded-xl bg-sky-500/10 border border-sky-500/30 text-sky-300 font-semibold flex items-center gap-1.5">
                                    <i class="fa-solid fa-store text-[10px]"></i>
                                    <span class="truncate">Phiếu liên quan: ${currentStoreName}</span>
                                </div>
                                <input type="hidden" name="storeId" value="${selectedStoreId}"/>
                            </c:when>
                            <c:otherwise>
                                <select name="storeId" class="glass-input w-full px-3 py-2 text-xs rounded-xl bg-[#161a22]">
                                    <option value="">-- Tất cả chi nhánh (Gửi hoặc Nhận) --</option>
                                    <c:forEach var="store" items="${stores}">
                                        <option value="${store.id}" ${selectedStoreId == store.id ? 'selected' : ''}>${store.name}</option>
                                    </c:forEach>
                                </select>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="sm:col-span-5">
                        <select name="status" class="glass-input w-full px-3 py-2 text-xs rounded-xl bg-[#161a22] text-slate-100">
                            <option value="">-- Tất cả trạng thái phiếu --</option>
                            <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>Chờ xuất kho (PENDING)</option>
                            <option value="IN_TRANSIT" ${selectedStatus == 'IN_TRANSIT' ? 'selected' : ''}>Đang vận chuyển (IN_TRANSIT)</option>
                            <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>Đã nhập kho (COMPLETED)</option>
                            <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy (CANCELLED)</option>
                        </select>
                    </div>
                    <div class="sm:col-span-2">
                        <button type="submit" class="w-full py-2 px-3 rounded-xl bg-white/10 hover:bg-white/20 text-white text-xs font-semibold transition">
                            Lọc Dữ Liệu
                        </button>
                    </div>
                </form>
            </div>

            <!-- Cảnh báo nếu nhân viên chưa được gán chi nhánh -->

            <c:if test="${unassignedStoreWarning}">

                <div class="mb-5 p-4 rounded-2xl bg-amber-500/15 border border-amber-500/40 text-amber-200 text-xs flex items-center gap-3 shadow-lg">

                    <i class="fa-solid fa-triangle-exclamation text-amber-400 text-xl shrink-0"></i>

                    <div>

                        <strong class="font-bold block text-sm mb-0.5">Tài khoản chưa được phân bổ chi nhánh công tác!</strong>

                        <span class="text-slate-300 leading-relaxed">Bạn không thể lập hoặc xem phiếu điều chuyển khi chưa được phân bổ chi nhánh. Vui lòng liên hệ Quản trị viên để được gán chi nhánh làm việc.</span>

                    </div>

                </div>

            </c:if>



            <!-- Feedback Messages -->
            <c:if test="${not empty successMessage}">
                <div class="mb-5 p-3.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs flex items-center justify-between">
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-circle-check"></i>
                        <span>${successMessage}</span>
                    </div>
                    <button onclick="this.parentElement.remove()" class="text-emerald-400 hover:text-emerald-200"><i class="fa-solid fa-xmark"></i></button>
                </div>
            </c:if>

            <!-- Main Glass Table Container -->
            <div class="glass-panel rounded-2xl border border-white/10 overflow-hidden shadow-lg">
                <div class="overflow-x-auto">
                    <table class="w-full text-left text-xs text-slate-300">
                        <thead class="bg-white/5 text-[11px] text-brand-gold uppercase tracking-wider border-b border-white/10">
                            <tr>
                                <th class="py-3.5 px-4 font-mono">Mã Phiếu</th>
                                <th class="py-3.5 px-4">Kho Gửi</th>
                                <th class="py-3.5 px-4">Kho Nhận</th>
                                <th class="py-3.5 px-4">Số Bản Sách</th>
                                <th class="py-3.5 px-4">Người Lập</th>
                                <th class="py-3.5 px-4 text-center">Trạng Thái</th>
                                <th class="py-3.5 px-4 text-right">Chi Tiết</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-white/5">
                            <c:forEach var="t" items="${transferPage.content}">
                                <tr class="hover:bg-white/5 transition">
                                    <td class="py-3.5 px-4 font-mono font-bold text-amber-300">#TR-${t.id}</td>
                                    <td class="py-3.5 px-4 font-semibold text-white">
                                        <div class="flex items-center gap-1.5">
                                            <i class="fa-solid fa-arrow-up-from-bracket text-rose-400 text-[10px]"></i>
                                            <span>${t.senderStoreName}</span>
                                        </div>
                                    </td>
                                    <td class="py-3.5 px-4 font-semibold text-white">
                                        <div class="flex items-center gap-1.5">
                                            <i class="fa-solid fa-arrow-down-to-bracket text-emerald-400 text-[10px]"></i>
                                            <span>${t.receiverStoreName}</span>
                                        </div>
                                    </td>
                                    <td class="py-3.5 px-4 text-slate-300">
                                        <span class="font-bold text-brand-gold font-mono">${t.itemCount}</span> cuốn
                                    </td>
                                    <td class="py-3.5 px-4 text-slate-400">
                                        <div>${t.creatorUsername}</div>
                                        <div class="text-[10px] text-slate-500 font-mono">
                                            ${t.createdAt}
                                        </div>
                                    </td>
                                    <td class="py-3.5 px-4 text-center">
                                        <c:choose>
                                            <c:when test="${t.status == 'PENDING'}">
                                                <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-amber-500/15 border border-amber-500/30 text-amber-400">
                                                    Chờ chuyển
                                                </span>
                                            </c:when>
                                            <c:when test="${t.status == 'IN_TRANSIT'}">
                                                <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-blue-500/15 border border-blue-500/30 text-blue-400">
                                                    Đang vận chuyển
                                                </span>
                                            </c:when>
                                            <c:when test="${t.status == 'COMPLETED'}">
                                                <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/15 border border-emerald-500/30 text-emerald-400">
                                                    Đã nhập kho
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-slate-500/15 border border-slate-500/30 text-slate-400">
                                                    Đã hủy
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="py-3.5 px-4 text-right">
                                        <a href="/admin/transfers/${t.id}" 
                                           class="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-white/5 hover:bg-brand-gold/20 border border-white/10 hover:border-brand-gold/40 text-slate-300 hover:text-brand-gold font-medium text-xs transition">
                                            <span>Xem Phiếu</span>
                                            <i class="fa-solid fa-chevron-right text-[10px]"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty transferPage.content}">
                                <tr>
                                    <td colspan="7" class="py-12 text-center text-slate-500">
                                        <i class="fa-solid fa-truck-ramp-box text-3xl mb-3 text-slate-600 block"></i>
                                        Chưa có phiếu chuyển kho nào khớp với điều kiện lọc.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination -->
                <c:if test="${transferPage.totalPages > 1}">
                    <div class="p-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-400">
                        <div>
                            Trang <span class="text-white font-medium">${transferPage.number + 1}</span> / <span class="text-white font-medium">${transferPage.totalPages}</span>
                            (Tổng <span class="text-brand-gold font-medium">${transferPage.totalElements}</span> phiếu)
                        </div>
                        <div class="flex items-center gap-1.5">
                            <c:forEach begin="0" end="${transferPage.totalPages - 1}" var="i">
                                <a href="?page=${i}&storeId=${selectedStoreId}&status=${selectedStatus}" 
                                   class="w-8 h-8 rounded-lg flex items-center justify-center font-medium transition ${transferPage.number == i ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300 border border-white/5'}">
                                    ${i + 1}
                                </a>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
            </div>
        </main>
    </div>
</body>
</html>
