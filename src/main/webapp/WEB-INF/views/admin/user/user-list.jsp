<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html class="dark" data-bs-theme="dark" lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Quản Lý Nhân Sự & Phân Quyền — OldBook</title>
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
    </style>
</head>
<body class="bg-reading-room min-h-screen relative flex flex-col justify-between py-6 px-4 md:px-8 lg:px-12 selection:bg-brand-gold selection:text-black">
    <div class="ambient-glow top-12 left-1/4"></div>
    <div class="ambient-glow bottom-20 right-10"></div>

    <div class="relative z-10 max-w-[1520px] w-full mx-auto flex flex-col xl:flex-row items-start gap-5">
        <!-- Unified Navigation Dock -->
        <jsp:include page="/WEB-INF/views/admin/common/nav-dock.jsp">
            <jsp:param name="active" value="users"/>
        </jsp:include>

        <!-- Main Display Frame -->
        <main class="flex-1 w-full glass-panel rounded-[32px] p-5 sm:p-7 shadow-glass-elevated border border-white/10 relative overflow-hidden">
            <!-- Top Nav & Breadcrumbs -->
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-white/5 mb-6">
                <div>
                    <div class="flex items-center gap-2 mb-1">
                        <span class="px-2.5 py-0.5 rounded-full bg-brand-gold/15 text-brand-gold text-[10px] font-semibold border border-brand-gold/30">
                            QUẢN TRỊ HỆ THỐNG
                        </span>
                    </div>
                    <h1 class="text-2xl sm:text-3xl font-serif font-bold text-white tracking-tight">
                        Quản Lý Nhân Sự & Phân Quyền
                    </h1>
                    <p class="text-xs text-slate-400 mt-1">Kiểm soát người dùng, phân cấp 4 vai trò hệ thống và phạm vi chi nhánh phụ trách.</p>
                </div>
                <div class="flex items-center gap-3">
                    <button onclick="document.getElementById('createUserModal').classList.remove('hidden')"
                            class="px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 hover:to-amber-600 text-slate-950 font-semibold text-xs tracking-wide uppercase transition shadow-glow-gold flex items-center gap-2">
                        <i class="fa-solid fa-user-plus"></i>
                        <span>Thêm Tài Khoản</span>
                    </button>
                </div>
            </div>

        <!-- Flash Messages -->
        <c:if test="${not empty successMessage}">
            <div class="mb-6 p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-sm flex items-center justify-between glass-panel">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-circle-check text-emerald-400 text-base"></i>
                    <span>${successMessage}</span>
                </div>
                <button onclick="this.parentElement.remove()" class="text-emerald-400/60 hover:text-emerald-300">
                    <i class="fa-solid fa-xmark"></i>
                </button>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="mb-6 p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-sm flex items-center justify-between glass-panel">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-circle-exclamation text-rose-400 text-base"></i>
                    <span>${errorMessage}</span>
                </div>
                <button onclick="this.parentElement.remove()" class="text-rose-400/60 hover:text-rose-300">
                    <i class="fa-solid fa-xmark"></i>
                </button>
            </div>
        </c:if>

        <!-- Search & Filter Card -->
        <div class="glass-panel rounded-2xl p-4 sm:p-5 mb-6 shadow-glass-elevated">
            <form action="/admin/users" method="get" class="grid grid-cols-1 sm:grid-cols-12 gap-3 items-end">
                <div class="sm:col-span-6">
                    <label class="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Từ khóa tìm kiếm</label>
                    <div class="relative">
                        <i class="fa-solid fa-magnifying-glass absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 text-xs"></i>
                        <input type="text" name="keyword" value="${keyword}"
                               placeholder="Tìm theo username, họ tên hoặc email..."
                               class="w-full pl-9 pr-3 py-2 rounded-xl text-xs glass-input focus:ring-0 placeholder-slate-500"/>
                    </div>
                </div>
                <div class="sm:col-span-3">
                    <label class="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Vai trò</label>
                    <select name="role" class="w-full px-3 py-2 rounded-xl text-xs glass-input focus:ring-0 text-slate-200">
                        <option value="" class="bg-slate-900 text-slate-300">— Tất cả vai trò —</option>
                        <c:forEach var="r" items="${roles}">
                            <option value="${r}" ${selectedRole == r ? 'selected' : ''} class="bg-slate-900 text-slate-200">${r}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="sm:col-span-3 flex gap-2">
                    <button type="submit"
                            class="flex-1 py-2 px-4 rounded-xl bg-slate-800 hover:bg-slate-700 border border-white/10 text-white font-medium text-xs transition flex items-center justify-center gap-1.5">
                        <i class="fa-solid fa-filter text-brand-gold"></i>
                        <span>Lọc</span>
                    </button>
                    <a href="/admin/users"
                       class="py-2 px-3 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white border border-white/5 text-xs transition flex items-center justify-center"
                       title="Đặt lại bộ lọc">
                        <i class="fa-solid fa-rotate-left"></i>
                    </a>
                </div>
            </form>
        </div>

        <!-- User Table Card -->
        <div class="glass-panel rounded-2xl overflow-hidden shadow-glass-elevated border border-white/10">
            <div class="overflow-x-auto">
                <table class="w-full text-left border-collapse">
                    <thead>
                        <tr class="border-b border-white/10 bg-white/[0.02] text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                            <th class="py-3.5 px-4 w-12 text-center">#</th>
                            <th class="py-3.5 px-4">Tài Khoản</th>
                            <th class="py-3.5 px-4">Họ Và Tên</th>
                            <th class="py-3.5 px-4">Email / SĐT</th>
                            <th class="py-3.5 px-4">Chi Nhánh</th>
                            <th class="py-3.5 px-4 text-center">Vai Trò</th>
                            <th class="py-3.5 px-4 text-center">Trạng Thái</th>
                            <th class="py-3.5 px-4 text-right">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-white/[0.06] text-xs">
                        <c:forEach var="user" items="${userPage.content}">
                            <tr class="hover:bg-white/[0.02] transition">
                                <td class="py-3.5 px-4 text-center text-slate-500 font-mono text-[11px]">${user.id}</td>
                                <td class="py-3.5 px-4">
                                    <div class="font-semibold text-white flex items-center gap-2">
                                        <div class="w-7 h-7 rounded-full bg-brand-gold/10 border border-brand-gold/20 flex items-center justify-center text-brand-gold text-[11px] font-bold uppercase">
                                            ${user.username.substring(0, 1)}
                                        </div>
                                        <span>${user.username}</span>
                                    </div>
                                </td>
                                <td class="py-3.5 px-4 text-slate-200 font-medium">
                                    ${user.fullName}
                                </td>
                                <td class="py-3.5 px-4 text-slate-400">
                                    <div>${user.email}</div>
                                    <c:if test="${not empty user.phone}">
                                        <div class="text-[11px] text-slate-500"><i class="fa-solid fa-phone text-[9px] mr-1"></i>${user.phone}</div>
                                    </c:if>
                                </td>
                                <td class="py-3.5 px-4">
                                    <c:choose>
                                        <c:when test="${not empty user.storeName}">
                                            <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-medium bg-white/5 border border-white/10 text-slate-300">
                                                <i class="fa-solid fa-shop text-[9px] text-brand-gold"></i>
                                                ${user.storeName}
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-slate-500 italic text-[11px]">— Toàn hệ thống —</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="py-3.5 px-4 text-center">
                                    <c:choose>
                                        <c:when test="${user.role == 'ADMIN'}">
                                            <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-rose-500/10 border border-rose-500/30 text-rose-300">
                                                <i class="fa-solid fa-shield-halved text-[9px]"></i> ADMIN
                                            </span>
                                        </c:when>
                                        <c:when test="${user.role == 'MANAGER'}">
                                            <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-purple-500/10 border border-purple-500/30 text-purple-300">
                                                <i class="fa-solid fa-user-tie text-[9px]"></i> MANAGER
                                            </span>
                                        </c:when>
                                        <c:when test="${user.role == 'STAFF'}">
                                            <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-sky-500/10 border border-sky-500/30 text-sky-300">
                                                <i class="fa-solid fa-id-badge text-[9px]"></i> STAFF
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/10 border border-emerald-500/30 text-emerald-300">
                                                <i class="fa-solid fa-user text-[9px]"></i> CUSTOMER
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="py-3.5 px-4 text-center">
                                    <c:choose>
                                        <c:when test="${user.active}">
                                            <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-medium bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">
                                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                                                Hoạt động
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-medium bg-rose-500/10 border border-rose-500/20 text-rose-400">
                                                <span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
                                                Bị khóa
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="py-3.5 px-4 text-right">
                                    <div class="inline-flex items-center gap-1.5">
                                        <!-- Đổi Role -->
                                        <button onclick="openChangeRoleModal('${user.id}', '${user.username}', '${user.role}')"
                                                class="w-7 h-7 rounded-lg bg-white/5 hover:bg-purple-500/20 border border-white/10 hover:border-purple-500/40 text-slate-300 hover:text-purple-300 flex items-center justify-center transition"
                                                title="Đổi vai trò">
                                            <i class="fa-solid fa-user-gear text-[11px]"></i>
                                        </button>
                                        <!-- Chỉnh sửa -->
                                        <a href="/admin/users/${user.id}/edit"
                                           class="w-7 h-7 rounded-lg bg-white/5 hover:bg-brand-gold/20 border border-white/10 hover:border-brand-gold/40 text-slate-300 hover:text-brand-gold flex items-center justify-center transition"
                                           title="Chỉnh sửa thông tin">
                                            <i class="fa-solid fa-pen-to-square text-[11px]"></i>
                                        </a>
                                        <!-- Khóa / Mở Khóa -->
                                        <form action="/admin/users/${user.id}/toggle-status" method="post" class="inline">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                            <c:choose>
                                                <c:when test="${user.active}">
                                                    <button type="submit"
                                                            onclick="return confirm('Bạn có chắc chắn muốn KHÓA tài khoản ${user.username}?')"
                                                            class="w-7 h-7 rounded-lg bg-white/5 hover:bg-rose-500/20 border border-white/10 hover:border-rose-500/40 text-slate-300 hover:text-rose-400 flex items-center justify-center transition"
                                                            title="Khóa tài khoản">
                                                        <i class="fa-solid fa-lock text-[11px]"></i>
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="submit"
                                                            onclick="return confirm('Mở khóa tài khoản ${user.username}?')"
                                                            class="w-7 h-7 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 text-emerald-300 flex items-center justify-center transition"
                                                            title="Mở khóa tài khoản">
                                                        <i class="fa-solid fa-lock-open text-[11px]"></i>
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty userPage.content}">
                            <tr>
                                <td colspan="8" class="py-12 text-center text-slate-500">
                                    <i class="fa-solid fa-users-slash text-3xl mb-3 text-slate-600 block"></i>
                                    <span>Không tìm thấy tài khoản nào phù hợp với bộ lọc.</span>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <!-- Pagination -->
            <c:if test="${userPage.totalPages > 1}">
                <div class="p-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-400">
                    <div>
                        Trang <span class="text-white font-medium">${userPage.number + 1}</span> / <span class="text-white font-medium">${userPage.totalPages}</span>
                        (Tổng <span class="text-brand-gold font-medium">${userPage.totalElements}</span> tài khoản)
                    </div>
                    <div class="flex items-center gap-1.5">
                        <c:forEach begin="0" end="${userPage.totalPages - 1}" var="i">
                            <a href="?page=${i}&keyword=${keyword}&role=${selectedRole}"
                               class="w-8 h-8 rounded-lg flex items-center justify-center font-medium transition ${userPage.number == i ? 'bg-gradient-to-r from-brand-gold to-amber-600 text-slate-950 font-bold shadow-glow-gold' : 'bg-white/5 hover:bg-white/10 text-slate-300 border border-white/5'}">
                                ${i + 1}
                            </a>
                        </c:forEach>
                    </div>
                </div>
            </c:if>
        </div>
        </main>
    </div>

    <!-- Modal Thêm Tài Khoản -->
    <div id="createUserModal" onclick="if(event.target === this) this.classList.add('hidden')" class="${openModal ? '' : 'hidden'} fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
        <div class="glass-panel w-full max-w-2xl rounded-2xl p-6 sm:p-8 shadow-glass-elevated border border-white/10 relative">
            <div class="flex items-center justify-between pb-4 mb-5 border-b border-white/10">
                <div class="flex items-center gap-2.5">
                    <div class="w-8 h-8 rounded-lg bg-brand-gold/10 border border-brand-gold/30 flex items-center justify-center text-brand-gold">
                        <i class="fa-solid fa-user-plus text-xs"></i>
                    </div>
                    <h3 class="font-serif font-bold text-lg text-white">Thêm Tài Khoản Mới</h3>
                </div>
                <button type="button" onclick="document.getElementById('createUserModal').classList.add('hidden')"
                        class="text-slate-400 hover:text-white transition">
                    <i class="fa-solid fa-xmark text-lg"></i>
                </button>
            </div>

            <form action="/admin/users/new" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                    <div>
                        <label class="block text-slate-400 font-medium mb-1 uppercase tracking-wider text-[11px]">
                            Tên đăng nhập <span class="text-rose-400">*</span>
                        </label>
                        <input type="text" name="username" value="${userForm.username}" required
                               placeholder="VD: nguyenvan_a"
                               class="w-full px-3 py-2 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500"/>
                    </div>
                    <div>
                        <label class="block text-slate-400 font-medium mb-1 uppercase tracking-wider text-[11px]">
                            Mật khẩu khởi tạo <span class="text-rose-400">*</span>
                        </label>
                        <input type="password" name="password" required minlength="6"
                               placeholder="Tối thiểu 6 ký tự"
                               class="w-full px-3 py-2 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500"/>
                    </div>
                    <div class="sm:col-span-2">
                        <label class="block text-slate-400 font-medium mb-1 uppercase tracking-wider text-[11px]">
                            Họ và Tên <span class="text-rose-400">*</span>
                        </label>
                        <input type="text" name="fullName" value="${userForm.fullName}" required
                               placeholder="VD: Nguyễn Văn A"
                               class="w-full px-3 py-2 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500"/>
                    </div>
                    <div>
                        <label class="block text-slate-400 font-medium mb-1 uppercase tracking-wider text-[11px]">
                            Email <span class="text-rose-400">*</span>
                        </label>
                        <input type="email" name="email" value="${userForm.email}" required
                               placeholder="email@example.com"
                               class="w-full px-3 py-2 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500"/>
                    </div>
                    <div>
                        <label class="block text-slate-400 font-medium mb-1 uppercase tracking-wider text-[11px]">
                            Số điện thoại
                        </label>
                        <input type="text" name="phone" value="${userForm.phone}"
                               placeholder="0987654321"
                               class="w-full px-3 py-2 rounded-xl glass-input focus:ring-0 text-slate-100 placeholder-slate-500"/>
                    </div>
                    <div>
                        <label class="block text-slate-400 font-medium mb-1 uppercase tracking-wider text-[11px]">
                            Vai trò hệ thống <span class="text-rose-400">*</span>
                        </label>
                        <select name="role" required class="w-full px-3 py-2 rounded-xl glass-input focus:ring-0 text-slate-200">
                            <c:forEach var="r" items="${roles}">
                                <option value="${r}" class="bg-slate-900 text-slate-200">${r}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div>
                        <label class="block text-slate-400 font-medium mb-1 uppercase tracking-wider text-[11px]">
                            Chi nhánh phụ trách
                        </label>
                        <select name="storeId" class="w-full px-3 py-2 rounded-xl glass-input focus:ring-0 text-slate-200">
                            <option value="" class="bg-slate-900 text-slate-400">— Toàn hệ thống (Không gán) —</option>
                            <c:forEach var="store" items="${stores}">
                                <option value="${store.id}" class="bg-slate-900 text-slate-200">${store.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="flex items-center justify-end gap-3 mt-6 pt-4 border-t border-white/10">
                    <button type="button" onclick="document.getElementById('createUserModal').classList.add('hidden')"
                            class="px-4 py-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white border border-white/5 text-xs transition">
                        Hủy
                    </button>
                    <button type="submit"
                            class="px-5 py-2 rounded-xl bg-gradient-to-r from-brand-gold to-amber-600 hover:from-amber-400 hover:to-amber-600 text-slate-950 font-semibold text-xs transition shadow-glow-gold flex items-center gap-2">
                        <i class="fa-solid fa-floppy-disk"></i>
                        <span>Tạo Tài Khoản</span>
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Modal Đổi Role Chung -->
    <div id="changeRoleModal" onclick="if(event.target === this) closeChangeRoleModal()" class="hidden fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
        <div class="glass-panel w-full max-w-md rounded-2xl p-6 shadow-glass-elevated border border-white/10 relative">
            <div class="flex items-center justify-between pb-3 mb-4 border-b border-white/10">
                <div class="flex items-center gap-2">
                    <div class="w-7 h-7 rounded-lg bg-purple-500/10 border border-purple-500/30 flex items-center justify-center text-purple-300">
                        <i class="fa-solid fa-user-gear text-xs"></i>
                    </div>
                    <h3 class="font-serif font-bold text-base text-white">Đổi Vai Trò</h3>
                </div>
                <button type="button" onclick="closeChangeRoleModal()" class="text-slate-400 hover:text-white transition">
                    <i class="fa-solid fa-xmark"></i>
                </button>
            </div>

            <form id="changeRoleForm" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <div class="mb-4">
                    <p class="text-xs text-slate-300 mb-3">Tài khoản: <span id="modalUsername" class="font-semibold text-brand-gold"></span></p>
                    <label class="block text-slate-400 font-medium mb-1.5 uppercase tracking-wider text-[11px]">
                        Chọn vai trò mới:
                    </label>
                    <select id="modalRoleSelect" name="role" class="w-full px-3 py-2 rounded-xl text-xs glass-input focus:ring-0 text-slate-200">
                        <c:forEach var="r" items="${roles}">
                            <option value="${r}" class="bg-slate-900 text-slate-200">${r}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="flex items-center justify-end gap-2.5 pt-3 border-t border-white/10">
                    <button type="button" onclick="closeChangeRoleModal()"
                            class="px-3.5 py-1.5 rounded-xl bg-white/5 hover:bg-white/10 text-slate-400 hover:text-white border border-white/5 text-xs transition">
                        Hủy
                    </button>
                    <button type="submit"
                            class="px-4 py-1.5 rounded-xl bg-gradient-to-r from-purple-500 to-indigo-600 hover:from-purple-400 hover:to-indigo-500 text-white font-medium text-xs transition shadow-lg flex items-center gap-1.5">
                        <i class="fa-solid fa-check"></i>
                        <span>Lưu Thay Đổi</span>
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openChangeRoleModal(userId, username, currentRole) {
            const form = document.getElementById('changeRoleForm');
            form.action = '/admin/users/' + userId + '/change-role';
            document.getElementById('modalUsername').textContent = username;
            document.getElementById('modalRoleSelect').value = currentRole;
            document.getElementById('changeRoleModal').classList.remove('hidden');
        }

        function closeChangeRoleModal() {
            document.getElementById('changeRoleModal').classList.add('hidden');
        }
    </script>
</body>
</html>
