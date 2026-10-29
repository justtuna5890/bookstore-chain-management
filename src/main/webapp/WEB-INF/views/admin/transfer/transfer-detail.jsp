<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="userRole" value="${pageContext.request.userPrincipal != null ? pageContext.request.userPrincipal.principal.user.role.name() : ''}" />
<c:set var="uStoreId" value="${pageContext.request.userPrincipal != null && pageContext.request.userPrincipal.principal.user.store != null ? pageContext.request.userPrincipal.principal.user.store.id : null}" />
<c:set var="uName" value="${pageContext.request.userPrincipal != null ? pageContext.request.userPrincipal.name : ''}" />
<c:set var="isStaff" value="${userRole == 'STAFF'}" />
<c:set var="isCreator" value="${transfer.creator.username == uName}" />
<c:set var="isSender" value="${uStoreId != null && uStoreId == transfer.senderStore.id}" />
<c:set var="isReceiver" value="${uStoreId != null && uStoreId == transfer.receiverStore.id}" />
<!DOCTYPE html>
<html class="dark" data-bs-theme="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Phiếu Chuyển Kho #${transfer.id} — OldBook</title>
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
        /* Universal Responsive Baseline */
        html, body {
            min-height: 100%;
            margin: 0;
            padding: 0;
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
    </style>
</head>
<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-3 sm:py-6 px-2.5 sm:px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0" aria-hidden="true">
        <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>
    </div>

<div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col lg:flex-row items-start gap-5">
        <!-- Unified Navigation Dock -->
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="transfers"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-xl sm:rounded-2xl lg:rounded-[32px] p-2.5 sm:p-4 lg:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Top Nav & Actions -->
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-white/5 mb-6">
                <div>
                    <a href="/admin/transfers" class="text-xs text-brand-gold hover:text-brand-goldLight transition flex items-center gap-1.5 mb-1.5">
                        <i class="fa-solid fa-arrow-left"></i>
                        <span>Quay lại Danh sách phiếu chuyển kho</span>
                    </a>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        Chi Tiết Phiếu Điều Chuyển Kho #${transfer.id}
                    </h1>
                    <p class="text-xs text-slate-400 mt-0.5">Tiến trình vận chuyển và kiểm đếm bản sách giữa các kho chi nhánh</p>
                </div>
                <div class="flex items-center gap-2.5">
                    <a href="/admin/export/pdf/transfer/${transfer.id}" target="_blank" 
                       class="px-3.5 py-2.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 font-semibold text-xs border border-rose-500/30 transition flex items-center gap-2 shadow-sm">
                        <i class="fa-solid fa-file-pdf text-rose-400"></i>
                        <span>Xuất Phiếu PDF</span>
                    </a>
                    <a href="/admin/transfers" class="px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-slate-300 text-xs transition">
                        Đóng
                    </a>
                </div>
            </div>

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
            <c:if test="${not empty errorMessage}">
                <div class="mb-5 p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/30 text-rose-300 text-xs flex items-center justify-between">
                    <div class="flex items-center gap-2">
                        <i class="fa-solid fa-triangle-exclamation"></i>
                        <span>${errorMessage}</span>
                    </div>
                    <button onclick="this.parentElement.remove()" class="text-rose-400 hover:text-rose-200"><i class="fa-solid fa-xmark"></i></button>
                </div>
            </c:if>

            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
                <!-- Left Info Card (5 Cols) -->
                <div class="lg:col-span-5">
                    <div class="glass-panel rounded-2xl p-6 border border-white/10 h-full flex flex-col justify-between shadow-lg">
                        <div>
                            <h3 class="text-xs uppercase tracking-wider font-semibold text-brand-gold mb-4">
                                Thông Tin Điều Chuyển
                            </h3>
                            <div class="space-y-3 text-xs">
                                <div class="flex items-center justify-between pb-2 border-b border-white/5">
                                    <span class="text-slate-400">Mã lệnh:</span>
                                    <span class="font-mono text-amber-300 font-bold">#TR-${transfer.id}</span>
                                </div>
                                <div class="flex items-center justify-between pb-2 border-b border-white/5">
                                    <span class="text-slate-400">Chi nhánh xuất (Gửi):</span>
                                    <span class="font-semibold text-white">${transfer.senderStore.name}</span>
                                </div>
                                <div class="flex items-center justify-between pb-2 border-b border-white/5">
                                    <span class="text-slate-400">Chi nhánh nhập (Nhận):</span>
                                    <span class="font-semibold text-white">${transfer.receiverStore.name}</span>
                                </div>
                                <div class="flex items-center justify-between pb-2 border-b border-white/5">
                                    <span class="text-slate-400">Người lập phiếu:</span>
                                    <span class="text-slate-300">${transfer.creator.fullName} (@${transfer.creator.username})</span>
                                </div>
                                <div class="flex items-center justify-between pb-2 border-b border-white/5">
                                    <span class="text-slate-400">Thời gian tạo:</span>
                                    <span class="text-slate-400 font-mono text-[11px]">
                                        ${transfer.createdAt}
                                    </span>
                                </div>
                                <div class="flex items-center justify-between pb-2 border-b border-white/5">
                                    <span class="text-slate-400">Trạng thái hiện tại:</span>
                                    <c:choose>
                                        <c:when test="${transfer.status == 'PENDING'}">
                                            <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-amber-500/15 border border-amber-500/30 text-amber-400">Chờ chuyển</span>
                                        </c:when>
                                        <c:when test="${transfer.status == 'IN_TRANSIT'}">
                                            <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-blue-500/15 border border-blue-500/30 text-blue-400">Đang vận chuyển</span>
                                        </c:when>
                                        <c:when test="${transfer.status == 'COMPLETED'}">
                                            <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/15 border border-emerald-500/30 text-emerald-400">Đã nhập kho</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-slate-500/15 border border-slate-500/30 text-slate-400">Đã hủy</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="pt-2">
                                    <span class="text-slate-400 block mb-1">Ghi chú điều chuyển:</span>
                                    <p class="text-slate-300 italic bg-white/5 p-2.5 rounded-xl text-xs border border-white/5">${not empty transfer.note ? transfer.note : 'Không có ghi chú'}</p>
                                </div>
                            </div>
                        </div>

                        <!-- Action Form theo vai trò -->
                        <div class="mt-6 pt-4 border-t border-white/10 space-y-2">
                            <!-- Nút gửi hàng (Chỉ khi PENDING) -->
                            <c:if test="${transfer.status == 'PENDING'}">
                                <c:choose>
                                    <c:when test="${isStaff && isCreator}">
                                        <button type="button" disabled class="w-full py-2.5 px-3 rounded-xl bg-white/5 border border-white/10 text-slate-400 font-semibold text-xs cursor-not-allowed flex items-center justify-center gap-2">
                                            <i class="fa-solid fa-lock text-amber-400 text-xs"></i>
                                            <span>Người lập phiếu không thể tự duyệt xuất kho</span>
                                        </button>
                                    </c:when>
                                    <c:when test="${isStaff && !isSender}">
                                        <div class="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-300 text-xs text-center leading-relaxed">
                                            <i class="fa-solid fa-clock text-xs mr-1"></i>Phiếu đang chờ chi nhánh xuất (<strong>${transfer.senderStore.name}</strong>) phê duyệt xuất kho.
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="/admin/transfers/${transfer.id}/status" method="post">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <input type="hidden" name="status" value="IN_TRANSIT" />
                                            <button type="submit" 
                                                    onclick="return confirm('Xác nhận đã xuất kho và bắt đầu vận chuyển?')"
                                                    class="w-full py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 text-white font-bold text-xs transition shadow flex items-center justify-center gap-2">
                                                <i class="fa-solid fa-truck-fast"></i>
                                                <span>Xác Nhận Xuất Kho & Vận Chuyển</span>
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>

                                <c:if test="${!isStaff || isSender || isCreator}">
                                    <form action="/admin/transfers/${transfer.id}/status" method="post" class="mt-2">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                        <input type="hidden" name="status" value="CANCELLED" />
                                        <button type="submit" 
                                                onclick="return confirm('Hủy bỏ đợt điều chuyển này?')"
                                                class="w-full py-2 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 text-xs transition border border-rose-500/20">
                                            Hủy Đợt Điều Chuyển
                                        </button>
                                    </form>
                                </c:if>
                            </c:if>

                            <!-- Nút nhận hàng (Chỉ khi IN_TRANSIT) -->
                            <c:if test="${transfer.status == 'IN_TRANSIT'}">
                                <c:choose>
                                    <c:when test="${isStaff && !isReceiver}">
                                        <div class="p-3 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-300 text-xs text-center leading-relaxed">
                                            <i class="fa-solid fa-truck-fast text-xs mr-1"></i>Sách đang trên đường vận chuyển tới <strong>${transfer.receiverStore.name}</strong>. Chỉ nhân viên chi nhánh nhận mới có quyền kiểm đếm và hoàn tất nhập kho.
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="/admin/transfers/${transfer.id}/status" method="post">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <input type="hidden" name="status" value="COMPLETED" />
                                            <button type="submit" 
                                                    onclick="return confirm('Xác nhận đã kiểm đếm đầy đủ và nhận sách vào kho đích?')"
                                                    class="w-full py-2.5 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 text-white font-bold text-xs transition shadow flex items-center justify-center gap-2">
                                                <i class="fa-solid fa-boxes-packing"></i>
                                                <span>Kiểm Đếm & Hoàn Tất Nhập Kho</span>
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </div>
                    </div>
                </div>

                <!-- Right Table (7 Cols) -->
                <div class="lg:col-span-7">
                    <div class="glass-panel rounded-2xl border border-white/10 overflow-hidden shadow-lg">
                        <div class="p-4 border-b border-white/10 flex items-center justify-between">
                            <h3 class="text-xs uppercase tracking-wider font-semibold text-brand-gold">
                                Danh Sách Bản Sách Điều Chuyển (${transfer.items.size()} cuốn)
                            </h3>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full text-left text-xs text-slate-300">
                                <thead class="bg-white/5 text-[11px] text-brand-gold uppercase tracking-wider border-b border-white/10">
                                    <tr>
                                        <th class="py-3 px-4 font-mono">Barcode</th>
                                        <th class="py-3 px-4">Tác Phẩm</th>
                                        <th class="py-3 px-4">Tình Trạng</th>
                                        <th class="py-3 px-4 text-right">Giá Niêm Yết</th>
                                    </tr>
                                </thead>
                                <tbody class="divide-y divide-white/5">
                                    <c:forEach var="item" items="${transfer.items}">
                                        <tr class="hover:bg-white/5 transition">
                                            <td class="py-3 px-4 font-mono font-bold text-amber-300">
                                                <div class="flex items-center gap-1.5">
                                                    <i class="fa-solid fa-barcode text-slate-500"></i>
                                                    <span>${item.bookCopy.copyCode}</span>
                                                </div>
                                            </td>
                                            <td class="py-3 px-4 font-semibold text-white">
                                                ${item.bookCopy.book.title}
                                            </td>
                                            <td class="py-3 px-4">
                                                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium bg-white/5 border border-white/10 text-slate-300">
                                                    ${item.bookCopy.bookCondition}
                                                </span>
                                            </td>
                                            <td class="py-3 px-4 text-right font-mono font-bold text-brand-gold">
                                                <fmt:formatNumber value="${item.bookCopy.price}" pattern="#,###"/> đ
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</body>
</html>
